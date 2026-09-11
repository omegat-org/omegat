/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025-2026 Hiroshi Miura
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

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jspecify.annotations.Nullable;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;

/**
 * Records the entries the editor activates and releases the given latches.
 * <p>
 * Every activation is appended to {@code selectedEntries} and counts down all
 * the given latches, so a test can wait for the n-th activation by passing a
 * latch initialized with n.
 */
class TestingEditorEntryListener implements IEntryEventListener {

    private final List<SourceTextEntry> selectedEntries;
    private final List<CountDownLatch> latches;

    TestingEditorEntryListener(List<SourceTextEntry> selectedEntries, CountDownLatch... latches) {
        this.selectedEntries = selectedEntries;
        this.latches = List.of(latches);
    }

    @Override
    public void onNewFile(String activeFileName) {
        // ignore the event
    }

    @Override
    public void onEntryActivated(@Nullable SourceTextEntry newEntry) {
        if (newEntry == null) {
            return;
        }
        selectedEntries.add(newEntry);
        for (CountDownLatch latch : latches) {
            latch.countDown();
        }
    }
}
