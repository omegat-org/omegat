/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
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

package org.omegat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.omegat.filters2.TranslationException;
import org.omegat.util.xml.XMLBlock;
import org.omegat.util.xml.XMLStreamReader;

/**
 * Class to load & save OmegaT preferences.
 * All methods are static here.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 */
public class Preferences
{
    /** OmegaT-wide Preferences Filename */
    public static final String FILE_PREFERENCES	= "omegat.prefs";				// NOI18N
    
    // preference names
    public static final String SOURCE_LOCALE	= "source_lang";                // NOI18N
    public static final String TARGET_LOCALE	= "target_lang";                // NOI18N
    public static final String CURRENT_FOLDER	= "current_folder";				// NOI18N
    public static final String SOURCE_FOLDER	= "source_folder";				// NOI18N
    public static final String TARGET_FOLDER	= "target_folder";				// NOI18N
    public static final String TM_FOLDER		= "tm_folder";                  // NOI18N
    public static final String GLOSSARY_FOLDER	= "glossary_folder";            // NOI18N
    
    public static final String MAINWINDOW_WIDTH  = "screen_width";              // NOI18N
    public static final String MAINWINDOW_HEIGHT = "screen_height";             // NOI18N
    public static final String MAINWINDOW_X      = "screen_x";                  // NOI18N
    public static final String MAINWINDOW_Y      = "screen_y";                  // NOI18N
    public static final String MAINWINDOW_LAYOUT = "docking_layout";            // NOI18N
    
    // Search window size and position    
    public static final String SEARCHWINDOW_WIDTH  = "search_window_width";     // NOI18N
    public static final String SEARCHWINDOW_HEIGHT = "search_window_height";    // NOI18N
    public static final String SEARCHWINDOW_X      = "search_window_x";         // NOI18N
    public static final String SEARCHWINDOW_Y      = "search_window_y";         // NOI18N
    
    // Tag validation window size and position    
    public static final String TAGVWINDOW_WIDTH  = "tagv_window_width";         // NOI18N
    public static final String TAGVWINDOW_HEIGHT = "tagv_window_height";        // NOI18N
    public static final String TAGVWINDOW_X      = "tagv_window_x";             // NOI18N
    public static final String TAGVWINDOW_Y      = "tagv_window_y";             // NOI18N
    
    // Help window size and position    
    public static final String HELPWINDOW_WIDTH  = "help_window_width";         // NOI18N
    public static final String HELPWINDOW_HEIGHT = "help_window_height";        // NOI18N
    public static final String HELPWINDOW_X      = "help_window_x";             // NOI18N
    public static final String HELPWINDOW_Y      = "help_window_y";             // NOI18N
    
    /** Use the TAB button to advance to the next segment */
    public static final String USE_TAB_TO_ADVANCE     = "tab_advance";          // NOI18N
    /** Always confirm Quit, even if the project is saved */
    public static final String ALWAYS_CONFIRM_QUIT     = "always_confirm_quit"; // NOI18N
    public static final String SEARCH_FOLDER	= "search_dir";                 // NOI18N
    
    /** Workflow Option: Don't Insert Source Text Into Translated Segment */
    public static final String DONT_INSERT_SOURCE_TEXT = "wf_noSourceText";                     // NOI18N
    /** Workflow Option: Allow translation to be equal to source */
    public static final String ALLOW_TRANS_EQUAL_TO_SRC = "wf_allowTransEqualToSrc";       // NOI18N
    /** Workflow Option: Insert Best Match Into Translated Segment */
    public static final String BEST_MATCH_INSERT = "wf_insertBestMatch";                        // NOI18N
    /** Workflow Option: Minimal Similarity Of the Best Fuzzy Match to insert */
    public static final String BEST_MATCH_MINIMAL_SIMILARITY = "wf_minimalSimilarity";          // NOI18N
    /** Default Value of Workflow Option: Minimal Similarity Of the Best Fuzzy Match to insert */
    public static final String BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT = "80";                    // NOI18N
    /** Workflow Option: Insert Explanatory Text before the Best Fuzzy Match */
    public static final String BEST_MATCH_EXPLANATORY_TEXT = "wf_explanatoryText";              // NOI18N

