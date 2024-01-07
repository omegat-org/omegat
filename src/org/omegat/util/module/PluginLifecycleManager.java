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

package org.omegat.util.module;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import org.omegat.MainClassLoader;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * @author miurahr
 */
public final class PluginLifecycleManager {

    private static PluginLifecycleManager pluginLifecycleManager;

    private final Map<String, Path> pluginDirectories = new HashMap<>();
    private final Map<String, MainClassLoader> pluginClassLoaders = new HashMap<>();

    private static final String MODULE_LAYER = "modules";
    public static final String PLUGIN_LAYER = "plugins";
    public static final String UI_LAYER = "ui";

    private static final String SYSTEM_PLUGIN_KEY = "system";
    private static final String USER_PLUGIN_KEY = "user";
    private static final String MODULE_KEY = "module";

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
        // UI_LAYER class loader should have hierarchy SYSTEM->UI
        // Because UIManager shall find VLDocking library is in
        // the SYSTEM class loader.
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        pluginClassLoaders.put(UI_LAYER, new MainClassLoader(cl));
        pluginClassLoaders.put(MODULE_LAYER, new MainClassLoader(cl));
        pluginClassLoaders.put(PLUGIN_LAYER, new MainClassLoader(cl));
    }

    private void initPluginDirectories() {
        pluginDirectories.put(USER_PLUGIN_KEY, Paths.get(StaticUtils.getConfigDir(), "plugins"));
        pluginDirectories.put(SYSTEM_PLUGIN_KEY, Paths.get(StaticUtils.installDir(), "plugins"));
        if (Paths.get(StaticUtils.installDir(), "build").toFile().exists()) {
            // when developers run on a source code tree, add system plugins
            pluginDirectories.put(MODULE_KEY, Paths.get(StaticUtils.installDir(), "build", "modules"));
        } else {
            pluginDirectories.put(MODULE_KEY, Paths.get(StaticUtils.installDir()).resolve("modules"));
        }
    }

    public void loadPlugins(Map<String, String> params) {
        // 1. load from application context class loader.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL mainClassJar = PluginUtils.getMainClassJarFile(cl);
        if (mainClassJar == null) {
            // run from development environment.
            PluginUtils.loadSystemPluginsForDev(params, cl);
        } else {
            // run from OmegaT.jar
            loadPlugin(cl, mainClassJar);
        }

        // 2. loads from modules.
        List<URL> moduleUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get(MODULE_KEY).toFile()));
        moduleUrlList.forEach(url -> loadPlugin(url, MODULE_LAYER));

        // 3. loads from system plugins.
        List<URL> systemUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get(SYSTEM_PLUGIN_KEY).toFile()));
        systemUrlList.forEach(url -> loadPlugin(url, PLUGIN_LAYER));

        // 4. loads from user plugins.
        List<URL> userUrlList = PluginUtils.populatePluginUrlList(
                Collections.singletonList(pluginDirectories.get(USER_PLUGIN_KEY).toFile()));
        userUrlList.forEach(url -> loadPlugin(url, PLUGIN_LAYER));

        // 5. set filter classes
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
    }

    public void loadPlugin(URL url, String layer) {
        MainClassLoader pluginsClassLoader = new MainClassLoader(pluginClassLoaders.get(layer));
        pluginsClassLoader.addJarToClasspath(url);
        if (loadPlugin(pluginsClassLoader, url)) {
            pluginClassLoaders.put(url.toString(), pluginsClassLoader);
        }
    }

    private boolean loadPlugin(ClassLoader pluginsClassLoader, URL url) {
        boolean result = false;
        try {
            Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF");
            while (mlist.hasMoreElements()) {
                URL mu = mlist.nextElement();
                if (url != null && !Files.isSameFile(getJarFilePathFromResourceUrl(mu), Path.of(url.getFile()))) {
                    continue;
                }
                try (InputStream in = mu.openStream()) {
                    Manifest m = new Manifest(in);
                    if (url != null && "theme".equals(m.getMainAttributes().getValue("Plugin-Category"))) {
                        // theme plugin should load from uiClassLoader
                        MainClassLoader uiClassLoader = pluginClassLoaders.get(UI_LAYER);
                        uiClassLoader.addJarToClasspath(url);
                        PluginUtils.loadFromManifest(m, uiClassLoader, mu);
                    } else {
                        PluginUtils.loadFromManifest(m, pluginsClassLoader, mu);
                        result = true;
                    }
                } catch (ClassNotFoundException e) {
                    Log.log(e);
                    return false;
                } catch (UnsupportedClassVersionError e) {
                    Log.logWarningRB("PLUGIN_JAVA_VERSION_ERROR", getJarFilePathFromResourceUrl(mu));
                    return false;
                }
            }
        } catch (IOException ex) {
            Log.log(ex);
            return false;
        }
        return result;
    }

    public void unloadPlugins() {
        PluginUtils.unloadPlugins();
    }

    public boolean unloadPlugin(String className) {
        ClassLoader classLoader = pluginClassLoaders.get(className);
        if (classLoader == null) {
            Log.logErrorRB("PLUGIN_UNLOAD_ERROR", className, "ClassLoader not found.");
            return false;
        }
        try {
            Class<?> p = Class.forName(className, false, classLoader);
            Method load = p.getMethod("unloadPlugins");
            load.invoke(p);
            pluginClassLoaders.remove(className);
        } catch (Throwable ex) {
            Log.logErrorRB(ex, "PLUGIN_UNLOAD_ERROR", className, ex.getMessage());
            return false;
        }
        return true;
    }

    public ClassLoader getPluginClassLoader(String name) {
        if (!pluginClassLoaders.containsKey(name)) {
            pluginClassLoaders.put(name, new MainClassLoader());
        }
        return pluginClassLoaders.get(name);
    }

    private Path getJarFilePathFromResourceUrl(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return Path.of(connection.getJarFileURL().getFile());
    }
}
