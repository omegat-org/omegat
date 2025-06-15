/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.hunspell.Dictionary;
import org.junit.Before;
import org.junit.Test;

import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;

public class HunspellTokenizerTest extends TokenizerTestBase {

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    /**
     * Test en_GB dictionary with stop-words from Lucene.
     */
    @Test
    public void testHunspellEnglish() {
        ITokenizer tok = new HunspellTokenizerMock(new Language("en_GB"));
        String orig = "The quick, brown <x0/> jumped over 1 \"lazy\" dog.";
        assertVerbatim(
                new String[] { "The", " ", "quick", ",", " ", "brown", " ", "<x0/>", " ", "jumped", " ",
                        "over", " ", "1", " ", "\"", "lazy", "\"", " ", "dog", "." },
                tok.tokenizeVerbatimToStrings(orig), tok.tokenizeVerbatim(orig), orig);
        assertResult(new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, ITokenizer.StemmingMode.NONE));
        assertResult(new String[] { "the", "quick", "brown", "x0", "jump", "jumped", "over", "1", "lazy",
                "laze", "lazy", "dog" }, tok.tokenizeWordsToStrings(orig, ITokenizer.StemmingMode.GLOSSARY));
        assertResult(
                new String[] { "quick", "brown", "jump", "jumped", "over", "lazy", "laze", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, ITokenizer.StemmingMode.MATCHING));
    }

    /**
     * Test es dictionary with stop-words from Lucene.
     */
    @Test
    public void testHunspellSpanish() {
        ITokenizer tok = new HunspellTokenizerMock(new Language("es"));
        String orig = "El rápido <e0/> marrón saltó sobre 1 perro perezoso.";
        assertResult(new String[] {"rápido", "marrón", "saltar", "saltó", "sobrar", "sobre", "perro",
                "perezoso"}, tok.tokenizeWordsToStrings(orig, ITokenizer.StemmingMode.MATCHING));
    }

    /**
     * Test vi dictionary with stop-words bundled.
     */
    @Test
    public void testHunspellVietnamese() {
        ITokenizer tok = new HunspellTokenizerMock(new Language("vi"));
        String orig = "<e0/> nâu, nhanh nhẹn nhảy qua 1 lười biếng.";
        assertResult(new String[] {"nâu", "nhanh", "nhẹn", "nhảy", "lười", "biếng"},
                tok.tokenizeWordsToStrings(orig, ITokenizer.StemmingMode.MATCHING));
    }

    /**
     * Mock to load test dictionary.
     */
    @Tokenizer(languages = { Tokenizer.DISCOVER_AT_RUNTIME })
    public static class HunspellTokenizerMock extends HunspellTokenizer {
        private final Language language;

        public HunspellTokenizerMock(Language language) {
            this.language = language;
        }

        @Override
        protected Language getEffectiveLanguage() {
            return language;
        }

        @Override
        protected Dictionary getDict() {
            File affixFile;
            File dictionaryFile;
            if (language.isSameLanguage("en_GB")) {
                affixFile = new File("test/data/spelldicts/en_GB.aff");
                dictionaryFile = new File("test/data/spelldicts/en_GB.dic");
            } else if (language.isSameLanguage("es")) {
                affixFile = new File("test/data/spelldicts/es.aff");
                dictionaryFile = new File("test/data/spelldicts/es.dic");
            } else if (language.isSameLanguage("vi")) {
                affixFile = new File("test/data/spelldicts/vi.aff");
                dictionaryFile = new File("test/data/spelldicts/vi.dic");
            } else {
                return null;
            }
            try {
                return new Dictionary(new FileInputStream(affixFile),
                        new FileInputStream(dictionaryFile));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
