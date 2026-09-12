/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Yu Tang
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.util.StaticUtils;

/**
 *
 * @author Yu Tang
 */
public class PropertiesShortcutsTest {
    private static final String TEST_SAVE = "TEST_SAVE";
    private static final String TEST_CUT = "TEST_CUT";
    private static final String TEST_DELETE = "TEST_DELETE"; // overrided with user properties
    private static final String TEST_USER_1 = "TEST_USER_1"; // added from user properties
    private static final String OUT_OF_LIST = "OUT_OF_LIST";
    private static final KeyStroke CTRL_S = KeyStroke.getKeyStroke("ctrl S");
    private static final KeyStroke CTRL_X = KeyStroke.getKeyStroke("ctrl X");
    private static final KeyStroke CTRL_D = KeyStroke.getKeyStroke("ctrl D");
    private static final KeyStroke CTRL_P = KeyStroke.getKeyStroke("ctrl P");
    private static final String USER_FILE_NAME = "test.properties";
    private static final String BUNDLED_ROOT = "/org/omegat/gui/shortcuts/";

    private static File userFile;

    private PropertiesShortcuts shortcuts;

    @BeforeClass
    public static void setUpClass() throws IOException {
        // Copy user-defined properties to user config dir
        userFile = new File(StaticUtils.getConfigDir(), USER_FILE_NAME);
        try (InputStream in = PropertiesShortcutsTest.class.getResourceAsStream("test.user.properties")) {
            FileUtils.copyInputStreamToFile(in, userFile);
        }
        assertTrue(userFile.isFile());
    }

    @AfterClass
    public static void tearDownClass() {
        // Delete user-defined properties
        assertTrue(userFile.delete());
    }

    @Before
    public final void setUp() throws IOException {
        shortcuts = new PropertiesShortcuts();
        assertTrue(shortcuts.isEmpty());
        shortcuts.loadFromClasspath(BUNDLED_ROOT + USER_FILE_NAME);
        shortcuts.loadFromFile(userFile);
        assertFalse(shortcuts.isEmpty());
    }

    @After
    public final void tearDown() {
        shortcuts = null;
    }

