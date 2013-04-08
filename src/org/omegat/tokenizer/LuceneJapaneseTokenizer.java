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
package org.omegat.tokenizer;

import java.io.StringReader;
import java.util.Collections;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.apache.lucene.util.Version;

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
    protected TokenStream getTokenStream(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed) {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopSet()
                    : new CharArraySet(Version.LUCENE_36, 0, false);
            Set<String> stopTags = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopTags()
                    : Collections.EMPTY_SET;
            return new JapaneseAnalyzer(Version.LUCENE_36, null,
                    Mode.SEARCH, stopWords, stopTags).tokenStream("", new StringReader(
                    strOrig));
        } else {
            return new JapaneseTokenizer(new StringReader(strOrig), null, false, Mode.NORMAL);
        }
    }
}
