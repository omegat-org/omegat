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
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.util.StaticUtils;

/**
 * Guards the shortcut catalog: the bundled shortcut files list every
 * shortcutable function (menu items become shortcutable through their field
 * name, see BaseMainWindowMenu#setActionCommands), and each platform pair
 * carries identical keysets - on macOS the mac file fully replaces the other
 * one, so a key missing there would silently lose its function.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ShortcutsCatalogTest {

    /** Keys bound outside the menu bar, documented in the manual. */
    private static final Set<String> NON_MENU_KEYS = Set.of("findInProjectReuseLastWindow",
            "jumpToEntryInEditor");

    @Test
    public void testPlatformPairsCarryIdenticalKeysets() throws Exception {
        assertEquals(keys("MainMenuShortcuts.properties"), keys("MainMenuShortcuts.mac.properties"));
        assertEquals(keys("EditorShortcuts.properties"), keys("EditorShortcuts.mac.properties"));
    }

    @Test
    public void testEveryMenuItemFieldIsCatalogued() throws Exception {
        Set<String> catalog = keys("MainMenuShortcuts.properties");
        Set<String> expected = new TreeSet<>(NON_MENU_KEYS);
        for (Field f : StaticUtils.getAllModelFields(BaseMainWindowMenu.class)) {
            // Menus themselves take no accelerator, see
            // PropertiesShortcuts#bindKeyStrokes(JMenuItem).
            if (JMenuItem.class.isAssignableFrom(f.getType())
                    && !JMenu.class.isAssignableFrom(f.getType())) {
                expected.add(f.getName());
            }
        }
        assertEquals("the main menu catalog must list exactly the menu item fields "
                + "plus the documented non-menu keys", expected, new TreeSet<>(catalog));
    }

    @Test
    public void testCatalogFunctionsAreDistinctFromStructureComments() throws Exception {
        // The structure markers must stay comments; a materialized marker
        // would surface as a bogus function in the shortcut table.
        for (String key : keys("MainMenuShortcuts.properties")) {
            assertTrue("bogus key: " + key, key.matches("[A-Za-z0-9]+"));
        }
        for (String key : keys("EditorShortcuts.properties")) {
            assertTrue("bogus key: " + key, key.matches("[A-Za-z0-9]+"));
        }
    }

    private static Set<String> keys(String file) throws Exception {
        Properties props = new Properties();
        try (InputStream in = PropertiesShortcuts.class
                .getResourceAsStream("/org/omegat/gui/main/" + file)) {
            assertNotNull(file, in);
            props.load(in);
        }
        return new TreeSet<>(props.stringPropertyNames());
    }
}
