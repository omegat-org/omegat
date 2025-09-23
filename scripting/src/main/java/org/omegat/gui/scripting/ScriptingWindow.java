/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.omegat.gui.scripting.ui.QuickScriptButtonsPanel;
import org.omegat.gui.scripting.ui.ScriptListPanel;
import org.omegat.gui.scripting.ui.ScriptingMenuFactory;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Scripting window
 *
 * @author Briac Pilpre
 * @author Alex Buloichik
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 */
public class ScriptingWindow {

    final JFrame frame;
    final ScriptingWindowController controller;

    // UI components
    private ScriptListPanel scriptListPanel;
    private JEditorPane txtResult;
    private IScriptEditor txtScriptEditor;

    // button components
    JButton btnRunScript = new JButton();
    JButton btnCancelScript = new JButton();

    // State
    ScriptItem currentScriptItem;
    final JMenuItem[] quickMenus = new JMenuItem[QuickScriptManager.NUMBERS_OF_QUICK_SCRIPTS];

    public ScriptingWindow(ScriptingWindowController controller) {
        this.controller = controller;
        frame = new JFrame(ScriptingWindowController.getString("SCW_TITLE"));
        StaticUIUtils.setWindowIcon(frame);
        StaticUIUtils.setEscapeClosable(frame);
        initWindowLayout();
        initMenus();
        addScriptCommandToOmegaT();
        controller.initQuickScriptManager(quickMenus);

        addRunShortcutToOmegaT();
    }

