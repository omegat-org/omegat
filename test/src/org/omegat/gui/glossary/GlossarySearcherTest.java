/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021-2025 Hiroshi Miura
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneCJKTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

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
        Language srcLang = new Language("en");
        Language trLang = new Language("de");
        setupProject(srcLang);
        List<GlossaryEntry> entries = List.of(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, srcLang, trLang, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testIsCjkMatchJapanese() {
        String sourceText = "場所";
        String targetText = "塗布";
        Language language = new Language("ja");
        setupProject(language);
        assertTrue(GlossarySearcher.isCjkMatch(sourceText, sourceText));
        assertFalse(GlossarySearcher.isCjkMatch(sourceText, targetText));
    }

    @Test
    public void testGlossarySearcherKorean() {
        String segmentText = "열 손가락 깨물어 안 아픈 손가락이 없다";
        String sourceText = "손가락";
        String translationText = "Korean term";
        String commentText = "comment";
        ITokenizer tok = new LuceneCJKTokenizer();
        Language language = new Language("ko");
        Language trLang = new Language("en");
        setupProject(language);
        List<GlossaryEntry> entries = Collections
                .singletonList(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(segmentText, tok, language, trLang, entries);
        assertEquals(1, result.size());
    }

    @Test
    public void testGlossarySearcherJapanese1() {
        String sourceText = "場所";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneJapaneseTokenizer();
        Language language = new Language("ja");
        Language trLang = new Language("en");
        setupProject(language);
        List<GlossaryEntry> entries = List.of(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, trLang, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testGlossarySearcherJapanese2() {
        String sourceText = "場所";
        Language language = new Language("ja");
        Language trLang = new Language("en");
        setupProject(language);
        ITokenizer tok = new LuceneJapaneseTokenizer();
        List<GlossaryEntry> entries = List.of(new GlossaryEntry("塗布", "wrong", "", true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, trLang, entries);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchSourceMatchesEmptyEntries() {
        String sourceText = "source text";
        Language srcLang = new Language("en");
        Language trgLang = new Language("es");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        List<GlossaryEntry> entries = Collections.emptyList();

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", sourceText, "id", null, null, null), 1,
                null, sourceText, Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchSourceMatchesWithTags() {
        String srcText = "source text";
        String translation = "translated text";
        ProtectedPart tag = new ProtectedPart();
        tag.setTextInSourceSegment("<b>");
        ProtectedPart closeTag = new ProtectedPart();
        closeTag.setTextInSourceSegment("</b>");

        Language srcLang = new Language("en");
        Language trgLang = new Language("fr");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", "<b>source</b> text", "id", null, null, null),
                1,
                null,
                "<b>source</b> text",
                Arrays.asList(tag, closeTag));

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(srcText, translation, "comment", true, null)
        );

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(srcText, result.get(0).getSrcText());
        assertEquals(translation, result.get(0).getLocText());
    }

    @Test
    public void testSearchSourceMatchesCaseInsensitive() {
        Language srcLang = new Language("en");
        Language trgLang = new Language("de");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        String srcText = "CaseInsensitive";
        String translation = "FallUnempfindlich";
        String segmentText = "caseinsensitive";

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(srcText, translation, "", false, null)
        );

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", segmentText, "id", null, null, null),
                1,
                null,
                segmentText,
                Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);

        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(srcText, result.get(0).getSrcText());
    }

    @Test
    public void testSearchSourceMatchesMerging() {
        Language srcLang = new Language("en");
        Language trgLang = new Language("es");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry1 = new GlossaryEntry("apple", "manzana", "", true, null);
        GlossaryEntry entry2 = new GlossaryEntry("apple", "apple fruit", "", true, null);

        List<GlossaryEntry> entries = Arrays.asList(entry1, entry2);
        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", "apple", "id", null, null, null),
                1,
                null,
                "apple",
                Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, true);

        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals("apple", result.get(0).getSrcText());
    }

    @Test
    public void testSearchSourceMatchesCJK() {
        Language srcLang = new Language("ja");
        Language trgLang = new Language("en");
        String srcText = "場所";

        String segmentText = "重要な場所です";
        String translation = "place";

        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(srcText, translation, "comment", false, null);

        List<GlossaryEntry> entries = Collections.singletonList(entry);

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", segmentText, "id", null, null, null),
                1,
                null,
                segmentText,
                Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);

        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(srcText, result.get(0).getSrcText());
        assertEquals(translation, result.get(0).getLocText());
    }

    @Test
    public void testGlossarySearcherJapaneseLongText() {
        Language language = new Language("ja");
        Language trLang = new Language("en");
        setupProject(language);
        ITokenizer tok = new LuceneJapaneseTokenizer();
        List<GlossaryEntry> entries = Arrays.asList(
                new GlossaryEntry("まぐろ", "tuna", "", true, ""),
                new GlossaryEntry("翻訳", "translation", "", true, ""),
                new GlossaryEntry("多言語", "multi-languages", "", true, ""),
                new GlossaryEntry("地域化", "localization", "", true, ""));
        String sourceText = "OmegaTのユーザーインターフェースやヘルプテキストを、さまざまな言語へ翻訳してくださった方々に感謝します。" +
                "そして、翻訳がなされていない言語がまだ数千残っています！OmegaT の多言語への地域化は、持続的な作業でもあります。" +
                "なぜなら、新しい機能が絶えず追加されているからです。OmegaTのローカライズ/翻訳に関する詳細については、" +
                "OmegaTローカリゼーションコーディネーターにお問い合わせください。";
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, trLang, entries);
        assertEquals(3, result.size());
    }

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

    private List<GlossaryEntry> glossarySearcherCommon(String sourceText, ITokenizer tok, Language srcLang,
            Language trLang, List<GlossaryEntry> entries) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trLang, false);
        return searcher.searchSourceMatches(ste, entries);
    }

    @Test
    public void testSearchSourceExactMatch() {
        String sourceText = "exact match";
        String translationText = "translation";
        String comment = "comment";
        Language srcLang = new Language("en");
        Language trLang = new Language("fr");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(sourceText, translationText, comment, true, null));

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", sourceText, "id", null, null, null), 1,
                null, sourceText, Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testSearchSourcePartialMatch() {
        String sourceText = "partial";
        String segmentText = "partial match example";
        String translation = "translation";
        String comment = "comment";
        Language srcLang = new Language("en");
        Language trLang = new Language("de");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        Preferences.setPreference(Preferences.GLOSSARY_NOT_EXACT_MATCH, true);

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(sourceText, translation, comment, true, null));

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", segmentText, "id", null, null, null), 1,
                null, segmentText, Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        Preferences.setPreference(Preferences.GLOSSARY_NOT_EXACT_MATCH, false);
    }

    @Test
    public void testSearchSourceCaseSensitiveMatch() {
        String sourceText = "CASE";
        String segmentText = "This is a case.";
        String translation = "translation";
        String comment = "comment";
        Language srcLang = new Language("en");
        Language trLang = new Language("es");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        Preferences.setPreference(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE, true);

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(sourceText, translation, comment, true, null));

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", segmentText, "id", null, null, null), 1,
                null, segmentText, Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertTrue(result.isEmpty());
        Preferences.setPreference(Preferences.GLOSSARY_REQUIRE_SIMILAR_CASE, false);
    }

    @Test
    public void testSearchSourceCJKMatch() {
        String sourceText = "場所";
        String segmentText = "場所は重要です";
        String translation = "Place";
        String comment = "comment";
        Language srcLang = new Language("ja");
        Language trLang = new Language("en");
        ITokenizer tok = new LuceneJapaneseTokenizer();
        setupProject(srcLang);

        List<GlossaryEntry> entries = Collections.singletonList(
                new GlossaryEntry(sourceText, translation, comment, true, null));

        SourceTextEntry ste = new SourceTextEntry(new EntryKey("file", segmentText, "id", null, null, null), 1,
                null, segmentText, Collections.emptyList());

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trLang, false);
        List<GlossaryEntry> result = searcher.searchSourceMatches(ste, entries);

        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
    }

    @Test
    public void testSearchTargetExactMatch() {
        String targetText = "translated text";
        String sourceText = "source";
        String comment = "comment";
        Language srcLang = new Language("en");
        Language trgLang = new Language("fr");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(sourceText, targetText, comment, true, null);
        ProtectedPart[] protectedParts = {};

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<String> result = searcher.searchTargetMatches(targetText, protectedParts, entry);

        assertEquals(1, result.size());
        assertTrue(result.contains(targetText));
    }

    @Test
    public void testSearchTargetCaseInsensitiveMatch() {
        String targetText = "Translated";
        String normalizedTarget = "translated";
        String sourceText = "source";
        Language srcLang = new Language("en");
        Language trgLang = new Language("de");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(sourceText, targetText, "", false, null);
        ProtectedPart[] protectedParts = {};

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<String> result = searcher.searchTargetMatches(normalizedTarget, protectedParts, entry);

        assertEquals(1, result.size());
        assertTrue(result.contains(targetText));
    }

    @Test
    public void testSearchTargetPartialMatch() {
        String targetText = "translation example";
        String partialText = "translation";
        String sourceText = "source";
        Language srcLang = new Language("en");
        Language trgLang = new Language("es");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(sourceText, targetText, "", true, null);
        ProtectedPart[] protectedParts = {};

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<String> result = searcher.searchTargetMatches(partialText, protectedParts, entry);

        assertEquals(0, result.size());
    }

    @Test
    public void testSearchTargetWithTags() {
        String targetText = "<b>translation</b> text";
        String sourceText = "source";
        String translateText = "translation";
        Language srcLang = new Language("en");
        Language trgLang = new Language("fr");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(sourceText, translateText, "", true, null);

        ProtectedPart protectedPart0 = new ProtectedPart();
        protectedPart0.setTextInSourceSegment("<br>");
        ProtectedPart protectedPart1 = new ProtectedPart();
        protectedPart1.setTextInSourceSegment("</b>");
        ProtectedPart[] protectedParts = { protectedPart0, protectedPart1 };

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<String> result = searcher.searchTargetMatches(targetText, protectedParts, entry);

        assertEquals(1, result.size());
        assertTrue(result.contains(translateText));
    }

    @Test
    public void testSearchTargetCJKMatch() {
        String targetText = "場所";
        String fullTargetText = "この場所は重要です";
        String sourceText = "source";
        Language srcLang = new Language("ja");
        Language trgLang = new Language("en");
        ITokenizer tok = new DefaultTokenizer();
        setupProject(srcLang);

        GlossaryEntry entry = new GlossaryEntry(sourceText, targetText, "", true, null);
        ProtectedPart[] protectedParts = {};

        GlossarySearcher searcher = new GlossarySearcher(tok, srcLang, trgLang, false);
        List<String> result = searcher.searchTargetMatches(fullTargetText, protectedParts, entry);

        assertEquals(1, result.size());
        assertTrue(result.contains(targetText));
    }
}
