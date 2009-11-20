/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
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

import java.util.ArrayList;
import java.util.List;

import gen.core.filters.Filter;
import gen.core.filters.Filters;

import javax.swing.table.AbstractTableModel;

import org.omegat.filters2.IFilter;
import org.omegat.util.OStrings;


/**
 * Wrapper around all the file filter classes.
 * Is a JavaBean, so that it's easy to write/read it to/from XML file
 * and provides a table model.
 *
 * @author Maxym Mykhalchuk
 */
public class FiltersTableModel extends AbstractTableModel {

    private final List<Filter> filters;

    public FiltersTableModel(final Filters config) {
        filters = new ArrayList<Filter>();
        // add only exist filters
        for (Filter f : config.getFilter()) {
            IFilter fi = FilterMaster.getInstance().getFilterInstance(
                    f.getClassName());
            if (fi != null) {
                // filter exist
                filters.add(f);
            }
        }
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
    
    public Class<?> getColumnClass(int columnIndex)
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
        return filters.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Filter filter = filters.get(rowIndex);
        switch( columnIndex )
        {
            case 0:
                IFilter f = FilterMaster.getInstance().getFilterInstance(
                        filter.getClassName());
                return f.getFileFormatName();
            case 1:
                return new Boolean(filter.isEnabled());
        }
        return null;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        Filter filter = filters.get(rowIndex);
        switch( columnIndex )
        {
            case 1:
                filter.setEnabled(((Boolean)aValue).booleanValue());
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
