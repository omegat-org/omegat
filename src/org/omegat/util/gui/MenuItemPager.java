/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * A helper class for creating nested submenus. Usage:
 * <ol>
 * <li>In the constructor provide the target menu to which you want to add items.
 * <li>Optionally set the number of items per page with {@link #setItemsPerPage(int)} (default is
 * {@link #DEFAULT_ITEMS_PER_PAGE}).
 * <li>Call {@link #add(JMenuItem)} for each item to add. A new submenu will be created and automatically
 * added to the current (sub)menu as necessary.
 * <li>Optionally obtain the "first page" of items (the items added directly to the root target menu) with
 * {@link #getFirstPage()}.
 * </ol>
 *
 * @author Aaron Madlon-Kay
 */
public class MenuItemPager {
    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    private Container menu;
    private boolean isManagedMenuExtenderAPI = false;
    private MenuExtender.MenuKey targetKey = null;
    private int count;
    private int itemsPerPage;
    private final List<JMenuItem> firstPage;

    public MenuItemPager(JPopupMenu menu) {
        this((Container) menu);
    }

    public MenuItemPager(JMenu menu) {
        this((Container) menu);
    }

    public MenuItemPager(MenuExtender.MenuKey target) {
        this((Container) Core.getMainWindow().getMainMenu().getMenu(target));
        targetKey = target;
        isManagedMenuExtenderAPI = true;
    }

    private MenuItemPager(Container menu) {
        this.menu = Objects.requireNonNull(menu);
        this.count = 0;
        this.itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        // Number of items on first page is itemsPerPage + 1 for the submenu
        this.firstPage = new ArrayList<>(itemsPerPage + 1);
    }

    /**
     * Set the size of the page (number of items allowed before a new submenu is created).
     *
     * The actual number of items added to any (sub)menu is <code>itemsPerPage + 1</code> for the next
     * submenu.
     *
     * @param itemsPerPage
     *            Page size
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Add an item to the menu. The item will be added to a submenu as appropriate.
     *
     * @param newItem
     *            The item to add
     * @return The provided item
     */
    public JMenuItem add(JMenuItem newItem) {
        if (count > 0 && count % itemsPerPage == 0) {
            newPage();
            // hack for second page
            isManagedMenuExtenderAPI = false;
        }
        addImpl(newItem);
        count++;
        return newItem;
    }

    private void newPage() {
        JMenu newSubmenu = new JMenu(OStrings.getString("MW_MORE_SUBMENU"));
        menu = addImpl(newSubmenu);
    }

    private JMenuItem addImpl(JMenuItem item) {
        if (firstPage.size() < itemsPerPage + 1) {
            firstPage.add(item);
        }
        if (isManagedMenuExtenderAPI) {
            MenuExtender.addMenuItem(targetKey, item);
        } else {
            menu.add(item);
        }
        return item;
    }

    /**
     * Get the "first page" of items (the items added directly to the provided root menu). If a second page
     * was added, the list will include the item representing the submenu.
     *
     * @return The first-page items
     */
    public List<JMenuItem> getFirstPage() {
        return Collections.unmodifiableList(firstPage);
    }
}
