/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

package org.omegat.gui.notes;

/**
 * Interface for access to notes pane.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface INotes {
    /**
     * Get note's text, which may be edited.
     * 
     * @return new note's text
     */
    String getNoteText();

    /**
     * Set note's text for current entry.
     * 
     * @param note
     *            note's text, or null if note doesn't exist
     */
    void setNoteText(String note);

    /**
     * Clear pane.
     */
    void clear();
}
