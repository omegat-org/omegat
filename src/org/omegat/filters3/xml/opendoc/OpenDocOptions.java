/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Didier Briel
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

package org.omegat.filters3.xml.opendoc;

import java.util.Map;

import org.omegat.filters2.AbstractOptions;

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
public class OpenDocOptions extends AbstractOptions {
    private static final String OPTION_TRANSLATE_INDEXES = "translateIndexes";
    private static final String OPTION_TRANSLATE_BOOKMARKS = "translateBookmarks";
    private static final String OPTION_TRANSLATE_BOOKMARKS_REFS = "translateBookmarkRefs";
    private static final String OPTION_TRANSLATE_NOTES = "translateNotes";
    private static final String OPTION_TRANSLATE_COMMENTS = "translateComments";

    public OpenDocOptions(Map<String, String> config) {
        super(config);
    }

    /**
     * Returns whether Indexes should be translated.
     */
    public boolean getTranslateIndexes() {
        return getBoolean(OPTION_TRANSLATE_INDEXES, true);
    }

    /**
     * Sets whether Indexes be translated.
     */
    public void setTranslateIndexes(boolean translateIndexes) {
        setBoolean(OPTION_TRANSLATE_INDEXES, translateIndexes);
    }

    /**
     * Returns whether Bookmarks should be translated.
     */
    public boolean getTranslateBookmarks() {
        return getBoolean(OPTION_TRANSLATE_BOOKMARKS, false);
    }

    /**
     * Sets whether Bookmarks should be translated.
     */
    public void setTranslateBookmarks(boolean translateBookmarks) {
        setBoolean(OPTION_TRANSLATE_BOOKMARKS, translateBookmarks);
    }

    /**
     * Returns whether Bookmark references should be translated.
     */
    public boolean getTranslateBookmarkRefs() {
        return getBoolean(OPTION_TRANSLATE_BOOKMARKS_REFS, true);
    }

    /**
     * Sets whether Bookmarks references should be translated.
     */
    public void setTranslateBookmarkRefs(boolean translateBookmarkRefs) {
        setBoolean(OPTION_TRANSLATE_BOOKMARKS_REFS, translateBookmarkRefs);
    }

    /**
     * Returns whether Notes should be translated.
     */
    public boolean getTranslateNotes() {
        return getBoolean(OPTION_TRANSLATE_NOTES, true);
    }

    /**
     * Sets whether Notes should be translated.
     */
    public void setTranslateNotes(boolean translateNotes) {
        setBoolean(OPTION_TRANSLATE_NOTES, translateNotes);
    }

    /**
     * Returns whether Comments should be translated.
     */

    public boolean getTranslateComments() {
        return getBoolean(OPTION_TRANSLATE_COMMENTS, true);
    }

    /**
     * Sets whether Comments should be translated.
     */
    public void setTranslateComments(boolean translateComments) {
        setBoolean(OPTION_TRANSLATE_COMMENTS, translateComments);
    }
}
