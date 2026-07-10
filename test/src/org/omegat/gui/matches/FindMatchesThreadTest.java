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

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneCJKTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

public class FindMatchesThreadTest {
    private static final File TMX_SEGMENT = new File("test/data/tmx/penalty-010/segment_1.tmx");
    private static final String SOURCE_TEXT = "地力の搾取と浪費が現われる。(1)";
    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Before
    public void setUp() throws Exception {
        TestCoreState.resetState();
        Core.initializeConsole();
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.EXT_TMX_SHOW_LEVEL2, false);
        Preferences.setPreference(Preferences.EXT_TMX_USE_SLASH, false);
        Preferences.setPreference(Preferences.EXT_TMX_KEEP_FOREIGN_MATCH, true);
        Core.registerTokenizerClass(DefaultTokenizer.class);
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
    }

    @Test
    public void testSearchBUGS1248() {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("ja");
        prop.setTargetLanguage("fr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        IProject project = new TestProject(prop, TMX_SEGMENT, new LuceneCJKTokenizer(),
                new LuceneFrenchTokenizer());
        TestCoreState.getInstance().setProject(project);
        TestCoreState.getInstance().setSegmenter(new Segmenter(SRX.getDefault()));
        List<NearString> result = FindMatchesThread.finderSearch(project, SOURCE_TEXT, () -> false,
                30);
        assertEquals(2, result.size());
        //
        assertEquals(SOURCE_TEXT, result.get(0).source);
        assertEquals("TM", result.get(0).comesFrom.name());
        assertEquals(90, result.get(0).scores[0].score);
        assertEquals("weird behavior", result.get(0).translation);
        //
        assertEquals(SOURCE_TEXT, result.get(1).source);
        assertEquals("SUBSEGMENTS", result.get(1).comesFrom.name());
        assertEquals(90, result.get(1).scores[0].score);
    }

    static class TestProject extends NotLoadedProject implements IProject {
        private final ProjectProperties prop;
        private final File testTmx;
        private final ITokenizer sourceTokenizer;
        private final ITokenizer targetTokenizer;

        TestProject(ProjectProperties prop, File testTmx, ITokenizer source, ITokenizer target) {
            this.prop = prop;
            this.testTmx = testTmx;
            sourceTokenizer = source;
            targetTokenizer = target;
        }

        @Override
        public ProjectProperties getProjectProperties() {
            return prop;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            List<SourceTextEntry> ste = new ArrayList<>();
            ste.add(new SourceTextEntry(new EntryKey("source.txt", SOURCE_TEXT, null, "", "", null),
                    1, null, null, Collections.emptyList()));
            return ste;
        }

        @Override
        public ITokenizer getSourceTokenizer() {
            return sourceTokenizer;
        }

        @Override
        public ITokenizer getTargetTokenizer() {
            return targetTokenizer;
        }

        @Override
        public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, ExternalTMX> getTransMemories() {
            Map<String, ExternalTMX> transMemories = new TreeMap<>();
            try {
                ExternalTMX newTMX = ExternalTMFactory.load(testTmx);
                transMemories.put(testTmx.getPath(), newTMX);
            } catch (Exception ignored) {
            }
            return Collections.unmodifiableMap(transMemories);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
        assertFalse(tmpDir.toFile().exists());
    }
}
