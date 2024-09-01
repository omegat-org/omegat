/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.glossary;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.util.Preferences;

public class FindGlossaryThreadTest extends TestCore {
    @Test
    public void testEntriesSort() {
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry("dog", "doggy", "cdog", false, null));
        entries.add(new GlossaryEntry("cat", "catty", "ccat", false, null));
        entries.add(new GlossaryEntry("cat", "mikeneko", "ccat", false, null));
        entries.add(new GlossaryEntry("zzz", "zzz", "czzz", true, null));
        entries.add(new GlossaryEntry("horse", "catty", "chorse", false, null));
        entries.add(new GlossaryEntry("向上", "enhance", "", false, null));
        entries.add(new GlossaryEntry("向", "direct", "", false, null));
        entries.add(new GlossaryEntry("上", "up", "", false, null));
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, true);
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, false);
        GlossarySearcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("mikeneko", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("catty", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, false);
        GlossarySearcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("catty", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("mikeneko", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
        assertEquals("up", entries.get(5).getLocText());
        assertEquals("direct", entries.get(6).getLocText());
        assertEquals("enhance", entries.get(7).getLocText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, true);
        GlossarySearcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("catty", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("mikeneko", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
        assertEquals("enhance", entries.get(5).getLocText());
        assertEquals("up", entries.get(6).getLocText());
        assertEquals("direct", entries.get(7).getLocText());
    }
}
