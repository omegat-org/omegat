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
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class FindGlossaryThreadTest extends TestCore {

    @Test
    public void testEntriesSortEn() {
        Language srcLang = new Language("en_US");
        Language targetLang = new Language("en_GB");
        ITokenizer tok = new DefaultTokenizer();
        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, targetLang, false);
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry("dog", "doggy", "cdog", false, null));
        entries.add(new GlossaryEntry("cat", "catty", "ccat", false, null));
        entries.add(new GlossaryEntry("cat", "mikeneko", "ccat", false, null));
        entries.add(new GlossaryEntry("zzz", "zzz", "czzz", true, null));
        entries.add(new GlossaryEntry("horse", "catty", "chorse", false, null));
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, true);
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, false);
        searcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("mikeneko", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("catty", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, false);
        searcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("catty", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("mikeneko", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, true);
        searcher.sortGlossaryEntries(entries);
        assertEquals("zzz", entries.get(0).getSrcText());
        assertEquals("cat", entries.get(1).getSrcText());
        assertEquals("catty", entries.get(1).getLocText());
        assertEquals("cat", entries.get(2).getSrcText());
        assertEquals("mikeneko", entries.get(2).getLocText());
        assertEquals("dog", entries.get(3).getSrcText());
        assertEquals("horse", entries.get(4).getSrcText());
    }

    @Test
    public void testEntriesSortJA() {
        Language lang = new Language("ja_JP");
        Language targetLang = new Language("en_GB");
        ITokenizer tok = new DefaultTokenizer();
        GlossarySearcher searcher = new GlossarySearcher(tok, lang, targetLang, false);
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry("向上", "enhance", "", false, null));
        entries.add(new GlossaryEntry("向", "direct", "", false, null));
        entries.add(new GlossaryEntry("上", "on", "", false, null));
        entries.add(new GlossaryEntry("上", "up to", "", false, null));
        entries.add(new GlossaryEntry("トヨタ自動車", "toyota motors", "", false, null));
        entries.add(new GlossaryEntry("トヨタ", "toyota", "", false, null));
        entries.add(new GlossaryEntry("さくら", "cherry blossom", "", false, null));
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, true);
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, false);
        searcher.sortGlossaryEntries(entries);
        assertEquals("さくら", entries.get(0).getSrcText());
        assertEquals("トヨタ", entries.get(1).getSrcText());
        assertEquals("トヨタ自動車", entries.get(2).getSrcText());
        assertEquals("向", entries.get(3).getSrcText());
        assertEquals("向上", entries.get(4).getSrcText());
        assertEquals("up to", entries.get(5).getLocText());
        assertEquals("on", entries.get(6).getLocText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, false);
        searcher.sortGlossaryEntries(entries);
        assertEquals("cherry blossom", entries.get(0).getLocText());
        assertEquals("toyota", entries.get(1).getLocText());
        assertEquals("toyota motors", entries.get(2).getLocText());
        assertEquals("direct", entries.get(3).getLocText());
        assertEquals("enhance", entries.get(4).getLocText());
        assertEquals("on", entries.get(5).getLocText());
        assertEquals("up to", entries.get(6).getLocText());
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, true);
        searcher.sortGlossaryEntries(entries);
        assertEquals("toyota motors", entries.get(1).getLocText());
        assertEquals("toyota", entries.get(2).getLocText());
        assertEquals("enhance", entries.get(3).getLocText());
        assertEquals("direct", entries.get(4).getLocText());
        assertEquals("on", entries.get(5).getLocText());
        assertEquals("up to", entries.get(6).getLocText());
    }
}
