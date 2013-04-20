/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters2.master;

import gen.core.filters.Filter;
import gen.core.filters.Filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.omegat.filters2.IFilter;
import org.omegat.util.OStrings;

/**
 * Wrapper around all the file filter classes. Is a JavaBean, so that it's easy
 * to write/read it to/from XML file and provides a table model.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class FiltersTableModel extends AbstractTableModel {

    private final List<Filter> filters;
    
    private final Map<String, String> filterNames = new TreeMap<String, String>();

    public FiltersTableModel(final Filters config) {
        filters = new ArrayList<Filter>();
        // add only exist filters
        for (Filter f : config.getFilters()) {
            IFilter fi = FilterMaster.getFilterInstance(f.getClassName());
            if (fi != null) {
                // filter exist
                filters.add(f);
                filterNames.put(f.getClassName(), fi.getFileFormatName());
            }
        }
        /*Collections.sort(filters, new Comparator<Filter>() {
            @Override
            public int compare(Filter o1, Filter o2) {
                String s1 = filterNames.get(o1.getClassName());
                String s2 = filterNames.get(o2.getClassName());
                return s1.compareToIgnoreCase(s2);
            }
        });*/
    }

    // ////////////////////////////////////////////////////////////////////////
    // TableModel implementation
    // ////////////////////////////////////////////////////////////////////////

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return OStrings.getString("FILTERS_FILE_FORMAT");
        case 1:
            return OStrings.getString("FILTERS_ON");
        }
        return null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return String.class;
        case 1:
            return Boolean.class;
        }
        return null;
    }

    public int getRowCount() {
        return filters.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Filter filter = filters.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return filterNames.get(filter.getClassName());
        case 1:
            return new Boolean(filter.isEnabled());
        }
        return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Filter filter = filters.get(rowIndex);
        switch (columnIndex) {
        case 1:
            filter.setEnabled(((Boolean) aValue).booleanValue());
            break;
        default:
            throw new IllegalArgumentException(OStrings.getString("FILTERS_ERROR_COLUMN_INDEX_NOT_1"));
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return false;
        case 1:
            return true;
        }
        return false;
    }
}
