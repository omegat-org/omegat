/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2014 Alex Buloichik
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

package org.omegat.core.statistics;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;

/**
 * Bean for store counts in statistics.
 * <p>
 * OmegaT has two possible words calculation modes:
 * <ol>
 * <li>All protected parts(including tags, placeholders, protected text and
 * related tags) are not counted in the word count (default). For example:
 * "&lt;i1&gt; "&lt;m0&gt;&lt;/m0&gt; will produce 0 words.
 *
 * <li>Protected texts are counted, but related tags are not counted in the word
 * count. For example: "&lt;i1&gt; - 0 words, "&lt;m0&gt;&lt;/m0&gt; - 1 word.
 * </ol>
 * The mode is stored in the
 * {@link StatisticsSettings#isCountingProtectedText()} property.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StatCount {

    @JsonProperty("segments")
    @XmlAttribute(name = "segments")
    public int segments;
    @JsonProperty("words")
    @XmlAttribute(name = "words")
    public int words;
    @JsonProperty("characters-without-spaces")
    @XmlAttribute(name = "characters-without-spaces")
    public int charsWithoutSpaces;
    @JsonProperty("characters")
    @XmlAttribute(name = "characters")
    public int charsWithSpaces;
    @JsonProperty("files")
    @XmlAttribute(name = "files")
    public int files;

    /**
     * Initialize counts with zeros.
     */
    public StatCount() {
    }

    /**
     * Initialize counters with counts from entry's source.
     */
    public StatCount(SourceTextEntry ste) {
        String src = ste.getSrcText();
        for (ProtectedPart pp : ste.getProtectedParts()) {
            src = src.replace(pp.getTextInSourceSegment(), pp.getReplacementWordsCountCalculation());
        }
        segments = 1;
        words = Statistics.numberOfWords(src);
        charsWithoutSpaces = Statistics.numberOfCharactersWithoutSpaces(src);
        charsWithSpaces = Statistics.numberOfCharactersWithSpaces(src);
    }

    public StatCount add(StatCount c) {
        segments += c.segments;
        words += c.words;
        charsWithoutSpaces += c.charsWithoutSpaces;
        charsWithSpaces += c.charsWithSpaces;
        return this;
    }

    public void addFiles(int count) {
        files += count;
    }
}
