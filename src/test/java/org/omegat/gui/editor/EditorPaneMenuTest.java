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
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.junit.Before;
import org.junit.Test;

import org.omegat.util.OStrings;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for the editor pane settings menu.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorPaneMenuTest {

    private AtomicInteger refreshes;
    private JPopupMenu popup;

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        refreshes = new AtomicInteger();
        popup = new JPopupMenu();
        new EditorPaneMenu(refreshes::incrementAndGet, column -> 60, () -> 120, () -> 14)
                .populatePaneMenu(popup);
    }

    @Test
    public void testOffersThePreferencesShortcut() {
        // the configuration dialog, a separator, the preferences shortcut
        assertEquals(3, popup.getComponentCount());
        assertTrue(popup.getComponent(1) instanceof JSeparator);
        JMenuItem prefs = (JMenuItem) popup.getComponent(2);
        assertEquals(OStrings.getString("GUI_EDITORWINDOW_OPEN_PREFS"), prefs.getText());
        assertTrue(prefs.isEnabled());
        assertEquals(1, prefs.getActionListeners().length);

        JMenuItem configure = (JMenuItem) popup.getComponent(0);
        assertEquals(OStrings.getString("GUI_EDITORWINDOW_GUTTER_CONFIGURE"), configure.getText());
        assertEquals(1, configure.getActionListeners().length);
    }
}
