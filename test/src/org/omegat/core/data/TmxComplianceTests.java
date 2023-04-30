/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.html2.HTMLOptions;
import org.omegat.filters2.rc.RcFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;

/**
 * TMX Compliance tests as described on
 * https://web.archive.org/web/20071011191836/http://www.lisa.org/standards/tmx/specification.html
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
        translateAndCheckTextUsingTmx("ImportTest1A.txt", "UTF-8", "ImportTest1A.tmx",
                "ImportTest1A_fr-ca.txt", "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1B - XML Syntax.
     */
    @Test
    public void testImport1B() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1B.txt", "UTF-8", "ImportTest1B.tmx",
                "ImportTest1B_fr-ca.txt", "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1C - Multiple Languages
     */
    @Test
    public void testImport1C() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1C.txt", "UTF-8", "ImportTest1C.tmx",
                "ImportTest1C_fr-ca.txt", "UTF-8", "EN-US", "FR-CA", null);
    }

    /**
     * Test Import1D - UTF-8 with BOM
     */
    @Test
    public void testImport1D() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1D.txt", "UTF-8", "ImportTest1D.tmx",
                "ImportTest1D_en-gb.txt", "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1E - UTF-8 without BOM
     */
    @Test
    public void testImport1E() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1E.txt", "UTF-8", "ImportTest1E.tmx",
                "ImportTest1E_en-gb.txt", "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1F - UTF-16 LE
     */
    @Test
    public void testImport1F() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1F.txt", "UTF-8", "ImportTest1F.tmx",
                "ImportTest1F_en-gb.txt", "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1F - UTF-16 BE
     */
    @Test
    public void testImport1G() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1G.txt", "UTF-8", "ImportTest1G.tmx",
                "ImportTest1G_en-gb.txt", "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1H - ASCII
     */
    @Test
    public void testImport1H() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1H.txt", "UTF-8", "ImportTest1H.tmx",
                "ImportTest1H_en-gb.txt", "UTF-8", "EN-US", "EN-GB", null);
    }

    /**
     * Test Import1I - Internal Asian White Spaces
     */
    @Test
    public void testImport1I() throws Exception {
        translateAndCheckTextUsingTmx("ImportTest1I.txt", "UTF-8", "ImportTest1I.tmx",
                "ImportTest1I_ja-jp.txt", "UTF-16LE", "EN-US", "JA-JP", null);
    }

    /**
     * Test Import1J - Language Selection
     */
    @Test
    public void testImport1J() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "EN-GB");
        final ProjectTMX tmx = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), new File("test/data/tmx/TMXComplianceKit/ImportTest1J_many.tmx"),
                orphanedCallback);
        tmx.exportTMX(props, outFile, false, false, false);
        // TODO validate via TMXCheck
    }

    /**
     * Test Import1K - No Import
     */
    @Ignore
    @Test
    public void testImport1K() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "EN-GB");
        final ProjectTMX tmx = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), new File("test/data/tmx/TMXComplianceKit/ImportTest1K.tmx"),
                orphanedCallback);
        assertEquals(0, tmx.defaults.size());
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

        FilterContext fc = new FilterContext(props);
        fc.setInEncoding("windows-1252");

        Map<String, String> config = new TreeMap<String, String>();
        new HTMLOptions(config).setSkipMeta("content=en-us,content=fr-ca");

        List<String> sources = loadTexts(new HTMLFilter2(), sourceFile, null, fc, config);
        List<String> translations = loadTexts(new HTMLFilter2(), translatedFile, null, fc, config);

        assertEquals(sources.size(), translations.size());

        ProjectTMX tmx = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), outFile, orphanedCallback);

        for (int i = 0; i < sources.size(); i++) {
            tmx.defaults.put(sources.get(i), createTMXEntry(sources.get(i), translations.get(i), true));
        }

        tmx.exportTMX(props, outFile, false, false, true);

        compareTMX(tmxFile, outFile, 2);
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
        /*
         * Test data contains .po files, which doesn't compliance with PO
         * specification
         * (https://www.gnu.org/savannah-checkouts/gnu/gettext/
         * manual/html_node/PO-Files.html). By the specification, msgid
         * should contain "untranslated-string", but in the ExportTest1D.po
         * file it contains ID.
         */

