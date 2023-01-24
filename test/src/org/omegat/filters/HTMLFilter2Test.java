/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.html2.HTMLOptions;
import org.omegat.util.HTMLUtils;
import org.omegat.util.Language;
import org.omegat.util.OStrings;

public class HTMLFilter2Test extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new HTMLFilter2(), "test/data/filters/html/file-HTMLFilter2.html");
        assertEquals(3, entries.size());
        assertEquals("en", entries.get(0));
        assertEquals("This is first line.", entries.get(1));
        assertEquals("This is second line.", entries.get(2));
    }

    @Test
    public void testParseAllBlockElements() throws Exception {
        List<String> entries = parse(new HTMLFilter2(),
                "test/data/filters/html/file-HTMLFilter2-all-block-elements.html");
        assertEquals(49, entries.size());
    }

    @Test
    public void testTranslate() throws Exception {
        HTMLFilter2 filter = new HTMLFilter2();
        translateText(filter, "test/data/filters/html/file-HTMLFilter2.html");
        translateText(filter, "test/data/filters/html/file-HTMLFilter2-SMP.html");
        translateText(filter, "test/data/filters/html/file-HTMLFilter2-recurse-bugfix-SF205.html");
        translateText(filter, "test/data/filters/html/file-HTMLFilter2-tag-dropping-bugfix-SF609.html");
        translateText(filter, "test/data/filters/html/file-HTMLFilter2-all-block-elements.html");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/html/file-HTMLFilter2.html";
        HTMLFilter2 filter = new HTMLFilter2();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("en", null, null, "", "This is first line.",
                   String.format("%s HTML %s lang",
                                 OStrings.getString("HTMLFILTER_TAG"),
                                 OStrings.getString("HTMLFILTER_ATTRIBUTE")));
        checkMulti("This is first line.", null, null, "en", "This is second line.", null);
        checkMulti("This is second line.", null, null, "This is first line.", "", null);
        checkMultiEnd();

        f = "test/data/filters/html/file-HTMLFilter2-SMP.html";
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("\uD835\uDC00\uD835\uDC01\uD835\uDC02", null, null, "", "\uD835\uDC03\uD835\uDC04\uD835\uDC05", null);
        checkMulti("\uD835\uDC03\uD835\uDC04\uD835\uDC05", null, null, "\uD835\uDC00\uD835\uDC01\uD835\uDC02", "", null);
        checkMultiEnd();
    }

    @Test
    public void testTagsOptimization() throws Exception {
        String f = "test/data/filters/html/file-HTMLFilter2-tags-optimization.html";
        HTMLFilter2 filter = new HTMLFilter2();

        Core.getFilterMaster().getConfig().setRemoveTags(false);
        filter.isFileSupported(new File(f), Collections.emptyMap(), new FilterContext(new Language("en"),
                new Language("be"), false));
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("<i0/><b1><c2>This</c2> is <i3>first</i3> line.</b1>", null, null, null);
        translateXML(filter, f);

        Core.getFilterMaster().getConfig().setRemoveTags(true);
        filter.isFileSupported(new File(f), Collections.emptyMap(), new FilterContext(new Language("en"),
                new Language("be"), false));
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("<c0>This</c0> is <i1>first</i1> line.", null, null, null);
        translateXML(filter, f);
    }

    @Test
    public void testHtmlEntityDecode() {
        // Should decode &apos; (was missing for some reason)
        assertEquals("foo 'bar'", HTMLUtils.entitiesToChars("foo &apos;bar&apos;"));
    }

    @Test
    public void testLayout() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "NEVER");

        String filename = "test/data/filters/html/file-HTMLFilter2-layout.html";
        translateText(new HTMLFilter2(), filename, config);

        testExtractedSTEForLayoutTest(config);
    }

    @Test
    public void testLayoutTrimWhitespace() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_COMPRESS_WHITESPACE, "true");
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "NEVER");
        translate(new HTMLFilter2(), "test/data/filters/html/file-HTMLFilter2-layout.html", config);
        compareBinary(new File("test/data/filters/html/file-HTMLFilter2-layout-compressed.html"), outFile);

        testExtractedSTEForLayoutTest(config);
    }

    @Test
    public void testLayoutPreserveWhitespace() throws Exception {
        boolean preserveSpacesOrig = Core.getFilterMaster().getConfig().isPreserveSpaces();
        Core.getFilterMaster().getConfig().setPreserveSpaces(true);

        //preserving whitespace is the default, so nothing changes when enabling this, compared to testLayout() test.
        testLayout();

        Core.getFilterMaster().getConfig().setPreserveSpaces(preserveSpacesOrig);
    }

    private void testExtractedSTEForLayoutTest(Map<String, String> filterOptions) throws Exception {
        String filename = "test/data/filters/html/file-HTMLFilter2-layout.html";
        IProject.FileInfo fi = loadSourceFiles(new HTMLFilter2(), filename, filterOptions);
        int i = 0;
        assertEquals("test", fi.entries.get(i++).getSrcText());
        assertEquals("ltr", fi.entries.get(i++).getSrcText());
        assertEquals("test spaces", fi.entries.get(i++).getSrcText());
        assertEquals("test spaces", fi.entries.get(i++).getSrcText());
        assertEquals("outoftags", fi.entries.get(i++).getSrcText());
        assertEquals("This <b0>is bold</b0>!", fi.entries.get(i++).getSrcText());
        // empty segments are skipped
        assertEquals("formatted\n    text", fi.entries.get(i++).getSrcText());
        assertEquals("a", fi.entries.get(i++).getSrcText());
        assertEquals(i, fi.entries.size());
    }

    @Test
    public void testAddCharsetHeaderWhenNoHeader() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "ALWAYS");
        String noHeaderFile = "test/data/filters/html/file-HTMLFilter2.html";
        String addedHeaderFile = "test/data/filters/html/file-HTMLFilter2-added-charset.html";
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFHEADER");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(noHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFMETA");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(noHeaderFile), outFile);
    }

    @Test
    public void testAddCharsetHeaderWhenExistingHeader() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "ALWAYS");
        String noHeaderFile = "test/data/filters/html/file-HTMLFilter2-headernocharset.html";
        String addedHeaderFile = "test/data/filters/html/file-HTMLFilter2-added-charset.html";
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFHEADER");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFMETA");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(noHeaderFile), outFile);
    }

    @Test
    public void testAddCharsetHeaderWhenExistingMeta() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "ALWAYS");
        String noHeaderFile = "test/data/filters/html/file-HTMLFilter2-headerdifferentcharset.html";
        String addedHeaderFile = "test/data/filters/html/file-HTMLFilter2-added-charset.html";
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFHEADER");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFMETA");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);
    }

    @Test
    public void testAddCharsetHeaderHtml5WhenExistingMeta() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "ALWAYS");
        String noHeaderFile = "test/data/filters/html/file-HTMLFilter2-HTML5-headerdifferentcharset.html";
        String addedHeaderFile = "test/data/filters/html/file-HTMLFilter2-HTML5-added-charset.html";
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFHEADER");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);

        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "IFMETA");
        translate(new HTMLFilter2(), noHeaderFile, config);
        compareBinary(new File(addedHeaderFile), outFile);
    }

}
