/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import org.omegat.core.TestCore;

/**
 * @author Alex Buloichik
 */
public class TMXReaderTest extends TestCore {
    public void testLeveL1() throws Exception {
        TMXReader rd = new TMXReader("UTF-8", new Language("en"), new Language(
                "be"), false);
        rd.loadFile("test/data/tmx/test-level1.tmx", false);
        assertEquals(1, rd.numSegments());
        assertEquals("entuv", rd.getSourceSegment(0));
        assertEquals("betuv", rd.getTargetSegment(0));
    }

    public void testLeveL2() throws Exception {
        TMXReader rd = new TMXReader("UTF-8", new Language("en"), new Language(
                "be"), false);
        rd.loadFile("test/data/tmx/test-level2.tmx", false);
        assertEquals(1, rd.numSegments());
        assertEquals("entuv", rd.getSourceSegment(0));
        assertEquals("betuv", rd.getTargetSegment(0));
    }
}
