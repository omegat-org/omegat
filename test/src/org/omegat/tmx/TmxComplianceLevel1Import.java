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

package org.omegat.tmx;

import java.io.File;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.tmx.TmxComplianceBase.TestProjectProperties;

/**
 * TMX Compliance tests as described on http://www.lisa.org/tmx/comp.htm
 * 
 * The Level 1 Compliance verifies mostly TMX structure, white spaces handling
 * and how the application deals with non-ASCII characters and special characters
 * in XML such as '<', or '&', XML syntax, encodings, and so forth.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TmxComplianceLevel1Import extends TmxComplianceBase {
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
        ProjectProperties props = new TestProjectProperties();
        props.setSourceLanguage("EN-US");
        props.setTargetLanguage("EN-GB");
        final ProjectTMX tmx = new ProjectTMX(props, new File(
                "test/data/tmx/TMXComplianceKit/ImportTest1J_many.tmx"), orphanedCallback);
        tmx.save(props, outFile, false, false, false);
    }
}
