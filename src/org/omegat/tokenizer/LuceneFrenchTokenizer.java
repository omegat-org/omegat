/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik (alex73mail@gmail.com)
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
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.FrenchStemmer;

/**
 * The LuceneFrenchTokenizer is a specialized tokenizer intended for processing
 * French language text with Lucene. It extends the functionality of the
 * BaseTokenizer and integrates language-specific tokenization features.
 * <p>
 * This tokenizer provides support for stop words and stemming, enabling
 * enhanced linguistic analysis for French text. When stop words and/or
 * stemming are enabled, it utilizes the FrenchAnalyzer from the Lucene
 * library. If stemming is disabled, standard tokenization is applied.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "fr" }, isDefault = true)
public class LuceneFrenchTokenizer extends BaseTokenizer {

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final StemmingMode stemmingMode,
            final boolean stopWordsAllowed) throws IOException {
        CharArraySet stopWords = stopWordsAllowed ? FrenchAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
        switch (stemmingMode) {
        case NONE:
            return getStandardTokenStream(strOrig);
        case GLOSSARY_FULL:
        case MATCHING_FULL:
            return new SnowballFilter(
                    new FrenchAnalyzer(stopWords).tokenStream("", new StringReader(strOrig)),
                    new FrenchStemmer());
        default:
            return new FrenchAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        }
    }

    @Override
    protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed)
            throws IOException {
        return getTokenStream(strOrig, stemsAllowed ? StemmingMode.GLOSSARY : StemmingMode.NONE, stopWordsAllowed);
    }
}
