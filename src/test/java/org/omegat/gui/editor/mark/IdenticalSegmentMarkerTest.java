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

public class IdenticalSegmentMarkerTest extends MarkerTestBase {

    @Before
    public void preUp() throws Exception {
        TestCoreInitializer.initEditor(editor);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        Core.setSegmenter(segmenter);
        Core.setProject(new MarkTestProject(
                Paths.get("src/test/resources/data/autotmx/auto1.tmx").toFile(), segmenter));
    }

    private SourceTextEntry ste(String sourceText) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        return new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
    }

    @Test
    public void testDisabledReturnsNothing() throws Exception {
        IMarker marker = new IdenticalSegmentMarker();
        Core.getEditor().getSettings().setMarkIdentical(false);
        assertNull(marker.getMarksForEntry(ste("OmegaT"), "OmegaT", "OmegaT", true));
    }

    @Test
    public void testIdenticalTranslationIsMarked() throws Exception {
        IMarker marker = new IdenticalSegmentMarker();
        Core.getEditor().getSettings().setMarkIdentical(true);
        List<Mark> result = marker.getMarksForEntry(ste("OmegaT"), "OmegaT", "OmegaT", true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).startOffset);
        assertEquals("OmegaT".length(), result.get(0).endOffset);
        assertEquals("TRANSLATION", result.get(0).entryPart.toString());
    }

    @Test
    public void testDifferentTranslationIsNotMarked() throws Exception {
        IMarker marker = new IdenticalSegmentMarker();
        Core.getEditor().getSettings().setMarkIdentical(true);
        // auto1.tmx translates "Edit" as "Edition", not identical to the source
        assertNull(marker.getMarksForEntry(ste("Edit"), "Edit", "Edition", true));
    }

    @Test
    public void testUntranslatedEchoIsNotMarked() throws Exception {
        IMarker marker = new IdenticalSegmentMarker();
        Core.getEditor().getSettings().setMarkIdentical(true);
        // An untranslated segment may echo the source in the target area, but
        // it has no stored translation and must not be marked.
        assertNull(marker.getMarksForEntry(ste("Not in the memory"), "Not in the memory",
                "Not in the memory", true));
    }
}
