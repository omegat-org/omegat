/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

package org.omegat.gui.editor.chartable;

import java.awt.Font;
import java.awt.Point;

import javax.swing.table.AbstractTableModel;

/**
 * Character table table model
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class CharTableModel extends AbstractTableModel {
    Font font;
    
    int columnCount = 16; 
    
    int glyphCount = 65535-32;
    
    StringBuilder data = null;
    
    public CharTableModel(String data) {
        setData(data);
    }
    
    /**
     * set the data to a selected string
     * @param data the new string
     * @return true if the data have been replaced.
     */
    public boolean setData(String data) {
        if (this.data == null && data == null)
            return false;
        
        if (data != null) {
            if (this.data == null || !this.data.equals(data)) {
                glyphCount = data.length();
                this.data = new StringBuilder(data);
                fireTableDataChanged();
                return true;
            }
        } else {
            glyphCount = 0xFFFF-32;
            this.data = null;
            fireTableDataChanged();
            return true;
        }
        return false;
    }
    
    public String getData() {
        return data.toString();
    }
    
    /**
     * leave only unique characters in the data string.
     */
    public void allowOnlyUnique() {
        if (data == null) {
            return;
        }
        
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            if (temp.indexOf(data.substring(i,i+1)) == -1) {
                temp.append(data.substring(i, i+1));
            }
        }
        data = new StringBuilder(temp);
        glyphCount = data.length();
        fireTableDataChanged();
    }
    
    /**
     * Append a new character to the data.
     * @param c the character
     * @param checkUnique check for being unique or not
     */
    public void appendChar(Character c, boolean checkUnique) {
        char cv = c.charValue();
        if (checkUnique)
            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == cv)
                    return;
            }
        
        this.data.append(cv);
        glyphCount++;
        fireTableDataChanged();
    }
    
    /**
     * Remove the selected characters from the model.
     * @param row1 from row
     * @param col1 from column
     * @param row2 to row
     * @param col2 to column
     */
    public void removeSelection(int row1, int col1, int row2, int col2) {
        if (data.length() == 0)
            return;
        
        int pos1 = row1 * getColumnCount() + col1;
        pos1 = pos1 >= data.length() ? data.length() - 1 : pos1;
        int pos2 = row2 * getColumnCount() + col2;
        pos2 = (pos2 >= data.length()) ? data.length() - 1 : pos2;
        pos2 = (pos2 == pos1) ? pos1+1 : pos2;
        data.delete(pos1, pos2);
        glyphCount = data.length();
        fireTableDataChanged();
    }
    
    @Override
    public int getRowCount() {
        return (glyphCount / columnCount) + ((glyphCount % columnCount > 0) ? 1 : 0);
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int value = rowIndex*columnCount + columnIndex;
        if (value < glyphCount)
            if (data!=null)
                return data.charAt(value);
            else 
                return (char)(value + 32);
        else
            return null;
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }
    
    /**
     * Prevent the use of invalid points in the table (beyond data string length).
     * @param p the point in question
     * @return the modified point.
     */
    public Point modifyPoint(Point p) {
        if (p.y * columnCount + p.x >= glyphCount) {
            int g = glyphCount == 0 ? 0 : glyphCount - 1;
            return new Point(g % columnCount, g / columnCount);
        }
        return p;
    }
}
