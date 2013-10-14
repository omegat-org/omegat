/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;

/**
 * Tests for (some) static utility methods.
 *
 * @author Maxym Mykhalchuk
 */
public class StaticUtilsTest extends TestCase
{
    
    public StaticUtilsTest(String testName)
    {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception
    {
    }

    @Override
    protected void tearDown() throws Exception
    {
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(StaticUtilsTest.class);
        
        return suite;
    }

    /**
     * Test of buildTagList method, of class org.omegat.util.StaticUtils.
     */
    public void testBuildTagList() {
        // TODO add your test code below by replacing the default call to fail.
        String str = "Tag <test> case <b0>one</b0>.<b1>";
        List<ProtectedPart> pps = StaticUtils.applyCustomProtectedParts(str, PatternConsts.OMEGAT_TAG, null);
        ArrayList<String> tagList = new ArrayList<String>();
        StaticUtils.buildTagList(str, new SourceTextEntry(null, 0, null, null, pps).getProtectedParts(),
                tagList);

        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<b0>", "</b0>", "<b1>"), tagList);

        tagList.clear();
        ProtectedPart p;
        List<ProtectedPart> pp = new ArrayList<ProtectedPart>();
        p = new ProtectedPart();
        p.setTextInSourceSegment("<b0>");
        pp.add(p);
        p = new ProtectedPart();
        p.setTextInSourceSegment("</b0>");
        pp.add(p);
        StaticUtils.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts(),
                tagList);
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<b0>", "</b0>"), tagList);

        str = "Tag <test>case</test>.";
        tagList.clear();
        pp.clear();
        p = new ProtectedPart();
        p.setTextInSourceSegment("<test>case</test>");
        pp.add(p);
        StaticUtils.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts(), tagList);
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<test>case</test>"), tagList);
    }

    public void testCompressSpace()
    {
        if ( !"One Two Three Four Five".equals(StaticUtils.compressSpaces(" One Two\nThree   Four\r\nFive ")) ) fail("Space wrongly compressed");
        if ( !"Six seven".equals(StaticUtils.compressSpaces("Six\tseven")) ) fail("Space wrongly compressed");
    }
}
