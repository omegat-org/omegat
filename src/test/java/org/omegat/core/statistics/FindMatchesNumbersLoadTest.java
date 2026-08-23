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

package org.omegat.core.statistics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Load characterization for feature request #465: how does number-aware
 * matching compare to the plain matching on a large translation memory?
 *
 * The timings are printed for inspection; the only hard assertion is a
 * generous upper bound on the slow-down factor, so pathological regressions
 * fail while normal machine jitter does not.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class FindMatchesNumbersLoadTest {

    /** Translation memory size; large enough to expose per-entry costs. */
    private static final int TM_SIZE = 10_000;
    /** Bound: number-aware matching may not be 3x slower. */
    private static final int MAX_SLOWDOWN_FACTOR = 3;

    private static Path tmpDir;
    private static File tmxFile;

    private static final List<String> QUERIES = Arrays.asList(
            "The invoice 4711 was paid on 2007-11-25.",
            "Order 815 contains 7 items and costs 1234 euros.",
            "2008-02-29",
            "99999",
            "Chapter XII",
            "Please review chapter twelve before the meeting.",
            "The delivery 300041 arrived on 2006-05-17.",
            "17",
            "Order 2 contains 9999 items and costs 5 euros.",
            "1999-12-31");

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        tmxFile = new File(tmpDir.toFile(), "load-en-cs.tmx");
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">\n");
        sb.append("<tmx version=\"1.4\">\n");
        sb.append("<header datatype=\"plaintext\" srclang=\"en\" adminlang=\"EN-US\""
                + " o-tmf=\"OmegaT TMX\" segtype=\"sentence\" creationtoolversion=\"test\""
                + " creationtool=\"test\"/>\n<body>\n");
        for (int i = 0; i < TM_SIZE; i++) {
            String source;
            switch (i % 5) {
            case 0:
                source = String.format(Locale.ROOT, "The invoice %d was paid on 2007-%02d-%02d.", i,
                        1 + i % 12, 1 + i % 28);
                break;
            case 1:
                source = Integer.toString(10000 + i);
                break;
            case 2:
                source = String.format(Locale.ROOT, "20%02d-%02d-%02d", i % 30, 1 + i % 12, 1 + i % 28);
                break;
            case 3:
                source = "Please review chapter " + wordFor(i) + " before the deadline.";
                break;
            default:
                source = String.format(Locale.ROOT, "Order %d contains %d items and costs %d euros.", i,
                        i % 100, i % 10000);
                break;
            }
            sb.append("<tu><tuv xml:lang=\"en\"><seg>").append(source)
                    .append("</seg></tuv><tuv xml:lang=\"cs\"><seg>Cíl ").append(i)
                    .append("</seg></tuv></tu>\n");
        }
        sb.append("</body>\n</tmx>\n");
        Files.write(tmxFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String wordFor(int i) {
        String[] words = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve" };
        return words[i % words.length];
    }

    @Before
    public void setUp() throws Exception {
        Core.initializeConsole();
        Core.registerTokenizerClass(DefaultTokenizer.class);
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
        TestPreferencesInitializer.init();
    }

    private IProject project(boolean matchNumbers) {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cs");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(true);
        prop.setMatchNumbersEnabled(matchNumbers);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        return new FindMatchesTest.TestProject(prop, tmxFile, null, new LuceneEnglishTokenizer(),
                new DefaultTokenizer(), segmenter);
    }

    /**
     * One finder per mode, as in production, where the tokenization caches
     * live for one search; every query builds a fresh finder.
     */
    private long timedRun(boolean matchNumbers, int rounds) {
        IProject project = project(matchNumbers);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IStopped stop = () -> false;
        long total = 0;
        for (int round = 0; round < rounds; round++) {
            for (String query : QUERIES) {
                FindMatches finder = new FindMatches(project, segmenter, 5, false, 30);
                long t0 = System.nanoTime();
                List<NearString> result = finder.search(query, false, stop, false);
                total += System.nanoTime() - t0;
                if (matchNumbers && ("99999".equals(query) || "2008-02-29".equals(query))) {
                    assertFalse("number-only query must match with the option on", result.isEmpty());
                }
            }
        }
        return total / 1_000_000;
    }

    @Test
    public void loadComparisonOldVersusNumberAwareMatching() {
        // warm-up both paths (JIT, ICU parser initialization)
        timedRun(false, 1);
        timedRun(true, 1);

        long plainMs = timedRun(false, 3);
        long numberAwareMs = timedRun(true, 3);

        System.out.println(String.format(Locale.ROOT,
                "FindMatches load (%d TM entries, %d queries x 3): plain=%d ms, numbers=%d ms, factor=%.2f",
                TM_SIZE, QUERIES.size(), plainMs, numberAwareMs,
                plainMs == 0 ? 0.0 : (double) numberAwareMs / plainMs));

        assertTrue("number-aware matching must stay within factor " + MAX_SLOWDOWN_FACTOR
                + " of plain matching (plain=" + plainMs + " ms, numbers=" + numberAwareMs + " ms)",
                numberAwareMs <= Math.max(200, plainMs * MAX_SLOWDOWN_FACTOR));
    }
}
