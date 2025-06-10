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

import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.MissingResourceException;

@SuppressWarnings("serial")
class SingleLineCellRenderer extends DefaultTableCellRenderer {

    private final Border noFocusBorder = new EmptyBorder(
            ISegmentPropertiesView.FOCUS_BORDER.getBorderInsets(this));

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
            result.setBorder(new CompoundBorder(marginBorder, ISegmentPropertiesView.FOCUS_BORDER));
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
            return OStrings.getString(
                    ISegmentPropertiesView.PROPERTY_TRANSLATION_KEY + value.toString().toUpperCase());
        } catch (MissingResourceException ex) {
            return value.toString();
        }
    }
}
