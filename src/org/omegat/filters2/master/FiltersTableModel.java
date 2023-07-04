/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2015 Yu Tang
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

package org.omegat.filters2.master;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.omegat.filters2.IFilter;
import org.omegat.util.OStrings;

import gen.core.filters.Filter;
import gen.core.filters.Filters;

/**
 * Wrapper around all the file filter classes. Is a JavaBean, so that it's easy
 * to write/read it to/from XML file and provides a table model.
 *
 * @author Maxym Mykhalchuk
 * @author Yu Tang
 */
@SuppressWarnings("serial")
public class FiltersTableModel extends AbstractTableModel {

    private final List<FilterData> filters = new ArrayList<>();

    public FiltersTableModel(final Filters config) {
        // add only exist filters
        for (Filter f : config.getFilters()) {
            IFilter fi = FilterMaster.getFilterInstance(f.getClassName());
            if (fi != null) {
                // filter exist
                String key = f.getClassName();
                FilterData data = new FilterData(key, fi.getFileFormatName(), f);
                filters.add(data);
            }
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return OStrings.getString("FILTERS_FILE_FORMAT");
        case 1:
            return OStrings.getString("FILTERS_ON");
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return String.class;
        case 1:
            return Boolean.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return filters.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return filters.get(rowIndex).filterName;
        case 1:
            return filters.get(rowIndex).filter.isEnabled();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 1:
            filters.get(rowIndex).filter.setEnabled((Boolean) aValue);
            break;
        default:
            throw new IllegalArgumentException(OStrings.getString("FILTERS_ERROR_COLUMN_INDEX_NOT_1"));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return false;
        case 1:
            return true;
        }
        return false;
    }

    public Filter getFilterAtRow(int row) {
        return filters.get(row).filter;
    }

    public void setFilter(Filter filter) {
        String key = filter.getClassName();
        IFilter fi = FilterMaster.getFilterInstance(key);
        if (fi != null) {
            FilterData oldData =
                    filters.stream().filter(data -> data.className.equals(key)).findFirst().orElse(null);
            if (oldData != null) {
                filters.set(filters.indexOf(oldData), new FilterData(key, fi.getFileFormatName(), filter));
            }
        }
    }

    /**
     * POJO for filter list.
     */
    static class FilterData {
        public String className;
        public String filterName;
        public Filter filter;

        public FilterData(final String className, final String filterName, final Filter filter) {
            this.className = className;
            this.filterName = filterName;
            this.filter = filter;
        }
    }
}
