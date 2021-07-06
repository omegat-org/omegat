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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
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
        setupProject(language);
        List<GlossaryEntry> entries = Arrays.asList(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testIsCjkMatchJapanese() {
        String sourceText = "\u5834\u6240";
        String targetText = "\u5857\u5E03";
        Language language = new Language("ja");
        setupProject(language);
        assertTrue(GlossarySearcher.isCjkMatch(sourceText, sourceText));
        assertFalse(GlossarySearcher.isCjkMatch(sourceText, targetText));
    }

    @Test
    public void testGlossarySearcherJapanese1() {
        String sourceText = "\u5834\u6240";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneJapaneseTokenizer();
        Language language = new Language("ja");
        setupProject(language);
        List<GlossaryEntry> entries = Arrays.asList(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testGlossarySearcherJapanese2() {
        String sourceText = "\u5834\u6240";
        Language language = new Language("ja");
        setupProject(language);
        ITokenizer tok = new LuceneJapaneseTokenizer();
        List<GlossaryEntry> entries = Arrays.asList( new GlossaryEntry("\u5857\u5E03", "wrong", "", true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(0, result.size());
    }

    @Test
    public void testGlossarySearcherJapaneseLongText() {
        Language language = new Language("ja");
        setupProject(language);
        ITokenizer tok = new LuceneJapaneseTokenizer();
        List<GlossaryEntry> entries = Arrays.asList(
                new GlossaryEntry("\u307E\u3050\u308D", "tuna", "", true, ""),
                new GlossaryEntry("\u7FFB\u8A33", "translation", "", true, ""),
                new GlossaryEntry("\u591A\u8A00\u8A9E", "multi-languages", "", true, ""),
                new GlossaryEntry("\u5730\u57DF\u5316", "localization", "", true, "")
        );
        String sourceText = "OmegaT\u306E\u30E6\u30FC\u30B6\u30FC\u30A4\u30F3\u30BF\u30FC\u30D5\u30A7\u30FC\u30B9\u3084\u30D8\u30EB\u30D7\u30C6\u30AD\u30B9\u30C8\u3092\u3001\u3055\u307E\u3056\u307E\u306A\u8A00\u8A9E\u3078\u7FFB\u8A33\u3057\u3066\u304F\u3060\u3055\u3063\u305F\u65B9\u3005\u306B\u611F\u8B1D\u3057\u307E\u3059\u3002\u305D\u3057\u3066\u3001\u7FFB\u8A33\u304C\u306A\u3055\u308C\u3066\u3044\u306A\u3044\u8A00\u8A9E\u304C\u307E\u3060\u6570\u5343\u6B8B\u3063\u3066\u3044\u307E\u3059\uFF01OmegaT \u306E\u591A\u8A00\u8A9E\u3078\u306E\u5730\u57DF\u5316\u306F\u3001\u6301\u7D9A\u7684\u306A\u4F5C\u696D\u3067\u3082\u3042\u308A\u307E\u3059\u3002\u306A\u305C\u306A\u3089\u3001\u65B0\u3057\u3044\u6A5F\u80FD\u304C\u7D76\u3048\u305A\u8FFD\u52A0\u3055\u308C\u3066\u3044\u308B\u304B\u3089\u3067\u3059\u3002OmegaT\u306E\u30ED\u30FC\u30AB\u30E9\u30A4\u30BA/\u7FFB\u8A33\u306B\u95A2\u3059\u308B\u8A73\u7D30\u306B\u3064\u3044\u3066\u306F\u3001OmegaT\u30ED\u30FC\u30AB\u30EA\u30BC\u30FC\u30B7\u30E7\u30F3\u30B3\u30FC\u30C7\u30A3\u30CD\u30FC\u30BF\u30FC\u306B\u304A\u554F\u3044\u5408\u308F\u305B\u304F\u3060\u3055\u3044\u3002";
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(3, result.size());
    }

    private void setupProject(Language language) {
        Core.setProject(new NotLoadedProject() {
            @Override
            public boolean isProjectLoaded() {
                return true;
            }
            @Override
            public ProjectProperties getProjectProperties() {
                try {
                    return new ProjectProperties(new File("stub")) {
                        @Override
                        public Language getSourceLanguage() {
                            return language;
                        }
                        @Override
                        public Language getTargetLanguage() {
                            return new Language("pl");
                        }
                    };

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private List<GlossaryEntry> glossarySearcherCommon(String sourceText, ITokenizer tok, Language language,
                                                       List<GlossaryEntry> entries) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        GlossarySearcher searcher = new GlossarySearcher(tok, language, false);
        return searcher.searchSourceMatches(ste, entries);
    }
}
