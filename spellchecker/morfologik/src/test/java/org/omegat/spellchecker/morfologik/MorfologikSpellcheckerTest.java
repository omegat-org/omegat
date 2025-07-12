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

package org.omegat.spellchecker.morfologik;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.plugin.PluginManager;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;


public class MorfologikSpellcheckerTest {
    private static final String DICTIONARY_PATH = "/org/omegat/spellchecker/morfologik/";

    private static Path tmpDir;
    private static Path configDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PluginManager.loadPlugins();
        tmpDir = Files.createTempDirectory("omegat");
        assertThat(tmpDir.toFile()).isDirectory();
        configDir = Files.createDirectory(tmpDir.resolve(".omegat"));
        TestPreferencesInitializer.init(configDir.toString());
        Files.createDirectory(configDir.resolve("spelling"));
        copyFile("de_DE.dict");
        copyFile("de_DE.info");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtils.forceDeleteOnExit(tmpDir.toFile());
    }

    @Test
    public void testReadMorfologikDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("de_DE"));
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        ISpellChecker checker = new MorfologikSpellchecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Hallo")).as("Spell check for correct word").isTrue();
        assertThat(checker.isCorrect("Hello")).as("Spell check for wrong word").isFalse();
        assertThat(checker.suggest("Hello")).as("Get suggestion")
                .hasSize(3).contains("Hell o", "Hella", "Cello");
    }

    @Test
    public void testLTBundledDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("en_AU"));
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        ISpellChecker checker = new MorfologikSpellchecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Hallo")).as("Spell check for correct word").isFalse();
        assertThat(checker.suggest("Hallo")).as("Get suggestion")
                .hasSize(10).contains("hello");

    }

    private static void copyFile(String target) throws IOException {
        try (InputStream is = MorfologikSpellcheckerTest.class.getResourceAsStream(DICTIONARY_PATH + target)) {
            if (is == null) {
                throw new IOException("Target resource not found");
            }
            Files.copy(is, configDir.resolve("spelling/" + target));
        }
    }
}