//        File tmxFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D.tmx");
//        File sourceFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D.po");
//        File translatedFile = new File("test/data/tmx/TMXComplianceKit/ExportTest1D_fr.po");
//
//        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");
//
//        align(new PoFilter(), sourceFile, "iso-8859-1", translatedFile, "iso-8859-1", props);
//
//        compareTMX(tmxFile, outFile, 8);
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
        config.put(HTMLOptions.OPTION_SKIP_META, "true");

        Map<String, TMXEntry> fix = new TreeMap<String, TMXEntry>();
        fix.put("Picture:", createTMXEntry("Picture:", "Image:", true));
        translateUsingTmx(new HTMLFilter2(), config, "ImportTest2A.htm", "UTF-8", "ImportTest2A.tmx",
                "windows-1252", props, fix);

        List<String> lines1 = readTextFile(new File("test/data/tmx/TMXComplianceKit/ImportTest2A_fr-ca.htm"), "windows-1252");
        List<String> lines2 = readTextFile(outFile, "windows-1252");

        // fix meta line, since OmegaT writes own meta line for encoding
        lines2.set(2, "<meta content=\"text/html; charset=windows-1252\" http-equiv=\"Content-Type\">");

        assertEquals(lines1.size(), lines2.size());
        for (int i = 0; i < lines1.size(); i++) {
            // HTML spec allows unescaped U+0022 QUOTE MARK (outside of attribute values);
            // we produce unescaped but the test assumes escaped, so we normalize for comparison purposes.
            String line1 = normalize(lines1.get(i));
            String line2 = normalize(lines2.get(i));
            assertEquals(line1, line2);
        }
    }

    @Test
    public void testImport2A0index() throws Exception {
        ProjectProperties props = new TestProjectProperties("EN-US", "FR-CA");
        props.setSentenceSegmentingEnabled(true);
        Map<String, String> config = new TreeMap<String, String>();
        config.put(HTMLOptions.OPTION_TRANSLATE_SRC, "false");
        config.put(HTMLOptions.OPTION_SKIP_META, "true");

        Map<String, TMXEntry> fix = new TreeMap<String, TMXEntry>();
        fix.put("Picture:", createTMXEntry("Picture:", "Image:", true));
        translateUsingTmx(new HTMLFilter2(), config, "ImportTest2A.htm", "UTF-8", "ImportTest2A-0index.tmx",
                "windows-1252", props, fix);

        List<String> lines1 = readTextFile(new File("test/data/tmx/TMXComplianceKit/ImportTest2A_fr-ca.htm"),
                "windows-1252");
        List<String> lines2 = readTextFile(outFile, "windows-1252");

        // fix meta line, since OmegaT writes own meta line for encoding
        lines2.set(2, "<meta content=\"text/html; charset=windows-1252\" http-equiv=\"Content-Type\">");

        assertEquals(lines1.size(), lines2.size());
        for (int i = 0; i < lines1.size(); i++) {
            // HTML spec allows unescaped U+0022 QUOTE MARK (outside of attribute values);
            // we produce unescaped but the test assumes escaped, so we normalize for comparison purposes.
            String line1 = normalize(lines1.get(i));
            String line2 = normalize(lines2.get(i));
            assertEquals(line1, line2);
        }
    }

    private String normalize(String str) {
        return str.replace("&quot;", "\"");
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
        props.setSentenceSegmentingEnabled(true);

        FilterContext fc = new FilterContext(props);
        fc.setInEncoding("windows-1252");

        Map<String, String> config = new TreeMap<String, String>();
        new HTMLOptions(config).setSkipMeta("content=en-us,content=fr-ca");

        List<String> sources = loadTexts(new HTMLFilter2(), sourceFile, null, fc, config);
        List<String> translations = loadTexts(new HTMLFilter2(), translatedFile, null, fc, config);

        assertEquals(sources.size(), translations.size());

        ProjectTMX tmx = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), outFile, orphanedCallback);

        for (int i = 0; i < sources.size(); i++) {
            tmx.defaults.put(sources.get(i), createTMXEntry(sources.get(i), translations.get(i), true));
        }

        tmx.exportTMX(props, outFile, false, true, true);

        compareTMX(tmxFile, outFile, 12);
    }

    TMXEntry createTMXEntry(String source, String translation, boolean def) {
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.source = source;
        tr.translation = translation;
        return new TMXEntry(tr, def, null);
    }
}
