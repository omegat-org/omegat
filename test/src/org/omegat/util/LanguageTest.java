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

import junit.framework.*;
import java.util.Locale;

/**
 * Tests for OmegaT language handling.
 *
 * @author Maxym Mykhalchuk
 */
public class LanguageTest extends TestCase
{
    
    public LanguageTest(String testName)
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
        TestSuite suite = new TestSuite(LanguageTest.class);
        
        return suite;
    }

    /**
     * Test of getLanguage method, of class org.omegat.util.Language.
     */
    public void testGetLanguage()
    {
        String LANG_1 = "xx-YY";
        Language lang = new Language(LANG_1);
        if( !lang.getLanguage().equals(LANG_1) )
            fail("http://sourceforge.net/support/tracker.php?aid=1473713\n" +
                    "Language/Country code case is changed: '"+lang.getLanguage()+"', should be '"+LANG_1+"'.");
        
        String LANG_2 = "XX-yy";
        lang = new Language(LANG_2);
        if( !lang.getLanguage().equals(LANG_2) )
            fail("http://sourceforge.net/support/tracker.php?aid=1473713\n" +
                    "Language/Country code case is changed: '"+lang.getLanguage()+"', should be '"+LANG_2+"'.");
    }

    /**
     * Test of getLocale method, of class org.omegat.util.Language.
     */
    public void testGetLocale()
    {
        String LANG = "XXX-yy";
        Language lang = new Language(LANG);
        if( !lang.getLocaleCode().equals("xxx_YY") )
            fail("Locale is wrong '"+lang.getLocale()+"', should be 'xxx_YY'");
    }

    /**
     * Test of equals method, of class org.omegat.util.Language.
     */
    public void testEquals()
    {
        String LANG_1 = "xxx-YY";
        Language lang1 = new Language(LANG_1);
        String LANG_2 = "XXX-yy";
        Language lang2 = new Language(LANG_2);
        String LANG_3 = "xxx_YY";
        Language lang3 = new Language(LANG_3);
        if( !lang1.equals(lang2) )
            fail("'"+LANG_1+"' is reported to be different from '"+LANG_2+"'");
        if( !lang1.equals(lang3) )
            fail("'"+LANG_1+"' is reported to be different from '"+LANG_3+"'");
        if( !lang2.equals(lang3) )
            fail("'"+LANG_2+"' is reported to be different from '"+LANG_3+"'");
    }
    
    /**
     * Test the constructor under lots of stress.
     */
    public void testConstructor()
    {
        try
        {
            Language lang1 = new Language((Locale)null);
            Language lang2 = new Language((String)null);
            if( !lang1.equals(lang2) )
                fail("Empty not equal");
        }
        catch( Exception e )
        {
            fail(e.getMessage());
        }
    }
}
