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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Map;

import org.junit.Test;

import org.omegat.core.matching.MatchEquivalence;

/**
 * The shipped test-area prefill of the equivalence dialog: both sample
 * variants fold onto each other with the default class set and stay apart
 * without folding.
 *
 * @author Stephan Pakebusch
 */
public class MatchEquivalenceDialogTest {

    @Test
    public void sampleVariantsFoldEqualByDefault() {
        Map<Integer, String> foldMap = MatchEquivalence.buildFoldMap(MatchEquivalence.all());
        assertEquals(MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_TYPOGRAPHIC, foldMap),
                MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_PLAIN, foldMap));
    }

    @Test
    public void sampleVariantsDifferWithoutFolding() {
        Map<Integer, String> empty = MatchEquivalence
                .buildFoldMap(EnumSet.noneOf(MatchEquivalence.class));
        assertNotEquals(MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_TYPOGRAPHIC, empty),
                MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_PLAIN, empty));
    }

    /**
     * Every member line names its own code point and shows the replacement
     * target with the target's code points, so the direction of the folding
     * is visible; removals name themselves without a target code point.
     */
    @Test
    public void memberLinesShowSourceAndTargetCodePoints() {
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            String[] lines = MatchEquivalenceDialog.describeMembers(eq);
            assertEquals(eq.getMembers().size(), lines.length);
            int i = 0;
            for (Map.Entry<Integer, String> member : eq.getMembers().entrySet()) {
                String line = lines[i++];
                assertTrue(line, line.contains(String.format("U+%04X", member.getKey())));
                assertTrue(line, line.contains("→"));
                if (!member.getValue().isEmpty()) {
                    int targetCp = member.getValue().codePointAt(0);
                    assertTrue(line, line.contains(String.format("U+%04X", targetCp)));
                }
            }
        }
    }

    /**
     * One summary line per group of inter-compatible members above each
     * inventory; the quotes class splits into its double-quote and apostrophe
     * groups. Visible members appear as glyph, invisible ones as code point.
     */
    @Test
    public void groupLinesJoinInterCompatibleMembers() {
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            java.util.List<String> lines = MatchEquivalenceDialog.describeGroups(eq);
            long targets = eq.getMembers().values().stream().distinct().count();
            assertEquals(lines.toString(), targets, lines.size());
            String joined = String.join(" ", lines);
            eq.getMembers().keySet().forEach(cp -> assertTrue(joined,
                    joined.contains(new String(Character.toChars(cp)))
                            || joined.contains(String.format("U+%04X", cp))));
        }
    }

    /**
     * The pure character pairs (the first three) fold equal with the full
     * class set and stay apart without folding.
     */
    @Test
    public void shippedCharExamplesFoldEqual() {
        Map<Integer, String> full = MatchEquivalence.buildFoldMap(MatchEquivalence.all());
        Map<Integer, String> empty = MatchEquivalence
                .buildFoldMap(EnumSet.noneOf(MatchEquivalence.class));
        for (int i = 0; i < 3; i++) {
            String[] pair = MatchEquivalenceDialog.EXAMPLES[i];
            assertEquals(pair[0] + " | " + pair[1], MatchEquivalence.fold(pair[0], full),
                    MatchEquivalence.fold(pair[1], full));
            assertNotEquals(pair[0], MatchEquivalence.fold(pair[0], empty),
                    MatchEquivalence.fold(pair[1], empty));
        }
    }

    /**
     * Group lines answer hover positions with the member behind the token, so
     * the tooltip can show the character's own replacement line.
     */
    @Test
    public void groupLineMapsOffsetsToMembers() {
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            for (MatchEquivalenceDialog.GroupLine line : MatchEquivalenceDialog.describeGroupLines(eq)) {
                String text = line.getText();
                java.util.List<Integer> cps = line.getMemberCps();
                int token = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == ' ') {
                        token++;
                        assertEquals(-1, line.memberAt(i));
                    } else {
                        // Every character of a token (both halves of an astral
                        // surrogate pair included) maps to the token's member.
                        assertEquals(text + "@" + i, (int) cps.get(token), line.memberAt(i));
                    }
                }
                assertEquals(cps.size() - 1, token);
                assertEquals(-1, line.memberAt(-1));
                assertEquals(-1, line.memberAt(text.length()));
            }
        }
    }

    /**
     * Every shipped example pair of the number test compares equal with the
     * numbers option and the Roman sub-option on; the Latin-letter Roman pair
     * is the only one that needs the sub-option.
     */
    @Test
    public void shippedNumberExamplesCompareEqual() {
        for (String[] pair : MatchEquivalenceDialog.EXAMPLES) {
            assertTrue(pair[0] + " | " + pair[1], MatchEquivalenceDialog.numbersCompareEqual(
                    pair[0], pair[1], MatchEquivalence.all(), true, true, null, null));
        }
        String[] roman = MatchEquivalenceDialog.EXAMPLES[MatchEquivalenceDialog.EXAMPLES.length - 1];
        assertFalse(MatchEquivalenceDialog.numbersCompareEqual(roman[0], roman[1],
                MatchEquivalence.all(), true, false, null, null));
    }

    /**
     * The locale examples pair the same value in source- and target-locale
     * formatting; the comparator resolves each side with its own locale.
     */
    @Test
    public void localeNumberExamplesCompareEqual() {
        java.util.Locale de = java.util.Locale.GERMANY;
        java.util.Locale en = java.util.Locale.US;
        for (String[] pair : MatchEquivalenceDialog.localeExamples(de, en)) {
            assertTrue(pair[0] + " | " + pair[1], MatchEquivalenceDialog.numbersCompareEqual(
                    pair[0], pair[1], MatchEquivalence.all(), true, true, de, en));
        }
    }
}
