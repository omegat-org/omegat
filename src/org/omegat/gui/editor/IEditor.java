/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2010 Alex Buloichik
               2011 Alex Buloichik, Didier Briel
               2012 Guido Leenders, Didier Briel
               2013 Zoltan Bartko, Aaron Madlon-Kay
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
 * @author Didier Briel
 * @author Guido Leenders
 * @author Aaron Madlon-Kay
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

    int CURSOR_ON_START_OF_ENTRY = -1;
    int CURSOR_ON_END_OF_ENTRY = -2;
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
     *
     * Will position cursor at the start of segment
     */
    void activateEntry();

    /**
     * Activate entry for edit.
     * 
     * Must be called only from UI thread.
     */
    void activateEntry(int preferredPosition);

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
     * Move to next entry with a note.
     * 
     * Must be called only from UI thread.
     */
    void nextEntryWithNote();

    /**
     * Move to previous entry with a note.
     * 
     * Must be called only from UI thread.
     */
    void prevEntryWithNote();

    /**
     * Move to next untranslated entry.
     * 
     * Must be called only from UI thread.
     */
    void nextUntranslatedEntry();

    /**
     * Move to next translated entry.
     * 
     * Must be called only from UI thread.
     */
    void nextTranslatedEntry();
    
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
     * Goto entry with specified number while avoiding clobbering the tag fixes.
     * 
     * @param fixedEntry
     *            entry number, starts from 1
     * @param fixedSource
     *            The source of the entry that was fixed
     * 
     *            Must be called only from UI thread.
     */
    void gotoEntryAfterFix(int fixedEntry, String fixedSource);

    /**
     * Refresh the current editor view while avoiding clobbering any tag fixes.
     * 
     * @param fixedEntries
     *            A list of all entries that were altered
     * 
     *            Must be called only from UI thread.
     */
    void refreshViewAfterFix(List<Integer> fixedEntries);

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
     * Replaces the entire edit area with a given text and mark for to be
     * changed by translator, i.e. background of segment should be marked
     * 
     * Must be called only from UI thread.
     */
    void replaceEditTextAndMark(String text);

    /**
     * Defines the current translation as empty.
     * This is reset after each going out of a segment.
     */
    void setEmptyTranslation(boolean flag);

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
     * Set default/alternate translation for current entry. 
     */
    void setAlternateTranslationForCurrentEntry(boolean alternate);

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
    void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks, String markerClassName);

    /**
     * Register constructor of popup menu.
     * 
     * @param priority
     *            priority of process order
     * @param constructor
     *            constructor instance
     */
    void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor);

    /**
     * Calls specified marker for reprocess all entries.
     */
    void remarkOneMarker(String markerClassName);

    /**
     * Adds a filter to this editor. The filter causes only the selected entries
     * to be shown in the editor.
     * 
     * @param entryList
     *            List of project-wide entry numbers
     */
    void addFilter(List<Integer> entryList);

    /**
     * Removes the current filter.
     */
    void removeFilter();

    /**
     * Returns current translation or null.
     */
    String getCurrentTranslation();
}
