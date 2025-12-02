/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.html2.HTMLOptions;
import org.omegat.filters3.xml.xhtml.XHTMLFilter;
import org.omegat.filters3.xml.xhtml.XHTMLOptions;
import org.omegat.util.Language;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertTrue;

public class XHTMLFilterTest extends TestFilterBase {
    @Before
    public final void setUp() {
        // Use custom EntityResolver to resolve DTD and entity files
        // to our locally provided files. Otherwise Java will actually
        // try to download them over the network each time, which is
        // *really* slow.
        // See http://stackoverflow.com/a/9398602
        EntityResolver er = new EntityResolver() {
            @SuppressWarnings("resource")
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                String filename = new File(systemId).getName();
                File localFile = new File("test/data/dtd", filename);
                if (localFile.exists()) {
                    return new InputSource(new FileInputStream(localFile));
                }
                throw new IOException("Could not resolve: " + publicId + " / " + systemId);
            }
        };
        XMLUnit.setTestEntityResolver(er);
        XMLUnit.setControlEntityResolver(er);
    }

    @After
    public final void tearDown() throws Exception {
        XMLUnit.setControlEntityResolver(null);
        XMLUnit.setTestEntityResolver(null);
    }

    @Test
    public void testParse() throws Exception {
        String f = "test/data/filters/xhtml/file-XHTMLFilter.html";
        XHTMLFilter filter = new XHTMLFilter();
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));

        parse(filter, f);
    }

    @Test
    public void testTranslate() throws Exception {
        String f = "test/data/filters/xhtml/file-XHTMLFilter.html";
        XHTMLFilter filter = new XHTMLFilter();
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        translateXML(filter, f);
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/xhtml/file-XHTMLFilter.html";
        XHTMLFilter filter = new XHTMLFilter();
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("en", null, null, "", "en", null);
        checkMulti("en", null, null, "en", "XHTML 1.0 Example", null);
        checkMulti("XHTML 1.0 Example", null, null, "en", "Extensible HyperText Markup Language", null);
        checkMulti("Extensible HyperText Markup Language", null, null, "XHTML 1.0 Example",
                "http://www.w3.org/Icons/valid-xhtml10", null);
    }

    @Test
    public void testTagsOptimization() throws Exception {
        String f = "test/data/filters/xhtml/file-XHTMLFilter-tags-optimization.html";
        XHTMLFilter filter = new XHTMLFilter();

        Core.getFilterMaster().getConfig().setRemoveTags(false);
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("en", null, null, null);
        checkMultiNoPrevNext("en", null, null, null);
        checkMultiNoPrevNext("<i0/><b1><c2>This</c2> is <i3>first</i3> line.</b1>", null, null, null);
        translateXML(filter, f);

        Core.getFilterMaster().getConfig().setRemoveTags(true);
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("en", null, null, null);
        checkMultiNoPrevNext("en", null, null, null);
        checkMultiNoPrevNext("<c0>This</c0> is <i1>first</i1> line.", null, null, null);
        translateXML(filter, f);
    }

    @Test
    public void testBadDocTypeIgnore() throws Exception {
        String f = "test/data/filters/xhtml/p-000-source.xhtml";
        String expected = "test/data/filters/xhtml/p-000-source-compress-space.xhtml";
        var filter = new XHTMLFilter();

        Map<String, String> config = new TreeMap<>();
        final String TRUE = Boolean.toString(true);
        final String FALSE = Boolean.toString(false);
        config.put(XHTMLOptions.OPTION_SKIP_META, TRUE);
        config.put(XHTMLOptions.OPTION_TRANSLATE_SRC, TRUE);
        config.put(XHTMLOptions.OPTION_IGNORE_TAGS, "");
        config.put(XHTMLOptions.OPTION_IGNORE_DOCTYPE, TRUE);
        Core.getFilterMaster().getConfig().setRemoveTags(true);

        assertTrue(filter.isFileSupported(new File(f), config, new FilterContext(new Language("zh_TW"),
                new Language("en"), false)));

        translate(filter, f, config);
        compareXML(new File(expected), outFile);
    }
}
