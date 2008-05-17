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
     * 
     * Must be called only from UI thread.
     */
    void showIntoduction();

    /**
     * Get current file name which opened in editor.
     * 
     * Can be called from any threads.
     */
    String getCurrentFile();

    /**
     * Get current active entry.
     * 
     * Can be called from any threads.
     */
    SourceTextEntry getCurrentEntry();

    /**
     * Activate entry for edit.
     * 
     * Must be called only from UI thread.
     */
    void activateEntry();

    /**
     * Commits the translation. Translation will be saved.
     * 
     * Must be called only from UI thread.
     */
    void commitEntry();

    /**
     * Commits the translation.
     * 
     * @param forceCommit
     *                If false, the translation will not be saved
     *                
     * Must be called only from UI thread.
     */
    void commitEntry(boolean forceCommit);

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
     * Goto entry with specified number.
     * 
     * @param entryNum
     *                entry number
     * 
     * Must be called only from UI thread.
     */
    void gotoEntry(int entryNum);

    /**
     * Change case of the selected text or if none is selected, of the current
     * word.
     * 
     * @param newCase :
     *                lower, title, upper or cycle
     *                
     * Must be called only from UI thread.
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
     * Clear history of moving by segments.
     * 
     * Must be called only from UI thread.
     */
    void clearHistory();

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
     * Must be called only from UI thread.
     */
    String getSelectedText();
}
