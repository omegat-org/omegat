/**************************************************************************
OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
              Home page: http://www.omegat.org/
              Support center: https://omegat.org/support

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Tests for (some) static utility methods.
 *
 * @author Martin Fleurke
 */
public class TMXDateParserTest {

    @Test
    public void testParseDate() {
        TMXDateParser parser = new TMXDateParser();
        // Test parse and toString with proper date string
        String dateString = "19971116T192059Z"; // normal time
        Date d = null;
        try {
            d = parser.parse(dateString);
        } catch (ParseException e) {
            fail("Valid date string could not be parsed: " + e.getMessage() + " [for " + dateString + "]");
        }
        String dateString2 = parser.getTMXDate(d);
        assertEquals("Parsing string to date and back does not give same string", dateString, dateString2);
        // Test parse and toString with proper date string in daylight savings time
        dateString = "19970716T192059Z";
        try {
            d = parser.parse(dateString);
        } catch (ParseException e) {
            fail("Valid date string could not be parsed: " + e.getMessage() + " [for " + dateString + "]");
        }
        dateString2 = parser.getTMXDate(d);
        assertEquals("Parsing string to date and back does not give same string (for daylight savings time)",
                dateString, dateString2);

        // Test if same dates but different time zone give equal strings
        GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("-02:00"));
        Date d2 = c.getTime(); // date in time zone -02:00 (hardly used anywhere, so most likely to be unique)
        Date dn = new Date(); // date with whatever time zone user is in (should be different from -02:00.
        dateString = parser.getTMXDate(dn);
        dateString2 = parser.getTMXDate(d2);
        // taking substring, to prevent seconds to be different because of later creation of Date object
        assertEquals("Two identical dates (in different timezones) do not give the same UTC String: "
                + dateString + " vs. " + dateString2, dateString.substring(0, 13),
                dateString2.substring(0, 13));

        try {
            parser.parse("19971116T19205Zs");
            // hmm, no error, interpreted as '19971116T192005Z' +'s'. should we add a check or not? I think
            // not useful.
        } catch (ParseException e) {
            // exception is good, although we do not get one in this case
        }

        // Test if invalid date (wrong time zone) gives error
        try {
            parser.parse("19971116T192059+00:00");
            fail("Invalid date string 19971116T192059+00:00 is parsed as valid");
        } catch (ParseException e) {
        }
        // Test if invalid date (too short) gives error
        try {
            parser.parse("19971116T");
            fail("Invalid date string 19971116T is parsed as valid");
        } catch (ParseException e) {
        }
        // Test if invalid date (null) gives error
        try {
            parser.parse(null);
            fail("Invalid date string null is parsed as valid");
        } catch (ParseException e) {
        }
        // Test if invalid date ("") gives error
        try {
            parser.parse("");
            fail("Invalid date string '' is parsed as valid");
        } catch (ParseException e) {
        }
    }
}
