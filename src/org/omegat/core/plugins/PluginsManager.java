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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.CLIParameters;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.core.data.RemotePluginInformation;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.util.FileUtil;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * Plugin information and installation manager.
 *
 * @author Hiroshi Miura
 */
public final class PluginsManager {

    private static final String LIST_URL = "https://github.com/omegat-org/omegat-plugins/releases/download/continuous-release/plugins.MF";

    private Map<String, RemotePluginInformation> availablePlugins = null;
    private final Map<String, PluginInformation> installedPlugins;
    private static final Set<PluginInformation> PLUGIN_INFORMATIONS = new HashSet<>();
    protected static final List<Class<?>> LOADED_PLUGINS = new ArrayList<>();

    /**
     * Fallback properties file of plugin descriptions.
     */
    public static final String PLUGINS_LIST_FILE = "Plugins.properties";

    /**
     * plugin folder in program installation folder.
     */
    public static final File pluginsDir = new File(StaticUtils.installDir(), "plugins");
    /**
     * plugin folder for user installation.
     */
    public static final File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");

    /**
     * Plugin type definitions.
     */
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

        private final String typeValue;

        PluginType(String type) {
            typeValue = type;
        }

        public String getTypeValue() {
            return typeValue;
        }
    }

    public PluginsManager() {
        installedPlugins = new TreeMap<>();
        Collections.unmodifiableSet(PLUGIN_INFORMATIONS).stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .filter(info -> !installedPlugins.containsKey(getPluginInformationKey(info)))
                .forEach(info -> installedPlugins.put(getPluginInformationKey(info), info));
    }

    /**
     * Download plugin list from github repository.
     * @return set of PluginInformation
     */
    private static Set<RemotePluginInformation> getPluginsList() {
        Set<RemotePluginInformation> pluginInfo = new TreeSet<>();
        String raw_value;
        try {
            raw_value = HttpConnectionUtils.getURL(new URL(LIST_URL));
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
                            pluginInfo.add(new RemotePluginInformation(clazz, m));
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

    /**
     * Get plugin information installed to system specified by parameter.
     * @param info PluginInformation to search
     * @return PluginInformation when found, otherwise return null
     */
    public PluginInformation getInstalledPluginInformation(PluginInformation info) {
        return installedPlugins.getOrDefault(getPluginInformationKey(info), null);
    }

    /**
     * Parse Manifest from plugin jar file.
     * @param pluginJarFile plugin jar file
     * @return PluginInformation
     */
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
        sb.append("<h4>Author: ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>\n");
        } else {
            sb.append("Unknown<br/>\n");
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
     public Map<String, RemotePluginInformation> getAvailablePluginInformation() {
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

    public static void downloadAndInstallPlugin(final RemotePluginInformation info) {
        try {
            URL downloadUrl = new URL(info.getRemoteJarFileUrl());
            String jarFilename = info.getJarFilename();
            String sha256sum = info.getSha256Sum();
            PluginDownloadThread downloadThread = new PluginDownloadThread(downloadUrl, sha256sum, homePluginsDir, jarFilename);
            downloadThread.checkInterrupted();
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    private String getPluginInformationKey(PluginInformation info) {
        return info.getId();
    }

    /**
     * Loads all plugins from main classloader and from /plugins/ dir. We should
     * load all jars from /plugins/ dir first, because some plugin can use more
     * than one jar.
     */
    public static void loadPlugins(final Map<String, String> params) {
        try {
            // list all jars in /plugins/
            FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");
            List<File> fs = Stream.of(PluginsManager.pluginsDir, PluginsManager.homePluginsDir)
                    .flatMap(dir -> FileUtil.findFiles(dir, jarFilter).stream())
                    .collect(Collectors.toList());
            URL[] urls = new URL[fs.size()];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = fs.get(i).toURI().toURL();
                Log.logInfoRB("PLUGIN_LOAD_JAR", urls[i].toString());
            }
            boolean foundMain = false;
            // look on all manifests
            URLClassLoader pluginsClassLoader = new URLClassLoader(urls, PluginUtils.class.getClassLoader());
            for (Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF"); mlist
                    .hasMoreElements();) {
                URL mu = mlist.nextElement();
                try (InputStream in = mu.openStream()) {
                    Manifest m = new Manifest(in);
                    if ("org.omegat.Main".equals(m.getMainAttributes().getValue("Main-Class"))) {
                        // found main manifest - not in development mode
                        foundMain = true;
                        loadFromManifest(m, pluginsClassLoader, null);
                    } else {
                        loadFromManifest(m, pluginsClassLoader, mu);
                    }
                }
            }
            if (!foundMain) {
                // development mode - load from dev-manifests CLI arg
                String manifests = params.get(CLIParameters.DEV_MANIFESTS);
                if (manifests != null) {
                    for (String mf : manifests.split(File.pathSeparator)) {
                        try (InputStream in = new FileInputStream(mf)) {
                            loadFromManifest(new Manifest(in), pluginsClassLoader, null);
                        }
                    }
                } else {
                    // load from plugins property list
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(PLUGINS_LIST_FILE)) {
                        props.load(fis);
                        loadFromProperties(props, pluginsClassLoader);
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }

        // run base plugins
        for (Class<?> pl : BASE_PLUGIN_CLASSES) {
            try {
                pl.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    public static List<Class<?>> getFilterClasses() {
        return FILTER_CLASSES;
    }

    public static List<Class<?>> getTokenizerClasses() {
        return TOKENIZER_CLASSES;
    }

    public static List<Class<?>> getMarkerClasses() {
        return MARKER_CLASSES;
    }

    public static List<Class<?>> getMachineTranslationClasses() {
        return MACHINE_TRANSLATION_CLASSES;
    }

    public static List<Class<?>> getGlossaryClasses() {
        return GLOSSARY_CLASSES;
    }

    protected static final List<Class<?>> FILTER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> TOKENIZER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> MARKER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> MACHINE_TRANSLATION_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> GLOSSARY_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> BASE_PLUGIN_CLASSES = new ArrayList<>();

    /**
     * Parse one manifest file.
     *
     * @param m
     *            manifest
     * @param classLoader
     *            classloader
     * @throws ClassNotFoundException when fails to load class.
     */
    protected static void loadFromManifest(final Manifest m, final ClassLoader classLoader, final URL mu)
            throws ClassNotFoundException {
        String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
        if (pluginClasses != null) {
            for (String clazz : pluginClasses.split("\\s+")) {
                if (clazz.trim().isEmpty()) {
                    continue;
                }
                if (loadClass(clazz, classLoader)) {
                    if (mu == null) {
                        PLUGIN_INFORMATIONS.add(new PluginInformation(clazz, m, null, PluginInformation.STATUS.BUNDLED));
                    } else {
                        PLUGIN_INFORMATIONS.add(new PluginInformation(clazz, m, mu, PluginInformation.STATUS.INSTALLED));
                    }
                }
            }
        }

        loadFromManifestOld(m, classLoader, mu);
    }

    protected static void loadFromProperties(Properties props, ClassLoader classLoader) throws ClassNotFoundException {
        for (Object o : props.keySet()) {
            String key = o.toString();
            String[] classes = props.getProperty(key).split("\\s+");
            if (key.equals("plugin")) {
                for (String clazz : classes) {
                    if (loadClass(clazz, classLoader)) {
                        PLUGIN_INFORMATIONS.add(new PluginInformation(clazz, props, key, null, PluginInformation.STATUS.BUNDLED));
                    }
                }
            } else {
                for (String clazz : classes) {
                    if (loadClassOld(key, clazz, classLoader)) {
                        PLUGIN_INFORMATIONS.add(new PluginInformation(clazz, props, key, null, PluginInformation.STATUS.BUNDLED));
                    }
                }
            }
        }
    }

    protected static boolean loadClass(String clazz, ClassLoader classLoader) {
        try {
            Class<?> c = classLoader.loadClass(clazz);
            if (LOADED_PLUGINS.contains(c)) {
                Log.logInfoRB("PLUGIN_SKIP_PREVIOUSLY_LOADED", clazz);
                return false;
            }
            Method load = c.getMethod("loadPlugins");
            load.invoke(c);
            LOADED_PLUGINS.add(c);
            Log.logInfoRB("PLUGIN_LOAD_OK", clazz);
            return true;
        } catch (Throwable ex) {
            Log.logErrorRB(ex, "PLUGIN_LOAD_ERROR", clazz, ex.getClass().getSimpleName(), ex.getMessage());
            Core.pluginLoadingError(StringUtil.format(OStrings.getString("PLUGIN_LOAD_ERROR"), clazz,
                    ex.getClass().getSimpleName(), ex.getMessage()));
            return false;
        }
    }

    public static void unloadPlugins() {
        for (Class<?> p : LOADED_PLUGINS) {
            try {
                Method load = p.getMethod("unloadPlugins");
                load.invoke(p);
            } catch (Throwable ex) {
                Log.logErrorRB(ex, "PLUGIN_UNLOAD_ERROR", p.getClass().getSimpleName(), ex.getMessage());
            }
        }
    }

    /**
     * Old-style plugin loading.
     */
    protected static void loadFromManifestOld(final Manifest m, final ClassLoader classLoader, final URL mu)
            throws ClassNotFoundException {
        if (m.getMainAttributes().getValue("OmegaT-Plugin") == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (Map.Entry<String, Attributes> e : entries.entrySet()) {
            String key = e.getKey();
            Attributes attrs = e.getValue();
            String sType = attrs.getValue("OmegaT-Plugin");
            if (sType == null) {
                // WebStart signing section, or other section
                continue;
            }
            if (loadClassOld(sType, key, classLoader)) {
                if (mu == null) {
                    PLUGIN_INFORMATIONS.add(new PluginInformation(key, m, null, PluginInformation.STATUS.BUNDLED));
                } else {
                    PLUGIN_INFORMATIONS.add(new PluginInformation(key, m, mu, PluginInformation.STATUS.INSTALLED));
                }
            }
        }
    }

    protected static boolean loadClassOld(String sType, String key, ClassLoader classLoader)
            throws ClassNotFoundException {
        PluginsManager.PluginType pType;
        try {
            pType = PluginsManager.PluginType.valueOf(sType.toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            pType = PluginsManager.PluginType.UNKNOWN;
        }
        boolean loadOk = true;
        switch (pType) {
            case FILTER:
                FILTER_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            case TOKENIZER:
                TOKENIZER_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            case MARKER:
                MARKER_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            case MACHINETRANSLATOR:
                MACHINE_TRANSLATION_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            case BASE:
                BASE_PLUGIN_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            case GLOSSARY:
                GLOSSARY_CLASSES.add(classLoader.loadClass(key));
                Log.logInfoRB("PLUGIN_LOAD_OK", key);
                break;
            default:
                Log.logErrorRB("PLUGIN_UNKNOWN", sType, key);
                loadOk = false;
        }

        return loadOk;
    }

}
