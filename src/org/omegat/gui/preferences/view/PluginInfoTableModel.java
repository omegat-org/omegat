package org.omegat.gui.preferences.view;

import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.core.plugins.PluginInstaller;
import org.omegat.util.OStrings;

class PluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5345248154613009632L;
    private static final String[] COLUMN_NAMES = {"STAT", "CATEGORY", "NAME", "VERSION"}; // NOI18N
    private final Map<String, PluginInformation> listPlugins;

    public static final int COLUMN_STAT = 0;
    public static final int COLUMN_CATEGORY = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_VERSION = 3;

    public PluginInfoTableModel() {
        listPlugins = PluginInstaller.getInstalledPlugins();
    }

    public final PluginInformation getValueAt(int rowIndex) {
        return new Vector<>(listPlugins.values()).get(rowIndex);
    }

    @Override
    public final Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public final boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public final int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public final int getRowCount() {
        return listPlugins == null ? 0 : listPlugins.size();
    }

    @Override
    public final String getColumnName(int column) {
        return OStrings.getString("PREFS_PLUGINS_COL_" + COLUMN_NAMES[column]);
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
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
}
