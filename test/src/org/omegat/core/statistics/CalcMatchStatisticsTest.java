/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.omegat.core.statistics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.po.PoFilter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

public class CalcMatchStatisticsTest {


    @Test
    public void testCalcMatchStatics() {
        int threshold = Preferences.getPreferenceDefault(Preferences.EXT_TMX_FUZZY_MATCH_THRESHOLD,
                OConsts.FUZZY_MATCH_THRESHOLD);
        Assert.assertEquals(30, threshold);
        IStatsConsumer callback = new TestStatsConsumer();
        CalcMatchStatisticsMock calcMatchStatistics = new CalcMatchStatisticsMock(callback);
        calcMatchStatistics.start();
        while (calcMatchStatistics.isAlive()) {
            calcMatchStatistics.checkInterrupted();
        }
        String[][] result = calcMatchStatistics.getTable();
        Assert.assertNotNull(result);

        // assertions
        // RowRepetitions  11 90 509 583
        Assert.assertEquals("11", result[0][1]);
        Assert.assertEquals("90", result[0][2]);
        Assert.assertEquals("509", result[0][3]);
        Assert.assertEquals("583", result[0][4]);
        // RowExactMatch 0 0 0 0
        Assert.assertEquals("0", result[1][1]);
        Assert.assertEquals("0", result[1][2]);
        Assert.assertEquals("0", result[1][3]);
        Assert.assertEquals("0", result[1][4]);
        // RowMatch95 84 712 3606 4225
        Assert.assertEquals("84", result[2][1]);
        Assert.assertEquals("712", result[2][2]);
        Assert.assertEquals("3606", result[2][3]);
        Assert.assertEquals("4225", result[2][4]);
        // RowMatch85 0 0 0 0
        Assert.assertEquals("0", result[3][1]);
        Assert.assertEquals("0", result[3][2]);
        Assert.assertEquals("0", result[3][3]);
        Assert.assertEquals("0", result[3][4]);
        // RowMatch75 3 32 234 256
        Assert.assertEquals("3", result[4][1]);
        Assert.assertEquals("32", result[4][2]);
        Assert.assertEquals("234", result[4][3]);
        Assert.assertEquals("256", result[4][4]);
        // RowMatch50 4 61 304 361
        Assert.assertEquals("4", result[5][1]);
        Assert.assertEquals("61", result[5][2]);
        Assert.assertEquals("304", result[5][3]);
        Assert.assertEquals("361", result[5][4]);
        // RowNoMatch 6 43 241 274
        Assert.assertEquals("6", result[6][1]);
        Assert.assertEquals("43", result[6][2]);
        Assert.assertEquals("241", result[6][3]);
        Assert.assertEquals("274", result[6][4]);
        // Total 108 938 4894 5699
        Assert.assertEquals("108", result[7][1]);
        Assert.assertEquals("938", result[7][2]);
        Assert.assertEquals("4894", result[7][3]);
        Assert.assertEquals("5699", result[7][4]);
    }

    /*
     * Setup test project.
     */

    @Before
    public final void setUp() throws Exception {
        Core.initializeConsole(Collections.emptyMap());
        TestPreferencesInitializer.init();
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        Core.setProject(new TestProject(new ProjectPropertiesTest()));
    }

    protected static class ProjectPropertiesTest extends ProjectProperties {
        ProjectPropertiesTest() {
            super();
            setSourceLanguage(new Language("en"));
            setSourceTokenizer(LuceneEnglishTokenizer.class);
            setSentenceSegmentingEnabled(false);
            setTargetLanguage(new Language("ca"));
            setTargetTokenizer(DefaultTokenizer.class);
        }
    }

    static class TestProject extends NotLoadedProject implements IProject {
        private final ProjectProperties prop;

        private final ProjectTMX projectTMX;
        private Map<String, ExternalTMX> transMemories;

        TestProject(ProjectProperties prop) throws Exception {
            super();
            this.prop = prop;
            projectTMX = new ProjectTMX(new Language("en"), new Language("ca"), true,
                    Paths.get("test/data/tmx/empty.tmx").toFile(), null);
        }

        @Override
        public ProjectProperties getProjectProperties() {
            return prop;
        }

