/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
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

package org.omegat.core.threads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.search.SearchWindow;
import org.omegat.util.Log;
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
 * @author Didier Briel
 */
public class SearchThread extends Thread
{
    public SearchThread(MainWindow par, SearchWindow window, String startText)
    {
        setPriority(Thread.MIN_PRIORITY);
        
        m_window = window;
        m_searchDir = null;
        m_searchRecursive = false;
        m_searching = false;
        m_tmSearch = false;
        m_entrySet = null; // HP
                
        m_numFinds = 0;
        m_curFileName = "";	// NOI18N
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
     * @param keyword search for keywords
     * @param caseSensitive search case sensitive
     * @param regex enable regular expressions, otherwise just use wildcards (*?)
     * @param tm search in legacy and orphan TM strings too
     */
    public void requestSearch(String  text,
                              String  rootDir,
                              boolean recursive,
                              boolean exact,
                              boolean keyword,
                              boolean caseSensitive,
                              boolean regex,
                              boolean tm,
                              boolean allResults)
    {
        if (!m_searching)
        {
            m_searchDir = rootDir;
            m_searchRecursive = recursive;
            m_tmSearch = tm;
            m_allResults = allResults;
            m_searching = true;
            m_entrySet = new HashSet<String>(); // HP

            // create a list of matchers
            m_matchers = new ArrayList<Matcher>();

            // determine pattern matching flags
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE;

            // if exact search, just use the entire search string as a single
            // search string; otherwise, if keyword, break up the string into
            // separate words (= multiple search strings)
            if (exact) {
                // escape the search string, if it's not supposed to be a regular expression
                if (!regex)
                    text = StaticUtils.escapeNonRegex(text, false);

                // create a matcher for the search string
                m_matchers.add(Pattern.compile(text, flags).matcher(""));
            }
            else {
                // break the search string into keywords,
                // each of which is a separate search string
                text = text.trim();
                if (text.length() > 0) {
                    int wordStart = 0;
                    while (wordStart < text.length()) {
                        // get the location of the next space
                        int spacePos = text.indexOf(' ', wordStart);

                        // get the next word
                        String word = (spacePos == -1) // last word reached
                                          ? text.substring(wordStart, text.length()).trim()
                                          : text.substring(wordStart, spacePos).trim();

                        if (word.length() > 0) {
                            // escape the word, if it's not supposed to be a regular expression
                            if (!regex)
                                word = StaticUtils.escapeNonRegex(word, false);

                            // create a matcher for the word
                            m_matchers.add(Pattern.compile(word, flags).matcher(""));
                        }

                        // set the position for the start of the next word
                        wordStart = (spacePos == -1) ? text.length() : spacePos + 1;
                    }
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////
    // thread main loop
    public void run()
    {
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
                            Log.logErrorRB(e, "ST_FILE_SEARCH_ERROR");
                            Core.getMainWindow().displayErrorRB(e, "ST_FILE_SEARCH_ERROR");
                            
                        }
                        catch (TranslationException te)
                        {
                            // something bad happened
                            // alert user to badness
                            Log.logErrorRB(te, "ST_FILE_SEARCH_ERROR");
                            Core.getMainWindow().displayErrorRB(te, "ST_FILE_SEARCH_ERROR");
                        }
                    }
                    
                    // whatever the states is, error or not, display what's
                    //	been found so far
                    if (m_numFinds == 0)
                    {
                        // no match
                        m_window.postMessage(OStrings.getString("ST_NOTHING_FOUND"));
                    }
                    m_window.displayResults();
                    m_searching = false;
                    m_entrySet = null; // HP
                }
            }
        }
        catch (RuntimeException re)
        {
            Log.logErrorRB(re, "ST_FATAL_ERROR");
            Core.getMainWindow().displayErrorRB(re, "ST_FATAL_ERROR");
            m_window.threadDied();
        }
    }
    
    //////////////////////////////////////////////////////////////
    // internal functions
    
