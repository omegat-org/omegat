/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2021 Hiroshi Miura
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

/**
 * Class for store one dictionary entry.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class DictionaryEntry implements Comparable<DictionaryEntry> {
    private final String query;
    private final String word;
    private final String article;

    public DictionaryEntry(final String word, final String article) {
        this(word.toLowerCase(), word, article);
    }

    public DictionaryEntry(final String query, final String word, final String article) {
        this.query = query;
        this.word = word;
        this.article = article;
    }

    public String getQuery() {
        return query;
    }

    public String getWord() {
        return word;
    }

    public String getArticle() {
        return article;
    }

    @Override
    public int compareTo(DictionaryEntry o) {
        int queryComparison = query.compareTo(o.query);
        if (queryComparison != 0) {
            return queryComparison;
        } else {
            return word.compareTo(o.word);
        }
    }
}
