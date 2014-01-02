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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;

/**
 * This uses the Korean tokenizer currently under development
 * for inclusion in Lucene (but not yet incorporated).
 * <p>
 * The code quality appears to be poor at the moment (see LUCENE-4956 thread;
 * spurious ArrayIndexOutOfBoundsException errors observable in normal usage
 * within OmegaT) so {@link Tokenizer#isDefault()} is currently <code>false</code>.
 * 
 * @see http://sourceforge.net/projects/lucenekorean/
 * @see http://cafe.naver.com/korlucene
 * @see https://issues.apache.org/jira/browse/LUCENE-4956
 * 
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "ko" })
public class LuceneKoreanTokenizer extends BaseTokenizer {

    @Override
    protected TokenStream getTokenStream(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed) {
        if (stemsAllowed) {
            Set<String> stopWords = stopWordsAllowed ? KoreanAnalyzer.STOP_WORDS_SET
                    : Collections.EMPTY_SET;
            return new KoreanAnalyzer(getBehavior(), stopWords).tokenStream("",
            		new StringReader(strOrig));
        } else {
            return new KoreanTokenizer(getBehavior(), new StringReader(strOrig));
        }
    }
}
