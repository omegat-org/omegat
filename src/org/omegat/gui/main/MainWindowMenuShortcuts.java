/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
package org.omegat.gui.main;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * Class for set shortcuts to main menu.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindowMenuShortcuts {
    /**
     * Initialize shortcuts from configured values.
     * 
     * @param menu
     *            main menu
     */
    public static void setShortcuts(final JMenuBar menu) {
        Properties shortcuts = loadPredefinedShortcuts();
        setup(menu.getComponents(), shortcuts);
    }

    /**
     * Travel by all submenus for setup shortcuts.
     * 
     * @param menu
     *            menu or menu item
     * @param shortcuts
     *            shortcuts list
     */
    private static void setup(final Component[] items, final Properties shortcuts) {
        for (Component c : items) {
            if (c instanceof JMenu) {
                setup(((JMenu) c).getMenuComponents(), shortcuts);
            }
            if (c instanceof JMenuItem) {
                String cmd = ((JMenuItem) c).getActionCommand();
                setAccelerator((JMenuItem) c, shortcuts.getProperty(cmd));
            }
        }
    }

    /**
     * Setup shortcut for one specified menu item.
     * 
     * @param item
     *            menu item
     * @param shortcut
     *            shortcut text
     */
    private static void setAccelerator(final JMenuItem item, final String shortcut) {
        if (StringUtil.isEmpty(shortcut)) {
            return;
        }

        item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
    }

    /**
     * Load shortcuts from MainMenuShortcuts.properties file in classpath and in
     * the user's config dir.
     * 
     * @return shortcuts list
     */
    private static final Properties loadPredefinedShortcuts() {
        Properties shortcuts = new Properties();
        String name = MainWindowMenuShortcuts.class.getPackage().getName().replace('.', '/')
                + "/MainMenuShortcuts";
        name += StaticUtils.onMacOSX() ? ".mac.properties" : ".properties";

        File userShortcuts = new File(StaticUtils.getConfigDir(), "MainMenuShortcuts.properties");
        try {
            InputStream in = MainWindowMenuShortcuts.class.getClassLoader().getResourceAsStream(name);
            try {
                shortcuts.load(in);
            } finally {
                in.close();
            }
            if (userShortcuts.exists()) {
                InputStream us = new FileInputStream(userShortcuts);
                try {
                    shortcuts.load(us);
                } finally {
                    us.close();
                }
            }
        } catch (IOException ex) {
            throw new ExceptionInInitializerError();
        }
        return shortcuts;
    }
}
