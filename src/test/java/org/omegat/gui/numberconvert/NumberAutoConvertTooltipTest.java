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
package org.omegat.gui.numberconvert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.LocaleRule;

/**
 * The segment-context half of the results-table row tooltip: every piece of
 * information the project has about the segment appears, and absent pieces
 * do not leave empty lines behind.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumberAutoConvertTooltipTest {

    /** The assertions check the English label texts. */
    @Rule
    public final LocaleRule localeRule = new LocaleRule(Locale.ENGLISH);

    private static SourceTextEntry entry(String id, String[] props) {
        return new SourceTextEntry(new EntryKey("manual/chapter1.txt", "42", id, "", "", null), 7, props,
                null, Collections.emptyList());
    }

    @Test
    public void bareUntranslatedEntryShowsOnlySegmentAndFile() {
        String html = NumberAutoConvertWindow.segmentInfoHtml(entry(null, null), null);
        assertTrue(html.contains("Segment 7"));
        assertTrue(html.contains("manual/chapter1.txt"));
        assertFalse("no ID line without an ID", html.contains("ID:"));
        assertFalse("no comment line without a comment", html.contains("Comment:"));
        assertFalse("no translation block when untranslated", html.contains("Current translation:"));
    }

    @Test
    public void presentInformationAllAppears() {
        SourceTextEntry ste = entry("msg.42", new String[] { "comment", "the answer" });
        TMXEntry info = mock(TMXEntry.class);
        when(info.isTranslated()).thenReturn(true);
        when(info.getTranslationText()).thenReturn("zweiundvierzig");
        when(info.getChanger()).thenReturn("alice");
        when(info.getChangeDate()).thenReturn(1_700_000_000_000L);
        when(info.getNote()).thenReturn("checked twice");

        String html = NumberAutoConvertWindow.segmentInfoHtml(ste, info);
        assertTrue(html.contains("ID: msg.42"));
        assertTrue(html.contains("the answer"));
        assertTrue(html.contains("zweiundvierzig"));
        assertTrue(html.contains("alice"));
        assertTrue(html.contains("checked twice"));
    }

    @Test
    public void creatorIsTheFallbackWhenNoChangerIsRecorded() {
        TMXEntry info = mock(TMXEntry.class);
        when(info.isTranslated()).thenReturn(true);
        when(info.getTranslationText()).thenReturn("42");
        when(info.getCreator()).thenReturn("bob");
        when(info.getCreationDate()).thenReturn(1_700_000_000_000L);

        String html = NumberAutoConvertWindow.segmentInfoHtml(entry(null, null), info);
        assertFalse(html.contains("Changed by"));
        assertTrue(html.contains("Created by bob"));
    }

    @Test
    public void userTextIsHtmlEscapedAndLongTextClipped() {
        StringBuilder note = new StringBuilder("<b>&");
        for (int i = 0; i < 300; i++) {
            note.append('x');
        }
        TMXEntry info = mock(TMXEntry.class);
        when(info.isTranslated()).thenReturn(true);
        when(info.getTranslationText()).thenReturn("<script>");
        when(info.getNote()).thenReturn(note.toString());

        String html = NumberAutoConvertWindow.segmentInfoHtml(entry(null, null), info);
        assertFalse("markup in user text must not survive", html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&lt;b&gt;&amp;"));
        assertTrue("long notes are clipped", html.contains("…"));
        assertFalse(html.contains("x".repeat(200)));
    }

    @Test
    public void repeatedSegmentsShowTheDuplicateCount() {
        // The duplicate links are wired package-privately during project
        // load, so a stubbed entry stands in for a real duplicate here.
        SourceTextEntry ste = mock(SourceTextEntry.class);
        when(ste.entryNum()).thenReturn(7);
        when(ste.getKey()).thenReturn(new EntryKey("manual/chapter1.txt", "42", null, "", "", null));
        when(ste.getDuplicate()).thenReturn(SourceTextEntry.DUPLICATE.FIRST);
        when(ste.getNumberOfDuplicates()).thenReturn(3);

        String html = NumberAutoConvertWindow.segmentInfoHtml(ste, null);
        assertTrue(html.contains("Repeated segment, +3 more"));
    }
}
