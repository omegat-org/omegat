/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences.view;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.omegat.core.machinetranslators.MachineTranslators;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.TableColumnSizer;

/**
 * @author Aaron Madlon-Kay
 */
public class MachineTranslationPreferencesController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    private final Map<String, Boolean> providerStatus = new HashMap<>();
    private MachineTranslationPreferencesPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_MACHINE_TRANSLATION");
    }

    enum ProviderColumn {
        NAME(0, OStrings.getString("PREFS_MT_HEADER_PROVIDER"), String.class),
        CHECKBOX(1, OStrings.getString("PREFS_MT_HEADER_ENABLED"), Boolean.class);

        private final int index;
        private final String label;
        private final Class<?> clazz;

        ProviderColumn(int i, String label, Class<?> clazz) {
            this.index = i;
            this.label = label;
            this.clazz = clazz;
        }

        static ProviderColumn get(int i) {
            return values()[i];
        }
    }

    private void initGui() {
        panel = new MachineTranslationPreferencesPanel();
        panel.autoFetchCheckBox.addActionListener(e -> {
            panel.untranslatedOnlyCheckBox.setEnabled(panel.autoFetchCheckBox.isSelected());
        });
        Dimension tableSize = panel.mtProviderTable.getPreferredSize();
        panel.mtProviderTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.mtProviderTable.getRowHeight() * MAX_ROW_COUNT));
        TableColumnSizer.autoSize(panel.mtProviderTable, ProviderColumn.NAME.index, true);
        panel.mtProviderTable.getSelectionModel().addListSelectionListener(e -> updateEnabledness());
        panel.mtProviderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int row = panel.mtProviderTable.rowAtPoint(e.getPoint());
                    getProviderAtRow(row).ifPresent(provider -> {
                        if (provider.isConfigurable()) {
                            showProviderConfigUI(provider);
                        }
                    });
                }
            }
        });
        panel.configureButton.addActionListener(e -> {
            getSelectedProvider().ifPresent(this::showProviderConfigUI);
        });
        panel.autoFetchCheckBox.addActionListener(
                e -> panel.untranslatedOnlyCheckBox.setSelected(panel.autoFetchCheckBox.isSelected()));
    }

    void updateEnabledness() {
        boolean enabled = getSelectedProvider().map(IMachineTranslation::isConfigurable).orElse(false);
        panel.configureButton.setEnabled(enabled);
    }

    Optional<IMachineTranslation> getSelectedProvider() {
        int row = panel.mtProviderTable.getSelectedRow();
        return getProviderAtRow(row);
    }

    Optional<IMachineTranslation> getProviderAtRow(int row) {
        try {
            ProvidersTableModel model = (ProvidersTableModel) panel.mtProviderTable.getModel();
            return Optional.of(model.getProviderAt(row));
        } catch (IndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    void showProviderConfigUI(IMachineTranslation provider) {
        provider.showConfigurationUI(SwingUtilities.windowForComponent(panel));
    }

    @Override
    protected void initFromPrefs() {
        boolean mtAutoFetch = Preferences.isPreference(Preferences.MT_AUTO_FETCH);
        panel.autoFetchCheckBox.setSelected(mtAutoFetch);
        panel.untranslatedOnlyCheckBox.setSelected(Preferences.isPreference(Preferences.MT_ONLY_UNTRANSLATED));
        panel.untranslatedOnlyCheckBox.setEnabled(mtAutoFetch);
        List<IMachineTranslation> mtProviders = MachineTranslators.getMachineTranslators();
        mtProviders.stream().forEach(p -> providerStatus.put(p.getName(), p.isEnabled()));
        panel.mtProviderTable.setModel(new ProvidersTableModel(mtProviders));
        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        panel.autoFetchCheckBox.setSelected(false);
        panel.untranslatedOnlyCheckBox.setSelected(false);
        panel.untranslatedOnlyCheckBox.setEnabled(false);
        List<IMachineTranslation> mtProviders = MachineTranslators.getMachineTranslators();
        mtProviders.stream().forEach(p -> providerStatus.put(p.getName(), false));
        panel.mtProviderTable.setModel(new ProvidersTableModel(mtProviders));
        updateEnabledness();
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.MT_AUTO_FETCH, panel.autoFetchCheckBox.isSelected());
        Preferences.setPreference(Preferences.MT_ONLY_UNTRANSLATED, panel.untranslatedOnlyCheckBox.isSelected());
        MachineTranslators.getMachineTranslators().stream().forEach(p -> {
            Boolean status = providerStatus.get(p.getName());
            if (status != null) {
                p.setEnabled(status);
            }
        });
    }

    @SuppressWarnings("serial")
    class ProvidersTableModel extends AbstractTableModel {

        private final List<IMachineTranslation> mtProviders;

        ProvidersTableModel(List<IMachineTranslation> mtProviders) {
            this.mtProviders = mtProviders;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return ProviderColumn.get(column) == ProviderColumn.CHECKBOX;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (ProviderColumn.get(column) == ProviderColumn.CHECKBOX) {
                providerStatus.put(mtProviders.get(row).getName(), (Boolean) aValue);
            }
        }

        @Override
        public int getColumnCount() {
            return ProviderColumn.values().length;
        }

        @Override
        public String getColumnName(int column) {
            return ProviderColumn.get(column).label;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (ProviderColumn.get(column)) {
            case CHECKBOX:
                return providerStatus.get(mtProviders.get(row).getName());
            case NAME:
                return mtProviders.get(row).getName();
            default:
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getRowCount() {
            return mtProviders.size();
        }

        @Override
        public java.lang.Class<?> getColumnClass(int columnIndex) {
            return ProviderColumn.get(columnIndex).clazz;
        };

        public IMachineTranslation getProviderAt(int row) {
            return mtProviders.get(row);
        }
    }
}
