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

package org.omegat.gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.StaticUIUtils;
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

    @Override
    public void install(final SegmentPropertiesArea parent) {
        this.parent = parent;
        model = new PropertiesTableModel();
        table = new FlashingTable(model);
        table.setForeground(parent.scrollPane.getForeground());
        table.setBackground(parent.scrollPane.getBackground());
        table.addMouseListener(parent.contextMenuListener);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setGridColor(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setCellRenderer(new SingleLineCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new MultilineCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new SingleLineCellRenderer());
        DataTableStyling.applyFont(table, Core.getMainWindow().getApplicationFont());
        TableColumnSizer.autoSize(table, 1, true).addColumnAdjustmentListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustRowHeights();
            }
        });
        table.addMouseListener(mouseAdapter);
        table.addMouseMotionListener(mouseAdapter);
        parent.scrollPane.setViewportView(table);
    }

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (mouseoverCol == 2) {
                parent.showContextMenu(SwingUtilities.convertPoint(table, e.getPoint(), parent.scrollPane));
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
    };

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
        List<Integer> result = new ArrayList<Integer>(indices.size());
        for (int i : indices) {
            result.add(i / 2);
        }
        return result;
    }

    @Override
    public String getKeyAtPoint(Point p) {
        int clickedRow = table.rowAtPoint(SwingUtilities.convertPoint(parent.scrollPane, p, table));
        if (clickedRow == -1) {
            return null;
        }
        return (String) model.getValueAt(clickedRow, 0);
    }

    private class PropertiesTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return OStrings.getString("SEGPROP_TABLE_HEADER_KEY");
            case 1:
                return OStrings.getString("SEGPROP_TABLE_HEADER_VALUE");
            case 2:
                return "";
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return parent.properties.size() / 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || columnIndex < 0) {
                return null;
            }
            if (columnIndex == 2) {
                return rowIndex == mouseoverRow && columnIndex == mouseoverCol ? SETTINGS_ICON
                        : rowIndex == mouseoverRow ? SETTINGS_ICON_INACTIVE : SETTINGS_ICON_INVISIBLE;
            }
            int realIndex = rowIndex * 2 + columnIndex;
            if (realIndex >= parent.properties.size()) {
                return null;
            }
            return parent.properties.get(rowIndex * 2 + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return Icon.class;
            }
            return null;
        }
    }

    private static class FlashingTable extends JTable {
        private FlashColorInterpolator flasher;
        private List<Integer> rows;

        FlashingTable(TableModel model) {
            super(model);
        }

        public void flash(List<Integer> rows) {
            this.rows = rows;
            flasher = new FlashColorInterpolator();
            repaint();
        }

        public boolean isHighlightedRow(int index) {
            return rows != null && rows.contains(index);
        }

        public void clearHighlight() {
            flasher = null;
        }

        public FlashColorInterpolator getFlasher() {
            return flasher;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (flasher != null && flasher.isFlashing()) {
                repaint();
            }
        }
    }

    private static class SingleLineCellRenderer extends DefaultTableCellRenderer {

        private final Border noFocusBorder = new EmptyBorder(FOCUS_BORDER.getBorderInsets(this));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!(comp instanceof JLabel)) {
                return comp;
            }
            JLabel result = (JLabel) comp;
            if (isSelected) {
                result.setBackground(table.getSelectionBackground());
                result.setForeground(table.getSelectionForeground());
            } else {
                Color bg = table.getBackground();
                result.setBackground(row % 2 == 0 ? bg : StaticUIUtils.getHighlightColor(bg));
                result.setForeground(table.getForeground());
            }
            Border marginBorder = new EmptyBorder(1, column == 0 ? 5 : 1, 1,
                    column == table.getColumnCount() - 1 ? 5 : 1);
            if (hasFocus) {
                result.setBorder(new CompoundBorder(marginBorder, FOCUS_BORDER));
            } else {
                result.setBorder(new CompoundBorder(marginBorder, noFocusBorder));
            }
            FlashingTable fTable = (FlashingTable) table;
            FlashColorInterpolator flasher = fTable.getFlasher();
            if (flasher != null) {
                flasher.mark();
                if (fTable.isHighlightedRow(row) && !isSelected) {
                    setBackground(flasher.getColor());
                }
            }
            result.setFont(table.getFont());
            if (value instanceof String) {
                result.setVerticalAlignment(SwingConstants.TOP);
                result.setText(getText(value));
                result.setIcon(null);
            } else if (value instanceof Icon) {
                result.setVerticalAlignment(SwingConstants.CENTER);
                result.setText(null);
                result.setIcon((Icon) value);
            }
            return result;
        }

        private String getText(Object value) {
            if (Preferences.isPreference(Preferences.SEGPROPS_SHOW_RAW_KEYS)) {
                return value.toString();
            }
            try {
                return OStrings.getString(PROPERTY_TRANSLATION_KEY + value.toString().toUpperCase());
            } catch (MissingResourceException ex) {
                return value.toString();
            }
        }
    }

    // See: http://esus.com/creating-a-jtable-with-multiline-cells/
    private static class MultilineCellRenderer extends JTextArea implements TableCellRenderer {

        private final Border noFocusBorder = new EmptyBorder(FOCUS_BORDER.getBorderInsets(this));

        MultilineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                Color bg = table.getBackground();
                setBackground(row % 2 == 0 ? bg : StaticUIUtils.getHighlightColor(bg));
                setForeground(table.getForeground());
            }
            Border marginBorder = new EmptyBorder(1, column == 0 ? 5 : 1, 1,
                    column == table.getColumnCount() - 1 ? 5 : 1);
            if (hasFocus) {
                setBorder(new CompoundBorder(marginBorder, FOCUS_BORDER));
            } else {
                setBorder(new CompoundBorder(marginBorder, noFocusBorder));
            }
            FlashingTable fTable = (FlashingTable) table;
            FlashColorInterpolator flasher = fTable.getFlasher();
            if (flasher != null) {
                flasher.mark();
                if (fTable.isHighlightedRow(row) && !isSelected) {
                    setBackground(flasher.getColor());
                }
            }
            setFont(table.getFont());
            setText(value.toString());
            return this;
        }
    }
}
