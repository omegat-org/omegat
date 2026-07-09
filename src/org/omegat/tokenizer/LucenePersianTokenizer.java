/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik (alex73mail@gmail.com)
               2013 Aaron Madlon-Kay
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
import org.apache.lucene.analysis.fa.PersianAnalyzer;

/**
 * A tokenizer for processing Persian text using Apache Lucene's {@link PersianAnalyzer}.
 * <p>
 * This tokenizer provides token streams specifically tailored for the Persian language
 * with options to enable or disable stemming and stop word processing. It leverages
 * the default stop word set provided in the {@link PersianAnalyzer}.
 * <p>
 * The class is designed to handle text tokenization requirements for Persian, considering
 * the unique linguistic characteristics of the language.
 *
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "fa" })
public class LucenePersianTokenizer extends BaseTokenizer {

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? PersianAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            PersianAnalyzer analyzer = new PersianAnalyzer(stopWords);
            return analyzer.tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }
}
