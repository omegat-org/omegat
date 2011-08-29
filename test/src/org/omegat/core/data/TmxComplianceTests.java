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

package org.omegat.core.data;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.filters2.html2.HTMLFilter2;

/**
 * TMX Compliance tests as described on http://www.lisa.org/tmx/comp.htm
 * 
 * The Level 1 Compliance verifies mostly TMX structure, white spaces handling
 * and how the application deals with non-ASCII characters and special characters
 * in XML such as '<', or '&', XML syntax, encodings, and so forth.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TmxComplianceTests extends TmxComplianceBase {
    /**
     * Test Import1A - Internal Classic White Spaces.
     */
    @Test
    public void testImport1A() throws Exception {
        translateTextUsingTmx("ImportTest1A.txt", "UTF-8", "ImportTest1A.tmx", "ImportTest1A_fr-ca.txt",
                "UTF-8", "EN-US", "FR-CA");
    }

    /**
     * Test Import1B - XML Syntax.
     */
    @Test
    public void testImport1B() throws Exception {
        translateTextUsingTmx("ImportTest1B.txt", "UTF-8", "ImportTest1B.tmx", "ImportTest1B_fr-ca.txt",
                "UTF-8", "EN-US", "FR-CA");
    }

    /**
     * Test Import1C - Multiple Languages
     */
    @Test
    public void testImport1C() throws Exception {
        translateTextUsingTmx("ImportTest1C.txt", "UTF-8", "ImportTest1C.tmx", "ImportTest1C_fr-ca.txt",
                "UTF-8", "EN-US", "FR-CA");
    }

    /**
     * Test Import1D - UTF-8 with BOM
     */
    @Test
    public void testImport1D() throws Exception {
        translateTextUsingTmx("ImportTest1D.txt", "UTF-8", "ImportTest1D.tmx", "ImportTest1D_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB");
    }

    /**
     * Test Import1E - UTF-8 without BOM
     */
    @Test
    public void testImport1E() throws Exception {
        translateTextUsingTmx("ImportTest1E.txt", "UTF-8", "ImportTest1E.tmx", "ImportTest1E_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB");
    }

    /**
     * Test Import1F - UTF-16 LE
     */
    @Test
    public void testImport1F() throws Exception {
        translateTextUsingTmx("ImportTest1F.txt", "UTF-8", "ImportTest1F.tmx", "ImportTest1F_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB");
    }

    /**
     * Test Import1F - UTF-16 BE
     */
    @Test
    public void testImport1G() throws Exception {
        translateTextUsingTmx("ImportTest1G.txt", "UTF-8", "ImportTest1G.tmx", "ImportTest1G_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB");
    }

    /**
     * Test Import1H - ASCII
     */
    @Test
    public void testImport1H() throws Exception {
        translateTextUsingTmx("ImportTest1H.txt", "UTF-8", "ImportTest1H.tmx", "ImportTest1H_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB");
    }

    /**
     * Test Import1I - Internal Asian White Spaces
     */
    @Test
    public void testImport1I() throws Exception {
        translateTextUsingTmx("ImportTest1I.txt", "UTF-8", "ImportTest1I.tmx", "ImportTest1I_ja-jp.txt",
                "UTF-16LE", "EN-US", "JA-JP");
    }

    /**
     * Test Import1J - Language Selection
     */
    @Test
    public void testImport1J() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "EN-GB");
        final ProjectTMX tmx = new ProjectTMX(props, new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1J_many.tmx"), orphanedCallback,
                new HashMap<EntryKey, TMXEntry>());
        tmx.save(props, outFile, false, false, false);
        // TODO validate via TMXCheck
    }

    /**
     * Test Import1K - No Import
     */
    @Test
    public void testImport1K() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "EN-GB");
        final ProjectTMX tmx = new ProjectTMX(props, new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1K.tmx"), orphanedCallback,
                new HashMap<EntryKey, TMXEntry>());
        // TODO assertEquals(0, tmx.translationDefault.size());
    }

    /**
     * Test Import1L - All Elements and Attributes
     */
    @Test
    public void testImport1L() throws Exception {
        // TODO
    }

    /**
     * Test Export1A - RC
     */
    @Test
    public void testExport1A() throws Exception {
        // TODO
    }

    /**
     * Test Export1B - HTML
     */
    @Test
    public void testExport1B() throws Exception {
        // TODO
    }

    /**
     * Test Export1C - Java Properties
     */
    @Test
    public void testExport1C() throws Exception {
        // TODO
    }

    /**
     * Test Export1D - Portable Object
     */
    @Test
    public void testExport1D() throws Exception {
        // TODO
    }

    /**
     * Test Export1E - XLIFF
     */
    @Test
    public void testExport1E() throws Exception {
        // TODO
    }

    /**
     * Test Import2A - Content Markup Syntax in HTML
     */
    @Test
    public void testImport2A() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");
        props.setSentenceSegmentingEnabled(true);
        translateUsingTmx(new HTMLFilter2(), new TreeMap<String, String>(), "ImportTest2A.htm", "UTF-8",
                "ImportTest2A.tmx", "ImportTest2A_fr-ca.htm", "UTF-8", props);
    }

    /**
     * Test Import2B - Content Markup Syntax in RTF
     */
    @Test
    public void testImport2B() throws Exception {
        // RTF not supported
    }

    /**
     * Test Import2C - All Elements and Attributes
     */
    @Test
    public void testImport2C() throws Exception {
        // TODO
    }

    /**
     * Test Export2A - HTML
     */
    @Test
    public void testExport2A() throws Exception {
        // TODO
    }
}
