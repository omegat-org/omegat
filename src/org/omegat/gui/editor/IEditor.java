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

import org.omegat.core.matching.SourceTextEntry;

/**
 * Interface for access to editor functionality.
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
     * Show introduction text.
     */
    void showIntoduction();

    /**
     * Get current file name which opened in editor.
     */
    String getCurrentFile();

    /**
     * Get current active entry.
     */
    SourceTextEntry getCurrentEntry();

    /**
     * Displays all segments in current document.
     */
    void loadDocument();

    /**
     * Activate entry for edit.
     */
    void activateEntry();

    /**
     * Commits the translation. Translation will be saved.
     */
    void commitEntry();

    /**
     * Commits the translation.
     * 
     * @param forceCommit
     *                If false, the translation will not be saved
     */
    void commitEntry(boolean forceCommit);

    /**
     * Move to next entry.
     */
    void nextEntry();

    /**
     * Move to previous entry.
     */
    void prevEntry();

    /**
     * Move to next untranslated entry.
     */
    void nextUntranslatedEntry();

    /**
     * Goto entry with specified number.
     * 
     * @param entryNum
     *                entry number
     */
    void gotoEntry(int entryNum);

    /**
     * TODO: change it to setup first entry on the 'onProjectLoaded' event
     */
    void setFirstEntry();

    /**
     * Change case of the selected text or if none is selected, of the current
     * word.
     * 
     * @param newCase :
     *                lower, title, upper or cycle
     */
    void changeCase(CHANGE_CASE_TO newCase);

    /**
     * Checks whether the selection & caret is inside editable text, and changes
     * their positions accordingly if not.
     */
    void checkCaret();

    /**
     * Make sure there's one character in the direction indicated for delete
     * operation.
     * 
     * @param forward
     * @return true if space is available
     */
    boolean checkCaretForDelete(boolean forward);

    /** replaces the entire edit area with a given text */
    void replaceEditText(String text);

    /** inserts text at the cursor position */
    void insertText(String text);

    /**
     * Clear history of moving by segments.
     */
    void clearHistory();

    /**
     * Go to next segment from history.
     */
    void gotoHistoryForward();

    /**
     * Go to previous segment from history.
     */
    void gotoHistoryBack();

    /** Get settings instance. */
    EditorSettings getSettings();
    
    void undo();
    
    void redo();
}
