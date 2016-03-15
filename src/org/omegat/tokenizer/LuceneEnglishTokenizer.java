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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.omegat.core.Core;

/**
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "en" }, isDefault = true)
public class LuceneEnglishTokenizer extends BaseTokenizer {

    private static final CharArraySet STOP_WORDS;

    static {
        try (InputStream is = LuceneEnglishTokenizer.class.getResourceAsStream(STOPWORDS_FILE_EN);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
            Set<String> set = br.lines().map(String::trim).filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());
            STOP_WORDS = CharArraySet.copy(set);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(
                    "Error load stopwords in LuceneEnglishTokenizer: " + ex.getMessage());
        }
    }

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
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? STOP_WORDS : CharArraySet.EMPTY_SET;
            return new EnglishAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return super.getStandardTokenStream(strOrig);
        }
    }
}
