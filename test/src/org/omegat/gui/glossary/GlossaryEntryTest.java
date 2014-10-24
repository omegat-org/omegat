/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.TestCore;

/**
 * @author Martin Fleurke
 */
public class GlossaryEntryTest extends TestCore {
    public void testRead() throws Exception {
        GlossaryEntry a = new GlossaryEntry("", "", "", false);
        GlossaryEntry b = new GlossaryEntry("", "", "", false);
        assertEquals(a, b);

        GlossaryEntry c = new GlossaryEntry("src", "", "", false);
        GlossaryEntry d = new GlossaryEntry("src", "", "", false);
        assertEquals(c, d);

        List<GlossaryEntry> list = new ArrayList<GlossaryEntry>();
        list.add(a);
        assertEquals(1, list.size());
        list.remove(c);
        assertEquals(1, list.size());
        list.remove(b);
        assertEquals(0, list.size());
        list.add(c);
        assertEquals(1, list.size());
        list.remove(d);
        assertEquals(0, list.size());

        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
        list.remove(a);
        assertEquals(3, list.size());
        list.remove(a);
        assertEquals(2, list.size());
    }

    public void testToStyledString() {
        GlossaryEntry ge = new GlossaryEntry("source1", "translation1", "", false);
        assertEquals("source1 = translation1", ge.toStyledString().text.toString());
    }

    public void testToStyledStringMultipleTranslations() {
        GlossaryEntry ge = new GlossaryEntry("source1", new String[] {"translation1", "translation2"},
                                             new String[] { "", "" }, new boolean[] {false, false});
        assertEquals("source1 = translation1, translation2", ge.toStyledString().text.toString());
    }

    public void testToStyledStringWithComment() {
        GlossaryEntry ge = new GlossaryEntry("source1", "translation1", "comment1", false);
        assertEquals("source1 = translation1\n1. comment1", ge.toStyledString().text.toString());
    }

    public void testToStyledStringMultipleComments() {
        GlossaryEntry ge = new GlossaryEntry("source1", new String[] {"translation1", "translation2"},
                                             new String[] { "comment1", "comment2" }, new boolean[] {false, false});
        assertEquals("source1 = translation1, translation2\n1. comment1\n2. comment2",
                     ge.toStyledString().text.toString());
    }
}
