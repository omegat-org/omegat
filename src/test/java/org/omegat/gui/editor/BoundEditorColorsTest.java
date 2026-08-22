/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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
import static org.junit.Assert.assertNotNull;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * The editor's segment attributes bind their colours to palette entries, and
 * the label views resolve that binding when they paint. Together this makes a
 * colour switch effective with a repaint — the document is not rebuilt, its
 * layout not recomputed — which keeps it instantaneous on large documents and
 * enables flash notifications via background changes.
 *
 * @author Stephan Pakebusch
 */
public class BoundEditorColorsTest {

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS, true);
    }

    @After
    public void tearDown() {
        EditorColor.COLOR_UNTRANSLATED.setColor(null);
        EditorColor.COLOR_ACTIVE_SOURCE.setColor(null);
    }

    /**
     * The attribute chooser hands out the palette entry, not a frozen colour.
     */
    @Test
    public void testSegmentAttributesAreBoundToThePalette() {
        EditorSettings settings = new EditorSettings(null);

        AttributeSet untranslatedSource = settings.getAttributeSet(true, false, false, DUPLICATE.NONE,
                false, false, false, false);
        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(),
                Styles.resolveBoundBackground(untranslatedSource));

        AttributeSet activeSource = settings.getAttributeSet(true, false, false, DUPLICATE.NONE, true,
                false, false, false);
        assertEquals(EditorColor.COLOR_ACTIVE_SOURCE.getColor(),
                Styles.resolveBoundBackground(activeSource));
    }

    /**
     * A view over a bound span sees a palette change immediately — without
     * the span's attributes being touched.
     */
    @Test
    public void testViewLabelResolvesPaletteChangesWithoutReattribution() throws Exception {
        EditorSettings settings = new EditorSettings(null);
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.insertString(0, "untranslated segment text", settings.getAttributeSet(true, false, false,
                DUPLICATE.NONE, false, false, false, false));

        Element span = doc.getCharacterElement(0);
        ViewLabel view = new ViewLabel(span);
        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(), view.getBackground());
        assertNotNull(view.getForeground());

        Color changed = new Color(0x65, 0x43, 0x21);
        EditorColor.COLOR_UNTRANSLATED.setColor(changed);
        assertEquals(changed, view.getBackground());
    }
}
