/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2024-2025 Hiroshi Miura
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

package org.omegat.convert.segmentation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.omegat.core.segmentation.LanguageCodes;
import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;
import org.omegat.util.ValidationResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
@RunWith(Enclosed.class)
public final class SegmentationConfMigratorTest {

    private static final String SEGMENT_CONF_BASE = "test/data/segmentation/";

    public static class SRXMigrateTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigration() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_en", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SegmentationConfMigratorTest.testSrxMigration(segmentConf, configDir);
        }
    }

    public static class SRXMigrateJaTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("ja"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigration() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_ja", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SegmentationConfMigratorTest.testSrxMigration(segmentConf, configDir);
        }
    }

    public static class SRXMigrateOldDeTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("de"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigration() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_de_54", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SegmentationConfMigratorTest.testSrxMigration(segmentConf, configDir);
        }
    }

    /**
     * Test SRX writer/reader.
     * <p>
     * Previous versions have a bug when saving segmentation.conf file. It is
     * better to save language property using language code defined in
     * LanguageCode class. Unfortunately, OmegaT 6.0 and before produce a
     * localized language name for the property. The test case here trys reading
     * a segmentation.conf file that is produced by OmegaT in English
     * environment and Japanese environment.
     */
    public static void testSrxMigration(File segmentConf, File configDir) {
        File segmentSrx = new File(configDir, "segmentation.srx");
        // load from conf file
        assertTrue(segmentConf.exists());
        assertTrue(segmentConf.isFile());
        ValidationResult result = SegmentationConfMigrator.checkConfigFile(segmentConf.toPath());
        assertTrue(result.isValid());
        SRX srxOrig = SegmentationConfMigrator.convertToSrx(segmentConf.toPath(), segmentSrx.toPath());
        assertNotNull(srxOrig);
        List<MapRule> mapRuleList = srxOrig.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule : mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                Assert.assertEquals(LanguageCodes.JAPANESE_CODE, mapRule.getLanguage());
                assertEquals(OStrings.getString(LanguageCodes.JAPANESE_KEY), mapRule.getLanguageName());
            } else if (mapRule.getLanguage().equals("Text")) {
                assertEquals(OStrings.getString(LanguageCodes.F_TEXT_KEY), mapRule.getLanguageName());
            }
        }
        // load from srx file
        assertTrue(segmentSrx.exists());
        assertTrue(segmentSrx.isFile());
        SRX srx1 = SRX.loadSrxFile(segmentSrx.toURI());
        assertNotNull(srx1);
        mapRuleList = srx1.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule : mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                assertEquals(LanguageCodes.JAPANESE_CODE, mapRule.getLanguage());
                assertEquals(OStrings.getString(LanguageCodes.JAPANESE_KEY), mapRule.getLanguageName());
            } else if (mapRule.getLanguage().equals("Text")) {
                assertEquals(LanguageCodes.F_TEXT_CODE, mapRule.getLanguage());
                assertEquals(OStrings.getString(LanguageCodes.F_TEXT_KEY), mapRule.getLanguageName());
            }
        }
        assertEquals("2.0", srx1.getVersion());
        assertTrue(srx1.isCascade());
        assertTrue(srx1.isSegmentSubflows());
    }

    private SegmentationConfMigratorTest() {
    }
}
