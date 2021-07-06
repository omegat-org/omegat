/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               2021 Hiroshi Miura
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

import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.util.PluginInstaller;
import org.omegat.util.OStrings;

public class PluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5345248154613009632L;

    public static final int COLUMN_STAT = 0;
    public static final int COLUMN_CATEGORY = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_VERSION = 3;

    private static final String[] COLUMN_NAMES = {"STAT", "CATEGORY", "NAME", "VERSION"}; // NOI18N

    private final Map<String, PluginInformation> listPlugins;

    public PluginInfoTableModel() {
        listPlugins = PluginInstaller.getPluginInformations();
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
        PluginInformation plugin = new Vector<>(listPlugins.values()).get(rowIndex);
        Object returnValue;

        switch (columnIndex) {
            case COLUMN_NAME:
                returnValue = plugin.getName();
                break;
            case COLUMN_VERSION:
                returnValue = plugin.getVersion();
                break;
            case COLUMN_CATEGORY:
                returnValue = plugin.getCategory();
                break;
            case COLUMN_STAT:
                if (plugin.getStatus() == PluginInformation.Status.INSTALLED) {
                    returnValue = OStrings.getString("PREFS_PLUGINS_UPTODATE");
                } else if (plugin.getStatus() == PluginInformation.Status.UPGRADABLE) {
                    returnValue = OStrings.getString("PREFS_PLUGINS_UPGRADABLE");
                } else if (plugin.getStatus() == PluginInformation.Status.BUNDLED) {
                    returnValue = OStrings.getString("PREFS_PLUGINS_BUNDLED");
                } else {
                    returnValue = OStrings.getString("PREFS_PLUGINS_NEW");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }

    public PluginInformation getValueAt(int rowIndex) {
        return new Vector<>(listPlugins.values()).get(rowIndex);
    }
}
