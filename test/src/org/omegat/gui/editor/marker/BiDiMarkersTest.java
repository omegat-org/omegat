/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.editor.marker;

import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.BidiMarkers;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;

public class BiDiMarkersTest extends MarketTestBase {

    @Before
    public void preUp() {
        TestCoreInitializer.initEditor(editor);
    }

    @Test
    public void testBidiMarkersDisabled() throws Exception {
        IMarker marker = new BidiMarkers();
        Core.getEditor().getSettings().setMarkBidi(false);
        assertNull(marker.getMarksForEntry(null, null, null, true));
    }

    @Test
    public void testBidiMarkersNotActive() throws Exception {
        IMarker marker = new BidiMarkers();
        Core.getEditor().getSettings().setMarkBidi(true);
        assertEquals(EMPTY_LIST, marker.getMarksForEntry(null, null, null, false));
    }

    @Test
    public void testBidiMarkersNoBidi() throws Exception {
        IMarker marker = new BidiMarkers();
        Core.getEditor().getSettings().setMarkBidi(true);
        EntryKey ek = new EntryKey("file", "edit", "10", null, null, null);
        SourceTextEntry ste = new SourceTextEntry(ek, 0, null, null, new ArrayList<>());
        assertEquals(EMPTY_LIST, marker.getMarksForEntry(ste, "edit", "edit", true));
    }

    @Test
    public void testMarkersBidi() throws Exception {
        IMarker marker = new BidiMarkers();
        Core.getEditor().getSettings().setMarkBidi(true);
        String sourceText = "\u0645\u0644\u0641\u0627\u062A\u202a XHTML";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, sourceText, true);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).startOffset);
        assertEquals(5, result.get(0).endOffset);
    }

    @Test
    public void testMarkersBidi2() throws Exception {
        IMarker marker = new BidiMarkers();
        Core.getEditor().getSettings().setMarkBidi(true);
        String sourceText = "The title is \"\u0645\u0641\u062A\u0627\u062D \u0645\u0639\u0627\u064A\u064A\u0631"
                + " \u0627\u0644\u0648\u064A\u0628!\u200F\" in Arabic.";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, sourceText, true);
        assertEquals(1, result.size());
        assertEquals(33, result.get(0).startOffset);
        assertEquals(34, result.get(0).endOffset);
        assertEquals("TRANSLATION", result.get(0).entryPart.toString());
    }

}
