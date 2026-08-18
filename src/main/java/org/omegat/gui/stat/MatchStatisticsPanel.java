/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2026 Stephan Pakebusch
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

package org.omegat.gui.stat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.statistics.IStatsConsumer;
import org.omegat.core.statistics.dso.MatchStatCounts;
import org.omegat.core.threads.Completion;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.filter.MatchRangeFilter;
import org.omegat.util.OStrings;

/**
 *
 * @author Aaron Madlon-Kay
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class MatchStatisticsPanel extends BaseMatchStatisticsPanel implements IStatsConsumer {

    /**
     * Number of rows in the final total match statistics table, including the
     * total row. Intermediate tables are smaller and get no filter buttons.
     */
    static final int FINAL_TABLE_ROWS = 8;

    /** Index of the filter button column, inserted before the segment counts. */
    static final int FILTER_COLUMN = 1;

    private volatile @Nullable Map<Integer, Integer> entryRowIndexes;
    private volatile String @Nullable [] lastHeaders;
    private volatile String @Nullable [][] lastData;
    private volatile @Nullable String lastTextData;

    public MatchStatisticsPanel(StatisticsWindow window) {
        super(window);
        setLayout(new BorderLayout());
    }

    /**
     * Restore the last scan of this session from the cache, if any.
     *
     * @return true if a cached result was restored
     */
    public boolean restoreFromCache() {
        Optional<MatchStatisticsCache.Snapshot> cached = MatchStatisticsCache.get();
        if (!cached.isPresent()) {
            return false;
        }
        MatchStatisticsCache.Snapshot snapshot = cached.get();
        if (!snapshot.getProjectRoot().equals(currentProjectRoot())) {
            // A scan of another project must never leak through; entry
            // numbers are only meaningful within the project they came from.
            return false;
        }
        entryRowIndexes = snapshot.getEntryRowIndexes();
        String textData = snapshot.getTextData();
        if (textData != null) {
            setTextData(textData);
        }
        setTable(snapshot.getHeaders(), snapshot.getData());
        return true;
    }

    @Override
    public void setEntryRowIndexes(Map<Integer, Integer> entryRowIndexes) {
        this.entryRowIndexes = entryRowIndexes;
    }

    @Override
    public void setTextData(String data) {
        lastTextData = data;
        super.setTextData(data);
    }

    @Override
    public void onComplete(Completion completion) {
        Map<Integer, Integer> rows = entryRowIndexes;
        String[] headers = lastHeaders;
        String[][] data = lastData;
        String projectRoot = currentProjectRoot();
        if (completion.isSuccess() && rows != null && headers != null && data != null
                && data.length == FINAL_TABLE_ROWS && projectRoot != null) {
            MatchStatisticsCache.store(headers, data, rows, lastTextData, projectRoot);
        } else if (!completion.isSuccess()) {
            // A failed or cancelled recalculation leaves the intermediate
            // table behind; fall back to the last complete result.
            restoreFromCache();
        }
        super.onComplete(completion);
    }

    private static @Nullable String currentProjectRoot() {
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()) {
            return null;
        }
        return project.getProjectProperties().getProjectRoot();
    }

    @Override
    public void appendTable(String title, String[] headers, String[][] data) {
        // Nothing
    }

    @Override
    public void setTable(final String[] headers, final String[][] data) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (data == null || data.length == 0) {
            return;
        }
        lastHeaders = headers;
        lastData = data;
        SwingUtilities.invokeLater(() -> {
            // A simpler table is first shown, then replaced with a fancier one,
            // so have to remove first.
            removeAll();
            add(createTablePanel(headers, data));
            revalidate();
            repaint();
        });
    }

    private Component createTablePanel(String[] headers, String[][] data) {
        Map<Integer, Integer> rows = entryRowIndexes;
        if (rows == null || data.length != FINAL_TABLE_ROWS) {
            return generateTableDisplay(null, headers, data);
        }
        String[] headersWithFilter = new String[headers.length + 1];
        headersWithFilter[0] = headers[0];
        headersWithFilter[FILTER_COLUMN] = "";
        System.arraycopy(headers, 1, headersWithFilter, FILTER_COLUMN + 1, headers.length - 1);
        FilterableTableModel model = new FilterableTableModel(data, rows);
        TitledTablePanel panel = generateTableDisplay(null, headersWithFilter, model);
        ButtonColumn buttonColumn = new ButtonColumn(model);
        TableColumn column = panel.table.getColumnModel().getColumn(FILTER_COLUMN);
        column.setCellRenderer(buttonColumn);
        column.setCellEditor(buttonColumn);
        int width = buttonColumn.getPreferredButtonWidth();
        column.setMinWidth(width);
        column.setMaxWidth(width);
        return panel;
    }

    /**
     * Map a displayed table row to the category row index of
     * {@link MatchStatCounts}. The displayed total table skips the
     * repetitions-from-other-files category (index 1) and appends a total row
     * that has no category.
     *
     * @param displayRow
     *            0-based displayed row, excluding the total row
     * @return category row index
     */
    static int categoryRowForDisplayRow(int displayRow) {
        if (displayRow == MatchStatCounts.ROW_REPETITIONS) {
            return MatchStatCounts.ROW_REPETITIONS;
        }
        return displayRow + 1;
    }

    private void applyFilter(int displayRow, String categoryLabel) {
        Map<Integer, Integer> rows = entryRowIndexes;
        if (rows == null) {
            return;
        }
        int categoryRow = categoryRowForDisplayRow(displayRow);
        Set<Integer> entries = rows.entrySet().stream().filter(e -> e.getValue() == categoryRow)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        if (entries.isEmpty()) {
            return;
        }
        IEditor editor = Core.getEditor();
        if (editor.getFilter() != null) {
            int answer = JOptionPane.showConfirmDialog(this,
                    OStrings.getString("STATSMATCH_FILTER_REPLACE_MESSAGE"),
                    OStrings.getString("STATSMATCH_FILTER_REPLACE_TITLE"), JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String label = categoryLabel.endsWith(":")
                ? categoryLabel.substring(0, categoryLabel.length() - 1).trim()
                : categoryLabel;
        editor.commitAndLeave(); // Otherwise, the current segment
                                 // being edited is lost
        editor.setFilter(new MatchRangeFilter(label, entries));
    }

    /**
     * Table model for the final table: the plain string data plus a virtual
     * filter button column inserted before the segment counts. Button cells
     * are editable so the button can be clicked; rows of empty categories and
     * the total row get no button.
     */
    private static class FilterableTableModel extends AbstractTableModel {

        private final String[][] data;
        private final boolean[] hasButton;

        FilterableTableModel(String[][] data, Map<Integer, Integer> entryRowIndexes) {
            this.data = data;
            hasButton = new boolean[data.length];
            for (int row = 0; row < data.length - 1; row++) {
                int categoryRow = categoryRowForDisplayRow(row);
                hasButton[row] = entryRowIndexes.containsValue(categoryRow);
            }
        }

        boolean hasButton(int row) {
            return hasButton[row];
        }

        String getCategoryLabel(int row) {
            return data[row][0];
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return data.length == 0 ? 0 : data[0].length + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == FILTER_COLUMN) {
                return "";
            }
            return data[rowIndex][columnIndex < FILTER_COLUMN ? columnIndex : columnIndex - 1];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == FILTER_COLUMN && hasButton(rowIndex);
        }
    }

    /**
     * Renderer and editor showing a filter button per category row.
     */
    private class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

        private final FilterableTableModel model;
        private final JButton rendererButton;
        private final JButton editorButton;
        private final JLabel emptyCell = new JLabel();
        private int editingRow;

        ButtonColumn(FilterableTableModel model) {
            this.model = model;
            String label = OStrings.getString("STATSMATCH_FILTER_BUTTON");
            rendererButton = new JButton(label);
            editorButton = new JButton(label);
            editorButton.addActionListener(e -> {
                int row = editingRow;
                fireEditingStopped();
                applyFilter(row, model.getCategoryLabel(row));
            });
        }

        int getPreferredButtonWidth() {
            return rendererButton.getPreferredSize().width + 4;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return model.hasButton(row) ? rendererButton : emptyCell;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            editingRow = row;
            return editorButton;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
