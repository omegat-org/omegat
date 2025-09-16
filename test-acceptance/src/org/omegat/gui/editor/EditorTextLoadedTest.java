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
import org.omegat.core.events.IEntryEventListener;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EditorTextLoadedTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    private static final int TIMEOUT_SECONDS = 10;
    private static final String INITIAL_TEXT = "Error {0}: {1}";
    private static final String TARGET_TEXT = "API key (optional)";
    private static final String EDITOR_TITLE = "Editor - Bundle.properties";

    private final List<SourceTextEntry> selectedEntries = new ArrayList<>();
    private final CountDownLatch initialLoadLatch = new CountDownLatch(1);
    private final CountDownLatch selectionChangeLatch = new CountDownLatch(2);

    @Test
    public void testEditorTextLoadedAndClickSingle() throws Exception {
        CoreEvents.registerEntryEventListener(new EditorEntryListener(selectedEntries, initialLoadLatch,
                selectionChangeLatch));
        openSampleProject(PROJECT_PATH);
        assertTrue("Editor show first entry.", initialLoadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        verifyInitialTextSelection();
        Point clickPoint = calculateTargetPoint();
        assertNotNull(window);
        JTextComponent editPane = window.panel(EDITOR_TITLE).textBox().target();
        //
        Preferences.setPreference(Preferences.SINGLE_CLICK_SEGMENT_ACTIVATION, true);
        robot().click(editPane, clickPoint);
        //
        assertTrue("Editor select clicked entry", selectionChangeLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        SourceTextEntry newEntry = selectedEntries.get(selectedEntries.size() - 1);
        assertEquals(TARGET_TEXT, newEntry.getSrcText());
    }

    private void verifyInitialTextSelection() {
        SourceTextEntry entry = selectedEntries.get(selectedEntries.size() - 1);
        assertEquals(INITIAL_TEXT, entry.getSrcText());
    }

    private Point calculateTargetPoint() throws BadLocationException {
        assertNotNull(window);
        String fullText = window.panel(EDITOR_TITLE).textBox().text();
        if (fullText == null || !fullText.contains(TARGET_TEXT)) {
            throw new IllegalStateException("Target text not found.");
        }
        int newCaretPos = fullText.indexOf(TARGET_TEXT);
        JTextComponent editPane = window.panel(EDITOR_TITLE).textBox().target();
        Rectangle rect = editPane.modelToView2D(newCaretPos).getBounds();
        // Center of rectangle
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }

    private static class EditorEntryListener implements IEntryEventListener {
        private final List<SourceTextEntry> selectedEntries;
        private final CountDownLatch initialLoadLatch;
        private final CountDownLatch selectionChangeLatch;

        EditorEntryListener(List<SourceTextEntry> selectedEntries, CountDownLatch initialLoadLatch, CountDownLatch selectionChangeLatch) {
            this.selectedEntries = selectedEntries;
            this.initialLoadLatch = initialLoadLatch;
            this.selectionChangeLatch = selectionChangeLatch;
        }

        @Override
        public void onNewFile(String activeFileName) {
            // ignore the event
        }

        @Override
        public void onEntryActivated(SourceTextEntry newEntry) {
            if (newEntry == null) {
                return;
            }
            selectedEntries.add(newEntry);
            initialLoadLatch.countDown();
            selectionChangeLatch.countDown();
        }
    }
}
