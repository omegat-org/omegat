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

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters;

import java.util.List;

import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.docbook.DocBookFilter;

public class DocBookFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> lines = parse(new DocBookFilter(), "test/data/filters/docBook/file-DocBookFilter.xml");
        boolean c = lines.contains("My String");
        assertTrue("'My String' not defined'", c);
    }

    public void testTranslate() throws Exception {
        translateText(new DocBookFilter(), "test/data/filters/docBook/file-DocBookFilter.xml");
    }

    public void testParseIntroLinux() throws Exception {
        List<String> lines = parse(new DocBookFilter(), "test/data/filters/docBook/Intro-Linux/abook.xml");
        assertTrue("Message not exist, i.e. entities not loaded",
                lines.contains("Why should I use an editor?"));
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/docBook/Intro-Linux/abook.xml";
        IProject.FileInfo fi = loadSourceFiles(new DocBookFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Introduction to Linux", null, null, "", "A Hands on Guide", null);
        checkMulti("A Hands on Guide", null, null, "Introduction to Linux", "Machtelt", null);
        checkMulti("Machtelt", null, null, "A Hands on Guide", "Garrels", null);
    }
}
