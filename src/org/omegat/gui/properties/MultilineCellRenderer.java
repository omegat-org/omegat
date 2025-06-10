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

import org.omegat.util.gui.StaticUIUtils;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

// See: http://esus.com/creating-a-jtable-with-multiline-cells/
@SuppressWarnings("serial")
class MultilineCellRenderer extends JTextArea implements TableCellRenderer {

    private final Border noFocusBorder = new EmptyBorder(
            ISegmentPropertiesView.FOCUS_BORDER.getBorderInsets(this));

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
            setBorder(new CompoundBorder(marginBorder, ISegmentPropertiesView.FOCUS_BORDER));
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
