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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import org.omegat.core.StringEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.xml.openoffice.OOFilter;
import org.omegat.util.LFileCopy;
import org.omegat.filters2.*;
import org.omegat.filters2.Instance;
import org.omegat.filters2.html.HTMLFilter;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;


/**
 * A master class that registers and handles all the filters.
 * Singleton - there can be only one instance of this class.
 *
 * @author Maxym Mykhalchuk
 */
public class FilterMaster
{
    /** Wrapper around filters storage in an XML file */
    private Filters  filters;
    /** Returns Wrapper around filters storage in an XML file */
    public Filters getFilters()
    {
        return filters;
    }
    
    /**
     * Create a new FilterMaster.
     */
    private FilterMaster()
    {
        loadPlugins();
        if( configFile.exists() )
            loadConfig();
        else
            setupBuiltinFilters();
        loadFilterClassesFromPlugins();
        saveConfig();
    }
    
    private static FilterMaster master = null;
    /**
     * Returns the only instance of this class.
     */
    public static FilterMaster getInstance()
    {
        if( master==null )
            master = new FilterMaster();
        return master;
    }
    

    private boolean memorizing = false;
    /**
     * Call this to turn on/off memorizing passed strings
     * in internal Translation Memory.
     * Typically to be called by Search-in-files functionality.
     */
    private void setMemorizing(boolean value)
    {
        memorizing = value;
    }
    /**
     * Returns whether we're memorizing passed strings
     * in internal Translation Memory.
     */
    private boolean isMemorizing()
    {
        return memorizing;
    }
    
    /**
     * Ugly hack to remove Windows Line Feed "\r".
     */
    private String removeLineFeed(String s)
    {
        return s.replaceAll("\r", "");		// NOI18N
    }
    
    private Pattern PATTERN_SENTENCE_BREAK = Pattern.compile("\\p{Ll}[\\.\\?!]\\.?\\.?(\\s+)\\p{Lu}");
    
    /**
     * This method is called by filters to:
     * <ul>
     * <li>Instruct OmegaT what source strings are translatable.
     * <li>Get the translation of each source string.
     * </ul>
     *
     * @param entry Translatable source string
     * @return Translation of the source string. If there's no translation, returns the source string itself.
     */
   	public String processEntry(String entry)
	{
        // ugly hack, to say the truth
		String src = removeLineFeed(entry);
        
        /*
        if( CommandThread.core.isPreference(OConsts.PREF_SENTENCE_SEGMENTING) )
        {
            Matcher m = PATTERN_SENTENCE_BREAK.matcher(src);
            StaticUtils.log();
            StaticUtils.log("src = '"+src+"'");
            int nextSentenceStart = 0;
            StringBuffer res = new StringBuffer();
            while( m.find() )
            {
                String onesrc = src.substring(nextSentenceStart, m.start(1));
                res.append(processSingleEntry(onesrc));
                res.append(m.group(1));
                nextSentenceStart = m.end(1);
            }
            res.append(processSingleEntry(src.substring(nextSentenceStart)));
            return res.toString();
        }
         */
        return processSingleEntry(entry);
	}
    
    /**
     * Processes a single entry.
     * This method doesn't perform any changes on the passed string.
     *
     * @param src Translatable source string
     * @return Translation of the source string. If there's no translation, returns the source string itself.
     */
    private String processSingleEntry(String src)
    {
        // if the search thread is non-null, we're searching inside files
        // else we're translating them
        if( searchthread!=null )
        {
            searchthread.searchText(src);
            return src;
        }
        else 
        {
            StringEntry se = CommandThread.core.getStringEntry(src);
            if( isMemorizing() )
                CommandThread.core.addEntry(src);
            if( se==null )
            {
                return src;
            }
            else
            {
                String s = se.getTrans();
                if( s==null || s.length()==0 )
                    s = src;
                return s;
            }
        }
    }

