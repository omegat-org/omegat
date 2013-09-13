/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko
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

package org.omegat.gui.editor.autotext;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * The table model of the table in the autotext configuration window.
 * @author bartkoz
 */
public class AutotextTableModel extends AbstractTableModel {

    private List<AutotextPair> data = new ArrayList<AutotextPair>();
    
    public AutotextTableModel() {}
    
    /**
     * Load the data from the core autotext list.
     */
    public void load() {
        data.clear();
        for (AutotextPair pair:Core.getAutoText().getList()) {
            data.add(new AutotextPair(pair.source, pair.target, pair.comment));
        }
    }
    
    /**
     * Store the data to the specified autotext list. All items, where the target is not empty are stored.
     * @param autotext the target list
     */
    public void store(Autotext autotext) {
        List<AutotextPair> list = autotext.getList();
        String source;
        String target;
        String comment;
        list.clear();
        for (AutotextPair pair:data) {
            if (pair.target != null || !pair.target.isEmpty()) {
                source = pair.source == null ? "" : pair.source;
                comment = pair.comment == null ? "" : pair.comment;
                list.add(new AutotextPair(source, pair.target, comment));
            }
                
                
        }
    }
    
    private String[] columnNames = { OStrings.getString("AC_AUTOTEXT_ABBREVIATION"), 
        OStrings.getString("AC_AUTOTEXT_TEXT"),
        OStrings.getString("AC_AUTOTEXT_COMMENT") };
    
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
        AutotextPair pair = data.get(i);
        switch (i1) {
            case 0: return pair.source;
            case 1: return pair.target;
            case 2: return pair.comment;
            default: return null;
        }
    }
    
    @Override
    public String getColumnName(int col) {
      return columnNames[col];
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        AutotextPair pair = data.get(row);
        switch (col) {
            case 0: pair.source = (String) value; break;
            case 1: pair.target = (String) value; break;
            case 2: pair.comment = (String) value;
        }
        fireTableCellUpdated(row, col);
    }
    
    @Override
    public boolean isCellEditable(int row, int col)
        { return true; }
    
    /**
     * add a new row.
     * @param pair what to add
     * @param position at which position
     */
    public void addRow(AutotextPair pair, int position) {
        int newPosition;
        if (position == -1)
            newPosition = data.size();
        else
            newPosition = position;
        data.add(newPosition, pair);
        fireTableDataChanged();
    }
    
    /**
     * remove a row. 
     * @param position where from
     */
    public void removeRow(int position) {
        data.remove(position);
        fireTableDataChanged();
    }
    
}
