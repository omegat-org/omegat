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

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters;

import org.omegat.core.data.IProject;
import org.omegat.filters2.hhc.HHCFilter2;

public class HHCFilter2Test extends TestFilterBase {
    public void testParse() throws Exception {
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        parse(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }

    public void testTranslate() throws Exception {
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2.hhc");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Contents file.hhc");
        translateText(new HHCFilter2(), "test/data/filters/hhc/file-HHCFilter2-Index file.hhk");
    }

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
