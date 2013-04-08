/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.util.Language;

public class HTMLFilter2Test extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new HTMLFilter2(), "test/data/filters/html/file-HTMLFilter2.html");
        assertEquals(entries.size(), 2);
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new HTMLFilter2(), "test/data/filters/html/file-HTMLFilter2.html");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/html/file-HTMLFilter2.html";
        IProject.FileInfo fi = loadSourceFiles(new HTMLFilter2(), f);

        checkMultiStart(fi, f);
        checkMulti("This is first line.", null, null, "", "This is second line.", null);
        checkMulti("This is second line.", null, null, "This is first line.", "", null);
        checkMultiEnd();
    }

    @Test
    public void testTagsOptimization() throws Exception {
        String f = "test/data/filters/html/file-HTMLFilter2-tags-optimization.html";
        HTMLFilter2 filter = new HTMLFilter2();

        Core.getFilterMaster().getConfig().setRemoveTags(false);
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("<i0/><b1><c2>This</c2> is <i3>first</i3> line.</b1>", null, null, null);
        translateXML(filter, f);

        Core.getFilterMaster().getConfig().setRemoveTags(true);
        filter.isFileSupported(new File(f), new TreeMap<String, String>(), new FilterContext(new Language("en"),
                new Language("be"), false));
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMultiNoPrevNext("<c0>This</c0> is <i1>first</i1> line.", null, null, null);
        translateXML(filter, f);
    }
}
