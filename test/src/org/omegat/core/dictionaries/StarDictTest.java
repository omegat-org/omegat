/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Hiroshi Miura, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.dictionaries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.github.eb4j.stardict.StarDictDictionary;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Dictionary test
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class StarDictTest {

    private static final Language FRENCH = new Language(Locale.FRENCH);

    @BeforeClass
    public static void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.DICTIONARY_CONDENSED_VIEW, false);
    }

    @Test
    public void testStardict4j() throws Exception {
        StarDictDictionary dict = StarDictDictionary.loadDictionary(
                new File("test/data/dicts/latin-francais.ifo"));
        assertEquals(10451, dict.getInformation().getWordCount());

        String word = "testudo";
        List<StarDictDictionary.Entry> result = dict.readArticles(word);
        assertEquals(1, result.size());
        assertEquals(word, result.get(0).getWord());
        assertEquals("dinis, f. : tortue", result.get(0).getArticle());

        // Test prediction
        word = "testu";
        result = dict.readArticles(word);
        assertTrue(result.isEmpty());
        result = dict.readArticlesPredictive(word);
        assertEquals(1, result.size());
        assertEquals("testudo", result.get(0).getWord());
        assertEquals("dinis, f. : tortue", result.get(0).getArticle());
    }

    @Test
    public void testReadFileDict() throws Exception {
        StarDict.StarDictDict dict = new StarDict.StarDictDict(
                new File("test/data/dicts/latin-francais.ifo"), FRENCH);

        String word = "testudo";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertEquals(1, result.size());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div>dinis, f. : tortue</div>", result.get(0).getArticle());

        // Test case normalization
        word = word.toUpperCase(FRENCH.getLocale());
        result = dict.readArticles(word);
        assertEquals(1, result.size());
        assertEquals("testudo", result.get(0).getWord());
        assertEquals("<div>dinis, f. : tortue</div>", result.get(0).getArticle());

        // Test prediction
        word = "testu";
        result = dict.readArticles(word);
        assertTrue(result.isEmpty());
        result = dict.readArticlesPredictive(word);
        assertEquals(1, result.size());
        assertEquals("testudo", result.get(0).getWord());
        assertEquals("<div>dinis, f. : tortue</div>", result.get(0).getArticle());
    }

    @Test
    public void testReadZipDict() throws Exception {
        StarDict.StarDictDict dict = new StarDict.StarDictDict(
                new File("test/data/dicts-zipped/latin-francais.ifo"), FRENCH);
        String word = "testudo";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertEquals(1, result.size());
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div>dinis, f. : tortue</div>", result.get(0).getArticle());
    }
}
