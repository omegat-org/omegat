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
package org.omegat.gui.editor.mark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.PatternConsts;
import org.omegat.util.TagUtil;

public class ProtectedPartsMarkerTest extends MarkerTestBase {

    @Before
    public void preUp() {
        TestCoreInitializer.initEditor(editor);
    }

    @Test
    public void testMarkerProtectedParts() throws Exception {
        IMarker marker = new ProtectedPartsMarker();
        String sourceText = "source %s text.";
        List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(sourceText, PatternConsts.PRINTF_VARS,
                null);
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, protectedParts);
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, null, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(7, result.get(0).startOffset);
        assertEquals(9, result.get(0).endOffset);
        assertEquals("%s", result.get(0).toolTipText);
        assertEquals("SOURCE", result.get(0).entryPart.toString());
    }

}
