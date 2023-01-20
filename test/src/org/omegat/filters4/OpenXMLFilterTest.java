/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2022 Thomas Cordonnier
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters4;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters4.xml.openxml.MsOfficeFileFilter;
import org.omegat.util.Language;

public class OpenXMLFilterTest extends org.omegat.filters.TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new MsOfficeFileFilter(), "test/data/filters/openXML/file-OpenXMLFilter.docx");
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    @Test
    public void testTranslate() throws Exception {
        // This filter has capability to change target language inside the translated file
        // But to do so, it takes info from project properties, so they must exist
        Core.getProject().getProjectProperties().setSourceLanguage(new Language("en-US"));
        Core.getProject().getProjectProperties().setTargetLanguage(new Language("fr-FR"));

        File in = new File("test/data/filters/openXML/file-OpenXMLFilter.docx");
        translate(new MsOfficeFileFilter(), in.getPath());

        // XML comparison should not work, 
        // because StaX filter for OpenXML also removes useless repetitions in the Word document!
        /*for (String f : new String[] { "word/document.xml" }) {
            compareXML(new URL("jar:file:" + in.getAbsolutePath() + "!/" + f),
                    new URL("jar:file:" + outFile.getAbsolutePath() + "!/" + f));
        }*/
        
        // So, instead we almost check that the contents matches correctly
        List<String> entries = parse(new MsOfficeFileFilter(), outFile.toString());
        assertEquals(2, entries.size());
        assertEquals("This is first line.", entries.get(0));
        assertEquals("This is second line.", entries.get(1));
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/openXML/file-OpenXMLFilter.docx";
        IProject.FileInfo fi = loadSourceFiles(new MsOfficeFileFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("This is first line.", null, null, "", "This is second line.", null);
        checkMulti("This is second line.", null, null, "This is first line.", "", null);
        checkMultiEnd();
    }
}
