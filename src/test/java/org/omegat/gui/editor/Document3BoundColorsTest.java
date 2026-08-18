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
import static org.junit.Assume.assumeFalse;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * The editor document resolves bound span attributes against the palette
 * currently in effect, so document-level consumers see live colours just
 * like the label views do.
 *
 * @author Stephan Pakebusch
 */
public class Document3BoundColorsTest extends TestCore {

    private EditorController editorController;

    @BeforeClass
    public static void setUpBeforeClass() {
        assumeFalse("Skipping test: headless environment", GraphicsEnvironment.isHeadless());
    }

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }

    @After
    public void tearDown() {
        EditorColor.COLOR_UNTRANSLATED.setColor(null);
    }

    @Test
    public void testDocumentResolvesBoundAttributesLive() {
        Document3 doc = new Document3(editorController);
        AttributeSet attrs = Styles.createBoundAttributeSet(EditorColor.COLOR_UNTRANSLATED_FG,
                EditorColor.COLOR_UNTRANSLATED, false, false);

        assertEquals(EditorColor.COLOR_UNTRANSLATED.getColor(), doc.getBackground(attrs));

        Color changed = new Color(0x22, 0x44, 0x66);
        EditorColor.COLOR_UNTRANSLATED.setColor(changed);
        assertEquals(changed, doc.getBackground(attrs));
        assertEquals(EditorColor.COLOR_UNTRANSLATED_FG.getColor(), doc.getForeground(attrs));
    }

    @Test
    public void testDefaultStyleFollowsTheEditorWideColors() {
        Document3 doc = new Document3(editorController);
        assertEquals(EditorColor.COLOR_FOREGROUND.getColor(),
                StyleConstants.getForeground(doc.getStyle(javax.swing.text.StyleContext.DEFAULT_STYLE)));
        assertEquals(EditorColor.COLOR_BACKGROUND.getColor(),
                StyleConstants.getBackground(doc.getStyle(javax.swing.text.StyleContext.DEFAULT_STYLE)));
    }
}
