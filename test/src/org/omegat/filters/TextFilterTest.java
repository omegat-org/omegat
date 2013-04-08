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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.text.TextFilter;

public class TextFilterTest extends TestFilterBase {
    @Test
    public void testTextFilterParsing() throws Exception {
        List<String> entries = parse(new TextFilter(), "test/data/filters/text/text1.txt");
        assertEquals("First entry\r\n", entries.get(0));
    }

    public void testTranslate() throws Exception {
        translateText(new TextFilter(), "test/data/filters/text/text1.txt");
    }

    @Test
    public void testParseNeverBreak() throws Exception {
        checkFile(TextFilter.SEGMENT_NEVER, 1, 1);
    }

    @Test
    public void testParseEmptyLinesBreak() throws Exception {
        checkFile(TextFilter.SEGMENT_EMPTYLINES, 3, 1);
    }

    @Test
    public void testParseLinesBreak() throws Exception {
        checkFile(TextFilter.SEGMENT_BREAKS, 3, 3);
    }

    protected void checkFile(String segValue, int count1, int count2) throws Exception {
        Map<String, String> options = new TreeMap<String, String>();
        options.put(TextFilter.OPTION_SEGMENT_ON, segValue);

        TextFilter filter = new TextFilter();

        List<String> entries = parse(filter, "test/data/filters/text/file-TextFilter.txt", options);
        assertEquals(count1, entries.size());

        entries = parse(filter, "test/data/filters/text/file-TextFilter-noemptylines.txt", options);
        assertEquals(count2, entries.size());
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/text/file-TextFilter-multiple.txt";
        IProject.FileInfo fi = loadSourceFiles(new TextFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("line1", null, null, "", "line2", null);
        checkMulti("line2", null, null, "line1", "line3", null);
        checkMulti("line3", null, null, "line2", "", null);
        checkMultiEnd();
    }
}