    /**
     * Test of getKeyStroke method, of class PropertiesShortcuts.
     */
    @Test
    public void testGetKeyStroke() {
        assertEquals(CTRL_S, shortcuts.getKeyStroke(TEST_SAVE));

        assertEquals(CTRL_X, shortcuts.getKeyStroke(TEST_CUT));

        assertNull(shortcuts.getKeyStroke(TEST_DELETE));

        assertEquals(CTRL_P, shortcuts.getKeyStroke(TEST_USER_1));

        try {
            shortcuts.getKeyStroke(OUT_OF_LIST);
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokesJMenuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu parent = new JMenu();
        JMenuItem child1 = new JMenu();
        JMenuItem child2 = new JMenuItem();
        child2.setActionCommand(TEST_DELETE);
        child2.setAccelerator(CTRL_D);
        JMenuItem grandchild1 = new JMenuItem();
        grandchild1.setActionCommand(TEST_USER_1);
        JMenuItem grandchild2 = new JMenuItem();
        grandchild2.setActionCommand(OUT_OF_LIST);
        grandchild2.setAccelerator(CTRL_X);
        menu.add(parent);
        parent.add(child1);
        parent.add(child2);
        child1.add(grandchild1);
        child1.add(grandchild2);

        // bind
        shortcuts.bindKeyStrokes(menu);

        assertNull(parent.getAccelerator());

        assertNull(child1.getAccelerator());

        assertNull(child2.getAccelerator());

        assertEquals(CTRL_P, grandchild1.getAccelerator());

        assertEquals(CTRL_X, grandchild2.getAccelerator());
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokesJMenuItem() {
        // case JMenuItem with no children
        JMenuItem item = new JMenuItem();
        item.setActionCommand(TEST_SAVE);
        assertNull(item.getAccelerator()); // before binding
        shortcuts.bindKeyStrokes(item); // bind
        assertEquals(CTRL_S, item.getAccelerator()); // after binding(1)

        item.setActionCommand(TEST_DELETE);
        shortcuts.bindKeyStrokes(item); // bind
        assertNull(item.getAccelerator()); // after binding(2)

        item.setActionCommand(OUT_OF_LIST);
        item.setAccelerator(CTRL_D);
        shortcuts.bindKeyStrokes(item); // bind
        assertEquals(CTRL_D, item.getAccelerator()); // after binding(3) - nothing has changed
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokesJMenuItemRecursive() {
        // case JMenu with children
        JMenu parent = new JMenu();
        JMenuItem child1 = new JMenu();
        JMenuItem child2 = new JMenuItem();
        child2.setActionCommand(TEST_DELETE);
        child2.setAccelerator(CTRL_D);
        JMenuItem grandchild1 = new JMenuItem();
        grandchild1.setActionCommand(TEST_USER_1);
        JMenuItem grandchild2 = new JMenuItem();
        grandchild2.setActionCommand(OUT_OF_LIST);
        grandchild2.setAccelerator(CTRL_X);
        parent.add(child1);
        parent.add(child2);
        child1.add(grandchild1);
        child1.add(grandchild2);

        // bind
        shortcuts.bindKeyStrokes(parent);

        assertNull(parent.getAccelerator());

        assertNull(child1.getAccelerator());

        assertNull(child2.getAccelerator());

        assertEquals(CTRL_P, grandchild1.getAccelerator());

        assertEquals(CTRL_X, grandchild2.getAccelerator());
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokesInputMapObjectArr() {
        // bind
        InputMap inputMap = new InputMap();
        shortcuts.bindKeyStrokes(inputMap, TEST_SAVE, TEST_CUT, TEST_USER_1);

        // test map size
        assertEquals(3, inputMap.size());

        // test keys
        KeyStroke[] expResults = new KeyStroke[] { CTRL_S, CTRL_X, CTRL_P };
        KeyStroke[] results = inputMap.keys();
        assertArrayEquals(expResults, results);

        // test entry1 exists
        assertEquals(TEST_SAVE, inputMap.get(CTRL_S));

        // test entry2 exists
        assertEquals(TEST_CUT, inputMap.get(CTRL_X));

        // test entry3 exists
        assertEquals(TEST_USER_1, inputMap.get(CTRL_P));

        // test remove entry with null shortcut
        inputMap.put(CTRL_D, TEST_DELETE); // put target
        assertEquals(TEST_DELETE, inputMap.get(CTRL_D)); // target exists before remove
        shortcuts.bindKeyStrokes(inputMap, TEST_DELETE); // key to be removed as null
        assertNull(inputMap.get(CTRL_D)); // target will be null after removed

        // test map size again
        assertEquals(3, inputMap.size());

        // ensure no affect for entry1 after removing
        assertEquals(TEST_SAVE, inputMap.get(CTRL_S));

        // ensure no affect for entry2 after removing
        assertEquals(TEST_CUT, inputMap.get(CTRL_X));

        // ensure no affect for entry3 after removing
        assertEquals(TEST_USER_1, inputMap.get(CTRL_P));
    }

    @Test
    public void testLoadBundled() {
        PropertiesShortcuts props = PropertiesShortcuts.loadBundled(BUNDLED_ROOT, USER_FILE_NAME);
        assertEquals(shortcuts.getData(), props.getData());
    }

    @Test
    public void testCatalogAndModificationState() {
        assertTrue(shortcuts.getKeys().containsAll(
                java.util.List.of(TEST_SAVE, TEST_CUT, TEST_DELETE, TEST_USER_1)));
        assertFalse(shortcuts.getKeys().contains(OUT_OF_LIST));

        // TEST_DELETE is overridden to empty by the user file, TEST_SAVE is
        // at its bundled default
        assertTrue(shortcuts.isModified(TEST_DELETE));
        assertEquals("", shortcuts.getShortcutValue(TEST_DELETE));
        assertFalse(shortcuts.isModified(TEST_SAVE));
        assertEquals(shortcuts.getDefaultValue(TEST_SAVE), shortcuts.getShortcutValue(TEST_SAVE));

        // setting a shortcut back to its default removes the override, even
        // though the canonical format differs from the file syntax
        shortcuts.setShortcut(TEST_DELETE, CTRL_D);
        assertFalse(shortcuts.isModified(TEST_DELETE));
        shortcuts.setShortcut(TEST_SAVE, CTRL_S);
        assertFalse(shortcuts.isModified(TEST_SAVE));
        shortcuts.setShortcut(TEST_SAVE, CTRL_P);
        assertTrue(shortcuts.isModified(TEST_SAVE));
        assertEquals(CTRL_P, shortcuts.getKeyStroke(TEST_SAVE));
        shortcuts.clearUserOverride(TEST_SAVE);
        assertFalse(shortcuts.isModified(TEST_SAVE));
        assertEquals(CTRL_S, shortcuts.getKeyStroke(TEST_SAVE));

        // null unbinds explicitly
        shortcuts.setShortcut(TEST_CUT, null);
        assertNull(shortcuts.getKeyStroke(TEST_CUT));
        assertEquals("", shortcuts.getShortcutValue(TEST_CUT));
    }

    @Test
    public void testSaveWritesOnlyOverridesAndReloadRestores() throws Exception {
        PropertiesShortcuts props = PropertiesShortcuts.loadBundled(BUNDLED_ROOT, USER_FILE_NAME);
        // The save target is the platform-specific user file, which on macOS
        // shadows the plain one on the next load.
        File savedFile = new File(StaticUtils.getConfigDir(),
                org.omegat.util.Platform.isMacOSX() ? "test.mac.properties" : USER_FILE_NAME);
        boolean[] notified = { false };
        Runnable listener = () -> notified[0] = true;
        props.addChangeListener(listener);
        try {
            props.setShortcut(TEST_SAVE, CTRL_P);
            props.save();
            // listeners are notified on the EDT
            javax.swing.SwingUtilities.invokeAndWait(() -> {
            });
            assertTrue("save must notify the change listeners", notified[0]);

            java.util.Properties written = new java.util.Properties();
            try (InputStream in = new java.io.FileInputStream(savedFile)) {
                written.load(in);
            }
            assertEquals("only the overrides may be written",
                    java.util.Set.of(TEST_SAVE, TEST_DELETE, TEST_USER_1, "TEST_USER_2"),
                    written.stringPropertyNames());
            assertEquals(PropertiesShortcuts.toPropertyValue(CTRL_P), written.getProperty(TEST_SAVE));

            // a fresh load sees the saved override; clearing and saving again
            // removes it from the file
            PropertiesShortcuts reloaded = PropertiesShortcuts.loadBundled(BUNDLED_ROOT, USER_FILE_NAME);
            assertEquals(CTRL_P, reloaded.getKeyStroke(TEST_SAVE));

            props.setShortcut(TEST_CUT, null);
            assertNull(props.getKeyStroke(TEST_CUT));
            props.reload();
            assertEquals("reload must discard unsaved session changes", CTRL_X,
                    props.getKeyStroke(TEST_CUT));
            assertEquals("reload must keep saved overrides", CTRL_P, props.getKeyStroke(TEST_SAVE));

            props.removeChangeListener(listener);
            props.clearUserOverride(TEST_SAVE);
            props.save();
            PropertiesShortcuts finalState = PropertiesShortcuts.loadBundled(BUNDLED_ROOT, USER_FILE_NAME);
            assertEquals(CTRL_S, finalState.getKeyStroke(TEST_SAVE));
        } finally {
            // The save target may be the shared fixture file itself (non-mac)
            // - restore it for the other test methods and tearDownClass.
            savedFile.delete();
            try (InputStream in = PropertiesShortcutsTest.class
                    .getResourceAsStream("test.user.properties")) {
                FileUtils.copyInputStreamToFile(in, userFile);
            }
        }
    }
}
