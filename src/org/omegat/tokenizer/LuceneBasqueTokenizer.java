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
import org.apache.lucene.analysis.eu.BasqueAnalyzer;

/**
 * Tokenizer class specifically designed for processing Basque text.
 * It extends the {@link BaseTokenizer} and provides functionality to tokenize Basque input text.
 * <p>
 * This tokenizer uses the {@link BasqueAnalyzer} to perform tokenization, optionally applying
 * stemming and stop word removal based on the parameter configurations. If stemming is not
 * allowed, it defaults to standard tokenization.
 * <p>
 * This class is annotated with {@link Tokenizer} to associate it with the Basque language using the language code "eu".
 *
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "eu" })
public class LuceneBasqueTokenizer extends BaseTokenizer {
    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? BasqueAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            return new BasqueAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }
}
