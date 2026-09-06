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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.edt.GuiActionRunner;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * Acceptance test of the "Register Identical Translation" feature.
 * <p>
 * The feature stores the source text of the active segment as its translation.
 * See BUGS#1334: it was broken and behaved as "Register Untranslated".
 */
public class EditorIdenticalTranslationTest extends EditorTestBase {

    /** Segment which is activated when the sample project is loaded. */
    private static final String INITIAL_TEXT = "Error {0}: {1}";
    /** Segment which has no translation in the sample project. */
    private static final String UNTRANSLATED_TEXT = "API key (optional)";

    private final List<SourceTextEntry> selectedEntries = new ArrayList<>();
    private final CountDownLatch initialLoadLatch = new CountDownLatch(1);

    private @Nullable TestingEditorEntryListener entryEventListener;

    @Override
    protected void onTearDown() throws Exception {
        if (entryEventListener != null) {
            CoreEvents.unregisterEntryEventListener(entryEventListener);
            entryEventListener = null;
        }
        super.onTearDown();
    }

    /**
     * Register an identical translation on the segment which is activated when
     * the project is loaded. The segment is already translated identically in
     * the sample project, so the translation should be kept as is.
     */
    @Test
    public void testRegisterIdenticalTranslationOnInitialEntry() throws Exception {
        registerEntryEventListener(initialLoadLatch);
        openSampleProject(PROJECT_PATH);
        assertTrue("Editor show first entry.", initialLoadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        robot().waitForIdle();
        SourceTextEntry entry = lastActivatedEntry();
        assertEquals(INITIAL_TEXT, entry.getSrcText());
        assertTrue("Sample project should have a translation for the first entry.",
                Core.getProject().getTranslationInfo(entry).isTranslated());
        //
        registerIdenticalTranslation();
        //
        TMXEntry translation = Core.getProject().getTranslationInfo(entry);
        assertTrue("Segment should be kept translated.", translation.isTranslated());
        assertEquals("Translation should be identical to the source text.", entry.getSrcText(),
                translation.translation);
        assertTrue("Translation should be a default translation.", translation.defaultTranslation);
    }

    /**
     * Register an identical translation on an untranslated segment: the
     * segment becomes translated with its source text.
     */
    @Test
    public void testRegisterIdenticalTranslationOnUntranslatedEntry() throws Exception {
        registerEntryEventListener(initialLoadLatch);
        openSampleProject(PROJECT_PATH);
        assertTrue("Editor show first entry.", initialLoadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        //
        SourceTextEntry entry = gotoSegment(UNTRANSLATED_TEXT);
        //
        assertEquals("Editor should activate the requested entry.", entry,
                GuiActionRunner.execute(() -> Core.getEditor().getCurrentEntry()));
        assertFalse("Selected segment should be untranslated.",
                Core.getProject().getTranslationInfo(entry).isTranslated());
        //
        registerIdenticalTranslation();
        //
        TMXEntry translation = Core.getProject().getTranslationInfo(entry);
        assertTrue("Segment should become translated.", translation.isTranslated());
        assertEquals("Translation should be identical to the source text.", entry.getSrcText(),
                translation.translation);
        assertEquals(entry.getSrcText(), translation.source);
        assertTrue("Translation should be a default translation.", translation.defaultTranslation);
        assertNull("Translation should not be linked to an external TM.", translation.linked);
        // the editor shows the registered translation.
        assertEquals(entry.getSrcText(),
                GuiActionRunner.execute(() -> Core.getEditor().getCurrentTranslation()));
    }

    private void registerEntryEventListener(CountDownLatch... latches) {
        entryEventListener = new TestingEditorEntryListener(selectedEntries, latches);
        CoreEvents.registerEntryEventListener(entryEventListener);
    }

    private SourceTextEntry lastActivatedEntry() {
        assertFalse("Editor should activate an entry.", selectedEntries.isEmpty());
        return selectedEntries.getLast();
    }
}
