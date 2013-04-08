/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2010 Volker Berlin
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
import org.omegat.core.data.IProject;
import org.omegat.filters2.text.dokuwiki.DokuWikiFilter;

public class DokuWikiFilterTest extends TestFilterBase {

    @Test
    public void testTextFilterParsing() throws Exception {
        List<String> entries = parse(new DokuWikiFilter(), "test/data/filters/dokuwiki/dokuwiki.txt");
        int i = 0;
        assertEquals("Header", entries.get(i++));
        assertEquals("This is a flow text.", entries.get(i++));
        assertEquals("multiple spaces in text", entries.get(i++));
        assertEquals("* asterisk * asterisk", entries.get(i++));
        assertEquals("list item", entries.get(i++));
        assertEquals("- minus - minus", entries.get(i++));
        assertEquals("numeric item", entries.get(i++));
        assertEquals("before code", entries.get(i++));
        assertEquals("mid code", entries.get(i++));
        assertEquals("after code", entries.get(i++));
        assertEquals("<del>deleted</del>", entries.get(i++));
        assertEquals("header", entries.get(i++));
        assertEquals("cell1", entries.get(i++));
        assertEquals("cell2", entries.get(i++));
        assertEquals("cell3 {{..:images:f_n_16.png|Number}}", entries.get(i++));
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new DokuWikiFilter(), "test/data/filters/dokuwiki/dokuwiki-translate.txt");
    }

    @Test
    public void testIsFileSupported() {
        DokuWikiFilter filter = new DokuWikiFilter();
        assertTrue(filter.isFileSupported(new File("test/data/filters/dokuwiki/dokuwiki.txt"),
                new TreeMap<String, String>(), context));
        assertFalse(filter.isFileSupported(new File("test/data/filters/text/text1.txt"),
                new TreeMap<String, String>(), context));
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/dokuwiki/dokuwiki.txt";
        IProject.FileInfo fi = loadSourceFiles(new DokuWikiFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Header", null, null, "", "This is a flow text.", null);
        checkMulti("This is a flow text.", null, null, "Header", "multiple spaces in text", null);
        checkMulti("multiple spaces in text", null, null, "This is a flow text.", "* asterisk * asterisk", null);
    }
}
