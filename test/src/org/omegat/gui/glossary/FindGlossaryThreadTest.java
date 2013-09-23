/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

public class FindGlossaryThreadTest extends TestCore {
    public void testEntriesSort() {
        List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("dog", "doggy", "cdog", false));
        entries.add(new GlossaryEntry("cat", "catty", "ccat", false));
        entries.add(new GlossaryEntry("zzz", "zzz", "czzz", true));
        entries.add(new GlossaryEntry("horse", "catty", "chorse", false));
        FindGlossaryThread.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("horse", entries.get(1).getSrcText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("dog", entries.get(3).getSrcText());
    }
}
