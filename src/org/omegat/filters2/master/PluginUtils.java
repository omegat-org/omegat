/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               2021-2022 Hiroshi Miura
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

package org.omegat.filters2.master;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.CLIParameters;
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

/**
 * Static utilities for OmegaT filter plugins.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    public static final String PLUGINS_LIST_FILE = "Plugins.properties";

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

    protected static final List<Class<?>> LOADED_PLUGINS = new ArrayList<>();
    private static final Set<PluginInformation> PLUGIN_INFORMATIONS = new HashSet<>();

    /** Private constructor to disallow creation */
    private PluginUtils() {
    }

    /**
     * Loads all plugins from main classloader and from /plugins/ dir. We should
     * load all jars from /plugins/ dir first, because some plugin can use more
     * than one jar.
     */
    public static void loadPlugins(final Map<String, String> params) {
        File pluginsDir = new File(StaticUtils.installDir(), "plugins");
        File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
        // list all jars in /plugins/
        FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");
        List<File> fs = Stream.of(pluginsDir, homePluginsDir)
                .flatMap(dir -> FileUtil.findFiles(dir, jarFilter).stream())
                .collect(Collectors.toList());
        List<URL> urlList = new ArrayList<>();
        for (File f : fs) {
            try {
                URL url = f.toURI().toURL();
                urlList.add(url);
                Log.logInfoRB("PLUGIN_LOAD_JAR", url.toString());
            } catch (IOException ex) {
                Log.log(ex);
            }
        }
        boolean foundMain = false;
        // look on all manifests
        URLClassLoader pluginsClassLoader = new URLClassLoader(urlList.toArray(new URL[0]),
                PluginUtils.class.getClassLoader());
        try {
            Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF");
            while (mlist.hasMoreElements()) {
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
                    if ("theme".equals(m.getMainAttributes().getValue("Plugin-Category"))) {
                        String target = mu.toString();
                        for (URL url : urlList) {
                            if (target.contains(url.toString())) {
                                THEME_PLUGIN_JARS.add(url);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    Log.log(e);
                }
            }
        } catch (IOException ex) {
            Log.log(ex);
        }
        try {
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
        } catch (ClassNotFoundException | IOException ex) {
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

    public static Class<?> getTokenizerClassForLanguage(Language lang) {
        if (lang == null) {
            return DefaultTokenizer.class;
        }

        // Prefer an exact match on the full ISO language code (XX-YY).
        Class<?> exactResult = searchForTokenizer(lang.getLanguage());
        if (isDefault(exactResult)) {
            return exactResult;
        }

        // Otherwise return a match for the language only (XX).
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

        // Choose first relevant tokenizer as fallback if no
        // "default" tokenizer is found.
        Class<?> fallback = null;

        for (Class<?> c : TOKENIZER_CLASSES) {
            Tokenizer ann = c.getAnnotation(Tokenizer.class);
            if (ann == null) {
                continue;
            }
            String[] languages = ann.languages();
            try {
                if (languages.length == 1 && languages[0].equals(Tokenizer.DISCOVER_AT_RUNTIME)) {
                    languages = ((ITokenizer) c.getDeclaredConstructor().newInstance()).getSupportedLanguages();
                }
            } catch (Exception ex) {
                Log.log(ex);
            }
            for (String s : languages) {
                if (lang.equals(s)) {
                    if (ann.isDefault()) {
                        return c; // Return best possible match.
                    } else if (fallback == null) {
                        fallback = c;
                    }
                }
            }
        }

        return fallback;
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

    public static List<URL> getThemePluginJars() {
        return THEME_PLUGIN_JARS;
    }

    protected static final List<Class<?>> FILTER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> TOKENIZER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> MARKER_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> MACHINE_TRANSLATION_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> GLOSSARY_CLASSES = new ArrayList<>();

    protected static final List<Class<?>> BASE_PLUGIN_CLASSES = new ArrayList<>();

    protected static final List<URL> THEME_PLUGIN_JARS = new ArrayList<>();

    /**
     * Parse one manifest file.
     *
     * @param m
     *            manifest
     * @param classLoader
     *            classloader
     * @throws ClassNotFoundException when plugin class not found.
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
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder
                                .fromManifest(clazz, m, null, PluginInformation.Status.BUNDLED));
                    } else {
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder
                                .fromManifest(clazz, m, mu, PluginInformation.Status.INSTALLED));
                    }
               }
            }
        }

        loadFromManifestOld(m, classLoader);
    }

    protected static void loadFromProperties(Properties props, ClassLoader classLoader) throws ClassNotFoundException {
        for (Object o : props.keySet()) {
            String key = o.toString();
            String[] classes = props.getProperty(key).split("\\s+");
            if (key.equals("plugin")) {
                for (String clazz : classes) {
                    if (loadClass(clazz, classLoader)) {
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder
                                .fromProperties(clazz, props, key, null, PluginInformation.Status.BUNDLED));
                    }
                }
            } else {
                for (String clazz : classes) {
                    if (loadClassOld(key, clazz, classLoader)) {
                        PLUGIN_INFORMATIONS.add(PluginInformation.Builder
                                .fromProperties(clazz, props, key, null, PluginInformation.Status.BUNDLED));
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
                Log.logErrorRB(ex, "PLUGIN_UNLOAD_ERROR", p.getSimpleName(), ex.getMessage());
            }
        }
    }

    /**
     * Old-style plugin loading.
     */
    protected static void loadFromManifestOld(final Manifest m, final ClassLoader classLoader)
            throws ClassNotFoundException {
        if (m.getMainAttributes().getValue("OmegaT-Plugin") == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (Entry<String, Attributes> e : entries.entrySet()) {
            String key = e.getKey();
            Attributes attrs = e.getValue();
            String sType = attrs.getValue("OmegaT-Plugin");
            if (sType == null) {
                // WebStart signing section, or other section
                continue;
            }
            if (loadClassOld(sType, key, classLoader)) {
                PLUGIN_INFORMATIONS.add(PluginInformation.Builder
                        .fromManifest(key, m, null, PluginInformation.Status.BUNDLED));
            }
        }
    }

    protected static boolean loadClassOld(String sType, String key, ClassLoader classLoader)
            throws ClassNotFoundException {
        boolean loadOk = true;
        switch (PluginType.getTypeByValue(sType)) {
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

    public static Collection<PluginInformation> getPluginInformations() {
        return Collections.unmodifiableSet(PLUGIN_INFORMATIONS);
    }
}
