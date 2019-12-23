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

    private static final String[] COLUMN_NAMES = { "CLASS", "NAME", "VERSION", "AUTHOR", "DESCRIPTION" };

    private List<PluginInformation> listPlugins = new ArrayList<>();

    public PluginInfoTableModel() {
        PluginUtils.PLUGIN_INFORMATIONS.stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .forEach(info -> listPlugins.add(info));
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
        return PluginUtils.PLUGIN_INFORMATIONS.size();
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
