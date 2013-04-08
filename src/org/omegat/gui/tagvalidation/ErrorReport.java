/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.gui.tagvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.OStrings;

/**
 * A class to encapuslate information about tag errors. Tag errors are stored
 * separately for the source and translation text. The SourceTextEntry is
 * retained so that we can look up and replace the translation text when
 * auto-fixing tags.
 * 
 * @author Aaron Madlon-Kay
 */
public class ErrorReport {

    final Map<String, TagError> srcErrors = new HashMap<String, TagError>();
    final Map<String, TagError> transErrors = new HashMap<String, TagError>();

    final SourceTextEntry ste;
    final String source;
    final String translation;
    final int entryNum;

    public ErrorReport(SourceTextEntry ste, String translation) {
        this.ste = ste;
        this.source = ste.getSrcText();
        this.translation = translation;
        this.entryNum = ste.entryNum();
    }

    public boolean isEmpty() {
        return srcErrors.isEmpty() && transErrors.isEmpty();
    }

    /**
     * Obtain an inverse map, indicating which tags are associated with which
     * errors.
     * 
     * @return A map between errors and tags
     */
    public Map<TagError, List<String>> inverseReport() {
        Map<TagError, List<String>> result = new HashMap<TagError, List<String>>();
        fillInverseReport(srcErrors, result);
        fillInverseReport(transErrors, result);
        return result;
    }

    private static void fillInverseReport(Map<String, TagError> input, Map<TagError, List<String>> collector) {
        for (Entry<String, TagError> e : input.entrySet()) {
            List<String> existing = collector.get(e.getValue());
            if (existing == null) {
                existing = new ArrayList<String>();
                collector.put(e.getValue(), existing);
            }
            existing.add(e.getKey());
        }
    }

    /**
     * An enum indicating various tag problems.
     */
    public enum TagError {
        /**
         * Indicates a tag in the source text that is not present in the
         * translation.
         */
        MISSING,
        /**
         * Indicates an extraneous tag in the translation that is not present in
         * the source text.
         */
        EXTRANEOUS,
        /**
         * Indicates that the tag appears in a different order in the
         * translation than in the source.
         */
        ORDER,
        /**
         * Indicates a nesting problem, such as overlapping or swapped
         * open/close tags.
         */
        MALFORMED,
        /**
         * Indicates that either leading or trailing whitespace in the
         * translation does not match the source.
         */
        WHITESPACE,
        /**
         * Indicates that a tag that is present in the source appears twice or
         * more in the translation.
         */
        DUPLICATE,
        /** Indicates the partner of a MISSING tag. */
        ORPHANED,
        /**
         * Indicates that something is wrong, but we don't know what (validation
         * logic too simple to give proper advice).
         */
        UNSPECIFIED
    }

    /**
     * Obtain the appropriate user-facing string for an error.
     * 
     * @param error
     * @return The localized error name
     */
    public static String localizedTagError(TagError error) {
        switch (error) {
        case DUPLICATE:
            return OStrings.getString("TAG_ERROR_DUPLICATE");
        case MALFORMED:
            return OStrings.getString("TAG_ERROR_MALFORMED");
        case MISSING:
            return OStrings.getString("TAG_ERROR_MISSING");
        case EXTRANEOUS:
            return OStrings.getString("TAG_ERROR_EXTRANEOUS");
        case ORDER:
            return OStrings.getString("TAG_ERROR_ORDER");
        case WHITESPACE:
            return OStrings.getString("TAG_ERROR_WHITESPACE");
        case ORPHANED:
            return OStrings.getString("TAG_ERROR_ORPHANED");
        case UNSPECIFIED:
            return OStrings.getString("TAG_ERROR_UNSPECIFIED");
        }
        return null;
    }
}
