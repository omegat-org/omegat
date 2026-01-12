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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the exportCurrentSegment method of SegmentExportImport. This method
 * exports the source text and its corresponding translation (if available) into
 * respective files and tracks the last modified time of these files.
 */
public class SegmentExportImportTest extends TestCore {

    @Before
    public void setUp() throws Exception {
        Core.setSegmenter(new Segmenter(Preferences.getSRX()));
        Core.setProject(new TestingProject(new TestingProjectProperties()));
    }

    @Test
    public void testSegmentExportCurrentSegment() throws Exception {
        // preparation
        SegmentExportImport segmentExportImport = new SegmentExportImport(Core.getEditor());
        SourceTextEntry ste = createSTE("1", "source");

        // run
        segmentExportImport.exportCurrentSegment(ste);

        // assertion
        File source = new File(StaticUtils.getScriptDir(), "source.txt");
        File target = new File(StaticUtils.getScriptDir(), "target.txt");
        assertTrue(source.exists());
        assertTrue(target.exists());

        String sourceContent = FileUtils.readFileToString(source, StandardCharsets.UTF_8);
        String targetContent = FileUtils.readFileToString(target, StandardCharsets.UTF_8);
        assertEquals("source", sourceContent);
        assertEquals("target", targetContent);
    }

    @Test
    public void testFlushExportedSegments() throws Exception {
        // preparation
        SegmentExportImport segmentExportImport = new SegmentExportImport(Core.getEditor());
        SourceTextEntry ste = createSTE("1", "source");
        segmentExportImport.exportCurrentSegment(ste);

        // files with content
        File source = new File(StaticUtils.getScriptDir(), "source.txt");
        File target = new File(StaticUtils.getScriptDir(), "target.txt");
        assertTrue(source.exists());
        assertTrue(target.exists());
        String sourceContent = FileUtils.readFileToString(source, StandardCharsets.UTF_8);
        String targetContent = FileUtils.readFileToString(target, StandardCharsets.UTF_8);
        assertEquals("source", sourceContent);
        assertEquals("target", targetContent);

        // run flushExportedSegments
        SegmentExportImport.flushExportedSegments();

        // read contents
        sourceContent = FileUtils.readFileToString(source, StandardCharsets.UTF_8);
        targetContent = FileUtils.readFileToString(target, StandardCharsets.UTF_8);

        // assertion: files should be empty
        assertTrue(sourceContent.isEmpty());
        assertTrue(targetContent.isEmpty());
    }

    @Test
    public void testExportCurrentSelection() throws IOException {
        // prepare
        String selection = "test";
        File selectionExport = new File(StaticUtils.getScriptDir(), "selection.txt");
        // run
        SegmentExportImport.exportCurrentSelection(selection);
        // assertion
        assertTrue(selectionExport.exists());
        String content = FileUtils.readFileToString(selectionExport, StandardCharsets.UTF_8);
        assertEquals(selection, content);
    }

    private SourceTextEntry createSTE(String id, String source) {
        EntryKey ek = new EntryKey("file", source, id, null, null, null);
        return new SourceTextEntry(ek, 0, null, null, new ArrayList<>());
    }
}
