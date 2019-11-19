/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura
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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.Test;
import org.omegat.core.dictionaries.LingvoDSL.LingvoDSLDict;
import org.omegat.util.Language;

/**
 * Dictionary test
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class LingvoDSLTest {

    private static final Language ENGLISH = new Language(Locale.ENGLISH);
    private static final File TEST_DICT = new File("test/data/dicts-lingvo/test.dsl");

    @Test
    public void testReadFileDict() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT, ENGLISH);
        assertEquals(6, dict.data.size());

        String word = "space";
        List<Entry<String, String>> data = dict.data.lookUp(word);
        assertNotNull(data);
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("Only a single white space on first character\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticle1() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT, ENGLISH);
        String word = "tab";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("Translation line also can have a single TAB char\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleRussian() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT, ENGLISH);
        String word = "tool";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("\u0441\u0442\u0430\u043d\u043e\u043a\n", result.get(0).getArticle());
    }
}
