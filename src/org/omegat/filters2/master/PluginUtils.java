/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * Static utilities for OmegaT filter plugins.
 *
 * @author Maxym Mykhalchuk
 */
public final class PluginUtils
{
    
    /** Private constructor to disallow creation */
    private PluginUtils() {}

    /**
     * Loads all plugins from main classloader and from /plugins/ dir. We should
     * load all jars from /plugins/ dir first, because some plugin can use more
     * than one jar.
     */
    public static void loadPlugins2() {
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
                urls[i] = fs.get(i).toURL();
            }
            boolean foundMain = false;
            // look on all manifests
            cls = new URLClassLoader(urls, PluginUtils.class.getClassLoader());
            for (Enumeration<URL> mlist = cls
                    .getResources("META-INF/MANIFEST.MF"); mlist
                    .hasMoreElements();) {
                URL mu = mlist.nextElement();
                InputStream in = mu.openStream();
                Manifest m;
                try {
                    m = new Manifest(in);
                } finally {
                    in.close();
                }
                if ("org.omegat.Main".equals(m.getMainAttributes().getValue(
                        "Main-Class"))) {
                    // found main manifest - not in development mode
                    foundMain = true;
                }
                loadByManifest(m, cls);
            }
            if (!foundMain) {
                // development mode - load main manifest template
                File mf = new File("manifest-template.mf");
                if (mf.exists()) {
                    InputStream in = new FileInputStream(mf);
                    Manifest m;
                    try {
                        m = new Manifest(in);
                    } finally {
                        in.close();
                    }
                    loadByManifest(m, cls);
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    public static List<Class<?>> getFilterClasses() {
        return filterClasses;
    }
    
    public static List<Class<?>> getTokenizerClasses() {
        return tokenizerClasses;
    }

    protected static List<Class<?>> filterClasses = new ArrayList<Class<?>>();

    protected static List<Class<?>> tokenizerClasses = new ArrayList<Class<?>>();

    /**
     * Parse one manifest file.
     * 
     * @param m
     *            manifest
     * @param classLoader
     *            classloader
     * @throws ClassNotFoundException
     */
    protected static void loadByManifest(final Manifest m,
            final ClassLoader classLoader) throws ClassNotFoundException {
        if (m.getMainAttributes().getValue("OmegaT-Plugin") == null) {
            return;
        }

        Map<String, Attributes> entries = m.getEntries();
        for (String key : entries.keySet()) {
            Attributes attrs = (Attributes) entries.get(key);
            String isfilter = attrs.getValue("OmegaT-Filter");
            if ("true".equals(isfilter)) {
                filterClasses.add(classLoader.loadClass(key));
            }
            String istokenizer = attrs.getValue("OmegaT-Tokenizer");
            if ("true".equals(istokenizer)) {
                tokenizerClasses.add(classLoader.loadClass(key));
            }
        }
    }
}
