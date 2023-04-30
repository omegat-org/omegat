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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.hhc.HHCFilter2;
import org.omegat.filters2.html2.HTMLOptions;

public class HHCFilter2Test extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }

    @Test
    public void testTranslate() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        // Rewriting the header will cause the first test to fail.
        // The other files don't have a header.
        config.put(HTMLOptions.OPTION_REWRITE_ENCODING, "NEVER");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc", config);
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/hhc/file-HHCFilter2.hhc";
        IProject.FileInfo fi = loadSourceFiles(new HHCFilter2(), f);

        checkMultiStart(fi, f);
        checkMulti("Introduction to software X", null, null, "", "Building Graphs", null);
        checkMulti("Building Graphs", null, null, "Introduction to software X",
                "Build a File Graph", null);
        checkMulti("Build a File Graph", null, null, "Building Graphs", "", null);
        checkMultiEnd();
    }
}
