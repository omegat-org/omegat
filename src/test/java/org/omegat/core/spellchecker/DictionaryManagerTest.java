/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.core.spellchecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.util.OStrings;

/**
 * Proves that the available-languages display aggregates all dictionary
 * sources of one language into a single line naming engine and origin.
 * The flat per-source list showed the same language up to three times with
 * identical labels (bundled Hunspell, bundled Morfologik, installed file).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class DictionaryManagerTest {

    /**
     * Fictitious language codes: provider registrations are static and
     * cannot be removed, so they must never collide with real languages
     * other tests may query.
     */
    private static final String BOTH_SOURCES = "zz_ZZ";
    private static final String FILE_ONLY = "zz_YY";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DictionaryManager manager;

    @Before
    public void setUp() throws Exception {
        File dir = folder.newFolder("dictionaries");
        Files.writeString(new File(dir, BOTH_SOURCES + ".aff").toPath(), "SET UTF-8\n",
                StandardCharsets.UTF_8);
        Files.writeString(new File(dir, BOTH_SOURCES + ".dic").toPath(), "1\nword\n",
                StandardCharsets.UTF_8);
        Files.writeString(new File(dir, FILE_ONLY + ".dict").toPath(), "", StandardCharsets.UTF_8);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider(BOTH_SOURCES,
                SpellCheckDictionaryType.HUNSPELL, "test.Provider");
        SpellCheckerManager.registerSpellCheckerDictionaryProvider(BOTH_SOURCES,
                SpellCheckDictionaryType.MORFOLOGIK, "test.Provider");
        manager = new DictionaryManager(dir);
    }

    @Test
    public void testDisplayListAggregatesSourcesPerLanguage() {
        List<String> display = manager.getLocalDictionaryDisplayList();

        String bothSources = expectedLine(BOTH_SOURCES,
                OStrings.getString("GUI_SPELLCHECKER_SOURCE_INSTALLED", "Hunspell") + ", "
                        + OStrings.getString("GUI_SPELLCHECKER_SOURCE_BUNDLED", "Hunspell + Morfologik"));
        assertEquals("every source of the language must fold into one line", 1,
                display.stream().filter(bothSources::equals).count());

        String fileOnly = expectedLine(FILE_ONLY,
                OStrings.getString("GUI_SPELLCHECKER_SOURCE_INSTALLED", "Morfologik"));
        assertTrue("origin of a lone local file must be named", display.contains(fileOnly));

        // the flat per-source list still carries the duplicates the display
        // list is there to fold
        assertEquals(3, manager.getLocalDictionaryCodeList().stream()
                .filter(c -> c.equals(BOTH_SOURCES)).count());
    }

    private static String expectedLine(String code, String sources) {
        return OStrings.getString("GUI_SPELLCHECKER_LANG_WITH_SOURCES", code,
                DictionaryManager.getLanguageDisplayName(code), sources);
    }
}
