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

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;

/**
 * A tokenizer implementation for the Norwegian language, tailored for use
 * with text processing tasks. This class extends the BaseTokenizer and
 * provides functionality to tokenize Norwegian text using Lucene's
 * NorwegianAnalyzer.
 * <p>
 * This tokenizer supports processing with and without stemming, as well as
 * with or without stop word filtering. The underlying tokenization engine
 * uses Lucene's NorwegianAnalyzer for producing tokens when stemming is
 * enabled.
 * <p>
 * This class is marked as the default tokenizer for the Norwegian language.
 *
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "nb" }, isDefault = true)
public class LuceneNorwegianTokenizer extends BaseTokenizer {
    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? NorwegianAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            return new NorwegianAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }
}