    private void initWindowLayout() {
        // set default size and position
        frame.setBounds(50, 80, 1150, 650);
        StaticUIUtils.persistGeometry(frame, Preferences.SCRIPTWINDOW_GEOMETRY_PREFIX);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        // Create script list panel
        scriptListPanel = new ScriptListPanel();
        scriptListPanel.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                onListSelectionChanged();
            }
        });

        // Create result panel
        txtResult = new JEditorPane();
        JScrollPane scrollPaneResults = new JScrollPane(txtResult);

        // Create script editor
        txtScriptEditor = getScriptEditor();
        txtScriptEditor.initLayout(this);

        // Create split pane
        JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, txtScriptEditor.getPanel(),
                scrollPaneResults);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setDividerLocation(430);
        Dimension minimumSize1 = new Dimension(100, 50);
        scrollPaneResults.setMinimumSize(minimumSize1);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scriptListPanel, splitPane1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);

        Dimension minimumSize = new Dimension(100, 50);
        scriptListPanel.setMinimumSize(minimumSize);
        scrollPaneResults.setMinimumSize(minimumSize);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        // Add bottom panel with buttoons
        JPanel panelSouth = new JPanel();
        FlowLayout flPanelSouth = (FlowLayout) panelSouth.getLayout();
        flPanelSouth.setAlignment(FlowLayout.LEFT);
        frame.getContentPane().add(panelSouth, BorderLayout.SOUTH);

        // Add run and cancel buttons
        setupRunButtons(panelSouth);

        // Add quick script buttons panel
        QuickScriptButtonsPanel quickScriptButtonsPanel = new QuickScriptButtonsPanel(controller);
        panelSouth.add(quickScriptButtonsPanel);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void initMenus() {
        ScriptingMenuFactory menuFactory = new ScriptingMenuFactory(
            controller,
            frame,
            // Script opener
            file -> {
                currentScriptItem = new ScriptItem(file);
                displayScriptItem(currentScriptItem);
            },
            // Script runner
            controller::runScript,
            // New script action
            () -> {
                currentScriptItem = null;
                scriptListPanel.clearSelection();
                txtScriptEditor.getTextArea().setText("");
                txtScriptEditor.getTextArea().setCaretPosition(0);
                txtScriptEditor.getTextArea().grabFocus();
            },
            // Script saver
            file -> {
                try {
                    currentScriptItem = new ScriptItem(file);
                    currentScriptItem.setText(txtScriptEditor.getTextArea().getText());
                    controller.logResultRB("SCW_SAVED_SCRIPT", file.getName());
                } catch (IOException exception) {
                    controller.logResultRB(exception, "SCW_CANNOT_SAVE_SCRIPT", file.getName());
                }
            }
        );
        // Menu components
        JMenuBar menuBar = menuFactory.createMenuBar();
        frame.setJMenuBar(menuBar);
    }

    private void setupRunButtons(JPanel panel) {
        Mnemonics.setLocalizedText(btnRunScript, ScriptingWindowController.getString("SCW_RUN_SCRIPT"));
        btnRunScript.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRunScript.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(btnRunScript);

        Mnemonics.setLocalizedText(btnCancelScript, ScriptingWindowController.getString("SCW_CANCEL_SCRIPT"));
        btnCancelScript.setToolTipText(ScriptingWindowController.getString("SCW_CANCEL_BUTTON_TOOLTIP"));
        btnCancelScript.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancelScript.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(btnCancelScript);
    }

    private void onListSelectionChanged() {
        if (scriptListPanel.isSelectionEmpty()) {
            return;
        }
        currentScriptItem = scriptListPanel.getSelectedValue();
        displayScriptItem(currentScriptItem);
    }

    /** Display the content of a script item in the Script Editor area. */
    public void displayScriptItem(ScriptItem item) {
        try {
            txtScriptEditor.setHighlighting(FilenameUtils
                    .getExtension(item.getFileName().toLowerCase(Locale.ENGLISH)));
            txtScriptEditor.getTextArea().setText(item.getText());
            txtScriptEditor.getTextArea().setCaretPosition(0);
        } catch (IOException ex) {
            controller.logResultRB("SCW_CANNOT_READ_SCRIPT");
        }
    }

    public void setScriptItems(Collection<ScriptItem> items) {
        scriptListPanel.setScriptItems(items);
    }

    private void addScriptCommandToOmegaT() {
        JMenu toolsMenu = Core.getMainWindow().getMainMenu().getToolsMenu();
        toolsMenu.add(new JSeparator());

        JMenuItem scriptMenu = new JMenuItem();
        Mnemonics.setLocalizedText(scriptMenu, ScriptingWindowController.getString("TF_MENU_TOOLS_SCRIPTING"));
        scriptMenu.addActionListener(e -> frame.setVisible(true));

        toolsMenu.add(scriptMenu);

        for (int i = 0; i < QuickScriptManager.NUMBERS_OF_QUICK_SCRIPTS; i++) {
            JMenuItem menuItem = new JMenuItem();
            quickMenus[i] = menuItem;
            quickMenus[i].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (i + 1)));
            toolsMenu.add(menuItem);
        }
    }

    /**
     * Print log text to the Scripting Window's console area. A trailing line
     * break will be added if the parameter newLine is true.
     */
    public void logResultToWindow(String s, boolean newLine) {
        Document doc = txtResult.getDocument();
        try {
            doc.insertString(doc.getLength(), s + (newLine ? "\n" : ""), null);
        } catch (BadLocationException e1) {
            /* empty */
        }
    }

    private void addRunShortcutToOmegaT() {
        JRootPane appliRootPane = Core.getMainWindow().getApplicationFrame().getRootPane();
        appliRootPane
                .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK, false),
                        "RUN_CURRENT_SCRIPT");
        appliRootPane.getActionMap().put("RUN_CURRENT_SCRIPT", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.runScript();
            }
        });
    }

    public @NotNull String getText() {
        return txtScriptEditor.getTextArea().getText();
    }

    public void setText(String scriptString) {
        txtScriptEditor.getTextArea().setText(scriptString);
    }

    private IScriptEditor getScriptEditor() {

        try {
            Class<?> richScriptEditorClass = Class.forName("org.omegat.gui.scripting.RichScriptEditor");
            return (IScriptEditor) richScriptEditorClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            // RichScriptEditor not present, fallback to the standard editor
            controller.logResult("RichScriptEditor not present, fallback to the standard editor");
        } catch (Exception e) {
            controller.logResult("Error loading RichScriptEditor: ", e);
        }

        return new StandardScriptEditor();
    }
}
