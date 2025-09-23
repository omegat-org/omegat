/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
               2025 Hiroshi Miura
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
package org.omegat.gui.scripting;

import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.openide.awt.Mnemonics;

/**
 * Manages quick script functionality, including storing references to scripts,
 * updating menu items, and handling UI interactions.
 */
public class QuickScriptManager {

    public static final int NUMBERS_OF_QUICK_SCRIPTS = 12;

    private final ScriptingWindowController controller;
    private final String[] quickScripts;
    private final JMenuItem[] quickMenuItems;

    /**
     * Creates a new QuickScriptManager.
     *
     * @param controller The scripting window controller
     * @param quickMenuItems Array of menu items for quick scripts
     */
    public QuickScriptManager(ScriptingWindowController controller, JMenuItem[] quickMenuItems) {
        this.controller = controller;
        this.quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];
        this.quickMenuItems = quickMenuItems;

        // Initialize quick menu items
        initializeQuickMenuItems();
    }

    /**
     * Sets up the quick menu items with appropriate labels and disabled state.
     */
    private void initializeQuickMenuItems() {
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            if (quickMenuItems[i] != null) {
                setQuickScriptMenuDisabled(i);
            }
        }
    }

    /**
     * Gets the filename of the quick script at the given index.
     *
     * @param index The index of the quick script
     * @return The filename of the quick script, or null if not set
     */
    public String getQuickScriptFilename(int index) {
        if (index < 0 || index >= quickScripts.length) {
            return null;
        }
        return quickScripts[index];
    }

    /**
     * Sets a quick script at the specified index.
     *
     * @param scriptItem The script item to set
     * @param index The index where to set the script
     */
    public void setQuickScript(ScriptItem scriptItem, int index) {
        if (index < 0 || index >= NUMBERS_OF_QUICK_SCRIPTS) {
            return;
        }

        quickScripts[index] = scriptItem.getFileName();

        // Update menu item
        updateQuickMenuItem(scriptItem, index);
    }

    /**
     * Removes a quick script at the specified index.
     *
     * @param index The index of the quick script to remove
     */
    public void unsetQuickScript(int index) {
        if (index < 0 || index >= NUMBERS_OF_QUICK_SCRIPTS) {
            return;
        }

        quickScripts[index] = null;
        setQuickScriptMenuDisabled(index);
    }

    /**
     * Updates the menu item for a quick script.
     *
     * @param scriptItem The script item
     * @param index The index of the menu item to update
     */
    private void updateQuickMenuItem(ScriptItem scriptItem, int index) {
        if (quickMenuItems[index] == null) {
            return;
        }

        // Remove existing listeners
        removeAllQuickScriptActionListenersFrom(quickMenuItems[index]);

        // Add new listener
        quickMenuItems[index].addActionListener(e -> controller.runQuickScript(index));

        // Set keyboard shortcut
        quickMenuItems[index].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (index + 1)));

        // Enable the menu item
        quickMenuItems[index].setEnabled(true);

        // Set tooltip if description exists
        if (scriptItem.getDescription() != null && !scriptItem.getDescription().isEmpty()) {
            quickMenuItems[index].setToolTipText(scriptItem.getDescription());
        }

        // Set menu text
        Mnemonics.setLocalizedText(quickMenuItems[index],
                "&" + (index + 1) + " - " + scriptItem.getScriptName());
    }

    /**
     * Disables a quick script menu item.
     *
     * @param index The index of the menu item to disable
     */
    private void setQuickScriptMenuDisabled(int index) {
        if (quickMenuItems[index] == null) {
            return;
        }

        removeAllQuickScriptActionListenersFrom(quickMenuItems[index]);
        quickMenuItems[index].setEnabled(false);
        Mnemonics.setLocalizedText(quickMenuItems[index],
                "&" + (index + 1) + " - " + ScriptingWindowController.getString("SCW_SCRIPTS_NONE"));
    }

    /**
     * Removes all quick script action listeners from a menu item.
     *
     * @param menuItem The menu item to remove listeners from
     */
    private void removeAllQuickScriptActionListenersFrom(JMenuItem menuItem) {
        if (menuItem == null) {
            return;
        }

        // Create a copy of the listeners array to avoid concurrent modification
        Object[] listeners = menuItem.getActionListeners();
        for (Object listener : listeners) {
            menuItem.removeActionListener(e -> controller.runQuickScript(0)); // Remove all action listeners
        }
    }

    /**
     * Loads a script set.
     *
     * @param set The script set to load
     */
    public void loadScriptSet(ScriptSet set) {
        for (int i = 0; i < QuickScriptManager.NUMBERS_OF_QUICK_SCRIPTS; i++) {
            ScriptItem scriptItem = set.getScriptItem(i + 1);
            if (scriptItem != null) {
                setQuickScript(scriptItem, i);
            } else {
                unsetQuickScript(i);
            }
        }
    }

    /**
     * Returns an array of all quick script filenames.
     *
     * @return Array of quick script filenames
     */
    public String[] getAllQuickScriptFilenames() {
        return quickScripts.clone(); // Return a copy to maintain encapsulation
    }

    /**
     * Loads quick scripts from preferences.
     */
    public void loadQuickScriptsFromPreferences() {
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            String scriptPath = Preferences.getPreferenceDefault(
                    Preferences.SCRIPTS_QUICK_PREFIX + (i + 1), null);

            if (!StringUtil.isEmpty(scriptPath)) {
                File scriptFile = new File(scriptPath);
                if (scriptFile.exists() && scriptFile.isFile()) {
                    ScriptItem item = new ScriptItem(scriptFile);
                    setQuickScript(item, i);
                } else {
                    unsetQuickScript(i);
                    Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + (i + 1), "");
                }
            } else {
                unsetQuickScript(i);
            }
        }
    }

    /**
     * Saves a quick script to preferences.
     *
     * @param index The index of the quick script
     * @param scriptPath The path of the script file
     */
    public void saveQuickScriptToPreferences(int index, String scriptPath) {
        Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + (index + 1), scriptPath);
    }
}
