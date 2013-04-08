/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.core.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.TestCore;
import org.omegat.util.Language;

/**
 * Tests for OmegaT segmentation.
 *
 * @author Maxym Mykhalchuk
 */
public class SegmenterTest extends TestCore
{
    /**
     * Test of segment method, of class org.omegat.core.segmentation.Segmenter.
     */
    public void testSegment()
    {
        List<StringBuffer> spaces = new ArrayList<StringBuffer>();
        List<String> segments = Segmenter.segment(new Language("en"),"<br7>\n\n<br5>\n\nother", spaces, new ArrayList<Rule>());
        if(segments.size()!=3 || !segments.get(0).toString().equals("<br7>") || 
                !segments.get(1).toString().equals("<br5>") ||
                !segments.get(2).toString().equals("other"))
            fail("Bug XXXXXX.");
    }
    
    /**
     * Test of glue method, of class org.omegat.core.segmentation.Segmenter.
     */
    public void testGlue()
    {
        List<StringBuffer> spaces = new ArrayList<StringBuffer>();
        List<Rule> brules = new ArrayList<Rule>();
        String oldString = "<br7>\n\n<br5>\n\nother";
        List<String> segments = Segmenter.segment(new Language("en"),oldString, spaces, brules);
        String newString = Segmenter.glue(new Language("en"),new Language("fr"),segments, spaces, brules);
        if(!newString.equals(oldString))
            fail("Glue failed.");
    }

}
