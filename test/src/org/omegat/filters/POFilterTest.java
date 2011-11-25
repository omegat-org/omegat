/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.data.IProject;
import org.omegat.filters2.po.PoFilter;
import org.omegat.util.OStrings;

public class POFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        Map<String, String> data = new TreeMap<String, String>();
        Map<String, String> tmx = new TreeMap<String, String>();

        parse2(new PoFilter(), "test/data/filters/po/file-POFilter-be.po", data, tmx);

        assertEquals(data.get("non-fuzzy"), "non-fuzzy translation");
        assertEquals(tmx.get("[PO-fuzzy] fuzzy"), "fuzzy translation");
        assertEquals(tmx.get("[PO-fuzzy] Delete Account"), "Supprimer le compte");
        assertEquals(tmx.get("[PO-fuzzy] Delete Accounts"), "Supprimer des comptes");
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/po/file-POFilter-multiple.po";
        IProject.FileInfo fi = loadSourceFiles(new PoFilter(), f);

        String comment = OStrings.getString("POFILTER_TRANSLATOR_COMMENTS") + "\n" + "A valid comment\nAnother valid comment\n\n" 
        + OStrings.getString("POFILTER_EXTRACTED_COMMENTS") + "\n" + "Some extracted comments\nMore extracted comments\n\n"
        + OStrings.getString("POFILTER_REFERENCES") + "\n" + "/my/source/file\n/my/source/file2\n\n"; 

        checkMultiStart(fi, f);
        checkMulti("source1", null, "some context", null, null, comment);
        checkMulti("source2", null, "", null, null, null);
        checkMulti("source3", null, "", null, null, null);
        checkMulti("source1", null, "", null, null, null);
        checkMulti("source1", null, "other context", null, null, null);
        checkMultiEnd();
    }

    public void testTranslate() throws Exception {
        // translateText(new PoFilter(),
        // "test/data/filters/po/file-POFilter-be.po");
    }

}
