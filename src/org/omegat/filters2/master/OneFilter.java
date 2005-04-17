/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.master;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.AbstractTableModel;
import org.omegat.filters2.*;
import org.omegat.filters2.Instance;


/**
 * Wrapper around a single file filter class
 * Manages entries in XML config file and provides a table model.
 *
 * @author Maxym Mykhalchuk
 */
public class OneFilter extends AbstractTableModel implements Serializable
{
    /////////////////////////////////////////////////////////////////////////
    // Two different constructors
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates an empty filter wrapper.
     * Needed for JavaBeans compliance.
     */
    public OneFilter()
    {
    }
    
    /**
     * Creates a wrapper from a filter class.
     * Needs to construct an XML element.
     */
    public OneFilter(AbstractFilter filter)
    {
        setClassName(filter.getClass().getName());
        setHumanName(filter.getFileFormatName());
        setOn(true);
        setSourceEncodingVariable(filter.isSourceEncodingVariable());
        setTargetEncodingVariable(filter.isTargetEncodingVariable());
        setInstance(filter.getDefaultInstances());
    }
    
    /**
     * Хранит значение свойства instance.
     */
    private ArrayList instances = new ArrayList();
    
    
    /////////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////////
    
    private String className;
    public String getClassName()
    {
        return className;
    }
    public void setClassName(String value)
    {
        className = value;
    }

    private String humanName;
    public String getHumanName()
    {
        return humanName;
    }
    public void setHumanName(String value)
    {
        humanName = value;
    }
    
    private boolean on;
    public boolean isOn()
    {
        return on;
    }
    public void setOn(boolean value)
    {
        on = value;
    }
    
    private boolean sourceEncodingVariable;
    public boolean isSourceEncodingVariable()
    {
        return sourceEncodingVariable;
    }
    public void setSourceEncodingVariable(boolean value)
    {
        sourceEncodingVariable = value;
    }
    
    private boolean targetEncodingVariable;
    public boolean isTargetEncodingVariable()
    {
        return targetEncodingVariable;
    }
    public void setTargetEncodingVariable(boolean value)
    {
        targetEncodingVariable = value;
    }
    
    public Instance[] getInstance()
    {
        return (Instance[])instances.toArray(new Instance[0]);
    }
    public void setInstance(Instance[] instance)
    {
        instances = new ArrayList(Arrays.asList(instance));
        fireTableDataChanged();
    }

    public void addInstance(Instance instance)
    {
        instances.add(instance);
        fireTableDataChanged();
    }
    public void removeInstance(int index)
    {
        instances.remove(index);
        fireTableDataChanged();
    }
    
    public Instance getInstance(int index)
    {
        return (Instance)instances.get(index);
    }
    public void setInstance(int index, Instance instance)
    {
        while( index>=instances.size() )
            instances.add(null);
        instances.set(index, instance);
        fireTableRowsUpdated(index, index);
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    //  TableModel implementation
    //////////////////////////////////////////////////////////////////////////

    public int getColumnCount()
    {
        return 4;
    }
    
    public String getColumnName(int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return "Source Filename Mask";
            case 1:
                return "Source File Encoding";
            case 2:
                return "Target File Encoding";
            case 3:
                return "Target Filename Pattern";
        }
        return null;
    }
    
    public Class getColumnClass(int columnIndex)
    {
        return String.class;
    }

    public int getRowCount()
    {
        return getInstance().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Instance instance = getInstance(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return instance.getSourceFilenameMask();
            case 1:
                return instance.getSourceEncoding();
            case 2:
                return instance.getTargetEncoding();
            case 3:
                return instance.getTargetFilenamePattern();
        }
        return null;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        Instance instance = getInstance(rowIndex);
        switch( columnIndex )
        {
            case 0:
                instance.setSourceFilenameMask(aValue.toString());
                break;
            case 1:
                instance.setSourceEncoding(aValue.toString());
                break;
            case 2:
                instance.setTargetEncoding(aValue.toString());
                break;
            case 3:
                instance.setTargetFilenamePattern(aValue.toString());
                break;
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
            case 3:
                return true;
            case 1:
                return isSourceEncodingVariable();
            case 2:
                return isTargetEncodingVariable();
        }
        return false;
    }

    
}
