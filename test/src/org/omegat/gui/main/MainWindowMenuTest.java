/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.gui.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.Test;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.MenuExtender;
import org.omegat.util.gui.MenuExtender.MenuKey;
import org.omegat.util.gui.MenuItemPager;

/**
 * @author Alex Buloichik
 */
public class MainWindowMenuTest extends TestCore {
    /**
     * Check MainWindow for all menu items action handlers exist.
     *
     * @throws Exception
     */
    @Test
    public void testMenuActions() throws Exception {
        int count = 0;

        Map<String, Method> existsMethods = new HashMap<String, Method>();

        for (Method m : MainWindowMenuHandler.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 0) {
                    existsMethods.put(m.getName(), m);
                }
                // Include menu items that take a modifier key.
                if (params.length == 1 && params[0] == Integer.TYPE) {
                    existsMethods.put(m.getName(), m);
                }
            }
        }

        for (Field f : StaticUtils.getAllModelFields(MainWindowMenu.class)) {
            if (JMenuItem.class.isAssignableFrom(f.getType()) && f.getType() != JMenu.class) {
                count++;
                String actionMethodName = f.getName() + "ActionPerformed";
                Method m;
                try {
                    m = MainWindowMenuHandler.class.getMethod(actionMethodName);
                } catch (NoSuchMethodException ignore) {
                    // See if the method accepts a modifier key argument.
                    m = MainWindowMenuHandler.class.getMethod(actionMethodName, Integer.TYPE);
                }
                assertNotNull("Action method not defined for " + f.getName(), m);
                assertNotNull(existsMethods.remove(actionMethodName));
            }
        }
        assertTrue("menu items not found", count > 30);
        assertTrue("There is action handlers in MainWindow which doesn't used in menu: "
                + existsMethods.keySet(), existsMethods.isEmpty());
    }

    @Test
    public void testMenuPositions() {
        TestMainMenu testMenu = new TestMainMenu();
        testMenu.initComponents();
        assertEquals(getLocalizedText("MW_PROJECTMENU_EDIT"), getExtensionPointItem(testMenu,
                MenuKey.PROJECT).getText());
        assertEquals(getLocalizedText("TF_MENU_EDIT_IDENTICAL_TRANSLATION"), getExtensionPointItem(testMenu,
                MenuKey.EDIT).getText());
        assertEquals(getLocalizedText("MW_VIEW_MENU_MODIFICATION_INFO"), getExtensionPointItem(testMenu,
                MenuKey.VIEW).getText());
        assertEquals(getLocalizedText("TF_MENU_GOTO_EDITOR_PANEL"), getExtensionPointItem(testMenu,
                MenuKey.GOTO).getText());
        assertEquals(getLocalizedText("MW_OPTIONSMENU_ACCESS_CONFIG_DIR"), getExtensionPointItem(testMenu,
                MenuKey.OPTIONS).getText());
        assertEquals(getLocalizedText("TF_MENU_HELP_LOG"), getExtensionPointItem(testMenu,
                MenuKey.HELP).getText());
        assertEquals(-1, MenuKey.EDIT.getPosition());
    }

    private JMenuItem getExtensionPointItem(TestMainMenu menu, MenuKey key) {
        if (key.getPosition() == -1) {
            return menu.getMenu(key).getItem(menu.getMenu(key).getItemCount() - 1);
        } else {
            return menu.getMenu(key).getItem(key.getPosition());
        }
    }

    private String getLocalizedText(final String key) {
        JLabel tmp = new JLabel();
        Mnemonics.setLocalizedText(tmp, OStrings.getString(key));
        return tmp.getText();
    }

    static class TestMainMenu extends BaseMainWindowMenu {

        public TestMainMenu() {
            super(null, null);
        }

        @Override
        void initComponents() {
            createComponents();
            constructMenu();
            createMenuBar();
        }

        @Override
        void createMenuBar() {
            mainMenu.add(projectMenu);
            mainMenu.add(editMenu);
            mainMenu.add(gotoMenu);
            mainMenu.add(viewMenu);
            mainMenu.add(toolsMenu);
            mainMenu.add(optionsMenu);
            mainMenu.add(helpMenu);
        }
    }

    /**
     * Test menu addition API.
     * <p>
     * menu items for a test case; {@see org.omegat.core.TestCore#getHelpMenu}
     * <ul>
     * <li>user manual</li>
     * <li>about</li>
     * <li>separator()</li>
     * <li>item 3</li>
     * </ul>
     * Amend data
     * <ul>
     * <li>ext 1</li>
     * <li>ext 2</li>
     * </ul>
     * expected result
     * <ul>
     * <li>user manual</li>
     * <li>about</li>
     * <li>separator()</li>
     * <li>ext 1</li>
     * <li>ext 2</li>
     * <li>item 3</li>
     * </ul>
     * extension point is after separator = position 3
     */
    @Test
    public void testAddHelpMenuItem() {
        JMenuItem menuItem1 = new JMenuItem("ext 1");
        JMenuItem menuItem2 = new JMenuItem("ext 2");
        assertEquals(3, MenuKey.HELP.getPosition());
        MenuExtender.addMenuItem(MenuKey.HELP, menuItem1);
        MenuExtender.addMenuItem(MenuKey.HELP, menuItem2);
        JMenu helpMenu = Core.getMainWindow().getMainMenu().getMenu(MenuKey.HELP);
        // Check result
        assertEquals(6, helpMenu.getMenuComponentCount());
        assertEquals("User manual", helpMenu.getItem(0).getText());
        assertEquals("About", helpMenu.getItem(1).getText());
        assertNull(helpMenu.getItem(2)); // separator
        assertEquals("ext 1", helpMenu.getItem(3).getText());
        assertEquals("ext 2", helpMenu.getItem(4).getText());
    }

    /**
     * Test menu addition on Options menu.
     */
    @Test
    public void testAddOptionsMenuItem() {
        assertEquals(Platform.isMacOSX() ? 9 : 11, MenuKey.OPTIONS.getPosition());
        JMenuItem menuItem3 = new JMenuItem("ext 3");
        JMenuItem menuItem4 = new JMenuItem("ext 4");
        MenuExtender.addMenuItem(MenuKey.OPTIONS, menuItem3);
        MenuExtender.addMenuItem(MenuKey.OPTIONS, menuItem4);
        JMenu optionsMenu = Core.getMainWindow().getMainMenu().getMenu(MenuKey.OPTIONS);
        int n = Platform.isMacOSX() ? 0 : 2;
        if (!Platform.isMacOSX()) {
            assertEquals("Preference", optionsMenu.getItem(0).getText());
            assertNull(optionsMenu.getItem(1)); // separator
        }
        assertEquals(n + 13, optionsMenu.getItemCount());
        assertEquals("MachineTranslate", optionsMenu.getItem(n).getActionCommand());
        assertEquals("Glossary", optionsMenu.getItem(n + 1).getActionCommand());
        assertEquals("Dictionary", optionsMenu.getItem(n + 2).getText());
        assertEquals("AutoComplete", optionsMenu.getItem(n + 3).getActionCommand());
        assertNull(optionsMenu.getItem(n + 4)); // separator
        assertEquals("SetupFileFilters", optionsMenu.getItem(n + 5).getText());
        assertEquals("Sentseg", optionsMenu.getItem(n + 6).getText());
        assertEquals("Workflow", optionsMenu.getItem(n + 7).getText());
        assertNull(optionsMenu.getItem(n + 8)); // separator
        assertEquals("ext 3", optionsMenu.getItem(n + 9).getText());
        assertEquals("ext 4", optionsMenu.getItem(n + 10).getText());
        assertEquals("AccessConfigDir", optionsMenu.getItem(n + 11).getText());
    }

    /**
     * Test menu addition on goto menu.
     */
    @Test
    public void testAddGotoMenuItem() {
        JMenuItem menuItem1 = new JMenuItem("ext 1");
        JMenuItem menuItem2 = new JMenuItem("ext 2");
        assertEquals(-1, MenuKey.GOTO.getPosition());
        MenuExtender.addMenuItem(MenuKey.GOTO, menuItem1);
        MenuExtender.addMenuItem(MenuKey.GOTO, menuItem2);
        JMenu gotoMenu = Core.getMainWindow().getMainMenu().getMenu(MenuKey.GOTO);
        assertEquals(19, gotoMenu.getItemCount());
        assertEquals("gotoNextUntranslatedMenuItem", gotoMenu.getItem(0).getText());
        assertNull(gotoMenu.getItem(9)); // separator
        assertEquals("ext 1", gotoMenu.getItem(17).getText());
        assertEquals("ext 2", gotoMenu.getItem(18).getText());
    }

    @Test
    public void testAddToolsMenuPagerItems() {
        JMenu toolsMenu = Core.getMainWindow().getMainMenu().getMenu(MenuKey.TOOLS);
        assertEquals(-1, MenuKey.TOOLS.getPosition());
        // create pager page
        assertEquals(7, toolsMenu.getItemCount());
        // add items through pager
        List<Component> newMenuItems = new ArrayList<>(addMenuItemsToPager(MenuKey.TOOLS));
        assertEquals(18, toolsMenu.getItemCount());
        // check consistency
        assertEquals("ext 1", toolsMenu.getItem(7).getText());
        assertEquals("ext 10", toolsMenu.getItem(16).getText());
        JMenuItem lastItem = toolsMenu.getItem(17);
        assertEquals(OStrings.getString("MW_MORE_SUBMENU"), lastItem.getText());
        // add more menu item
        JMenuItem menuItem13 = new JMenuItem("ext 13");
        MenuExtender.addMenuItem(MenuKey.TOOLS, menuItem13);
        assertEquals(19, toolsMenu.getItemCount());
        // removal of menu items from pager
        MenuExtender.removeMenuItems(MenuKey.TOOLS, newMenuItems);
        // The last item is one added at above.
        assertEquals(8, toolsMenu.getItemCount());
        assertEquals("ext 13", toolsMenu.getItem(7).getText());
    }

    private List<JMenuItem> addMenuItemsToPager(MenuKey target) {
        List<JMenuItem> newMenuItems = new ArrayList<>();
        JMenuItem menuItem1 = new JMenuItem("ext 1");
        newMenuItems.add(menuItem1);
        JMenuItem menuItem2 = new JMenuItem("ext 2");
        newMenuItems.add(menuItem2);
        JMenuItem menuItem3 = new JMenuItem("ext 3");
        newMenuItems.add(menuItem3);
        JMenuItem menuItem4 = new JMenuItem("ext 4");
        newMenuItems.add(menuItem4);
        JMenuItem menuItem5 = new JMenuItem("ext 5");
        newMenuItems.add(menuItem5);
        JMenuItem menuItem6 = new JMenuItem("ext 6");
        newMenuItems.add(menuItem6);
        JMenuItem menuItem7 = new JMenuItem("ext 7");
        newMenuItems.add(menuItem7);
        JMenuItem menuItem8 = new JMenuItem("ext 8");
        newMenuItems.add(menuItem8);
        JMenuItem menuItem9 = new JMenuItem("ext 9");
        newMenuItems.add(menuItem9);
        JMenuItem menuItem10 = new JMenuItem("ext 10");
        newMenuItems.add(menuItem10);
        JMenuItem menuItem11 = new JMenuItem("ext 11");
        newMenuItems.add(menuItem11);
        JMenuItem menuItem12 = new JMenuItem("ext 12");
        newMenuItems.add(menuItem12);
        // create pager
        MenuItemPager pager = new MenuItemPager(target);
        newMenuItems.forEach(pager::add);
        return pager.getFirstPage();
    }
}
