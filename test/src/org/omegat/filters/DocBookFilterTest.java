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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters;

import java.util.List;

import org.omegat.filters3.xml.docbook.DocBookFilter;

public class DocBookFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new DocBookFilter(),
                "test/data/filters/docBook/file-DocBookFilter.xml");
    }

    public void testTranslate() throws Exception {
        translateText(new DocBookFilter(),
                "test/data/filters/docBook/file-DocBookFilter.xml");
    }

    public void testParseIntroLinux() throws Exception {
        List<String> lines = parse(new DocBookFilter(),
                "test/data/filters/docBook/Intro-Linux/abook.xml");
        assertTrue("Message not exist, i.e. entities not loaded", lines
                .contains("Why should I use an editor?"));
    }
}
