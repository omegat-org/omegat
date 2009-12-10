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
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util;

import junit.framework.*;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
* Tests for (some) static utility methods.
*
* @author Martin Fleurke
*/
public class TMXDateParserTest extends TestCase
{
   
   public TMXDateParserTest(String testName)
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
       TestSuite suite = new TestSuite(TMXDateParserTest.class);
       return suite;
   }
   
   public void testParseDate() {
       //Test parse and toString with proper date string
       String dateString = "19971116T192059Z";//normal time
       Date d=null;
       try {
           d = TMXDateParser.parse(dateString);
       } catch (ParseException e) {
           fail("Valid date string could not be parsed: "+e.getMessage()+" [for "+dateString+"]");
       }
       String dateString2 = TMXDateParser.getTMXDate(d);
       if (!dateString.equals(dateString2)) fail("Parsing string to date and back does not give same string");

       //Test parse and toString with proper date string in daylight savings time
       dateString = "19970716T192059Z";
       try {
           d = TMXDateParser.parse(dateString);
       } catch (ParseException e) {
           fail("Valid date string could not be parsed: "+e.getMessage()+" [for "+dateString+"]");
       }
       dateString2 = TMXDateParser.getTMXDate(d);
       if (!dateString.equals(dateString2)) fail("Parsing string to date and back does not give same string (for daylight savings time)");
       
       //Test if same dates but different time zone give equal strings 
       GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("-02:00"));
       Date d2 = c.getTime(); //date in time zone -02:00 (hardly used anywhere, so most likely to be unique)
       Date dn = new Date(); //date with whatever time zone user is in (should be different from -02:00.
       dateString = TMXDateParser.getTMXDate(dn);
       dateString2 = TMXDateParser.getTMXDate(d2);
       if (!dateString.substring(0,13).equals(dateString2.substring(0,13))) { //taking substring, to prevent seconds to be different because of later creation of Date object
           fail("Two identical dates (in different timezones) do not give the same UTC String: "+dateString+" vs. "+dateString2);
       }

       try {
           TMXDateParser.parse("19971116T19205Zs"); //hmm, no error, interpreted as '19971116T192005Z' +'s'. should we add a check or not? I think not useful.
       } catch (ParseException e) {
           //exception is good, although we do not get one in this case
       }

       //Test if invalid date (wrong time zone) gives error
       try {
           TMXDateParser.parse("19971116T192059+00:00");
           fail("Invalid date string 19971116T192059+00:00 is parsed as valid");
       } catch (ParseException e) {}
       //Test if invalid date (too short) gives error
       try {
           TMXDateParser.parse("19971116T");
           fail("Invalid date string 19971116T is parsed as valid");
       } catch (ParseException e) {}
       //Test if invalid date (null) gives error
       try {
           TMXDateParser.parse(null);
           fail("Invalid date string null is parsed as valid");
       } catch (ParseException e) {}
       //Test if invalid date ("") gives error
       try {
           TMXDateParser.parse("");
           fail("Invalid date string '' is parsed as valid");
       } catch (ParseException e) {}
   }
}
