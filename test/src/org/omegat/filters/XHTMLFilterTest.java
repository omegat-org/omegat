/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeMap;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.FilterContext;
import org.omegat.filters3.xml.xhtml.XHTMLFilter;
import org.omegat.util.Language;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XHTMLFilterTest extends TestFilterBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use custom EntityResolver to resolve DTD and entity files
        // to our locally provided files. Otherwise Java will actually
        // try to download them over the network each time, which is
        // *really* slow.
        // See http://stackoverflow.com/a/9398602
        EntityResolver er = new EntityResolver() {
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
}