    /**
     * Queue found string.
     * Since 1.6.0 RC9 removes duplicate segments (by Henry Pijffers)
     */
    private void foundString(int entryNum, String intro, String src, String target)
    {
        if (m_numFinds++ > OConsts.ST_MAX_SEARCH_RESULTS)
        {
            return;
        }

        if (entryNum >= 0)
        {
           if (!m_entrySet.contains(src + target) || m_allResults) { // HP, duplicate entry prevention
                // entries are referenced at offset 1 but stored at offset 0
                m_window.addEntry(entryNum+1, null, (entryNum+1)+"> "+src, target);	// NOI18N
                if (!m_allResults) // If we filter results
                    m_entrySet.add(src + target); // HP
           }
        }
        else
        {
            m_window.addEntry(entryNum, intro, src, target);
        }

        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
        {
            m_window.postMessage(StaticUtils.format(
                OStrings.getString("SW_MAX_FINDS_REACHED"),
                new Object[] {new Integer(OConsts.ST_MAX_SEARCH_RESULTS)}));
        }
    }

    private void searchProject()
    {
        IProject project = Core.getProject();
        // reset the number of search hits
        m_numFinds = 0;

        // search through all project entries
        IProject dataEngine = Core.getProject();
        for (int i = 0; i < project.getAllEntries().size(); i++) {
            // get the source and translation of the next entry
            SourceTextEntry ste = dataEngine.getAllEntries().get(i);
            String srcText = ste.getSrcText();
            String locText = ste.getTranslation();

            // if the source or translation contain all
            // search strings, report the hit
            if (   searchString(srcText)
                || searchString(locText))
                foundString(i, null, srcText, locText);

            // stop searching if the max. nr of hits has been reached
            if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                break;
        }

        // search the TM, if requested
        if (m_tmSearch) {
            // search all TM entries
            for (TransMemory tm : Core.getProject().getTransMemory()) {
                String srcText = tm.source;
                String locText = tm.target;

                // if the source or translation contain all
                // search strings, report the hit
                if (   searchString(srcText)
                    || searchString(locText))
                    foundString(-1, tm.file, srcText, locText);

                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
                    break;
            }
        }
    }

    private void searchFiles() throws IOException, TranslationException
    {
        List<String> fileList = new ArrayList<String>(256);
        if (!m_searchDir.endsWith(File.separator))
            m_searchDir += File.separator;
        StaticUtils.buildFileList(fileList, new File(m_searchDir), m_searchRecursive);
        
        FilterMaster fm = FilterMaster.getInstance();
        Set<File> processedFiles = new HashSet<File>();
        
        for (String filename :  fileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            
            // determine actual file name w/ no root path info
            m_curFileName = filename.substring(m_searchDir.length());
            
            // don't bother to tell handler what we're looking for -
            //	the search data is already known here (and the
            //	handler is in the same thread, so info is not volatile)
            fm.loadFile(filename, processedFiles, new ParseEntry(Core
                    .getProject().getProjectProperties()) {                
                protected String processSingleEntry(String src) {
                    searchText(src);
                    return src;
                }
            });
        }
    }
    
    ///////////////////////////////////////////////////////////////////////
    // search algorithm
    
    /**
      * Looks for an occurrence of the search string(s) in the supplied text string.
      *
      * @param text   The text string to search in
      *
      * @return True if the text string contains all search strings
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private boolean searchString(String text) {
        if (text == null || m_matchers == null || m_matchers.isEmpty())
            return false;

        // check the text against all matchers
        for (Matcher matcher : m_matchers) {
            // check the text against the current matcher
            // if one of the search strings is not found, don't
            // bother looking for the rest of the search strings
            matcher.reset(text);
            if (!matcher.find())
                return false;
        }

        // if we arrive here, all search strings have been matched,
        // so this is a hit
        return true;
    }

    /////////////////////////////////////////////////////////////////
    // interface used by FileHandlers
    
    public void searchText(String seg)
    {
        // don't look further if the max. nr of hits has been reached
        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
            return;

        if (searchString(seg))
            // found a match - do something about it
            foundString(-1, m_curFileName, seg, null);
    }

    private SearchWindow m_window;
    private boolean   m_searching;
    private String    m_searchDir;
    private boolean   m_searchRecursive;
    private String    m_curFileName;
    private boolean   m_tmSearch;
    private boolean   m_allResults;
    private Set<String>   m_entrySet; // HP: keeps track of previous results, to avoid duplicate entries
    private List<Matcher> m_matchers; // HP: contains a matcher for each search string
                                  //     (multiple if keyword search)

    private int m_numFinds;
}

