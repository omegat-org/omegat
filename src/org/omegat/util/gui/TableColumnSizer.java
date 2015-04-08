/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.util.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class TableColumnSizer {
    
    private int[] optimalColWidths;
    private int remainderColReferenceWidth = -1;
    private boolean didManuallyAdjustCols;
    private int remainderColumn = 0;
    private final boolean fitTableToWidth;
    
    private boolean didApplySizes;
    
    private final JTable table;
    private final List<ActionListener> listeners = new ArrayList<ActionListener>();
    
    /**
     * Automatically optimize the column widths of a table. The {@link #remainderColumn}
     * is the index of the column that will receive additional space left over after
     * all other columns have been optimally sized. Usually this should be the largest
     * column in the table.
     * <p>
     * When {@link #fitTableToWidth} is false:
     * <p>
     * All columns will be optimally sized all the time (except the {@link #remainderColumn}
     * which may be larger) even if the table then exceeds its parent's width.
     * <p>
     * When {@link #fitTableToWidth} is true:
     * <p>
     * Columns will only be optimized if doing so results in the {@link #remainderColumn}
     * receiving more space than it would under {@link JTable#AUTO_RESIZE_SUBSEQUENT_COLUMNS}-
     * resizing. Resizing behavior will fall back to {@link JTable#AUTO_RESIZE_SUBSEQUENT_COLUMNS}
     * under some threshold below which the latter is better.
     * <p>
     * The result of this is that when widening the table, after the threshold the
     * columns will snap into their optimal size and all additional space goes to the
     * {@link #remainderColumn}.
     * <p>
     * Also note that automatic sizing will be disabled if the user manually adjusts column widths.
     *  
     * @param table
     * @param remainderColumn
     * @param fitTableToWidth
     * @return 
     */
    public static TableColumnSizer autoSize(JTable table, int remainderColumn, boolean fitTableToWidth) {
        TableColumnSizer colSizer = new TableColumnSizer(table, remainderColumn, fitTableToWidth);
        colSizer.init();
        return colSizer;
    }
    
    private TableColumnSizer(JTable table, int remainderColumn, boolean fitTableToWidth) {
        this.table = table;
        this.remainderColumn = Math.max(remainderColumn, 0);
        this.fitTableToWidth = fitTableToWidth;
    }
    
    /**
     * Add various listeners to keep the columns in sync with the table's
     * current width.
     */
    private void init() {
        table.getColumnModel().addColumnModelListener(colListener);
        table.getModel().addTableModelListener(modelListener);
        table.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Object oldVal = evt.getOldValue();
                Object newVal = evt.getNewValue();
                if (newVal != null && newVal.equals(evt.getOldValue())) {
                    return;
                }
                if (evt.getPropertyName().equals("columnModel")) {
                    if (newVal != null && newVal instanceof TableColumnModel) {
                        ((TableColumnModel) newVal).addColumnModelListener(colListener);
                    }
                    if (oldVal != null && oldVal instanceof TableColumnModel) {
                        ((TableColumnModel) oldVal).removeColumnModelListener(colListener);
                    }
                } else if (evt.getPropertyName().equals("model")) {
                    if (newVal != null  && newVal instanceof TableModel) {
                        ((TableModel) newVal).addTableModelListener(modelListener);
                    }
                    if (oldVal != null && oldVal instanceof TableModel) {
                        ((TableModel) oldVal).removeTableModelListener(modelListener);
                    }
                }
            }
        });
        table.getParent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustTableColumns();
            } 
        });
    }
    
    TableModelListener modelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            reset();
            adjustTableColumns();
        }
    };
    
    TableColumnModelListener colListener = new TableColumnModelListener() {
        @Override
        public void columnAdded(TableColumnModelEvent e) {
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            TableColumn col = table.getTableHeader().getResizingColumn();
            if (col != null) {
                didManuallyAdjustCols = true;
                adjustTableColumns();
            }
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            if (optimalColWidths != null) {
                int from = optimalColWidths[e.getFromIndex()];
                int to = optimalColWidths[e.getToIndex()];
                optimalColWidths[e.getFromIndex()] = to;
                optimalColWidths[e.getToIndex()] = from;
            }
            adjustTableColumns();
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
        }
    };
    
    /**
     * Calculate the width that the {@link #remainderColumn} would get under
     * {@link JTable#AUTO_RESIZE_SUBSEQUENT_COLUMNS}. The result is cached.
     */
    private void calculateRemainderColReferenceWidth() {
        if (remainderColReferenceWidth != -1) {
            return;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        remainderColReferenceWidth = table.getColumnModel().getColumn(remainderColumn).getWidth();
    }
    
    /**
     * Calculate each column's ideal width, based on header and cells.
     * Results are cached.
     * See: https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
     */
    private void calculateOptimalColWidths() {
        if (optimalColWidths != null) {
            return;
        }
        optimalColWidths = new int[table.getColumnCount()];
        
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn col = table.getColumnModel().getColumn(column);
            int preferredWidth = col.getMinWidth();
            int maxWidth = col.getMaxWidth();

            int startRow = table.getTableHeader() == null ? 0 : -1;
            for (int row = startRow; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer;
                Component c;
                int margin = 5;
                if (row == -1) {
                    cellRenderer = col.getHeaderRenderer();
                    if (cellRenderer == null) {
                        cellRenderer = col.getCellRenderer();
                    }
                    if (cellRenderer == null) {
                        // Headers are usually Strings
                        cellRenderer = table.getDefaultRenderer(String.class);
                    }
                    c = cellRenderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, column);
                    // Add somewhat arbitrary margin to header because it gets truncated at a smaller width
                    // than a regular cell does (Windows LAF more than OS X LAF).
                    margin = 10;
                } else {
                    cellRenderer = table.getCellRenderer(row, column);
                    c = table.prepareRenderer(cellRenderer, row, column);
                }
                
                c.setBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width + margin;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            optimalColWidths[column] = preferredWidth;
        }
    }
    
    /**
     * Get the new suggested width for the {@link #remainderColumn}, based on
     * what is left over after all other columns get their optimal widths.
     */
    private int calculateProposedRemainderColWidth() {
        int otherCols = 0;
        for (int i = 0; i < optimalColWidths.length; i++) {
            if (i == remainderColumn) {
                continue;
            }
            otherCols += optimalColWidths[i];
        }
        
        return table.getParent().getWidth() - otherCols;
    }
    
    /**
     * Reset any state that relies on the contents of the table.
     */
    public void reset() {
        optimalColWidths = null;
        remainderColReferenceWidth = -1;
        didApplySizes = false;
    }
    
    public void setRestoreAutoSizing() {
        didManuallyAdjustCols = false;
        didApplySizes = false;
    }
    
    /**
     * Adjust the columns of the table.
     * 
     * If possible, this optimally sizes the columns such that columns greater
     * than 0 are only as big as necessary, and the rest of the space goes to
     * column 0.
     * 
     * This auto-sizing only happens if it represents an improvement over the
     * default sizing (gives more space to the {@link #remainderColumn} than
     * it would get with {@link JTable#AUTO_RESIZE_SUBSEQUENT_COLUMNS}),
     * and only if the user has not manually adjusted column widths.
     */
    public void adjustTableColumns() {
        if (table.getColumnCount() == 0) {
            return;
        }
        
        calculateOptimalColWidths();
        
        ensureTableResizeMode(fitTableToWidth ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF);
        
        int proposedRemainderWidth = calculateProposedRemainderColWidth();
                        
        if (shouldAutoSize(proposedRemainderWidth)) {
            doAutoSize(proposedRemainderWidth);
        } else if (didApplySizes) {
            undoAutoSize();
        }
        
        notifyListeners();
    }
    
    private void doAutoSize(int proposedRemainderWidth) {
        
        if (!fitTableToWidth) {
            int width = Math.max(proposedRemainderWidth, optimalColWidths[remainderColumn]);
            table.getColumnModel().getColumn(remainderColumn).setPreferredWidth(width);
        }
        
        if (didApplySizes) {
            return;
        }

        for (int width, i = 0; i < optimalColWidths.length; i++) {
            width = optimalColWidths[i];
            TableColumn col = table.getColumnModel().getColumn(i);
            if (i == remainderColumn) {
                continue;
            }
            if (fitTableToWidth) {
                col.setMaxWidth(width);
            }
            col.setPreferredWidth(width);
        }
        didApplySizes = true;
    }
    
    private void undoAutoSize() {
        if (!didApplySizes) {
            return;
        }
        
        if (!fitTableToWidth) {
            didApplySizes = false;
            return;
        }
        
        for (int i = 0; i < optimalColWidths.length; i++) {
            if (i == remainderColumn) {
                continue;
            }
            TableColumn col = table.getColumnModel().getColumn(i);
            // For some reason the column will "jump" to a larger size if we restore
            // the max to Integer.MAX_VALUE. This doesn't happen with Short.MAX_VALUE.
            col.setMaxWidth(Short.MAX_VALUE);
        }
        didApplySizes = false;
    }
    
    /**
     * Set table mode if it isn't already in the desired mode.
     */
    private void ensureTableResizeMode(int mode) {
        if (table.getAutoResizeMode() == mode) {
            return;
        }
        table.setAutoResizeMode(mode);
    }
    
    /**
     * Decide if we should apply optimized column sizes given the proposed
     * {@link #remainderColumn} width.
     * 
     * @param proposedRemainderWidth
     * @return 
     */
    private boolean shouldAutoSize(int proposedRemainderWidth) {
        if (didManuallyAdjustCols) {
            return false;
        }
        if (!fitTableToWidth) {
            return true;
        }
        if (proposedRemainderWidth >= optimalColWidths[remainderColumn]) {
            return true;
        }
        calculateRemainderColReferenceWidth();
        return proposedRemainderWidth >= remainderColReferenceWidth;
    }
    
    public void addColumnAdjustmentListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }
    
    public void removeColumnAdjustmentListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "columnsAdjusted");
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }
}
