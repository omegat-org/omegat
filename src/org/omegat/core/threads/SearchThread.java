/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Didier Briel
               2013 Alex Buloichik
               2018 Thomas Cordonnier
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.threads;

import java.util.regex.PatternSyntaxException;

import org.omegat.core.Core;
import org.omegat.core.search.Searcher;
import org.omegat.gui.search.SearchWindowController;
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
 * @author Thomas Cordonnier
 */
public class SearchThread extends LongProcessThread {
    /**
     * Starts a new search. To search a current project only, set rootDir to
     * null.
     * <p>
     * The main loop (in the run method) waits for the variable m_searching to
     * be set to true. This variable is set to true in this function on
     * successful setting of the search parameters.
     *
     * @param window
     *            search window for display results
     * @param searcher
     *            search expression
     */
    public SearchThread(SearchWindowController window, Searcher searcher) {
        this.window = window;
        this.searcher = searcher;
        this.searcher.setThread(this);
    }

    // /////////////////////////////////////////////////////////
    // thread main loop
    @Override
    public void run() {
        try {
            try {
                searcher.search();

                checkInterrupted();

                window.displaySearchResult(searcher);
            } catch (LongProcessInterruptedException ex) {
                // interrupted - nothing to do
            } catch (PatternSyntaxException e) {
                // bad regexp input
                // alert user to badness
                window.displayErrorRB(e, "ST_REGEXP_ERROR");
            } catch (IndexOutOfBoundsException e) {
                // replacement with group which does not exist
                window.displayErrorRB(e, "ST_REGEXP_REPLACE_ERROR");
            } catch (Exception e) {
                // something bad happened
                // alert user to badness
                Log.logErrorRB(e, "ST_FILE_SEARCH_ERROR");
                Core.getMainWindow().displayErrorRB(e, "ST_FILE_SEARCH_ERROR");
            }
        } catch (RuntimeException re) {
            Log.logErrorRB(re, "ST_FATAL_ERROR");
            Core.getMainWindow().displayErrorRB(re, "ST_FATAL_ERROR");
        }
    }

    private SearchWindowController window;
    private Searcher searcher;
}