    /** Utility Method to instantiate a filter */
    private AbstractFilter instantiateFilter(OneFilter filter)
            throws TranslationException
    {
        AbstractFilter filterObject = null;
        try
        {
            Class filterClass = Class.forName(filter.getClassName());
            Constructor filterConstructor = filterClass.getConstructor((Class[])null);
            filterObject = (AbstractFilter)filterConstructor.newInstance((Object[])null);
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

    /**
     * Returns whether the file can be handled by one of OmegaT
     * filters.
     *
     * @param filename  The name of the source file to load.
     * @return whether this file can be handled (i.e. it is translatable)
     * @see #translateFile(String, String, String)
     */
    public boolean isTranslatable(String filename)
            throws TranslationException
    {
        File file = new File(filename);
        String name = file.getName();
        String path = file.getParent();
        if( path==null )
            path = "";                                                          // NOI18N
        
        for(int i=0; i<getFilters().getFilter().length; i++)
        {
            OneFilter filter = getFilters().getFilter(i);
            if( !filter.isOn() )
                continue;
            for(int j=0; j<filter.getInstance().length; j++)
            {
                Instance instance = filter.getInstance(j);
                if( matchesMask(name, instance.getSourceFilenameMask()) )
                {
                    AbstractFilter filterObject = instantiateFilter(filter);
                    try
                    {
                        BufferedReader reader = filterObject.createReader(file, instance.getSourceEncoding());

                        if( filterObject.isFileSupported(reader) )
                        {
                            return true;
                        }
                        else
                        {
                            break;
                        }
                    }
                    catch( Exception e )
                    {
                        // do nothing but assume this filter cannot handle this file
                        break;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * OmegaT core calls this method to load a source file.
     *
     * @param filename  The name of the source file to load.
     * @see #translateFile(String, String, String)
     */
    public void loadFile(String filename)
            throws IOException, TranslationException
    {
        setMemorizing(true);
        
        File file = new File(filename);
        String name = file.getName();
        String path = file.getParent();
        if( path==null )
            path = "";                                                          // NOI18N
        
        for(int i=0; i<getFilters().getFilter().length; i++)
        {
            OneFilter filter = getFilters().getFilter(i);
            if( !filter.isOn() )
                continue;
            for(int j=0; j<filter.getInstance().length; j++)
            {
                Instance instance = filter.getInstance(j);
                if( matchesMask(name, instance.getSourceFilenameMask()) )
                {
                    AbstractFilter filterObject = instantiateFilter(filter);
                    BufferedReader reader = filterObject.createReader(file, instance.getSourceEncoding());
                    
                    reader.mark(OConsts.READ_AHEAD_LIMIT);
                    if( !filterObject.isFileSupported(reader) )
                    {
                        j = filter.getInstance().length;
                        continue;
                    }
                    
                    try
                    {
                        reader.reset();
                    }
                    catch( IOException e )
                    {
                        // it means that isFileSupported() have read more than the buffer was
                        StaticUtils.log(filter.getClassName()+
                                ".isFileSupported() violated the contract:\n " +
                                "It have read more than "+OConsts.READ_AHEAD_LIMIT+
                                " bytes from the reader.");
                        // we need to reopen the reader
                        reader = filterObject.createReader(file, instance.getSourceEncoding());
                    }
                    
                    BufferedWriter writer = new BufferedWriter(new StringWriter());
                    
                    filterObject.processFile(reader, writer);
                    reader.close();
                    writer.close();
                    
                    setMemorizing(false);
                    return;
                }
            }
        }
        setMemorizing(false);
    }

    
    private SearchThread searchthread = null;
    /**
     * When mode is set, 
     * strings are passed to supplied search thread.
     *
     * @param searchthread The Search Thread supplied.
     */
	private void setSearchMode(SearchThread searchthread)
	{
        setMemorizing(false);
		this.searchthread = searchthread;
	}
    /**
     * Cancels search mode.
     */
	private void cancelSearchMode()
	{
		this.searchthread = null;
	}

    
    /**
     * OmegaT core calls this method to search within a source file.
     * (used for source files outside project source dir)
     *
     * @param filename  The name of the source file to search.
     * @see #translateFile(String, String, String)
     */
    public void searchFile(String filename, SearchThread searchthread)
            throws IOException, TranslationException
    {
        setSearchMode(searchthread);
        loadFile(filename);
        cancelSearchMode();
    }
    
    /**
     * OmegaT core calls this method to translate a source file.
     * <ul>
     * <li>OmegaT first looks through registered filter instances
     *     to find filter(s) that can handle this file.
     * <li>Opens the file and tests if filter(s) want to handle it.
     * <li>If the filter accepts the file, the appropriate target file is opened.
     * <li>Filter is asked to process the file.
     * <li>Target writer is closed.
     * </ul>
     * If no filter is found, that processes this file,
     * we simply copy it to target folder.
     *
     * @param sourcedir The folder of the source file.
     * @param filename  The name of the source file to process (only the part, relative to source folder).
     * @param targetdir The folder to place the translated file to.
     */
    public void translateFile(String sourcedir, String filename, String targetdir)
            throws IOException, TranslationException
    {
        setMemorizing(false);
        
        int lastslash = filename.lastIndexOf(File.separatorChar);
        String name = filename.substring(lastslash+1);
        String path = "";                                                       // NOI18N
        if( lastslash>=0 )
            path = filename.substring(0, lastslash);
        
        for(int i=0; i<getFilters().getFilter().length; i++)
        {
            OneFilter filter = getFilters().getFilter(i);
            if( !filter.isOn() )
                continue;
            for(int j=0; j<filter.getInstance().length; j++)
            {
                Instance instance = filter.getInstance(j);
                String sourceMask = instance.getSourceFilenameMask();
                if( matchesMask(name, sourceMask) )
                {
                    File infile = new File(sourcedir+File.separatorChar+filename);
                    AbstractFilter filterObject = null;
                    try
                    {
                        Class filterClass = Class.forName(filter.getClassName());
                        Constructor filterConstructor = filterClass.getConstructor((Class[])null);
                        filterObject = (AbstractFilter)filterConstructor.newInstance((Object[])null);
                    }
                    catch( InvocationTargetException ite )
                    {
                        throw new TranslationException(ite.getCause().toString());
                    }
                    catch( Exception e )
                    {
                        throw new TranslationException(e.toString());
                    }

                    BufferedReader reader = filterObject.createReader(infile, instance.getSourceEncoding());
                    reader.mark(OConsts.READ_AHEAD_LIMIT);
                    
                    if( !filterObject.isFileSupported(reader) )
                    {
                        j = filter.getInstance().length;
                        continue;
                    }
                    
                    try
                    {
                        reader.reset();
                    }
                    catch( IOException e )
                    {
                        // it means that isFileSupported() have read more than the buffer was
                        StaticUtils.log(filter.getClassName()+
                                ".isFileSupported() violated the contract:\n " +
                                "It have read more than "+OConsts.READ_AHEAD_LIMIT+
                                " bytes from the reader.");
                        // we need to reopen the reader
                        reader = filterObject.createReader(infile, instance.getSourceEncoding());
                    }
                    
                    File outfile = new File(targetdir+File.separatorChar+path+File.separatorChar+
                            constructTargetFilename(sourceMask, name, instance.getTargetFilenamePattern()));
                    BufferedWriter writer = filterObject.createWriter(outfile, instance.getTargetEncoding());
                    
                    filterObject.processFile(reader, writer);
                    reader.close();
                    writer.close();
                    
                    return;
                }
            }
        }
        
        // If we did get to this point, it means that 
        // none of the filters processed the file.
        // Copying it
        LFileCopy.copy(sourcedir+File.separator+filename, targetdir+File.separator+filename);
    }
    
    private static List supportedEncodings = null;
    /**
     * Queries JRE for the list of supported encodings.
     * Also adds the human name for no/automatic encoding.
     *
     * @return names of all the encodings in an array
     */
    public static List getSupportedEncodings()
    {
        if( supportedEncodings==null )
        {
            supportedEncodings = new ArrayList();
            supportedEncodings.add(AbstractFilter.ENCODING_AUTO_HUMAN);
            supportedEncodings.addAll(Charset.availableCharsets().keySet());
        }
        return supportedEncodings;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Plugins
    //////////////////////////////////////////////////////////////////////////
    
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
    private List plugins;
    
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
    private void loadPlugins()
    {
        plugins = new ArrayList();
        File pluginsDir = new File("plugins/");
        if( pluginsDir.exists() && pluginsDir.isDirectory() )
            loadPluginsFrom(pluginsDir);
    }

    /**
     * Loads filter plugins from a single directory.
     */
    private void loadPluginsFrom(File dir)
    {
        File[] filters = dir.listFiles(new FileFilter()
        // File filter that accepts JAR files or directories
        {
            public boolean accept(File file)
            {
                return (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) ||
                        (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..") );
            }
        });
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
                    StaticUtils.log("Something went wrong!");
                    mue.printStackTrace(StaticUtils.getLogStream());
                }
                catch( IOException ioe )
                {
                    // nothing is really wrong
                    // we just couldn't load one JAR
                    StaticUtils.log("Couldn't load plugin JAR '"+filters[i]+"' !");
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
    private void loadOnePlugin(URL jar) throws IOException
    {
        JarFile filter_jar = new JarFile(jar.getFile());
        Manifest manifest = filter_jar.getManifest();

        Attributes mainattribs = manifest.getMainAttributes();
        if( mainattribs.getValue("OmegaT-Plugin")==null )
            return; // it's not OmegaT plugin

        List filterList = new ArrayList();
        filterList.add(jar);

        Map entries = manifest.getEntries();
        String[] keys = (String[])entries.keySet().toArray(new String[]{});
        for(int i=0; i<keys.length; i++)
        {
            Attributes attrs = (Attributes)entries.get(keys[i]);
            String name = attrs.getValue("Name");
            String isfilter = attrs.getValue("OmegaT-Filter");
            if( isfilter!=null && isfilter.equals("true") )
                filterList.add(keys[i]);
        }

        plugins.add(filterList);
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Filters
    //////////////////////////////////////////////////////////////////////////

    /**
     * Reverts Filter Configuration to Default values.
     * Basically
     * <ul>
     * <li>Sets up built-in filters
     * <li>Reloads the plugins
     * <li>Loads filters from plugins
     * <li>Saves the configuration
     * </ul>
     */
    public void revertFiltersConfigToDefaults()
    {
        setupBuiltinFilters();
        loadPlugins();
        loadFilterClassesFromPlugins();
        saveConfig();
    }
    
    /** XML file with filters configuration */
    private File configFile = new File("filters.conf");                         // NOI18N
    
    /**
     * Loads information about the filters from an XML file.
     * If there's an error loading a file, it calls <code>setupDefaultFilters</code>.
     */
    public void loadConfig()
    {
        try
        {
            XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile));
            filters = (Filters)xmldec.readObject();
            xmldec.close();
            checkIfAllFilterPluginsAreAvailable();
        }
        catch( Exception e )
        {
            StaticUtils.log(OStrings.getString("FILTERMASTER_ERROR_LOADING_FILTERS_CONFIG") + e);
            setupBuiltinFilters();
        }
    }
    
    /**
     * Goes through the list of loaded plugins to see if
     * all the filters that are there in config file are present.
     */
    private void checkIfAllFilterPluginsAreAvailable()
    {
        List jars = new ArrayList(plugins.size());
        for(int i=0; i<plugins.size(); i++)
        {
            List pfilters = (List)plugins.get(i);
            URL jarurl = (URL)pfilters.get(0);
            jars.add(jarurl);
        }

        URLClassLoader cl = new URLClassLoader((URL[])jars.toArray(new URL[plugins.size()]));
        
        int k=0;
        while( k<filters.filtersSize() )
        {
            OneFilter onefilter = filters.getFilter(k);
            if( !onefilter.isFromPlugin() )
            {
                k++;
                continue;
            }

            for(int i=0; i<plugins.size(); i++)
            {
                List filterList = (List)plugins.get(i);
                for(int j=1; j<filterList.size(); j++)
                {
                    String classname = (String)filterList.get(j);
                    if( onefilter.getClass().getName().equals(classname) )
                    {
                        // trying to create
                        try
                        {
                            Class filter_class = cl.loadClass(classname);
                            Constructor filter_constructor = filter_class.getConstructor((Class[])null);
                            Object filter = filter_constructor.newInstance((Object[])null);
                            if( filter instanceof AbstractFilter )
                            {
                                // OK
                                k++;
                                continue;
                            }
                        }
                        catch( Exception e )
                        {
                            // couldn't load one of filters
                            // removing it
                            filters.removeFilter(k);
                            continue;
                        }
                    }
                }
            }
            
            // if we are here, it means that there's no such filter class
            // in all the plugins currently present
            // removing it
            filters.removeFilter(k);
        }
    }
    
    /**
     * Initializes Filter Master defaults
     * by re-creating all information about built-in file filters.
     */
    private void setupBuiltinFilters()
    {
        filters = new Filters();
        filters.addFilter(new OneFilter(new TextFilter(), false));
        filters.addFilter(new OneFilter(new ResourceBundleFilter(), false));
        // filters.addFilter(new OneFilter(new XHTMLFilter(), false));  // XHTML before HTML
        filters.addFilter(new OneFilter(new HTMLFilter(), false));
        filters.addFilter(new OneFilter(new OOFilter(), false));
    }

    /**
     * Loads filter classes from plugins.
     * <p>
     * Filter plugins should be situated in &lt;OmegaT-install-dir&gt;/plugins,
     * and be packed as JAR files with manifest stating
     * <pre> OmegaT-Plugin: true </pre>
     * and then for each filter
     * <pre> Name: the.package.name.TheFilterName
     * OmegaT-Filter: true</pre> for each filter in a plugin 
     * (plugin may have more than one filter).
     */
    private void loadFilterClassesFromPlugins()
    {
        List jars = new ArrayList(plugins.size());
        for(int i=0; i<plugins.size(); i++)
        {
            List pfilters = (List)plugins.get(i);
            URL jarurl = (URL)pfilters.get(0);
            jars.add(jarurl);
        }
        
        URLClassLoader cl = new URLClassLoader((URL[])jars.toArray(new URL[plugins.size()]));
        for(int i=0; i<plugins.size(); i++)
        {
            List filterList = (List)plugins.get(i);
            for(int j=1; j<filterList.size(); j++)
            {
                try
                {
                    Class filter_class = cl.loadClass((String)filterList.get(j));
                    Constructor filter_constructor = filter_class.getConstructor((Class[])null);
                    Object filter = filter_constructor.newInstance((Object[])null);
                    if( filter instanceof AbstractFilter )
                    {
                        OneFilter one_filter = new OneFilter((AbstractFilter)filter, true);
                        filters.addFilter(one_filter);
                    }   
                }
                catch( Exception e )
                {
                    // couldn't load one of filters
                    // eat (almost) silently
                    StaticUtils.log("Filter '"+(String)filterList.get(j)+
                            "' from '"+((URL)filterList.get(0)).getFile()+"'"+
                            " cannot be loaded");
                }
            }
        }
    }
    
    /**
     * Saves information about the filters to an XML file.
     */
    public void saveConfig()
    {
        try
        {
            XMLEncoder xmlenc = new XMLEncoder(new FileOutputStream(configFile));
            xmlenc.writeObject(filters);
            xmlenc.close();
        }
        catch( FileNotFoundException fnfe )
        {
            StaticUtils.log(OStrings.getString("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG") + fnfe);
            JOptionPane.showMessageDialog(null, 
                    OStrings.getString("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG") + fnfe,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    // Static Utility Methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Whether the mask matches the filename. 
     * Filename should be "name.ext", without path.
     *
     * @param filename The filename to check
     * @param mask The mask, against which the filename is tested
     * @return Whether the mask matches the filename.
     */
    private boolean matchesMask(String filename, String mask)
    {
        mask = mask.replaceAll("\\.", "\\.");                                   // NOI18N
        mask = mask.replaceAll("\\*", ".*");                                    // NOI18N
        mask = mask.replaceAll("\\?", ".");                                     // NOI18N
        return filename.matches("(?iu)"+mask);                                  // NOI18N
    }
    
    /**
     * Construct a target filename according to pattern from a file's name.
     * Filename should be "name.ext", without path.
     * <p>
     * Output filename pattern is pretty complex.
     * <br>
     * It may consist of normal characters and some substituted variables. 
     * They have the format <code>${variableName}</code> and are case insensitive.
     * <br>
     * There're such variables:
     * <ul>
     * <li><code>${filename}</code> - full filename of the input file, both name and extension (default)
     * <li><code>${nameOnly}</code> - only the name of the input file without extension part
     * <li><code>${extension}</code> - the extension of the input file
     * <li><code>${sourceLanguage}</code> - the source language of the project
     * <li><code>${targetLanguage}</code> - the target language of the project
     * </ul>
     * <p>
     * Most file filters will use default "<code>${filename}</code>, 
     * that leads to the name of translated file being the same as
     * the name of source file. But for example the Java(TM) Resource Bundles file filter
     * will have the pattern equal to 
     * "<code>${nameonly}_${targetlanguage}.${extension}</code>".
     * <p>
     * E.g. if you have 
     * <ul>
     * <li>a source filename mask "*.ext1.ext2"
     * <li>file name "thisisfile.ext1.ext2"
     * </ul>
     * Then
     * <ul>
     * <li><code>${nameOnly}</code> will be equal to "thisisfile"
     * <li>and <code>${extension}</code> - "ext1.ext2"
     * </ul>
     * 
     * @param filename Filename to change
     * @param pattern Pattern, according to which we change the filename
     * @return The changed filename
     */
    private String constructTargetFilename(String sourceMask, String filename, String pattern)
    {
        int lastStarPos = sourceMask.lastIndexOf('*');
        int dot = 0; 
        if( lastStarPos>=0 )
            dot = filename.indexOf('.', lastStarPos);
        else
            dot = filename.lastIndexOf('.');
        String nameOnly = filename;
        String extension = "";                                                  // NOI18N
        if( dot>=0 )
        {
            nameOnly = filename.substring(0, dot);
            extension = filename.substring(dot+1);
        }
        String sourceLanguage = CommandThread.core.
                getPreference(OConsts.PREF_SOURCELOCALE).replace('-', '_');
        String targetLanguage = CommandThread.core.
                getPreference(OConsts.PREF_TARGETLOCALE).replace('-', '_');
        
        String res = pattern;
        res = res.replaceAll("(?i)\\$\\{filename\\}", filename);                // NOI18N
        res = res.replaceAll("(?i)\\$\\{nameOnly\\}", nameOnly);                // NOI18N
        res = res.replaceAll("(?i)\\$\\{extension\\}", extension);              // NOI18N
        res = res.replaceAll("(?i)\\$\\{sourceLanguage\\}", sourceLanguage);    // NOI18N
        res = res.replaceAll("(?i)\\$\\{targetLanguage\\}", targetLanguage);    // NOI18N
        return res;
    }
}
