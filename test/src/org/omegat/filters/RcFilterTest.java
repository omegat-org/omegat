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
        context.setInEncoding("UTF-8");
        context.setOutEncoding("UTF-8");
        align(new RcFilter(), "Rc/prog.rc", "Rc/prog_be.rc", callback);
        checkAlignStart(callback);
        // Translation strings are Russian(?); originally they were actual UTF-8,
        // but that causes the test to fail when environment encodings aren't set
        // correctly, so we are now using Unicode literals.
        checkAlignById("103/512", "&File", "&\u0424\u0430\u0439\u043B", null);
        checkAlignById("103/601", "&Import...", "&\u0406\u043C\u043F\u0430\u0440\u0442\u0430\u0432\u0430\u0446\u044C...", null);
        checkAlignById("103/602", "&Export...", "&\u042D\u043A\u0441\u043F\u0430\u0440\u0442\u0430\u0432\u0430\u0446\u044C...", null);
        checkAlignById("103/603", "Exit", "\u0412\u044B\u0439\u0441\u044C\u0446\u0456", null);
        checkAlignById("/61", "Error Import", "\u041F\u0430\u043C\u044B\u043B\u043A\u0430 \u0456\u043C\u043F\u0430\u0440\u0442\u0443", null);
        checkAlignById("/62", "Error Output", "\u041F\u0430\u043C\u044B\u043B\u043A\u0430 \u0432\u044B\u0432\u0430\u0434\u0443", null);
        checkAlignById("/63", "Exiting...", "\u0412\u044B\u0439\u0441\u044C\u0446\u0435...", null);
        checkAlignEnd();
    }
}
