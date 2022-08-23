/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.OStrings;

public class PluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5345248154613009632L;

    protected static final int COLUMN_NAME = 0;
    protected static final int COLUMN_CLASS = 1;
    protected static final int COLUMN_VERSION = 2;
    protected static final int COLUMN_AUTHOR = 3;
    protected static final int COLUMN_DESCRIPTION = 4;

    private static final String[] COLUMN_NAMES = { "NAME", "CLASS", "VERSION", "AUTHOR", "DESCRIPTION" };

    private final List<PluginInformation> listPlugins = new ArrayList<>();

    public PluginInfoTableModel() {
        PluginUtils.getPluginInformations().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .forEach(listPlugins::add);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount() {
       return listPlugins == null ? 0 : listPlugins.size();
    }

    @Override
    public String getColumnName(int column) {
        return OStrings.getString("PREFS_PLUGINS_COL_" + COLUMN_NAMES[column]);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PluginInformation plugin = listPlugins.get(rowIndex);
        Object returnValue = null;

        switch (columnIndex) {
        case COLUMN_CLASS:
            returnValue = plugin.getClassName();
            break;
        case COLUMN_NAME:
            returnValue = plugin.getName();
            break;
        case COLUMN_VERSION:
            returnValue = plugin.getVersion();
            break;
        case COLUMN_AUTHOR:
            returnValue = plugin.getAuthor();
            break;
        case COLUMN_DESCRIPTION:
            returnValue = plugin.getDescription();
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }
}
