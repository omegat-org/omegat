/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.util;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.Shortcuts;

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
        String str = "Tag <test> case <b0>one</b0>.<b0>";
        ArrayList<String> tagList = new ArrayList<String>();
        StaticUtils.buildTagList(str, null, tagList);

        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<b0>", "</b0>", "<b0>"), tagList);

        tagList.clear();
        Shortcuts pp = new Shortcuts();
        pp.put("<b0>", "");
        pp.put("</b0>", "");
        StaticUtils.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts(), tagList);
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<b0>", "</b0>", "<b0>"), tagList);

        str = "Tag <test>case</test>.";
        tagList.clear();
        pp.clear();
        pp.put("<test>case</test>", "");
        StaticUtils.buildTagList(str, new SourceTextEntry(null, 0, null, null, pp).getProtectedParts(), tagList);
        assertEquals("Wrong tags found in '" + str + "'", Arrays.asList("<test>case</test>"), tagList);
    }

    public void testCompressSpace()
    {
        if ( !"One Two Three Four Five".equals(StaticUtils.compressSpaces(" One Two\nThree   Four\r\nFive ")) ) fail("Space wrongly compressed");
        if ( !"Six seven".equals(StaticUtils.compressSpaces("Six\tseven")) ) fail("Space wrongly compressed");
    }

    public void testStripTags() {
        Shortcuts protectedParts = new Shortcuts();
        protectedParts.put("#part#", "zz");
        SourceTextEntry ste = new SourceTextEntry(null, 0, null, null, protectedParts);
        assertEquals("1\b2", StaticUtils.stripProtectedParts("1#part#2", ste));
        protectedParts.clear();
        assertEquals("1#part#2", StaticUtils.stripProtectedParts("1#part#2", ste));
    }
}
