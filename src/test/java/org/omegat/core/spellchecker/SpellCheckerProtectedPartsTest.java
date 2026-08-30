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

package org.omegat.core.spellchecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.omegat.core.data.ProtectedPartsFixtures.entryWithProtectedParts;
import static org.omegat.core.data.ProtectedPartsFixtures.tokenAt;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Token;

/**
 * Words inside protected placeholders are not prose, so they get no
 * spelling marks: the "ld" inside "%1$ld" is placeholder syntax, not a
 * misspelled word.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SpellCheckerProtectedPartsTest {

    @Test
    public void testTokenInsidePlaceholderIsDropped() {
        String translation = "Server error %1$ld: %2$@";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld: %2$@", "%1$ld", "%2$@");
        List<Token> filtered = SpellCheckerMarker.filterProtectedParts(
                Collections.singletonList(tokenAt(translation, "ld")), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testTokenOutsidePlaceholderIsKept() {
        String translation = "Server errorr %1$ld";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld", "%1$ld");
        List<Token> filtered = SpellCheckerMarker.filterProtectedParts(
                Collections.singletonList(tokenAt(translation, "errorr")), ste, translation);
        assertEquals(1, filtered.size());
    }

    @Test
    public void testTokenOverlappingPlaceholderBoundaryIsKept() {
        // The tokenizer never produces such a token today, but the filter
        // must not swallow anything reaching outside the placeholder.
        String translation = "errld%1$ld";
        SourceTextEntry ste = entryWithProtectedParts("%1$ld", "%1$ld");
        List<Token> filtered = SpellCheckerMarker.filterProtectedParts(
                Collections.singletonList(new Token("errld%1$", 0, 8)), ste, translation);
        assertEquals(1, filtered.size());
    }

    @Test
    public void testAllOccurrencesAreCovered() {
        String translation = "%1$ld or %1$ld";
        SourceTextEntry ste = entryWithProtectedParts("%1$ld oder %1$ld", "%1$ld");
        Token second = new Token("ld", translation.lastIndexOf("ld"), 2);
        List<Token> filtered = SpellCheckerMarker.filterProtectedParts(
                Collections.singletonList(second), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testNullEntryFiltersNothing() {
        List<Token> tokens = Collections.singletonList(new Token("ld", 0, 2));
        assertSame(tokens, SpellCheckerMarker.filterProtectedParts(tokens, null, "ld"));
    }

    @Test
    public void testTokenCoveringWholePlaceholderIsDropped() {
        String translation = "Server error %1$ld";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld", "%1$ld");
        Token whole = new Token("%1$ld", translation.indexOf("%1$ld"), 5);
        List<Token> filtered = SpellCheckerMarker.filterProtectedParts(
                Collections.singletonList(whole), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testEntryWithoutProtectedPartsReturnsSameList() {
        String translation = "Server errorr";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler");
        List<Token> tokens = Collections.singletonList(tokenAt(translation, "errorr"));
        assertSame(tokens, SpellCheckerMarker.filterProtectedParts(tokens, ste, translation));
    }

    @Test
    public void testPlaceholderAbsentFromTranslationKeepsTokens() {
        String translation = "Server errorr without placeholder";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld", "%1$ld");
        List<Token> tokens = Collections.singletonList(tokenAt(translation, "errorr"));
        assertSame(tokens, SpellCheckerMarker.filterProtectedParts(tokens, ste, translation));
    }

    @Test
    public void testOccurrenceHelperSkipsNullAndEmptyPartTexts() {
        // SourceTextEntry never delivers such parts, so the helper's guard
        // is exercised directly: without it, null throws and the empty
        // text loops forever.
        ProtectedPart nullText = new ProtectedPart();
        ProtectedPart emptyText = new ProtectedPart();
        emptyText.setTextInSourceSegment("");
        ProtectedPart real = new ProtectedPart();
        real.setTextInSourceSegment("%1$ld");
        List<int[]> occurrences = ProtectedPart.occurrencesIn("Server error %1$ld",
                new ProtectedPart[] { nullText, emptyText, real });
        assertEquals(1, occurrences.size());
    }
}
