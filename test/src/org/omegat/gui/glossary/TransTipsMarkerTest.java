/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.gui.glossary;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TestCoreState;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransTipsMarkerTest extends TestCore {

    @Before
    public final void setUp() {
        Preferences.setPreference(Preferences.MARK_GLOSSARY_MATCHES, true);
        initEditor(null);
        TestCoreState.getInstance().getEditor().getSettings().setMarkGlossaryMatches(true);
    }

    /**
     * Test case: getMarksForEntry returns null when isActive is false.
     */
    @Test
    public void testGetMarksForEntryInactive() {
        TransTipsMarker marker = new TransTipsMarker();
        SourceTextEntry ste = mock(SourceTextEntry.class);

        List<Mark> marks = marker.getMarksForEntry(ste, "source", "translation", false);

        assertNull(marks);
    }

    /**
     * Test case: getMarksForEntry returns null when sourceText is null.
     */
    @Test
    public void testGetMarksForEntryNullSourceText() {
        TransTipsMarker marker = new TransTipsMarker();
        SourceTextEntry ste = mock(SourceTextEntry.class);

        List<Mark> marks = marker.getMarksForEntry(ste, null, "translation", true);

        assertNull(marks);
    }

    /**
     * Test case: getMarksForEntry returns null when isMarkGlossaryMatches is disabled.
     */
    @Test
    public void testGetMarksForEntryGlossaryMatchingDisabled() {
        TestCoreState.getInstance().getEditor().getSettings().setMarkGlossaryMatches(false);
        TransTipsMarker marker = new TransTipsMarker();
        SourceTextEntry ste = mock(SourceTextEntry.class);
        List<Mark> marks = marker.getMarksForEntry(ste, "source", "translation", true);

        assertNull(marks);
    }

    /**
     * Test case: getMarksForEntry returns null when glossary entries are empty.
     */
    @Test
    public void testGetMarksForEntryNoGlossaryEntries() {
        IGlossaries glossaryMock = mock(IGlossaries.class);
        when(glossaryMock.getDisplayedEntries()).thenReturn(Collections.emptyList());
        TestCoreState.getInstance().setGlossaries(glossaryMock);

        TransTipsMarker marker = new TransTipsMarker();
        SourceTextEntry ste = mock(SourceTextEntry.class);

        List<Mark> marks = marker.getMarksForEntry(ste, "source", "translation", true);

        assertNull(marks);
    }

    /**
     * Test case: getMarksForEntry correctly processes glossary matches.
     */
    @Test
    public void testGetMarksForEntryValidGlossaryMatches() {
        GlossaryEntry glossaryEntry = mock(GlossaryEntry.class);
        when(glossaryEntry.getSrcText()).thenReturn("source text");
        when(glossaryEntry.getLocText()).thenReturn("translation");
        List<GlossaryEntry> glossaryEntries = new ArrayList<>();
        glossaryEntries.add(glossaryEntry);

        IGlossaries glossaryMock = mock(IGlossaries.class);
        when(glossaryMock.getDisplayedEntries()).thenReturn(glossaryEntries);
        TestCoreState.getInstance().setGlossaries(glossaryMock);

        IGlossaryRenderer rendererMock = mock(IGlossaryRenderer.class);
        when(rendererMock.renderToHtml(glossaryEntry)).thenReturn("tooltip");
        GlossaryRenderers.setPreferredGlossaryRenderer(rendererMock);

        GlossaryManager glossaryManagerMock = mock(GlossaryManager.class);
        List<Token[]> tokenMatches = new ArrayList<>();
        tokenMatches.add(new Token[]{new Token("text", 7)});
        tokenMatches.add(new Token[]{new Token("source", 0)});
        when(glossaryManagerMock.searchSourceMatchTokens(any(SourceTextEntry.class), eq(glossaryEntry)))
                .thenReturn(tokenMatches);
        TestCoreState.getInstance().setGlossaryManager(glossaryManagerMock);

        SourceTextEntry ste = mock(SourceTextEntry.class);
        when(ste.getSrcText()).thenReturn("source text");

        TransTipsMarker marker = new TransTipsMarker();
        List<Mark> marks = marker.getMarksForEntry(ste, "source text", "translation", true);

        assertNotNull(marks);
        assertEquals(1, marks.size());
        assertEquals(0, marks.get(0).startOffset);
        assertEquals(11, marks.get(0).endOffset);
        assertEquals("tooltip", marks.get(0).toolTipText);
    }

    /**
     * Test case: getMarksForEntry handles null or empty token matches gracefully.
     */
    @Test
    public void testGetMarksForEntryEmptyTokenMatches() {
        GlossaryEntry glossaryEntry = mock(GlossaryEntry.class);
        List<GlossaryEntry> glossaryEntries = new ArrayList<>();
        glossaryEntries.add(glossaryEntry);


        IGlossaries glossaryMock = mock(IGlossaries.class);
        when(glossaryMock.getDisplayedEntries()).thenReturn(glossaryEntries);
        TestCoreState.getInstance().setGlossaries(glossaryMock);

        IGlossaryRenderer rendererMock = mock(IGlossaryRenderer.class);
        when(rendererMock.renderToHtml(glossaryEntry)).thenReturn("tooltip");
        GlossaryRenderers.setPreferredGlossaryRenderer(rendererMock);

        GlossaryManager glossaryManagerMock = mock(GlossaryManager.class);
        when(glossaryManagerMock.searchSourceMatchTokens(any(SourceTextEntry.class), eq(glossaryEntry)))
                .thenReturn(Collections.emptyList());
        TestCoreState.getInstance().setGlossaryManager(glossaryManagerMock);

        SourceTextEntry ste = mock(SourceTextEntry.class);
        when(ste.getSrcText()).thenReturn("source text");

        TransTipsMarker marker = new TransTipsMarker();
        List<Mark> marks = marker.getMarksForEntry(ste, "source text", "translation", true);

        assertNotNull(marks);
        assertTrue(marks.isEmpty());
    }
}
