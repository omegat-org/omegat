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
        GlossaryEntry a = new GlossaryEntry(null, null, null);
        GlossaryEntry b = new GlossaryEntry(null, null, null);
        assertEquals(a, b);

        GlossaryEntry c = new GlossaryEntry("src", null, null);
        GlossaryEntry d = new GlossaryEntry("src", null, null);
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
}
