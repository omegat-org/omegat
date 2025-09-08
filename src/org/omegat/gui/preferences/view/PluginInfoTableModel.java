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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.util.OStrings;

public class PluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5345248154613009633L;

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_CATEGORY = 1;

    private static final String[] COLUMN_NAMES = {
            "PREFS_PLUGINS_COL_NAME",
            "PREFS_PLUGINS_COL_CATEGORY"
    };

    private final List<PluginInformation> listPlugins;

    public PluginInfoTableModel() {
        listPlugins = new ArrayList<>();
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
        case COLUMN_CATEGORY:
            returnValue = plugin.getCategory().getLocalizedValue();
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }

    public PluginInformation getItemAt(int rowIndex) {
        return listPlugins.get(rowIndex);
    }

    /**
     * Update the model with new plugin data
     * @param plugins List of plugins to display
     */
    public void setPlugins(List<PluginInformation> plugins) {
        synchronized (this) {
            listPlugins.clear();
            if (plugins != null) {
                listPlugins.addAll(plugins);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Clear all plugin data
     */
    public void clear() {
        synchronized (this) {
            listPlugins.clear();
        }
        fireTableDataChanged();
    }

    /**
     * Get all plugins currently in the model
     * @return List of plugins
     */
    public List<PluginInformation> getPlugins() {
        return new ArrayList<>(listPlugins);
    }

}
