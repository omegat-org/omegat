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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;

import org.junit.Test;

/**
 * Testing some of regular expressions.
 *
 * @author Maxym Mykhalchuk
 */
public class PatternConstsTest {

    /**
     * Tests {@link PatternConsts#LANG_AND_COUNTRY} regular expression.
     */
    @Test
    public void testLangAndCountry() {
        String lcBad = "abc*DEF";
        Matcher m = PatternConsts.LANG_AND_COUNTRY.matcher(lcBad);
        if (m.matches()) {
            fail("Language and Country pattern '" + PatternConsts.LANG_AND_COUNTRY.pattern()
                    + "' incorrectly matches a wrong string '" + lcBad + "'");
        }
        String lcGood = "abc-DEF";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(lcGood);
        assertTrue("Language and Country pattern '" + PatternConsts.LANG_AND_COUNTRY.pattern()
                + "' does not match a good string '" + lcGood + "'", m.matches());
        assertEquals("Wrong group count extracted (" + m.groupCount() + "), should be 2.", 2, m.groupCount());
        assertEquals("Wrong language extracted", "abc", m.group(1));
        assertEquals("Wrong country extracted", "DEF", m.group(2));

        String lBad = "abc";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(lBad);
        assertTrue("Language and Country pattern '" + PatternConsts.LANG_AND_COUNTRY.pattern()
                + "' does not match a good string '" + lBad + "'", m.matches());
        assertEquals("Wrong language extracted", "abc", m.group(1));
        assertNull("Country extracted, but it should not", m.group(2));

        String cGood = "Z-abc";
        m = PatternConsts.LANG_AND_COUNTRY.matcher(cGood);
        assertTrue("Language and Country pattern '" + PatternConsts.LANG_AND_COUNTRY.pattern()
                + "' does not match a good string '" + lcGood + "'", m.matches());
        assertEquals("Wrong language extracted", "Z", m.group(1));
        assertEquals("Wrong country extracted", "abc", m.group(2));
    }

}
