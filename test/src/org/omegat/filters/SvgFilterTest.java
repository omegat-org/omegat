/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.svg.SvgFilter;

public class SvgFilterTest extends TestFilterBase {

    public void testLoad() throws Exception {
        String f = "test/data/filters/SVG/Neural_network_example.svg";
        IProject.FileInfo fi = loadSourceFiles(new SvgFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("image/svg+xml", null, null, "", "<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>", null);
        checkMulti("<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>", null, null, "image/svg+xml",
                "<t0><t1>hidden</t1></t0><t2><t3>layer</t3></t2>", null);
        checkMulti("<t0><t1>hidden</t1></t0><t2><t3>layer</t3></t2>", null, null,
                "<t0><t1>input</t1></t0><t2><t3>layer</t3></t2>",
                "<t0><t1>output</t1></t0><t2><t3>layer</t3></t2>", null);
    }
}
