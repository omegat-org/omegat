/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.core.segmentation.datamodels;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.util.OStrings;

/**
 *
 * @author Maxym Mykhalchuk
 */
public class MappingRulesModel extends AbstractTableModel
{
    private SRX srx;
    
    /**
     * Creates a new instance of MappingRulesModel 
     */
    public MappingRulesModel(SRX srx)
    {
        this.srx = srx;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        MapRule maprule = (MapRule)srx.getMappingRules().get(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return maprule.getLanguage();
            case 1:
                return maprule.getPattern();
        }
        return null;
    }

    public int getRowCount()
    {
        return srx.getMappingRules().size();
    }

    public int getColumnCount()
    {
        return 2;
    }

    /** The names of table columns */
    private static String[] COLUMN_NAMES = 
            new String[] {
                OStrings.getString("CORE_SRX_TABLE_HEADER_Language_Name"), 
                OStrings.getString("CORE_SRX_TABLE_HEADER_Language_Pattern")};
            
    public String getColumnName(int column)
    {
        return COLUMN_NAMES[column];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return true;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        MapRule maprule = (MapRule)srx.getMappingRules().get(rowIndex);
        switch( columnIndex )
        {
            case 0:
                maprule.setLanguage((String)aValue);
                break;
            case 1:
                maprule.setPattern((String)aValue);
                break;
        }
    }

    public Class getColumnClass(int columnIndex)
    {
        return String.class;
    }
    
    /** Adds a new empty mapping rule. */
    public int addRow()
    {
        int rows = srx.getMappingRules().size();
        srx.getMappingRules().add(new MapRule("", "", new ArrayList()));        // NOI18N
        fireTableRowsInserted(rows, rows);
        return rows;
    }

    /** Removes a mapping rule. */
    public void removeRow(int row)
    {
        srx.getMappingRules().remove(row);
        fireTableRowsDeleted(row, row);
    }

    /** Moves a mapping rule up an order. */
    public void moveRowUp(int row)
    {
        MapRule maprulePrev = (MapRule)srx.getMappingRules().get(row-1);
        MapRule maprule = (MapRule)srx.getMappingRules().get(row);
        srx.getMappingRules().remove(row-1);
        srx.getMappingRules().add(row, maprulePrev);
        fireTableRowsUpdated(row-1, row);
    }
    
    /** Moves a mapping rule down an order. */
    public void moveRowDown(int row)
    {
        MapRule mapruleNext = (MapRule)srx.getMappingRules().get(row+1);
        MapRule maprule = (MapRule)srx.getMappingRules().get(row);
        srx.getMappingRules().remove(row+1);
        srx.getMappingRules().add(row, mapruleNext);
        fireTableRowsUpdated(row, row+1);
    }
    
}
