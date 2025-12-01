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
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.omegat.core.Core;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 * The LuceneEnglishTokenizer class provides a tokenization implementation for
 * the English language using Lucene's EnglishAnalyzer. It is integrated as the
 * default tokenizer for the "en" (English) language in OmegaT.
 * <p>
 * This tokenizer can handle stop words and word stemming based on the provided
 * configuration flags. It supports tokenization of text using either Lucene's
 * standard tokenization or with additional analysis features such as stemming
 * and stop word removal.
 *
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "en" }, isDefault = true)
public class LuceneEnglishTokenizer extends BaseTokenizer {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
    }

    public static void unloadPlugins() {
    }

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(String strOrig, StemmingMode stemmingMode, boolean stopWordsAllowed)
            throws IOException {
        CharArraySet stopWords = stopWordsAllowed ? EnglishAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
        switch (stemmingMode) {
        case NONE:
            return getStandardTokenStream(strOrig);
        case GLOSSARY_FULL:
        case MATCHING_FULL:
            return new SnowballFilter(
                    new EnglishAnalyzer(stopWords).tokenStream("", new StringReader(strOrig)),
                    new EnglishStemmer());
        default:
            return new EnglishAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        }
    }

    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        return getTokenStream(strOrig, stemsAllowed ? StemmingMode.GLOSSARY : StemmingMode.NONE, stopWordsAllowed);
    }
}
