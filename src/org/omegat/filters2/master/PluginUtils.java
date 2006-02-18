/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
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
        plugins = new ArrayList();
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
        for(int i=0; i<filters.length; i++)
        {
            if( filters[i].isFile() )
            {
                try
                {
                    URL jar = filters[i].toURL();
                    loadOnePlugin(jar);
                }
                catch( MalformedURLException mue )
                {
                    // nothing is really wrong
                    // strange exception
                    StaticUtils.log("Couldn't access local file system " +      // NOI18N
                            "to get '"+filters[i]+"' !");                       // NOI18N
                    mue.printStackTrace(StaticUtils.getLogStream());
                }
                catch( IOException ioe )
                {
                    // nothing is really wrong
                    // we just couldn't load one JAR
                    StaticUtils.log("Couldn't load plugin JAR '"+               // NOI18N
                            filters[i]+"' !");                                  // NOI18N
                }
            }
            else
                loadPluginsFrom(filters[i]);
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
        
        List filterList = new ArrayList();
        filterList.add(jar);
        
        Map entries = manifest.getEntries();
        String[] keys = (String[])entries.keySet().toArray(new String[]{});
        for(int i=0; i<keys.length; i++)
        {
            Attributes attrs = (Attributes)entries.get(keys[i]);
            String name = attrs.getValue("Name");                               // NOI18N
            String isfilter = attrs.getValue("OmegaT-Filter");                  // NOI18N
            if( isfilter!=null && isfilter.equals("true") )                     // NOI18N
            {
                filterList.add(keys[i]);
                try
                {
                    URLClassLoader.newInstance(new URL[] {jar}).loadClass(keys[i]);
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
            Class filterClass;
            if( filter.isFromPlugin() )
            {
                ClassLoader plugins_cl = getPluginsClassloader();
                filterClass = plugins_cl.loadClass(filter.getClassName());
            }
            else
                filterClass = Class.forName(filter.getClassName());
            Constructor filterConstructor = filterClass.getConstructor((Class[])null);
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
            List jars = new ArrayList(plugins.size());
            for(int i=0; i<plugins.size(); i++)
            {
                List pfilters = (List)plugins.get(i);
                URL jarurl = (URL)pfilters.get(0);
                jars.add(jarurl);
            }
            plugins_cl = new URLClassLoader((URL[])jars.toArray(new URL[plugins.size()]));
            old_plugins = new ArrayList();
            old_plugins.addAll(plugins);
        }
        return plugins_cl;
    }

    /** Holds the list of plugins. */
    private static List plugins;
    
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
    public static List getPlugins()
    {
        return plugins;
    }

    /** Old list of plugins we already created a classloader for. */
    private static List old_plugins = null;
    /** Class Loader for filter plugins. */
    private static ClassLoader plugins_cl = null;
    
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
