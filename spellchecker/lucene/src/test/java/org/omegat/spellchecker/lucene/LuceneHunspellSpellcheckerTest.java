/*******************************************************************************
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
******************************************************************************/

package org.omegat.spellchecker.lucene;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LuceneHunspellSpellcheckerTest {
    private static final String DICTIONARY_PATH = "/org/omegat/spellchecker/lucene/";

    private Path tmpDir;
    private Path configDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertThat(tmpDir.toFile()).isDirectory();
        configDir = Files.createDirectory(tmpDir.resolve(".omegat"));
        TestPreferencesInitializer.init(configDir.toString());
        Files.createDirectory(configDir.resolve("spelling"));
        copyFile("en.aff");
        copyFile("en.dic");
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.forceDeleteOnExit(tmpDir.toFile());
    }

    @Test
    public void testReadHunspellDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("en"));
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        ISpellChecker checker = new LuceneHunSpellChecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Hello")).isTrue();
        assertThat(checker.isCorrect("incorrecti")).isFalse();
        assertThat(checker.suggest("incorrecti")).contains("incorrect");
    }

    private void copyFile(String target) throws IOException {
        try (InputStream is = LuceneHunspellSpellcheckerTest.class.getResourceAsStream(DICTIONARY_PATH + target)) {
            if (is == null) {
                throw new IOException("Target resource not found");
            }
            Files.copy(is, configDir.resolve("spelling/" + target));
        }

    }
}
