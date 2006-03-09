/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters2.master;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.AbstractTableModel;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

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
     *
     * @param fromPlugin is the filter loaded from a plugin? if yes, we'll check is the plugin still available upon loading configuration
     */
    public OneFilter(AbstractFilter filter, boolean fromPlugin)
    {
        setClassName(filter.getClass().getName());
        setOn(true);
        setFromPlugin(fromPlugin);
        setSourceEncodingVariable(filter.isSourceEncodingVariable());
        setTargetEncodingVariable(filter.isTargetEncodingVariable());
        setInstance(filter.getDefaultInstances());
    }
    
    /////////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////////
    
    /** Holds the class name of the filter */
    private String className = null;
    /**
     * Returns the class name of the filter.
     */
    public String getClassName()
    {
        return className;
    }
    /**
     * Sets the class name of the filter.
     */
    public void setClassName(String value)
    {
        className = value;
    }

    /**
     * Returns the human-readable name of the filter.
     */
    public String getHumanName()
    {
        try
        {
            AbstractFilter filter = PluginUtils.instantiateFilter(this);
            return filter.getFileFormatName();
        }
        catch (Exception e)
        {
            StaticUtils.log(e.getMessage());
            return getClassName();
        }
    }
    /**
     * Sets the "human" name of the filter.
     * Since 1.6.0 RC7 does nothing.
     */
    public void setHumanName(String value) {}
    
    /** If the filter is used. */
    private boolean on = true;
    /** 
     * Returns whether the filter is on (used by OmegaT).
     */
    public boolean isOn()
    {
        return on;
    }
    /** 
     * Sets whether the filter is on (used by OmegaT). 
     */
    public void setOn(boolean value)
    {
        on = value;
    }


    /** Holds whether this filter was loaded from a plugin */
    private boolean fromPlugin = false;
    /** 
     * Returns whether this filter was loaded from a plugin 
     */
    public boolean isFromPlugin()
    {
        return fromPlugin;
    }
    /** 
     * Sets whether this filter was loaded from a plugin 
     */
    public void setFromPlugin(boolean fromPlugin)
    {
        this.fromPlugin = fromPlugin;
    }
    
    /** Holds whether the filter's source encoding can be varied by user */
    private boolean sourceEncodingVariable;
    /** 
     * Returns whether the filter's source encoding can be varied by user 
     */
    public boolean isSourceEncodingVariable()
    {
        return sourceEncodingVariable;
    }
    /** 
     * Sets whether the filter's source encoding can be varied by user 
     */
    public void setSourceEncodingVariable(boolean value)
    {
        sourceEncodingVariable = value;
    }
    
    /** Holds whether the filter's target encoding can be varied by user */
    private boolean targetEncodingVariable;
    /** 
     * Returns whether the filter's target encoding can be varied by user 
     */
    public boolean isTargetEncodingVariable()
    {
        return targetEncodingVariable;
    }
    /** 
     * Sets whether the filter's target encoding can be varied by user 
     */
    public void setTargetEncodingVariable(boolean value)
    {
        targetEncodingVariable = value;
    }

    /** Holds instances property. */
    private ArrayList instances = new ArrayList();

    /**
     * Returns all the instances of the filter.
     */
    public Instance[] getInstance()
    {
        return (Instance[])instances.toArray(new Instance[0]);
    }
    /**
     * Sets all the instances of the filter at once.
     */
    public void setInstance(Instance[] instance)
    {
        instances = new ArrayList(Arrays.asList(instance));
        fireTableDataChanged();
    }

    /**
     * Adds one the instance of the filter.
     */
    public void addInstance(Instance instance)
    {
        instances.add(instance);
        fireTableDataChanged();
    }
    /**
     * Removes one instance of the filter.
     */
    public void removeInstance(int index)
    {
        instances.remove(index);
        fireTableDataChanged();
    }
    
    /** Returns the filter instance #... */
    public Instance getInstance(int index)
    {
        return (Instance)instances.get(index);
    }
    /** Sets the filter instance #... */
    public void setInstance(int index, Instance instance)
    {
        while( index>=instances.size() )
            instances.add(null);
        instances.set(index, instance);
        fireTableRowsUpdated(index, index);
    }
    
    /** Returns the hint for editing the filter and adding/editing filter instance */
    public String getHint()  
    {
        try
        {
            AbstractFilter filter = PluginUtils.instantiateFilter(this);
            return filter.getHint();
        }
        catch (Exception e)
        {
            StaticUtils.log(e.getMessage());
            return "";                                                          // NOI18N
        }
    }
    /** 
     * Sets the hint for editing the filter and adding/editing filter instance. 
     * Since 1.6.0 RC7 does nothing.
     */
    public void setHint(String hint)  {}
    
    /** Holds options of the filter. */
    private Serializable options = null;

    /**
     * Returns filter's options.
     * @return Filter options object.
     */
    public Serializable getOptions()
    {
        return this.options;
    }

    /**
     * Setter for property options.
     * @param options New value of property options.
     */
    public void setOptions(Serializable options)
    {
        this.options = options;
    }
    
    //////////////////////////////////////////////////////////////////////////
    //  Filter Options
    //////////////////////////////////////////////////////////////////////////

    /** Returns whether the filter has options. */
    public boolean hasOptions()
    {
        try
        {
            AbstractFilter filter = PluginUtils.instantiateFilter(this);
            return filter.hasOptions();
        }
        catch (Exception e)
        {
            StaticUtils.log(e.getMessage());
            return false;
        }
    }
    
    /** Calls filter's modal dialog to change its options. */
    public void changeOptions(java.awt.Dialog parent)
    {
        try
        {
            AbstractFilter filter = PluginUtils.instantiateFilter(this);
            setOptions(filter.changeOptions(parent, getOptions()));
        }
        catch (Exception e)
        {
            StaticUtils.log(e.getMessage());
        }
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
                return OStrings.getString("ONEFILTER_SOURCE_FILENAME_MASK");
            case 1:
                return OStrings.getString("ONEFILTER_SOURCE_FILE_ENCODING");
            case 2:
                return OStrings.getString("ONEFILTER_TARGET_FILE_ENCODING");
            case 3:
                return OStrings.getString("ONEFILTER_TARGET_FILENAME_ENCODING");
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
                return instance.getSourceEncodingHuman();
            case 2:
                return instance.getTargetEncodingHuman();
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
