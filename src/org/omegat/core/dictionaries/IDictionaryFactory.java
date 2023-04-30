/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.io.File;

import org.omegat.util.Language;

/**
 * An interface that defines support for a particular dictionary format.
 *
 * @author Aaron Madlon-Kay
 */
public interface IDictionaryFactory {

    /**
     * Determine whether or not the supplied file is supported by this factory.
     * This is intended to be a lightweight check, e.g. looking for a file
     * extension.
     *
     * @param file
     *            The file to check
     * @return Whether or not the file is supported
     */
    boolean isSupportedFile(File file);

    /**
     * Load the given file and return an {@link IDictionary} that wraps it.
     * Deprecated; use {@link #loadDict(File, Language)} instead.
     *
     * @param file
     *            The file to load
     * @return An IDictionary file that can read articles from the file
     * @throws Exception
     *             If the file could not be loaded for reasons that were not
     *             determined by {@link #isSupportedFile(File)}
     */
    IDictionary loadDict(File file) throws Exception;

    /**
     * Load the given file and return an {@link IDictionary} that wraps it. The
     * supplied file is guaranteed to have returned true from
     * {@link #isSupportedFile(File)}.
     *
     * @param file
     *            The file to load
     * @param language
     *            The language of the dictionary's index (e.g. English for an
     *            English-French dictionary)
     * @return An IDictionary file that can read articles from the file
     * @throws Exception
     *             If the file could not be loaded for reasons that were not
     *             determined by {@link #isSupportedFile(File)}
     */
    default IDictionary loadDict(File file, Language language) throws Exception {
        return loadDict(file);
    }
}
