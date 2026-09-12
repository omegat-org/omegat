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

package org.omegat.gui.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import org.omegat.gui.editor.SegmentEditingOps.TokenSwap;

/**
 * Tests for the pure text computations behind the editing shortcuts.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SegmentEditingOpsTest {

    private static final List<String> NO_PROTECTED = Collections.emptyList();

    @Test
    public void testPlaceablesComeInSourceOrder() {
        List<String> found = SegmentEditingOps.extractPlaceables(
                "Send 12 units to sales@example.com via https://example.com/track?id=9 by 2026", NO_PROTECTED);
        assertEquals(Arrays.asList("12", "sales@example.com", "https://example.com/track?id=9", "2026"),
                found);
    }

    @Test
    public void testProtectedPartsShadowPatternMatches() {
        // the number inside the protected placeholder must not surface as a
        // second, separate placeable
        List<String> found = SegmentEditingOps.extractPlaceables("Order %1 of 5 pieces",
                Collections.singletonList("%1"));
        assertEquals(Arrays.asList("%1", "5"), found);
    }

    @Test
    public void testNumberInsideUrlIsNotASeparatePlaceable() {
        List<String> found = SegmentEditingOps.extractPlaceables("See https://host/path/42 now",
                NO_PROTECTED);
        assertEquals(Collections.singletonList("https://host/path/42"), found);
    }

    @Test
    public void testMissingPlaceablesCountOccurrences() {
        // "7" appears twice in the source but only once in the target: it is
        // missing exactly once, and earlier source occurrences claim the
        // target occurrences first
        List<String> missing = SegmentEditingOps.missingPlaceables("7 of 7 by 12", "7 Stück", NO_PROTECTED);
        assertEquals(Arrays.asList("7", "12"), missing);
    }

    @Test
    public void testWorkingThroughTheMissingListConverges() {
        String source = "<t0>5</t0> kg";
        List<String> protectedTexts = Arrays.asList("<t0>", "</t0>");
        String target = "";
        for (int i = 0; i < 4; i++) {
            List<String> missing = SegmentEditingOps.missingPlaceables(source, target, protectedTexts);
            if (missing.isEmpty()) {
                break;
            }
            target += missing.get(0);
        }
        assertEquals("<t0>5</t0>",
                target);
        assertTrue(SegmentEditingOps.missingPlaceables(source, target, protectedTexts).isEmpty());
    }

    @Test
    public void testTokenSwapForwardKeepsSeparatorAndCaret() {
        // caret inside "quick"; swapping forward moves it past "brown",
        // separator ", " stays where it was
        String text = "quick, brown fox";
        TokenSwap swap = SegmentEditingOps.computeTokenSwap(text, 2, true, Locale.ENGLISH, NO_PROTECTED);
        assertNotNull(swap);
        assertEquals(0, swap.regionStart);
        assertEquals("quick, brown".length(), swap.regionEnd);
        assertEquals("brown, quick", swap.replacement);
        // caret stays at offset 2 inside the moved token "quick"
        assertEquals("brown, ".length() + 2, swap.caretAfter);
    }

    @Test
    public void testTokenSwapBackwardMovesTokenToFront() {
        String text = "quick brown";
        int caretInBrown = "quick b".length();
        TokenSwap swap = SegmentEditingOps.computeTokenSwap(text, caretInBrown, false, Locale.ENGLISH, NO_PROTECTED);
        assertNotNull(swap);
        assertEquals("brown quick", swap.replacement);
        // caret keeps its offset inside "brown", which now starts the text
        assertEquals(1, swap.caretAfter);
    }

    @Test
    public void testTokenSwapAtEdgesIsRefused() {
        String text = "one two";
        assertNull(SegmentEditingOps.computeTokenSwap(text, 1, false, Locale.ENGLISH, NO_PROTECTED));
        assertNull(SegmentEditingOps.computeTokenSwap(text, text.length(), true, Locale.ENGLISH, NO_PROTECTED));
    }

    @Test
    public void testTokenSwapTreatsTagsAsAtomicTokens() {
        // swapping "foo" forward moves it over the whole tag instead of
        // splitting the tag into pieces
        String text = "foo <t0> bar";
        TokenSwap swap = SegmentEditingOps.computeTokenSwap(text, 1, true, Locale.ENGLISH,
                Collections.singletonList("<t0>"));
        assertNotNull(swap);
        assertEquals("<t0> foo", swap.replacement);
        assertEquals(0, swap.regionStart);
        assertEquals("foo <t0>".length(), swap.regionEnd);
        // caret keeps its offset inside "foo", which now follows the tag
        assertEquals("<t0> ".length() + 1, swap.caretAfter);
    }

    @Test
    public void testTokenSwapFromInsideTagMovesTheTag() {
        String text = "foo <t0> bar";
        TokenSwap swap = SegmentEditingOps.computeTokenSwap(text, 6, false, Locale.ENGLISH,
                Collections.singletonList("<t0>"));
        assertNotNull(swap);
        assertEquals("<t0> foo", swap.replacement);
    }

    @Test
    public void testNullProtectedTextsAreIgnored() {
        List<String> found = SegmentEditingOps.extractPlaceables("Order 5 pieces",
                Arrays.asList(null, "", "5"));
        assertEquals(Collections.singletonList("5"), found);
    }

    @Test
    public void testEmailInsideUrlIsNotASeparatePlaceable() {
        List<String> found = SegmentEditingOps.extractPlaceables("See https://user@host.example/x now",
                NO_PROTECTED);
        assertEquals(Collections.singletonList("https://user@host.example/x"), found);
    }

    @Test
    public void testNumberInsideWordIsAPlaceable() {
        // documented simple semantics: digits in mixed tokens still count,
        // consistently on both the source and the target side
        assertTrue(SegmentEditingOps.missingPlaceables("Bond007", "Bond007", NO_PROTECTED).isEmpty());
        assertEquals(Collections.singletonList("007"),
                SegmentEditingOps.missingPlaceables("Bond007", "", NO_PROTECTED));
    }

    @Test
    public void testWorkingThroughTheMissingListBackwards() {
        String source = "1 a 2 b 3";
        String target = "";
        for (int i = 0; i < 4; i++) {
            List<String> missing = SegmentEditingOps.missingPlaceables(source, target, NO_PROTECTED);
            if (missing.isEmpty()) {
                break;
            }
            target = missing.get(missing.size() - 1) + target;
        }
        assertEquals("123", target);
    }

    @Test
    public void testCaretOnTokenBoundaryBelongsToTheLeftToken() {
        // caret exactly between "one" and the space: the left token moves
        TokenSwap swap = SegmentEditingOps.computeTokenSwap("one two", 3, true, Locale.ENGLISH,
                NO_PROTECTED);
        assertNotNull(swap);
        assertEquals("two one", swap.replacement);
    }

    @Test
    public void testFirstMissingTagPairPicksThePairEntry() {
        // grouped offerings mix single tags and pair entries; only entries
        // carrying the sentinel are pairs
        String[] pair = SegmentEditingOps.firstMissingTagPair(
                Arrays.asList("<t0>", "<t0>\uE100</t0>", "</t0>"), "\uE100");
        assertNotNull(pair);
        assertEquals("<t0>", pair[0]);
        assertEquals("</t0>", pair[1]);
        assertNull(SegmentEditingOps.firstMissingTagPair(Arrays.asList("<t0>", "</t0>"), "\uE100"));
    }

    @Test
    public void testTokenSwapOffTokenIsRefused() {
        // caret in the separator between the tokens: no token to move
        assertNull(SegmentEditingOps.computeTokenSwap("one  two", 4, true, Locale.ENGLISH, NO_PROTECTED));
    }
}