        @Override
        public TMXEntry getTranslationInfo(SourceTextEntry ste) {
            if (projectTMX == null) {
                return EMPTY_TRANSLATION;
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
        public List<SourceTextEntry> getAllEntries() {
            List<SourceTextEntry> ste = new ArrayList<>();
            IFilter filter = new PoFilter();
            Path testSource = Paths.get("test/data/filters/po/file-POFilter-match-stat-en-ca.po");
            IParseCallback testCallback = new TestCallback(ste);
            FilterContext context = new FilterContext(new Language("en"), new Language("ca"), true);
            try {
                filter.parseFile(testSource.toFile(), Collections.emptyMap(), context, testCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        public AllTranslations getAllTranslations(SourceTextEntry ste) {
            TestAllTranslations r = new TestAllTranslations();
            synchronized (projectTMX) {
                r.setDefaultTranslation(projectTMX.getDefaultTranslation(ste.getSrcText()));
                r.setAlternativeTranslation(projectTMX.getMultipleTranslation(ste.getKey()));
                if (r.getAlternativeTranslation() != null) {
                    r.setCurrentTranslation(r.getAlternativeTranslation());
                } else if (r.getDefaultTranslation() != null) {
                    r.setCurrentTranslation(r.getDefaultTranslation());
                } else {
                    r.setCurrentTranslation(EMPTY_TRANSLATION);
                }
                if (r.getDefaultTranslation() == null) {
                    r.setDefaultTranslation(EMPTY_TRANSLATION);
                }
                if (r.getAlternativeTranslation() == null) {
                    r.setAlternativeTranslation(EMPTY_TRANSLATION);
                }
            }
            return r;
        }

        @Override
        public Map<String, ExternalTMX> getTransMemories() {
            synchronized (projectTMX) {
                if (transMemories == null) {
                     transMemories = new TreeMap<>();
                    try {
                        ExternalTMX newTMX;
                        Path testTmx = Paths.get("test/data/tmx/test-match-stat-en-ca.tmx");
                        newTMX = ExternalTMFactory.load(testTmx.toFile());
                        transMemories.put(testTmx.toString(), newTMX);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return Collections.unmodifiableMap(transMemories);
        }
    }

    static class TestAllTranslations extends IProject.AllTranslations {
        public void setAlternativeTranslation(TMXEntry entry) {
            alternativeTranslation = entry;
        }

        public void setDefaultTranslation(TMXEntry entry) {
            defaultTranslation = entry;
        }

        public void setCurrentTranslation(TMXEntry entry) {
            currentTranslation = entry;
        }
    }

    static class TestCallback implements IParseCallback {

        private final List<SourceTextEntry> steList;

        TestCallback(final List<SourceTextEntry> ste) {
            this.steList = ste;
        }

        @Override
        public void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy,
                                           String[] props, String path, IFilter filter,
                                           List<ProtectedPart> protectedParts) {
            SourceTextEntry ste = new SourceTextEntry(new EntryKey("source.po", source, id, "", "", path),
                    1, props, translation, protectedParts);
            ste.setSourceTranslationFuzzy(isFuzzy);
            steList.add(ste);
        }

        @Override
        public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                             String path, IFilter filter, List<ProtectedPart> protectedParts) {
            List<String> propList = new ArrayList<>(2);
            if (comment != null) {
                propList.add("comment");
                propList.add(comment);
            }
            String[] props = propList.toArray(new String[0]);
            addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
        }

        @Override
        public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                             IFilter filter) {
            addEntry(id, source, translation, isFuzzy, comment, null, filter, Collections.emptyList());
        }

        @Override
        public void linkPrevNextSegments() {
        }
    }

    static class CalcMatchStatisticsMock extends CalcMatchStatistics {

        private final String[] rowsTotal = new String[] { "RowRepetitions", "RowExactMatch", "RowMatch95", "RowMatch85",
                "RowMatch75", "RowMatch50", "RowNoMatch", "Total" };

        private MatchStatCounts result;

        CalcMatchStatisticsMock(IStatsConsumer callback) {
            super(callback, false);
        }

        @Override
        public void run() {
            entriesToProcess = Core.getProject().getAllEntries().size();
            result = calcTotal(false);
        }

        public String[][] getTable() {
            return result.calcTable(rowsTotal, i -> i != 1);
        }
    }

    static class TestStatsConsumer implements IStatsConsumer {

        @Override
        public void appendTextData(final String result) {
        }

        @Override
        public void appendTable(final String title, final String[] headers, final String[][] data) {
        }

        @Override
        public void setTextData(final String data) {
        }

        @Override
        public void setTable(final String[] headers, final String[][] data) {
        }

        @Override
        public void setDataFile(final String path) {
        }

        @Override
        public void finishData() {
        }

        @Override
        public void showProgress(final int percent) {
        }
    }
}
