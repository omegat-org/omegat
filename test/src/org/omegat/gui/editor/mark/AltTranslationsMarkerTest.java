/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Lev Abashkin
               2024 Hiroshi Miura
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

public class AltTranslationsMarkerTest extends MarkerTestBase {

    @Before
    public void preUp() throws Exception {
        TestCoreInitializer.initEditor(editor);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        Core.setSegmenter(segmenter);
        Core.setProject(new MarkTestProject(Paths.get("test/data/mark/alternative.tmx").toFile(), segmenter));
    }

    @Test
    public void testAltTranslationsMarker() throws Exception {
        IMarker marker = new AltTranslationsMarker();
        Core.getEditor().getSettings().setMarkAltTranslations(true);
        String sourceText = "Edit";
        String translationText = "default";
        // default entry: file and
        EntryKey key0 = new EntryKey("file0", sourceText, "1_0", "prev0", "next0", "path");
        SourceTextEntry ste0 = new SourceTextEntry(key0, 1, new String[0], sourceText,
                Collections.emptyList());
        List<Mark> result = marker.getMarksForEntry(ste0, sourceText, translationText, true);
        assertNull(result);
        // alternative entry: file and 1_0
        translationText = "alternative";
        EntryKey key1 = new EntryKey("file1", sourceText, "1_1", "prev1", "next1", null);
        SourceTextEntry ste1 = new SourceTextEntry(key1, 1, new String[0], sourceText,
                Collections.emptyList());
        result = marker.getMarksForEntry(ste1, sourceText, translationText, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        // TODO: further checks
    }
}
