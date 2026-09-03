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
package org.omegat.gui.editor.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Numeric sorting of a project-sized list must complete in interactive time.
 * The first live use froze the UI for minutes: the comparator re-parsed both
 * strings through the ICU numeral parsers on every one of the O(n log n)
 * comparisons. This pins the fixed behavior (one parse per distinct string,
 * cheap pre-filtering of prose) at a scale where the old code was hopeless.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumericValueComparatorPerformanceTest {

    private static final int ENTRIES = 20_000;

    @Test
    public void sortingProjectSizedListStaysInteractive() {
        org.junit.Assume.assumeTrue(
                "Skipping performance test: wall-clock timings are unreliable on CI runners",
                System.getenv("CI") == null && System.getenv("TF_BUILD") == null);
        List<String> targets = new ArrayList<>(ENTRIES);
        for (int i = 0; i < ENTRIES; i++) {
            if (i % 3 == 0) {
                // Prose without any number: the expensive worst case.
                targets.add("Ein Beispielsatz ohne Nummer, Variante " + wordOf(i) + " im laufenden Text.");
            } else {
                targets.add("Kapitel " + (i % 4000) + " beginnt mit einem laengeren Satz Nummer " + i + ".");
            }
        }

        Comparator<String> comparator = new NumericValueComparator(
                Collator.getInstance(Locale.GERMAN)).reversed();
        long t0 = System.nanoTime();
        targets.sort(comparator);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        System.out.println(String.format(Locale.ROOT,
                "Numeric sort of %d strings: %d ms", ENTRIES, ms));
        assertTrue("numeric sort of " + ENTRIES + " strings must stay interactive, took " + ms + " ms",
                ms < 5_000);
        // Reversed numeric order: prose (no number) first, then chapters descending.
        assertTrue(targets.get(0).startsWith("Ein Beispielsatz"));
        assertTrue("last entry must be the smallest chapter, got: " + targets.get(ENTRIES - 1),
                targets.get(ENTRIES - 1).startsWith("Kapitel 0 "));
        int firstNumbered = 0;
        while (firstNumbered < ENTRIES && !targets.get(firstNumbered).startsWith("Kapitel")) {
            firstNumbered++;
        }
        assertEquals("the highest chapter must lead the numbered block", "Kapitel 3999",
                targets.get(firstNumbered).substring(0, "Kapitel 3999".length()));
    }

    /** A deterministic pseudo-word so prose strings differ without digits. */
    private static String wordOf(int i) {
        StringBuilder sb = new StringBuilder();
        int v = i;
        do {
            sb.append((char) ('a' + (v % 26)));
            v /= 26;
        } while (v > 0);
        return sb.toString();
    }
}
