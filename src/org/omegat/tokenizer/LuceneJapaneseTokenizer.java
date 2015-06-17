/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2013, 2015 Aaron Madlon-Kay
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
package org.omegat.tokenizer;

import java.io.StringReader;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.omegat.util.PatternConsts;

/**
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "ja" }, isDefault = true)
public class LuceneJapaneseTokenizer extends BaseTokenizer {

    public LuceneJapaneseTokenizer() {
        super();
        shouldDelegateTokenizeExactly = false;
    }

    @Override
    protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed) {
        if (stemsAllowed) {
            // Blank out tags when stemming only
            strOrig = blankOutTags(strOrig);
            CharArraySet stopWords = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopSet()
                    : new CharArraySet(getBehavior(), 0, false);
            Set<String> stopTags = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopTags()
                    : Collections.EMPTY_SET;
            return new JapaneseAnalyzer(getBehavior(), null, Mode.SEARCH, stopWords, stopTags)
                    .tokenStream("", new StringReader(strOrig));
        } else {
            return new JapaneseTokenizer(new StringReader(strOrig), null, false, Mode.NORMAL);
        }
    }
    
    /**
     * Replace all instances of OmegaT-style tags (&lt;x0>, etc.) with blank spaces
     * of equal length. This is done because
     * <ul><li>This tokenizer will turn "&lt;x0>" into [x, 0], leaving the alphabetical part
     * intact (other tokenizers are expected to produce [x0], which is suppressed by digit filtering)
     * <li>Instead of merely removing the tags, they are replaced with spaces so that
     * the tokens produced correctly line up with the original, unmodified string.
     * </ul>
     */
    private String blankOutTags(String text) {
        StringBuilder buffer = new StringBuilder(text);
        Matcher m = PatternConsts.OMEGAT_TAG.matcher(text);
        while (m.find()) {
            for (int i = m.start(), end = m.end(); i < end; i++) {
                buffer.replace(i, i + 1, " ");
            }
        }
        return buffer.toString();
    }
}
