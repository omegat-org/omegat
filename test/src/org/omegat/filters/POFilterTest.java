/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.ITMXEntry;
import org.omegat.filters2.po.PoFilter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

public class POFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        Map<String, String> data = new TreeMap<String, String>();
        Map<String, String> tmx = new TreeMap<String, String>();

        parse2(new PoFilter(), "test/data/filters/po/file-POFilter-be.po", data, tmx);

        assertEquals(data.get("non-fuzzy"), "non-fuzzy translation");
        assertEquals(tmx.get("[PO-fuzzy] fuzzy"), "fuzzy translation");
        assertEquals(tmx.get("[PO-fuzzy] Delete Account"), "Supprimer le compte");
        assertEquals(tmx.get("[PO-fuzzy] Delete Accounts"), "Supprimer des comptes");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/po/file-POFilter-multiple.po";
        Map<String, String> options = new TreeMap<String, String>();
        options.put("skipHeader", "true");
        TestFileInfo fi = loadSourceFiles(new PoFilter(), f, options);

        String comment = OStrings.getString("POFILTER_TRANSLATOR_COMMENTS") + "\n"
                + "A valid comment\nAnother valid comment\n\n" + OStrings.getString("POFILTER_EXTRACTED_COMMENTS")
                + "\n" + "Some extracted comments\nMore extracted comments\n\n"
                + OStrings.getString("POFILTER_REFERENCES") + "\n" + "/my/source/file\n/my/source/file2\n\n";

        checkMultiStart(fi, f);
        checkMulti("source1", null, "some context", null, null, comment);
        checkMulti("source2", null, "", null, null, null);
        checkMulti("source3", null, "", null, null, null);
        checkMulti("source1", null, "", null, null, null);
        checkMulti("source1", null, "other context", null, null, null);
        checkMulti("source4", null, "one more context", null, null, null);
        checkMulti("source4", null, "one more context[1]", null, null,
                StringUtil.format(OStrings.getString("POFILTER_PLURAL_FORM_COMMENT"), 1) + "\n");
        checkMulti("source4", null, "one more context[2]", null, null,
                StringUtil.format(OStrings.getString("POFILTER_PLURAL_FORM_COMMENT"), 2) + "\n");
        checkMulti("source5", null, "", null, null, null);
        checkMulti("source6", null, "", null, null, null);
        checkMultiEnd();

        ExternalTMX tmEntries = fi.referenceEntries;
        assertEquals(2, tmEntries.getEntries().size());
        {
            ITMXEntry entry = tmEntries.getEntries().get(0);
            assertEquals("True fuzzy!", entry.getSourceText());
            assertEquals("trans5", entry.getTranslationText());
        }
        {
            ITMXEntry entry = tmEntries.getEntries().get(1);
            assertEquals("True fuzzy 2!", entry.getSourceText());
            assertEquals("trans6", entry.getTranslationText());
        }
    }

    @Test
    public void testLoadMonolingual() throws Exception {
        String f = "test/data/filters/po/file-POFilter-Monolingual.po";
        PoFilter filter = new PoFilter();
        Map<String, String> options = new TreeMap<String, String>();
        options.put(PoFilter.OPTION_FORMAT_MONOLINGUAL, "true");
        List<ParsedEntry> parsed = parse3(filter, f, options);
        assertEquals(2, parsed.size());
        assertEquals("firstId", parsed.get(0).id);
        assertEquals("first source", parsed.get(0).source);
        assertEquals("secondId", parsed.get(1).id);
        assertEquals("second source", parsed.get(1).source);
    }

    @Test
    public void testTranslate() throws Exception {
        // translateText(new PoFilter(),
        // "test/data/filters/po/file-POFilter-be.po");
    }

    @Test
    public void testLoad2() throws Exception {
        String f = "test/data/filters/po/file-POFilter-multiple2.po";
        Map<String, String> options = new HashMap<>();
        options.put(PoFilter.OPTION_SKIP_HEADER, "true");
        options.put(PoFilter.OPTION_ALLOW_EDITING_BLANK_SEGMENT, "true");
        TestFileInfo fi = loadSourceFiles(new PoFilter(), f, options);

        checkMultiStart(fi, f);
        checkMulti("Changed %d key", null, "", null, null, null);
        checkMulti("Changed %d keys", null, "[1]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 1) + "\n");
        checkMulti("Changed %d keys", null, "[2]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 2) + "\n");
        checkMulti("Changed %d keys", null, "[3]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 3) + "\n");
        checkMulti("../foo/boo.c, 123", null, "", null, null,
                OStrings.getString("POFILTER_REFERENCES") + "\n" + "../foo/boo.c, 123\n" + "\n");
        checkMultiEnd();
    }

    @Test
    public void testLoad3() throws Exception {
        String f = "test/data/filters/po/file-POFilter-multiple2.po";
        Map<String, String> options = new HashMap<>();
        options.put(PoFilter.OPTION_SKIP_HEADER, "true");
        options.put(PoFilter.OPTION_ALLOW_EDITING_BLANK_SEGMENT, "false");
        TestFileInfo fi = loadSourceFiles(new PoFilter(), f, options);

        checkMultiStart(fi, f);
        checkMulti("Changed %d key", null, "", null, null, null);
        checkMulti("Changed %d keys", null, "[1]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 1) + "\n");
        checkMulti("Changed %d keys", null, "[2]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 2) + "\n");
        checkMulti("Changed %d keys", null, "[3]", null, null,
                OStrings.getString("POFILTER_PLURAL_FORM_COMMENT", 3) + "\n");
        checkMultiEnd();
    }
}
