/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 Portions Copyright 2006 Henry Pijffers
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

package org.omegat.core.threads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.TransMemory;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.SearchWindow;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;


/**
 * Each search window has its own search thread to actually do the
 * searching.
 * This prevents lockup of the UI during intensive searches
 *
 * @author Keith Godfrey
 * @author Henry Pijffers
 */
public class SearchThread extends Thread
{
    public SearchThread(MainWindow par, String startText)
    {
        setPriority(Thread.MIN_PRIORITY);
        
        m_window = new SearchWindow(par, this, startText);
        m_searchDir = null;
        m_searchRecursive = false;
        m_searchText = "";	// NOI18N
        m_searching = false;
        m_tmSearch = false;
        m_entrySet = null; // HP
                
        m_numFinds = 0;
        m_curFileName = "";	// NOI18N
        
        m_extList = new ArrayList();
        m_extMapList = new ArrayList();
    }
    
    /////////////////////////////////////////////////////////
    // public interface
    
    /**
     * Starts a search if another is not currently running.
     * To search current project only, set rootDir to null.
     *
     * @param text string to searh for
     * @param rootDir folder to search in
     * @param recursive search in subfolders of rootDir too
     * @param exact search for a substring
     * @param tm search in legacy and orphan TM strings too
     * @param keyword search for keywords
     */
    public void requestSearch(String text, String rootDir,
            boolean recursive, boolean exact, boolean tm, boolean keyword, boolean regex)
    {
        if (!m_searching)
        {
            m_searchDir = rootDir;
            m_searchRecursive = recursive;
            m_searchText = text;
            m_exactSearch = exact;
            m_tmSearch = tm;
            m_keywordSearch = keyword;
            m_regexSearch = regex;
            m_searching = true;
            m_entrySet = new HashSet(); // HP
        }
    }
    
    ///////////////////////////////////////////////////////////
    // thread main loop
    public void run()
    {
        // have search thread control search window to allow parent
        //	window to avoid blocking
        // need to spawn subthread so we don't block either
        DialogThread dlgThread = new DialogThread(m_window);
        dlgThread.start();
        
        boolean firstPass = true;
        try
        {
            while( !interrupted() )
            {
                try
                {
                    sleep(100); // not to occupy 100% CPU
                }
                catch (InterruptedException e)
                {
                    interrupt();
                }
                
                if (firstPass)
                {
                    // on first pass send a request to place cursor in
                    //	search field (otherwise search window has no
                    //	control with default keyboard focus)
                    // this is a hack, but can't find another way to do
                    //	this gracefully
                    firstPass = false;
                    m_window.setSearchControlFocus();
                }
                
                if (m_searching)
                {
                    // work to be done
                    if (m_searchDir == null)
                    {
                        // if no search directory specified, then we are
                        //	searching currnet project only
                        searchProject();
                    }
                    else
                    {
                        // search specified directory tree
                        try
                        {
                            searchFiles();
                        }
                        catch (IOException e)
                        {
                            // something bad happened
                            // alert user to badness
                            String msg = OStrings.ST_FILE_SEARCH_ERROR;
                            CommandThread.core.displayError(msg, e);
                        }
                        catch (TranslationException te)
                        {
                            // something bad happened
                            // alert user to badness
                            String msg = OStrings.ST_FILE_SEARCH_ERROR;
                            CommandThread.core.displayError(msg, te);
                        }
                    }
                    
                    // whatever the states is, error or not, display what's
                    //	been found so far
                    if (m_numFinds == 0)
                    {
                        // no match
                        m_window.postMessage(OStrings.ST_NOTHING_FOUND);
                    }
                    m_window.displayResults();
                    m_searching = false;
                    m_entrySet = null; // HP
                }
            }
        }
        catch (RuntimeException re)
        {
            String msg = OStrings.ST_FATAL_ERROR;
            CommandThread.core.displayError(msg, re);
            m_window.threadDied();
        }
    }
    
    //////////////////////////////////////////////////////////////
    // internal functions
    
    /**
     * Queue found string.
     * Since 1.6.0 RC9 removes duplicate segments (by Henry Pijffers)
     */
    private void foundString(int entryNum, String intro, String src,
            String target)
    {
        if (m_numFinds++ > OConsts.ST_MAX_SEARCH_RESULTS)
        {
            return;
        }
        
        if (entryNum >= 0)
        {
            if (!m_entrySet.contains(src + target)) { // HP, duplicate entry prevention
                // entries are referenced at offset 1 but stored at offset 0
                m_window.addEntry(entryNum+1, null, (entryNum+1)+"> "+src, target);	// NOI18N
                m_entrySet.add(src + target); // HP
            }
        }
        else
        {
            m_window.addEntry(entryNum, intro, src, target);
        }
        
        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
        {
            m_window.postMessage(OStrings.SW_MAX_FINDS_REACHED);
        }
    }
    
