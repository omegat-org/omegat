/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Language;
import org.omegat.util.Token;

/**
 * Reproduction of SF bug #981 ("5.2.0 painfully slow"): with large glossaries
 * (reporter: ~55 files, ~40 MB total, largest 12 MB, ja&rarr;en) every segment
 * switch stalls for seconds from OmegaT 5.x on, long segments (150+
 * characters) freeze longest; 4.3.2 was fine.
 *
 * Each test times or counts one {@link GlossarySearcher} or tokenization code
 * path with a glossary of the reported magnitude and asserts a time budget.
 * Before the SF #981 fixes (per-search token prefilter, one tokenization per
 * marker pass) one segment switch scanned 300k entries in 1.6 s; a failure
 * here means one of those regressions is back. The prints document the
 * measured magnitudes. Rendering of the glossary pane is covered end-to-end,
 * through the real GUI, by {@code GlossaryPaneSegmentSwitchTest} in
 * testAcceptance.
 *
 * The paths mirror production wiring: one long-lived source tokenizer whose
 * per-instance token cache stays warm across segment switches (as in
 * {@code GlossaryManager#buildSearcher}), while every switch presents a new
 * segment text.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class GlossarySearchLoadTest extends TestCore {

    /**
     * Glossary size in the reported magnitude: reporter had ~40 MB of
     * glossary files; at typical TSV line lengths that is roughly 300k
     * entries.
     */
    private static final int LARGE_GLOSSARY = 300_000;
    /** Entries displayed in the glossary pane while typing. */
    private static final int DISPLAYED_ENTRIES = 50;
    /** Budget for one segment switch, with headroom for loaded CI runners. */
    private static final long SWITCH_BUDGET_MS = 750;
    /** Budget for one TransTips marker pass (recalculated per keystroke). */
    private static final long MARKER_PASS_BUDGET_MS = 100;
    /** Accepted slowdown of the stemming-off configuration. */
    private static final int STEMMING_OFF_FACTOR = 4;

    /**
     * Real Japanese segments in the reported length class; cycled so that
     * every measured switch tokenizes a fresh segment while the glossary term
     * cache stays warm, as in production.
     */
    private static final String[] SEGMENTS = {
            "OmegaTのユーザーインターフェースやヘルプテキストを、さまざまな言語へ翻訳してくださった方々に感謝します。"
                    + "そして、翻訳がなされていない言語がまだ数千残っています！OmegaT の多言語への地域化は、持続的な作業でもあります。"
                    + "なぜなら、新しい機能が絶えず追加されているからです。",
            "翻訳メモリーと用語集を併用すると、文書全体の用語の統一と品質の管理が容易になります。"
                    + "大きな案件では数十の用語集ファイルを同時に参照することも珍しくなく、"
                    + "編集画面の応答速度は作業の効率に直接影響します。設定を変更した場合は、必ず検査を実行してください。",
            "このプロジェクトの原文ファイルは複数の形式で提供されており、変換と保存の手順は開発環境の構成によって異なります。"
                    + "出力の品質を確認するために、対象の文書を選択し、表示された項目を編集してから、"
                    + "状態と情報を資料として保存してください。",
            "辞書と用語集の検索は、入力のたびに実行されるため、処理の速度が遅いと編集そのものが困難になります。"
                    + "特に長い文では、機能の追加や更新のたびに再検索が発生し、画面の表示が数秒間固まることがあります。"
                    + "これがこの試験で再現しようとしている問題です。",
    };

    /**
     * Real two-kanji morphemes; compounds of two or three of them form the
     * synthetic glossary terms (Japanese compounding is productive, so the
     * terms stay plausible while scaling to any size).
     */
    private static final String[] JA_MORPHEMES = { "翻訳", "言語", "地域", "設定", "品質", "管理", "検査", "辞書",
            "用語", "資料", "出力", "入力", "変換", "保存", "形式", "構成", "環境", "開発", "試験", "手順", "画面", "文書",
            "項目", "対象", "機能", "作業", "追加", "削除", "更新", "検索", "表示", "編集", "選択", "状態", "情報", "処理" };
    private static final String[] EN_WORDS = { "translation", "language", "region", "setting", "quality",
            "management", "inspection", "dictionary", "term", "material", "output", "input", "conversion",
            "storage", "format", "configuration", "environment", "development", "test", "procedure", "screen",
            "document", "item", "target", "feature", "work", "addition", "deletion", "update", "search",
            "display", "editing", "selection", "state", "information", "processing" };

    private ITokenizer tokenizer;

    @Before
    public final void setUpGlossaryLoad() {
        Language srcLang = new Language("ja");
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
                            return srcLang;
                        }

                        @Override
                        public Language getTargetLanguage() {
                            return new Language("en");
                        }
                    };
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        tokenizer = new LuceneJapaneseTokenizer();
    }

    /**
     * One segment switch (FindGlossaryThread's searchSourceMatches) must stay
     * within its time budget with a glossary of the reported size.
     */
    @Test
    public void segmentSwitchWithLargeGlossaryStaysWithinBudget() {
        List<GlossaryEntry> entries = buildEntries(LARGE_GLOSSARY);
        GlossarySearcherTest.MockGlossarySearcher searcher = productionSearcher(tokenizer, true);

        for (String segment : SEGMENTS) {
            searcher.searchSourceMatches(ste(segment), entries); // warm-up
        }
        long best = Long.MAX_VALUE;
        for (int round = 0; round < 3; round++) {
            for (String segment : SEGMENTS) {
                long t0 = System.nanoTime();
                List<GlossaryEntry> result = searcher.searchSourceMatches(ste(segment), entries);
                best = Math.min(best, (System.nanoTime() - t0) / 1_000_000);
                assertFalse("segments contain glossary terms", result.isEmpty());
            }
        }
        System.out.println(String.format(Locale.ROOT,
                "SF-981 segment switch: %d entries, best of %d switches = %d ms",
                LARGE_GLOSSARY, 3 * SEGMENTS.length, best));
        assertTrue("segment switch with " + LARGE_GLOSSARY + " glossary entries took " + best
                + " ms, time budget is " + SWITCH_BUDGET_MS + " ms (SF bug #981)",
                best <= SWITCH_BUDGET_MS);
    }

    /**
     * TransTipsMarker matches every displayed entry against the segment; the
     * pass is recalculated on every keystroke, so it must fit a small time
     * budget even with a well-filled glossary pane.
     */
    @Test
    public void transTipsMarkerPassPerKeystrokeStaysWithinBudget() {
        List<GlossaryEntry> displayed = matchingEntries(DISPLAYED_ENTRIES);
        GlossarySearcherTest.MockGlossarySearcher searcher = productionSearcher(tokenizer, true);

        markerPass(searcher, ste(SEGMENTS[0]), displayed); // warm-up
        long best = Long.MAX_VALUE;
        // Each keystroke changes the segment text, so every marker pass sees
        // an uncached string, as in EditorController.onTextChanged.
        for (int keystroke = 0; keystroke < 5; keystroke++) {
            SourceTextEntry ste = ste(SEGMENTS[0] + "追加の入力".substring(0, keystroke + 1));
            long t0 = System.nanoTime();
            int hits = markerPass(searcher, ste, displayed);
            best = Math.min(best, (System.nanoTime() - t0) / 1_000_000);
            assertTrue("displayed entries must match", hits > 0);
        }
        System.out.println(String.format(Locale.ROOT,
                "SF-981 marker pass: %d displayed entries, best of 5 keystrokes = %d ms",
                DISPLAYED_ENTRIES, best));
        assertTrue("one TransTips marker pass (" + DISPLAYED_ENTRIES + " displayed entries) took " + best
                + " ms on data of SF bug #981; per-keystroke budget is " + MARKER_PASS_BUDGET_MS + " ms",
                best <= MARKER_PASS_BUDGET_MS);
    }

    /**
     * With glossary stemming off (plain user setting) the searcher falls back
     * to uncached tokenizeVerbatim, which rebuilds the Kuromoji analyzer per
     * glossary entry; that configuration must stay within a small factor of
     * the stemming path.
     */
    @Test
    public void stemmingOffStaysComparableToStemmingOn() {
        List<GlossaryEntry> entries = buildEntries(2_000);
        long onMs = timeSwitches(productionSearcher(tokenizer, true), entries);
        long offMs = timeSwitches(productionSearcher(tokenizer, false), entries);
        System.out.println(String.format(Locale.ROOT,
                "SF-981 stemming: %d entries, on=%d ms, off=%d ms, factor=%.1f", 2_000, onMs, offMs,
                onMs == 0 ? 0.0 : (double) offMs / onMs));
        assertTrue("stemming-off search took " + offMs + " ms vs " + onMs
                + " ms with stemming (SF bug #981); accepted factor is " + STEMMING_OFF_FACTOR,
                offMs <= Math.max(1_000, onMs * STEMMING_OFF_FACTOR));
    }

    /**
     * One segment activation should tokenize the segment text a bounded
     * number of times: once for searchSourceMatches and once for the batched
     * marker pass. Before the batch API every displayed entry tokenized the
     * segment again.
     */
    @Test
    public void segmentTokenizedBoundedOftenPerActivation() {
        CountingJapaneseTokenizer counting = new CountingJapaneseTokenizer(SEGMENTS[0]);
        List<GlossaryEntry> displayed = matchingEntries(10);
        GlossarySearcherTest.MockGlossarySearcher searcher = productionSearcher(counting, true);
        SourceTextEntry ste = ste(SEGMENTS[0]);

        searcher.searchSourceMatches(ste, displayed);
        markerPass(searcher, ste, displayed);
        System.out.println(String.format(Locale.ROOT,
                "SF-981 tokenizations of one segment per activation (search + marker pass over %d entries): %d",
                displayed.size(), counting.segmentTokenizations));
        assertTrue("segment text was tokenized " + counting.segmentTokenizations
                + " times for one activation; budget is 2 (SF bug #981)",
                counting.segmentTokenizations <= 2);
    }

    private static int markerPass(GlossarySearcher searcher, SourceTextEntry ste,
            List<GlossaryEntry> displayed) {
        int hits = 0;
        for (List<Token[]> tokens : searcher.searchSourceMatchTokens(ste, displayed)) {
            if (!tokens.isEmpty()) {
                hits++;
            }
        }
        return hits;
    }

    private long timeSwitches(GlossarySearcher searcher, List<GlossaryEntry> entries) {
        for (String segment : SEGMENTS) {
            searcher.searchSourceMatches(ste(segment), entries); // warm-up
        }
        long best = Long.MAX_VALUE;
        for (int round = 0; round < 2; round++) {
            long total = 0;
            for (String segment : SEGMENTS) {
                long t0 = System.nanoTime();
                searcher.searchSourceMatches(ste(segment), entries);
                total += System.nanoTime() - t0;
            }
            best = Math.min(best, total / 1_000_000);
        }
        return best;
    }

    /** Searcher with shipped defaults (stemming on, inexact matching on). */
    private static GlossarySearcherTest.MockGlossarySearcher productionSearcher(ITokenizer tok,
            boolean stemming) {
        GlossarySearcherTest.MockGlossarySearcher searcher = new GlossarySearcherTest.MockGlossarySearcher(
                tok, new Language("ja"), new Language("en"), false);
        searcher.enableGlossaryStemming(stemming);
        searcher.setGlossaryNotExactMatch(true);
        return searcher;
    }

    private static SourceTextEntry ste(String sourceText) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        return new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
    }

    /** Synthetic ja&rarr;en glossary of real-morpheme compounds. */
    private static List<GlossaryEntry> buildEntries(int count) {
        List<GlossaryEntry> entries = new ArrayList<>(count);
        int n = JA_MORPHEMES.length;
        for (int i = 0; entries.size() < count; i++) {
            String src;
            String loc;
            if (i < n * n) {
                src = JA_MORPHEMES[i / n] + JA_MORPHEMES[i % n];
                loc = EN_WORDS[i / n] + " " + EN_WORDS[i % n];
            } else if (i < n * n + n * n * n) {
                int j = i - n * n;
                src = JA_MORPHEMES[j / (n * n)] + JA_MORPHEMES[(j / n) % n] + JA_MORPHEMES[j % n];
                loc = EN_WORDS[j / (n * n)] + " " + EN_WORDS[(j / n) % n] + " " + EN_WORDS[j % n];
            } else {
                int j = i - n * n - n * n * n;
                src = JA_MORPHEMES[j / (n * n * n)] + JA_MORPHEMES[(j / (n * n)) % n]
                        + JA_MORPHEMES[(j / n) % n] + JA_MORPHEMES[j % n];
                loc = EN_WORDS[j / (n * n * n)] + " " + EN_WORDS[(j / (n * n)) % n] + " "
                        + EN_WORDS[(j / n) % n] + " " + EN_WORDS[j % n];
            }
            entries.add(new GlossaryEntry(src, loc, "", true, "load-test"));
        }
        return entries;
    }

    /**
     * Entries whose terms occur in SEGMENTS[0], several target variants per
     * term, as displayed by the glossary pane.
     */
    private static List<GlossaryEntry> matchingEntries(int count) {
        String[] present = { "翻訳", "言語", "地域化", "機能", "作業" };
        List<GlossaryEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new GlossaryEntry(present[i % present.length],
                    EN_WORDS[i % EN_WORDS.length] + " variant " + (i / present.length), "", true,
                    "load-test"));
        }
        return entries;
    }

    /** Counts how often the observed segment text reaches the tokenizer. */
    private static final class CountingJapaneseTokenizer extends LuceneJapaneseTokenizer {
        private final String segment;
        private int segmentTokenizations;

        CountingJapaneseTokenizer(String segment) {
            // GlossarySearcher.tokenize lowercases before tokenizing.
            this.segment = segment.toLowerCase(Locale.JAPANESE);
        }

        @Override
        public Token[] tokenizeWords(String strOrig, StemmingMode stemmingMode) {
            if (segment.equalsIgnoreCase(strOrig)) {
                segmentTokenizations++;
            }
            return super.tokenizeWords(strOrig, stemmingMode);
        }

        @Override
        public Token[] tokenizeVerbatim(String strOrig) {
            if (segment.equalsIgnoreCase(strOrig)) {
                segmentTokenizations++;
            }
            return super.tokenizeVerbatim(strOrig);
        }
    }
}
