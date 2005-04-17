/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.master;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.omegat.core.StringEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.xml.openoffice.OOFileHandler;
import org.omegat.filters2.xml.xhtml.XHTMLFileHandler;
import org.omegat.util.LFileCopy;
import org.omegat.filters2.*;
import org.omegat.filters2.Instance;
import org.omegat.filters2.html.HTMLFilter;

import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.util.OConsts;

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
        if( configFile.exists() )
            loadConfig();
        else
            setupDefaultFilters();
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
    

    private boolean memorizing = true;
    /**
     * Call this to turn on/off memorizing passed strings
     * in internal Translation Memory.
     * Typically to be called by Search-in-files functionality.
     */
    public void setMemorizing(boolean value)
    {
        memorizing = value;
    }
    /**
     * Returns whether we're memorizing passed strings
     * in internal Translation Memory.
     */
    public boolean isMemorizing()
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

        // if the search thread is non-null, we're searching inside files
        // else we're translating them
        if( searchthread!=null )
        {
            searchthread.searchText(src);
        }
        {
            StringEntry se = CommandThread.core.getStringEntry(src);

            if( se==null )
            {
                if( isMemorizing() )
                    CommandThread.core.addEntry(src);
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
     * OmegaT core calls this method to load a source file.
     *
     * @param filename  The name of the source file to load.
     * @see #translateFile(String, String, String)
     */
    public void loadFile(String filename)
            throws IOException, TranslationException
    {
        File file = new File(filename);
        String name = file.getName();
        String path = file.getParent();
        if( path==null )
            path = "";
        
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
                    
                    if( !filterObject.isFileSupported(file) )
                    {
                        j = filter.getInstance().length;
                        continue;
                    }
                    
                    Reader reader = filterObject.createReader(file, instance.getSourceEncoding());
                    
                    Writer writer = new StringWriter();
                    
                    filterObject.processFile(reader, writer);
                    reader.close();
                    writer.close();
                    
                    return;
                }
            }
        }
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
        int lastslash = filename.lastIndexOf(File.separatorChar);
        String name = filename.substring(lastslash+1);
        String path = "";
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
                if( matchesMask(name, instance.getSourceFilenameMask()) )
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
                    
                    if( !filterObject.isFileSupported(infile) )
                    {
                        j = filter.getInstance().length;
                        continue;
                    }
                    
                    Reader reader = filterObject.createReader(infile, instance.getSourceEncoding());
                    
                    File outfile = new File(targetdir+File.separatorChar+path+
                            constructFilename(name, instance.getTargetFilenamePattern()));
                    Writer writer = filterObject.createWriter(outfile, instance.getTargetEncoding());
                    
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
     *
     * @return names of all the encodings in an array
     */
    public static List getSupportedEncodings()
    {
        if( supportedEncodings==null )
        {
            supportedEncodings = new ArrayList();
            supportedEncodings.add(AbstractFilter.ENCODING_AUTO);
            supportedEncodings.addAll(Charset.availableCharsets().keySet());
        }
        return supportedEncodings;
    }
    

    //////////////////////////////////////////////////////////////////////////
    // Filters
    //////////////////////////////////////////////////////////////////////////

    /** XML file with filters configuration */
    private File configFile = new File("filters.conf");
    
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
        }
        catch( Exception e )
        {
            System.out.println("Error parsing a saved file: " + e);
            setupDefaultFilters();
        }
    }
    
    /**
     * Initializes Filter Master defaults
     * by re-creating all information about file filters.
     */
    public void setupDefaultFilters()
    {
        filters = new Filters();
        filters.setFilter(new OneFilter[]
            {
                new OneFilter(new TextFilter()),
                new OneFilter(new ResourceBundleFilter()),
                new OneFilter(new XHTMLFileHandler()),  // XHTML before HTML
                new OneFilter(new HTMLFilter()),
                new OneFilter(new OOFileHandler()),
            });
        saveConfig();
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
            System.out.println("Error saving filters configuration: " + fnfe);
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    // Static Utility Methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Whether the mask matches the filename. 
     * Filename should be "name.ext", without path.
     * @param filename The filename to check
     * @param mask The mask, against which the filename is tested
     * @return Whether the mask matches the filename.
     */
    public static boolean matchesMask(String filename, String mask)
    {
        mask = mask.replaceAll("\\.", "\\.");
        mask = mask.replaceAll("\\*", ".*");
        mask = mask.replaceAll("\\?", ".");
        return filename.matches("(?iu)"+mask);
    }
    
    /**
     * Construct a filename according to pattern from a file's name.
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
     * @param filename Filename to change
     * @param pattern Pattern, according to which we change the filename
     * @return The changed filename
     */
    public static String constructFilename(String filename, String pattern)
    {
        int dot = filename.lastIndexOf('.');
        String nameOnly = filename;
        String extension = "";
        if( dot>=0 )
        {
            nameOnly = filename.substring(0, dot);
            extension = filename.substring(dot+1);
        }
        String sourceLanguage = CommandThread.core.getPreference(OConsts.PREF_SRCLANG);
        String targetLanguage = CommandThread.core.getPreference(OConsts.PREF_LOCLANG);
        
        String res = pattern;
        res = res.replaceAll("(?i)\\$\\{filename\\}", filename);
        res = res.replaceAll("(?i)\\$\\{nameOnly\\}", nameOnly);
        res = res.replaceAll("(?i)\\$\\{extension\\}", extension);
        res = res.replaceAll("(?i)\\$\\{sourceLanguage\\}", sourceLanguage);
        res = res.replaceAll("(?i)\\$\\{targetLanguage\\}", targetLanguage);
        return res;
    }
}
