/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.statistics;

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
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;


public class FindMatchesTest {

    private static final File TMX_EN_US_SR = new File("test/data/tmx/en-US_sr.tmx");
    private static final File TMX_EN_US_GB_SR = new File("test/data/tmx/en-US_en-GB_fr_sr.tmx");
    private static Path tmpDir;

    /**
     * Reproduce and test for RFE#1578.
     * <p>
     * When external TM has different target language, and
     * source has country code such as "en-US", and
     * project source is only language code such as "en",
     * and set preference to use other target language,
     * OmegaT show the source of "en-US" as reference.
     *
     * test conditions:
     *   header adminlang=en
     *   header srclang=en-US
     *   header segtype=sentence
     *   1st tuv: en-US  value: XXX
     *   2nd tuv: sr     value: YYY
     */
    @Test
    public void testSearchRFE1578() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cnr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        IProject project = new TestProject(prop, TMX_EN_US_SR);
        Core.setProject(project);
        Core.setSegmenter(new Segmenter(new SRX()));
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, OConsts.MAX_NEAR_STRINGS, true, false);
        List<NearString> result = finder.search("XXX", true, true, iStopped);
        // Without the fix, the result has two entries, but it should one.
        assertEquals(1, result.size());
        assertEquals("XXX", result.get(0).source);
        assertEquals("YYY", result.get(0).translation);
    }

    /**
     * Test with tmx file with en-US, en-GB, fr and sr.
     * <p>
     * test conditions:
     *   header adminlang=en
     *   header srclang=en-US
     *   header segtype=sentence
     *   1st tuv: en-US  value: XXx
     *   2nd tuv: en-GB  value: XXX
     *   3rd tuv: fr     value: YYY
     *   4th tuv: sr     value: ZZZ
     * project properties:
     *   source: en
     *   target: cnr
     *
     */
    @Test
    public void testSearchRFE1578_2() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cnr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        IProject project = new TestProject(prop, TMX_EN_US_GB_SR);
        Core.setProject(project);
        Core.setSegmenter(new Segmenter(new SRX()));
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, OConsts.MAX_NEAR_STRINGS, true, false);
	// Search source "XXx" in en-US
        List<NearString> result = finder.search("XXX", true, true, iStopped);
        // There should be three entries.
        assertEquals("XXx", result.get(0).source);  // should be en-US.
        assertEquals("XXX", result.get(0).translation); // should be en-GB
        assertEquals("YYY", result.get(1).translation); // fr
        assertEquals("ZZZ", result.get(2).translation); // sr
        assertEquals(3, result.size());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Before
    public void setUp() throws Exception {
        Core.initializeConsole(new TreeMap<>());
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.EXT_TMX_SHOW_LEVEL2, false);
        Preferences.setPreference(Preferences.EXT_TMX_USE_SLASH, false);
        Preferences.setPreference(Preferences.EXT_TMX_KEEP_FOREIGN_MATCH, true);
        Core.registerTokenizerClass(DefaultTokenizer.class);
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
    }

    static class TestProject extends NotLoadedProject implements IProject {
        private ProjectProperties prop;
        private File testTmx;

        public TestProject(final ProjectProperties prop, final File testTmx) {
            this.prop = prop;
            this.testTmx = testTmx;
        }

        @Override
        public ProjectProperties getProjectProperties() {
            return prop;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            List<SourceTextEntry> ste = new ArrayList<>();
            ste.add(new SourceTextEntry(new EntryKey("source.txt", "XXX", null, "", "", null),
                    1, null, null, new ArrayList<>()));
            return ste;
        }

        @Override
        public ITokenizer getSourceTokenizer() {
            return new LuceneEnglishTokenizer();
        };

        @Override
        public ITokenizer getTargetTokenizer() {
            return new DefaultTokenizer();
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
