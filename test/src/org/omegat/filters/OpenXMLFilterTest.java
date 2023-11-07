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

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.openxml.OpenXMLFilter;

public class OpenXMLFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new OpenXMLFilter(), "test/data/filters/openXML/file-OpenXMLFilter.docx");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    @Test
    public void testTranslate() throws Exception {
        File in = new File("test/data/filters/openXML/file-OpenXMLFilter.docx");
        translate(new OpenXMLFilter(), in.getPath());

        for (String f : new String[] { "word/document.xml" }) {
            compareXML(new URL("jar:file:" + in.getAbsolutePath() + "!/" + f),
                    new URL("jar:file:" + outFile.getAbsolutePath() + "!/" + f));
        }
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/openXML/file-OpenXMLFilter.docx";
        IProject.FileInfo fi = loadSourceFiles(new OpenXMLFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("This is first line.", null, null, "", "This is second line.", null);
        checkMulti("This is second line.", null, null, "This is first line.", "", null);
        checkMultiEnd();
    }


    @Test
    public void testParseTags() throws Exception {
        boolean removeSpacesOrig = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
        Core.getFilterMaster().getConfig().setRemoveSpacesNonseg(true);

        String f = "test/data/filters/openXML/file-OpenXMLFilter-tags.docx";
        OpenXMLFilter filter = new OpenXMLFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("The black widow has a black cat.", null, null, "",
                "<t0>The black widow</t0> has a <t1>black cat.</t1>", null);
        checkMulti("<t0>The black widow</t0> has a <t1>black cat.</t1>", null, null,
                "The black widow has a black cat.",
                "<t0>The black widow</t0> has a black cat.", null);
        checkMulti("<t0>The black widow</t0> has a black cat.", null, null,
                "<t0>The black widow</t0> has a <t1>black cat.</t1>",
                "The black widow has a <t0>black cat.</t0>", null);
        checkMulti("The black widow has a <t0>black cat.</t0>", null, null,
                "<t0>The black widow</t0> has a black cat.",
                "The black widow has a <t0>black</t0> cat.", null);
        checkMulti("The black widow has a <t0>black</t0> cat.", null, null,
                "The black widow has a <t0>black cat.</t0>", "", null);
        checkMultiEnd();

        Core.getFilterMaster().getConfig().setRemoveSpacesNonseg(removeSpacesOrig);
    }
}
