/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.io.IOException;
import java.util.List;

/**
 * Interface for dictionary access.
 * <p>
 * Each dictionary format reader should implement this interface. Each instance
 * of this interface represents one dictionary.
 * <p>
 * Implementers are encouraged to use {@link DictionaryData} to store their
 * data. A simple implementation can just load its entire data at once, but we
 * don't recommend it because it's very memory-intensive.
 * <p>
 * Instead we recommend that you read the dictionary's index (article titles)
 * and store some short data, like article offsets into the dictionary file, as
 * the value. In your implementation of the <code>readArticles*</code> methods
 * you can use these offsets to actually load the content from disk. The OS will
 * cache dictionary file against slow access.
 * <p>
 * See {@link StarDict} for an example of the recommended deferred-loading
 * implementation, and {@link LingvoDSL} for an example of an simpler, up-front
 * loading implementation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public interface IDictionary extends AutoCloseable {

    /**
     * Read article's text.
     *
     * @param word
     *            The word to look up in the dictionary
     *
     * @return List of entries. May be empty, but cannot be null.
     */
    List<DictionaryEntry> readArticles(String word) throws Exception;

    /**
     * Read article's text. Matching is predictive, so e.g. supplying "term"
     * will return articles for "term", "terminology", "termite", etc. The
     * default implementation simply calls {@link #readArticles(String)} for
     * backwards compatibility.
     *
     * @param word
     *            The word to look up in the dictionary
     *
     * @return List of entries. May be empty, but cannot be null.
     */
    default List<DictionaryEntry> readArticlesPredictive(String word) throws Exception {
        // Default implementation for backwards compatibility
        return readArticles(word);
    }

    /**
     * Dispose IDictionary. Default is no action.
     */
    default void close() throws IOException { }
}
