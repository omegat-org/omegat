/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Didier Briel
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

import java.io.IOException;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.omegat.core.Core;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.core.search.Searcher;
import org.omegat.filters2.TranslationException;
import org.omegat.gui.search.SearchWindow;
import org.omegat.util.Log;

/**
 * Each search window has its own search thread to actually do the searching.
 * This prevents lockup of the UI during intensive searches
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 */
public class SearchThread extends Thread implements Searcher.ISearchCheckStop {
    /**
     * Starts a new search. To search current project only, set rootDir to null.
     * 
     * @param window
     *            search window for display results
     * @param text
     *            string to search for
     * @param rootDir
     *            folder to search in
     * @param recursive
     *            search in subfolders of rootDir too
     * @param exact
     *            search for a substring, including wildcards (*?)
     * @param keyword
     *            search for keywords, including wildcards (*?)
     * @param regex
     *            search based on regular expressions
     * @param caseSensitive
     *            search case sensitive
     * @param tm
     *            search in legacy and orphan TM strings too
     * @param allResults
     *            include duplicate results
     * @param searchSource
     *            search in source text
     * @param searchTarget
     *            search in target text
     * @param searchAuthor
     *            search for tmx segments modified by author id/name
     * @param author
     *            string to search for in TMX attribute modificationId
     * @param searchDateAfter
     *            search for translation segments modified after the given date
     * @param dateAfter
     *            the date after which the modification date has to be
     * @param searchDateBefore
     *            search for translation segments modified before the given date
     * @param dateBefore
     *            the date before which the modification date has to be
     * @param  numberOfResults
     *            the maximum number of results returned by the thread
     * @internal The main loop (in the run method) waits for the variable
     *           m_searching to be set to true. This variable is set to true in
     *           this function on successful setting of the search parameters.
     */
    public SearchThread(SearchWindow window, String text, String rootDir,
            boolean recursive, boolean exact, boolean keyword, boolean regex,
            boolean caseSensitive, boolean tm, boolean allResults,
            boolean searchSource, boolean searchTarget, boolean searchAuthor,
            String author, boolean searchDateAfter, long dateAfter,
            boolean searchDateBefore, long dateBefore, int numberOfResults) {
        m_window = window;

        m_searchExpression = new SearchExpression(text, rootDir, recursive,
                exact, keyword, regex, caseSensitive, tm, allResults,
                searchSource, searchTarget, searchAuthor, author,
                searchDateAfter, dateAfter, searchDateBefore, dateBefore,
                numberOfResults);
    }

    // /////////////////////////////////////////////////////////
    // thread main loop
    @Override
    public void run() {
        try {
            try {
                List<SearchResultEntry> resultsList = new Searcher(
                        Core.getProject(), this).getSearchResults(
                        m_searchExpression);
                
                if (stopped) {
                    return;
                }

                m_window.displaySearchResult(resultsList);
            } catch (PatternSyntaxException e) {
                // bad regexp input
                // alert user to badness
                m_window.displayErrorRB(e, "ST_REGEXP_ERROR");
            } catch (IOException e) {
                // something bad happened
                // alert user to badness
                Log.logErrorRB(e, "ST_FILE_SEARCH_ERROR");
                Core.getMainWindow().displayErrorRB(e, "ST_FILE_SEARCH_ERROR");
            } catch (TranslationException te) {
                // something bad happened
                // alert user to badness
                Log.logErrorRB(te, "ST_FILE_SEARCH_ERROR");
                Core.getMainWindow().displayErrorRB(te, "ST_FILE_SEARCH_ERROR");
            }
        } catch (RuntimeException re) {
            Log.logErrorRB(re, "ST_FATAL_ERROR");
            Core.getMainWindow().displayErrorRB(re, "ST_FATAL_ERROR");
        }
    }

    public boolean isStopped() {
        return stopped;
    }
    
    /**
     * Stop search.
     */
    public void fin() {
        stopped = true;
    }

    private boolean stopped;
    private SearchWindow m_window;
    private SearchExpression m_searchExpression;
}
