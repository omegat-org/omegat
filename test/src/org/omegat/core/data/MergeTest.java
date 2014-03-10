/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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
package org.omegat.core.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.junit.Test;
import org.omegat.core.data.TMXEntry.ExternalLinked;

/**
 * Tests for merge in team project.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MergeTest extends TestCase {
    @Test
    public void testTimeTruncate() throws Exception {
        // Time should be truncated(not rounded!) to 1 second
        SimpleDateFormat tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeText = tmxDateFormat.format(new Date(123456999));
        Date newdt = tmxDateFormat.parse(timeText);
        assertEquals(123456000, newdt.getTime());
    }

    @Test
    public void testEquals() throws Exception {
        PrepareTMXEntry e1 = new PrepareTMXEntry();
        e1.translation = "trans";
        e1.changeDate = 123456999;
        PrepareTMXEntry e2 = new PrepareTMXEntry();
        e2.translation = "trans";
        e2.changeDate = 123456999;

        // test equals
        assertTrue(new TMXEntry(e1, true, null).equals(new TMXEntry(e2, true, null)));

        e2.changeDate = 123456000;
        // test truncated time
        assertTrue(new TMXEntry(e1, true, null).equals(new TMXEntry(e2, true, null)));

        e2.changeDate = 123457000;
        // test other time
        assertFalse(new TMXEntry(e1, true, null).equals(new TMXEntry(e2, true, null)));
        e2.changeDate = 123456999;

        e2.translation = "t";
        // test different translation
        assertFalse(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));
        e2.translation = "trans";

        e2.note = "n";
        // test different note
        assertFalse(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));
        e2.note = null;

        e2.changer = "c";
        // test different changer
        assertTrue(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));
        e2.changer = null;

        // test different linked
        assertFalse(new TMXEntry(e1, true, ExternalLinked.xICE).equalsTranslation(new TMXEntry(e2, true,
                ExternalLinked.x100PC)));
        assertFalse(new TMXEntry(e1, true, ExternalLinked.xICE)
                .equalsTranslation(new TMXEntry(e2, true, null)));
    }
}
