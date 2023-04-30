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

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.text.ini.INIFilter;

public class INIFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        parse(new INIFilter(), "test/data/filters/ini/file-INIFilter.ini");
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new INIFilter(), "test/data/filters/ini/file-INIFilter.ini");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/ini/file-INIFilter.ini";
        IProject.FileInfo fi = loadSourceFiles(new INIFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Value", "nsID", null, null, null, null);
        checkMulti("Value", "Section/ID", null, null, null, null);
        checkMulti("Value2", "Section/ID2", null, null, null, null);
        checkMulti("Value3", "Section/ID3", null, null, null, null);
        checkMulti("Value4", "Section/ID4", null, null, null, null);
        checkMulti("Continued Value4", "Section/ID4/#1", null, null, null, null);
        checkMulti("Value5", "Section/ID5", null, null, null, null);
        checkMulti("Continued Value5 start with space", "Section/ID5/#1", null, null, null, null);
        checkMulti("More Continued Value5", "Section/ID5/#2", null, null, null, null);
        checkMulti("Value6", "Section/[ID6]", null, null, null, null);
        checkMulti("Value7 # not comment", "Section/ID7", null, null, null, null);
        checkMulti("Value without ID", "Section/#L31", null, null, null, null);
        checkMultiEnd();
    }
}
