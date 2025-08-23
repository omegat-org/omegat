/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
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

package org.omegat.gui.preferences.view;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.util.OStrings;
import org.omegat.util.PluginInstaller;

public class PluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5345248154613009633L;

    public static final int COLUMN_STAT = 0;
    public static final int COLUMN_CATEGORY = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_AUTHOR = 3;
    public static final int COLUMN_VERSION = 4;

    private static final String[] COLUMN_NAMES = { "PREFS_PLUGINS_COL_STAT", "PREFS_PLUGINS_COL_CATEGORY",
            "PREFS_PLUGINS_COL_NAME", "PREFS_PLUGINS_COL_AUTHOR", "PREFS_PLUGINS_COL_VERSION"};

    private final List<PluginInformation> listPlugins;

    public PluginInfoTableModel() {
        listPlugins = PluginInstaller.getInstance().getPluginList().stream().filter(p -> !p.isBundled())
                .collect(Collectors.toList());
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
        return OStrings.getString(COLUMN_NAMES[column]);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row > listPlugins.size() - 1) {
            return null;
        }
        PluginInformation plugin = listPlugins.get(row);
        Object returnValue;

        switch (column) {
        case COLUMN_NAME:
            returnValue = plugin.getName();
            break;
        case COLUMN_VERSION:
            returnValue = plugin.getVersion();
            break;
        case COLUMN_AUTHOR:
            returnValue = plugin.getAuthor();
            break;
        case COLUMN_CATEGORY:
            returnValue = plugin.getCategory().getLocalizedValue();
            break;
        case COLUMN_STAT:
            returnValue = plugin.getStatus().getLocalizedValue();
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }

    public PluginInformation getItemAt(int rowIndex) {
        return listPlugins.get(rowIndex);
    }

    public void updateModel(boolean showBundledPlugins) {
        List<PluginInformation> newListOfPlugins = PluginInstaller.getInstance().getPluginList().stream()
                .filter(p -> showBundledPlugins || !p.isBundled()).collect(Collectors.toList());
        synchronized (this) {
            listPlugins.clear();
            listPlugins.addAll(newListOfPlugins);
        }
        fireTableDataChanged();
    }
}
