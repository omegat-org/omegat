/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013-2015 Aaron Madlon-Kay
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

import java.util.Arrays;

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
    public final int replacementLength;

    public AutoCompleterItem(String payload, String[] extras, int replacementLength) {
        this.payload = payload;
        this.extras = extras;
        this.cursorAdjust = 0;
        this.keepSelection = false;
        this.replacementLength = replacementLength;
    }

    public AutoCompleterItem(String payload, String[] extras, int cursorAdjust, boolean keepSelection, int replacementLength) {
        this.payload = payload;
        this.extras = extras;
        this.cursorAdjust = cursorAdjust;
        this.keepSelection = keepSelection;
        this.replacementLength = replacementLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cursorAdjust;
        result = prime * result + Arrays.hashCode(extras);
        result = prime * result + (keepSelection ? 1231 : 1237);
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + replacementLength;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AutoCompleterItem other = (AutoCompleterItem) obj;
        if (cursorAdjust != other.cursorAdjust) {
            return false;
        }
        if (!Arrays.equals(extras, other.extras)) {
            return false;
        }
        if (keepSelection != other.keepSelection) {
            return false;
        }
        if (payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!payload.equals(other.payload)) {
            return false;
        }
        if (replacementLength != other.replacementLength) {
            return false;
        }
        return true;
    }
}
