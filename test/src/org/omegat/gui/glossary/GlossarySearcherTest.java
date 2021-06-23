/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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
package org.omegat.gui.glossary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Language;

/**
 * @author Hiroshi Miura
 */
public class GlossarySearcherTest extends TestCore {
    @Test
    public void testGlossarySearcherEnglish() {
        String sourceText = "source";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneEnglishTokenizer();
        Language language = new Language("en");
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        glossarySearcherCommon(sourceText, translationText, commentText, tok, language, entries);
    }

    @Test
    public void testIsCjkMatchJapanese() {
        String sourceText = "場所";
        assertTrue(GlossarySearcher.isCjkMatch(sourceText, sourceText));
    }

    @Test
    public void testGlossarySearcherJapanese1() {
        String sourceText = "場所";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneJapaneseTokenizer();
        Language language = new Language("ja");
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, translationText, commentText, tok, language, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testGlossarySearcherJapanese2() {
        String sourceText = "場所";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneJapaneseTokenizer();
        Language language = new Language("ja");
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry("塗布", "wrong", commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, translationText, commentText, tok, language, entries);
        assertEquals(0, result.size());
    }

    private List<GlossaryEntry> glossarySearcherCommon(String sourceText, String translationText, String commentText, ITokenizer tok,
                                        Language language, List<GlossaryEntry> entries) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        String[] props = new String[]{};
        SourceTextEntry ste = new SourceTextEntry(key, 1, props, sourceText, new ArrayList<>());
        GlossarySearcher searcher = new GlossarySearcher(tok, language, false);
        return searcher.searchSourceMatches(ste, entries);
    }

}
