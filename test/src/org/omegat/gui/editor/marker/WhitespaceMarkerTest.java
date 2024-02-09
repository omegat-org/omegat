/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.editor.mark.WhitespaceMarker;

public class WhitespaceMarkerTest extends MarketTestBase {

    @Before
    public void preUp() {
        TestCoreInitializer.initEditor(editor);
    }

    @Test
    public void testMarkersDisabled() throws Exception {
        IMarker marker = new WhitespaceMarker();
        Core.getEditor().getSettings().setMarkWhitespace(false);
        assertNull(marker.getMarksForEntry(null, null, null, true));
    }

    @Test
    public void testMarkersNotActive() throws Exception {
        IMarker marker = new WhitespaceMarker();
        Core.getEditor().getSettings().setMarkWhitespace(true);
        assertEquals(null, marker.getMarksForEntry(null, null, null, false));
    }

    @Test
    public void testMarkersSP() throws Exception {
        IMarker marker = new WhitespaceMarker();
        Core.getEditor().getSettings().setMarkWhitespace(true);
        String sourceText = "source text with \tTAB.";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, sourceText, true);
        assertEquals(8, result.size());
        assertEquals(6, result.get(0).startOffset);
        assertEquals(7, result.get(0).endOffset);
        assertEquals(17, result.get(3).startOffset);
        assertEquals(18, result.get(3).endOffset);
        assertEquals("Tab", result.get(3).toolTipText);
        assertEquals("SOURCE", result.get(3).entryPart.toString());
        assertEquals(17, result.get(7).startOffset);
        assertEquals(18, result.get(7).endOffset);
        assertEquals("Tab", result.get(7).toolTipText);
        assertEquals("TRANSLATION", result.get(7).entryPart.toString());
    }
}
