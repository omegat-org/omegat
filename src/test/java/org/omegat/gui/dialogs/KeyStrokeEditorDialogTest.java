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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.junit.Test;

/**
 * Proves that the keystroke capture ignores key events without a key code:
 * macOS delivers the fn key as VK_UNDEFINED, which used to display and
 * store as "Unknown keyCode: 0x0".
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class KeyStrokeEditorDialogTest {

    @Test
    public void testCaptureIgnoresEventsWithoutKeyCode() {
        KeyStroke current = KeyStroke.getKeyStroke("ctrl S");
        KeyEvent fnKey = keyEvent(KeyEvent.VK_UNDEFINED, 0);

        assertEquals("an event without key code must keep the current keystroke", current,
                KeyStrokeEditorDialog.capture(fnKey, current));
        assertNull("an event without key code must not invent a keystroke",
                KeyStrokeEditorDialog.capture(fnKey, null));
    }

    @Test
    public void testCaptureTakesRegularKeyEvents() {
        KeyEvent ctrlN = keyEvent(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        assertEquals(KeyStroke.getKeyStroke("ctrl pressed N"),
                KeyStrokeEditorDialog.capture(ctrlN, null));
    }

    private static KeyEvent keyEvent(int keyCode, int modifiers) {
        return new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers,
                keyCode, KeyEvent.CHAR_UNDEFINED);
    }
}
