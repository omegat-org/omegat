package org.omegat.gui.preferences.view;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

import org.omegat.core.data.PluginInformation;
import org.omegat.core.plugins.PluginsManager;
import org.omegat.util.OStrings;

public class RemotePluginInfoTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 52734789123814035L;


    protected static final int COLUMN_CATEGORY = 0;
    protected static final int COLUMN_NAME = 1;
    protected static final int COLUMN_VERSION = 2;

    private static final String[] COLUMN_NAMES = { "CATEGORY", "NAME", "VERSION" };

    private final Map<String, PluginInformation> listPlugins = new TreeMap<>();

    public RemotePluginInfoTableModel() {
        PluginsManager.getAvailablePluginInformation().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .filter(info -> !existInListPlugins(info))
                .forEach(info -> listPlugins.put(getPluginInformationKey(info), info));
    }

    private String getPluginInformationKey(PluginInformation info) {
        return info.getName() + info.getAuthor() + info.getVersion();
    }

    private boolean existInListPlugins(PluginInformation info) {
        return listPlugins.containsKey(getPluginInformationKey(info));
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
        default:
            throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }

}
