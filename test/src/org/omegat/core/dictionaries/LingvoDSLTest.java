/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura
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

package org.omegat.core.dictionaries;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.omegat.core.dictionaries.LingvoDSL.LingvoDSLDict;

import junit.framework.TestCase;

/**
 * Dictionary test
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class LingvoDSLTest extends TestCase {

    private static final String TEST_DICT = "test/data/dicts-lingvo/test.dsl";

    @Test
    public void testReadFileDict() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(new File(TEST_DICT));
        assertEquals(6, dict.data.size());

        String word = "space";
        String data = dict.data.get(word);
        assertNotNull(data);
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("Only a single white space on first character\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticle1() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(new File(TEST_DICT));
        String word = "tab";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("Translation line also can have a single TAB char\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleRussian() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(new File(TEST_DICT));
        String word = "tool";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("\u0441\u0442\u0430\u043d\u043e\u043a\n", result.get(0).getArticle());
    }
}
