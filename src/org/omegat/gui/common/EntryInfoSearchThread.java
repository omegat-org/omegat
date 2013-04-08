/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.common;

import javax.swing.SwingUtilities;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Log;

/**
 * Base class for search info about current entry in the separate thread.
 * 
 * Implementation must check isEntryChanged method and exit if antry changed,
 * against create multimple threads when user travels by entries fast.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * 
 * @param <T>
 *            result type of found data
 */
public abstract class EntryInfoSearchThread<T> extends Thread {
    private final EntryInfoThreadPane<T> pane;

    /**
     * Entry which processed currently.
     * 
     * If entry in pane was changed, it means user was moved to other entry, and
     * there is no sense to continue search.
     */
    private final SourceTextEntry currentlyProcessedEntry;

    /**
     * Constructor.
     * 
     * @param pane
     *            entry info pane
     * @param entry
     *            current entry
     */
    public EntryInfoSearchThread(final EntryInfoThreadPane<T> pane, final SourceTextEntry entry) {
        this.pane = pane;
        this.currentlyProcessedEntry = entry;

    }

    /**
     * Check if current entry was changed. If user moved to other entry, there
     * is no sence to continue search.
     * 
     * @return true if current entry was changed
     */
    private boolean isEntryChanged() {
        return currentlyProcessedEntry != pane.currentlyProcessedEntry;
    }
    
    /**
     * Throws exception if entry changed for stop processing.
     */
    protected void checkEntryChanged() throws EntryChangedException {
        if (isEntryChanged()) {
            throw new EntryChangedException();
        }
    }

    @Override
    public void run() {
        if (isEntryChanged()) {
            return;
        }
        T result = null;
        Exception error = null;
        try {
            result = search();
        } catch (EntryChangedException ex) {
            // entry changed - there is no sence to display results
            return;
        } catch (Exception ex) {
            error = ex;
            Log.log(ex);
        }
        if (isEntryChanged()) {
            return;
        }

        final T fresult = result;
        final Exception ferror = error;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (isEntryChanged()) {
                    return;
                }
                if (ferror != null) {
                    pane.setError(ferror);
                } else {
                    pane.setFoundResult(currentlyProcessedEntry, fresult);
                }
            }
        });
    }

    /**
     * Implementation-dependent method for search info.
     * 
     * If entry changed, method can return null.
     * 
     * @return result of search
     */
    protected abstract T search() throws EntryChangedException, Exception;

    /**
     * Any search can generate this exception for stop searching if entry changed. All callers must catch it
     * and just skip.
     */
    @SuppressWarnings("serial")
    public static class EntryChangedException extends RuntimeException {
    }
}
