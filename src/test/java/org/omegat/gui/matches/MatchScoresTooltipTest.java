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

package org.omegat.gui.matches;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;

import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.matching.NearString;
import org.omegat.util.LocaleRule;

/**
 * Tests for the fuzzy-match score tooltip (#465).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchScoresTooltipTest {

    @Rule
    public final LocaleRule localeRule = new LocaleRule(Locale.ENGLISH);

    private NearString near(String source, int score, int noStem, int adjusted, int penalty,
            boolean fuzzy) {
        PrepareTMXEntry entry = new PrepareTMXEntry();
        entry.source = source;
        entry.translation = "translation";
        return new NearString(null, entry, NearString.MATCH_SOURCE.MEMORY, fuzzy,
                new NearString.Scores(score, noStem, adjusted, penalty), null, null);
    }

    @Test
    public void scoresLineAlwaysPresent() {
        String tip = MatchScoresTooltip.render(3, near("Chapter 12", 62, 60, 58, 0, false), "Chapter 12");
        assertTrue("title line with number and text expected: " + tip,
                tip.contains("#3") && tip.contains("Chapter 12"));
        assertTrue(tip, tip.contains("62%") && tip.contains("60%") && tip.contains("58%"));
        assertFalse("no warning for identical numbers", tip.contains("Warning"));
        assertFalse("no penalty line without penalty", tip.contains("penalty"));
    }

    @Test
    public void warnsWhenNumbersDiffer() {
        String tip = MatchScoresTooltip.render(1, near("Paid on 2007-11-21.", 100, 100, 87, 0, false),
                "Paid on 2007-11-25.");
        assertTrue(tip, tip.contains("Warning"));
    }

    @Test
    public void sameValueInOtherScriptDoesNotWarn() {
        // Arabic-Indic 12 equals ASCII 12 by value.
        assertFalse(MatchScoresTooltip.numbersDiffer("Chapter ١٢", "Chapter 12"));
        // Full-width 12 as well.
        assertFalse(MatchScoresTooltip.numbersDiffer("Chapter １２", "Chapter 12"));
        // Repetition counts: "5 of 5" differs from "5".
        assertTrue(MatchScoresTooltip.numbersDiffer("5 of 5", "5"));
    }

    @Test
    public void penaltyAndFuzzyLines() {
        String tip = MatchScoresTooltip.render(1, near("source", 52, 50, 48, 10, true), "source");
        assertTrue(tip, tip.contains("10%"));
        assertTrue(tip, tip.contains("40%"));
    }
}
