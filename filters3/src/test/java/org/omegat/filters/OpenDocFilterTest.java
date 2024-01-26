/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.opendoc.OpenDocFilter;

public class OpenDocFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new OpenDocFilter(), "test/data/filters/openDoc/file-OpenDocFilter.odt");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    @Test
    public void testTranslate() throws Exception {
        File in = new File("test/data/filters/openDoc/file-OpenDocFilter.odt");
        translate(new OpenDocFilter(), in.getPath());

        for (String f : new String[] { "content.xml", "styles.xml", "meta.xml" }) {
            compareXML(new URL("jar:file:" + in.getAbsolutePath() + "!/" + f),
                    new URL("jar:file:" + outFile.getAbsolutePath() + "!/" + f));
        }
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/openDoc/file-OpenDocFilter.odt";
        IProject.FileInfo fi = loadSourceFiles(new OpenDocFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("This is first line.", null, null, "", "This is second line.", null);
        checkMulti("This is second line.", null, null, "This is first line.", "", null);
        checkMultiEnd();
    }
}
