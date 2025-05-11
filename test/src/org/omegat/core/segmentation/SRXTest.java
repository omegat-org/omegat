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

package org.omegat.core.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import org.omegat.util.OStrings;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public final class SRXTest {

    private static final File SEGMENT_DEFAULT = new File("test/data/segmentation/default/segmentation.srx");

    @Test
    public void testSrxComparison() {
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
        org.omegat.core.segmentation.Rule rule = clone.getMappingRules().get(0).getRules().get(0);
        rule.setAfterbreak(rule.getAfterbreak() + "foo");
        assertNotEquals(orig, clone);
    }

    /**
     * Test SRX#loadFromDir produce SRX object properly.
     * <p>
     * MapRule#getLanguageCode should return Language Code defined in
     * LanguageCode class. MapRule#getLanguage should return a localized
     * name of language. The test here check both values. OmegaT 6.0 and
     * before,
     */
    @Test
    public void testSrxReaderDefault() throws IOException {
        assertTrue(SEGMENT_DEFAULT.exists());
        assertTrue(SEGMENT_DEFAULT.isFile());
        SRX srx = SRX.loadSrxFile(SEGMENT_DEFAULT.toURI());
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
}
