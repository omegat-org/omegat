/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2026 Stephan Pakebusch.
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
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;

public class ComesFromSourceFileMarkerTest extends MarkerTestBase {

    private Segmenter segmenter;

    @Before
    public void preUp() throws Exception {
        TestCoreInitializer.initEditor(editor);
        segmenter = new Segmenter(SRX.getDefault());
        Core.setSegmenter(segmenter);
        Core.setProject(new MarkTestProject(
                Paths.get("src/test/resources/data/mark/prefilled1.tmx").toFile(), segmenter));
    }

    private SourceTextEntry createEntry(String sourceText, String sourceTranslation) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        return new SourceTextEntry(key, 1, new String[0], sourceTranslation, Collections.emptyList());
    }

    @Test
    public void testMarkerDisabled() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(false);
        SourceTextEntry ste = createEntry("Edit", "Edition");
        assertNull(marker.getMarksForEntry(ste, "Edit", "Edition", true));
    }

    @Test
    public void testMarkerNullEntry() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        assertNull(marker.getMarksForEntry(null, null, null, true));
    }

    @Test
    public void testMarkerPrefilledTranslation() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Edit", "Edition");
        List<Mark> result = marker.getMarksForEntry(ste, "Edit", "Edition", true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).startOffset);
        assertEquals("Edition".length(), result.get(0).endOffset);
        assertEquals(Mark.ENTRY_PART.TRANSLATION, result.get(0).entryPart);
    }

    @Test
    public void testMarkerNoSourceTranslation() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Edit", null);
        assertNull(marker.getMarksForEntry(ste, "Edit", "Edition", true));
    }

    @Test
    public void testMarkerFuzzySourceTranslation() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Edit", "Edition");
        ste.setSourceTranslationFuzzy(true);
        assertNull(marker.getMarksForEntry(ste, "Edit", "Edition", true));
    }

    @Test
    public void testMarkerTranslationChanged() throws Exception {
        // The project translation differs from the pre-filled one: the
        // translator has replaced it, so the mark must disappear.
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Edit", "Modification");
        assertNull(marker.getMarksForEntry(ste, "Edit", "Edition", true));
    }

    @Test
    public void testMarkerUntranslated() throws Exception {
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Delete", "Suppression");
        assertNull(marker.getMarksForEntry(ste, "Delete", "Suppression", true));
    }

    @Test
    public void testMarkerLinkedEntryExcluded() throws Exception {
        // Entries linked to an external TMX (tm/auto etc.) are already
        // colored by ComesFromAutoTMMarker and must not be marked again.
        Core.setProject(new MarkTestProject(
                Paths.get("src/test/resources/data/autotmx/auto1.tmx").toFile(), segmenter));
        IMarker marker = new ComesFromSourceFileMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);
        SourceTextEntry ste = createEntry("Edit",
                Core.getProject().getTranslationInfo(createEntry("Edit", null)).translation);
        assertNull(marker.getMarksForEntry(ste, "Edit", ste.getSourceTranslation(), true));
    }
}
