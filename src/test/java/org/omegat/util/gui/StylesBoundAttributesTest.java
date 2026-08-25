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

package org.omegat.util.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * Attribute sets bound to palette entries must resolve the palette at query
 * time, so a colour change becomes visible with a plain repaint instead of a
 * rebuild of the editor document.
 *
 * @author Stephan Pakebusch
 */
public class StylesBoundAttributesTest {

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    @After
    public void tearDown() {
        // reset the palette entries the tests override
        EditorColor.COLOR_UNTRANSLATED.setColor(null);
        EditorColor.COLOR_UNTRANSLATED_FG.setColor(null);
    }

    @Test
    public void testBoundAttributesResolveThePaletteLive() {
        AttributeSet attrs = Styles.createBoundAttributeSet(EditorColor.COLOR_UNTRANSLATED_FG,
                EditorColor.COLOR_UNTRANSLATED, false, false);

        Color bakedBackground = EditorColor.COLOR_UNTRANSLATED.getColor();
        assertEquals(bakedBackground, Styles.resolveBoundBackground(attrs));

        // a palette change resolves through the existing attributes ...
        Color changed = new Color(0x12, 0x34, 0x56);
        EditorColor.COLOR_UNTRANSLATED.setColor(changed);
        assertEquals(changed, Styles.resolveBoundBackground(attrs));

        // ... while the baked snapshot keeps the build-time value; painting
        // must therefore prefer the bound resolution
        assertEquals(bakedBackground, StyleConstants.getBackground(attrs));
    }

    @Test
    public void testBoundAttributesBakeCurrentColorsForPlainConsumers() {
        AttributeSet attrs = Styles.createBoundAttributeSet(EditorColor.COLOR_UNTRANSLATED_FG,
                EditorColor.COLOR_UNTRANSLATED, true, false);
        assertEquals(EditorColor.COLOR_UNTRANSLATED_FG.getColor(), StyleConstants.getForeground(attrs));
        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(), StyleConstants.getBackground(attrs));
        assertTrue(StyleConstants.isBold(attrs));
        assertFalse(StyleConstants.isItalic(attrs));
    }

    @Test
    public void testMergedMarkBindingOverridesTheStateBinding() {
        // Segment text carries a bound state style; a mark merges its own
        // attributes on top without replacing, the way MarkerController
        // applies them. The mark's binding must win for the foreground while
        // the untouched background binding of the state style stays in place.
        MutableAttributeSet text = new SimpleAttributeSet();
        text.addAttributes(Styles.createBoundAttributeSet(EditorColor.COLOR_UNTRANSLATED_FG,
                EditorColor.COLOR_UNTRANSLATED, false, false));
        text.addAttributes(Styles.createBoundAttributeSet(EditorColor.COLOR_PLACEHOLDER, null, null, null));

        assertEquals(EditorColor.COLOR_PLACEHOLDER.getColor(), Styles.resolveBoundForeground(text));
        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(), Styles.resolveBoundBackground(text));
    }

    @Test
    public void testUnboundAttributesCarryNoResolution() {
        AttributeSet attrs = Styles.createAttributeSet(Color.RED, Color.WHITE, null, null);
        assertNull(Styles.resolveBoundForeground(attrs));
        assertNull(Styles.resolveBoundBackground(attrs));
    }

    @Test
    public void testNullEntriesLeaveAttributesUnbound() {
        AttributeSet attrs = Styles.createBoundAttributeSet(null, EditorColor.COLOR_UNTRANSLATED, null,
                null);
        assertNull(Styles.resolveBoundForeground(attrs));
        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(), Styles.resolveBoundBackground(attrs));
    }
}
