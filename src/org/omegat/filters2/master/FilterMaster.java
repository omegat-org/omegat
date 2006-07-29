/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 Portions Copyright (C) 2005-06 Henry Pijffers
 Portions Copyright (C) 2006 Martin Wunderlich
               Home page: http://www.omegat.org/omegat/omegat.html
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

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

import org.omegat.core.StringEntry;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.po.PoFilter;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.filters2.text.ini.INIFilter;
import org.omegat.filters3.xml.docbook.DocBookFilter;
import org.omegat.filters3.xml.opendoc.OpenDocFilter;
import org.omegat.filters3.xml.opendoc.OpenDocXMLFilter;
import org.omegat.filters3.xml.xhtml.XHTMLFilter;
import org.omegat.util.LFileCopy;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * A master class that registers and handles all the filters.
 * Singleton - there can be only one instance of this class.
 *
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 */
public class FilterMaster
{
	/** name of the filter configuration file */
	private final static String FILE_FILTERS = "filters.conf";                  // NOI18N

    /** There was no version of file filters support (1.4.5 Beta 1 -- 1.6.0 RC12). */
    public static String INITIAL_VERSION = new String();
    /** File filters support of 1.6.0 RC12a: now upgrading the configuration. */
    public static String OT160RC12a_VERSION = "1.6 RC12a";                        // NOI18N
    /** Currently file filters support version. */
    public static String CURRENT_VERSION = OT160RC12a_VERSION;
    
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
        PluginUtils.loadPlugins();
        if( configFile.exists() )
            loadConfig();
        else
            filters = setupBuiltinFilters();
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
        return memorizing && (searchthread==null);
    }
    
    /**
     * Ugly hack to remove Windows Line Feed "\r".
     */
    private String removeLineFeed(String s)
    {
        return s.replaceAll("\r", "");                                          // NOI18N
    }
    
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
        
        // some special space handling
        int len = src.length();
        int b = 0;
        StringBuffer bs = new StringBuffer();
        while( b<len && Character.isWhitespace(src.charAt(b)) )
        {
            bs.append(src.charAt(b));
            b++;
        }

        int e = len-1;
        StringBuffer es = new StringBuffer();
        while( e>=b && Character.isWhitespace(src.charAt(e)) )
        {
            es.append(src.charAt(e));
            e--;
        }
        es.reverse();

        src = src.substring(b, e+1);
        
        StringBuffer res = new StringBuffer();
        res.append(bs);
        
        if( CommandThread.core.getProjectProperties().isSentenceSegmentingEnabled() )
        {
            List spaces = new ArrayList();
            List brules = new ArrayList();
            List segments = Segmenter.segment(src, spaces, brules);
            for(int i=0; i<segments.size(); i++)
            {
                String onesrc = (String)segments.get(i);
                segments.set(i, processSingleEntry(onesrc));
            }
            res.append(Segmenter.glue(segments, spaces, brules));
        }
        else
            res.append(processSingleEntry(src));
        
        res.append(es);
        return res.toString();
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
                String s = se.getTranslation();
                if( s==null || s.length()==0 )
                    s = src;
                return s;
            }
        }
    }
    
    /**
     * OmegaT core calls this method to load a source file.
     *
     * @param filename  The name of the source file to load.
     * @param processedFiles The set of already processed files.
     * @return          Whether the file was handled by one of OmegaT filters.
     * @see #translateFile(String, String, String)
     */
    public boolean loadFile(String filename, Set processedFiles)
            throws IOException, TranslationException
    {
        try
        {
            LookupInformation lookup = lookupFilter(filename);
            if( lookup==null )
                return false;

            setMemorizing(true);

            File inFile = new File(filename);
            String inEncoding = lookup.inEncoding;
            AbstractFilter filterObject = lookup.filterObject;
            List files = filterObject.processFile(inFile, inEncoding, null, null);
            if (files!=null)
                processedFiles.addAll(files);
        }
        catch( IOException ioe )
        {
            ioe.printStackTrace();
            throw new IOException(filename + "\n" + ioe);                       // NOI18N
        }
        
        setMemorizing(false);
        return true;
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
     * @param processedFiles Set of already searched files.
     * @see #translateFile(String, String, String)
     */
    public void searchFile(String filename, SearchThread searchthread, Set processedFiles)
            throws IOException, TranslationException
    {
        setSearchMode(searchthread);
        loadFile(filename, processedFiles);
        cancelSearchMode();
    }
    
    /**
     * OmegaT core calls this method to translate a source file.
     * <ul>
     * <li>OmegaT first looks through registered filter instances
     *     to find filter(s) that can handle this file.
     * <li>Tests if filter(s) want to handle it.
     * <li>If the filter accepts the file,
     * <li>Filter is asked to process the file.
     * </ul>
     * If no filter is found, that processes this file,
     * we simply copy it to target folder.
     * 
     * @param sourcedir The folder of the source inFile.
     * @param filename  The name of the source inFile to process (only the part, relative to source folder).
     * @param targetdir The folder to place the translated inFile to.
     * @param processedFiles Set of all already processed files not to redo them again.
     */
    public void translateFile(String sourcedir, String filename, String targetdir, Set processedFiles)
            throws IOException, TranslationException
    {
        setMemorizing(false);
        
        LookupInformation lookup = lookupFilter(sourcedir+File.separator+filename);
        if( lookup==null )
        {
            // The file is not supported by any of the filters.
            // Copying it
            LFileCopy.copy(sourcedir+File.separator+filename,
                    targetdir+File.separator+filename);
            return;
        }
        
        File inFile = new File(sourcedir+File.separator+filename);
        String inEncoding = lookup.inEncoding;
        
        String name = inFile.getName();
        String path = filename.substring(0, filename.length()-name.length());
        
        Instance instance = lookup.instance;
        File outFile =
                new File(
                targetdir + File.separator +
                path + File.separator +
                constructTargetFilename(
                instance.getSourceFilenameMask(),
                name,
                instance.getTargetFilenamePattern()));
        String outEncoding = instance.getTargetEncoding();
        
        AbstractFilter filterObject = lookup.filterObject;
        List files = filterObject.processFile(inFile, inEncoding, outFile, outEncoding);
        if (files!=null)
            processedFiles.addAll(files);
    }
    
    class LookupInformation
    {
        public OneFilter filter;
        public Instance instance;
        public AbstractFilter filterObject;
        public String inEncoding;
        
        public LookupInformation(OneFilter filter, Instance instance,
                AbstractFilter filterObject, String inEncoding)
        {
            this.filter = filter;
            this.instance = instance;
            this.filterObject = filterObject;
            this.inEncoding = inEncoding;
        }
    }
    
    /**
     * Gets the filter according to the source
     * filename provided.
     * In case of failing to find a filter to handle the file
     * returns <code>null</code>.
     * 
     * In case of finding an appropriate filter it
     * <ul>
     * <li>Creates the filter (use <code>OneFilter.getFilter()</code> to get it)
     * <li>Creates a reader (use <code>OneFilter.getReader()</code> to get it)
     * <li>Checks whether the filter supports the file.
     * </ul>
     * It <b>does not</b> check whether the filter supports the inFile,
     * i.e. it doesn't call <code>isFileSupported</code>
     * 
     * 
     * @param filename    The source filename.
     * @return The filter to handle the inFile.
     */
    private LookupInformation lookupFilter(String filename)
            throws TranslationException, IOException
    {
        File inFile = new File(filename);
        String name = inFile.getName();
        String path = inFile.getParent();
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
                    AbstractFilter filterObject;
                    filterObject = PluginUtils.instantiateFilter(filter);
                    
                    String inEncoding = instance.getSourceEncoding();
                    if( !filterObject.isFileSupported(inFile, inEncoding) )
                    {
                        break;
                    }
                    
                    return new LookupInformation(filter, instance, filterObject, inEncoding);
                }
            }
        }
        return null;
    }
    
    
    private static List supportedEncodings = null;
    /**
     * Queries JRE for the list of supported encodings.
     * Also adds the human name for no/automatic inEncoding.
     * 
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
        filters = setupBuiltinFilters();
        PluginUtils.loadPlugins();
        loadFilterClassesFromPlugins();
        saveConfig();
    }
    
    /** XML file with filters configuration */
    private File configFile = new File(StaticUtils.getConfigDir() + FILE_FILTERS);
    
    /**
     * My Own Class to listen to exceptions,
     * occured while loading filters configuration.
     */
    class MyExceptionListener implements ExceptionListener
    {
        private List exceptionsList = new ArrayList();
        private boolean exceptionOccured = false;
        public void exceptionThrown(Exception e)
        {
            exceptionOccured = true;
            exceptionsList.add(e);
        }
        
        /**
         * Returns whether any exceptions occured.
         */
        public boolean isExceptionOccured()
        {
            return exceptionOccured;
        }
        /**
         * Returns the list of occured exceptions.
         */
        public List getExceptionsList()
        {
            return exceptionsList;
        }
    }
    
    /**
     * Loads information about the filters from an XML file.
     * If there's an error loading a file, it calls <code>setupDefaultFilters</code>.
     */
    public void loadConfig()
    {
        try
        {
            MyExceptionListener myel = new MyExceptionListener();
            XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), this, myel);
            filters = (Filters)xmldec.readObject();
            xmldec.close();
            
            if( myel.isExceptionOccured() )
            {
                StringBuffer sb = new StringBuffer();
                List exceptions = myel.getExceptionsList();
                for(int i=0; i<exceptions.size(); i++)
                {
                    sb.append("    ");                                          // NOI18N
                    sb.append(exceptions.get(i));
                    sb.append("\n");                                            // NOI18N
                }
                throw new Exception("Exceptions occured while loading file filters:\n"+sb.toString()); // NOI18N
            }
            
            checkIfAllFilterPluginsAreAvailable();
            
            // checking the version
            if (CURRENT_VERSION.compareTo(Preferences.getPreference(Preferences.FILTERS_VERSION))>0)
            {
                // yeap, the config file with filters settings is of the older version
                
                // initing defaults
                Filters defaults = setupBuiltinFilters();
                // and merging them into loaded settings
                filters = upgradeFilters(filters, defaults);
            }
        }
        catch( Exception e )
        {
            StaticUtils.log(OStrings.getString("FILTERMASTER_ERROR_LOADING_FILTERS_CONFIG") + e);
            filters = setupBuiltinFilters();
        }
    }
    

    /** Upgrades current filters settings using current defaults. */
    private Filters upgradeFilters(Filters filters, Filters defaults)
    {
        if (OT160RC12a_VERSION.compareTo(Preferences.getPreference(Preferences.FILTERS_VERSION))>0)
        {
            // removing old OO filter but moving all its instances to new OpenDoc one
            for (int i = 0; i < filters.getFilter().length; i++)
            {
                OneFilter oo = filters.getFilter(i);
                if (oo.getClassName().equals("org.omegat.filters2.xml.openoffice.OOFilter")) // NOI18N
                {
                    OneFilter opendoc = new OneFilter(new OpenDocFilter(), false);
                    for (int j = 0; j < oo.getInstance().length; j++)
                    {
                        Instance ooi = oo.getInstance(j);
                        for (int k = 0; k < opendoc.getInstance().length; k++)
                        {
                            Instance odi = opendoc.getInstance(k);
                            if (odi.getSourceFilenameMask().equals(ooi.getSourceFilenameMask()))
                            {
                                opendoc.setInstance(k, ooi);
                                break;
                            }
                        }
                    }
                    filters.setFilter(i, opendoc);
                    break;
                }
            }
        }
        
        // now adding those filters from defaults which appeared in new version only
        HashSet existing = new HashSet();
        for (int i = 0; i < filters.getFilter().length; i++)
            existing.add(filters.getFilter(i).getClassName());
        for (int i = 0; i < defaults.getFilter().length; i++)
        {
            OneFilter deffilter = defaults.getFilter(i);
            if (!existing.contains(deffilter.getClassName()))
                filters.addFilter(deffilter);
        }
        
        return filters;
    }
    
    /**
     * Goes through the list of loaded plugins to see if
     * all the filters that are there in config file are present.
     */
    private void checkIfAllFilterPluginsAreAvailable()
    {
        ClassLoader cl = PluginUtils.getPluginsClassloader();
        List plugins = PluginUtils.getPlugins();
        
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
    private Filters setupBuiltinFilters()
    {
        Filters res = new Filters();
        res.addFilter(new OneFilter(new TextFilter(), false));
        res.addFilter(new OneFilter(new PoFilter(), false));
        res.addFilter(new OneFilter(new ResourceBundleFilter(), false));
        res.addFilter(new OneFilter(new XHTMLFilter(), false));
        res.addFilter(new OneFilter(new HTMLFilter2(), false));
        res.addFilter(new OneFilter(new INIFilter(), false));
        res.addFilter(new OneFilter(new DocBookFilter(), false));
        res.addFilter(new OneFilter(new OpenDocFilter(), false));
        return res;
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
        ClassLoader cl = PluginUtils.getPluginsClassloader();
        List plugins = PluginUtils.getPlugins();
        
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
                    StaticUtils.log("Filter '"+(String)filterList.get(j)+       // NOI18N
                            "' from '"+((URL)filterList.get(0)).getFile()+"'"+  // NOI18N
                            " cannot be loaded");                               // NOI18N
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
            Preferences.setPreference(Preferences.FILTERS_VERSION, CURRENT_VERSION);
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
        mask = mask.replaceAll("\\.", "\\\\.");                                   // NOI18N
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
     * <li><code>${targetLocale}</code> - target locale code (of a form "xx_YY")
     * <li><code>${targetLanguage}</code> - the target language and country code together (of a form "XX-YY")
     * <li><code>${targetLanguageCode}</code> - the target language only ("XX")
     * <li><code>${targetCoutryCode}</code> - the target country only ("YY")
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
        {
            // bugfix #1204740
            // so where's the dot next to the star
            int lastDotPos=sourceMask.indexOf('.', lastStarPos);
            // counting chars after the dot
            int extlength=sourceMask.length()-lastDotPos;
            // going forward this many chars
            // and finding the dot we looked for
            dot = filename.length()-extlength;
        }
        else
        {
            dot = filename.lastIndexOf('.');
        }
        
        String nameOnly = filename;
        String extension = "";                                                  // NOI18N
        if( dot>=0 )
        {
            nameOnly = filename.substring(0, dot);
            extension = filename.substring(dot+1);
        }
        
        String res = pattern;
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_FILENAME),
                filename);
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_NAMEONLY),
                nameOnly);
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_EXTENSION),
                extension);
        
        Language targetLang = new Language(
                Preferences.getPreference(Preferences.TARGET_LOCALE));
        
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LOCALE),
                targetLang.getLocale());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LANGUAGE),
                targetLang.getLanguage());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LANG_CODE),
                targetLang.getLanguageCode());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_COUNTRY_CODE),
                targetLang.getCountryCode());
        
        return res;
    }
    
    private String targetRegexer(String tfp)
    {
        String pattern = tfp;
        pattern = pattern.replaceAll("\\$", "\\\\\\$");                         // NOI18N
        pattern = pattern.replaceAll("\\{", "\\\\{");                           // NOI18N
        pattern = pattern.replaceAll("\\}", "\\\\}");                           // NOI18N
        pattern = "(?i)"+pattern;                                               // NOI18N
        return pattern;
    }
}
