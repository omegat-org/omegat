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
package org.omegat.gui.editor;

import org.junit.Test;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EditorTextLoadedTest extends EditorTestBase {

    private static final String INITIAL_TEXT = "Error {0}: {1}";
    private static final String TARGET_TEXT = "API key (optional)";

    private final List<SourceTextEntry> selectedEntries = new ArrayList<>();
    private final CountDownLatch initialLoadLatch = new CountDownLatch(1);
    private final CountDownLatch selectionChangeLatch = new CountDownLatch(2);

    @Test
    public void testEditorTextLoadedAndClickSingle() throws Exception {
        CoreEvents.registerEntryEventListener(new TestingEditorEntryListener(selectedEntries, initialLoadLatch,
                selectionChangeLatch));
        openSampleProject(PROJECT_PATH);
        assertTrue("Editor show first entry.", initialLoadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        verifyInitialTextSelection();
        //
        clickSegment(TARGET_TEXT);
        //
        assertTrue("Editor select clicked entry", selectionChangeLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        SourceTextEntry newEntry = selectedEntries.getLast();
        assertEquals(TARGET_TEXT, newEntry.getSrcText());
    }

    private void verifyInitialTextSelection() {
        SourceTextEntry entry = selectedEntries.getLast();
        assertEquals(INITIAL_TEXT, entry.getSrcText());
    }
}
