/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autotext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import org.omegat.gui.editor.autotext.Autotext.AutotextItem;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * The table model of the table in the autotext configuration window.
 *
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class AutotextTableModel extends AbstractTableModel {

    private List<AutotextItem> data = Collections.emptyList();

    public AutotextTableModel(Collection<AutotextItem> data) {
        this.data = new ArrayList<>(data);
    }

    /**
     * Store the data to the specified autotext list. All items, where the
     * target is not empty are stored.
     */
    public List<AutotextItem> getData() {
        return data.stream().filter(item -> !StringUtil.isEmpty(item.target)).collect(Collectors.toList());
    }

    private String[] columnNames = { OStrings.getString("AC_AUTOTEXT_ABBREVIATION"),
            OStrings.getString("AC_AUTOTEXT_TEXT"), OStrings.getString("AC_AUTOTEXT_COMMENT") };

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        AutotextItem item = data.get(i);
        switch (i1) {
        case 0:
            return item.source;
        case 1:
            return item.target;
        case 2:
            return item.comment;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        AutotextItem current = data.get(row);
        String source = col == 0 ? (String) value : current.source;
        String target = col == 1 ? (String) value : current.target;
        String comment = col == 2 ? (String) value : current.comment;
        AutotextItem item = new AutotextItem(source, target, comment);
        data.set(row, item);
        fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    /**
     * add a new row.
     * 
     * @param item
     *            what to add
     * @param position
     *            at which position
     */
    public int addRow(AutotextItem item, int position) {
        int newPosition = position == -1 ? data.size() : position;
        data.add(newPosition, item);
        fireTableDataChanged();
        return newPosition;
    }

    /**
     * remove a row.
     * 
     * @param position
     *            where from
     */
    public void removeRow(int position) {
        data.remove(position);
        fireTableDataChanged();
    }

}
