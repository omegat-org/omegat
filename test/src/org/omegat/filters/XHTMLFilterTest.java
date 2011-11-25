/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters;

import java.io.File;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.FilterContext;
import org.omegat.filters3.xml.xhtml.XHTMLFilter;
import org.omegat.util.Language;

public class XHTMLFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new XHTMLFilter(), "test/data/filters/xhtml/file-XHTMLFilter.html");
    }

    public void testTranslate() throws Exception {
        // translateXML(new XHTMLFilter(), "test/data/filters/xhtml/file-XHTMLFilter.html");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/xhtml/file-XHTMLFilter.html";
        XHTMLFilter filter = new XHTMLFilter();
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language(
                "en"), new Language("be"), false));
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("en", null, null, "", "en", null);
        checkMulti("en", null, null, "en", "XHTML 1.0 Example", null);
        checkMulti("XHTML 1.0 Example", null, null, "en", "Extensible HyperText Markup Language", null);
        checkMulti("Extensible HyperText Markup Language", null, null, "XHTML 1.0 Example",
                "http://www.w3.org/Icons/valid-xhtml10", null);
    }
}
