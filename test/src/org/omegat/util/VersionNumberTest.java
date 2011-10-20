/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

import junit.framework.TestCase;

/**
 * Tests for versions parsing.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class VersionNumberTest extends TestCase {
    public void tests() {
        VersionNumber v;

        v = new VersionNumber("375");
        assertEquals(375, v.v1);
        assertEquals(0, v.v2);
        assertEquals(0, v.v3);
        assertEquals(0, v.v4);

        v = new VersionNumber("1.2");
        assertEquals(1, v.v1);
        assertEquals(2, v.v2);
        assertEquals(0, v.v3);
        assertEquals(0, v.v4);

        v = new VersionNumber("5.23.9078");
        assertEquals(5, v.v1);
        assertEquals(23, v.v2);
        assertEquals(9078, v.v3);
        assertEquals(0, v.v4);

        v = new VersionNumber("1.2.003.04");
        assertEquals(1, v.v1);
        assertEquals(2, v.v2);
        assertEquals(3, v.v3);
        assertEquals(4, v.v4);

        try {
            v = new VersionNumber("1.2_04");
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}
