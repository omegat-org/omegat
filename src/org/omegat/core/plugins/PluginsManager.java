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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;
import org.omegat.util.WikiGet;

/**
 * Plugin information and installation manager.
 *
 * @author Hiroshi Miura
 */
public class PluginsManager {

    /**
     * Plugin list download URL.
     */
    private static final String LIST_URL = "https://raw.githubusercontent.com/miurahr/omegat-plugins/main/plugins.MF";
    private Map<String, PluginInformation> availablePlugins = null;
    private Map<String, PluginInformation> installedPlugins;

    public static final File pluginsDir = new File(StaticUtils.installDir(), "plugins");
    public static final File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");

    public enum PluginType {
        FILTER("filter"),
        TOKENIZER("tokenizer"),
        MARKER("marker"),
        MACHINETRANSLATOR("machinetranslator"),
        BASE("base"),
        GLOSSARY("glossary"),
        DICTIONARY("dictionary"),
        MISCELLANEOUS("miscellaneous"),
        UNKNOWN("Undefined");

        private String typeValue;

        PluginType(String type) {
            this.typeValue = type;
        }

        public String getTypeValue() {
            return typeValue;
        }
    };

    public PluginsManager() {
        installedPlugins = new TreeMap<>();
        PluginUtils.getPluginInformations().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .filter(info -> !installedPlugins.containsKey(getPluginInformationKey(info)))
                .forEach(info -> installedPlugins.put(getPluginInformationKey(info), info));
    }

    /**
     * Download plugin list from github repository.
     * @return set of PluginInformation
     */
    private static Set<PluginInformation> getPluginsList() {
        Set<PluginInformation> pluginInfo = new TreeSet<>();
        String raw_value;
        try {
            raw_value = WikiGet.getURL(LIST_URL);
            Scanner scanner = new Scanner(raw_value);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    sb.append(line).append("\n");
                } else {
                    try {
                        Manifest m = new Manifest(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
                        String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
                        for (String clazz : pluginClasses.split("\\s+")) {
                            if (clazz.trim().isEmpty()) {
                                continue;
                            }
                            pluginInfo.add(new PluginInformation(clazz, m, null, PluginInformation.STATUS.UNINSTALLED));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb = new StringBuilder();
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pluginInfo;
    }

    public PluginInformation getInstalledPluginInformation(PluginInformation info) {
        return installedPlugins.getOrDefault(getPluginInformationKey(info), null);
    }

    /**
     * Install specified plugin jar file.
     * @param pluginJarFile plugin jar.
     * @throws IOException when I/O error happened.
     */
    public void installPlugin(File pluginJarFile, File upgrade) throws IOException {
       File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
       if (upgrade != null) {
           FileUtils.forceDelete(upgrade);
       }
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
                            pluginInfo.add(new PluginInformation(clazz, m, null, PluginInformation.STATUS.UNINSTALLED));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return pluginInfo;
    }

    /**
     * Format plugin information for details pane of UI.
     * @param info PluginInformation to show
     * @return HTML text
     */
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

    /**
     * Return installed plugins.
     * @return Set of PluginInformation
     */
    public Map<String, PluginInformation> getInstalledPlugins() {
        return installedPlugins;
    }

   /**
     * Return known available plugins.
     * It can has plugins that has already installed.
     * @return Map of PluginInformation
     */
     public Map<String, PluginInformation> getAvailablePluginInformation() {
        if (availablePlugins == null) {
            availablePlugins = new TreeMap<>();
            getPluginsList().stream()
                    .sorted(Comparator.comparing(PluginInformation::getClassName))
                    .forEach(info -> {
                        String key = getPluginInformationKey(info);
                        if (installedPlugins.get(key) != null) {
                            PluginInformation installed = installedPlugins.get(key);
                            if (info.compareTo(installed) > 0) {
                                info.setStatus(PluginInformation.STATUS.UPGRADABLE);
                            } else {
                                info.setStatus(PluginInformation.STATUS.INSTALLED);
                            }
                        } else {
                            info.setStatus(PluginInformation.STATUS.UNINSTALLED);
                        }
                        availablePlugins.put(key, info);
                    });
        }
        return availablePlugins;
    }

    private String getPluginInformationKey(PluginInformation info) {
        return info.getName() + info.getAuthor();
    }
}
