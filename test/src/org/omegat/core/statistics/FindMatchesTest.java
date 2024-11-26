/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021-2024 Hiroshi Miura
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

    private static final File TMX_MATCH_EN_CA = new File("test/data/tmx/test-match-stat-en-ca.tmx");
    private static final File TMX_EN_US_SR = new File("test/data/tmx/en-US_sr.tmx");
    private static final File TMX_EN_US_GB_SR = new File("test/data/tmx/en-US_en-GB_fr_sr.tmx");
    private static final File TMX_SEGMENT = new File("test/data/tmx/penalty-010/segment_1.tmx");
    private static Path tmpDir;


    /**
     * Test the case when a translation project is configured in segmented mode,
     * then change to non-segmented translation.
     * <p>
     * This is the case in which the original source text has three sentences.
     * The project is configured in non-segmenting mode.
     * There are three tmx entries for each sentence.
     */
    @Test
    public void testSegmented() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("ca");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IProject project = new TestProject(prop, TMX_MATCH_EN_CA, null, new LuceneEnglishTokenizer(),
                new DefaultTokenizer(), segmenter);
        IStopped iStopped = () -> false;
        String srcText = "This badge is granted when you’ve invited 5 people who subsequently spent enough "
                + "time on the site to become full members. "
                + "Wow! "
                + "Thanks for expanding the diversity of our community with new members!";
        String expectWhole = "Aquesta insígnia es concedeix quan heu convidat 5 persones que posteriorment "
                + "han passat prou temps en ellloc web per a convertir-se en membres plens. "
                + "Bé! "
                + "Gràcies per ampliar la diversitat de la comunitat amb nous membres.";
        String expectFirst = "Aquesta insígnia es concedeix quan heu convidat 5 persones que posteriorment "
                + "han passat prou temps en ellloc web per a convertir-se en membres plens.";
        String expectNear = "Aquesta insígnia es concedeix quan heu convidat 3 persones que posteriorment "
                + "han passat prou temps al lloc web per a convertir-se en usuaris bàsics."
                + " Una comunitat vibrant necessita una entrada regular de nouvinguts que hi participen habitualment"
                + " i aporten veus noves a les converses.\n";
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        // search without a separated segment match.
        List<NearString> result = finder.search(srcText, true, true, iStopped, false, true);
        assertEquals(OConsts.MAX_NEAR_STRINGS, result.size());
        assertEquals(65, result.get(0).scores[0].score);
        assertEquals(62, result.get(0).scores[0].scoreNoStem);
        assertEquals(62, result.get(0).scores[0].adjustedScore);
        assertEquals(expectFirst, result.get(0).translation);
        assertEquals(expectNear, result.get(1).translation);
        // search with a segmented match.
        List<StringBuilder> spaces = new ArrayList<>();
        List<Rule> brules = new ArrayList<>();
        List<String> segments = segmenter.segment(prop.getSourceLanguage(), srcText, spaces, brules);
        assertEquals(3, segments.size());
        finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        result = finder.search(srcText, false, iStopped);
        assertEquals(OConsts.MAX_NEAR_STRINGS, result.size());
        assertEquals("Hit with segmented tmx record", 100, result.get(0).scores[0].score);
        assertEquals(100, result.get(0).scores[0].scoreNoStem);
        assertEquals(100, result.get(0).scores[0].adjustedScore);
        assertEquals(expectWhole, result.get(0).translation);
        assertEquals(65, result.get(1).scores[0].score);
        assertEquals(62, result.get(1).scores[0].scoreNoStem);
        assertEquals(62, result.get(1).scores[0].adjustedScore);
        assertEquals(expectFirst, result.get(1).translation);
        assertEquals(expectNear, result.get(2).translation);
    }

    /**
     * Reproduce and test for RFE#1578.
     * <p>
     * When external TM has different target language, and a source has country
     * code such as "en-US", and a project source is only language code such as
     * "en", and set preference to use another target language, OmegaT shows the
     * source of "en-US" as reference.
     *
     * test conditions:
     * <ul>
     *   <li>header adminlang=en</li>
     *   <li>header srclang=en-US</li>
     *   <li>header segtype=sentence</li>
     *   <li>1st tuv: en-US  value: XXX</li>
     *   <li>2nd tuv: sr     value: YYY</li>
     * </ul>
     */
    @Test
    public void testSearchRFE1578() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cnr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IProject project = new TestProject(prop, null, TMX_EN_US_SR, new LuceneEnglishTokenizer(),
                new DefaultTokenizer(), segmenter);
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        List<NearString> result = finder.search("XXX", false, iStopped);
        // Without the fix, the result has two entries, but it should one.
        assertEquals(1, result.size());
        assertEquals("XXX", result.get(0).source);
        assertEquals("YYY", result.get(0).translation);
    }

    /**
     * Test with tmx file with en-US, en-GB, fr and sr.
     * <p>
     * test conditions:
     * <ul>
     *   <li>header adminlang=en</li>
     *   <li>header srclang=en-US</li>
     *   <li>header segtype=sentence</li>
     *   <li>1st tuv: en-US  value: XXx</li>
     *   <li>2nd tuv: en-GB  value: XXX</li>
     *   <li>3rd tuv: fr     value: YYY</li>
     *   <li>4th tuv: sr     value: ZZZ</li>
     * </ul>
     * project properties:
     * <ul>
     *   <li>source: en</li>
     *   <li>target: cnr</li>
     * </ul>
     */
    @Test
    public void testSearchRFE1578_2() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cnr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IProject project = new TestProject(prop, null, TMX_EN_US_GB_SR, new LuceneEnglishTokenizer(),
                new DefaultTokenizer(), segmenter);
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        // Search source "XXx" in en-US
        List<NearString> result = finder.search("XXX", false, iStopped);
        // There should be three entries.
        assertEquals(3, result.size());
        assertEquals("XXx", result.get(0).source); // should be en-US.
        assertEquals("XXX", result.get(0).translation); // should be en-GB
        assertEquals("YYY", result.get(1).translation); // fr
        assertEquals("ZZZ", result.get(2).translation); // sr
    }

    @Test
    public void testSearchBUGS1251() throws Exception {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("ja");
        prop.setTargetLanguage("fr");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(false);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        IProject project = new TestProject(prop, null, TMX_SEGMENT, new LuceneCJKTokenizer(),
                new LuceneFrenchTokenizer(), segmenter);
        Core.setProject(project);
        SourceTextEntry ste = project.getAllEntries().get(1);
        Language sourceLanguage = prop.getSourceLanguage();
        String srcText = ste.getSrcText();
        List<StringBuilder> spaces = new ArrayList<>();
        List<Rule> brules = new ArrayList<>();
        List<String> segments = segmenter.segment(sourceLanguage, srcText, spaces, brules);
        assertEquals(2, segments.size());
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        List<NearString> result = finder.search(srcText, false, iStopped);
        assertEquals(srcText, result.get(0).source);
        assertEquals(1, result.size());
        assertEquals("TM", result.get(0).comesFrom.name());
        assertEquals(90, result.get(0).scores[0].score);
        assertEquals("weird behavior", result.get(0).translation);
     }

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Before
    public void setUp() throws Exception {
        Core.initializeConsole(new TreeMap<>());
        Core.registerTokenizerClass(DefaultTokenizer.class);
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
        // initialize Preferences and segmentation
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.EXT_TMX_SHOW_LEVEL2, false);
        Preferences.setPreference(Preferences.EXT_TMX_KEEP_FOREIGN_MATCH, true);
    }

    static class TestProject extends NotLoadedProject implements IProject {
        private final ProjectProperties prop;
        private ProjectTMXMock projectTMX;
        private final File externalTmx;
        private final ITokenizer sourceTokenizer;
        private final ITokenizer targetTokenizer;
        private final Segmenter segmenter;

        final ProjectTMX.CheckOrphanedCallback checkOrphanedCallback = new ProjectTMX.CheckOrphanedCallback() {
            public boolean existSourceInProject(String src) {
                return false;
            }
            public boolean existEntryInProject(EntryKey key) {
                return false;
            }
        };

        TestProject(final ProjectProperties prop, File testTmx, File externalTmx,
                    ITokenizer sourceTokenizer, ITokenizer targetTokenizer, Segmenter segmenter) {
            this.prop = prop;
            this.sourceTokenizer = sourceTokenizer;
            this.targetTokenizer = targetTokenizer;
            this.externalTmx = externalTmx;
            this.segmenter = segmenter;
            projectTMX = null;
            if (testTmx != null) {
                try {
                    projectTMX = new ProjectTMXMock(prop.getSourceLanguage(), prop.getTargetLanguage(),
                            prop.isSentenceSegmentingEnabled(), testTmx, checkOrphanedCallback, segmenter);
                } catch (Exception e) {
                    Log.log(e);
                }
            }
       }

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
                    1, null, null, Collections.emptyList()));
            ste.add(new SourceTextEntry(new EntryKey("source.txt", "地力の搾取と浪費が現われる。(1)", null, "", "", null),
                    1, null, null, Collections.emptyList()));
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
        public TMXEntry getTranslationInfo(SourceTextEntry ste) {
            if (projectTMX == null) {
                return null;
            }
            TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
            if (r == null) {
                r = projectTMX.getDefaultTranslation(ste.getSrcText());
            }
            if (r == null) {
                r = EMPTY_TRANSLATION;
            }
            return r;
        }

        @Override
        public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, ExternalTMX> getTransMemories() {
            if (externalTmx == null) {
                return Collections.emptyMap();
            }

            Map<String, ExternalTMX> transMemories = new TreeMap<>();
            try {
                ExternalTMX newTMX = ExternalTMFactory.load(externalTmx, prop, segmenter, null);
                transMemories.put(externalTmx.getPath(), newTMX);
            } catch (Exception ignored) {
            }
            return Collections.unmodifiableMap(transMemories);
        }
    }

    public static class ProjectTMXMock extends ProjectTMX {

        public ProjectTMXMock(Language sourceLanguage, Language targetLanguage,
                           boolean isSentenceSegmentingEnabled,
                          File file, CheckOrphanedCallback callback, Segmenter segmenter) throws Exception {
            super(sourceLanguage, targetLanguage, isSentenceSegmentingEnabled, file, callback, segmenter);
        }

        public Map<String, TMXEntry> getDefaultsMap() {
            return defaults;
        };

        public Map<EntryKey, TMXEntry> getAlternativesMap() {
            return alternatives;
        }
    }

}
