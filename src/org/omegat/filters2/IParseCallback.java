/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.filters2;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProtectedPart;

/**
 * Callback for parse files.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public interface IParseCallback {
    /**
     * Read entry from a source file, with arbitrary (optional) properties
     *
     * @param id
     *            ID in a source file, or null if ID not supported by format
     * @param source
     *            source entry text
     * @param translation
     *            exist translation text
     * @param isFuzzy
     *            true if translation is fuzzy
     * @param props
     *            an array of key=value metadata properties for the entry
     * @param path
     *            path of segment
     * @param filter
     *            filter which produces entry
     * @param protectedParts
     *            protected parts
     */
    void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy, String[] props,
            String path, IFilter filter, List<ProtectedPart> protectedParts);

    /**
     * Read entry from a source file, with single "comment" property.
     * Convenience method for
     * {@link #addEntryWithProperties(String, String, String, boolean, String[], String, IFilter, List)}.
     */
    void addEntry(@Nullable String id, String source, @Nullable String translation, boolean isFuzzy,
            @Nullable String comment, @Nullable String path, IFilter filter,
            @Nullable List<ProtectedPart> protectedParts);

    /**
     * This method is called by filters to add new entry in OmegaT after read it
     * from a source file.
     * <p>
     * Old call without a path, for compatibility. Comment is converted to a
     * property.
     *
     * @param id
     *            ID of entry, if a format supports it
     * @param source
     *            Translatable source string
     * @param translation
     *            translation of the source string, if a format supports it
     * @param isFuzzy
     *            flag for fuzzy translation. If a translation is fuzzy, it is
     *            not added to the projects TMX, but it is added to the
     *            generated 'reference' TMX, a special TMX that is used as extra
     *            reference during translation.
     * @param comment
     *            entry's comment, if format supports it
     * @param filter
     *            filter which produces entry
     */
    default void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
            IFilter filter) {
        addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
    }

    /**
     * This method can be called from any filter on the end of file processing.
     * It links prev/next segments for multiple translations.
     */
    void linkPrevNextSegments();
}
