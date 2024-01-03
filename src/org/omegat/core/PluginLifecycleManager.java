/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.omegat.core;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.MainClassLoader;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.StaticUtils;

/**
 * @author miurahr
 */
public final class PluginLifecycleManager {

    private static PluginLifecycleManager pluginLifecycleManager;

    private final Map<String, Path> pluginDirectories = new HashMap<>();
    private final Map<String, MainClassLoader> pluginLayers = new HashMap<>();

    public static PluginLifecycleManager getInstance() {
        if (pluginLifecycleManager == null) {
            pluginLifecycleManager = new PluginLifecycleManager();
        }
        return pluginLifecycleManager;
    }

    /**
     * Plugin lifecycle manager.
     * <p>
     * This is a class to manage plugin life cycles.
     * We manage plugins with several plugin layers; user plugins, system plugins and system modules.
     * We also handle theme plugins as special for treatment, we also have ui plugin layer.
     * We look the following directories.
     * <ul>
     * <li>(configdir)/plugins/ User level 3rd party plugins</li>
     * <li>(installdir/plugins/ System level 3rd party plugins</li>
     * <li>(installdir)/modules/ OmegaT genuine sub-component</li>
     * </ul>
     */
    private PluginLifecycleManager() {
        initPluginDirectories();
        initPluginLayers();
    }

    private void initPluginLayers() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        pluginLayers.put("module", new MainClassLoader(cl));
        pluginLayers.put("ui", new MainClassLoader(pluginLayers.get("module")));
        pluginLayers.put("system", new MainClassLoader(cl));
        pluginLayers.put("user", new MainClassLoader(cl));
    }

    private void initPluginDirectories() {
        pluginDirectories.put("user", Paths.get(StaticUtils.getConfigDir(), "plugins"));
        pluginDirectories.put("system", Paths.get(StaticUtils.installDir(), "plugins"));
        if (Paths.get(StaticUtils.installDir(), "build").toFile().exists()) {
            // when developers run on a source code tree, add system plugins
            pluginDirectories.put("module", Paths.get(StaticUtils.installDir(), "build", "modules"));
        } else {
            pluginDirectories.put("module", Paths.get(StaticUtils.installDir()).resolve("modules"));
        }
    }

    public void loadPlugins(Map<String, String> params) {
        // 1. load from application context class loader.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (!PluginUtils.checkMainClass(cl)) {
            // run from development environment.
            PluginUtils.loadSystemPluginsForDev(params, cl);
        }
        PluginUtils.loadPlugins(cl);


        // 2. loads from modules.
        List<URL> moduleUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get("module").toFile()));
        MainClassLoader moduleClassLoader = pluginLayers.get("module");
        moduleUrlList.forEach(moduleClassLoader::addJarToClassPath);
        PluginUtils.loadPlugins(moduleClassLoader);

        // 3. loads from system plugins (deprecated).
        List<URL> systemUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get("system").toFile()));
        MainClassLoader systemClassLoader = pluginLayers.get("system");
        systemUrlList.forEach(systemClassLoader::addJarToClassPath);
        PluginUtils.loadPlugins(systemClassLoader);

        // 4. loads from user plugins.
        List<URL> userUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get("user").toFile()));
        MainClassLoader userClassLoader = pluginLayers.get("user");
        userUrlList.forEach(userClassLoader::addJarToClassPath);
        PluginUtils.loadPlugins(userClassLoader);

        // 5. set theme classes
        MainClassLoader uiClassLoader = pluginLayers.get("ui");
        List<URL> themeUrlList = PluginUtils.getThemePluginJars();
        themeUrlList.forEach(uiClassLoader::addJarToClassPath);

        // 6. set filter classes
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
    }

    public void unloadPlugins() {
        PluginUtils.unloadPlugins();
    }

    public ClassLoader getPluginClassLoader(String name) {
        if (!pluginLayers.containsKey(name)) {
            pluginLayers.put(name, new MainClassLoader());
        }
        return pluginLayers.get(name);
    }
}
