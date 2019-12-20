/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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
package org.omegat.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.pdf.PdfFilter;

public class PdfFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<ParsedEntry> lines = parse3(new PdfFilter(),
                "test/data/filters/pdf/file-PdfFilter.pdf", null);
        assertEquals("This is some text. This is also some text. ", lines.get(0).source);
    }

    @Test
    public void testTranslate() throws Exception {
        translate(new PdfFilter(), "test/data/filters/pdf/file-PdfFilter.pdf", Collections.emptyMap());
        compareBinary(new File("test/data/filters/pdf/file-PdfFilter-gold.txt"), outFile);
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/pdf/file-PdfFilter.pdf";
        IProject.FileInfo fi = loadSourceFiles(new PdfFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("This is some text. This is also some text.", null, null, null, null, null);
        checkMultiEnd();
    }

    @Test
    public void testPasswordProtected() throws Exception {
        String f = "test/data/filters/pdf/file-PdfFilter-password.pdf";
        try {
            loadSourceFiles(new PdfFilter(), f);
            fail("Password-protected PDFs are not supported");
        } catch (TranslationException ex) {
            // OK
        }
    }
}
