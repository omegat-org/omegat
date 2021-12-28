/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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
package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.omegat.core.data.TMXEntry.ExternalLinked;

/**
 * Tests for merge in team project.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MergeTest {
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
        TMXEntry e1 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e2 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e3 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456000)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e4 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123457000)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e5 = new TMXEntry.Builder()
                .setTranslation("t")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e6 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .setNote("n")
                .build();
        TMXEntry e7 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChanger("c")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .build();
        TMXEntry e8 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .setExternalLinked(ExternalLinked.xICE)
                .build();
        TMXEntry e9 = new TMXEntry.Builder()
                .setTranslation("trans")
                .setChangeDate(123456999)
                .setDefaultTranslation(true)
                .setExternalLinked(ExternalLinked.x100PC)
                .build();

        // test equals
        assertEquals(e1, e2);

        // test truncated time
        assertEquals(e1, e3);

        // test other time
        assertNotEquals(e1, e4);

        // test different translation
        assertFalse(e1.equalsTranslation(e5));

        // test different note
        assertFalse(e1.equalsTranslation(e6));

        // test different changer
        assertTrue(e1.equalsTranslation(e7));

        // test different linked
        assertFalse(e8.equalsTranslation(e9));
        assertFalse(e8.equalsTranslation(e2));
    }
}
