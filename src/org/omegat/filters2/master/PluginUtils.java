/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.CLIParameters;
import org.omegat.core.Core;
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

    enum PluginType {
        FILTER, TOKENIZER, MARKER, MACHINETRANSLATOR, BASE, GLOSSARY, UNKNOWN
    };

    protected static final List<Class<?>> LOADED_PLUGINS = new ArrayList<Class<?>>();

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
        try {
            // list all jars in /plugins/
            FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");
            List<File> fs = Stream.of(pluginsDir, homePluginsDir)
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
                    }
                    loadFromManifest(m, pluginsClassLoader);
                }
            }
            if (!foundMain) {
                // development mode - load from dev-manifests CLI arg
                String manifests = params.get(CLIParameters.DEV_MANIFESTS);
                if (manifests != null) {
                    for (String mf : manifests.split(File.pathSeparator)) {
                        try (InputStream in = new FileInputStream(mf)) {
                            loadFromManifest(new Manifest(in), pluginsClassLoader);
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
        return ann == null ? false : ann.isDefault();
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
     * @throws ClassNotFoundException
     */
    protected static void loadFromManifest(final Manifest m, final ClassLoader classLoader)
            throws ClassNotFoundException {
        String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
        if (pluginClasses != null) {
            for (String clazz : pluginClasses.split("\\s+")) {
                if (clazz.trim().isEmpty()) {
                    continue;
                }
                loadClass(clazz, classLoader);
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
                    loadClass(clazz, classLoader);
                }
            } else {
                for (String clazz : classes) {
                    loadClassOld(key, clazz, classLoader);
                }
            }
        }
    }

    protected static void loadClass(String clazz, ClassLoader classLoader) {
        try {
            Class<?> c = classLoader.loadClass(clazz);
            if (LOADED_PLUGINS.contains(c)) {
                Log.logInfoRB("PLUGIN_SKIP_PREVIOUSLY_LOADED", clazz);
                return;
            }
            Method load = c.getMethod("loadPlugins");
            load.invoke(c);
            LOADED_PLUGINS.add(c);
            Log.logInfoRB("PLUGIN_LOAD_OK", clazz);
        } catch (Throwable ex) {
            Log.logErrorRB(ex, "PLUGIN_LOAD_ERROR", clazz, ex.getClass().getSimpleName(), ex.getMessage());
            Core.pluginLoadingError(StringUtil.format(OStrings.getString("PLUGIN_LOAD_ERROR"), clazz,
                    ex.getClass().getSimpleName(), ex.getMessage()));
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
            if ("true".equals(attrs.getValue("OmegaT-Tokenizer"))) {
                // TODO remove after release new tokenizers
                sType = "tokenizer";
            }
            if (sType == null) {
                // WebStart signing section, or other section
                continue;
            }
            loadClassOld(sType, key, classLoader);
        }
    }

    protected static void loadClassOld(String sType, String key, ClassLoader classLoader)
            throws ClassNotFoundException {
        PluginType pType;
        try {
            pType = PluginType.valueOf(sType.toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            pType = PluginType.UNKNOWN;
        }
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
        }
    }
}
