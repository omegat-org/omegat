/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik, Yu Tang
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

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * The <code>PropertiesShortcuts</code> class represents a persistent set of shortcut.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Yu Tang
 */
public class PropertiesShortcuts {

    private static final Logger LOGGER = Logger.getLogger(PropertiesShortcuts.class.getName());

    private static final String MAIN_MENU_SHORTCUTS_FILE = "/org/omegat/gui/main/MainMenuShortcuts.properties";
    private static final String EDITOR_SHORTCUTS_FILE = "/org/omegat/gui/main/EditorShortcuts.properties";

    private static class LoadedShortcuts {
        static final PropertiesShortcuts MAIN_MENU_SHORTCUTS = new PropertiesShortcuts(
                MAIN_MENU_SHORTCUTS_FILE);
        static final PropertiesShortcuts EDITOR_SHORTCUTS = new PropertiesShortcuts(EDITOR_SHORTCUTS_FILE);
    }

    public static PropertiesShortcuts getMainMenuShortcuts() {
        return LoadedShortcuts.MAIN_MENU_SHORTCUTS;
    }

    public static PropertiesShortcuts getEditorShortcuts() {
        return LoadedShortcuts.EDITOR_SHORTCUTS;
    }

    final Properties properties = new Properties();

    /**
     * Creates shortcut list with the specified defaults and user shortcuts.
     * Look for specified file in these places in this order:
     * <ol>
     * <li>Stream in classpath
     * <li>File of same name in the user's config dir
     * </ol>
     * For each shortcut, user shortcuts have priority, then defaults (for
     * Mac-specific or others).
     *
     * @param conf
     *            ShortcutsConfiguration
     */
    public PropertiesShortcuts(String propertiesFile) {
        try {
            if (Platform.isMacOSX()) {
                String macSpecific = propertiesFile.replaceAll("\\.properties$", ".mac.properties");
                loadProperties(macSpecific);
            }
            if (properties.isEmpty()) {
                loadProperties(propertiesFile);
            }
            File userFile = new File(StaticUtils.getConfigDir(), new File(propertiesFile).getName());
            loadProperties(userFile);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private void loadProperties(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                properties.load(in);
            }
        }
    }

    private void loadProperties(File file) throws IOException {
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            }
        }
    }

    public KeyStroke getKeyStroke(String key) {
        String shortcut = properties.getProperty(key);
        if (shortcut == null) {
            throw new IllegalArgumentException("Keyboard shortcut not defined. Key=" + key);
        }
        KeyStroke result = KeyStroke.getKeyStroke(shortcut);
        if (!shortcut.isEmpty() && result == null) {
            LOGGER.warning("Keyboard shortcut is invalid: " + key + "=" + shortcut);
        }
        return result;
    }

    public void bindKeyStrokes(JMenuBar menu) {
        applyTo(menu.getComponents());
    }

    /**
     * Travel by all submenus for setup shortcuts.
     * 
     * @param menu
     *            menu or menu item
     */
    private void applyTo(final Component[] items) {
        for (Component c : items) {
            if (c instanceof JMenuItem) {
                bindKeyStrokes((JMenuItem) c);
            }
        }
    }

    public void bindKeyStrokes(final JMenuItem item) {
        if (item instanceof JMenu) {
            // setAccelerator() is not defined for JMenu.
            applyTo(((JMenu) item).getMenuComponents());
        } else {
            String shortcut = item.getActionCommand();
            if (!StringUtil.isEmpty(shortcut)) {
                try {
                item.setAccelerator(getKeyStroke(shortcut));
                } catch (Exception ex) {
                    // Eat exception silently
                }
            }
        }
    }

    public void bindKeyStrokes(InputMap inputMap, String... keys) {
        for (String key : keys) {
            try {
                KeyStroke keyStroke = getKeyStroke(key);
                if (keyStroke == null) {
                    removeEntry(inputMap, key);
                } else {
                    inputMap.put(keyStroke, key);
                }
            } catch (Exception ex) {
                // Eat exception silently
            }
        }
    }

    private KeyStroke removeEntry(InputMap inputMap, String keyToBeRemoved) {
        KeyStroke removedEntry = null;
        for (KeyStroke ks : inputMap.keys()) {
            String key = (String) inputMap.get(ks);
            if (key.equals(keyToBeRemoved)) {
                inputMap.remove(ks);
                removedEntry = ks;
                break;
            }
        }
        return removedEntry;
    }
}
