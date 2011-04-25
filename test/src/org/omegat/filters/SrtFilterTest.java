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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters;

import java.util.List;

import org.omegat.core.data.IProject;
import org.omegat.filters2.subtitles.SrtFilter;

public class SrtFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
        List<String> lines = parse(new SrtFilter(), "test/data/filters/srt/file-SrtFilter.srt");
        assertEquals(lines.size(), 3);
        assertEquals("First title", lines.get(0));
        assertEquals("Second title", lines.get(1));
        assertEquals("Third title\nand again", lines.get(2));
    }

    public void testTranslate() throws Exception {
        translateText(new SrtFilter(), "test/data/filters/srt/file-SrtFilter.srt");
    }

    public void testLoad() throws Exception {
        String f = "test/data/filters/srt/file-SrtFilter.srt";
        IProject.FileInfo fi = loadSourceFiles(new SrtFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("First title", "00:00:11,201 --> 00:01:00,005", null, null, null, null);
        checkMulti("Second title", "00:01:00,407 --> 00:01:02,300", null, null, null, null);
        checkMulti("Third title\nand again", "00:01:04,002 --> 00:01:05,200", null, null, null, null);
        checkMultiEnd();
    }
}
