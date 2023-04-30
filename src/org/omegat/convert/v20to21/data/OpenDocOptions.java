/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Didier Briel
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

package org.omegat.convert.v20to21.data;

import java.io.Serializable;

/**
 * Options for OpenDoc filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * OpenDoc filter have the following options ([+] means default on).
 * Translatable elements:
 * <ul>
 * <li>[+] Index entries
 * <li>[] Bookmarks
 * <li>[+] Bookmark references
 * <li>[+] Notes
 * <li>[+] Comments
 * </ul>
 *
 * @author Didier Briel
 */
public class OpenDocOptions implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /** Hold value of properties. */
    private boolean translateIndexes = true;
    private boolean translateBookmarks = false;
    private boolean translateBookmarkRefs = true;
    private boolean translateNotes = true;
    private boolean translateComments = true;

    /**
     * Returns whether Indexes should be translated.
     */
    public boolean getTranslateIndexes() {
        return this.translateIndexes;
    }

    /**
     * Sets whether Indexes be translated.
     */
    public void setTranslateIndexes(boolean translateIndexes) {
        this.translateIndexes = translateIndexes;
    }

    /**
     * Returns whether Bookmarks should be translated.
     */
    public boolean getTranslateBookmarks() {
        return this.translateBookmarks;
    }

    /**
     * Sets whether Bookmarks should be translated.
     */
    public void setTranslateBookmarks(boolean translateBookmarks) {
        this.translateBookmarks = translateBookmarks;
    }

    /**
     * Returns whether Bookmark references should be translated.
     */
    public boolean getTranslateBookmarkRefs() {
        return this.translateBookmarkRefs;
    }

    /**
     * Sets whether Bookmarks references should be translated.
     */
    public void setTranslateBookmarkRefs(boolean translateBookmarkRefs) {
        this.translateBookmarkRefs = translateBookmarkRefs;
    }

    /**
     * Returns whether Notes should be translated.
     */
    public boolean getTranslateNotes() {
        return this.translateNotes;
    }

    /**
     * Sets whether Notes should be translated.
     */
    public void setTranslateNotes(boolean translateNotes) {
        this.translateNotes = translateNotes;
    }

    /**
     * Returns whether Comments should be translated.
     */

    public boolean getTranslateComments() {
        return this.translateComments;
    }

    /**
     * Sets whether Comments should be translated.
     */
    public void setTranslateComments(boolean translateComments) {
        this.translateComments = translateComments;
    }

}
