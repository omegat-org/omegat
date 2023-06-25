/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014-2015 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autocompleter;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.text.BadLocationException;

import org.omegat.core.Core;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * An abstract auto-completer view.
 * 
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public abstract class AbstractAutoCompleterView {

    /**
     * the name appearing in the auto-completer.
     */
    private String name;

    /**
     * the completer
     */
    protected AutoCompleter completer;

    /**
     * Creates a new auto-completer view.
     * 
     * @param name
     *            the name of this view
     */
    public AbstractAutoCompleterView(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the AutoCompleter that this view belongs to.
     * 
     * @param completer
     */
    public void setParent(AutoCompleter completer) {
        this.completer = completer;
    }

    /**
     * Return the tokenizer for use with the view. Custom views should override
     * this if they have special tokenization needs.
     */
    public ITokenizer getTokenizer() {
        return Core.getProject().getTargetTokenizer();
    }

    /**
     * Return the target language currently in use.
     */
    public Language getTargetLanguage() {
        return Core.getProject().getProjectProperties().getTargetLanguage();
    }

    /**
     * Process the autocompletion keys
     * 
     * @param e
     *            the key event to process
     * @return true if a key has been processed, false if otherwise.
     */
    public abstract boolean processKeys(KeyEvent e);

    /**
     * return the size of the data list / array.
     * 
     * @return
     */
    public abstract int getRowCount();

    /**
     * get the preferred height of the component
     * 
     * @return
     */
    public abstract int getPreferredHeight();

    /**
     * get the preferred width of the component
     * 
     * @return
     */
    public abstract int getPreferredWidth();

    /**
     * get the selected value
     * 
     * @return
     */
    public abstract AutoCompleterItem getSelectedValue();

    /**
     * Update the view data
     */
    public abstract void updateViewData();

    /**
     * Obtain the content to put in the autocompleter popup. The view should
     * also do any other preparation necessary for display.
     *
     * @return the component to show in the autocompleter popup
     */
    public abstract Component getViewContent();

    /**
     * Return a modified row count. The basic implementation. Override this in
     * the different view types.
     * 
     * @return a modified row count.
     */
    protected int getModifiedRowCount() {
        return Math.min(getRowCount() + 1, AutoCompleter.PAGE_ROW_COUNT);
    }

    /**
     * Return true to indicate that the view has relevant contextual suggestions
     * that merit displaying the AutoCompleter popup unprompted.
     *
     * @return Whether or not the AutoCompleter should appear
     */
    public abstract boolean shouldPopUp();

    /**
     * Indicates whether or not the AutoCompleter should close by default when
     * the user confirms a selection. Override and return false to keep the
     * popup open.
     *
     * @return Whether or not the AutoCompleter popup should close upon
     *         selection
     */
    public boolean shouldCloseOnSelection() {
        return true;
    }

    protected String getLeadingText() {
        try {
            EditorTextArea3 editor = completer.getEditor();
            int offset = editor.getCaretPosition();
            int translationStart = editor.getOmDocument().getTranslationStart();
            return editor.getDocument().getText(translationStart, offset - translationStart);
        } catch (BadLocationException e) {
            return "";
        }
    }

    /**
     * Indicates whether the view should be considered "on" or "off". When off,
     * the view will not be shown in any circumstances (whether automatically or
     * manually).
     *
     * @return
     */
    protected boolean isEnabled() {
        return true;
    }
}
