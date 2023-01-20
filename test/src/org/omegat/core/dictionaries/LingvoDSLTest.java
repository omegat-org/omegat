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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.github.eb4j.dsl.DslResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.dictionaries.LingvoDSL.LingvoDSLDict;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Dictionary test
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class LingvoDSLTest {

    private final static String LINE_SEPARATOR = System.lineSeparator();

    private static final File TEST_DICT = new File("test/data/dicts-lingvo/test.dsl");
    private static final File TEST_DICT_DZ = new File("test/data/dicts-lingvo-dz/test.dsl.dz");
    private static final Path TEST_DICT_IDX = Paths.get(Paths.get(TEST_DICT.toURI()) + ".idx");
    private static final Path TEST_DICT_DZ_IDX = Paths.get(Paths.get(TEST_DICT_DZ.toURI()) + ".idx");

    @BeforeClass
    public static void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.DICTIONARY_CONDENSED_VIEW, false);
    }

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
    public void testIsSupported() throws Exception {
        assertTrue(new LingvoDSL().isSupportedFile(TEST_DICT));
        assertFalse(new LingvoDSL().isSupportedFile(TEST_DICT_IDX.toFile()));
    }

    @Test
    public void testReadFileDict() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "space";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div style=\"text-indent: 30px\">Only a single white space on first character</div>",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticle1() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "tab";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div style=\"text-indent: 30px\">Translation line also can have a single TAB char</div>",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticle2() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "ta";
        List<DictionaryEntry> result = dict.readArticlesPredictive(word);
        assertEquals(2, result.size());  // tag and tab
        assertTrue(result.get(0).getWord().startsWith(word));
        for (DictionaryEntry entry: result) {
            if (entry.getWord().equals("tab")) {
                assertEquals("<div style=\"text-indent: 30px\">Translation line also can have a single TAB char</div>",
                        entry.getArticle());
            }
        }
    }

    @Test
    public void testReadArticleRussian() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "tool";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div style=\"text-indent: 30px\">\u0441\u0442\u0430\u043d\u043e\u043a</div>",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticleChinese() throws Exception {
        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "\u4e00\u4e2a\u6837";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals("\u4E00\u500B\u6A23"+ LINE_SEPARATOR + "\u4E00\u4E2A\u6837",
                result.get(0).getWord());
        assertEquals("[y\u012B ge y\u00E0ng]&nbsp;\nsame as \u4E00\u6A23|\u4E00\u6837 y\u012B " +
                "y\u00E0ng&nbsp;, the same", result.get(0).getArticle());
    }

    @Test
    public void testReadArticleFontStyles() throws Exception {
        TestPreferencesInitializer.init();

        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "italic";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div>Here is an <span style='font-style: italic'>italic</span> <strong>word</strong>.</div>",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticleIndentStyles() throws Exception {
        TestPreferencesInitializer.init();

        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "abandon";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<div style=\"text-indent: 30px\"><strong>1.</strong>"
                        + " \u043E\u0442\u043A\u0430\u0437\u044B\u0432\u0430\u0442\u044C\u0441\u044F"
                        + " (<span style='font-style: italic'>"
                        + "\u043E\u0442 \u0447\u0435\u0433\u043E-\u043B.</span>),"
                        + " \u043F\u0440\u0435\u043A\u0440\u0430\u0449\u0430\u0442\u044C "
                        + "(<span style='font-style: italic'>"
                        + "\u043F\u043E\u043F\u044B\u0442\u043A\u0438 \u0438 \u0442."
                        + " \u043F.</span>)</div>\n"
                        + "<div style=\"text-indent: 30px\"><strong>2.</strong>"
                        + " \u043F\u043E\u043A\u0438\u0434\u0430\u0442\u044C,"
                        + " \u043E\u0441\u0442\u0430\u0432\u043B\u044F\u0442\u044C</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon attempts</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon a claim</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon convertibility</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon the [gold] standard</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon price control</div>\n"
                        + "<div style=\"text-indent: 60px\">to abandon a right</div>",
                result.get(0).getArticle());
    }

    @Test
    public void testReadArticleDetails() throws Exception {
        TestPreferencesInitializer.init();

        LingvoDSLDict dict = (LingvoDSLDict) new LingvoDSL().loadDict(TEST_DICT);
        String word = "clear";
        List<DictionaryEntry> result = dict.readArticles(word);
        assertFalse(result.isEmpty());
        assertEquals(word, result.get(0).getWord());
        assertEquals("<strong>1.</strong> [kl\u0131\u0259] <span style='font-style: italic'>"
                        + "<span style=\"color: green\">a</span></span>"
                        + " <div style=\"text-indent: 60px\">1. \u044F\u0441\u043D\u044B\u0439,"
                        + " \u0441\u0432\u0435\u0442\u043B\u044B\u0439 </div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ day</span> - \u044F\u0441\u043D\u044B\u0439"
                        + " \u0434\u0435\u043D\u044C </span></div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ sky</span> - \u0447\u0438\u0441\u0442\u043E\u0435"
                        + " /\u044F\u0441\u043D\u043E\u0435,"
                        + " \u0431\u0435\u0437\u043E\u0431\u043B\u0430\u0447\u043D\u043E\u0435/"
                        + " \u043D\u0435\u0431\u043E </span></div>\n"
                        + "<div style=\"text-indent: 60px\">2. 1) \u0447\u0438\u0441\u0442\u044B\u0439,"
                        + " \u043F\u0440\u043E\u0437\u0440\u0430\u0447\u043D\u044B\u0439 </div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ water of the lake</span>"
                        + " - \u0447\u0438\u0441\u0442\u0430\u044F /\u043F\u0440\u043E\u0437\u0440\u0430"
                        + "\u0447\u043D\u0430\u044F/ \u0432\u043E\u0434\u0430 \u043E\u0437\u0435\u0440\u0430"
                        + " </span></div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ glass</span> - \u043F\u0440\u043E\u0437\u0440\u0430"
                        + "\u0447\u043D\u043E\u0435 \u0441\u0442\u0435\u043A\u043B\u043E </span></div>\n"
                        + "<div style=\"text-indent: 60px\">"
                        + "2) \u0437\u0435\u0440\u043A\u0430\u043B\u044C\u043D\u044B\u0439 "
                        + "(<span style='font-style: italic'>\u043E \u043F\u043E\u0432\u0435\u0440\u0445"
                        + "\u043D\u043E\u0441\u0442\u0438</span>) </div>\n"
                        + "<div style=\"text-indent: 60px\">3. \u043E\u0442\u0447\u0451\u0442\u043B\u0438"
                        + "\u0432\u044B\u0439, \u044F\u0441\u043D\u044B\u0439 </div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ outline</span>"
                        + " - \u044F\u0441\u043D\u043E\u0435 /\u043E\u0442\u0447\u0451\u0442\u043B\u0438"
                        + "\u0432\u043E\u0435/ \u043E\u0447\u0435\u0440\u0442\u0430\u043D\u0438\u0435 </span>"
                        + "</div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ sight</span> - \u0445\u043E\u0440\u043E\u0448\u0435"
                        + "\u0435 \u0437\u0440\u0435\u043D\u0438\u0435 </span></div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ reflection in the water</span>"
                        + " - \u044F\u0441\u043D\u043E\u0435 \u043E\u0442\u0440\u0430\u0436\u0435\u043D"
                        + "\u0438\u0435 \u0432 \u0432\u043E\u0434\u0435 </span></div>\n"
                        + "<div style=\"text-indent: 90px\"><span class=\"details\">"
                        + "<span class=\"lang_en\">~ view</span> - \u0445\u043E\u0440\u043E\u0448\u0430\u044F"
                        + " \u0432\u0438\u0434\u0438\u043C\u043E\u0441\u0442\u044C </span></div>",
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
