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

    private final Properties properties = new Properties();

    /**
     * Creates shortcut list with the specified defaults and user shortcuts. Load defaults from properties
     * file in classpath and user-defined one in the user's config dir. For each shortcut, user shortcuts have
     * priority, then defaults (for Mac-specific or others).
     *
     * @param conf
     *            ShortcutsConfiguration
     */
    public PropertiesShortcuts(String propertiesFileInClasspath) {
        try {
            if (Platform.isMacOSX()) {
                String macSpecific = propertiesFileInClasspath
                        .replaceAll("\\.properties$", ".mac.properties");
                loadProperties(PropertiesShortcuts.class.getResourceAsStream(macSpecific));
            }
            if (properties.isEmpty()) {
                loadProperties(PropertiesShortcuts.class.getResourceAsStream(propertiesFileInClasspath));
            }
            String userFile = propertiesFileInClasspath
                    .substring(propertiesFileInClasspath.lastIndexOf('/') + 1);
            File userShortcutsFile = new File(StaticUtils.getConfigDir(), userFile);
            if (userShortcutsFile.exists()) {
                loadProperties(new FileInputStream(userShortcutsFile));
            }
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private void loadProperties(final InputStream in) throws IOException {
        if (in != null) {
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        }
    }

    public KeyStroke getKeyStroke(String key) {
        String shortcut = properties.getProperty(key);
        if (shortcut == null) {
            throw new IllegalArgumentException("Key '" + key + "' is not found.");
        }
        return KeyStroke.getKeyStroke(shortcut);
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

    public void bindKeyStrokes(InputMap inputMap, Object... keys) {
        for (Object o : keys) {
            try {
                String key = (String) o;
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
