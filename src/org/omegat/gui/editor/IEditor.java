/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.gui.editor;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;

/**
 * Interface for access to editor functionality.
 * 
 * Almost all methods must be called from UI thread.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IEditor {
    enum CHANGE_CASE_TO {
        /** lower case */
        LOWER,
        /** title case */
        TITLE,
        /** upper case */
        UPPER,
        /** cycle between cases */
        CYCLE,
    }

    /**
     * Get current file name which opened in editor.
     * 
     * Can be called from any thread.
     */
    String getCurrentFile();

    /**
     * Get current active entry.
     * 
     * Can be called from any thread.
     */
    SourceTextEntry getCurrentEntry();

    /**
     * Get current active entry number.
     * 
     * Can be called from any thread.
     */
    int getCurrentEntryNumber();

    /**
     * Activate entry for edit.
     * 
     * Must be called only from UI thread.
     */
    void activateEntry();

    /**
     * Commits the translation and deactivate entry. Translation will be saved.
     * 
     * Must be called only from UI thread.
     */
    void commitAndDeactivate();

    /**
     * Commits the translation and leave entry activated. Translation will be
     * saved.
     * 
     * Must be called only from UI thread.
     */
    void commitAndLeave();

    /**
     * Move to next entry.
     * 
     * Must be called only from UI thread.
     */
    void nextEntry();

    /**
     * Move to previous entry.
     * 
     * Must be called only from UI thread.
     */
    void prevEntry();

    /**
     * Move to next untranslated entry.
     * 
     * Must be called only from UI thread.
     */
    void nextUntranslatedEntry();

    /**
     * Goto first entry in specified file.
     * 
     * @param fileIndex
     *            file index in project
     */
    void gotoFile(int fileIndex);

    /**
     * Goto entry with specified number.
     * 
     * @param entryNum
     *            entry number, starts from 1
     * 
     *            Must be called only from UI thread.
     */
    void gotoEntry(int entryNum);

    /**
     * Set current focus to editor.
     */
    void requestFocus();

    /**
     * Change case of the selected text or if none is selected, of the current
     * word.
     * 
     * @param newCase
     *            : lower, title, upper or cycle
     * 
     *            Must be called only from UI thread.
     */
    void changeCase(CHANGE_CASE_TO newCase);

    /**
     * Replaces the entire edit area with a given text.
     * 
     * Must be called only from UI thread.
     */
    void replaceEditText(String text);

    /**
     * Inserts text at the cursor position.
     * 
     * Must be called only from UI thread.
     */
    void insertText(String text);

    /**
     * Go to next segment from history.
     * 
     * Must be called only from UI thread.
     */
    void gotoHistoryForward();

    /**
     * Go to previous segment from history.
     * 
     * Must be called only from UI thread.
     */
    void gotoHistoryBack();

    /**
     * Get settings instance.
     * 
     * @return interface for read and change editor settings
     * 
     *         Can be called from any thread.
     */
    EditorSettings getSettings();

    /**
     * Undo editing.
     * 
     * Must be called only from UI thread.
     */
    void undo();

    /**
     * Redo editing.
     * 
     * Must be called only from UI thread.
     */
    void redo();

    /**
     * Get currently selected text.
     * 
     * @return selected text
     * 
     *         Must be called only from UI thread.
     */
    String getSelectedText();

    /**
     * All plugins can call this method for mark something in active entry.
     * 
     * @param requiredActiveEntry
     *            entry which should be active. If user moved to other entry,
     *            then marks will be skipped
     * @param marks
     *            list of marks
     * @param markerClassName
     *            marker's class name
     */
    void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
            String markerClassName);

    /**
     * Register constructor of popup menu.
     * 
     * @param priority
     *            priority of process order
     * @param constructor
     *            constructor instance
     */
    void registerPopupMenuConstructors(int priority,
            IPopupMenuConstructor constructor);
    
    /**
     * Calls specified marker for reprocess all entries.
     */
    void remarkOneMarker(String markerClassName);
}
