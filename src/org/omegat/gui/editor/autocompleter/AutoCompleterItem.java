/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013, 2014 Aaron Madlon-Kay
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

/**
 * A class to represent an item available for auto-completion.
 * Upon confirming the selection, the item's payload will be
 * inserted into the translation.
 * <p>
 * Additional information such as comments, source terms, etc.
 * can be stored in <code>extras</code>. The {@link AutoCompleterListView}
 * that created the AutoCompleterItem should know how to use these
 * values appropriately in its {@link AutoCompleterListView#itemToString(AutoCompleterItem)}
 * method.
 * 
 * @author Aaron Madlon-Kay
 */
public class AutoCompleterItem {
    public final String payload;
    public final String[] extras;
    public final int cursorAdjust;
    public final boolean keepSelection;
    
    public AutoCompleterItem(String payload, String[] extras) {
        this.payload = payload;
        this.extras = extras;
        this.cursorAdjust = 0;
        this.keepSelection = false;
    }
    
    public AutoCompleterItem(String payload, String[] extras, int cursorAdjust, boolean keepSelection) {
        this.payload = payload;
        this.extras = extras;
        this.cursorAdjust = cursorAdjust;
        this.keepSelection = keepSelection;
    }
}
