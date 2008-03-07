/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/omegat/omegat.html
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

import java.io.File;
import java.net.URL;
import java.util.List;

import org.omegat.filters3.xml.openxml.OpenXMLFilter;

public class OpenXMLFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> entries = parse(new OpenXMLFilter(), "test/data/filters/openXML/file-OpenXMLFilter.docx");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    public void testTranslate() throws Exception {
        File in = new File("test/data/filters/openXML/file-OpenXMLFilter.docx");
        translate(new OpenXMLFilter(), in.getPath());

        for (String f : new String[] { "word/document.xml" }) {
            compareXML(new URL("jar:file:/" + in.getAbsolutePath() + "!/" + f), new URL("jar:file:/"
                    + outFile.getAbsolutePath() + "!/" + f));
        }
    }
}
