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

package org.omegat.gui.issues;

import org.omegat.util.gui.ResourcesUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;

@SuppressWarnings("serial")
class IssuesTableModel extends AbstractTableModel {

    private final transient JTable table;
    private int mouseoverRow = -1;
    private int mouseoverCol = -1;

    private static final Icon SETTINGS_ICON = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.active.png"));
    private static final Icon SETTINGS_ICON_INACTIVE = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.inactive.png"));
    private static final Icon SETTINGS_ICON_PRESSED = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.pressed.png"));
    private static final Icon SETTINGS_ICON_INVISIBLE = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // do nothing to hide an icon
        }

        @Override
        public int getIconWidth() {
            return SETTINGS_ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return SETTINGS_ICON.getIconHeight();
        }
    };

    private final transient List<IIssue> issues;

    IssuesTableModel(JTable table, List<IIssue> issues) {
        this.table = table;
        this.issues = issues;
    }

    public int getMouseoverRow() {
        return mouseoverRow;
    }

    public void setMouseoverRow(int mouseoverRow) {
        this.mouseoverRow = mouseoverRow;
    }

    public int getMouseoverCol() {
        return mouseoverCol;
    }

    public void setMouseoverCol(int mouseoverCol) {
        this.mouseoverCol = mouseoverCol;
    }

    @Override
    public int getRowCount() {
        return issues.size();
    }

    @Override
    public int getColumnCount() {
        return IssueColumn.values().length;
    }

    @Override
    public String getColumnName(int column) {
        return IssueColumn.get(column).getLabel();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        IIssue iss = issues.get(rowIndex);
        switch (IssueColumn.get(columnIndex)) {
        case SEG_NUM:
            return iss.getSegmentNumber();
        case ICON:
            return iss.getIcon();
        case TYPE:
            return iss.getTypeName();
        case DESCRIPTION:
            return iss.getDescription();
        case ACTION_BUTTON:
            return getActionMenuIcon(iss, rowIndex, columnIndex);
        }
        throw new IllegalArgumentException("Unknown column requested: " + columnIndex);
    }

    Icon getActionMenuIcon(IIssue issue, int modelRow, int col) {
        // The row argument is in terms of the model while mouseoverRow is in
        // terms of the view, so convert first.
        int viewRow = table.getRowSorter().convertRowIndexToView(modelRow);
        if (!issue.hasMenuComponents()) {
            return SETTINGS_ICON_INVISIBLE;
        } else if (table.getSelectedRow() == viewRow) {
            // Show "pressed" version here for better contrast against the table
            // selection highlight.
            return SETTINGS_ICON_PRESSED;
        } else if (viewRow == mouseoverRow && col == mouseoverCol) {
            return SETTINGS_ICON;
        } else if (viewRow == mouseoverRow) {
            return SETTINGS_ICON_INACTIVE;
        } else {
            return SETTINGS_ICON_INVISIBLE;
        }
    }

    public IIssue getIssueAt(int rowIndex) {
        return issues.get(rowIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return IssueColumn.get(columnIndex).getClazz();
    }
}
