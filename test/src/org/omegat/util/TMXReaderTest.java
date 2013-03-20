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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.util;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.TestCore;

/**
 * @author Alex Buloichik
 */
public class TMXReaderTest extends TestCore {
    protected File outFile = new File(System.getProperty("java.io.tmpdir"), "OmegaT test - "
            + getClass().getSimpleName());

    public void testLeveL1() throws Exception {
        final Map<String, String> tr = new TreeMap<String, String>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-level1.tmx"), new Language("en-US"),
                new Language("be"), false, false, false, false, new TMXReader2.LoadCallback() {
                    public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                            TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                        tr.put(tuvSource.text, tuvTarget.text);
                        return true;
                    }
                });
        assertEquals("betuv", tr.get("entuv"));
        assertEquals("tr1", tr.get("lang1"));
        assertEquals("tr2", tr.get("lang2"));
        assertEquals("tr3", tr.get("lang3"));
    }

    public void testLeveL2() throws Exception {
        final Map<String, String> tr = new TreeMap<String, String>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-level2.tmx"), new Language("en-US"),
                new Language("be"), false, false, true, false, new TMXReader2.LoadCallback() {
                    public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                            TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                        tr.put(tuvSource.text, tuvTarget.text);
                        return true;
                    }
                });
        assertEquals("betuv", tr.get("entuv"));
        assertEquals("tr", tr.get("2 <a0> zz <t1>xx</t1>"));
        assertEquals("tr", tr.get("3 <n0>xx</n0>"));
    }

    public void testInvalidTMX() throws Exception {
        final Map<String, String> tr = new TreeMap<String, String>();
        new TMXReader2().readTMX(new File("test/data/tmx/invalid.tmx"), new Language("en"),
                new Language("be"), false, false, true, false, new TMXReader2.LoadCallback() {
                    public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                            TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                        tr.put(tuvSource.text, tuvTarget.text);
                        return true;
                    }
                });
    }
    
    public void testGetTuvByLang() {
        TMXReader2.ParsedTuv tuvBE = new TMXReader2.ParsedTuv();
        tuvBE.lang = "be";

        TMXReader2.ParsedTuv tuvFR = new TMXReader2.ParsedTuv();
        tuvFR.lang = "FR";
        
        TMXReader2.ParsedTuv tuvFRCA = new TMXReader2.ParsedTuv();
        tuvFRCA.lang = "FR-CA";

        TMXReader2.ParsedTuv tuvFRFR = new TMXReader2.ParsedTuv();
        tuvFRFR.lang = "FR-FR";

        TMXReader2.ParsedTuv tuvENGB = new TMXReader2.ParsedTuv();
        tuvENGB.lang = "EN-GB";

        TMXReader2 tmx = new TMXReader2();
        tmx.currentTu.tuvs.add(tuvBE);
        tmx.currentTu.tuvs.add(tuvFR);
        tmx.currentTu.tuvs.add(tuvFRCA);
        tmx.currentTu.tuvs.add(tuvFRFR);
        tmx.currentTu.tuvs.add(tuvENGB);

        assertEquals(tmx.getTuvByLang(new Language("BE")), tuvBE);
        assertEquals(tmx.getTuvByLang(new Language("BE-NN")), tuvBE);

        assertNotNull(tmx.getTuvByLang(new Language("FR")));
        assertEquals(tmx.getTuvByLang(new Language("FR-CA")), tuvFRCA);
        assertEquals(tmx.getTuvByLang(new Language("FR-NN")), tuvFR);

        assertEquals(tmx.getTuvByLang(new Language("EN")), tuvENGB);
        assertEquals(tmx.getTuvByLang(new Language("EN-CA")), tuvENGB);
        
        assertNull(tmx.getTuvByLang(new Language("ZZ")));
    }
}
