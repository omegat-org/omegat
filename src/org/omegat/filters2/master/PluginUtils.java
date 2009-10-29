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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;

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
     * Loads filter plugins.
     * <p>
     * Filter plugins should be situated in &lt;OmegaT-install-dir&gt;/plugins,
     * and be packed as JAR files with manifest stating
     * <pre> OmegaT-Plugin: true </pre>
     * and then for each filter
     * <pre> Name: the.package.name.TheFilterName
     * OmegaT-Filter: true</pre> for each filter in a plugin
     * (plugin may have more than one filter).
     */
    public static void loadPlugins()
    {
        plugins = new ArrayList<List<Object>>();
        File pluginsDir = new File("plugins/");                                 // NOI18N
        if( pluginsDir.exists() && pluginsDir.isDirectory() )
            loadPluginsFrom(pluginsDir);
    }

    /**
     * Loads filter plugins from a single directory.
     */
    private static void loadPluginsFrom(File dir)
    {
        File[] filters = dir.listFiles(new JARorFolderFileFilter());
        for(File filter : filters)
        {
            if( filter.isFile() )
            {
                try
                {
                    URL jar = filter.toURL();
                    loadOnePlugin(jar);
                }
                catch( MalformedURLException mue )
                {
                    // nothing is really wrong
                    // strange exception
                    Log.log("Couldn't access local file system " +      // NOI18N
                            "to get '"+filter+"' !");                       // NOI18N
                }
                catch( IOException ioe )
                {
                    // nothing is really wrong
                    // we just couldn't load one JAR
                    Log.log("Couldn't load plugin JAR '"+               // NOI18N
                            filter+"' !");                                  // NOI18N
                }
            }
            else
                loadPluginsFrom(filter);
        }
    }
    
    /**
     * Loads plugins from a single JAR.
     *
     * @param jar name of the plugin JAR file to load filters from.
     */
    private static void loadOnePlugin(URL jar) throws IOException
    {
        JarFile filter_jar = new JarFile(jar.getFile());
        Manifest manifest = filter_jar.getManifest();
        
        Attributes mainattribs = manifest.getMainAttributes();
        if( mainattribs.getValue("OmegaT-Plugin")==null )                       // NOI18N
            return; // it's not OmegaT plugin
        
        List<Object> filterList = new ArrayList<Object>();
        filterList.add(jar);
        
        Map<String,Attributes> entries = manifest.getEntries();
        for(String key : entries.keySet())
        {
            Attributes attrs = (Attributes)entries.get(key);
            String name = attrs.getValue("Name");                               // NOI18N
            String isfilter = attrs.getValue("OmegaT-Filter");                  // NOI18N
            if( isfilter!=null && isfilter.equals("true") )                     // NOI18N
            {
                filterList.add(key);
                try
                {
                    URLClassLoader.newInstance(new URL[] {jar}).loadClass(key);
                }
                catch( ClassNotFoundException e )
                {
                    // we just don't load the plugin
                    throw new IOException(e.getLocalizedMessage());
                }
            }
        }
        
        if( filterList.size()>1 )
        {
            plugins.add(filterList);
        }
    }
    
    
    /** 
     * Utility Method to instantiate a filter.
     * 
     * @param filter OneFilter object with information about a filter.
     * @return       AbstractFilter object ready for processing file(s).
     * @throws       TranslationException Iff any error happens.
     */
    public static AbstractFilter instantiateFilter(OneFilter filter)
            throws TranslationException
    {
        AbstractFilter filterObject = null;
        try
        {
            Class<?> filterClass;
            if( filter.isFromPlugin() )
            {
                ClassLoader plugins_cl = getPluginsClassloader();
                filterClass = plugins_cl.loadClass(filter.getClassName());
            }
            else
                filterClass = Class.forName(filter.getClassName());
            Constructor<?> filterConstructor = filterClass.getConstructor((Class[])null);
            filterObject = (AbstractFilter)filterConstructor.newInstance((Object[])null);
            filterObject.setOptions(filter.getOptions());
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new TranslationException(cnfe.toString());
        }
        catch( NoSuchMethodException nsme )
        {
            throw new TranslationException(nsme.toString());
        }
        catch( InstantiationException ie )
        {
            throw new TranslationException(ie.toString());
        }
        catch ( IllegalAccessException iae )
        {
            throw new TranslationException(iae.toString());
        }
        catch( InvocationTargetException ite )
        {
            throw new TranslationException(ite.getCause().toString());
        }
        return filterObject;
    }
    
    /** Returns the classloader of the filter plugins. */
    public static ClassLoader getPluginsClassloader()
    {
        if( !plugins.equals(old_plugins) )
        {
            List<URL> jars = new ArrayList<URL>(plugins.size());
            for(int i=0; i<plugins.size(); i++)
            {
                List<Object> pfilters = plugins.get(i);
                URL jarurl = (URL)pfilters.get(0);
                jars.add(jarurl);
            }
            plugins_cl = new URLClassLoader(jars.toArray(new URL[plugins.size()]));
            old_plugins = new ArrayList<List<Object>>();
            old_plugins.addAll(plugins);
        }
        return plugins_cl;
    }

    /** Holds the list of plugins. */
    private static List<List<Object>> plugins;
    
    /**
     * The list of plugins.
     * <p>
     * The format is simple:
     * <ul>
     * <li>each element of this list is a List itself
     * <li>the first (0th) element of each sublist is an URL of JAR file
     * <li>further elements are names of filter classes
     * </ul>
     */
    public static List<List<Object>> getPlugins()
    {
        return plugins;
    }

    /** Old list of plugins we already created a classloader for. */
    private static List<List<Object>> old_plugins = null;
    /** Class Loader for filter plugins. */
    private static ClassLoader plugins_cl = null;
    
    
    /**
     * Loads all plugins from main classloader and from /plugins/ dir. We should
     * load all jars from /plugins/ dir first, because some plugin can use more
     * than one jar.
     */
    public static void loadPlugins2() {
        File pluginsDir = new File("plugins/");
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

/** File filter that accepts JAR files or directories. */
class JARorFolderFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        return
                (
                file.isFile() &&
                file.getName().toLowerCase().endsWith(".jar")       // NOI18N
                )
                ||
                (
                file.isDirectory() &&
                !file.getName().equals(".") &&                      // NOI18N
                !file.getName().equals("..")                        // NOI18N
                );
    }
}
