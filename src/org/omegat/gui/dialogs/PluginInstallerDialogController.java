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

import org.omegat.core.data.PluginInformation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Hiroshi Miura
 */
public class PluginInstallerDialogController {
    private final Set<PluginInformation> pluginInformations;
    private final Map<String, String> config;
    public static final String DO_INSTALL_KEY = "install-plugin";

    public PluginInstallerDialogController(Set<PluginInformation> pluginInformations, Map<String, String> config) {
        this.pluginInformations = pluginInformations;
        this.config = config;
    }

    public void show(java.awt.Frame parent) {
        PluginInstallerDialog pluginInstallerDialog = new PluginInstallerDialog(parent, true);
        String sb = pluginInformations.stream().map(this::formatDetailText).collect(Collectors.joining());
        pluginInstallerDialog.pluginInformationsTextArea.setText(sb);
        pluginInstallerDialog.cancelButton.addActionListener(e -> {
            config.put(DO_INSTALL_KEY, Boolean.toString(false));
            pluginInstallerDialog.dispose();
        });
        pluginInstallerDialog.okButton.addActionListener(e -> {
            config.put(DO_INSTALL_KEY, Boolean.toString(true));
            pluginInstallerDialog.dispose();
        });
        pluginInstallerDialog.setVisible(true);
    }

    private String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(info.getName()).append("\n");
        if (info.getCategory() != null) sb.append("Category: ").append(info.getCategory()).append("\n");
        if (info.getVersion() != null) sb.append("Version: ").append(info.getVersion()).append("\n");
        sb.append("ClassName: ").append(info.getClassName()).append("\n\n");
        if (info.getDescription() != null) sb.append(info.getDescription()).append("\n");
        sb.append("\n");
        return sb.toString();
    }
}
