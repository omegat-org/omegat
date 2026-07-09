/*******************************************************************************
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
 ******************************************************************************/
package org.omegat.languages;

import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.languagetool.JLanguageTool;
import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.languagetools.LanguageDataBroker;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@NullMarked
public class LanguageModuleTestBase {

    private static @Nullable Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws IOException {
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        PluginUtils.loadPlugins(Collections.emptyMap());
        tmpDir = Files.createTempDirectory("omegat");
        assertThat(tmpDir.toFile()).isDirectory();
        Path configDir = Files.createDirectory(tmpDir.resolve(".omegat"));
        TestPreferencesInitializer.init(configDir.toString());
        Files.createDirectory(configDir.resolve("spelling"));
    }

    protected void testDictionaryHelper(ISpellChecker checker, String languageCode, @Nullable String good,
                                        @Nullable String bad) throws Exception {
        if (tmpDir == null) {
            fail();
        }
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language(languageCode));
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        if (good != null) {
            assertThat(checker.isCorrect(good)).as("Spell check for correct word").isTrue();
        }
        if (bad != null) {
            assertThat(checker.isCorrect(bad)).as("Spell check for bad word").isFalse();
        }
    }

    private static final int ATTEMPTS = 5;

    /**
     * Lucene 8.x {@code Hunspell.suggest()} is internally time-bounded: on cold
     * JVMs with the dictionary, the first call(s) can exceed the budget on slow
     * CI runners and return a short or an empty list.
     * Retry a small number of times so warmed-up code paths and caches yield the
     * expected suggestion.
     * A real regression (no expected suggestion ever produced) still fails the
     * test because the final call's result is returned and asserted on.
     */
    public List<String> suggestWithRetry(ISpellChecker checker, String word, int size) {
        List<String> suggestions = Collections.emptyList();
        for (int i = 0; i < ATTEMPTS; i++) {
            suggestions = checker.suggest(word);
            if (suggestions.size() >= size) {
                return suggestions;
            }
        }
        return suggestions;
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        if (tmpDir != null) {
            FileUtils.forceDeleteOnExit(tmpDir.toFile());
        }
    }
}
