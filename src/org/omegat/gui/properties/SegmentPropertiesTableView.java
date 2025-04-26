/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.omegat.core.Core;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.TableColumnSizer;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * A table-based view of key=value properties of the current segment
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class SegmentPropertiesTableView implements ISegmentPropertiesView {

    private SegmentPropertiesArea parent;
    private FlashingTable table;
    private PropertiesTableModel model;
    private int mouseoverRow = -1;
    private int mouseoverCol = -1;

    public SegmentPropertiesArea getParent() {
        return parent;
    }

    public int getMouseoverRow() {
        return mouseoverRow;
    }

    public int getMouseoverCol() {
        return mouseoverCol;
    }

    @Override
    public void install(final SegmentPropertiesArea parent) {
        this.parent = parent;
        model = new PropertiesTableModel(this);
        table = new FlashingTable(model);
        table.setName("SegmentPropertiesTable");
        table.setForeground(parent.getScrollPane().getForeground());
        table.setBackground(parent.getScrollPane().getBackground());
        final MouseListener contextMenuListener = new SegmentPropertiesMouseAdapter(parent);
        table.addMouseListener(contextMenuListener);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setGridColor(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setCellRenderer(new SingleLineCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new MultilineCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new SingleLineCellRenderer());
        DataTableStyling.applyFont(table, Core.getMainWindow().getApplicationFont());
        TableColumnSizer.autoSize(table, 1, true).addColumnAdjustmentListener(e ->
                adjustRowHeights());
        table.addMouseListener(mouseAdapter);
        table.addMouseMotionListener(mouseAdapter);
        parent.getScrollPane().setViewportView(table);
    }

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (mouseoverCol == 2) {
                parent.showContextMenu(SwingUtilities.convertPoint(table, e.getPoint(), parent.getScrollPane()));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateRollover();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateRollover();
        }

        private void updateRollover() {
            Point point = table.getMousePosition();
            int oldRow = mouseoverRow;
            int oldCol = mouseoverCol;
            int newRow = point == null ? -1 : table.rowAtPoint(point);
            int newCol = point == null ? -1 : table.columnAtPoint(point);
            boolean doRepaint = newRow != oldRow || newCol != oldCol;
            mouseoverRow = newRow;
            mouseoverCol = newCol;
            if (doRepaint) {
                Rectangle rect = table.getCellRect(oldRow, 2, true);
                table.repaint(rect);
                rect = table.getCellRect(newRow, 2, true);
                table.repaint(rect);
            }
        }
    };

    @Override
    public JComponent getViewComponent() {
        return table;
    }

    @Override
    public void update() {
        UIThreadsUtil.mustBeSwingThread();
        table.clearSelection();
        table.clearHighlight();
        model.fireTableDataChanged();
        adjustRowHeights();
    }

    private void adjustRowHeights() {
        int column1Width = table.getColumnModel().getColumn(1).getWidth();
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, 1);
            Component c = table.prepareRenderer(cellRenderer, row, 1);
            c.setBounds(0, 0, column1Width, Integer.MAX_VALUE);
            int height = c.getPreferredSize().height;
            table.setRowHeight(row, height);
        }
    }

    @Override
    public void notifyUser(List<Integer> notify) {
        UIThreadsUtil.mustBeSwingThread();
        notify = translateIndices(notify);
        table.clearSelection();
        table.scrollRectToVisible(table.getCellRect(notify.get(0), notify.get(notify.size() - 1), true));
        table.flash(notify);
    }

    private List<Integer> translateIndices(List<Integer> indices) {
        List<Integer> result = new ArrayList<>(indices.size());
        for (int i : indices) {
            result.add(i / 2);
        }
        return result;
    }

    @Override
    public String getKeyAtPoint(Point p) {
        int clickedRow = table.rowAtPoint(SwingUtilities.convertPoint(parent.getScrollPane(), p, table));
        if (clickedRow == -1) {
            return null;
        }
        return (String) model.getValueAt(clickedRow, 0);
    }
}
