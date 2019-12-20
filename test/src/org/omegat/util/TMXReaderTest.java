/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.ByteOrderMark;
import org.junit.Test;
import org.omegat.core.TestCore;

/**
 * @author Alex Buloichik
 */
public class TMXReaderTest extends TestCore {
    protected File outFile = new File(System.getProperty("java.io.tmpdir"), "OmegaT test - "
            + getClass().getSimpleName());

    @Test
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

    @Test
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

    @Test
    public void testGzip() throws Exception {
        final Map<String, String> tr = new TreeMap<>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-level2.tmx.gz"), new Language("en"), new Language("be"),
                false, false, true, false, (tu, tuvSource, tuvTarget, isParagraphSegtype) -> {
                    tr.put(tuvSource.text, tuvTarget.text);
                    return true;
                });
        // Same content as test-level2.tmx
        assertFalse(tr.isEmpty());
        assertEquals("betuv", tr.get("entuv"));
        assertEquals("tr", tr.get("2 <a0> zz <t1>xx</t1>"));
        assertEquals("tr", tr.get("3 <n0>xx</n0>"));
    }

    @Test
    public void testZip() throws Exception {
        final Map<String, String> tr = new TreeMap<>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-level2.tmx.zip"), new Language("en"), new Language("be"),
                false, false, true, false, (tu, tuvSource, tuvTarget, isParagraphSegtype) -> {
                    tr.put(tuvSource.text, tuvTarget.text);
                    return true;
                });
        // Same content as test-level2.tmx
        assertFalse(tr.isEmpty());
        assertEquals("betuv", tr.get("entuv"));
        assertEquals("tr", tr.get("2 <a0> zz <t1>xx</t1>"));
        assertEquals("tr", tr.get("3 <n0>xx</n0>"));
    }

    @Test
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

    @Test
    public void testSMP() throws Exception {
        final Map<String, String> tr = new TreeMap<String, String>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-SMP.tmx"), new Language("en"),
                new Language("be"), false, false, true, false, new TMXReader2.LoadCallback() {
                    public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                            TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                        tr.put(tuvSource.text, tuvTarget.text);
                        return true;
                    }
                });
        assertFalse(tr.isEmpty());
        // Assert contents are {"ABC": "DEF"} where letters are MATHEMATICAL BOLD CAPITALs (U+1D400-)
        assertEquals("\uD835\uDC03\uD835\uDC04\uD835\uDC05", tr.get("\uD835\uDC00\uD835\uDC01\uD835\uDC02"));
    }

    @Test
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

    @Test
    public void testCharset() throws Exception {
        File xml = new File("build/testdata/test.xml");
        xml.getParentFile().mkdirs();

        testXml(xml, ByteOrderMark.UTF_8, "<?xml version=\"1.0\"?>", "UTF-8");
        testXml(xml, ByteOrderMark.UTF_16LE, "<?xml version=\"1.0\"?>", "UTF-16LE");
        testXml(xml, ByteOrderMark.UTF_16BE, "<?xml version=\"1.0\"?>", "UTF-16BE");
        testXml(xml, ByteOrderMark.UTF_32LE, "<?xml version=\"1.0\"?>", "UTF-32LE");
        testXml(xml, ByteOrderMark.UTF_32BE, "<?xml version=\"1.0\"?>", "UTF-32BE");
        testXml(xml, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "UTF-8");
        testXml(xml, null, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", "ISO-8859-1");
    }

    private void testXml(File xml, ByteOrderMark bom, String text, String charset) throws Exception {
        try (FileOutputStream out = new FileOutputStream(xml)) {
            if (bom != null) {
                out.write(bom.getBytes());
            }
            out.write(text.getBytes(charset));
        }
        assertEquals(charset, TMXReader2.detectCharset(xml));
    }

    /**
     * Test for bug #892
     *
     * @see <a href="https://sourceforge.net/p/omegat/bugs/892/">Bug #892</a>
     */
    @Test
    public void testMissingSource() throws Exception {
        Map<String, String> tr = new TreeMap<>();
        new TMXReader2().readTMX(new File("test/data/tmx/test-missingSource.tmx"), new Language("en"),
                new Language("be"), false, false, true, false, (tu, tuvSource, tuvTarget, isParagraphSegtype) -> {
                    if (tuvSource != null && tuvTarget != null) {
                        tr.put(tuvSource.text, tuvTarget.text);
                        return true;
                    }
                    return false;
                });
        // Make sure everything is loaded despite TU missing source TUV
        assertFalse(tr.isEmpty());
        assertEquals(3, tr.size());
        assertEquals("betuv", tr.get("entuv"));
        assertNull(tr.get("lang1"));
        assertEquals("tr2", tr.get("lang2"));
        assertEquals("tr3", tr.get("lang3"));
    }
}
