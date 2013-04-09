/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.Tokenizer;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * Static utilities for OmegaT filter plugins.
 * 
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    enum PLUGIN_TYPE {
        FILTER, TOKENIZER, MARKER, MACHINETRANSLATOR, BASE, GLOSSARY, UNKNOWN
    };

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
            URLClassLoader cls;
            // list all jars in /plugins/
            List<File> fs = FileUtil.findFiles(pluginsDir, new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            List<File> fsHome = FileUtil.findFiles(homePluginsDir, new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            fs.addAll(fsHome);
            URL[] urls = new URL[fs.size()];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = fs.get(i).toURI().toURL();
            }
            boolean foundMain = false;
            // look on all manifests
            cls = new URLClassLoader(urls);
            for (Enumeration<URL> mlist = cls.getResources("META-INF/MANIFEST.MF"); mlist.hasMoreElements();) {
                URL mu = mlist.nextElement();
                InputStream in = mu.openStream();
                Manifest m;
                try {
                    m = new Manifest(in);
                } finally {
                    in.close();
                }
                if ("org.omegat.Main".equals(m.getMainAttributes().getValue("Main-Class"))) {
                    // found main manifest - not in development mode
                    foundMain = true;
                }
                loadFromManifest(m, cls);
            }
            if (!foundMain) {
                // development mode - load main manifest template
                String manifests = params.get("dev-manifests");
                if (manifests == null) {
                    manifests = "manifest-template.mf";
                }
                for (String mf : manifests.split(File.pathSeparator)) {
                    Manifest m;
                    InputStream in = new FileInputStream(mf);
                    try {
                        m = new Manifest(in);
                    } finally {
                        in.close();
                    }
                    loadFromManifest(m, cls);
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        
        // Sort tokenizer list for display in Project Properties dialog.
        Collections.sort(tokenizerClasses, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> c1, Class<?> c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        
        // run base plugins
        for (Class<?> pl : basePluginClasses) {
            try {
                pl.newInstance();
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    public static List<Class<?>> getFilterClasses() {
        return filterClasses;
    }

    public static List<Class<?>> getTokenizerClasses() {
        return tokenizerClasses;
    }

    public static Class<?> getTokenizerClassForLanguage(Language lang) {
        if (lang == null) return DefaultTokenizer.class;
        
        // Prefer an exact match on the full ISO language code (XX-YY).
        Class<?> result = searchForTokenizer(lang.getLanguage());
        if (result != null) return result;
        
        // Otherwise return a match for the language only (XX).
        result = searchForTokenizer(lang.getLanguageCode());
        if (result != null) return result;
        
        return DefaultTokenizer.class;
    }

    private static Class<?> searchForTokenizer(String lang) {
        if (lang.length() < 1) return null;
        
        lang = lang.toLowerCase();
        
        // Choose first relevant tokenizer as fallback if no
        // "default" tokenizer is found.
        Class<?> fallback = null;
        
        for (Class<?> c : tokenizerClasses) {
            Tokenizer ann = c.getAnnotation(Tokenizer.class);
            if (ann == null) continue;
            for (String s : ann.languages()) {
                if (lang.equals(s)) {
                    if (ann.isDefault()) return c; // Return best possible match.
                    else if (fallback == null) fallback = c;
                }
            }
        }
        
        return fallback;
    }

    public static List<Class<?>> getMarkerClasses() {
        return markerClasses;
    }

    public static List<Class<?>> getMachineTranslationClasses() {
        return machineTranslationClasses;
    }

    public static List<Class<?>> getGlossaryClasses() {
        return glossaryClasses;
    }

    protected static List<Class<?>> filterClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> tokenizerClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> markerClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> machineTranslationClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> glossaryClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> basePluginClasses = new ArrayList<Class<?>>();

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
        if (m.getMainAttributes().getValue("OmegaT-Plugin") == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (String key : entries.keySet()) {
            Attributes attrs = (Attributes) entries.get(key);
            String sType = attrs.getValue("OmegaT-Plugin");
            if ("true".equals(attrs.getValue("OmegaT-Tokenizer"))) {
                // TODO remove after release new tokenizers
                sType = "tokenizer";
            }
            if (sType == null) {
                // WebStart signing section, or other section
                continue;
            }
            PLUGIN_TYPE pType;
            try {
                pType = PLUGIN_TYPE.valueOf(sType.toUpperCase(Locale.ENGLISH));
            } catch (Exception ex) {
                pType = PLUGIN_TYPE.UNKNOWN;
            }
            switch (pType) {
            case FILTER:
                filterClasses.add(classLoader.loadClass(key));
                break;
            case TOKENIZER:
                tokenizerClasses.add(classLoader.loadClass(key));
                break;
            case MARKER:
                markerClasses.add(classLoader.loadClass(key));
                break;
            case MACHINETRANSLATOR:
                machineTranslationClasses.add(classLoader.loadClass(key));
                break;
            case BASE:
                basePluginClasses.add(classLoader.loadClass(key));
                break;
            case GLOSSARY:
                glossaryClasses.add(classLoader.loadClass(key));
                break;
            default:
                Log.logErrorRB("PLUGIN_UNKNOWN", key);
            }
        }
    }
}
