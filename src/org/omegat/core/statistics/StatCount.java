/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2013 Alex Buloichik
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

package org.omegat.core.statistics;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.StaticUtils;

/**
 * Bean for store counts in statistics.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StatCount {
    /**
     * OmegaT has two possible words calculation modes:
     * 
     * 1) All protected parts(including tags, placeholders, protected text and related tags) are not counted in
     * the word count(default). For example: "<i1>", "<m0>Acme</m0>" will produce 0 words.
     * 
     * 2) Protected texts are counted, but related tags are not counted in the word count. For example: "<i1>"
     * - 0 words, "<m0>Acme</m0>" - 1 word.
     */
    static final boolean REMOVE_ALL_PROTECTED_PARTS = true;

    public int segments, words, charsWithoutSpaces, charsWithSpaces;

    /**
     * Initialize counts with zeros.
     */
    public StatCount() {
    }

    /**
     * Initialize counters with counts from entry's source.
     */
    public StatCount(SourceTextEntry ste) {
        String src;
        if (REMOVE_ALL_PROTECTED_PARTS) {
            src = StaticUtils.stripAllTagsFromSource(ste);
        } else {
            src = StaticUtils.stripXmlTags(ste.getSrcText());
        }
        segments = 1;
        words = Statistics.numberOfWords(src);
        charsWithoutSpaces = Statistics.numberOfCharactersWithoutSpaces(src);
        charsWithSpaces = Statistics.numberOfCharactersWithSpaces(src);
    }

    public void add(StatCount c) {
        segments += c.segments;
        words += c.words;
        charsWithoutSpaces += c.charsWithoutSpaces;
        charsWithSpaces += c.charsWithSpaces;
    }
}
