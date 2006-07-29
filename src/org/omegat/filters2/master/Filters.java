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

import org.omegat.util.OStrings;


/**
 * Wrapper around all the file filter classes.
 * Is a JavaBean, so that it's easy to write/read it to/from XML file
 * and provides a table model.
 *
 * @author Maxym Mykhalchuk
 */
public class Filters extends AbstractTableModel implements Serializable
{

    /**
     * Create new empty Filters storage backend.
     * Only for JavaBeans compliance here, do not call!
     * <p>
     * Call rather <code>FilterMaster.getInstance().getFilters()</code>.
     */
    public Filters()
    {
    }
    
    /** Holds the list of available filters. */
    private ArrayList filters = new ArrayList();
    
    /** 
     * Returns the number of filters. 
     */
    public int filtersSize()
    {
        return filters.size();
    }
    
    /**
     * Returns all the filters as an array.
     */
    public OneFilter[] getFilter()
    {
        return (OneFilter[])filters.toArray(new OneFilter[0]);
    }
    /**
     * Sets all filters from the array.
     */
    public void setFilter(OneFilter[] filter)
    {
        filters = new ArrayList(Arrays.asList(filter));
    }
    
    /**
     * Returns a filter by index.
     */
    public OneFilter getFilter(int index)
    {
        return (OneFilter)filters.get(index);
    }
    /**
     * Sets a filter by index.
     */
    public void setFilter(int index, OneFilter filter)
    {
        while( index>=filters.size() )
            filters.add(null);
        filters.set(index, filter);
    }
    
    /**
     * Adds one filter to the list of filters.
     * <p>
     * Checks if there's already such a filter installed
     * (by filter's class name).
     */
    public void addFilter(OneFilter filter)
    {
        for(int i=0; i<filters.size(); i++)
            if(getFilter(i).getClassName().equals(filter.getClassName()))
                return;
        filters.add(filter);
    }
    /**
     * Removes one filter from the list of filters.
     * <p>
     * It might happen if we loaded the filter from a plugin,
     * and the plugin is no longer available.
     */
    public void removeFilter(int index)
    {
        filters.remove(index);
    }
    
    //////////////////////////////////////////////////////////////////////////
    //  TableModel implementation
    //////////////////////////////////////////////////////////////////////////

    public int getColumnCount()
    {
        return 2;
    }
    
    public String getColumnName(int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return OStrings.getString("FILTERS_FILE_FORMAT");
            case 1:
                return OStrings.getString("FILTERS_ON");
        }
        return null;
    }
    
    public Class getColumnClass(int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
        }
        return null;
    }

    public int getRowCount()
    {
        return getFilter().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        OneFilter filter = getFilter(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return filter.getHumanName();
            case 1:
                return new Boolean(filter.isOn());
        }
        return null;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        OneFilter filter = getFilter(rowIndex);
        switch( columnIndex )
        {
            case 1:
                filter.setOn(((Boolean)aValue).booleanValue());
                break;
            default:
                throw new IllegalArgumentException(
                        OStrings.getString("FILTERS_ERROR_COLUMN_INDEX_NOT_1"));
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        switch( columnIndex )
        {
            case 0:
                return false;
            case 1:
                return true;
        }
        return false;
    }
    
}