    /** 
     * Version of file filters. 
     * Unfortunately cannot put it into filters itself for backwards 
     * compatibility reasons.
     */
    public static final String FILTERS_VERSION = "filters_version";             // NOI18N
    
    
    /** Private constructor, because this file is singleton */
    static
    {
        m_loaded = false;
        m_preferenceMap = new HashMap(64);
        m_nameList = new ArrayList(32);
        m_valList = new ArrayList(32);
        m_changed = false;
        doLoad();
    }
    
    /**
     * Returns the defaultValue of some preference out of OmegaT's preferences file.
     * <p>
     * If the key is not found, returns the empty string.
     *
     * @param key key of the key to look up, usually OConsts.PREF_...
     * @return    preference defaultValue as a string
     */
    public static String getPreference(String key)
    {
        if (key == null || key.equals(""))					// NOI18N
            return "";								// NOI18N
        if (!m_loaded)
            doLoad();
        
        Integer i = (Integer) m_preferenceMap.get(key);
        String v = "";								// NOI18N
        if (i != null)
        {
            // mapping exists - recover defaultValue
            v = (String) m_valList.get(i.intValue());
        }
        return v;
    }
    
    /**
     * Returns the boolean defaultValue of some preference.
     * <p>
     * Returns true if the preference exists and is equal to "true",
     * false otherwise (no such preference, or it's equal to "false", etc).
     *
     * @param key preference key, usually OConsts.PREF_...
     * @return    preference defaultValue as a boolean
     */
    public static boolean isPreference(String key)
    {
        return "true".equals(getPreference(key));                               // NOI18N
    }
    
    /**
     * Returns the value of some preference out of OmegaT's preferences file,
     * if it exists.
     * <p>
     * If the key is not found, returns the default value provided
     * and sets the preference to the default value.
     *
     * @param key           name of the key to look up, usually OConsts.PREF_...
     * @param defaultValue  default value for the key
     * @return              preference value as a string
     */
    public static String getPreferenceDefault(String key, String defaultValue)
    {
        String val = getPreference(key);
        if (val.equals("")) // NOI18N
        {
            val = defaultValue;
            setPreference(key, defaultValue);
        }
        return val;
    }
    
    /**
     * Returns the integer value of some preference out of OmegaT's 
     * preferences file, if it exists.
     * <p>
     * If the key is not found, returns the default value provided
     * and sets the preference to the default value.
     *
     * @param key           name of the key to look up, usually OConsts.PREF_...
     * @param defaultValue  default value for the key
     * @return              preference value as an integer
     */
    public static int getPreferenceDefault(String key, int defaultValue)
    {
        String val = getPreferenceDefault(key, Integer.toString(defaultValue));
        int res = defaultValue;
        try
        {
            res = Integer.parseInt(val);
        } catch (NumberFormatException nfe) { }
        return res;
    }
    
    /**
     * Sets the value of some preference.
     *
     * @param name  preference key name, usually Preferences.PREF_...
     * @param value preference value as a string
     */
    public static void setPreference(String name, String value)
    {
        m_changed = true;
        if( name!=null && name.length()!=0 && value!=null )
        {
            if (!m_loaded)
                doLoad();
            Integer i = (Integer) m_preferenceMap.get(name);
            if (i == null)
            {
                // defaultValue doesn't exist - add it
                i = new Integer(m_valList.size());
                m_preferenceMap.put(name, i);
                m_valList.add(value);
                m_nameList.add(name);
            }
            else
            {
                // mapping exists - reset defaultValue to new
                m_valList.set(i.intValue(), value);
            }
        }
    }
    /**
     * Sets the boolean value of some preference.
     *
     * @param name      preference key name, usually Preferences.PREF_...
     * @param boolvalue preference defaultValue as a boolean
     */
    public static void setPreference(String name, boolean boolvalue)
    {
        setPreference(name, String.valueOf(boolvalue));
    }
    /**
     * Sets the int value of some preference.
     *
     * @param name     preference key name, usually Preferences.PREF_...
     * @param intvalue preference value as an integer
     */
    public static void setPreference(String name, int intvalue)
    {
        setPreference(name, String.valueOf(intvalue));
    }
    
