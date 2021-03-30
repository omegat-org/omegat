/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.gui.dialogs;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.omegat.core.data.PluginInformation;
import org.omegat.core.plugins.PluginsManager;
import org.omegat.gui.preferences.view.PluginDetailsPane;

/**
 *
 * @author Hiroshi Miura
 */
public class PluginInstallerDialogController {
    private final Set<PluginInformation> pluginInformations;
    private final PluginsManager pluginsManager;
    private final Map<String, String> config;
    public static final String DO_INSTALL_KEY = "install-plugin-action";
    public static final String CURRENT_VERSION = "current-plugin-vesrion";
    public static final String ACTION_NAME = "install-action-name";

    public PluginInstallerDialogController(Set<PluginInformation> pluginInformations, PluginsManager pluginsManager,
                                           Map<String, String> config) {
        this.pluginInformations = pluginInformations;
        this.pluginsManager = pluginsManager;
        this.config = config;
    }

    public final void show(java.awt.Frame parent) {
        PluginInstallerDialog pluginInstallerDialog = new PluginInstallerDialog(parent, true);
        PluginDetailsPane pluginDetailsPane = new PluginDetailsPane();
        StringBuilder sb = new StringBuilder("<h2>Install a new plugin</h2>");
        sb.append(pluginInformations.stream().map(pluginsManager::formatDetailText).collect(Collectors.joining()));
        if (config.get(CURRENT_VERSION) != null) {
            sb.append("<br/>\n Current plugin version is: ").append(config.get(CURRENT_VERSION)).append("<br/>\n");
        }
        pluginDetailsPane.setText(sb.toString());
        pluginInstallerDialog.panelPluginDetails.add(pluginDetailsPane);
        pluginDetailsPane.setVisible(true);
        pluginInstallerDialog.cancelButton.addActionListener(e -> {
            config.put(DO_INSTALL_KEY, Boolean.toString(false));
            pluginInstallerDialog.dispose();
        });
        pluginInstallerDialog.okButton.setText(config.get(ACTION_NAME));
        pluginInstallerDialog.okButton.addActionListener(e -> {
            config.put(DO_INSTALL_KEY, Boolean.toString(true));
            pluginInstallerDialog.dispose();
        });
        pluginInstallerDialog.setVisible(true);
    }
}
