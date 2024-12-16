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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneCJKTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;


public class FindMatchesTest {

    private static final File TMX_EN_US_SR = new File("test/data/tmx/en-US_sr.tmx");
    private static final File TMX_EN_US_GB_SR = new File("test/data/tmx/en-US_en-GB_fr_sr.tmx");
    private static final File TMX_SEGMENT = new File("test/data/tmx/penalty-010/segment_1.tmx");
    private static final File TMX_MULTI = new File("test/data/tmx/test-multiple-entries.tmx");
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
        IProject project = new TestProject(prop, null, TMX_EN_US_SR, new LuceneEnglishTokenizer(), new DefaultTokenizer());
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
        IProject project = new TestProject(prop, null, TMX_EN_US_GB_SR, new LuceneEnglishTokenizer(),
                new DefaultTokenizer());
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

    @Test
    public void testSearchBUGS1251() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("ja");
        prop.setTargetLanguage("fr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        IProject project = new FindMatchesTest.TestProject(prop, null, TMX_SEGMENT, new LuceneCJKTokenizer(),
                new LuceneFrenchTokenizer());
        Core.setProject(project);
        SourceTextEntry ste = project.getAllEntries().get(1);
        Language sourceLanguage = prop.getSourceLanguage();
        String srcText = ste.getSrcText();
        List<StringBuilder> spaces = new ArrayList<>();
        List<Rule> brules = new ArrayList<>();
        List<String> segments = segmenter.segment(sourceLanguage, srcText, spaces, brules);
        assertEquals(2, segments.size());
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, OConsts.MAX_NEAR_STRINGS, true, true, true);
        List<NearString> result = finder.search(srcText, false, false, iStopped);
        assertEquals(srcText, result.get(0).source);
        assertEquals(2, result.size());
        // match normal
        assertEquals("TM", result.get(0).comesFrom.name());
        assertEquals(90, result.get(0).scores[0].score);
        assertEquals("weird behavior", result.get(0).translation);
        assertTrue(result.get(0).projs[0].contains("penalty-010"));
        // match segmented, with penalty
        assertEquals("TM_SUBSEG", result.get(1).comesFrom.name());
        assertEquals(90, result.get(1).scores[0].score);
        assertTrue(result.get(1).projs[0].contains("penalty-010"));
    }

    @Test
    public void testSearchMulti() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en-US");
        prop.setTargetLanguage("co");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(true);
        IProject project = new TestProject(prop, TMX_MULTI, null, new LuceneEnglishTokenizer(),
                new DefaultTokenizer());
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, OConsts.MAX_NEAR_STRINGS, true, true, true);
        List<NearString> result = finder.search("Other", false, false, iStopped);
        assertEquals(3, result.size());
        assertEquals("Other", result.get(0).source);
        assertEquals("Altre", result.get(0).translation); // default
        assertNull(result.get(0).key);
        assertEquals("Altri", result.get(1).translation); // alternative
        assertNotNull(result.get(1).key);
        assertEquals("website/download.html", result.get(1).key.file);
        assertEquals("Other", result.get(2).translation); // source translation
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
        Core.registerTokenizerClass(LuceneFrenchTokenizer.class);
        Core.registerTokenizerClass(LuceneCJKTokenizer.class);
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
    }

    static class TestProject extends NotLoadedProject implements IProject {
        private final ProjectProperties prop;
        private final File externalTmx;
        private final ITokenizer sourceTokenizer;
        private final ITokenizer targetTokenizer;
        private ProjectTMXMock projectTMX;

        public TestProject(ProjectProperties prop, File testTmx, File externalTmx,
                           ITokenizer sourceTokenizer, ITokenizer targetTokenizer) {
            this.prop = prop;
            this.externalTmx = externalTmx;
            this.sourceTokenizer = sourceTokenizer;
            this.targetTokenizer = targetTokenizer;
            if (testTmx != null) {
                try {
                    projectTMX = new ProjectTMXMock(prop.getSourceLanguage(), prop.getTargetLanguage(),
                            prop.isSentenceSegmentingEnabled(), testTmx, checkOrphanedCallback);
                } catch (Exception e) {
                    Log.log(e);
                }
            }
        }

        final ProjectTMX.CheckOrphanedCallback checkOrphanedCallback = new ProjectTMX.CheckOrphanedCallback() {
            public boolean existSourceInProject(String src) {
                return false;
            }
            public boolean existEntryInProject(EntryKey key) {
                return false;
            }
        };

        public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
            if (projectTMX == null) {
                return;
            }
            Map.Entry<String, TMXEntry>[] entries;
            synchronized (checkOrphanedCallback) {
                entries = entrySetToArray(projectTMX.getDefaultsMap().entrySet());
            }
            for (Map.Entry<String, TMXEntry> en : entries) {
                it.iterate(en.getKey(), en.getValue());
            }
        }

        public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
            if (projectTMX == null) {
                return;
            }
            Map.Entry<EntryKey, TMXEntry>[] entries;
            synchronized (checkOrphanedCallback) {
                entries = entrySetToArray(projectTMX.getAlternativesMap().entrySet());
            }
            for (Map.Entry<EntryKey, TMXEntry> en : entries) {
                it.iterate(en.getKey(), en.getValue());
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private <K, V> Map.Entry<K, V>[] entrySetToArray(Set<Map.Entry<K, V>> set) {
            // Assign to variable to facilitate suppressing the rawtypes warning
            Map.Entry[] a = new Map.Entry[set.size()];
            return set.toArray(a);
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
            ste.add(new SourceTextEntry(new EntryKey("source.txt",
                    "\u5730\u529B\u306E\u643E\u53D6\u3068\u6D6A\u8CBB\u304C\u73FE\u308F\u308C\u308B\u3002(1)",
                    null, "", "", null),
                    1, null, null, Collections.emptyList()));
            ste.add(new SourceTextEntry(new EntryKey("website/download.html", "Other", "id",
                    "For installation on Linux.",
                    "For installation on other operating systems (such as FreeBSD and Solaris).&lt;br0/>",
                    null), 1, null, "Other", Collections.emptyList()));
            return ste;
        }

        @Override
        public ITokenizer getSourceTokenizer() {
            return sourceTokenizer;
        };

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
                ExternalTMX newTMX = ExternalTMFactory.load(externalTmx);
                transMemories.put(externalTmx.getPath(), newTMX);
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

    public static class ProjectTMXMock extends ProjectTMX {

        public ProjectTMXMock(Language sourceLanguage, Language targetLanguage,
                           boolean isSentenceSegmentingEnabled,
                          File file, CheckOrphanedCallback callback) throws Exception {
            super(sourceLanguage, targetLanguage, isSentenceSegmentingEnabled, file, callback);
        }

        public Map<String, TMXEntry> getDefaultsMap() {
            return defaults;
        };

        public Map<EntryKey, TMXEntry> getAlternativesMap() {
            return alternatives;
        }
    }
}
