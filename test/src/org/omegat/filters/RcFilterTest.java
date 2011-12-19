/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.rc.RcFilter;

public class RcFilterTest extends TestFilterBase {
    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/Rc/prog.rc";
        IProject.FileInfo fi = loadSourceFiles(new RcFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("&File", "103/512", null, null, null, null);
        checkMulti("&Import...", "103/601", null, null, null, null);
    }

    @Test
    public void testAlign() throws Exception {
        TestAlignCallback callback = new TestAlignCallback();
        align(new RcFilter(), "Rc/prog.rc", "Rc/prog_be.rc", callback);
        checkAlignStart(callback);
        checkAlignById("103/512", "&File", "&Файл", null);
        checkAlignById("103/601", "&Import...", "&Імпартаваць...", null);
        checkAlignById("103/602", "&Export...", "&Экспартаваць...", null);
        checkAlignById("103/603", "Exit", "Выйсьці", null);
        checkAlignById("/61", "Error Import", "Памылка імпарту", null);
        checkAlignById("/62", "Error Output", "Памылка вываду", null);
        checkAlignById("/63", "Exiting...", "Выйсьце...", null);
        checkAlignEnd();
    }
}
