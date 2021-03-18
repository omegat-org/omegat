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

package org.omegat.core.plugins;

import org.apache.commons.io.FileUtils;
import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

public class PluginsManager {

    public PluginsManager() {
    }

    public void installPlugin(File pluginJarFile) throws IOException {
        File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
        FileUtils.copyFileToDirectory(pluginJarFile, homePluginsDir, true);
    }

    public Set<PluginInformation> parsePluginJarFileManifest(File pluginJarFile) {
        Set<PluginInformation> pluginInfo = new HashSet<>();
        try {
            URL[] urls = new URL[1];
            urls[0] = pluginJarFile.toURI().toURL();
            URLClassLoader pluginsClassLoader = new URLClassLoader(urls,
                    PluginsPreferencesController.class.getClassLoader());
            for (Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF"); mlist
                    .hasMoreElements();) {
                URL mu = mlist.nextElement();
                try (InputStream in = mu.openStream()) {
                    Manifest m = new Manifest(in);
                    String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
                    if (pluginClasses != null) {
                        for (String clazz : pluginClasses.split("\\s+")) {
                            if (clazz.trim().isEmpty()) {
                                continue;
                            }
                            pluginInfo.add(new PluginInformation(clazz, m));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return pluginInfo;
    }

    public String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(info.getName()).append("</h2>\n");
        sb.append("<h4>Author: ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>\n");
        } else {
            sb.append("Unknown<br/>\n");
        }
        if (info.getCategory() != null) {
            sb.append("Category: ").append(info.getCategory()).append("<br/>\n");
        }
        if (info.getVersion() != null) {
            sb.append("Version: ").append(info.getVersion()).append("<br/>\n");
        }
        sb.append("</h4>\n");
        if (info.getDescription() != null) {
            sb.append("<p>").append(info.getDescription()).append("</p>\n");
        }
        if (info.getLink() != null) {
            sb.append("<br/><div><a href=\"").append(info.getLink()).append("\">Plugin homepage</a></div>");
        }
        return sb.toString();
    }

    public Collection<PluginInformation> getInstalledPluginInformation() {
        return PluginUtils.getPluginInformations();
    }

    public Collection<PluginInformation> getAvailablePluginInformation() {
        return PluginUtils.getPluginInformations();
    }

}
