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
import org.madlonkay.supertmxmerge.StmProperties;
import org.madlonkay.supertmxmerge.SuperTmxMerge;
import org.omegat.core.Core;
import org.omegat.core.data.TMXEntry.ExternalLinked;
import org.omegat.util.OStrings;

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
        assertTrue(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));

        e2.changeDate = 123456000;
        // test truncated time
        assertTrue(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));

        e2.changeDate = 123457000;
        // test other time
        assertFalse(new TMXEntry(e1, true, null).equalsTranslation(new TMXEntry(e2, true, null)));
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

    @Test
    public void testAutoMerge() throws Exception {
        PrepareTMXEntry e1 = new PrepareTMXEntry();
        e1.translation = "t1";
        e1.changeDate = 10000;
        PrepareTMXEntry e2 = new PrepareTMXEntry();
        e2.translation = "t2";
        e2.changeDate = 20000;
        TMXEntry r;

        r = TMXEntry.autoMerge(new TMXEntry(e1, true, null), new TMXEntry(e2, true, null));
        assertEquals("t2", r.translation);
        assertNull(r.note);

        e1.note = "123";
        r = TMXEntry.autoMerge(new TMXEntry(e1, true, null), new TMXEntry(e2, true, null));
        assertEquals("t2", r.translation);
        assertEquals("123", r.note);
    }

    @Test
    public void testProjectTMXMerge() throws Exception {
        ProjectTMX baseTMX, projectTMX, headTMX;

        baseTMX = createEmptyTMX();
        projectTMX = createTMX(10000, "t1");
        headTMX = createEmptyTMX();
        checkTMXMerge(baseTMX, projectTMX, headTMX, "t1");

        baseTMX = createEmptyTMX();
        projectTMX = createEmptyTMX();
        headTMX = createTMX(10000, "t2");
        checkTMXMerge(baseTMX, projectTMX, headTMX, "t2");

        baseTMX = createEmptyTMX();
        projectTMX = createTMX(10000, "t11");
        headTMX = createTMX(20000, "t21");
        checkTMXMerge(baseTMX, projectTMX, headTMX, "t21");

        baseTMX = createEmptyTMX();
        projectTMX = createTMX(30000, "t12");
        headTMX = createTMX(20000, "t22");
        checkTMXMerge(baseTMX, projectTMX, headTMX, "t12");

        baseTMX = createTMX(30000, "t12");
        projectTMX = createEmptyTMX();
        headTMX = createTMX(20000, "t22");
        checkTMXMerge(baseTMX, projectTMX, headTMX, null);
    }

    ProjectTMX createEmptyTMX() {
        return new ProjectTMX();
    }

    ProjectTMX createTMX(long changeDate, String translation) {
        ProjectTMX tmx = new ProjectTMX();
        PrepareTMXEntry e = new PrepareTMXEntry();
        e.translation = translation;
        tmx.defaults.put("source", new TMXEntry(e, true, null));
        return tmx;
    }

    String getTMXTrans(ProjectTMX tmx) {
        TMXEntry e = tmx.defaults.get("source");
        return e == null ? null : e.translation;
    }

    void checkTMXMerge(ProjectTMX baseTMX, ProjectTMX projectTMX, ProjectTMX headTMX, String trans) {
        StmProperties props = new StmProperties().setBaseTmxName(OStrings.getString("TMX_MERGE_BASE"))
                .setTmx1Name(OStrings.getString("TMX_MERGE_MINE"))
                .setTmx2Name(OStrings.getString("TMX_MERGE_THEIRS"))
                .setLanguageResource(OStrings.getResourceBundle())
                .setParentWindow(Core.getMainWindow().getApplicationFrame());
        ProjectTMX mergedTMX = SuperTmxMerge.merge(baseTMX, projectTMX, headTMX, "en", "be", props);
        assertEquals(trans, getTMXTrans(mergedTMX));
    }
}
