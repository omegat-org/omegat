/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.omegat.gui.editor.CsvExportOptions.Scope;
import org.omegat.util.OStrings;

/**
 * Accessory of the CSV export file chooser: segment scope, whether the
 * editor's current filter and display order apply, the columns with their
 * export order, and the embedded {@link CsvFormatOptionsPanel}. Preferences
 * provide the initial state. Applying the display order is only offered for
 * the current document and, while a filter is active, only together with the
 * filter: the order of the filtered-out segments would be undefined
 * otherwise.
 * <p>
 * Column order is edited keyboard-friendly: the list is a table (arrow keys
 * move, space toggles the checkbox), the move buttons work on the selected
 * row and Alt+Up/Down does the same without leaving the table.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
final class CsvExportOptionsPanel extends JPanel {

    private final boolean filterActive;
    private final JRadioButton scopeProject = new JRadioButton(
            OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_SCOPE_PROJECT"));
    private final JRadioButton scopeCurrentFile = new JRadioButton(
            OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_SCOPE_CURRENT_FILE"));
    private final JCheckBox applyFilter = new JCheckBox(
            OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_APPLY_FILTER"));
    private final JCheckBox applySort = new JCheckBox(
            OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_APPLY_SORT"));
    private final ColumnTableModel columnModel;
    private final JTable columnTable;
    private final CsvFormatOptionsPanel formatPanel = new CsvFormatOptionsPanel();

    CsvExportOptionsPanel(boolean filterActive) {
        this.filterActive = filterActive;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        CsvExportOptions initial = CsvExportOptions.loadFromPreferences();

        add(leftAligned(new JLabel(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_SCOPE"))));
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(scopeProject);
        scopeGroup.add(scopeCurrentFile);
        (initial.getScope() == Scope.PROJECT ? scopeProject : scopeCurrentFile).setSelected(true);
        scopeProject.addActionListener(e -> updateCheckboxStates());
        scopeCurrentFile.addActionListener(e -> updateCheckboxStates());
        add(leftAligned(scopeProject));
        add(leftAligned(scopeCurrentFile));

        applyFilter.setSelected(initial.isApplyFilter());
        applyFilter.addActionListener(e -> updateCheckboxStates());
        applySort.setSelected(initial.isApplySort());
        applySort.setToolTipText(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_APPLY_SORT_TOOLTIP"));
        add(leftAligned(applyFilter));
        add(leftAligned(applySort));

        add(leftAligned(new JLabel(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_COLUMNS"))));
        columnModel = new ColumnTableModel(initial.getColumnOrder());
        columnTable = createColumnTable();
        JScrollPane columnScroll = new JScrollPane(columnTable);
        columnScroll.setPreferredSize(new Dimension(220,
                columnTable.getRowHeight() * columnModel.getRowCount() + 4));
        JPanel columns = new JPanel();
        columns.setLayout(new BoxLayout(columns, BoxLayout.X_AXIS));
        columns.add(columnScroll);
        columns.add(createMoveButtons());
        add(leftAligned(columns));

        add(leftAligned(formatPanel));
        updateCheckboxStates();
    }

    private JTable createColumnTable() {
        JTable table = new JTable(columnModel);
        table.setTableHeader(null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getAccessibleContext()
                .setAccessibleName(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_COLUMNS"));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggleColumn");
        table.getActionMap().put("toggleColumn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    columnModel.setValueAt(!(Boolean) columnModel.getValueAt(row, 0), row, 0);
                }
            }
        });
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), "moveColumnUp");
        table.getActionMap().put("moveColumnUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(-1);
            }
        });
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), "moveColumnDown");
        table.getActionMap().put("moveColumnDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(1);
            }
        });
        return table;
    }

    private Component createMoveButtons() {
        JButton up = moveButton("GUI_EDITORWINDOW_EXPORT_CSV_COL_UP", "▲", -1);
        JButton down = moveButton("GUI_EDITORWINDOW_EXPORT_CSV_COL_DOWN", "▼", 1);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(up);
        buttons.add(Box.createVerticalStrut(5));
        buttons.add(down);
        buttons.add(Box.createVerticalGlue());
        return buttons;
    }

    private JButton moveButton(String nameKey, String glyph, int direction) {
        JButton button = new JButton(glyph);
        String name = OStrings.getString(nameKey);
        button.setToolTipText(name);
        button.getAccessibleContext().setAccessibleName(name);
        button.addActionListener(e -> moveSelected(direction));
        return button;
    }

    private void moveSelected(int direction) {
        int row = columnTable.getSelectedRow();
        int to = row + direction;
        if (row < 0 || to < 0 || to >= columnModel.getRowCount()) {
            return;
        }
        columnModel.move(row, to);
        columnTable.setRowSelectionInterval(to, to);
        columnTable.scrollRectToVisible(columnTable.getCellRect(to, 0, true));
    }

    private static Component leftAligned(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    private void updateCheckboxStates() {
        applyFilter.setEnabled(filterActive);
        applySort.setEnabled(scopeCurrentFile.isSelected() && (!filterActive || applyFilter.isSelected()));
    }

    /** The chosen options; disabled checkboxes count as not applied. */
    CsvExportOptions getOptions() {
        return new CsvExportOptions(scopeProject.isSelected() ? Scope.PROJECT : Scope.CURRENT_FILE,
                applyFilter.isEnabled() && applyFilter.isSelected(),
                applySort.isEnabled() && applySort.isSelected(), columnModel.toColumnOrder(),
                formatPanel.getOptions());
    }

    /** Columns in export order; first column toggles the export, second shows the name. */
    private static final class ColumnTableModel extends AbstractTableModel {
        private final List<CsvColumn> order = new ArrayList<>();
        private final List<Boolean> selected = new ArrayList<>();

        ColumnTableModel(Map<CsvColumn, Boolean> columnOrder) {
            columnOrder.forEach((column, on) -> {
                order.add(column);
                selected.add(on);
            });
        }

        Map<CsvColumn, Boolean> toColumnOrder() {
            Map<CsvColumn, Boolean> columnOrder = new LinkedHashMap<>();
            for (int i = 0; i < order.size(); i++) {
                columnOrder.put(order.get(i), selected.get(i));
            }
            return columnOrder;
        }

        void move(int from, int to) {
            order.add(to, order.remove(from));
            selected.add(to, selected.remove(from));
            fireTableRowsUpdated(Math.min(from, to), Math.max(from, to));
        }

        @Override
        public int getRowCount() {
            return order.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == 0 ? selected.get(rowIndex) : order.get(rowIndex).getDisplayName();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                selected.set(rowIndex, Boolean.TRUE.equals(aValue));
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
