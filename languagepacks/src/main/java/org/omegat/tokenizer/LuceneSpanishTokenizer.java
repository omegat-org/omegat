/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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
package org.omegat.tokenizer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "es" }, isDefault = true)
public class LuceneSpanishTokenizer extends BaseTokenizer {
    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? SpanishAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            return new SpanishAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }
}
