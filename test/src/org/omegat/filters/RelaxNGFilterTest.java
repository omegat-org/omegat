/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2016 Tony Graham
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.relaxng.RelaxNGFilter;

public class RelaxNGFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        List<String> lines = parse(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
        boolean c = lines.contains("RELAX NG is a schema language for XML.");
        assertTrue("'RELAX NG is a schema language for XML.' not defined'", c);
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
    }

    @Test
    public void testParseIntroLinux() throws Exception {
        List<String> lines = parse(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
        assertTrue("Message not exist, i.e. entities not loaded",
                lines.contains("RELAX NG is a schema language for XML."));
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/relaxng/relaxng.rng";
        IProject.FileInfo fi = loadSourceFiles(new RelaxNGFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("RELAX NG is a schema language for XML.", null, null, "", "RELAX NG is simple and easy to learn.", null);
    }

    @Test
    public void testIsSupported() throws Exception {
        RelaxNGFilter filter = new RelaxNGFilter();
        Path goodFile = Paths.get("test/data/filters/relaxng/relaxng.rng");
        try (BufferedReader reader = Files.newBufferedReader(goodFile)) {
            assertTrue(filter.isFileSupported(reader));
        }
        Path badFile = Paths.get("test/data/filters/relaxng/relaxng-invalid.rng");
        try (BufferedReader reader = Files.newBufferedReader(badFile)) {
            assertFalse(filter.isFileSupported(reader));
        }
        badFile = Paths.get("test/data/filters/relaxng/relaxng-invalid-ns.rng");
        try (BufferedReader reader = Files.newBufferedReader(badFile)) {
            assertFalse(filter.isFileSupported(reader));
        }
    }
}
