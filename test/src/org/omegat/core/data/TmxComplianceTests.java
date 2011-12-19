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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.html2.HTMLOptions;
import org.omegat.filters2.po.PoFilter;
import org.omegat.filters2.rc.RcFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;

/**
 * TMX Compliance tests as described on http://www.localization.org/fileadmin/standards/tmx1.4/comp.htm
 * 
 * The Level 1 Compliance verifies mostly TMX structure, white spaces handling
 * and how the application deals with non-ASCII characters and special characters
 * in XML such as '<', or '&', XML syntax, encodings, and so forth.
 * 
 * The Level 2 compliance verifies mostly how the application deals with content 
 * markup. To qualify for Level 2 compliance, the application must also qualify 
 * for Level 1 compliance.
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
                "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1B - XML Syntax.
     */
    @Test
    public void testImport1B() throws Exception {
        translateTextUsingTmx("ImportTest1B.txt", "UTF-8", "ImportTest1B.tmx", "ImportTest1B_fr-ca.txt",
                "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1C - Multiple Languages
     */
    @Test
    public void testImport1C() throws Exception {
        translateTextUsingTmx("ImportTest1C.txt", "UTF-8", "ImportTest1C.tmx", "ImportTest1C_fr-ca.txt",
                "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1D - UTF-8 with BOM
     */
    @Test
    public void testImport1D() throws Exception {
        translateTextUsingTmx("ImportTest1D.txt", "UTF-8", "ImportTest1D.tmx", "ImportTest1D_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1E - UTF-8 without BOM
     */
    @Test
    public void testImport1E() throws Exception {
        translateTextUsingTmx("ImportTest1E.txt", "UTF-8", "ImportTest1E.tmx", "ImportTest1E_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1F - UTF-16 LE
     */
    @Test
    public void testImport1F() throws Exception {
        translateTextUsingTmx("ImportTest1F.txt", "UTF-8", "ImportTest1F.tmx", "ImportTest1F_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1F - UTF-16 BE
     */
    @Test
    public void testImport1G() throws Exception {
        translateTextUsingTmx("ImportTest1G.txt", "UTF-8", "ImportTest1G.tmx", "ImportTest1G_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1H - ASCII
     */
    @Test
    public void testImport1H() throws Exception {
        translateTextUsingTmx("ImportTest1H.txt", "UTF-8", "ImportTest1H.tmx", "ImportTest1H_en-gb.txt",
                "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1I - Internal Asian White Spaces
     */
    @Test
    public void testImport1I() throws Exception {
        translateTextUsingTmx("ImportTest1I.txt", "UTF-8", "ImportTest1I.tmx", "ImportTest1I_ja-jp.txt",
                "UTF-16LE", "EN-US", "JA-JP", null);
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
        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1A.tmx");
        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1A.rc");
        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1A_fr.rc");

        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");

        RcFilter filter = new RcFilter();

        align(filter, sourceFile, "windows-1252", translatedFile, "windows-1252", props);

        compareTMX(tmxFile, outFile, 6);
    }

    /**
     * Test Export1B - HTML
     */
    @Test
    public void testExport1B() throws Exception {
        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1B.tmx");
        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1B.htm");
        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1B_fr.htm");

        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");

        List<String> sources = loadTexts(new HTMLFilter2(), sourceFile, null, props);
        List<String> translations = loadTexts(new HTMLFilter2(), translatedFile, null, props);

        assertEquals(sources.size(), translations.size());

        ProjectTMX tmx = new ProjectTMX(props, outFile, orphanedCallback, new TreeMap<EntryKey, TMXEntry>());

        for (int i = 0; i < sources.size(); i++) {
            tmx.translationDefault.put(sources.get(i), new TMXEntry(sources.get(i), translations.get(i),
                    null, 0, null, true));
        }

        tmx.save(props, outFile, false, false, true);

        compareTMX(tmxFile, outFile, 6);
    }

    /**
     * Test Export1C - Java Properties
     */
    @Test
    public void testExport1C() throws Exception {
        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1C.tmx");
        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1C.properties");
        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1C_fr.properties");

        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");

        align(new ResourceBundleFilter(), sourceFile, null, translatedFile, null, props);

        compareTMX(tmxFile, outFile, 6);
    }

    /**
     * Test Export1D - Portable Object
     */
    @Test
    public void testExport1D() throws Exception {
        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D.tmx");
        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D.po");
        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D_fr.po");

        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");

        align(new PoFilter(), sourceFile, "iso-8859-1", translatedFile, "iso-8859-1", props);

        compareTMX(tmxFile, outFile, 6);
    }

    /**
     * Test Export1E - XLIFF
     */
    @Test
    public void testExport1E() throws Exception {
        // TODO
    }

    /**
     * Test Import2A - Content Markup Syntax in HTML.
     * 
     * TEST CHANGED FROM TMX COMPLIANCE PACK BECAUSE WE HAVE OTHER SEGMENTATION SETTINGS, i.e. "Picture: <img
     * src="img.png"/>" should be processed as one segment by TMX compliance tests, but it's not a one segment
     * by OmegaT segmentation. Since it out of scope of testing, we patch tmx for runtime-only.
     */
    @Test
    public void testImport2A() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");
        props.setSentenceSegmentingEnabled(true);
        Map<String, String> config = new TreeMap<String, String>();
        config.put(HTMLOptions.OPTION_TRANSLATE_SRC, "false");

        Map<String, TMXEntry> fix = new TreeMap<String, TMXEntry>();
        fix.put("Picture:", new TMXEntry("Picture:", "Image:", null, 0, null, true));
        translateUsingTmx(new HTMLFilter2(), config, "ImportTest2A.htm", "UTF-8", "ImportTest2A.tmx",
                "ImportTest2A_fr-ca.htm", "UTF-8", props, fix);
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
        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest2A.tmx");
        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest2A.htm");
        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest2A_fr.htm");

        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");

        align(new HTMLFilter2(), sourceFile, null, translatedFile, null, props);

        compareTMX(tmxFile, outFile, 6);
    }
}
