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
