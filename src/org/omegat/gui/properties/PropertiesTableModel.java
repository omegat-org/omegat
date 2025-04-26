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

import org.jetbrains.annotations.Nullable;
import org.omegat.util.OStrings;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class PropertiesTableModel extends AbstractTableModel {

    private final transient SegmentPropertiesTableView segmentPropertiesTableView;

    public PropertiesTableModel(SegmentPropertiesTableView segmentPropertiesTableView) {
        this.segmentPropertiesTableView = segmentPropertiesTableView;
    }

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
            default:
                return null;
        }
    }

    @Override
    public int getRowCount() {
        return segmentPropertiesTableView.getParent().getProperties().size() / 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || columnIndex < 0) {
            return null;
        }
        Icon rowIndex1 = getIcon(rowIndex, columnIndex);
        if (rowIndex1 != null) return rowIndex1;
        int realIndex = rowIndex * 2 + columnIndex;
        if (realIndex >= segmentPropertiesTableView.getParent().getProperties().size()) {
            return null;
        }
        return segmentPropertiesTableView.getParent().getProperties().get(rowIndex * 2 + columnIndex);
    }

    private @Nullable Icon getIcon(int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            if (rowIndex == segmentPropertiesTableView.getMouseoverRow() && columnIndex ==
                    segmentPropertiesTableView.getMouseoverCol()) {
                return ISegmentPropertiesView.SETTINGS_ICON;
            } else {
                if (rowIndex == segmentPropertiesTableView.getMouseoverRow())
                    return ISegmentPropertiesView.SETTINGS_ICON_INACTIVE;
                return ISegmentPropertiesView.SETTINGS_ICON_INVISIBLE;
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return Icon.class;
            default:
                return null;
        }
    }

}
