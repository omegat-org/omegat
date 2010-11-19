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

import gen.core.filters.Files;
import gen.core.filters.Filter;

import javax.swing.table.AbstractTableModel;

import org.omegat.filters2.IFilter;
import org.omegat.util.OStrings;

/**
 * Wrapper around a single file filter class Manages entries in XML config file
 * and provides a table model.
 * 
 * @author Maxym Mykhalchuk
 */
public class OneFilterTableModel extends AbstractTableModel {

    private final Filter filter;
    private boolean sourceEncodingVariable, targetEncodingVariable;

    private final String ENC_AUTO_NAME = OStrings.getString("ENCODING_AUTO");

    public OneFilterTableModel(final Filter f) {
        this.filter = f;
        IFilter fi = FilterMaster.getInstance().getFilterInstance(f.getClassName());
        sourceEncodingVariable = fi.isSourceEncodingVariable();
        targetEncodingVariable = fi.isTargetEncodingVariable();
    }

    // ////////////////////////////////////////////////////////////////////////
    // TableModel implementation
    // ////////////////////////////////////////////////////////////////////////

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
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

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getRowCount() {
        return filter.getFiles().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Files instance = filter.getFiles().get(rowIndex);
        switch (columnIndex) {
        case 0:
            return instance.getSourceFilenameMask();
        case 1:
            return getEncodingName(instance.getSourceEncoding());
        case 2:
            return getEncodingName(instance.getTargetEncoding());
        case 3:
            return instance.getTargetFilenamePattern();
        }
        return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Files instance = filter.getFiles().get(rowIndex);
        switch (columnIndex) {
        case 0:
            instance.setSourceFilenameMask(aValue.toString());
            break;
        case 1:
            instance.setSourceEncoding(setEncodingName(aValue.toString()));
            break;
        case 2:
            instance.setTargetEncoding(setEncodingName(aValue.toString()));
            break;
        case 3:
            instance.setTargetFilenamePattern(aValue.toString());
            break;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
        case 3:
            return true;
        case 1:
            return sourceEncodingVariable;
        case 2:
            return targetEncodingVariable;
        }
        return false;
    }

    private String getEncodingName(final String enc) {
        return enc != null ? enc : ENC_AUTO_NAME;
    }

    private String setEncodingName(final String encName) {
        return ENC_AUTO_NAME.equals(encName) ? null : encName;
    }
}
