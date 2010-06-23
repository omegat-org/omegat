/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Alex Buloichik
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
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
 * @author Martin Fleurke
 * @author Antonio Vilei
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SearchThread extends Thread
{
    public SearchThread(SearchWindow window)
    {
        m_window = window;
        m_searchDir = null;
        m_searchRecursive = false;
        m_searching = false;
        m_tmSearch = false;
        m_entrySet = null; // HP
                
        m_numFinds = 0;
        m_curFileName = "";	
    }
    
    /////////////////////////////////////////////////////////
    // public interface
    
    /**
     * Starts a search if another is not currently running.
     * To search current project only, set rootDir to null.
     *
     * @param text string to search for
     * @param rootDir folder to search in
     * @param recursive search in subfolders of rootDir too
     * @param exact search for a substring, including wildcards (*?)
     * @param keyword search for keywords, including wildcards (*?)
     * @param regex search based on regular expressions
     * @param caseSensitive search case sensitive
     * @param tm search in legacy and orphan TM strings too
     * @param allResults
     * @param searchSource search in source text
     * @param searchTarget search in target text
     * @param searchAuthor search for tmx segments modified by author id/name
     * @param author string to search for in TMX attribute modificationId
     * @param searchDateAfter search for translation segments modified after the given date
     * @param dateAfter the date after which the modification date has to be
     * @param searchDateBefore search for translation segments modified before the given date
     * @param dateBefore the date before which the modification date has to be
     * @internal The main loop (in the run method) waits for the variable 
     *           m_searching to be set to true. This variable is set to true
     *           in this function on successful setting of the search parameters.
     */
    public void requestSearch(String  text,
                              String  rootDir,
                              boolean recursive,
                              boolean exact,
                              boolean keyword,
                              boolean regex,
                              boolean caseSensitive,
                              boolean tm,
                              boolean allResults,
                              boolean searchSource,
                              boolean searchTarget,
                              boolean searchAuthor,
                              String  author,
                              boolean searchDateAfter,
                              long    dateAfter,
                              boolean searchDateBefore,
                              long    dateBefore
                              )
    {
        if (!m_searching)
        {
            m_searchDir = rootDir;
            m_searchRecursive = recursive;
            m_tmSearch = tm;
            m_allResults = allResults;
            m_searchSource = searchSource;
            m_searchTarget = searchTarget;
            m_searchAuthor = searchAuthor;
            m_searchDateAfter = searchDateAfter;
            m_searchDateBefore = searchDateBefore;

            m_entrySet = new HashSet<String>(); // HP

            // create a list of matchers
            m_matchers = new ArrayList<Matcher>();

            // determine pattern matching flags
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE;

            // if exact search, just use the entire search string as a single
            // search string; otherwise, if keyword, break up the string into
            // separate words (= multiple search strings)
            try {
                if (exact) {
                    // escape the search string, it's not supposed to be a regular expression
                    text = StaticUtils.escapeNonRegex(text, false);

                    // create a matcher for the search string
                    m_matchers.add(Pattern.compile(text, flags).matcher(""));
                } else if (regex) {
                    // create a matcher for the search string
                    m_matchers.add(Pattern.compile(text, flags).matcher(""));
                } else {
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
                // create a matcher for the author search string
                if (!regex)
                    author = StaticUtils.escapeNonRegex(author, false);
                
                m_author = Pattern.compile(author, flags).matcher("");
            } catch (PatternSyntaxException e) {
                // bad regexp input
                // alert user to badness
                m_window.displayErrorRB(e, "ST_REGEXP_ERROR");
                m_window.setSearchControlFocus();
            }
            m_dateBefore=dateBefore;
            m_dateAfter = dateAfter;

            m_searching = true;
        }
    }
    
    ///////////////////////////////////////////////////////////
    // thread main loop
    @Override
    public void run()
    {
        boolean firstPass = true;

        setPriority(Thread.MIN_PRIORITY);

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
                        // searching current project only
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
     * Removes duplicate segments (by Henry Pijffers)
     * except if m_allResults = true
     */
    private void foundString(int entryNum, String intro, String src,
            String target, Match[] srcMatches, Match[] targetMatches) {
        if (m_numFinds++ > OConsts.ST_MAX_SEARCH_RESULTS)
        {
            return;
        }

        if (entryNum >= 0) {
            if (!m_entrySet.contains(src + target) || m_allResults) {
                // HP, duplicate entry prevention
                // entries are referenced at offset 1 but stored at offset 0
                m_window.addEntry(entryNum + 1, null, (entryNum + 1) + "> ",
                        src, target, srcMatches, targetMatches);
                if (!m_allResults) // If we filter results
                    m_entrySet.add(src + target); // HP
            }
        } else {
            m_window.addEntry(entryNum, intro, null, src, target, srcMatches,
                    targetMatches);
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
            TransEntry te = Core.getProject().getTranslation(ste);
            String locText = te != null ? te.translation : "";

            checkEntry(srcText, locText, te, i, null);

            // stop searching if the max. nr of hits has been reached
            if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS) {
                break;
            }
        }

        // search the TM, if requested
        if (m_tmSearch) {
            // search in orphaned
            String file = OStrings.getString("CT_ORPHAN_STRINGS");
            for (Map.Entry<String, TransEntry> en : Core.getProject()
                    .getOrphanedSegments().entrySet()) {
                String srcText = en.getKey();
                TransEntry te = en.getValue();

                checkEntry(srcText, te.translation, te, -1, file);

                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS) {
                    break;
                }
            }
            // Search TM entries, unless we search for date or author.
            // They are not available in external TM, so skip the search in 
            // that case.
            if (!m_searchAuthor && !m_searchDateAfter && !m_searchDateBefore) {
                for (Map.Entry<String, List<TransMemory>> tmEn : Core.getProject()
                    .getTransMemories().entrySet()) {
                    file = tmEn.getKey();
                    for (TransMemory tm : tmEn.getValue()) {

                        checkEntry(tm.source, tm.target, null, -1, file);
                        // stop searching if the max. nr of hits has been reached
                        if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS) {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if specified entry should be found.
     * 
     * @param srcText
     *            source text
     * @param locText
     *            translation text
     * @param entry
     *            entry
     * @param entryNum
     *            entry number
     * @param intro
     *            file
     */
    protected void checkEntry(String srcText, String locText, TransEntry entry,
            int entryNum, String intro) {
        Match[] srcMatches = null;
        if (m_searchSource) {
            if (searchString(srcText)) {
                srcMatches = foundMatches
                        .toArray(new Match[foundMatches.size()]);
            }
        }
        Match[] targetMatches = null;
        if (m_searchTarget) {
            if (searchString(locText)) {
                targetMatches = foundMatches.toArray(new Match[foundMatches
                        .size()]);
            }
        }

        if (srcMatches == null && targetMatches == null) {
            return;
        }

        if (entry != null) {
            if (m_searchAuthor && !searchAuthor(entry)) {
                return;
            }
            if (m_searchDateBefore && entry.changeDate != 0
                    && entry.changeDate > m_dateBefore) {
                return;
            }
            if (m_searchDateAfter && entry.changeDate != 0
                    && entry.changeDate < m_dateAfter) {
                return;
            }
        }

        // found
        foundString(entryNum, intro, srcText, locText, srcMatches, targetMatches);
    }

    private void searchFiles() throws IOException, TranslationException
    {
        List<String> fileList = new ArrayList<String>(256);
        if (!m_searchDir.endsWith(File.separator))
            m_searchDir += File.separator;
        StaticUtils.buildFileList(fileList, new File(m_searchDir), m_searchRecursive);
        
        FilterMaster fm = FilterMaster.getInstance();
        
        for (String filename :  fileList) {
            // determine actual file name w/ no root path info
            m_curFileName = filename.substring(m_searchDir.length());
            
            // don't bother to tell handler what we're looking for -
            //	the search data is already known here (and the
            //	handler is in the same thread, so info is not volatile)
            fm.loadFile(filename, new SearchCallback(Core
                    .getProject().getProjectProperties()) {
                protected void addSegment(String id, short segmentIndex,
                        String segmentSource, String segmentTranslation,
                        String comment) {
                    searchText(segmentSource);
                }

                public void addFileTMXEntry(String source, String translation) {
                }
            });
        }
    }

    protected abstract class SearchCallback extends ParseEntry implements
            IParseCallback {
        public SearchCallback(ProjectProperties config) {
            super(config);
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

        foundMatches.clear();
        // check the text against all matchers
        for (Matcher matcher : m_matchers) {
            // check the text against the current matcher
            // if one of the search strings is not found, don't
            // bother looking for the rest of the search strings
            matcher.reset(text);
            if (!matcher.find())
                return false;

            while (true) {
                foundMatches.add(new Match(matcher.start(), matcher.end()
                        - matcher.start()));
                int pos = matcher.start();
                if (!matcher.find(pos + 1)) {
                    break;
                }
            }
        }

        // if we arrive here, all search strings have been matched,
        // so this is a hit
        
        return true;
    }
    
    /**
     * Looks for an occurrence of the author search string in the supplied text string.
     *
     * @param author The text string to search in
     *
     * @return True if the text string contains the search string
     */
   private boolean searchAuthor(TransEntry te) {
       if (te == null || m_author == null )
           return false;
       String author = te.changeId;
       if (author == null) return false;

       // check the text against the author matcher
       m_author.reset(author);
       if (!m_author.find()) return false;

       // if we arrive here, the search string has been matched,
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

        if (searchString(seg)) {
            Match[] matches=foundMatches.toArray(new Match[foundMatches.size()]);
            // found a match - do something about it
            foundString(-1, m_curFileName, seg, null,matches,null);
        }
    }

    private SearchWindow m_window;
    private boolean   m_searching;
    private String    m_searchDir;
    private boolean   m_searchRecursive;
    private String    m_curFileName;
    private boolean   m_tmSearch;
    private boolean   m_allResults;
    private boolean   m_searchSource;
    private boolean   m_searchTarget;
    private boolean   m_searchAuthor;
    private boolean   m_searchDateAfter;
    private boolean   m_searchDateBefore;
    private Set<String>   m_entrySet; // HP: keeps track of previous results, to avoid duplicate entries
    private List<Matcher> m_matchers; // HP: contains a matcher for each search string
                                  //     (multiple if keyword search)
    private Matcher   m_author;
    private long m_dateBefore;
    private long m_dateAfter;

    private int m_numFinds;
    
    private final List<Match> foundMatches=new ArrayList<Match>();
    
    /**
     * Class for store info about matching position.
     */
    public static class Match {
        public int start, length;

        public Match(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }
}

