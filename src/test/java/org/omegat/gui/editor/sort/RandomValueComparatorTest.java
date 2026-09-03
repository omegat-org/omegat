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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Tests for {@link RandomValueComparator}: seed-derived pseudo-random order
 * that groups equal values and is reproducible per seed.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RandomValueComparatorTest {

    private static final List<String> VALUES = Arrays.asList("Anna", "Bert", "Carla", "Dora", "Emil",
            "Fritz", "Gerd", "Hanna");

    private static List<String> sorted(long seed) {
        List<String> copy = new ArrayList<>(VALUES);
        copy.sort(new RandomValueComparator(Collator.getInstance(Locale.ENGLISH), seed));
        return copy;
    }

    @Test
    public void sameSeedReproducesTheSameOrder() {
        assertEquals(sorted(42L), sorted(42L));
        assertEquals(sorted(0L), sorted(0L));
    }

    @Test
    public void differentSeedsShuffleDifferently() {
        // Eight values have 40320 orders; a handful of seeds mapping to the
        // same one would mean the seed is not feeding the rank at all.
        List<String> a = sorted(1L);
        assertTrue(!a.equals(sorted(2L)) || !a.equals(sorted(3L)) || !a.equals(sorted(4L)));
    }

    @Test
    public void equalValuesGetEqualRanks() {
        RandomValueComparator cmp = new RandomValueComparator(Collator.getInstance(Locale.ENGLISH), 7L);
        assertEquals(0, cmp.compare("Anna", "Anna"));
        List<String> withDupes = new ArrayList<>(Arrays.asList("Bert", "Anna", "Bert", "Carla", "Anna"));
        withDupes.sort(cmp);
        // Equal values must end up adjacent (grouped), wherever the group landed.
        assertEquals(withDupes.indexOf("Anna") + 1, withDupes.lastIndexOf("Anna"));
        assertEquals(withDupes.indexOf("Bert") + 1, withDupes.lastIndexOf("Bert"));
    }

    @Test
    public void orderDoesNotShuffleAlphabetically() {
        // A shuffled order that equals the alphabetical one for several seeds
        // would suggest the rank degenerated to the collator fallback.
        List<String> alpha = new ArrayList<>(VALUES);
        assertTrue(!alpha.equals(sorted(1L)) || !alpha.equals(sorted(5L)) || !alpha.equals(sorted(9L)));
    }

    @Test
    public void ranksArePlatformStable() {
        // The rank function is part of the persisted-preference contract:
        // seed 42 must order these values identically on every platform/JVM.
        assertEquals(Arrays.asList("Anna", "Fritz", "Emil", "Gerd", "Bert", "Carla", "Hanna", "Dora"),
                sorted(42L));
    }

    @Test
    public void primeOnlyFillsTheCache() {
        RandomValueComparator cmp = new RandomValueComparator(Collator.getInstance(Locale.ENGLISH), 11L);
        VALUES.forEach(cmp::prime);
        List<String> primed = new ArrayList<>(VALUES);
        primed.sort(cmp);
        assertEquals(sorted(11L), primed);
    }

    @Test
    public void comparatorContractSpotCheck() {
        RandomValueComparator cmp = new RandomValueComparator(Collator.getInstance(Locale.ENGLISH), 3L);
        for (String a : VALUES) {
            for (String b : VALUES) {
                assertEquals("antisymmetry for " + a + "/" + b, Integer.signum(cmp.compare(a, b)),
                        -Integer.signum(cmp.compare(b, a)));
                for (String c : VALUES) {
                    if (cmp.compare(a, b) <= 0 && cmp.compare(b, c) <= 0) {
                        assertTrue("transitivity for " + a + "/" + b + "/" + c, cmp.compare(a, c) <= 0);
                    }
                }
            }
        }
        assertNotEquals(0, cmp.compare("Anna", "Bert"));
    }
}
