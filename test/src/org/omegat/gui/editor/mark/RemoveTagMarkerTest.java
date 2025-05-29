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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.re2j.Pattern;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.OStrings;

public class RemoveTagMarkerTest extends MarkerTestBase  {

    @Before
    public void preUp() throws IOException {
        TestCoreInitializer.initEditor(editor);
    }

    @Test
    public void testRemoveTagMarker() throws Exception {
        RemoveTagMarker marker = new RemoveTagMarker();
        /* The marker updates its remove pattern from preference,
           when project change event fired.
           Here replace it with direct update to avoid interference
           among test cases.
         */
        marker.pattern = Pattern.compile("%remove");
        assertNotNull(marker.pattern);
        String sourceText = "source %remove";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, null, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(7, result.get(0).startOffset);
        assertEquals(14, result.get(0).endOffset);
        assertEquals(OStrings.getString("MARKER_REMOVETAG"), result.get(0).toolTipText);
        assertEquals("SOURCE", result.get(0).entryPart.toString());
    }

}
