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

package org.omegat.languagetools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.omegat.core.data.ProtectedPartsFixtures.entryWithProtectedParts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.omegat.core.data.SourceTextEntry;

/**
 * Tests for suppressing LanguageTool matches inside protected parts.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
public class LanguageToolProtectedPartsTest {

    private static LanguageToolResult result(int start, int end) {
        return new LanguageToolResult("message", start, end, "RULE_ID", "rule description");
    }

    @Test
    public void testMatchInsidePlaceholderIsDropped() {
        // The currency rule fires on the "3$" inside the placeholder.
        String translation = "Error: %3$@ Do you want to save the e-prescriptions?";
        SourceTextEntry ste = entryWithProtectedParts("Fehler: %3$@ Wollen Sie speichern?", "%3$@");
        int pos = translation.indexOf("3$");
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Collections.singletonList(result(pos, pos + 2)), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testMatchOutsidePlaceholderIsKept() {
        String translation = "Error: %3$@ Do you want too save?";
        SourceTextEntry ste = entryWithProtectedParts("Fehler: %3$@ Speichern?", "%3$@");
        int pos = translation.indexOf("too");
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Collections.singletonList(result(pos, pos + 3)), ste, translation);
        assertEquals(1, filtered.size());
    }

    @Test
    public void testMatchOverlappingPlaceholderBoundaryIsKept() {
        // A match reaching beyond the placeholder is not fully inside it and
        // may point at a real problem around it, so it survives.
        String translation = "Error:%3$@ text";
        SourceTextEntry ste = entryWithProtectedParts("Fehler: %3$@ Text", "%3$@");
        int start = translation.indexOf(":");
        int end = translation.indexOf("@") + 1;
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Collections.singletonList(result(start, end)), ste, translation);
        assertEquals(1, filtered.size());
    }

    @Test
    public void testAllOccurrencesAreCovered() {
        String translation = "%1$@ and %1$@ again";
        SourceTextEntry ste = entryWithProtectedParts("%1$@ und %1$@ nochmal", "%1$@");
        int first = translation.indexOf("1$");
        int second = translation.lastIndexOf("1$");
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Arrays.asList(result(first, first + 2), result(second, second + 2)), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testMatchCoveringWholePlaceholderIsDropped() {
        String translation = "Error: %3$@ text";
        SourceTextEntry ste = entryWithProtectedParts("Fehler: %3$@ Text", "%3$@");
        int start = translation.indexOf("%3$@");
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Collections.singletonList(result(start, start + "%3$@".length())), ste, translation);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testNullEntryReturnsSameList() {
        List<LanguageToolResult> matches = Collections.singletonList(result(0, 2));
        assertSame(matches, LanguageToolWrapper.filterProtectedParts(matches, null, "some text"));
    }

    @Test
    public void testEntryWithoutProtectedPartsReturnsSameList() {
        String translation = "Plain text";
        SourceTextEntry ste = entryWithProtectedParts("Reiner Text");
        List<LanguageToolResult> matches = Collections.singletonList(result(0, 5));
        assertSame(matches, LanguageToolWrapper.filterProtectedParts(matches, ste, translation));
    }

    @Test
    public void testPlaceholderAbsentFromTranslationKeepsMatches() {
        String translation = "No placeholder here";
        SourceTextEntry ste = entryWithProtectedParts("%3$@ hier", "%3$@");
        List<LanguageToolResult> filtered = LanguageToolWrapper.filterProtectedParts(
                Collections.singletonList(result(0, 2)), ste, translation);
        assertEquals(1, filtered.size());
    }
}
