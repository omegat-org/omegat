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

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

    public void testCompressSpace()
    {
        if ( !"One Two Three Four Five".equals(StaticUtils.compressSpaces(" One Two\nThree   Four\r\nFive ")) ) fail("Space wrongly compressed");
        if ( !"Six seven".equals(StaticUtils.compressSpaces("Six\tseven")) ) fail("Space wrongly compressed");
    }

    public void testCompileFileMask() {
        Pattern r = StaticUtils.compileFileMask("Ab1-&*/**");
        assertEquals("Ab1\\-\\&[^/]*/.*", r.pattern());
    }
}
