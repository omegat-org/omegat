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

package org.omegat.gui.main;

import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.event.MenuListener;

import org.omegat.gui.accesstool.AccessTools;
import org.omegat.util.gui.ResourcesUtil;

/**
 * Class for newUI main menu.
 * 
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public final class MainWindowBurgerMenu extends BaseMainWindowMenu
        implements ActionListener, MenuListener, IMainMenu {

    JMenu burgerMenu;

    public MainWindowBurgerMenu(MainWindow mainWindow, MainWindowMenuHandler mainWindowMenuHandler) {
        super(mainWindow, mainWindowMenuHandler);
        initComponents();
    }

    @Override
    void createMenuBar() {
        // build burger menu
        burgerMenu = new JMenu();
        Icon burgerIcon = UIManager.getIcon("OmegaT.newUI.menu.icon");
        burgerMenu.setIcon(Objects.requireNonNullElseGet(burgerIcon,
                () -> new ImageIcon(ResourcesUtil.getBundledImage("newUI.burgerMenu.png"))));
        burgerMenu.add(projectMenu);
        burgerMenu.add(editMenu);
        burgerMenu.add(gotoMenu);
        burgerMenu.add(viewMenu);
        burgerMenu.add(toolsMenu);
        burgerMenu.add(optionsMenu);
        burgerMenu.add(helpMenu);
        burgerMenu.add(burgerMenu);
        mainMenu.add(burgerMenu);
        mainMenu.add(new AccessTools(mainWindow, mainWindowMenuHandler));
    }
}
