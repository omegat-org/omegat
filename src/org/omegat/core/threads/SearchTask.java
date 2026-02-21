/*
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Hiroshi Miura
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
 */
package org.omegat.core.threads;

import java.util.Objects;
import java.util.regex.PatternSyntaxException;

import org.omegat.core.Core;
import org.omegat.gui.search.SearchWindowController;
import org.omegat.core.search.Searcher;
import org.omegat.util.Log;

/**
 * Modern replacement for SearchThread.
 * Executed by {@link LongProcessExecutor}.
 */
public final class SearchTask {

    private final SearchWindowController window;
    private final Searcher searcher;

    public SearchTask(SearchWindowController window, Searcher searcher) {
        this.window = Objects.requireNonNull(window, "window");
        this.searcher = Objects.requireNonNull(searcher, "searcher");
    }

    /**
     * Executes the search. Returns no value; completion is signaled via the returned future.
     */
    public Void run(CancellationToken token) {
        try {
            try {
                searcher.setCancellationToken(token);
                token.throwIfCancelled();

                searcher.search();

                token.throwIfCancelled();

                window.displaySearchResult(searcher);
                return null;

            } catch (LongProcessInterruptedException ex) {
                // cancelled: do nothing (caller can observe via completion future)
                return null;

            } catch (PatternSyntaxException e) {
                window.displayErrorRB(e, "ST_REGEXP_ERROR");
                return null;

            } catch (IndexOutOfBoundsException e) {
                window.displayErrorRB(e, "ST_REGEXP_REPLACE_ERROR");
                return null;

            } catch (Exception e) {
                Log.logErrorRB(e, "ST_FILE_SEARCH_ERROR");
                Core.getMainWindow().displayErrorRB(e, "ST_FILE_SEARCH_ERROR");
                return null;
            }
        } catch (RuntimeException re) {
            Log.logErrorRB(re, "ST_FATAL_ERROR");
            Core.getMainWindow().displayErrorRB(re, "ST_FATAL_ERROR");
            throw re;
        }
    }
}
