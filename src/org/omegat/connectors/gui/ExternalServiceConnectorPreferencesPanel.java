/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.connectors.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.jspecify.annotations.Nullable;
import org.omegat.connectors.spi.IExternalServiceConnector;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.config.ExternalConnectorXmlStore;
import org.omegat.core.data.CoreState;

/**
 * Preferences view panel for External Service settings. Users can manage a list of
 * target projects per connector with a base URL and optional default page. This
 * panel is embedded by {@link ExternalServiceConnectorPreferencesController}.
 */
public class ExternalServiceConnectorPreferencesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final TargetsTableModel model = new TargetsTableModel();
    private final JTable table = new JTable(model);

    public ExternalServiceConnectorPreferencesPanel() {
        super(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        buttons.add(addButton);
        JButton editButton = new JButton("Edit");
        buttons.add(editButton);
        JButton removeButton = new JButton("Remove");
        buttons.add(removeButton);
        add(buttons, BorderLayout.SOUTH);

        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
        removeButton.addActionListener(e -> onRemove());
    }

    public List<ServiceTarget> getTargets() {
        return new ArrayList<>(model.items);
    }

    public void setTargets(List<ServiceTarget> items) {
        model.items.clear();
        model.items.addAll(items);
        model.fireTableDataChanged();
    }

    public void loadFromPrefs() {
        setTargets(ExternalConnectorXmlStore.loadTargets());
    }

    public void saveToPrefs() {
        // Persist to XML store.
        ExternalConnectorXmlStore.saveTargets(getTargets());
    }

    private void onAdd() {
        ExternalServiceTargetEditor dlg = new ExternalServiceTargetEditor(null);
        ServiceTarget t = dlg.showDialog();
        if (t != null) {
            model.items.add(t);
            model.fireTableDataChanged();
        }
    }

    private void onEdit() {
        int idx = table.getSelectedRow();
        if (idx < 0) {
            return;
        }
        ServiceTarget src = model.items.get(idx);
        ExternalServiceTargetEditor dlg = new ExternalServiceTargetEditor(src);
        ServiceTarget t = dlg.showDialog();
        if (t != null) {
            model.items.set(idx, t);
            model.fireTableRowsUpdated(idx, idx);
        }
    }

    private void onRemove() {
        int idx = table.getSelectedRow();
        if (idx >= 0) {
            model.items.remove(idx);
            model.fireTableDataChanged();
        }
    }

    static class TargetsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        final String[] COLS = { "Type", "Project", "Base URL", "Default Page" };
        final List<ServiceTarget> items = new ArrayList<>();

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ServiceTarget t = items.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return t.getConnectorId();
            case 1:
                return t.getProjectId();
            case 2:
                return t.getBaseUrl();
            case 3:
                return t.getDefaultPage();
            default:
                return "";
            }
        }
    }

    /** Simple modal editor dialog for a External Service Integration target row. */
    class ExternalServiceTargetEditor {
        private final JDialog dialog;
        private final JComboBox<IExternalServiceConnector> typeCombo;
        private final JTextField projectField;
        private final JTextField baseUrlField;
        private final JTextField defaultPageField;
        private @Nullable ServiceTarget result;

        ExternalServiceTargetEditor(@Nullable ServiceTarget initial) {
            dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(ExternalServiceConnectorPreferencesPanel.this),
                    "Edit External Service Integration", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            JPanel panel = new JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
            gc.insets = new java.awt.Insets(4, 4, 4, 4);
            gc.anchor = java.awt.GridBagConstraints.WEST;
            gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            int row = 0;

            typeCombo = new JComboBox<>();
            List<IExternalServiceConnector> connectors = CoreState.getInstance().getExternalConnectorsManager().getAll();
            for (IExternalServiceConnector c : connectors) {
                typeCombo.addItem(c);
            }

            projectField = new JTextField(20);
            baseUrlField = new JTextField(25);
            defaultPageField = new JTextField(20);

            // Row: Type
            gc.gridx = 0;
            gc.gridy = row;
            panel.add(new JLabel("Type"), gc);
            gc.gridx = 1;
            gc.gridy = row;
            gc.weightx = 1;
            panel.add(typeCombo, gc);
            gc.weightx = 0;
            row++;
            // Row: Project
            gc.gridx = 0;
            gc.gridy = row;
            panel.add(new JLabel("Project"), gc);
            gc.gridx = 1;
            gc.gridy = row;
            gc.weightx = 1;
            panel.add(projectField, gc);
            gc.weightx = 0;
            row++;
            // Row: Base URL
            gc.gridx = 0;
            gc.gridy = row;
            panel.add(new JLabel("Base URL"), gc);
            gc.gridx = 1;
            gc.gridy = row;
            gc.weightx = 1;
            panel.add(baseUrlField, gc);
            gc.weightx = 0;
            row++;
            // Row: Default Page
            gc.gridx = 0;
            gc.gridy = row;
            panel.add(new JLabel("Default Page"), gc);
            gc.gridx = 1;
            gc.gridy = row;
            gc.weightx = 1;
            panel.add(defaultPageField, gc);
            gc.weightx = 0;

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            buttons.add(ok);
            buttons.add(cancel);

            ok.addActionListener(e -> {
                IExternalServiceConnector sel = (IExternalServiceConnector) typeCombo.getSelectedItem();
                if (sel == null) {
                    return;
                }
                var projectId = projectField.getText().trim();
                if (projectId.isEmpty()) {
                    return;
                }
                var baseUrl = baseUrlField.getText().trim();
                if (baseUrl.isEmpty()) {
                    return;
                }
                result = new ServiceTarget(sel.getId(), projectId, baseUrl, defaultPageField.getText().trim());
                dialog.dispose();
            });
            cancel.addActionListener(e -> {
                result = null;
                dialog.dispose();
            });
            typeCombo.addActionListener(e -> {
                IExternalServiceConnector connector = (IExternalServiceConnector) typeCombo.getSelectedItem();
                if (connector != null && connector.getDefaultBaseUrl() != null) {
                    baseUrlField.setText(connector.getDefaultBaseUrl());
                }
            });
            JPanel root = new JPanel(new BorderLayout());
            root.add(panel, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);
            dialog.getContentPane().add(root);
            dialog.pack();
            dialog.setLocationRelativeTo(ExternalServiceConnectorPreferencesPanel.this);

            if (initial != null) {
                // Preselect connector by id
                for (int i = 0; i < typeCombo.getItemCount(); i++) {
                    IExternalServiceConnector c = typeCombo.getItemAt(i);
                    if (Objects.equals(c.getId(), initial.getConnectorId())) {
                        typeCombo.setSelectedIndex(i);
                        break;
                    }
                }
                projectField.setText(initial.getProjectId());
                baseUrlField.setText(initial.getBaseUrl());
                defaultPageField.setText(initial.getDefaultPage());
            }
        }

        @Nullable
        ServiceTarget showDialog() {
            dialog.setVisible(true);
            return result;
        }
    }
}
