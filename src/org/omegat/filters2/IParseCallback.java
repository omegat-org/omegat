/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters2;


/**
 * Callback for parse files.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public interface IParseCallback {
    /**
     * Read entry from source file
     * 
     * @param id
     *            ID in source file, or null if ID not supported by format
     * @param source
     *            source entry text
     * @param translation
     *            exist translation text
     * @param isFuzzy
     *            true if translation is fuzzy
     * @param comment
     *            comment for entry, if format supports it
     * @param path
     *            path of segment
     * @param filter
     *            filter which produces entry
     * @param shortcutDetails
     *            shortcuts of source text details
     */
    void addEntry(String id, String source, String translation, boolean isFuzzy, String comment, String path,
            IFilter filter, Shortcuts shortcutDetails);

    /**
     * Old call without path, for compatibility 
     */
    void addEntry(String id, String source, String translation, boolean isFuzzy, String comment, 
            IFilter filter);

    /**
     * This method can be called from any filter on the end of file processing. It links prev/next segments
     * for multiple translations.
     */
    void linkPrevNextSegments();
}
