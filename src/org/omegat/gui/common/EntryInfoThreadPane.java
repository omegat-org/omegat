/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.common;

import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;

/**
 * Base class for show information about currently selected entry, searched by separate thread. It can be used
 * for glossaries, dictionaries and other panes.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @param <T>
 *            result type of found data
 */
@SuppressWarnings("serial")
public abstract class EntryInfoThreadPane<T> extends EntryInfoPane<T> implements IEntryEventListener {

    SourceTextEntry currentlyProcessedEntry;

    public EntryInfoThreadPane(final boolean useApplicationFont) {
        super(useApplicationFont);
        CoreEvents.registerEntryEventListener(this);
    }

    public void onNewFile(String activeFileName) {
        currentlyProcessedEntry = null;
    }

    public void onEntryActivated(SourceTextEntry newEntry) {
        currentlyProcessedEntry = newEntry;
        startSearchThread(newEntry);
    }

    public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            currentlyProcessedEntry = null;
            onProjectOpen();
            break;
        case CLOSE:
            currentlyProcessedEntry = null;
            onProjectClose();
            break;
        }
    }

    /**
     * Each implementation should start own EntryInfoSearchThread thread.
     * 
     * @param newEntry
     *            new entry for find
     */
    protected abstract void startSearchThread(final SourceTextEntry newEntry);

    /**
     * Callback from search thread.
     * 
     * @param processedEntry
     *            entry which produce data
     * @param data
     *            found data
     */
    protected abstract void setFoundResult(SourceTextEntry processedEntry, T data);

    /**
     * Callback from search thread if error occured.
     * 
     * @param ex
     */
    protected void setError(Exception ex) {
    }
}
