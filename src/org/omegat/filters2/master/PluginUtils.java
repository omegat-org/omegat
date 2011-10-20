/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.master;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.VersionNumber;

/**
 * Static utilities for OmegaT filter plugins.
 * 
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class PluginUtils {

    enum PLUGIN_TYPE {
        FILTER, TOKENIZER, MARKER, MACHINETRANSLATOR, BASE, UNKNOWN
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
        try {
            URLClassLoader cls;
            // list all jars in /plugins/
            List<File> fs = FileUtil.findFiles(pluginsDir, new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            URL[] urls = new URL[fs.size()];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = fs.get(i).toURI().toURL();
            }
            boolean foundMain = false;
            // look on all manifests
            cls = new URLClassLoader(urls, PluginUtils.class.getClassLoader());
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
                loadFromManifest(m, cls, false);
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
                    loadFromManifest(m, cls, true);
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        
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

    public static List<Class<?>> getMarkerClasses() {
        return markerClasses;
    }

    public static List<Class<?>> getMachineTranslationClasses() {
        return machineTranslationClasses;
    }

    protected static List<Class<?>> filterClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> tokenizerClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> markerClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> machineTranslationClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> basePluginClasses = new ArrayList<Class<?>>();

    /**
     * Parse one manifest file.
     * 
     * @param m
     *            manifest
     * @param classLoader
     *            classloader
     * @param devMode
     *            development mode - version checking for '@version@'
     * @throws ClassNotFoundException
     */
    protected static void loadFromManifest(final Manifest m, final ClassLoader classLoader,
            final boolean devMode) throws ClassNotFoundException {
        if (m.getMainAttributes().getValue("OmegaT-Plugin") == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (String key : entries.keySet()) {
            Attributes attrs = (Attributes) entries.get(key);
            String sType = attrs.getValue("OmegaT-Plugin");
            String sVersion = attrs.getValue("OmegaT-Version");
            if (devMode && "@version@-@version@".equals(sVersion)) {
                // development mode hack for '@version@-@version@'
                sVersion = OStrings.VERSION + "-" + OStrings.VERSION;
            }
            if (!isVersionAllowed(sVersion, OStrings.VERSION)) {
                Log.logInfoRB("PLUGIN_WRONG_VERSION", key, OStrings.VERSION, sVersion);
                continue;
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
            default:
                Log.logErrorRB("PLUGIN_UNKNOWN", key);
            }
        }
    }
    
    /**
     * This method checks if required version is between min and max version.
     * 
     * @param minMaxVersion
     *            min and max version in format '1.1.1-2.2.2'
     * @param requiredVersion
     *            required version
     * @return true if required version between min and max(inclusive)
     */
    protected static boolean isVersionAllowed(String minMaxVersion, String requiredVersion) {
        if (minMaxVersion == null) {
            return true;
            // TODO : allow after all plugins will be fixed
            //return false;
        }
        String[] vv = minMaxVersion.split("-");
        if (vv.length != 2) {
            // invalid format
            Log.log(new IllegalArgumentException("Wrong min/max version definition: " + minMaxVersion));
            return false;
        }
        VersionNumber min, max, cur;
        try {
            min = new VersionNumber(vv[0]);
            max = new VersionNumber(vv[1]);
            cur = new VersionNumber(requiredVersion);
        } catch (IllegalArgumentException ex) {
            Log.log(ex);
            return false;
        }
        return min.compareTo(cur) <= 0 && max.compareTo(cur) >= 0;
    }
}
