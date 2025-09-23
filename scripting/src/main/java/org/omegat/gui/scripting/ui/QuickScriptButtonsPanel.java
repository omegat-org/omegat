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
package org.omegat.gui.scripting.ui;

import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptingWindowController;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.FlowLayout;
import java.util.function.Consumer;

/**
 * Panel containing quick script access buttons
 */
@SuppressWarnings("serial")
public class QuickScriptButtonsPanel extends JPanel {

    private static final int NUMBERS_OF_QUICK_SCRIPTS = 12;
    private final JButton[] quickScriptButtons = new JButton[NUMBERS_OF_QUICK_SCRIPTS];
    private final ScriptingWindowController controller;

    private Consumer<ScriptItem> quickScriptUpdater;

    public QuickScriptButtonsPanel(ScriptingWindowController controller) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.controller = controller;
        initButtons();
    }

    private void initButtons() {
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            final int index = i;
            final int scriptKey = index + 1;
            quickScriptButtons[i] = new JButton(String.valueOf(scriptKey));

            // Run a script from the quick button bar
            quickScriptButtons[i].addActionListener(a -> {
                if (Preferences.existsPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey)) {
                    controller.runQuickScript(index);
                } else {
                    controller.logResultRB("SCW_NO_SCRIPT_BOUND", scriptKey);
                }
            });

            setupButtonPopupMenu(i, scriptKey);
            add(quickScriptButtons[i]);
        }
    }

    private void setupButtonPopupMenu(int index, int scriptKey) {
        JPopupMenu quickScriptPopup = new JPopupMenu();

        // Add a script to the quick script button bar
        final JMenuItem addQuickScriptMenuItem = new JMenuItem(ScriptingWindowController.getString("SCW_ADD_SCRIPT"));
        addQuickScriptMenuItem.addActionListener(e -> {
            if (quickScriptUpdater != null) {
                quickScriptUpdater.accept(null); // Will trigger updater with currently selected script
            }
        });
        quickScriptPopup.add(addQuickScriptMenuItem);

        // Remove a script from the button bar
        final JMenuItem removeQuickScriptMenuItem = new JMenuItem(
                ScriptingWindowController.getString("SCW_REMOVE_SCRIPT"));
        removeQuickScriptMenuItem.addActionListener(evt -> {
            String scriptName = Preferences
                    .getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "(unknown)");
            controller.logResult(StringUtil.format(ScriptingWindowController.getString("SCW_REMOVED_QUICK_SCRIPT"), scriptName,
                    scriptKey));
            Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "");
            quickScriptButtons[index].setToolTipText(ScriptingWindowController.getString("SCW_NO_SCRIPT_SET"));
            quickScriptButtons[index].setText(" " + scriptKey + " ");

            controller.unsetQuickScript(index);
        });
        quickScriptPopup.add(removeQuickScriptMenuItem);

        quickScriptPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // These will be handled by the updater callback
                if (quickScriptUpdater != null) {
                    addQuickScriptMenuItem.setEnabled(true);
                }

                // Disable remove a script command if the quick run button is not bounded
                String scriptName = Preferences
                        .getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, null);
                removeQuickScriptMenuItem.setEnabled(!StringUtil.isEmpty(scriptName));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // do nothing
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // do nothing
            }
        });

        quickScriptButtons[index].setComponentPopupMenu(quickScriptPopup);
    }

    public void setQuickScriptUpdater(Consumer<ScriptItem> updater) {
        this.quickScriptUpdater = updater;
    }

    public void updateButtonState(int index, String scriptName) {
        int key = index + 1;

        if (controller.getScriptsDirectory() != null && !StringUtil.isEmpty(scriptName)) {
            quickScriptButtons[index].setToolTipText(scriptName);
            quickScriptButtons[index].setText("<" + key + ">");
        } else {
            if (quickScriptButtons.length <= index || quickScriptButtons[index] == null) {
                return;
            }

            quickScriptButtons[index].setToolTipText(ScriptingWindowController.getString("SCW_NO_SCRIPT_SET"));
            quickScriptButtons[index].setText(String.valueOf(key));
        }
    }
}
