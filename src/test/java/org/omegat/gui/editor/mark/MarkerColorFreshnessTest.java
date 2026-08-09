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

import java.awt.Color;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.gui.Styles;

/**
 * Checks that markers pick up color preference changes without requiring an
 * application restart, i.e. that painters are not cached at marker
 * construction time.
 */
public class MarkerColorFreshnessTest extends MarkerTestBase {

    private Styles.EditorColor changedColor;

    @Before
    public void preUp() throws Exception {
        TestCoreInitializer.initEditor(editor);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        Core.setSegmenter(segmenter);
        Core.setProject(new MarkTestProject(
                Paths.get("src/test/resources/data/autotmx/auto1.tmx").toFile(), segmenter));
    }

    @After
    public void tearDown() {
        if (changedColor != null) {
            // restore the default so other tests see pristine colors
            changedColor.setColor(null);
        }
    }

    @Test
    public void testPainterFollowsColorPreferenceChange() throws Exception {
        IMarker marker = new ComesFromAutoTMMarker();
        Core.getEditor().getSettings().setMarkAutoPopulated(true);

        String sourceText = "Edit";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText,
                Collections.emptyList());
        TMXEntry entry = Core.getProject().getTranslationInfo(ste);
        assertNotNull(entry.linked);
        switch (entry.linked) {
        case xICE:
            changedColor = Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XICE;
            break;
        case x100PC:
            changedColor = Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_X100PC;
            break;
        case xAUTO:
            changedColor = Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO;
            break;
        case xENFORCED:
        default:
            changedColor = Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XENFORCED;
            break;
        }

        List<Mark> before = marker.getMarksForEntry(ste, sourceText, "target", true);
        assertNotNull(before);
        assertEquals(changedColor.getColor(),
                ((TransparentHighlightPainter) before.get(0).painter).getColor());

        Color newColor = new Color(0x12, 0x34, 0x56);
        changedColor.setColor(newColor);

        List<Mark> after = marker.getMarksForEntry(ste, sourceText, "target", true);
        assertNotNull(after);
        assertEquals(newColor, ((TransparentHighlightPainter) after.get(0).painter).getColor());
    }
}
