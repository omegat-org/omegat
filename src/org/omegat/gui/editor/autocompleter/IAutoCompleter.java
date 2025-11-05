/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import java.awt.event.KeyEvent;

/**
 * Interface for autocompleter.
 *
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public interface IAutoCompleter {
    /**
     * Add a view to the autocompleter.
     * @param view the view to add
     * @deprecated
     */
    @Deprecated(since = "6.1.0")
    void addView(AbstractAutoCompleterView view);

    /**
     * Process the autocompletion keys.
     * @param e the key event to process
     * @return true if a key has been processed, false if otherwise.
     */
    boolean processKeys(KeyEvent e);

    /**
     * Reset the autocompletion keys.
     */
    void resetKeys();

    /**
     * Check if the autocompleter is visible.
     * @return true if visible, false otherwise.
     */
    boolean isVisible();

    /**
     * Set the visibility of the autocompleter.
     * @param visible true to show, false to hide.
     */
    void setVisible(boolean visible);

    /**
     * Update the autocompleter popup.
     * @param onlyIfVisible true to update only if visible, false otherwise.
     */
    void updatePopup(boolean onlyIfVisible);

    /**
     * Notify the autocompleter that the text has changed.
     */
    void textDidChange();
}
