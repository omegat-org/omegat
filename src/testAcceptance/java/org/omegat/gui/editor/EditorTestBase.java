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

import static org.junit.Assert.assertNotNull;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

/**
 * Common utilities for the editor acceptance tests which load the sample
 * project.
 */
abstract class EditorTestBase extends TestCoreGUI {

    protected static final Path PROJECT_PATH = Paths.get("src/testAcceptance/resources/data/project/");

    protected static final int TIMEOUT_SECONDS = 15;
    protected static final String EDITOR_TITLE = "Editor - Bundle.properties";

    /**
     * Return the text component of the editor pane.
     */
    protected JTextComponent editorPane() {
        assertNotNull(window);
        return window.panel(EDITOR_TITLE).textBox().target();
    }

    /**
     * Calculate a point in the editor pane where the given text is displayed.
     *
     * @param targetText
     *            text to look for in the editor pane.
     * @return the center of the rectangle which shows the first character of
     *         the target text.
     * @throws BadLocationException
     *             when the position is out of the document.
     */
    protected Point calculateTargetPoint(String targetText) throws BadLocationException {
        assertNotNull(window);
        String fullText = window.panel(EDITOR_TITLE).textBox().text();
        if (fullText == null || !fullText.contains(targetText)) {
            throw new IllegalStateException("Target text not found.");
        }
        int newCaretPos = fullText.indexOf(targetText);
        Rectangle rect = editorPane().modelToView2D(newCaretPos).getBounds();
        // Center of rectangle
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }

    /**
     * Activate the segment which shows the given text by a single click.
     *
     * @param targetText
     *            text of the segment to activate.
     * @throws BadLocationException
     *             when the position is out of the document.
     */
    protected void clickSegment(String targetText) throws BadLocationException {
        Point clickPoint = calculateTargetPoint(targetText);
        Preferences.setPreference(Preferences.SINGLE_CLICK_SEGMENT_ACTIVATION, true);
        robot().click(editorPane(), clickPoint);
    }

    /**
     * Activate the segment which has the given source text.
     * <p>
     * Unlike {@link #clickSegment(String)}, it does not depend on the position
     * where the segment is drawn.
     *
     * @param srcText
     *            source text of the segment to activate.
     * @return the activated entry.
     */
    protected SourceTextEntry gotoSegment(String srcText) throws Exception {
        SourceTextEntry entry = Core.getProject().getAllEntries().stream()
                .filter(ste -> srcText.equals(ste.getSrcText())).findFirst()
                .orElseThrow(() -> new IllegalStateException("Segment not found: " + srcText));
        SwingUtilities.invokeAndWait(() -> Core.getEditor().gotoEntry(entry.entryNum()));
        robot().waitForIdle();
        return entry;
    }

    /**
     * Run the "Register Identical Translation" action on the Swing thread and
     * wait until the editor becomes idle.
     */
    protected void registerIdenticalTranslation() throws Exception {
        SwingUtilities.invokeAndWait(() -> Core.getEditor().registerIdenticalTranslation());
        robot().waitForIdle();
    }
}