    private void searchProject()
    {
        m_numFinds = 0;
        if (m_exactSearch || m_regexSearch)
        {
            int i;
            for (i=0; i<CommandThread.core.numEntries(); i++)
            {
                SourceTextEntry ste = CommandThread.core.getSTE(i);
                String srcText = ste.getSrcText();
                String locText = ste.getTranslation();
                if (   searchString(srcText, m_searchText, m_regexSearch)
                    || searchString(locText, m_searchText, m_regexSearch))
                {
                    // found a match - relay source and trans text
                    foundString(i, null, srcText, locText);
                    if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                    {
                        break;
                    }
                }
            }
            if (m_tmSearch)
            {
                ArrayList tmList = CommandThread.core.getTransMemory();
                TransMemory tm;
                for (i=0; i<tmList.size(); i++)
                {
                    tm = (TransMemory) tmList.get(i);
                    String srcText = tm.source;
                    String locText = tm.target;
                    if (   searchString(srcText, m_searchText, m_regexSearch)
                        || searchString(locText, m_searchText, m_regexSearch))
                    {
                        // found a match - relay source and trans text
                        foundString(-1, tm.file, srcText, locText);
                        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                        {
                            break;
                        }
                    }
                }
            }
        }
        else if( m_keywordSearch )
        {
            ArrayList searchTokens = new ArrayList();
            StaticUtils.tokenizeText(m_searchText, searchTokens);
            int i;
            for (i=0; i<CommandThread.core.numEntries(); i++)
            {
                SourceTextEntry ste = CommandThread.core.getSTE(i);
                List srcTokens = ste.getStrEntry().getSrcTokenList();
                List transTokens = ste.getStrEntry().getTransTokenList();
                
                if( StaticUtils.isSubset(searchTokens, srcTokens) ||
                        StaticUtils.isSubset(searchTokens, transTokens) )
                {
                    // found a match - relay source and trans text
                    foundString(i, null, ste.getStrEntry().getSrcText(),
                            ste.getStrEntry().getTranslation());
                    if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                        break;
                }
            }
            if (m_tmSearch)
            {
                ArrayList tmList = CommandThread.core.getTransMemory();
                TransMemory tm;
                for (i=0; i<tmList.size(); i++)
                {
                    tm = (TransMemory) tmList.get(i);
                    List srcTokens = new ArrayList();
                    StaticUtils.tokenizeText(tm.source, srcTokens);
                    List transTokens = new ArrayList();
                    StaticUtils.tokenizeText(tm.target, transTokens);
                    
                    if( StaticUtils.isSubset(searchTokens, srcTokens) ||
                            StaticUtils.isSubset(searchTokens, transTokens) )
                    {
                        // found a match - relay source and trans text
                        foundString(-1, tm.file, tm.source, tm.target);
                        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void searchFiles() throws IOException, TranslationException
    {
        int i;
        int j;
        
        ArrayList fileList = new ArrayList(256);
        if (!m_searchDir.endsWith(File.separator))
            m_searchDir += File.separator;
        StaticUtils.buildFileList(fileList, new File(m_searchDir), m_searchRecursive);
        
        FilterMaster fm = FilterMaster.getInstance();
        Set processedFiles = new HashSet();
        
        for (i=0; i<fileList.size(); i++)
        {
            String filename = (String) fileList.get(i);
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            
            // determine actual file name w/ no root path info
            m_curFileName = filename.substring(m_searchDir.length());
            
            // don't bother to tell handler what we're looking for -
            //	the search data is already known here (and the
            //	handler is in the same thread, so info is not volatile)
            fm.searchFile(filename, this, processedFiles);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////
    // search algorithm
    
    // looks for the search string in the text string
    private boolean searchString(String text, String search) {
        return searchString(text, search, false);
    }
    
    // looks for the search string in the text string
    // if regex is true, the search string is expected to contain a regular expression
    private boolean searchString(String text, String search, boolean regex) {
        if (text == null || search == null)
            return false;

        // escape the search string, if it's not supposed to be a regular expression
        if (!regex)
            search = StaticUtils.escapeNonRegex(search);

        // do the search
        Matcher matcher = Pattern.compile(search).matcher(text);
        return matcher.find();
    }

    /////////////////////////////////////////////////////////////////
    // interface used by FileHandlers
    
    public void searchText(String seg)
    {
        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
        {
            return;
        }
        
        if (searchString(seg, m_searchText))
        {
            // found a match - do something about it
            foundString(-1, m_curFileName, seg, null);
        }
    }
    
    private SearchWindow m_window;
    private boolean		 m_searching;
    private String		 m_searchText;
    private String		 m_searchDir;
    private boolean		 m_searchRecursive;
    private String		 m_curFileName;
    private boolean		 m_exactSearch;
    private boolean		 m_regexSearch;
    private boolean		 m_tmSearch;
    private boolean      m_keywordSearch;
    private HashSet      m_entrySet; // HP: keeps track of previous results, to avoid duplicate entries
    
    private int			m_numFinds;
    
    private ArrayList		m_extList;
    private ArrayList		m_extMapList;
}

