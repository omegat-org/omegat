/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.TagUtil.TagType;

import junit.framework.TestCase;

/**
 * Tests for tag utility methods.
 * 
 * @author Alex Buloichik
 * @author Aaron Madlon-Kay
 */
public class TagUtilTest extends TestCase {
    
    /**
     * Test of buildTagList method, of class org.omegat.util.StaticUtils.
     */
    public void testBuildTagList() {
        String str = "Tag <test> case <b0>one</b0>.<b1>";
        List<ProtectedPart> pps = TagUtil.applyCustomProtectedParts(str, PatternConsts.OMEGAT_TAG, null);
        List<Tag> tagList = TagUtil.buildTagList(str, new SourceTextEntry(null, 0, null, null, pps).getProtectedParts());

        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList(new Tag(16, "<b0>"), new Tag(23, "</b0>"), new Tag(29, "<b1>")), tagList);

        tagList.clear();
        ProtectedPart p;
        List<ProtectedPart> pp = new ArrayList<ProtectedPart>();
        p = new ProtectedPart();
        p.setTextInSourceSegment("<b0>");
        pp.add(p);
        p = new ProtectedPart();
        p.setTextInSourceSegment("</b0>");
        pp.add(p);
        tagList = TagUtil.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts());
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList(new Tag(16, "<b0>"), new Tag(23, "</b0>")), tagList);

        str = "Tag <test>case</test>.";
        tagList.clear();
        pp.clear();
        p = new ProtectedPart();
        p.setTextInSourceSegment("<test>case</test>");
        pp.add(p);
        tagList = TagUtil.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts());
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList(new Tag(4, "<test>case</test>")), tagList);
    }
    
    public void testTagType() {
        // Only OmegaT tags (per PatternConsts.OMEGAT_TAG_DECOMPILE) can be
        // anything other than TagType.SINGLE.
        assertEquals(TagType.START, new Tag(-1, "<x0>").getType());
        assertEquals(TagType.START, new Tag(-1, "<x10>").getType());
        assertEquals(TagType.END, new Tag(-1, "</x0>").getType());
        assertEquals(TagType.SINGLE, new Tag(-1, "<x0/>").getType());
        // Generic XML-like tags are always TagType.SINGLE.
        assertEquals(TagType.SINGLE, new Tag(-1, "<x>").getType());
        assertEquals(TagType.SINGLE, new Tag(-1, "<x/>").getType());
        assertEquals(TagType.SINGLE, new Tag(-1, "</x>").getType());
        assertEquals(TagType.SINGLE, new Tag(-1, "</x0/>").getType());
        assertEquals(TagType.SINGLE, new Tag(-1, "foo").getType());
    }
    
    public void testTagName() {
        assertEquals("x0", new Tag(-1, "<x0>").getName());
        assertEquals("x10", new Tag(-1, "<x10>").getName());
        assertEquals("x0", new Tag(-1, "</x0>").getName());
        assertEquals("x0", new Tag(-1, "<x0/>").getName());
        assertEquals("<x>", new Tag(-1, "<x>").getName());
        assertEquals("<x/>", new Tag(-1, "<x/>").getName());
        assertEquals("</x>", new Tag(-1, "</x>").getName());
        assertEquals("</x0/>", new Tag(-1, "</x0/>").getName());
        assertEquals("foo", new Tag(-1, "foo").getName());
    }
    
    public void testPairedTag() {
        assertEquals("</x0>", new Tag(-1, "<x0>").getPairedTag());
        assertEquals("</x10>", new Tag(-1, "<x10>").getPairedTag());
        assertEquals("<x0>", new Tag(-1, "</x0>").getPairedTag());
        assertEquals(null, new Tag(-1, "<x0/>").getPairedTag());
        assertEquals(null, new Tag(-1, "<x>").getPairedTag());
        assertEquals(null, new Tag(-1, "<x/>").getPairedTag());
        assertEquals(null, new Tag(-1, "</x>").getPairedTag());
        assertEquals(null, new Tag(-1, "</x0/>").getPairedTag());
        assertEquals(null, new Tag(-1, "foo").getPairedTag());
    }
}
