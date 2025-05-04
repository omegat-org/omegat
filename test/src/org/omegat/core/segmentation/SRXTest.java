/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Aaron Madlon-Kay
 */
public class SRXTest {

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

    @Test
    public void testSRXLoaderSecureCVE_2024_51366() throws IOException {
        var segmentConf = Files.createTempDirectory("omegat").resolve("segment.conf");
        // prepare CVE-2024-51366 exploit code
        String xmlContent = "<java>\n" +
                "    <object class=\"java.lang.ProcessBuilder\">\n" +
                "        <array class=\"java.lang.String\" length=\"1\" >\n" +
                "            <void index=\"0\">\n" +
                "                <string>gnome-calculator</string>\n" +
                "            </void>\n" +
                "        </array>\n" +
                "        <void method=\"start\"/>\n" +
                "    </object>\n" +
                "</java>";
        Files.writeString(segmentConf, xmlContent);
        SRX srx = SRX.loadSRX(segmentConf.toFile());
        assertNotNull(srx);
        assertEquals("\\s", srx.getMappingRules().get(0).getRules().get(0).getAfterbreak());
    }
}
