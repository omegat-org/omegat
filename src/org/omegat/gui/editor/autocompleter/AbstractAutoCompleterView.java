/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.editor.autocompleter;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.tokenizer.ITokenizer;

/**
 * An abstract auto-completer view.
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
abstract public class AbstractAutoCompleterView {

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
     * @param name the name of this view
     * @param completer the completer it belongs to
     */
    public AbstractAutoCompleterView(String name, AutoCompleter completer) {
        this.name = name;
        this.completer = completer;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the tokenizer for use with the view.
     * Custom views should override this if they have special
     * tokenization needs.
     */
    public ITokenizer getTokenizer() {
        return Core.getProject().getTargetTokenizer();
    }
    
    /**
     * Process the autocompletion keys
     * @param e the key event to process
     * @return true if a key has been processed, false if otherwise.
     */
    public abstract boolean processKeys(KeyEvent e, boolean visible);
    
    /**
     * return the size of the data list / array.
     * @return 
     */
    public abstract int getRowCount();
    
    /**
     * get the preferred height of the component
     * @return 
     */
    public abstract int getPreferredHeight();
    
    /**
     * set the list or table data
     * @param entryList the entries
     */
    public abstract void setData(List<AutoCompleterItem> entryList);
    
    /**
     * get the selected value
     * @return 
     */
    public abstract AutoCompleterItem getSelectedValue();
    
    /**
     * Update the view data
     * @return true if any update has been done.
     */
    public abstract boolean updateViewData();
    
    /**
     * Obtain the content to put in the autocompleter popup.
     * The view should also do any other preparation necessary for
     * display.
     * 
     * @return the component to show in the autocompleter popup
     */
    public abstract Component getViewContent();
    
    /**
     * Return a modified row count. The basic implementation. Override this in the
     * different view types.
     * @return a modified row count.
     */
    protected int getModifiedRowCount() {
        return Math.min(getRowCount(), AutoCompleter.pageRowCount);
    }
}
