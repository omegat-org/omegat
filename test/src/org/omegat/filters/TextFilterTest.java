/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/omegat/omegat.html
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

import org.junit.Test;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.TextOptions;

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
	checkFile(TextOptions.SEGMENT_NEVER, 1, 1);
    }

    @Test
    public void testParseEmptyLinesBreak() throws Exception {
	checkFile(TextOptions.SEGMENT_EMPTYLINES, 3, 1);
    }

    @Test
    public void testParseLinesBreak() throws Exception {
	checkFile(TextOptions.SEGMENT_BREAKS, 3, 3);
    }

    protected void checkFile(int segValue, int count1, int count2) throws Exception {
	TextOptions options = new TextOptions();
	options.setSegmentOn(segValue);
	TextFilter filter = new TextFilter();
	filter.setOptions(options);

	List<String> entries = parse(filter, "test/data/filters/text/file-TextFilter.txt");
	assertEquals(count1, entries.size());

	entries = parse(filter, "test/data/filters/text/file-TextFilter-noemptylines.txt");
	assertEquals(count2, entries.size());
    }
}
