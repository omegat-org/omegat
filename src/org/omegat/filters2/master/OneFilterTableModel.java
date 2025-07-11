/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2025 Hiroshi Miura
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

import javax.swing.table.AbstractTableModel;

import org.jetbrains.annotations.Nullable;
import org.omegat.filters2.IFilter;
import org.omegat.util.OStrings;

import gen.core.filters.Files;
import gen.core.filters.Filter;

/**
 * Wrapper around a single file filter class Manages entries in XML config file
 * and provides a table model.
 *
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class OneFilterTableModel extends AbstractTableModel {

    private final Filter filter;
    private final boolean sourceEncodingVariable;
    private final boolean targetEncodingVariable;

    private static final String ENC_AUTO_NAME = OStrings.getString("ENCODING_AUTO");

    public OneFilterTableModel(final Filter f) {
        this.filter = f;
        IFilter fi = FilterMaster.getFilterInstance(f.getClassName());
        if (fi != null) {
            sourceEncodingVariable = fi.isSourceEncodingVariable();
            targetEncodingVariable = fi.isTargetEncodingVariable();
        } else {
            sourceEncodingVariable = false;
            targetEncodingVariable = false;
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // TableModel implementation
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
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
        default:
            throw new IllegalArgumentException("Invalid column index: " + columnIndex + " for table model: ");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getRowCount() {
        return filter.getFiles().size();
    }

    @Override
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
        default:
            throw new IllegalArgumentException("Invalid column index: " + columnIndex + " for table model: ");
        }
    }

    @Override
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
        default:
            throw new IllegalArgumentException("Invalid column index: " + columnIndex + " for table model: ");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return true;
        case 1:
            return sourceEncodingVariable;
        case 2:
            return targetEncodingVariable;
        case 3:
            return true;
        default:
            throw new IllegalArgumentException("Invalid column index: " + columnIndex + " for table model: ");
        }
    }

    private String getEncodingName(final @Nullable String enc) {
        return enc != null ? enc : ENC_AUTO_NAME;
    }

    private String setEncodingName(final String encName) {
        return ENC_AUTO_NAME.equals(encName) ? null : encName;
    }
}
