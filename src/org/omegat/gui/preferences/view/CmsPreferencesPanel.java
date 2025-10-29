package org.omegat.gui.preferences.view;

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

import org.omegat.cms.spi.CmsConnector;
import org.omegat.cms.dto.CmsTarget;
import org.omegat.cms.config.CmsXmlStore;
import org.omegat.core.data.CoreState;

/**
 * Preferences view panel for External CMS settings. Users can manage a list of
 * target projects per connector with a base URL and optional default page. This
 * panel is embedded by {@link CmsPreferencesController}.
 */
public class CmsPreferencesPanel extends JPanel {

    private final TargetsTableModel model = new TargetsTableModel();
    private final JTable table = new JTable(model);

    public CmsPreferencesPanel() {
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

    public List<CmsTarget> getTargets() {
        return new ArrayList<>(model.items);
    }

    public void setTargets(List<CmsTarget> items) {
        model.items.clear();
        if (items != null) {
            model.items.addAll(items);
        }
        model.fireTableDataChanged();
    }

    public void loadFromPrefs() {
        setTargets(CmsXmlStore.loadTargets());
    }

    public void saveToPrefs() {
        // Persist to XML store.
        CmsXmlStore.saveTargets(getTargets());
    }

    private void onAdd() {
        CmsTargetEditor dlg = new CmsTargetEditor(null);
        CmsTarget t = dlg.showDialog();
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
        CmsTarget src = model.items.get(idx);
        CmsTargetEditor dlg = new CmsTargetEditor(src);
        CmsTarget t = dlg.showDialog();
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
        final String[] COLS = { "Type", "Project", "Base URL", "Default Page" };
        final List<CmsTarget> items = new ArrayList<>();

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
            CmsTarget t = items.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return t.getConnectorId();
            case 1:
                return t.getProjectId();
            case 2:
                return t.getBaseUrl() == null ? "" : t.getBaseUrl();
            case 3:
                return t.getDefaultPage() == null ? "" : t.getDefaultPage();
            default:
                return "";
            }
        }
    }

    /** Simple modal editor dialog for a CMS target row. */
    class CmsTargetEditor {
        private final JDialog dialog;
        private final JComboBox<CmsConnector> typeCombo;
        private final JTextField projectField;
        private final JTextField baseUrlField;
        private final JTextField defaultPageField;
        private CmsTarget result;

        CmsTargetEditor(CmsTarget initial) {
            dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(CmsPreferencesPanel.this),
                    "Edit CMS Target", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            JPanel panel = new JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
            gc.insets = new java.awt.Insets(4, 4, 4, 4);
            gc.anchor = java.awt.GridBagConstraints.WEST;
            gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            int row = 0;

            typeCombo = new JComboBox<>();
            List<CmsConnector> connectors = CoreState.getInstance().getCmsConnectors().getAll();
            for (CmsConnector c : connectors) {
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
                CmsConnector sel = (CmsConnector) typeCombo.getSelectedItem();
                if (sel == null) {
                    return;
                }
                result = new CmsTarget(sel.getId(), projectField.getText().trim(),
                        emptyToNull(baseUrlField.getText().trim()),
                        emptyToNull(defaultPageField.getText().trim()));
                dialog.dispose();
            });
            cancel.addActionListener(e -> {
                result = null;
                dialog.dispose();
            });

            JPanel root = new JPanel(new BorderLayout());
            root.add(panel, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);
            dialog.getContentPane().add(root);
            dialog.pack();
            dialog.setLocationRelativeTo(CmsPreferencesPanel.this);

            if (initial != null) {
                // Preselect connector by id
                for (int i = 0; i < typeCombo.getItemCount(); i++) {
                    CmsConnector c = typeCombo.getItemAt(i);
                    if (Objects.equals(c.getId(), initial.getConnectorId())) {
                        typeCombo.setSelectedIndex(i);
                        break;
                    }
                }
                projectField.setText(initial.getProjectId());
                baseUrlField.setText(initial.getBaseUrl() == null ? "" : initial.getBaseUrl());
                defaultPageField.setText(initial.getDefaultPage() == null ? "" : initial.getDefaultPage());
            }
        }

        private String emptyToNull(String s) {
            return s == null || s.isEmpty() ? null : s;
        }

        CmsTarget showDialog() {
            dialog.setVisible(true);
            return result;
        }
    }
}
