/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersionComparison() {
        assertEquals(0, VersionChecker.compareVersions("1.0.0", "0", "1.0.0", "0"));
        compareLessThan("1.0.0", "0", "1.0.0", "1");
        compareLessThan("1.0.0", "0", "1.0.1", "0");
        compareLessThan("1.0.0", "0", "1.1.0", "0");
        compareLessThan("1.0.0", "0", "2.0.0", "0");
        try {
            VersionChecker.compareVersions("1.0", "0", "1.0.0", "0");
            fail("Should throw");
        } catch (IllegalArgumentException e) {
            // OK
        }
        try {
            VersionChecker.compareVersions("a.b.c", "0", "1.0.0", "0");
            fail("Should throw");
        } catch (NumberFormatException e) {
            // OK
        }
    }

    private static void compareLessThan(String v1, String u1, String v2, String u2) {
        assertTrue(VersionChecker.compareVersions(v1, u1, v2, u2) < 0);
        assertTrue(0 < VersionChecker.compareVersions(v2, u2, v1, u1));
    }
}
