/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Yu Tang
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.shortcuts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.omegat.util.LFileCopy;
import org.omegat.util.StaticUtils;

/**
 *
 * @author Yu Tang
 */
public class PropertiesShortcutsTest {
    private PropertiesShortcuts shotcuts;
    private final String TEST_SAVE = "TEST_SAVE";
    private final String TEST_CUT = "TEST_CUT";
    private final String TEST_DELETE = "TEST_DELETE"; // overrided with user properties
    private final String TEST_USER_1 = "TEST_USER_1"; // added from user properties
    private final String OUT_OF_LIST = "OUT_OF_LIST";
    private final KeyStroke CTRL_S = KeyStroke.getKeyStroke("ctrl S");
    private final KeyStroke CTRL_X = KeyStroke.getKeyStroke("ctrl X");
    private final KeyStroke CTRL_D = KeyStroke.getKeyStroke("ctrl D");
    private final KeyStroke CTRL_P = KeyStroke.getKeyStroke("ctrl P");

    @BeforeClass
    public static void setUpClass() throws IOException {
        // Copy user-defined properties to user config dir
        ClassLoader classLoader = PropertiesShortcuts.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream("org/omegat/gui/shortcuts/test.user.properties");
        File file = new File(StaticUtils.getConfigDir(), "test.properties");
        try {
            LFileCopy.copy(in, file);
        } finally {
            in.close();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Delete user-defined properties
        File file = new File(StaticUtils.getConfigDir(), "test.properties");
        file.delete();
    }

    @Before
    public void setUp() {
        shotcuts = new PropertiesShortcuts("/org/omegat/gui/shortcuts/test.properties");
    }

    @After
    public void tearDown() {
        shotcuts = null;
    }

    /**
     * Test of getKeyStroke method, of class PropertiesShortcuts.
     */
    @Test
    public void testGetKeyStroke() {
        System.out.println("getKeyStroke()");

        KeyStroke expected = CTRL_S;
        KeyStroke result = shotcuts.getKeyStroke(TEST_SAVE);
        assertEquals(expected, result);

        expected = CTRL_X;
        result = shotcuts.getKeyStroke(TEST_CUT);
        assertEquals(expected, result);

        result = shotcuts.getKeyStroke(TEST_DELETE);
        assertNull(result);

        expected = CTRL_P;
        result = shotcuts.getKeyStroke(TEST_USER_1);
        assertEquals(expected, result);

        try {
            shotcuts.getKeyStroke(OUT_OF_LIST);
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokes_JMenuBar() {
        System.out.println("bindKeyStrokes(JMenuBar)");

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
        shotcuts.bindKeyStrokes(menu);

        KeyStroke result = parent.getAccelerator();
        assertNull(result);

        result = child1.getAccelerator();
        assertNull(result);

        result = child2.getAccelerator();
        assertNull(result);

        KeyStroke expected = CTRL_P;
        result = grandchild1.getAccelerator();
        assertEquals(expected, result);

        expected = CTRL_X;
        result = grandchild2.getAccelerator();
        assertEquals(expected, result);
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokes_JMenuItem() {
        System.out.println("bindKeyStrokes(JMenuItem)");

        // case JMenuItem with no children
        JMenuItem item = new JMenuItem();
        item.setActionCommand(TEST_SAVE);
        KeyStroke expected = CTRL_S;
        KeyStroke result = item.getAccelerator();
        assertNull(result); // before binding
        shotcuts.bindKeyStrokes(item); // bind
        result = item.getAccelerator();
        assertEquals(expected, result); // after binding(1)

        item.setActionCommand(TEST_DELETE);
        shotcuts.bindKeyStrokes(item); // bind
        result = item.getAccelerator();
        assertNull(result); // after binding(2)

        item.setActionCommand(OUT_OF_LIST);
        item.setAccelerator(CTRL_D);
        shotcuts.bindKeyStrokes(item); // bind
        expected = CTRL_D;
        result = item.getAccelerator();
        assertEquals(expected, result); // after binding(3) - nothing has changed
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokes_JMenuItem_Recursive() {
        System.out.println("bindKeyStrokes(JMenuItem)");

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
        shotcuts.bindKeyStrokes(parent);

        KeyStroke result = parent.getAccelerator();
        assertNull(result);

        result = child1.getAccelerator();
        assertNull(result);

        result = child2.getAccelerator();
        assertNull(result);

        KeyStroke expected = CTRL_P;
        result = grandchild1.getAccelerator();
        assertEquals(expected, result);

        expected = CTRL_X;
        result = grandchild2.getAccelerator();
        assertEquals(expected, result);
    }

    /**
     * Test of bindKeyStrokes method, of class PropertiesShortcuts.
     */
    @Test
    public void testBindKeyStrokes_InputMap_ObjectArr() {
        System.out.println("bindKeyStrokes(InputMap, Object[])");

        // bind
        InputMap inputMap = new InputMap();
        shotcuts.bindKeyStrokes(inputMap, TEST_SAVE, TEST_CUT, TEST_USER_1);

        // test map size
        long expSize = 3;
        long size = inputMap.size();
        assertEquals(expSize, size);

        // test keys
        KeyStroke[] expResults = new KeyStroke[] { CTRL_S, CTRL_X, CTRL_P };
        KeyStroke[] results = inputMap.keys();
        assertArrayEquals(expResults, results);

        // test entry1 exists
        Object expResult = TEST_SAVE;
        Object result = inputMap.get(CTRL_S);
        assertEquals(expResult, result);

        // test entry2 exists
        expResult = TEST_CUT;
        result = inputMap.get(CTRL_X);
        assertEquals(expResult, result);

        // test entry3 exists
        expResult = TEST_USER_1;
        result = inputMap.get(CTRL_P);
        assertEquals(expResult, result);

        // test remove entry with null shortcut
        inputMap.put(CTRL_D, TEST_DELETE); // put target
        expResult = TEST_DELETE;
        result = inputMap.get(CTRL_D);
        assertEquals(expResult, result); // target exists before remove
        shotcuts.bindKeyStrokes(inputMap, TEST_DELETE); // key to be removed as null
        result = inputMap.get(CTRL_D);
        assertNull(result); // target will be null after removed

        // test map size again
        expSize = 3;
        size = inputMap.size();
        assertEquals(expSize, size);

        // ensure no affect for entry1 after removing
        expResult = TEST_SAVE;
        result = inputMap.get(CTRL_S);
        assertEquals(expResult, result);

        // ensure no affect for entry2 after removing
        expResult = TEST_CUT;
        result = inputMap.get(CTRL_X);
        assertEquals(expResult, result);

        // ensure no affect for entry3 after removing
        expResult = TEST_USER_1;
        result = inputMap.get(CTRL_P);
        assertEquals(expResult, result);
    }
}
