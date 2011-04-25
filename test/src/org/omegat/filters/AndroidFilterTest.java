/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/
package org.omegat.filters;

import java.util.List;

import org.omegat.core.data.IProject;
import org.omegat.filters3.xml.android.AndroidFilter;

public class AndroidFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<ParsedEntry> lines = parse3(new AndroidFilter(),
                "test/data/filters/Android/file-AndroidFilter.xml", null);
        assertTrue("MyApp".equals(lines.get(0).source));
        // assertTrue("Some comment".equals(lines.get(0).comment));
        // assertTrue("app_label".equals(lines.get(0).id));

        assertTrue("T'est".equals(lines.get(2).source));
    }

    public void testTranslate() throws Exception {
        translateXML(new AndroidFilter(), "test/data/filters/Android/file-AndroidFilter.xml");
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/Android/file-AndroidFilter.xml";
        IProject.FileInfo fi = loadSourceFiles(new AndroidFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("MyApp", "app_label", null, null, null, " Some comment ");
        checkMulti("<f0>Welcome !</f0> \\nAdditional comment", "line", null, null, null,
                " This is line for... ");
        checkMulti("T'est", "apo_test", null, null, null, null);
        checkMulti("1 minute", "Nminutes/one", null, null, null, " 1-minute ");
        checkMulti("<x0>%d</x0> minutes", "Nminutes/other", null, null, null, " 2-minute or more ");
        checkMultiEnd();
    }
}