    public static void save()
    {
        try
        {
            if (m_changed)
                doSave();
        }
        catch (IOException e)
        {
            StaticUtils.log(OStrings.getString("PM_ERROR_SAVE") + e);
        }
    }
    
    private static void doLoad()
    {
        try
        {
            // mark as loaded - if the load fails, there's no use
            //  trying again later
            m_loaded = true;
            
            XMLStreamReader xml = new XMLStreamReader();
            xml.killEmptyBlocks();
            xml.setStream(new File(StaticUtils.getConfigDir() + FILE_PREFERENCES));
            XMLBlock blk;
            ArrayList lst;
            
            m_preferenceMap.clear();
            String pref;
            String val;
            // advance to omegat tag
            if (xml.advanceToTag("omegat") == null)	// NOI18N
                return;
            
            // advance to project tag
            if ((blk = xml.advanceToTag("preference")) == null)	// NOI18N
                return;
            
            String ver = blk.getAttribute("version");                           // NOI18N
            if (ver != null && !ver.equals("1.0"))                              // NOI18N
            {
                // unsupported preference file version - abort read
                return;
            }
            
            lst = xml.closeBlock(blk);
            if (lst == null)
                return;
            
            Integer j;
            for (int i=0; i<lst.size(); i++)
            {
                blk = (XMLBlock) lst.get(i);
                if (blk.isClose())
                    continue;
                
                if (!blk.isTag())
                    continue;
                
                pref = blk.getTagName();
                blk = (XMLBlock) lst.get(++i);
                if (blk.isTag())
                {
                    // parse error - keep trying
                    continue;
                }
                val = blk.getText();
                if (pref != null && !pref.equals("") && val != null)		// NOI18N
                {
                    // valid match - record these
                    j = new Integer(m_valList.size());
                    m_preferenceMap.put(pref, j);
                    m_nameList.add(pref);
                    m_valList.add(val);
                }
            }
        }
        catch (TranslationException te)
        {
            // error loading preference file - keep whatever was
            //  loaded then return gracefully to calling function
            // print an error to the console as an FYI
            StaticUtils.log(OStrings.getString("PM_WARNING_PARSEERROR_ON_READ") + te);
            te.printStackTrace(StaticUtils.getLogStream());
        }
        catch (IndexOutOfBoundsException e3)
        {
            // error loading preference file - keep whatever was
            //  loaded then return gracefully to calling function
            // print an error to the console as an FYI
            StaticUtils.log(OStrings.getString("PM_WARNING_PARSEERROR_ON_READ") + e3);
            e3.printStackTrace(StaticUtils.getLogStream());
        }
        catch (UnsupportedEncodingException e3)
        {
            // unrecognized file - forget about it
            StaticUtils.log("Unrecognized file's encoding"); // NOI18N
            e3.printStackTrace(StaticUtils.getLogStream());
        }
        catch (IOException e4)
        {
            // can't read file - forget about it and move on
            // e4.printStackTrace();
        }
    }
    
    private static void doSave() throws IOException
    {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(StaticUtils.getConfigDir() + FILE_PREFERENCES), 
                "UTF-8"));                                                      // NOI18N
        
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");             // NOI18N
        out.write("<omegat>\n");						// NOI18N
        out.write("  <preference version=\"1.0\">\n");				// NOI18N
        
        for (int i=0; i<m_nameList.size(); i++)
        {
            String name = (String) m_nameList.get(i);
            String val = StaticUtils.makeValidXML((String) m_valList.get(i));
            if (val.equals(""))							// NOI18N
                continue;	// don't write blank preferences
            out.write("    <" + name + ">");					// NOI18N
            out.write(val);
            out.write("</" + name + ">\n");					// NOI18N
        }
        out.write("  </preference>\n");						// NOI18N
        out.write("</omegat>\n");						// NOI18N
        out.close();
        m_changed = false;
    }
    
    
    private static boolean	m_loaded;
    private static boolean	m_changed;
    
    // use a hash map for fast lookup of data
    // use array lists for orderly recovery of it for saving to disk
    private static ArrayList m_nameList;
    private static ArrayList m_valList;
    private static HashMap   m_preferenceMap;
    
}

