/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;

import org.omegat.core.Core;
import org.omegat.util.Platform;

public final class MenuExtender {

    public enum MenuKey {
        /**
         * Project menu.
         */
        PROJECT("project", 21),
        /**
         * Edit menu.
         */
        EDIT("edit", -1),
        /**
         * Goto menu.
         */
        GOTO("goto", -1),
        /**
         * View menu.
         */
        VIEW("view", 13),
        /**
         * Tools menu.
         */
        TOOLS("tools", -1),
        /**
         * Options menu.
         */
        OPTIONS("options", Platform.isMacOSX() ? 9 : 11),
        /**
         * Help menu.
         */
        HELP("help", 3);

        private final String value;
        private final int position;

        MenuKey(String value, int position) {
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Return index of menu items to insert custom item. Position should be a
         * place where extension point. when you want to insert a menu item between
         * item 2 and item 3, a number should be a position of item 3.
         *
         * @return index of menu items. 0 is first and -1 is last.
         */
        public int getPosition() {
            return position;
        }
    }

    private MenuExtender() {
    }

    private static final Map<MenuKey, Integer> numAddedMenuItems = new EnumMap<>(MenuKey.class);
    private static final Map<MenuKey, List<Component>> addedMenuItems = new EnumMap<>(MenuKey.class);

    public static void removeMenuItems(MenuKey target, List<Component> components) {
        int offset = numAddedMenuItems.getOrDefault(target, 0);
        JMenu menu = Core.getMainWindow().getMainMenu().getMenu(target);
        components.forEach(menu::remove);
        if (target.getPosition() >= 0) {
            numAddedMenuItems.put(target, offset - components.size());
        } else {
            numAddedMenuItems.put(target, 0);
        }
    }

    public static void addMenuItems(MenuKey target, List<Component> newMenuItems) {
        int offset = numAddedMenuItems.getOrDefault(target, 0);
        JMenu targetMenu = Core.getMainWindow().getMainMenu().getMenu(target);
        newMenuItems.forEach(targetMenu::add);
        if (target.getPosition() >= 0) {
            numAddedMenuItems.put(target, offset + newMenuItems.size());
        } else {
            numAddedMenuItems.put(target, 0);
        }
    }

    /**
     * API for plugins/modules to add menu item.
     * <p>
     * Plugins are recommended to use `IMainWindow#addMenuItem` instead of
     * `IMainMenu#getProjectMenu` or similar methods.
     * Because `MainWindow#getMainMenu` is an internal API and recommended
     * not to use in plugins.
     *
     * @param target MenuMarker object to indicate where to insert menu item.
     * @param menuItem component to insert.
     */
    public static void addMenuItem(MenuKey target, Component menuItem) {
        if (target == null) {
            throw new NullPointerException();
        }
        if (menuItem == null) {
            throw new NullPointerException();
        }
        JMenu targetMenu = Core.getMainWindow().getMainMenu().getMenu(target);
        int offset = numAddedMenuItems.getOrDefault(target, 0);
        List<Component> menuItems;
        if (addedMenuItems.get(target) == null) {
            menuItems = new ArrayList<>();
            addedMenuItems.put(target, menuItems);
        } else {
            menuItems = addedMenuItems.get(target);
        }
        targetMenu.add(menuItem, target.getPosition() + offset);
        menuItems.add(menuItem);
        if (target.getPosition() >= 0) {
            numAddedMenuItems.put(target, offset + 1);
        } else {
            numAddedMenuItems.put(target, 0);
        }
    }

}
