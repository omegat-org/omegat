/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015,2021 Hiroshi Miura
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.github.eb4j.dsl.DslResult;
import org.junit.AfterClass;
import org.junit.Test;

import org.omegat.core.dictionaries.LingvoDSL.LingvoDSLDict;

/**
 * Dictionary test
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class LingvoDSLTest {

    private static final File TEST_DICT = new File("test/data/dicts-lingvo/test.dsl");
    private static final File TEST_DICT_DZ = new File("test/data/dicts-lingvo-dz/test.dsl.dz");
    private static final Path TEST_DICT_IDX = Paths.get(Paths.get(TEST_DICT.toURI()) + ".idx");
    private static final Path TEST_DICT_DZ_IDX = Paths.get(Paths.get(TEST_DICT_DZ.toURI()) + ".idx");

    @AfterClass
    public static void cleanUp() {
        try {
            Files.deleteIfExists(TEST_DICT_IDX);
        } catch (IOException ignored) {
        }
        try {
            Files.deleteIfExists(TEST_DICT_DZ_IDX);
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testReadFileDict() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "space";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<p style=\"text-indent: 30px\">Only a single white space on first character</p>\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticle1() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "tab";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<p style=\"text-indent: 30px\">Translation line also can have a single TAB char</p>\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleRussian() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "tool";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<p style=\"text-indent: 30px\">\u0441\u0442\u0430\u043d\u043e\u043a</p>\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleChinese() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "\u4e00\u4e2a\u6837";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("[y\u012B ge y\u00E0ng]&nbsp;\nsame as \u4E00\u6A23|\u4E00\u6837 y\u012B " +
                "y\u00E0ng&nbsp;, the same\n", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleFontStyles() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "italic";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<p>Here is an <span style='font-style: italic'>italic</span> <strong>word</strong>.</p>\n",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticleIndentStyles() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "abandon";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<p style=\"text-indent: 30px\"><strong>1.</strong> \u043E\u0442\u043A\u0430\u0437\u044B\u0432\u0430\u0442\u044C\u0441\u044F" +
                        " (<span style='font-style: italic'>\u043E\u0442 \u0447\u0435\u0433\u043E-\u043B.</span>)," +
                        " \u043F\u0440\u0435\u043A\u0440\u0430\u0449\u0430\u0442\u044C " +
                        "(<span style='font-style: italic'>\u043F\u043E\u043F\u044B\u0442\u043A\u0438 \u0438 \u0442." +
                        " \u043F.</span>)</p>\n" +
                        "<p style=\"text-indent: 30px\"><strong>2.</strong> \u043F\u043E\u043A\u0438\u0434\u0430\u0442\u044C," +
                        " \u043E\u0441\u0442\u0430\u0432\u043B\u044F\u0442\u044C</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon attempts</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon a claim</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon convertibility</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon the [gold] standard</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon price control</p>\n" +
                        "<p style=\"text-indent: 60px\">to abandon a right</p>\n",
                result.get(0).getArticle());
    }

    @Test
    public void testReadFileDictDz() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT_DZ);
        String word = "space";
        DslResult data = dict.data.lookup(word);
        assertNotNull(data);
    }
}
