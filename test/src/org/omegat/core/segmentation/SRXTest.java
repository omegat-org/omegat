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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Aaron Madlon-Kay
 */
public class SRXTest {

    private final File segmentGenerated = new File("test/data/segmentation/generated/");
    private final File segmentDefault = new File("test/data/segmentation/default/");

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
     */
    @Test
    public void testSrxReaderDefault() {
        assertTrue(segmentDefault.exists());
        SRX srx = SRX.loadFromDir(segmentDefault);
        assertNotNull(srx);
        assertTrue(srx.isCascade());
        List<MapRule> mapRuleList = srx.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule : mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                assertEquals("Japanese", mapRule.getLanguageCode());
                // assertEquals("Japanese", mapRule.getLanguage());
            }
        }
        assertEquals("2.0", srx.getVersion());
        assertTrue(srx.isSegmentSubflows());
    }

    /**
     * Test SRX writer/reader.
     * <p>
     * try read a segmentation.srx file that is produced by OmegaT when system locale is Japanese.
     */
    @Test
    public void testSrxReaderGenerated() {
        assertTrue(segmentGenerated.exists());
        SRX srx = SRX.loadFromDir(segmentGenerated);
        assertNotNull(srx);
        List<MapRule> mapRuleList = srx.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(18, mapRuleList.size());
        for (MapRule mapRule: mapRuleList) {
            if (mapRule.getPattern().equals("JA.*")) {
                assertEquals("Japanese", mapRule.getLanguageCode());
                assertEquals("\u65E5\u672C\u8A9E", mapRule.getLanguage());
            }
        }
        assertEquals("2.0", srx.getVersion());
        assertTrue(srx.isCascade());
        assertTrue(srx.isSegmentSubflows());
    }
}
