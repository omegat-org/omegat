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

import org.omegat.gui.scripting.ScriptSet;
import org.omegat.gui.scripting.ScriptingWindowController;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.help.Help;
import org.omegat.util.gui.DesktopWrapper;
import org.openide.awt.Mnemonics;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Factory for creating menus for the scripting window
 */
public class ScriptingMenuFactory {

    private final ScriptingWindowController controller;
    private final JFrame parentFrame;
    private final Consumer<File> scriptOpener;
    private final Runnable scriptRunner;
    private final Runnable newScriptAction;
    private final Consumer<File> scriptSaver;

    public ScriptingMenuFactory(ScriptingWindowController controller, JFrame parentFrame,
                                Consumer<File> scriptOpener, Runnable scriptRunner,
                                Runnable newScriptAction, Consumer<File> scriptSaver) {
        this.controller = controller;
        this.parentFrame = parentFrame;
        this.scriptOpener = scriptOpener;
        this.scriptRunner = scriptRunner;
        this.newScriptAction = newScriptAction;
        this.scriptSaver = scriptSaver;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        menuBar.add(createFileMenu());

        // Sets menu
        JMenu setsMenu = createSetsMenu();
        menuBar.add(setsMenu);

        // Help menu
        menuBar.add(createHelpMenu());

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, ScriptingWindowController.getString("SCW_MENU_TITLE"));

        JMenuItem item;

        // Open script
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_LOAD_FILE"));
        item.addActionListener(e -> {
            File openFileDir = controller.getScriptsDirectory();
            JFileChooser chooser = new JFileChooser(openFileDir);
            chooser.setDialogTitle(ScriptingWindowController.getString("SCW_SCRIPTS_OPEN_SCRIPT_TITLE"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = chooser.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                scriptOpener.accept(chooser.getSelectedFile());
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        // New script
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_NEW_SCRIPT"));
        item.addActionListener(e -> newScriptAction.run());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        // Save script
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_SAVE_SCRIPT"));
        item.addActionListener(e -> {
            File scriptsDir = controller.getScriptsDirectory();
            JFileChooser chooser = new JFileChooser(scriptsDir);
            chooser.setDialogTitle(ScriptingWindowController.getString("SCW_SAVE_SCRIPT"));
            int result = chooser.showSaveDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                scriptSaver.accept(chooser.getSelectedFile());
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        // Run script
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_RUN_SCRIPT"));
        item.addActionListener(e -> scriptRunner.run());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        menu.addSeparator();

        // Set scripts folder
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_MENU_SET_SCRIPTS_FOLDER"));
        item.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(controller.getScriptsDirectory());
            chooser.setDialogTitle(ScriptingWindowController.getString("SCW_SCRIPTS_FOLDER_CHOOSE_TITLE"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                controller.setScriptsDirectory(chooser.getSelectedFile().getPath());
            }
        });
        menu.add(item);

        // Access scripts folder
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_MENU_ACCESS_FOLDER"));
        item.addActionListener(e -> {
            try {
                DesktopWrapper.open(controller.getScriptsDirectory());
            } catch (Exception ex) {
                controller.logErrorRB(ex, "RPF_ERROR");
            }
        });
        menu.add(item);

        menu.addSeparator();

        // Close window
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_MENU_CLOSE"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        item.addActionListener(e -> {
            parentFrame.setVisible(false);
            parentFrame.dispose();
        });
        menu.add(item);

        PropertiesShortcuts.getMainMenuShortcuts().bindKeyStrokes(menu);

        return menu;
    }

    private JMenu createSetsMenu() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, ScriptingWindowController.getString("SCW_MENU_SETS"));

        JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_MENU_SAVE_SET"));
        item.addActionListener(e -> {
            String setName = JOptionPane.showInputDialog(parentFrame,
                    ScriptingWindowController.getString("SCW_SAVE_SET_MSG"),
                    ScriptingWindowController.getString("SCW_MENU_SAVE_SET"),
                    JOptionPane.QUESTION_MESSAGE);

            if (setName == null) {
                return;
            }

            try {
                ScriptSet.saveSet(new File(controller.getScriptsDirectory(), setName + ".set"),
                        setName, controller.getQuickScriptFilenames());
                // Refresh the menu
                // This would be better with a proper observer pattern
                updateSetsMenu(menu);
            } catch (IOException ex) {
                controller.logErrorRB(ex, "SCW_ERROR_SAVE_SCRIPT");
            }
        });
        menu.add(item);
        menu.addSeparator();

        updateSetsMenu(menu);

        return menu;
    }

    private void updateSetsMenu(JMenu menu) {
        // Remove existing set items
        for (int i = menu.getItemCount() - 1; i >= 2; i--) {
            menu.remove(i);
        }

        File scriptsDir = controller.getScriptsDirectory();
        if (scriptsDir == null) {
            return;
        }

        var scripts = scriptsDir.listFiles(script -> script.getName().endsWith(".set"));
        if (scripts != null) {
            for (File script : scripts) {
                ScriptSet set = new ScriptSet(script);

                JMenuItem setMenuItem = new JMenuItem();
                setMenuItem.setText(set.getTitle());
                setMenuItem.putClientProperty("set", set);
                setMenuItem.addActionListener(e -> {
                    JMenuItem source = (JMenuItem) e.getSource();
                    ScriptSet selectedSet = (ScriptSet) source.getClientProperty("set");

                    // Load the set
                    controller.loadScriptSet(selectedSet);
                });

                menu.add(setMenuItem);
            }
        }
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, ScriptingWindowController.getString("SCW_MENU_HELP"));

        JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingWindowController.getString("SCW_MENU_JAVADOC"));
        item.addActionListener(e -> {
            try {
                Help.showJavadoc();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentFrame, ex.getLocalizedMessage(),
                        ScriptingWindowController.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                controller.logErrorRB(ex, "SCW_ERROR_SHOW_JAVADOC");
            }
        });
        menu.add(item);

        return menu;
    }
}