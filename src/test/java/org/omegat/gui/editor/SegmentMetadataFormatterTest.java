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

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import org.junit.Test;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * Tests for the segment metadata gutter values.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SegmentMetadataFormatterTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    public void testNumberIsLocalized() {
        assertEquals("42", SegmentMetadataFormatter.number(42, Locale.GERMANY));
        assertEquals("12.345", SegmentMetadataFormatter.number(12345, Locale.GERMANY));
        assertEquals("12,345", SegmentMetadataFormatter.number(12345, Locale.US));
    }

    @Test
    public void testStatus() {
        assertEquals("TU",
                SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE, null, null));
        assertEquals("UU",
                SegmentMetadataFormatter.status(false, SourceTextEntry.DUPLICATE.NONE, null, null));
        assertEquals("TF",
                SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.FIRST, null, null));
        assertEquals("TR",
                SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NEXT, null, null));
        assertEquals("UR",
                SegmentMetadataFormatter.status(false, SourceTextEntry.DUPLICATE.NEXT, null, null));
    }

    @Test
    public void testStatusShowsTheProvenance() {
        assertEquals("TUA", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                TMXEntry.ExternalLinked.xAUTO, null));
        assertEquals("TUE", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                TMXEntry.ExternalLinked.xENFORCED, null));
        assertEquals("TUI", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                TMXEntry.ExternalLinked.xICE, null));
        assertEquals("TUC", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                TMXEntry.ExternalLinked.x100PC, null));
        assertEquals("TU*", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                null, "DeepL"));
        assertEquals("TRA*", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NEXT,
                TMXEntry.ExternalLinked.xAUTO, "DeepL"));
        assertEquals("TU", SegmentMetadataFormatter.status(true, SourceTextEntry.DUPLICATE.NONE,
                null, ""));
    }

    @Test
    public void testAuthorPrefersTheLastChanger() {
        assertEquals("erika", SegmentMetadataFormatter.author("erika", "max"));
        assertEquals("max", SegmentMetadataFormatter.author("", "max"));
        assertEquals("max", SegmentMetadataFormatter.author(null, "max"));
        assertEquals("", SegmentMetadataFormatter.author(null, null));
    }

    @Test
    public void testSegmentProperties() {
        assertEquals("intro.title", SegmentMetadataFormatter.id("intro.title"));
        assertEquals("", SegmentMetadataFormatter.id(null));
        assertEquals("11", SegmentMetadataFormatter.length("elf Zeichen", Locale.GERMANY));
        assertEquals("0", SegmentMetadataFormatter.length("", Locale.GERMANY));
        assertEquals("", SegmentMetadataFormatter.length(null, Locale.GERMANY));
        assertEquals("", SegmentMetadataFormatter.alternative(true));
        assertEquals("alt", SegmentMetadataFormatter.alternative(false));
    }

    @Test
    public void testRegexMatchUsesTheLastGroup() {
        assertEquals("Button", SegmentMetadataFormatter.regexMatch("ui::Button",
                java.util.regex.Pattern.compile("::(.*)")));
        assertEquals("Button", SegmentMetadataFormatter.regexMatch("ui::x::Button",
                java.util.regex.Pattern.compile("(\\w+)::(\\w+)$")));
        assertEquals("::Button", SegmentMetadataFormatter.regexMatch("ui::Button",
                java.util.regex.Pattern.compile("::\\w+")));
        assertEquals("", SegmentMetadataFormatter.regexMatch("ui",
                java.util.regex.Pattern.compile("::(.*)")));
        assertEquals("ui", SegmentMetadataFormatter.regexMatch("ui", null));
    }

    @Test
    public void testLengthOptions() {
        assertEquals("4", SegmentMetadataFormatter.length(" ab c ", Locale.GERMANY, true, false));
        assertEquals("3", SegmentMetadataFormatter.length(" ab c ", Locale.GERMANY, false, true));
        assertEquals("6", SegmentMetadataFormatter.length(" ab c ", Locale.GERMANY, false, false));
    }

    @Test
    public void testDateFallsBackToTheCreationDay() {
        long change = Instant.parse("2026-08-14T12:00:00Z").toEpochMilli();
        long creation = Instant.parse("2025-01-02T23:59:00Z").toEpochMilli();
        assertEquals("2026-08-14", SegmentMetadataFormatter.date(change, creation, UTC));
        assertEquals("2025-01-02", SegmentMetadataFormatter.date(0, creation, UTC));
        assertEquals("", SegmentMetadataFormatter.date(0, 0, UTC));
    }
}
