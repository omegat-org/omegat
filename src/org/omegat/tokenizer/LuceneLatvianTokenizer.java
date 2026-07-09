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
import org.apache.lucene.analysis.lv.LatvianAnalyzer;

/**
 * A tokenizer for processing Latvian language text using Lucene's analysis capabilities.
 * This class extends the BaseTokenizer and provides tokenization for Latvian text with
 * options for stemming and the inclusion/exclusion of stop words.
 * <p>
 * This tokenizer utilizes the {@link LatvianAnalyzer} from Lucene for tokenization
 * when stemming is enabled. If stemming is disabled, a standard token stream is used.
 * <p>
 * The tokenizer can be configured to include or exclude stop words and stems in the
 * tokenization process:
 * - When stemming is allowed, the {@link LatvianAnalyzer} is used, and the inclusion of
 *   stop words depends on the specified configuration.
 * - When stemming is not allowed, the tokenizer falls back to the standard token stream
 *   implementation.
 *
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "lv" })
public class LuceneLatvianTokenizer extends BaseTokenizer {
    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? LatvianAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            return new LatvianAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }
}
