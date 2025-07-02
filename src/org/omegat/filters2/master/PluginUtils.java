/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               2021-2022 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.master;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.CLIParameters;
import org.omegat.MainClassLoader;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.Tokenizer;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.VersionChecker;

/**
 * Static utilities for OmegaT filter plugins.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    public static final String PLUGINS_LIST_FILE = "Plugins.properties";

    private static final String PLUGIN_CATEGORY = "Plugin-Category";
    private static final String MAIN_CLASS = "Main-Class";
    private static final String MANIFEST_MF = "META-INF/MANIFEST.MF";
    private static final String OMEGAT_PLUGINS = "OmegaT-Plugins";
    private static final String OMEGAT_PLUGIN = "OmegaT-Plugin";

    /**
     * Plugin type definitions.
     */
    public enum PluginType {
        /** File filters that provide IFilter API. */
        FILTER("filter"),
        /**
         * Tokenizers, currently bundled and it is for backward compatibility.
         */
        TOKENIZER("tokenizer"),
        /** Markers, that provide IMaker, mostly bundled. */
        MARKER("marker"),
        /**
         * Machine Translator service connectors, that provide
         * IMachineTranslation API.
         */
        MACHINETRANSLATOR("machinetranslator"),
        /** A plugin that change base of OmegaT system, not recommended. */
        BASE("base"),
        /**
         * Glosary, that provide IGlossary API.
         */
        GLOSSARY("glossary"),
        /**
         * Dictionary files/services connectors, that provide IDictionary and/or
         * IDictionaryFactory API.
         */
        DICTIONARY("dictionary"),
        /**
         * theme, that register Swing-Look-and-Feel with OmegaT properties into
         * UIManager.
         */
        THEME("theme"),
        /**
         * team repository version control system connector plugins.
         */
        REPOSITORY("repository"),
        /**
         * Misc plugins, such as a GUI extension like web browser support.
         */
        MISCELLANEOUS("miscellaneous"),
        /**
         * Spellchecker plugins.
         */
        SPELLCHECK("spellcheck"),
        /**
         * language plugin that bundles LanguageTool-language module and
         * spell-check dictionaries.
         */
        LANGUAGE("language"),
        /**
         * When plugin does not define any of the above.
         */
        UNKNOWN("Undefined");

        private final String typeValue;

        PluginType(String type) {
            typeValue = type;
        }

        public String getTypeValue() {
            return typeValue;
        }

        public static PluginType getTypeByValue(String str) {
            if (!StringUtil.isEmpty(str)) {
                String sType = str.toLowerCase(Locale.ENGLISH);
                for (PluginType v : values()) {
                    if (v.getTypeValue().equals(sType)) {
                        return v;
                    }
                }
            }
            return UNKNOWN;
        }
    }

    private static final List<Class<?>> LOADED_PLUGINS = new ArrayList<>();
    private static final Set<PluginInformation> PLUGIN_INFORMATIONS = new HashSet<>();

    private static final Map<PluginType, MainClassLoader> MAINCLASSLOADERS = new EnumMap<>(PluginType.class);

    /** Private constructor to disallow creation */
    private PluginUtils() {
    }

    private static final String OMEGAT_MAIN_CLASS = "org.omegat.Main";
    private static final String BUILD_DIR = "build";
    private static final String MODULES_DIR = "modules";
    private static final String PLUGINS_DIR = "plugins";

    /**
     * Loads plugins for the application. This method initializes plugin
     * locations, class loaders, and processes plugin manifests to load plugins
     * based on the provided parameters.
     *
     * @param params
     *            a map containing configuration parameters that may specify
     *            additional settings for plugin loading, including development
     *            mode manifests or other configurations.
     */
    public static void loadPlugins(final Map<String, String> params) {
        List<URL> pluginUrls = initializePluginLocations();
        initializePluginClassLoaders(pluginUrls);
        try {
            boolean isMainManifestFound = processPluginManifests(pluginUrls);
            processManifest(params, isMainManifestFound);
            loadBasePlugin();
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    /**
     * Initializes the list of plugin locations by identifying and adding
     * directories where plugins may be located, including standard installation
     * directories and, if applicable, development build directories. The method
     * returns a list of URLs pointing to these plugin locations.
     * <p>
     * We should load all jars from /plugins/ dir first, because some plugin can
     * use more than one jar. There are three different "plugins" directory, and
     * one development treatment.
     * <ul>
     * <li>(installdir)/core-plugins/ OmegaT genuine sub-component</li>
     * <li>(installdir/plugins/ System level 3rd party plugins</li>
     * <li>(configdir)/plugins/ User level 3rd party plugins</li>
     * </ul>
     *
     * @return a list of URLs representing the locations of plugins.
     */
    private static List<URL> initializePluginLocations() {
        List<File> pluginDirectories = new ArrayList<>();
        pluginDirectories.add(new File(StaticUtils.installDir(), MODULES_DIR));
        pluginDirectories.add(new File(StaticUtils.installDir(), PLUGINS_DIR));
        pluginDirectories.add(new File(StaticUtils.getConfigDir(), PLUGINS_DIR));

        // Add development plugins if running from source
        File buildDir = Paths.get(StaticUtils.installDir(), BUILD_DIR).toFile();
        if (buildDir.exists()) {
            pluginDirectories.add(Paths.get(StaticUtils.installDir(), BUILD_DIR, MODULES_DIR).toFile());
        }

        return populatePluginUrlList(pluginDirectories);
    }

    /**
     * Processes plugin manifest files found via the given class loader and a
     * list of plugin URLs. Determines whether a main manifest specifying the
     * main application class is found and processes each manifest file to load
     * plugins or log relevant warnings.
     *
     * @param pluginUrls
     *            a list of plugin URLs that may be updated during the
     *            processing of manifests.
     * @return true if a main manifest specifying the main application class is
     *         found, false otherwise.
     * @throws IOException
     *             if an I/O error occurs during manifest processing.
     */
    private static boolean processPluginManifests(List<URL> pluginUrls) throws IOException {
        MainClassLoader pluginsClassLoader = MAINCLASSLOADERS.get(PluginType.UNKNOWN);
        boolean isMainManifestFound = false;
        Enumeration<URL> manifestUrls = pluginsClassLoader.getResources(MANIFEST_MF);

        while (manifestUrls.hasMoreElements()) {
            URL manifestUrl = manifestUrls.nextElement();
            try {
                isMainManifestFound |= processManifestUrl(manifestUrl, pluginUrls);
            } catch (ClassNotFoundException e) {
                Log.log(e);
            } catch (UnsupportedClassVersionError e) {
                Log.logWarningRB("PLUGIN_JAVA_VERSION_ERROR", getJarFileUrlFromResourceUrl(manifestUrl));
            }
        }
        return isMainManifestFound;
    }

    /**
     * Processes a plugin manifest URL to determine if it represents a main
     * manifest or a plugin. Updates the plugin class loader and plugin URL list
     * as necessary based on the manifest's attributes and plugin category.
     *
     * @param manifestUrl
     *            the URL pointing to the plugin manifest to be processed
     * @param pluginUrls
     *            a list of plugin URLs that may be updated if the manifest
     *            describes a valid plugin
     * @return true if the manifest represents the main application manifest,
     *         false otherwise
     * @throws IOException
     *             if an I/O error occurs while reading the manifest
     * @throws ClassNotFoundException
     *             if a plugin class specified in the manifest cannot be found
     */
    private static boolean processManifestUrl(URL manifestUrl, List<URL> pluginUrls)
            throws IOException, ClassNotFoundException {
        boolean isMainManifestFound = false;
        try (InputStream manifestStream = manifestUrl.openStream()) {
            Manifest manifest = new Manifest(manifestStream);
            String pluginCategory = manifest.getMainAttributes().getValue(PLUGIN_CATEGORY);

            // Check for main manifest
            if (OMEGAT_MAIN_CLASS.equals(manifest.getMainAttributes().getValue(MAIN_CLASS))) {
                isMainManifestFound = true;
                MainClassLoader pluginsClassLoader = MAINCLASSLOADERS.get(PluginType.UNKNOWN);
                loadFromManifest(manifest, pluginsClassLoader, manifestUrl);
            }

            // Process plugin based on category
            if (pluginCategory != null) {
                PluginType type = PluginType.getTypeByValue(pluginCategory);
                if (type != PluginType.UNKNOWN && isUrlInList(manifestUrl, pluginUrls)) {
                    MainClassLoader categoryLoader = MAINCLASSLOADERS.get(type);
                    addUrlToClasspath(manifestUrl, pluginUrls, categoryLoader);
                    loadFromManifest(manifest, categoryLoader, manifestUrl);
                }
            }
        }
        return isMainManifestFound;
    }

    private static void loadBasePlugin() {
        // run base plugins
        for (Class<?> pl : BASE_PLUGIN_CLASSES) {
            try {
                pl.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    /**
     * Processes the plugin manifest based on the provided configuration
     * parameters and conditions. This method supports both development mode and
     * production mode for plugin manifest loading.
     *
     * @param params
     *            a map containing configuration parameters that specify
     *            settings for plugin loading, including potential development
     *            mode manifests.
     * @param foundMain
     *            a boolean flag indicating whether a main application manifest
     *            has already been found. If false, the method attempts to load
     *            manifests either from CLI arguments (in development mode) or
     *            from a properties file.
     */
    private static void processManifest(Map<String, String> params, boolean foundMain) {
        MainClassLoader pluginsClassLoader = MAINCLASSLOADERS.get(PluginType.UNKNOWN);
        try {
            if (!foundMain) {
                // development mode - load from dev-manifests CLI arg
                String manifests = params.get(CLIParameters.DEV_MANIFESTS);
                if (manifests != null) {
                    for (String mf : manifests.split(File.pathSeparator)) {
                        try (InputStream in = Files.newInputStream(Paths.get(mf))) {
                            loadFromManifest(new Manifest(in), pluginsClassLoader, null);
                        }
                    }
                } else {
                    // load from `Plugins.properties` file
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(PLUGINS_LIST_FILE)) {
                        props.load(fis);
                        loadFromProperties(props, pluginsClassLoader);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            Log.log(ex);
        }
    }

    private static boolean isUrlInList(URL target, List<URL> urlList) {
        String targetString = target.toString();
        return urlList.stream().anyMatch(url -> targetString.contains(url.toString()));
    }

    private static void addUrlToClasspath(URL manifestUrl, List<URL> urlList, MainClassLoader loader) {
        String target = manifestUrl.toString();
        urlList.stream().filter(url -> target.contains(url.toString())).forEach(loader::addJarToClasspath);
    }

    private static void initializePluginClassLoaders(List<URL> urlList) {
        ClassLoader cl = PluginUtils.class.getClassLoader();
        MAINCLASSLOADERS.put(PluginType.UNKNOWN, new MainClassLoader(urlList.toArray(new URL[0]), cl));
        for (PluginType type : PluginType.values()) {
            if (type == PluginType.UNKNOWN) {
                continue;
            }
            MAINCLASSLOADERS.put(type,  new MainClassLoader(cl));
        }
    }

    /**
     * Load plugin for test.
     * <p>
     * WARN: don't use it for general purpose.
     * 
     * @param props
     *            properties that have "plugin=(classes...)"
     * @throws ClassNotFoundException
     *             when specified plugin is not found.
     */
    @VisibleForTesting
    public static void loadPluginFromProperties(Properties props) throws ClassNotFoundException {
        initializePluginClassLoaders(new ArrayList<>());
        ClassLoader pluginsClassLoader = MAINCLASSLOADERS.get(PluginType.UNKNOWN);
        loadFromProperties(props, pluginsClassLoader);
    }

    /**
     * This method creates a list of plugins to load. It tries to only take the
     * most recent version of plugins. To differentiate between different
     * versions, the plugins must have the same name, have the same number of
     * version components (we can't compare <code>x.y.z</code> with
     * <code>y.z</code>). Also, the qualifier (i.e., anything after the "-" in
     * the version number) is discarded for the comparison.
     *
     * @param pluginsDirs
     *            List of directories where plugins can be loaded
     */
    static List<URL> populatePluginUrlList(List<File> pluginsDirs) {
        // list all jars in /plugins/
        List<URL> urlList = loadJarUrls(pluginsDirs);
        Map<String, PluginInformation> pluginVersions = new HashMap<>();
        List<URL> jarToRemove = new ArrayList<>();
        // look on all manifests
        for (URL url : urlList) {
            try (JarInputStream jarStream = new JarInputStream(url.openStream())) {
                Manifest mf = jarStream.getManifest();
                if (mf == null) {
                    // mf can be null when a jar file does not have a manifest.
                    continue;
                }
                processPluginManifest(url, mf, pluginVersions, jarToRemove);
            } catch (IOException | NumberFormatException ex) {
                Log.log(ex);
            }
        }

        removeOutdatedPlugins(urlList, jarToRemove);
        return urlList;
    }

    private static void processPluginManifest(URL url, Manifest manifest,
                                              Map<String, PluginInformation> pluginVersions, List<URL> jarToRemove) {
        String pluginClass = manifest.getMainAttributes().getValue(OMEGAT_PLUGINS);
        String oldPluginClass = manifest.getMainAttributes().getValue(OMEGAT_PLUGIN);

        // if the jar doesn't look like an OmegaT plugin (it doesn't
        // contain any "Omegat-Plugins?" attribute, we don't need to
        // compare versions.
        if (!isValidPlugin(pluginClass, oldPluginClass)) {
            return;
        }

        PluginInformation pluginInfo = PluginInformation.Builder.fromManifest(null, manifest, url, null);
        String pluginName = pluginInfo.getName();

        if (pluginVersions.containsKey(pluginName)) {
            handleVersionConflict(pluginVersions, jarToRemove, pluginInfo, pluginName);
        } else {
            pluginVersions.put(pluginName, pluginInfo);
        }
    }

    private static final String VERSION_QUALIFIER_PATTERN = "-.*";

    private static void handleVersionConflict(Map<String, PluginInformation> pluginVersions,
                                              List<URL> jarToRemove, PluginInformation newPlugin, String pluginName) {
        // Fetch all the information from the manifest
        PluginInformation existingPlugin = pluginVersions.get(pluginName);

        // We get rid of versions qualifiers (x.y.z-beta) and we
        // assume dots are used to separate version components
        String existingVersion = existingPlugin.getVersion().replaceAll(VERSION_QUALIFIER_PATTERN, "");
        String newVersion = newPlugin.getVersion().replaceAll(VERSION_QUALIFIER_PATTERN, "");

        int comparison = VersionChecker.compareVersions(existingVersion, "0",
                newVersion, "0");

        updatePluginVersions(pluginVersions, jarToRemove, newPlugin, pluginName,
                existingPlugin, comparison);
    }

    private static void updatePluginVersions(Map<String, PluginInformation> pluginVersions,
                                             List<URL> jarToRemove, PluginInformation newPlugin, String pluginName,
                                             PluginInformation existingPlugin, int comparison) {
        if (comparison < 0) {
            Log.logWarningRB("PLUGIN_EXCLUDE_OLD_VERSION", pluginName,
                    existingPlugin.getVersion(), newPlugin.getVersion());
            jarToRemove.add(existingPlugin.getUrl());
            pluginVersions.put(pluginName, newPlugin);
        } else if (comparison == 0) {
            Log.logWarningRB("PLUGIN_EXCLUDE_SIMILAR_VERSION", pluginName,
                    existingPlugin.getVersion(), newPlugin.getVersion());
            jarToRemove.add(existingPlugin.getUrl());
            pluginVersions.put(pluginName, newPlugin);
        } else {
            Log.logWarningRB("PLUGIN_EXCLUDE_OLD_VERSION", pluginName,
                    newPlugin.getVersion(), existingPlugin.getVersion());
            jarToRemove.add(newPlugin.getUrl());
            pluginVersions.put(pluginName, existingPlugin);
        }
    }

    private static List<URL> loadJarUrls(List<File> pluginsDirs) {
        FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");
        List<File> jarFiles = pluginsDirs.stream()
                .flatMap(dir -> FileUtil.findFiles(dir, jarFilter).stream())
                .collect(Collectors.toList());

        List<URL> urls = new ArrayList<>();
        for (File file : jarFiles) {
            try {
                URL url = file.toURI().toURL();
                urls.add(url);
                Log.logInfoRB("PLUGIN_LOAD_JAR", url);
            } catch (IOException ex) {
                Log.log(ex);
            }
        }
        return urls;
    }

    private static boolean isValidPlugin(String pluginClass, String oldPluginClass) {
        return (oldPluginClass != null || pluginClass != null)
                && (pluginClass == null || pluginClass.indexOf('.') >= 0);
    }

    private static void removeOutdatedPlugins(List<URL> urlList, List<URL> jarToRemove) {
        if (!jarToRemove.isEmpty()) {
            Log.logWarningRB("PLUGIN_EXCLUSION_MESSAGE", jarToRemove);
            urlList.removeAll(jarToRemove);
        }
    }

    public static List<Class<?>> getFilterClasses() {
        return FILTER_CLASSES;
    }

    public static List<Class<?>> getTokenizerClasses() {
        return TOKENIZER_CLASSES;
    }

    /**
     * Reterun registered plugin classes for spellchecker category.
     * 
     * @return list of classes.
     */
    public static List<Class<?>> getSpellCheckClasses() {
        return SPELLCHECK_CLASSES;
    }

    public static Class<?> getTokenizerClassForLanguage(Language lang) {
        if (lang == null) {
            return DefaultTokenizer.class;
        }

        // Prefer an exact match on the full ISO language code (XX-YY).
        Class<?> exactResult = searchForTokenizer(lang.getLanguage());
        if (isDefault(exactResult)) {
            return exactResult;
        }

        // Otherwise, return a match for the language only (XX).
        Class<?> generalResult = searchForTokenizer(lang.getLanguageCode());
        if (isDefault(generalResult)) {
            return generalResult;
        } else if (exactResult != null) {
            return exactResult;
        } else if (generalResult != null) {
            return generalResult;
        }

        return DefaultTokenizer.class;
    }

    private static boolean isDefault(Class<?> c) {
        if (c == null) {
            return false;
        }
        Tokenizer ann = c.getAnnotation(Tokenizer.class);
        return ann != null && ann.isDefault();
    }

    private static Class<?> searchForTokenizer(String lang) {
        if (lang.isEmpty()) {
            return null;
        }

        lang = lang.toLowerCase(Locale.ENGLISH);

        // Choose the first relevant tokenizer as fallback if no
        // "default" tokenizer is found.
        Class<?> fallback = null;

        for (Class<?> tokenizerClass : TOKENIZER_CLASSES) {
            Tokenizer ann = tokenizerClass.getAnnotation(Tokenizer.class);
            if (ann == null) {
                continue;
            }

            String[] languages = getSupportedLanguages(tokenizerClass, ann);
            for (String s : languages) {
                if (lang.equals(s)) {
                    if (ann.isDefault()) {
                        return tokenizerClass; // Return best possible match.
                    } else if (fallback == null) {
                        fallback = tokenizerClass;
                    }
                }
            }
        }

        return fallback;
    }

    private static String[] getSupportedLanguages(Class<?> tokenizerClass, Tokenizer annotation) {
        String[] languages = annotation.languages();
        if (languages.length == 1 && languages[0].equals(Tokenizer.DISCOVER_AT_RUNTIME)) {
            try {
                return ((ITokenizer) tokenizerClass.getDeclaredConstructor().newInstance())
                        .getSupportedLanguages();
            } catch (Exception ex) {
                Log.log(ex);
                return new String[0];
            }
        }
        return languages;
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

    /**
     * Retrieves the {@link ClassLoader} associated with the specified
     * {@link PluginType}. If the provided plugin type is {@code UNKNOWN}, the
     * method returns {@code null}.
     *
     * @param type
     *            the {@link PluginType} for which the class loader is needed.
     * @return the {@link ClassLoader} associated with the specified plugin
     *         type, or {@code null} if the type is {@code UNKNOWN}.
     */
    public static ClassLoader getClassLoader(PluginType type) {
        if (type == PluginType.UNKNOWN) {
            return null;
        }
        return MAINCLASSLOADERS.get(type);
    }

    private static final List<Class<?>> FILTER_CLASSES = new ArrayList<>();

    private static final List<Class<?>> TOKENIZER_CLASSES = new ArrayList<>();

    private static final List<Class<?>> MARKER_CLASSES = new ArrayList<>();

    private static final List<Class<?>> SPELLCHECK_CLASSES = new ArrayList<>();

    private static final List<Class<?>> MACHINE_TRANSLATION_CLASSES = new ArrayList<>();

    private static final List<Class<?>> GLOSSARY_CLASSES = new ArrayList<>();

    private static final List<Class<?>> BASE_PLUGIN_CLASSES = new ArrayList<>();

    /**
     * Parse one manifest file.
     *
     * @param m
     *            manifest
     * @param classLoader
     *            classloader
     * @throws ClassNotFoundException
     *             when plugin class is not found.
     */
    private static void loadFromManifest(Manifest m, ClassLoader classLoader, URL mu)
            throws ClassNotFoundException {
        String classes = m.getMainAttributes().getValue(OMEGAT_PLUGINS);
        if (classes != null) {
            for (String clazz : classes.split("\\s+")) {
                if (clazz.trim().isEmpty()) {
                    continue;
                }
                if (loadClass(clazz, classLoader)) {
                    if (mu == null) {
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder.fromManifest(clazz, m, null,
                                PluginInformation.Status.BUNDLED));
                    } else {
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder.fromManifest(clazz, m, mu,
                                PluginInformation.Status.INSTALLED));
                    }
                }
            }
        }
        loadFromManifestOld(m);
    }

    private static void loadFromProperties(Properties props, ClassLoader classLoader)
            throws ClassNotFoundException {
        for (Object o : props.keySet()) {
            String key = o.toString();
            String[] classes = props.getProperty(key).split("\\s+");
            if (key.startsWith("plugin.desc")) {
                continue;
            }
            boolean isMainPlugin = key.equals("plugin");
            for (String clazz : classes) {
                boolean loaded = isMainPlugin
                        ? loadClass(clazz, classLoader)
                        : loadClassOld(key, clazz);
                if (loaded) {
                    PLUGIN_INFORMATIONS.add(PluginInformation.Builder.fromProperties(clazz, props, key,
                            null, PluginInformation.Status.BUNDLED));
                }
            }
        }
    }

    private static boolean loadClass(String clazz, ClassLoader classLoader) {
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
        } catch (Exception ex) {
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
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PLUGIN_UNLOAD_ERROR", p.getSimpleName(), ex.getMessage());
            }
        }
    }

    /**
     * Old-style plugin loading.
     */
    private static void loadFromManifestOld(final Manifest m)
            throws ClassNotFoundException {
        if (m.getMainAttributes().getValue(OMEGAT_PLUGIN) == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (Entry<String, Attributes> e : entries.entrySet()) {
            String key = e.getKey();
            Attributes attrs = e.getValue();
            String sType = attrs.getValue(OMEGAT_PLUGIN);
            if (sType == null) {
                // WebStart signing section, or other section
                continue;
            }
            if (loadClassOld(sType, key)) {
                PLUGIN_INFORMATIONS.add(PluginInformation.Builder.fromManifest(key, m, null,
                        PluginInformation.Status.BUNDLED));
            }
        }
    }

    private static boolean loadClassOld(String sType, String key)
            throws ClassNotFoundException {
        boolean loadOk = true;
        switch (PluginType.getTypeByValue(sType)) {
        case FILTER:
            FILTER_CLASSES.add(MAINCLASSLOADERS.get(PluginType.FILTER).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        case TOKENIZER:
            TOKENIZER_CLASSES.add(MAINCLASSLOADERS.get(PluginType.TOKENIZER).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        case MARKER:
            MARKER_CLASSES.add(MAINCLASSLOADERS.get(PluginType.MARKER).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        case MACHINETRANSLATOR:
            MACHINE_TRANSLATION_CLASSES.add(MAINCLASSLOADERS.get(PluginType.MACHINETRANSLATOR).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        case BASE:
            BASE_PLUGIN_CLASSES.add(MAINCLASSLOADERS.get(PluginType.MISCELLANEOUS).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        case GLOSSARY:
            GLOSSARY_CLASSES.add(MAINCLASSLOADERS.get(PluginType.GLOSSARY).loadClass(key));
            Log.logInfoRB("PLUGIN_LOAD_OK", key);
            break;
        default:
            Log.logErrorRB("PLUGIN_UNKNOWN", sType, key);
            loadOk = false;
        }

        return loadOk;
    }

    public static Collection<PluginInformation> getPluginInformations() {
        return Collections.unmodifiableSet(PLUGIN_INFORMATIONS);
    }

    public static URL getJarFileUrlFromResourceUrl(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }
}
