/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Test;

import org.omegat.util.OStrings;

/**
 * @author Aaron Madlon-Kay
 */
public class SRXTest {

    private static final File SEGMENT_DEFAULT = new File("test/data/segmentation/default/");
    private static final String SEGMENT_CONF_BASE = "test/data/segmentation/migrate/";

    @Test
    public void testSRXComparison() {
        SRX orig = SRX.getDefault();
        SRX clone = orig.copy();
        assertNotSame(orig, clone);
        assertEquals(orig, clone);
        assertEquals(orig.hashCode(), clone.hashCode());

        // Shallow change
        clone.setIncludeEndingTags(!clone.isIncludeEndingTags());
        assertNotEquals(orig, clone);

        // Deep change
        clone = orig.copy();
        Rule rule = clone.getMappingRules().get(0).getRules().get(0);
        rule.setAfterbreak(rule.getAfterbreak() + "foo");
        assertNotEquals(orig, clone);
    }

    /**
     * Test SRX#loadFromDir produce SRX object properly.
     * <p>
     * MapRule#getLanguageCode should return Language Code defined in
     * LanguageCode class. MapRule#getLanguage should return a localized name of
     * language. The test here check both values. OmegaT 6.0 and before,
     */
    @Test
    public void testSrxReaderDefault() {
        assertTrue(SEGMENT_DEFAULT.exists());
        SRX srx = SRX.loadFromDir(SEGMENT_DEFAULT);
        assertNotNull(srx);
        assertTrue(srx.isCascade());
        List<MapRule> mapRuleList = srx.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule : mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                assertEquals(LanguageCodes.JAPANESE_CODE, mapRule.getLanguage());
                assertEquals(OStrings.getString(LanguageCodes.JAPANESE_KEY), mapRule.getLanguageName());
            }
        }
        assertEquals("2.0", srx.getVersion());
        assertTrue(srx.isSegmentSubflows());
    }

    /**
     * Test SRX writer/reader.
     * <p>
     * Previous versions has a bug when saving segmentation.conf file. It is
     * better to save language property using language code defined in
     * LanguageCode class. Unfortunately OmegaT 6.0 and before produce a
     * localized language name for the property. The test case here trys reading
     * a segmentation.conf file that is produced by OmegaT in English
     * environment and Japanese environment.
     */
    @Test
    public void testSrxMigration() throws IOException {
        File segmentConf;
        Path segmentSrxPath;
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {
            segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_ja").toFile();
            segmentSrxPath = Paths.get(SEGMENT_CONF_BASE, "locale_ja", "segmentation.srx");
        } else {
            segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_en").toFile();
            segmentSrxPath = Paths.get(SEGMENT_CONF_BASE, "locale_en", "segmentation.srx");
        }
        Files.deleteIfExists(segmentSrxPath);
        //
        File segmentSrx = segmentSrxPath.toFile();
        assertFalse(segmentSrx.exists());
        // load from conf file
        SRX srxOrig = SRX.loadFromDir(segmentConf);
        assertNotNull(srxOrig);
        List<MapRule> mapRuleList = srxOrig.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        // load from srx file
        assertTrue(segmentSrx.exists());
        SRX srx1 = SRX.loadFromDir(segmentConf);
        assertNotNull(srx1);
        mapRuleList = srx1.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule : mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                assertEquals(LanguageCodes.JAPANESE_CODE, mapRule.getLanguage());
                assertEquals(OStrings.getString(LanguageCodes.JAPANESE_KEY), mapRule.getLanguageName());
            }
        }
        assertEquals("2.0", srx1.getVersion());
        assertTrue(srx1.isCascade());
        assertTrue(srx1.isSegmentSubflows());
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Path segmentSrxPath;
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {
            segmentSrxPath = Paths.get(SEGMENT_CONF_BASE, "locale_ja", "segmentation.srx");
        } else {
            segmentSrxPath = Paths.get(SEGMENT_CONF_BASE, "locale_en", "segmentation.srx");
        }
        Files.deleteIfExists(segmentSrxPath);
    }
}
