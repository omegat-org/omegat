/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022 Hiroshi Miura
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *************************************************************************
 *
 */

package org.omegat.core.dictionaries;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.gui.dictionaries.IDictionaries;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Language;

public class OnlineDictionaryTest {

    private static final String HEAD_WORD = "head_word";
    private static final String ARTICLE = "article";
    private static final String ARTICLE2 = "article2";
    private static final int EXPECTED_NUM_RESULT = 4;

    private DictionariesManager manager;
    private IDictionary mock1;
    private IDictionary mock2;

    /**
     * Test online dictionary API with mocks.
     * <p>
     *    A test with two mock drivers with two query words.
     *    Drivers returns tow results in thread, so we get
     *    four results.
     *    One mock returns head words equals with query words.
     *    The other returns a static head word.
     */
    @Test
    public void onlineDictionaryTest() {
        List<String> words = new ArrayList<>();
        words.add("testudo");
        words.add("tete");
        List<DictionaryEntry> result = manager.findWords(words);
        assertEquals(EXPECTED_NUM_RESULT, result.size());
        // assert variations
        for (DictionaryEntry en: result) {
            if (en.getWord().equals(en.getQuery())) {
                assertEquals(ARTICLE, en.getArticle());
            } else {
                assertEquals(HEAD_WORD, en.getWord());
                assertEquals(ARTICLE2, en.getArticle());
            }
        }
    }

    /**
     * Test DictionaryManager#addOnlineDictionary interface.
     * @throws Exception when error occurred.
     */
    @Before
    public final void setUp() throws Exception {
        mock1 = new OnlineDictionaryMock();
        mock2 = new OnlineDictionaryMock2();
        manager = new DictionariesManager(new IDictionaries() {
            @Override
            public void refresh() {
            }
            @Override
            public void addDictionaryFactory(IDictionaryFactory factory) {
            }
            @Override
            public void removeDictionaryFactory(IDictionaryFactory factory) {
            }
            @Override
            public void addDictionary(IDictionary dictionary) {
            }
            @Override
            public void removeDictionary(IDictionary dictionary) {
            }
            @Override
            public void searchText(String text) {
            }
        }) {
            @Override
            public boolean doFuzzyMatching() {
                return true;
            }
        };
        manager.setIndexLanguage(new Language(Locale.ENGLISH));
        manager.setTokenizer(new DefaultTokenizer());
        manager.addOnlineDictionary(mock1);
        manager.addOnlineDictionary(mock2);
    }

    /**
     * Test DictionaryManager#removeOnlineDictionary interface.
     */
    @After
    public void tearDown() {
        manager.removeOnlineDictionary(mock1);
        manager.removeOnlineDictionary(mock2);
    }

    public static final class OnlineDictionaryMock2 extends OnlineDictionaryMock implements IDictionary {
        private static final int DELAY = 10;
        @Override
        public List<DictionaryEntry> retrieveArticles(final Collection<String> words) throws Exception {
            List<DictionaryEntry> result = new ArrayList<>();
            for (String word: words) {
                result.add(new DictionaryEntry(word, HEAD_WORD, ARTICLE2));
            }
            // insert 10ms delay like as online query
            Thread.sleep(DELAY);
            return result;
        }

    }

    public static class OnlineDictionaryMock implements IDictionary {
        /**
         * Read article's text.
         *
         * @param word The word to look up in the dictionary
         * @return List of entries. May be empty, but cannot be null.
         */
        @Override
        public List<DictionaryEntry> readArticles(final String word) throws Exception {
            return retrieveArticles(Collections.singletonList(word));
        }

        /**
         * Retrieve article's text.
         * @param words query words
         * @return list of results.
         * @throws Exception when network error occurred.
         */
        @Override
        public List<DictionaryEntry> retrieveArticles(final Collection<String> words) throws Exception {
            List<DictionaryEntry> result = new ArrayList<>();
            for (String word: words) {
                result.add(new DictionaryEntry(word, word, ARTICLE));
            }
            return result;
        }

        /**
         * Retrieve article's text with predictive word query.
         * @param words query words.
         * @return list of results.
         * @throws Exception when network error occurred.
         */
        @Override
        public List<DictionaryEntry> retrieveArticlesPredictive(final Collection<String> words)
                throws Exception {
            return retrieveArticles(words);
        }

        /**
         * Dispose IDictionary. Default is no action.
         */
        @Override
        public void close() throws IOException {
        }
    }
}
