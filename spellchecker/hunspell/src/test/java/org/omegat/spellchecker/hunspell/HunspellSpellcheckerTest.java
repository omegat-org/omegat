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

package org.omegat.spellchecker.hunspell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.TestRuntimePreferenceStore;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.TestPreferencesInitializer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HunspellSpellcheckerTest {
    private static final String DICTIONARY_PATH = "/org/omegat/spellchecker/hunspell/";

    private static Path tmpDir;
    private static Path configDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestRuntimePreferenceStore.reset();
        PluginUtils.loadPlugins(Collections.emptyMap());
        tmpDir = Files.createTempDirectory("omegat");
        assertThat(tmpDir.toFile()).isDirectory();
        configDir = Files.createDirectory(tmpDir.resolve(".omegat"));
        TestPreferencesInitializer.init(configDir.toString());
        TestCoreInitializer.initMainWindow(new ConsoleWindow());
        Files.createDirectory(configDir.resolve("spelling"));
        copyFile("es_MX.aff");
        copyFile("es_MX.dic");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtils.forceDeleteOnExit(tmpDir.toFile());
    }

    @Test
    public void testReadHunspellDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("es_MX"));
        setupProject(props, false);
        ISpellChecker checker = new HunSpellChecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Hola")).isTrue();
        assertThat(checker.isCorrect("incorrecti")).isFalse();
        assertThat(checker.suggest("incorrecti")).contains("incorrecto");
    }

    @Test
    public void testReadHunspellLTDictionary() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("de_DE"));
        setupProject(props, false);
        ISpellChecker checker = new HunSpellChecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Hallo")).as("Spell check for correct word").isTrue();
        assertThat(checker.isCorrect("Hello")).as("Spell check for wrong word").isFalse();
        assertThat(checker.suggest("Hello")).as("Get suggestion").hasSize(8).contains("Holle", "Hella",
                "Cello", "Hell", "Helle", "Hallo", "Hellt", "Helot");
    }

    @Test
    public void testBundledDictionaryFR() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("fr_FR"));
        setupProject(props, false);
        ISpellChecker checker = new HunSpellChecker();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.isCorrect("Bonjour")).as("Spell check for correct word").isTrue();
        assertThat(checker.isCorrect("Erruer")).as("Spell check for wrong word").isFalse();
        assertThat(checker.suggest("Erruer")).as("Get suggestion").contains("Erreur", "Errer");
    }

    @Test
    public void testReinitializeWhenProjectChanged() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        props.setTargetLanguage(new Language("fr_FR"));
        setupProject(props, false);
        HunSpellCheckerMock checker = new HunSpellCheckerMock();
        assertThat(checker.initialize()).as("Success initialize").isTrue();
        assertThat(checker.getInitializeCounter()).as("Hunspell Checker initialized once").isEqualTo(1);
        // Fire project change events twice
        final CountDownLatch latch = new CountDownLatch(2);
        CoreEvents.registerProjectChangeListener(eventType -> latch.countDown());
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        latch.await();
        //
        assertThat(checker.getInitializeCounter()).as("Hunspell Checker loaded 3rd times").isEqualTo(3);
    }

    @Test
    public void testSaveWordListsWhenProjectStatusChanged() throws Exception {
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        tmpDir.resolve("omegat").toFile().mkdir();
        props.setTargetLanguage(new Language("fr_FR"));
        verifyWordListsSave(props, false);
        verifyWordListsSave(props, true);
    }

    private void setupProject(ProjectProperties props, boolean isProjectLoaded) {
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }

            @Override
            public boolean isProjectLoaded() {
                return isProjectLoaded;
            }
        });
    }

    private void verifyWordListsSave(ProjectProperties props, boolean isProjectLoaded) throws Exception {
        // Setup project state
        setupProject(props, isProjectLoaded);

        // Initialize spell checker
        HunSpellCheckerMock checker = new HunSpellCheckerMock();
        checker.initialize();

        // Setup and trigger save event
        CountDownLatch latch = new CountDownLatch(1);
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE) {
                latch.countDown();
            }
        });
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
        latch.await();

        // Verify results
        String projectDir = Core.getProject().getProjectProperties().getProjectInternal();
        File wordListFile = Paths.get(projectDir, OConsts.IGNORED_WORD_LIST_FILE_NAME).toFile();

        if (isProjectLoaded) {
            assertTrue(wordListFile.exists());
        } else {
            assertFalse(wordListFile.exists());
        }
    }

    private static void copyFile(String target) throws IOException {
        try (InputStream is = HunspellSpellcheckerTest.class.getResourceAsStream(DICTIONARY_PATH + target)) {
            if (is == null) {
                throw new IOException("Target resource not found");
            }
            Files.copy(is, configDir.resolve("spelling/" + target));
        }

    }

    public static class HunSpellCheckerMock extends HunSpellChecker {
        private int initializeCounter = 0;

        @Override
        public boolean initialize() {
            initializeCounter++;
            return super.initialize();
        }

        public int getInitializeCounter() {
            return initializeCounter;
        }
    }
}
