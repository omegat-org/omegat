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

import java.util.regex.Matcher;
import junit.framework.*;


/**
 * Testing some of regular expressions.
 *
 * @author Maxym Mykhalchuk
 */
public class PatternConstsTest extends TestCase
{
    
    public PatternConstsTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
    }

    protected void tearDown() throws Exception
    {
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(PatternConstsTest.class);
        
        return suite;
    }
    
    /**
     * Tests {@link PatternConsts#LANG_AND_COUNTRY} regular expression.
     */
    public void testLangAndCountry()
    {
        String LC_BAD = "abc*DEF";
        Matcher m = PatternConsts.LANG_AND_COUNTRY.matcher(LC_BAD);
        if( m.matches() )
            fail("Language and Country pattern '" +
                    PatternConsts.LANG_AND_COUNTRY.pattern() +
                    "' incorrectly matches a wrong string '" +
                    LC_BAD+
                    "'");
        String LC_GOOD = "abc-DEF";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(LC_GOOD);
        if( !m.matches() )
            fail("Language and Country pattern '" +
                    PatternConsts.LANG_AND_COUNTRY.pattern() +
                    "' does not match a good string '" +
                    LC_GOOD +
                    "'");
        if( m.groupCount()!=2 )
            fail("Wrong group count extracted ("+m.groupCount()+"), should be 2.");
        if( !m.group(1).equals("abc") )
            fail("Wrong language extracted");
        if( !m.group(2).equals("DEF") )
            fail("Wrong country extracted");
        
        String L_GOOD = "abc";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(L_GOOD);
        if( !m.matches() )
            fail("Language and Country pattern '" +
                    PatternConsts.LANG_AND_COUNTRY.pattern() +
                    "' does not match a good string '" +
                    L_GOOD +
                    "'");
        if( !m.group(1).equals("abc") )
            fail("Wrong language extracted");
        if( m.group(2)!=null )
            fail("Country extracted, but it should not");
        
        String C_GOOD = "Z-abc";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(C_GOOD);
        if( !m.matches() )
            fail("Language and Country pattern '" +
                    PatternConsts.LANG_AND_COUNTRY.pattern() +
                    "' does not match a good string '" +
                    C_GOOD +
                    "'");
        if( !m.group(1).equals("Z") )
            fail("Wrong language extracted");
        if( !m.group(2).equals("abc") )
            fail("Wrong country extracted");
    }
    
}
