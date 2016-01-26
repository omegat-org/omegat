/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.util.List;

/**
 * Interface for dictionary access.
 * <p>
 * Each dictionary format reader should implement this interface. Each instance
 * of this interface represents one dictionary.
 * <p>
 * Simplest dictionary implementation can just read full dictionary data into
 * map, but we don't recommend it, because it's very memory consuming algorithm.
 * Recommended way is to just read article's names and store some short data,
 * like position in dictionary file, in the value of map. After that,
 * {@link #readArticles(String)} method could read dictionary file. OS will
 * cache dictionary file against slow access.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public interface IDictionary {

    /**
     * Read article's text.
     * 
     * @param word
     *            The word to look up in the dictionary
     * 
     * @return List of entries. May be empty, but cannot be null.
     */
    List<DictionaryEntry> readArticles(String word) throws Exception;
}
