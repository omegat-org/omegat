/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.mockito.MockedStatic;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.StaticUtils;
import org.omegat.util.OConsts;
import org.omegat.util.Token;

public class SpellCheckerManagerTest {

    @Test
    public void testGetDefaultDictionaryDir_ReturnsCorrectPath() {
        File expectedDir = new File(StaticUtils.getConfigDir(), OConsts.SPELLING_DICT_DIR);
        File actualDir = SpellCheckerManager.getDefaultDictionaryDir();
        assertEquals(expectedDir.getAbsolutePath(), actualDir.getAbsolutePath());
    }

    @Test
    public void testGetMorfologikDictionaryLanguages() {
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("dummy", SpellCheckDictionaryType.MORFOLOGIK, "DummySpellCheckProvider");

        Set<String> languages = SpellCheckerManager.getMorfologikDictionaryLanguages();
        assertTrue(languages.contains("dummy"));
    }

    @Test
    public void testGetHunspellDictionaryLanguages() {
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("dummy", SpellCheckDictionaryType.HUNSPELL, "DummySpellCheckProvider");

        Set<String> languages = SpellCheckerManager.getHunspellDictionaryLanguages();
        assertTrue(languages.contains("dummy"));
    }

    @Test
    public void testGetCurrentSpellChecker_CustomSpellCheckerInitialized() {
        try (MockedStatic<PluginUtils> pluginUtilsMock = mockStatic(PluginUtils.class)) {
            pluginUtilsMock.when(PluginUtils::getSpellCheckClasses).thenReturn(List.of(CustomSpellChecker.class));
            SpellCheckerManager spyManager = spy(new SpellCheckerManager());

            ISpellChecker checker = spyManager.getCurrentSpellChecker();
            assertNotNull("Spell checker should not be null", checker);
            assertTrue("Spell checker should be an instance of CustomSpellChecker", checker instanceof CustomSpellChecker);
        }
    }

    @Test
    public void testGetCurrentSpellChecker_FallsBackToDummy() {
        try (MockedStatic<PluginUtils> pluginUtilsMock = mockStatic(PluginUtils.class)) {
            pluginUtilsMock.when(PluginUtils::getSpellCheckClasses).thenReturn(Collections.emptyList());
            SpellCheckerManager manager = new SpellCheckerManager();

            ISpellChecker checker = manager.getCurrentSpellChecker();
            assertNotNull("Spell checker should not be null", checker);
            assertTrue("Spell checker should be an instance of SpellCheckerDummy", checker instanceof SpellCheckerDummy);
        }
    }

    public static class CustomSpellChecker implements ISpellChecker {

        @Override
        public boolean initialize() {
            return true;
        }

        @Override
        public void destroy() {
            // do nothing
        }

        @Override
        public void saveWordLists() {
            // do nothing
        }

        @Override
        public boolean isCorrect(String word) {
            return false;
        }

        @Override
        public List<String> suggest(String word) {
            return List.of();
        }

        @Override
        public void learnWord(String word) {
            // do nothing
        }

        @Override
        public void ignoreWord(String word) {
            // do nothing
        }

        @Override
        public List<Token> getMisspelledTokens(String text) {
            return List.of();
        }

        @Override
        public boolean isIgnoredWord(String word) {
            return false;
        }

        @Override
        public boolean isLearnedWord(String word) {
            return false;
        }
    }

}
