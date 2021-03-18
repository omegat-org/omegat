/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.core.plugins.PluginsManager;
import org.omegat.gui.dialogs.ChoosePluginFile;
import org.omegat.gui.dialogs.PluginInstallerDialogController;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DesktopWrapper;
import org.omegat.util.gui.TableColumnSizer;


/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class PluginsPreferencesController extends BasePreferencesController {

    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";
    private PluginsManager pluginsManager = new PluginsManager();
    private PluginsPreferencesPanel panel;
    private TableRowSorter<LocalPluginInfoTableModel> sorter;
    private TableRowSorter<RemotePluginInfoTableModel> availableSorter;
    private PluginDetailsPane localPluginDetailsPane;
    private PluginDetailsPane remotePluginDetailsPane;
    private final Map<String, String> installConfig = new HashMap<>();

    @Override
    public final JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    final void selectRowAction(ListSelectionEvent evt) {
        int rowIndex = panel.tablePluginsInfo.convertRowIndexToModel(panel.tablePluginsInfo.getSelectedRow());
        if (rowIndex == -1) {
            localPluginDetailsPane.setText("");
        } else {
            LocalPluginInfoTableModel model = (LocalPluginInfoTableModel) panel.tablePluginsInfo.getModel();
            String name = (String) model.getValueAt(rowIndex, LocalPluginInfoTableModel.COLUMN_NAME);
            StringBuilder sb = new StringBuilder();
            Optional<PluginInformation> pluginInformation = PluginsManager.getInstalledPluginInformation().stream()
                    .filter(info -> info.getName().equals(name))
                    .findFirst();
            pluginInformation.ifPresent(information -> sb.append(pluginsManager.formatDetailText(information)));
            localPluginDetailsPane.setText(sb.toString());
        }
    }

    final void selectRowActionRemote(ListSelectionEvent evt) {
        int rowIndex = panel.tableAvailablePluginsInfo.convertRowIndexToModel(
                panel.tableAvailablePluginsInfo.getSelectedRow());
        if (rowIndex == -1) {
            remotePluginDetailsPane.setText("");
        } else {
            RemotePluginInfoTableModel model = (RemotePluginInfoTableModel) panel.tableAvailablePluginsInfo.getModel();
            String name = (String) model.getValueAt(rowIndex, RemotePluginInfoTableModel.COLUMN_NAME);
            StringBuilder sb = new StringBuilder();
            Optional<PluginInformation> pluginInformation = PluginsManager.getAvailablePluginInformation().stream()
                    .filter(info -> info.getName().equals(name))
                    .findFirst();
            pluginInformation.ifPresent(information -> sb.append(pluginsManager.formatDetailText(information)));
            remotePluginDetailsPane.setText(sb.toString());
        }
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_PLUGINS");
    }

    private void initGui() {
        panel = new PluginsPreferencesPanel();
        localPluginDetailsPane = new PluginDetailsPane();
        remotePluginDetailsPane = new PluginDetailsPane();
        panel.panelPluginDetails.add(localPluginDetailsPane);
        TableColumnSizer.autoSize(panel.tablePluginsInfo, 0, true);
        panel.panelAvailablePluginDetails.add(remotePluginDetailsPane);
        TableColumnSizer.autoSize(panel.tableAvailablePluginsInfo, 0, true);
        panel.browsePluginsButton.addActionListener(e -> {
            try {
                DesktopWrapper.browse(URI.create(PLUGINS_WIKI_URL));
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(panel, ex.getLocalizedMessage(),
                        OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        });
        LocalPluginInfoTableModel model = (LocalPluginInfoTableModel) panel.tablePluginsInfo.getModel();
        RemotePluginInfoTableModel availableModel = (RemotePluginInfoTableModel) panel.tableAvailablePluginsInfo.getModel();
        sorter = new TableRowSorter<>(model);
        availableSorter = new TableRowSorter<>(availableModel);
        panel.tablePluginsInfo.setRowSorter(sorter);
        panel.tableAvailablePluginsInfo.setRowSorter(availableSorter);

        panel.tablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowAction);
        panel.tableAvailablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowActionRemote);

        panel.installFromDiskButton.addActionListener(e -> {
            ChoosePluginFile choosePluginFile = new ChoosePluginFile();
            int choosePluginFileResult = choosePluginFile.showOpenDialog(Core.getMainWindow().getApplicationFrame());
            if (choosePluginFileResult != JFileChooser.APPROVE_OPTION) {
                // user press 'Cancel' in dialog
                return;
            }
            final File pluginFile = choosePluginFile.getSelectedFile();
            File pluginJarFile;
            if (pluginFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                // TODO: support zip
                // extract zip file into temporary directory and look up *.jar file
                // and set it to pluginJarFile.
                return;
            } else {
                pluginJarFile = pluginFile;
            }
            // check manifest
            Set<PluginInformation> pluginInfo = pluginsManager.parsePluginJarFileManifest(pluginJarFile, false);
            new PluginInstallerDialogController(pluginInfo,
                    installConfig).show(Core.getMainWindow().getApplicationFrame());
            boolean result = Boolean.parseBoolean(installConfig.get(PluginInstallerDialogController.DO_INSTALL_KEY));
            if (result) {
                try {
                    pluginsManager.installPlugin(pluginJarFile);
                } catch (IOException ex) {
                    Log.log(ex);
                }
            }
        });
    }

    @Override
    protected void initFromPrefs() {
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    public void persist() {
    }
}
