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
package org.omegat.languages.km;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.spellchecker.hunspell.HunSpellChecker;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;


public class HunspellTest {

    private static final String LANGUAGE = "km_KH";
    private static final String GOOD = "រកឃើញ";
    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws IOException {
        PluginUtils.loadPlugins(Collections.emptyMap());
        tmpDir = Files.createTempDirectory("omegat");
        assertThat(tmpDir.toFile()).isDirectory();
        Path configDir = Files.createDirectory(tmpDir.resolve(".omegat"));
        TestPreferencesInitializer.init(configDir.toString());
        Files.createDirectory(configDir.resolve("spelling"));
        FileUtils.forceDeleteOnExit(tmpDir.toFile());
    }

    @Test
    public void testDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language(LANGUAGE));
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        ISpellChecker checker = new HunSpellChecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect(GOOD)).as("Spell check for correct word").isTrue();
    }

}
