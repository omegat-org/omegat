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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.omegat.util.Platform;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class DataTableStyling {
    
    public static final Color COLOR_STANDARD_FG = Color.BLACK;
    public static final Color COLOR_STANDARD_BG = Color.WHITE;
    public static final Color COLOR_SPECIAL_FG = Color.BLACK;
    public static final Color COLOR_SPECIAL_BG = new Color(0xC8DDF2);
    public static final Color COLOR_SELECTION_FG = Color.WHITE;
    public static final Color COLOR_SELECTION_BG = new Color(0x2F77DA);
    public static final Color COLOR_ALTERNATING_HILITE = new Color(245, 245, 245);
    public static final Border TABLE_FOCUS_BORDER = new MatteBorder(1, 1, 1, 1, new Color(0x76AFE8));
    
    public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(",##0");

    public static final int FONT_NO_CHANGE = -1;
    
    public static final int LINE_SPACING = 6;
    
    public static void applyColors(JTable table) {
        if (!Platform.isMacOSX()) {
            // Windows needs some extra colors set for consistency, but these
            // ruin native LAF on OS X.
            if (table.getParent() instanceof JViewport) {
                table.getParent().setBackground(COLOR_STANDARD_BG);
            }
            if (table.getParent().getParent() instanceof JScrollPane) {
                table.getParent().getParent().setBackground(COLOR_STANDARD_BG);
            }
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(COLOR_STANDARD_BG);
            }
        }
        table.setForeground(COLOR_STANDARD_FG);
        table.setBackground(COLOR_STANDARD_BG);
        table.setSelectionForeground(COLOR_SELECTION_FG);
        table.setSelectionBackground(COLOR_SELECTION_BG);
        table.setGridColor(COLOR_STANDARD_BG);
    }
    
    public static void applyFont(JTable table, Font font) {
        table.setFont(font);
        table.setRowHeight(font.getSize() + DataTableStyling.LINE_SPACING);
    }
    
    public static TableCellRenderer getNumberCellRenderer() {
        return new AlternatingHighlightRenderer(SwingConstants.RIGHT, NUMBER_FORMAT, true, FONT_NO_CHANGE);
    }
    
    public static TableCellRenderer getTextCellRenderer() {
        return new AlternatingHighlightRenderer(SwingConstants.LEFT, null, true, FONT_NO_CHANGE);
    }
    
    public static TableCellRenderer getHeaderTextCellRenderer() {
        return new AlternatingHighlightRenderer(SwingConstants.LEFT, null, true, Font.BOLD);
    }
    
    public static class AlternatingHighlightRenderer extends DefaultTableCellRenderer {
        private final NumberFormat numberFormat;        
        private final boolean doHighlight;
        private final int fontStyle;
        
        public AlternatingHighlightRenderer(int alignment, NumberFormat numberFormat, boolean doHighlight, int fontStyle) {
            setHorizontalAlignment(alignment);
            this.numberFormat = numberFormat;
            this.doHighlight = doHighlight;
            this.fontStyle = fontStyle;
        }

        @Override
        protected void setValue(Object value) {
            if (numberFormat != null) {
                if (value instanceof Number) {
                    super.setValue(numberFormat.format((Number) value));
                    return;
                }
                if (value instanceof String) {
                    try {
                        long lVal = Long.parseLong((String) value);
                        super.setValue(numberFormat.format(lVal));
                        return;
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            super.setValue(value);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (fontStyle != FONT_NO_CHANGE) {
                result.setFont(table.getFont().deriveFont(fontStyle));
            }
            if (isSelected) {
                result.setForeground(table.getSelectionForeground());
                result.setBackground(table.getSelectionBackground());
            } else if (isSpecialHighlightRow(row)) {
                result.setForeground(COLOR_SPECIAL_FG);
                result.setBackground(COLOR_SPECIAL_BG);
            } else if (row % 2 != 0 && doHighlight) {
                result.setForeground(table.getForeground());
                result.setBackground(COLOR_ALTERNATING_HILITE);
            } else {
                result.setForeground(table.getForeground());
                result.setBackground(table.getBackground());
            }
            if (hasFocus && result instanceof JComponent) {
                ((JComponent) result).setBorder(TABLE_FOCUS_BORDER);
            }
            return result;
        }
        
        protected boolean isSpecialHighlightRow(int row) {
            return false;
        }
    }
}
