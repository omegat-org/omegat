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

package org.omegat.externalfinder.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.omegat.externalfinder.ExternalFinder;
import org.omegat.externalfinder.item.ExternalFinderConfiguration;
import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.externalfinder.item.ExternalFinderItem.SCOPE;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;

public class ExternalFinderPreferencesController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    private final boolean isProjectSpecific;
    private final ExternalFinderConfiguration originalConfig;

    private ExternalFinderPreferencesPanel panel;

    public ExternalFinderPreferencesController() {
        this(false, ExternalFinder.getGlobalConfig());
    }

    public ExternalFinderPreferencesController(boolean isProjectSpecific,
            ExternalFinderConfiguration originalConfig) {
        this.isProjectSpecific = isProjectSpecific;
        this.originalConfig = originalConfig;
    }

    @Override
    public Component getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    private void initGui() {
        panel = new ExternalFinderPreferencesPanel();
        JComponent editor = panel.prioritySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(4);
        }
        panel.addButton.addActionListener(e -> addItem());
        panel.removeButton.addActionListener(e -> removeSelection());
        panel.editButton.addActionListener(e -> editSelection());
        panel.prioritySpinner.addChangeListener(e -> {
            boolean changed = (Integer) panel.prioritySpinner.getValue() != originalConfig.getPriority();
            setRestartRequired(changed);
        });
        panel.itemTable.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
        Dimension tableSize = panel.itemTable.getPreferredSize();
        panel.itemTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.itemTable.getRowHeight() * MAX_ROW_COUNT));
        panel.itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    editSelection();
                }
            }
        });
        TableColumnSizer.autoSize(panel.itemTable, ItemColumn.CONTENTS.index, true);
        panel.globalOptionsPanel.setVisible(!isProjectSpecific);
    }

    private void addItem() {
        SCOPE scope = isProjectSpecific ? SCOPE.PROJECT : SCOPE.GLOBAL;
        ExternalFinderItemEditorController editor = new ExternalFinderItemEditorController(scope);
        if (editor.show(SwingUtilities.windowForComponent(panel))) {
            int row = panel.itemTable.getSelectedRow();
            ItemsTableModel model = (ItemsTableModel) panel.itemTable.getModel();
            int newRow = row >= 0 ? row + 1 : panel.itemTable.getRowCount();
            model.addItemAtRow(newRow, editor.getResult());
            panel.itemTable.setRowSelectionInterval(newRow, newRow);
        }
    }

    private void removeSelection() {
        int row = panel.itemTable.getSelectedRow();
        if (row >= 0) {
            ItemsTableModel model = (ItemsTableModel) panel.itemTable.getModel();
            model.removeItemAtRow(row);
        }
    }

    private void editSelection() {
        int row = panel.itemTable.getSelectedRow();
        if (row >= 0) {
            ItemsTableModel model = (ItemsTableModel) panel.itemTable.getModel();
            ExternalFinderItem item = model.getItemAtRow(row);
            ExternalFinderItemEditorController editor = new ExternalFinderItemEditorController(item);
            if (editor.show(SwingUtilities.windowForComponent(panel))) {
                model.setItemAtRow(row, editor.getResult());
            }
        }
    }

    private void onSelectionChanged() {
        int row = panel.itemTable.getSelectedRow();
        boolean enabled = row >= 0;
        panel.removeButton.setEnabled(enabled);
        panel.editButton.setEnabled(enabled);
    }

    @Override
    public void persist() {
        if (!isProjectSpecific) {
            ExternalFinder.setGlobalConfig(getResult());
            Preferences.setPreference(Preferences.EXTERNAL_FINDER_ALLOW_PROJECT_COMMANDS,
                    panel.projectSpecificCommandsCheckBox.isSelected());
        }
    }

    public ExternalFinderConfiguration getResult() {
        int priority = (Integer) panel.prioritySpinner.getValue();
        List<ExternalFinderItem> items = ((ItemsTableModel) panel.itemTable.getModel()).getItems();
        return new ExternalFinderConfiguration(priority, items);
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    protected void initFromPrefs() {
        panel.projectSpecificCommandsCheckBox
                .setSelected(Preferences.isPreference(Preferences.EXTERNAL_FINDER_ALLOW_PROJECT_COMMANDS));
        panel.prioritySpinner.setValue(originalConfig.getPriority());
        ItemsTableModel model = new ItemsTableModel(originalConfig.getItems());
        panel.itemTable.setModel(model);
        onSelectionChanged();
    }

    @Override
    public String toString() {
        return isProjectSpecific ? OStrings.getString("PREFS_TITLE_EXTERNALFINDER_PROJ_SPECIFIC")
                : OStrings.getString("PREFS_TITLE_EXTERNALFINDER");
    }

    enum ItemColumn {
        NAME(0, OStrings.getString("PREFS_EXTERNALFINDER_COL_NAME"), String.class),
        CONTENTS(1, OStrings.getString("PREFS_EXTERNALFINDER_COL_SUMMARY"), String.class),
        KEYSTROKE(2, OStrings.getString("PREFS_EXTERNALFINDER_COL_KEYSTROKE"), String.class),
        NOPOPUP(3, OStrings.getString("PREFS_EXTERNALFINDER_COL_POPUP"), Boolean.class);

        final int index;
        final String label;
        final Class<?> clazz;

        ItemColumn(int index, String label, Class<?> clazz) {
            this.index = index;
            this.label = label;
            this.clazz = clazz;
        }

        static ItemColumn get(int index) {
            return values()[index];
        }
    }

    @SuppressWarnings("serial")
    static class ItemsTableModel extends AbstractTableModel {

        private final List<ExternalFinderItem> data;

        ItemsTableModel(List<ExternalFinderItem> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return ItemColumn.values().length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (ItemColumn.get(columnIndex)) {
            case NAME:
                return getItemAtRow(rowIndex).getName();
            case CONTENTS:
                return getItemAtRow(rowIndex).getContentSummary();
            case KEYSTROKE:
                KeyStroke ks = getItemAtRow(rowIndex).getKeystroke();
                return ks == null ? null : StaticUIUtils.getKeyStrokeText(ks);
            case NOPOPUP:
                return !getItemAtRow(rowIndex).isNopopup();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int column) {
            return ItemColumn.get(column).label;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return ItemColumn.get(columnIndex).clazz;
        }

        ExternalFinderItem getItemAtRow(int row) {
            return data.get(row);
        }

        void removeItemAtRow(int row) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }

        void addItemAtRow(int row, ExternalFinderItem newItem) {
            data.add(row, newItem);
            fireTableRowsInserted(row, row);
        }

        public void setItemAtRow(int row, ExternalFinderItem newItem) {
            data.set(row, newItem);
            fireTableRowsUpdated(row, row);
        }

        public List<ExternalFinderItem> getItems() {
            return data;
        }
    }
}
