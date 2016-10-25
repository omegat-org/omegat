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
import org.omegat.filters3.xml.relaxng.RelaxNGFilter;

public class RelaxNGFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> lines = parse(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
        boolean c = lines.contains("RELAX NG is a schema language for XML.");
        assertTrue("'RELAX NG is a schema language for XML.' not defined'", c);
    }

    public void testTranslate() throws Exception {
        translateText(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
    }

    public void testParseIntroLinux() throws Exception {
        List<String> lines = parse(new RelaxNGFilter(), "test/data/filters/relaxng/relaxng.rng");
        assertTrue("Message not exist, i.e. entities not loaded",
                lines.contains("RELAX NG is a schema language for XML."));
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/relaxng/relaxng.rng";
        IProject.FileInfo fi = loadSourceFiles(new RelaxNGFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("RELAX NG is a schema language for XML.", null, null, "", "RELAX NG is simple and easy to learn.", null);
    }
}
