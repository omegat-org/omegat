/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2010 Alex Buloichik
               2011 Alex Buloichik, Didier Briel
               2012 Guido Leenders, Didier Briel
               2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
               2016 Didier Briel
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.List;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
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
        /** title case for whole string */
        SENTENCE,
        /** title case for each token in string */
        TITLE,
        /** upper case */
        UPPER,
        /** cycle between cases */
        CYCLE,
    }

    /**
     * Storage for caret position and selection.
     */
    class CaretPosition {
        Integer position;
        Integer selectionStart, selectionEnd;

        public CaretPosition(int position) {
            this.position = position;
            this.selectionStart = null;
            this.selectionEnd = null;
        }

        public CaretPosition(int selectionStart, int selectionEnd) {
            this.position = null;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        /**
         * We can't define it once since 'position' can be changed later.
         */
        public static CaretPosition startOfEntry() {
            return new CaretPosition(0);
        }
    }

    /**
     * Get relative path (under <code>source</code>) of the source file
     * currently open in the editor.
     * <p>
     * Can be called from any thread.
     */
    String getCurrentFile();

    /**
     * Get the relative path (under <code>target</code>) of the target file
     * corresponding to the current source file (per {@link #getCurrentFile()}).
     * This file is what is created upon doing Create Target Documents, but it
     * may not exist yet.
     * <p>
     * Can be called from any thread.
     */
    String getCurrentTargetFile();

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
     * Move to the first non-unique entry.
     * Must be called from UI thread.
     */
    void nextUniqueEntry();

    /**
     * Goto first entry in specified file.
     *
     * @param fileIndex
     *            file index in project
     * @throws IndexOutOfBoundsException
     *             If there is no file for the given index
     */
    void gotoFile(int fileIndex) throws IndexOutOfBoundsException;

    /**
     * Goto entry with specified number. Convenience method for
     * {@link #gotoEntry(int, CaretPosition)} where the caret position will be
     * the start of the entry.
     *
     * @param entryNum
     *            entry number, starts from 1
     *
     *            Must be called only from UI thread.
     */
    void gotoEntry(int entryNum);

    /**
     * Goto entry with specified number, and restore caret to specified
     * position.
     *
     * @param entryNum
     *            entry number, starts from 1
     *
     *            Must be called only from UI thread.
     */
    void gotoEntry(int entryNum, CaretPosition pos);

    /**
     * Goto entry based on a string and entry key.
     *
     * @param srcString
     *            entry source string
     * @param key
     *            entry key (can be null)
     *
     *            Must be called only from UI thread.
     */
    void gotoEntry(String srcString, EntryKey key);

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

    void refreshView(boolean doCommit);

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
     * Replaces the entire edit area with a given text which origin is origin.
     * <p>
     *     when manual edit and origin is unknown, origin will be null.
     *
     * Must be called only from UI thread.
     */
    void replaceEditText(String text, String origin);

    /**
     * Replaces the entire edit area with a given text.
     *
     * Must be called only from UI thread.
     */
    void replaceEditText(String text);

    /**
     * Replace text and mark as to be changed by the translator from origin,
     * i.e, background of segment should be marked
     *
     * Must be called only from UI thread.
     */
    void replaceEditTextAndMark(String text, String origin);

    /**
     * Inserts text at the cursor position and mark as to be changed
     * by the translator, i.e, background of segment should be marked
     *
     * Must be called only from UI thread.
     */
    void replaceEditTextAndMark(String text);

    /**
     * Inserts text at the cursor position.
     *
     * Must be called only from UI thread.
     */
    void insertText(String text);

    /**
     * Inserts text at the cursor position and mark as to be changed
     * by the translator, i.e, background of segment should be marked
     *
     * Must be called only from UI thread.
     * @param text The text to insert
     */
    void insertTextAndMark(String text);

    /**
     * Inserts tag at the cursor position, probably with adding bidi control chars.
     *
     * Must be called only from UI thread.
     */
    void insertTag(String tag);

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
    IEditorSettings getSettings();

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
     * Gets a filter for this editor, or null if filter doesn't applied.
     */
    IEditorFilter getFilter();

    /**
     * Sets a filter to this editor. The filter causes only the selected entries to be shown in the editor.
     *
     * @param filter
     *            Filter instance
     */
    void setFilter(IEditorFilter filter);

    /**
     * Removes the current filter.
     */
    void removeFilter();

    /**
     * Returns current translation or null.
     */
    String getCurrentTranslation();

    /**
     * Perform any necessary actions for window deactivation.
     */
    void windowDeactivated();

    /**
     * Register untranslated.
     */
    void registerUntranslated();

    /**
     * Register as empty.
     */
    void registerEmptyTranslation();

    /**
     * Register translation to be identical to source.
     */
    void registerIdenticalTranslation();

    /**
     * Access the AutoCompleter
     */
    IAutoCompleter getAutoCompleter();
}
