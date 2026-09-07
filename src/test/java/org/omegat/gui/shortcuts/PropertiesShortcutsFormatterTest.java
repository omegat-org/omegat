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

package org.omegat.gui.shortcuts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.junit.Test;

/**
 * Proves that {@link PropertiesShortcuts#toPropertyValue} yields the
 * properties-file syntax: every produced value must parse back to the same
 * keystroke, for every bundled shortcut and for the modifier matrix.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class PropertiesShortcutsFormatterTest {

    @Test
    public void testEveryBundledShortcutRoundtrips() throws Exception {
        for (String file : new String[] { "MainMenuShortcuts.properties",
                "MainMenuShortcuts.mac.properties", "EditorShortcuts.properties",
                "EditorShortcuts.mac.properties" }) {
            Properties props = new Properties();
            try (InputStream in = PropertiesShortcuts.class
                    .getResourceAsStream("/org/omegat/gui/main/" + file)) {
                assertNotNull(file, in);
                props.load(in);
            }
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                if (value.isEmpty()) {
                    continue;
                }
                KeyStroke ks = KeyStroke.getKeyStroke(value);
                assertNotNull(file + ": unparseable bundled value " + key + "=" + value, ks);
                assertEquals(file + ": " + key + " must roundtrip", ks,
                        KeyStroke.getKeyStroke(PropertiesShortcuts.toPropertyValue(ks)));
            }
        }
    }

    @Test
    public void testModifierMatrixRoundtrips() {
        int[] modifiers = { 0, InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
                InputEvent.META_DOWN_MASK, InputEvent.ALT_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK };
        int[] keys = { KeyEvent.VK_A, KeyEvent.VK_F5, KeyEvent.VK_TAB, KeyEvent.VK_ENTER,
                KeyEvent.VK_UP, KeyEvent.VK_DELETE, KeyEvent.VK_CONTEXT_MENU };
        for (int mod : modifiers) {
            for (int key : keys) {
                KeyStroke ks = KeyStroke.getKeyStroke(key, mod);
                assertEquals(ks, KeyStroke.getKeyStroke(PropertiesShortcuts.toPropertyValue(ks)));
            }
        }
        KeyStroke typed = KeyStroke.getKeyStroke("typed +");
        assertEquals(typed, KeyStroke.getKeyStroke(PropertiesShortcuts.toPropertyValue(typed)));
        assertEquals("null must format as the explicit-unbind value", "",
                PropertiesShortcuts.toPropertyValue(null));
    }
}
