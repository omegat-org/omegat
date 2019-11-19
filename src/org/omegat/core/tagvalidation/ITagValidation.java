/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2013 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.tagvalidation;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;

/**
 * Interface for tag validation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface ITagValidation {
    /**
     * Get invalid tags entries.
     *
     * @return List of entries with invalid tags. Will be empty if no invalid
     *         tags are found.
     */
    List<ErrorReport> listInvalidTags();

    /**
     * Get invalid tags entries from specified files corresponding to
     * sourcePattern.
     *
     * @param sourcePattern
     *            The regexp of files to validate
     * @return List of entries with invalid tags. Will be empty if no invalid
     *         tags are found.
     */
    List<ErrorReport> listInvalidTags(String sourcePattern);

    /**
     * Checks invalid tags for one entry.
     *
     * @param ste
     *            entry
     * @return true if there are no invalid tags
     */
    boolean checkInvalidTags(SourceTextEntry ste);

    /**
     * Log invalid tags entries to console.
     *
     * @param invalidTagsEntries
     *            list of invalid tags entries(from {@link #listInvalidTags()}
     *            method)
     */
    void logTagValidationErrors(List<ErrorReport> invalidTagsEntries);
}
