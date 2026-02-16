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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import javax.script.ScriptEngineFactory;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.gui.scripting.runner.AbstractScriptRunner;
import org.omegat.gui.scripting.ui.AbstractScriptEditor;
import org.omegat.gui.scripting.ui.StandardScriptEditor;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.help.Help;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DesktopWrapper;
import org.omegat.util.gui.OSXIntegration;
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

    private final JFrame frame;

    public Frame getParent() {
        return frame;
    }

    public void addContent(Component content) {
        frame.getContentPane().add(content);
    }

    public void dispose() {
        if (frame != null) {
            frame.dispose();
        }
    }

    public ScriptingWindow() {
        this(new JFrame(ScriptingUtils.getBundleString("SCW_TITLE")));
    }

    public ScriptingWindow(JFrame frame) {
        this.frame = frame;
        setScriptsDirectory(StaticUtils.getUserScriptsDir());

        if (frame != null) {
            StaticUIUtils.setWindowIcon(frame);
            StaticUIUtils.setEscapeClosable(frame);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    monitor.stop();
                }
            });
            initWindowLayout();
            addScriptCommandToOmegaT();
            addRunShortcutToOmegaT();
        }

        updateQuickScripts();

        monitor = new ScriptsMonitor(this);
        if (scriptsDirectory != null) {
            monitor.start(scriptsDirectory);
        }

        logResult(listScriptEngines());

    }

    @VisibleForTesting
    public File getScriptsFolder() {
        return scriptsDirectory;
    }

    private String listScriptEngines() {
        StringBuilder sb = new StringBuilder(ScriptingUtils.getBundleString("SCW_LIST_ENGINES") + "\n");
        for (ScriptEngineFactory engine : ScriptRunner.getManager().getEngineFactories()) {
            sb.append(" - ");
            sb.append(engine.getEngineName());
            sb.append(" ");
            sb.append(engine.getLanguageName());
            sb.append(" v.");
            sb.append(engine.getLanguageVersion());
            sb.append(" (").append(ScriptingUtils.getBundleString("SCW_EXTENSIONS")).append(" ");
            boolean hasMore = false;
            for (String ext : engine.getExtensions()) {
                if (hasMore) {
                    sb.append(", ");
                }
                sb.append(ext);
                hasMore = true;
            }
            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }

    private void addScriptCommandToOmegaT() {
        JMenu toolsMenu = Core.getMainWindow().getMainMenu().getToolsMenu();
        toolsMenu.add(new JSeparator());

        JMenuItem scriptMenu = new JMenuItem();
        Mnemonics.setLocalizedText(scriptMenu, ScriptingUtils.getBundleString("TF_MENU_TOOLS_SCRIPTING"));
        scriptMenu.addActionListener(e -> frame.setVisible(true));

        toolsMenu.add(scriptMenu);

        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            JMenuItem menuItem = new JMenuItem();
            quickMenus[i] = menuItem;

            unsetQuickScriptMenu(i);

            // Since the script is run while editing a segment, the shortcut
            // should not interfere with the segment content, so we set it
            // to a Function key.
            quickMenus[i].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (i + 1)));

            toolsMenu.add(menuItem);
        }
    }

    private int scriptKey(int i) {
        return i + 1;
    }

    private void unsetQuickScriptMenu(int index) {
        quickScripts[index] = null;
        removeAllQuickScriptActionListenersFrom(quickMenus[index]);

        if (quickMenus[index] == null) {
            return;
        }

        quickMenus[index].setEnabled(false);
        Mnemonics.setLocalizedText(quickMenus[index],
                "&" + scriptKey(index) + " - " + ScriptingUtils.getBundleString("SCW_SCRIPTS_NONE"));
    }

    private void setQuickScriptMenu(ScriptItem scriptItem, int index) {
        quickScripts[index] = scriptItem.getFileName();

        removeAllQuickScriptActionListenersFrom(quickMenus[index]);
        quickMenus[index].addActionListener(new QuickScriptActionListener(index));

        // Since the script is run while editing a segment, the shortcut should
        // not interfere with the segment content, so we set it to a Function key.
        quickMenus[index].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (index + 1)));
        quickMenus[index].setEnabled(true);
        if ("".equals(scriptItem.getDescription())) {
            quickMenus[index].setToolTipText(scriptItem.getDescription());
        }

        Mnemonics.setLocalizedText(quickMenus[index],
                "&" + scriptKey(index) + " - " + scriptItem.getScriptName());
    }

    private void removeAllQuickScriptActionListenersFrom(JMenuItem menu) {
        if (menu == null) {
            return;
        }

        for (ActionListener l : menu.getActionListeners()) {
            if (l instanceof QuickScriptActionListener) {
                menu.removeActionListener(l);
            }
        }
    }

    private class QuickScriptActionListener implements ActionListener {

        private final int index;

        QuickScriptActionListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runQuickScript(index);
        }
    }

    private void runQuickScript(int index) {

        if (quickScripts[index] == null) {
            logResult(ScriptingUtils.getBundleString("SCW_NO_SCRIPT_SELECTED"));
            return;
        }

        logResult(StringUtil.format(ScriptingUtils.getBundleString("SCW_QUICK_RUN"), (index + 1)));
        ScriptItem scriptFile = new ScriptItem(new File(scriptsDirectory, quickScripts[index]));

        executeScript(scriptFile);
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
                runScript();
            }
        });
    }

    // CHECKSTYLE:OFF
    private void initWindowLayout() {
        // set default size and position
        frame.setBounds(50, 80, 1150, 650);
        StaticUIUtils.persistGeometry(frame, Preferences.SCRIPTWINDOW_GEOMETRY_PREFIX);

        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        scriptList = new JList<>();
        JScrollPane scrollPaneList = new JScrollPane(scriptList);

        scriptList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                onListSelectionChanged();
            }
        });

        scriptList.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                ListModel<ScriptItem> lm = scriptList.getModel();
                int index = scriptList.locationToIndex(e.getPoint());
                if (index > -1) {
                    scriptList.setToolTipText(lm.getElementAt(index).getFileName());
                }
            }

        });

        txtResult = new JEditorPane();
        JScrollPane scrollPaneResults = new JScrollPane(txtResult);

        // m_txtScriptEditor = new StandardScriptEditor();
        txtScriptEditor = getScriptEditor();

        txtScriptEditor.initLayout(this);

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, txtScriptEditor.getPanel(),
                scrollPaneResults);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setDividerLocation(430);
        Dimension minimumSize1 = new Dimension(100, 50);
        // scrollPaneEditor.setMinimumSize(minimumSize1);
        scrollPaneResults.setMinimumSize(minimumSize1);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneList, splitPane1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPaneList.setMinimumSize(minimumSize);
        scrollPaneResults.setMinimumSize(minimumSize);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel();
        FlowLayout flPanelSouth = (FlowLayout) panelSouth.getLayout();
        flPanelSouth.setAlignment(FlowLayout.LEFT);
        frame.getContentPane().add(panelSouth, BorderLayout.SOUTH);
        setupRunButtons(panelSouth);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setJMenuBar(createMenuBar());
    }
    // CHECKSTYLE:ON

    private AbstractScriptEditor getScriptEditor() {
        try {
            return new org.omegat.gui.scripting.ui.RichScriptEditor();
        } catch (Exception e) {
            logResult("Error loading RichScriptEditor, fallback to the standard editor: ", e);
            return new StandardScriptEditor();
        }
    }

    private class QuickScriptUpdater implements ActionListener {
        int index;
        int scriptKey;

        QuickScriptUpdater(int index) {
            this.scriptKey = scriptKey(index);
            this.index = index;
        }

        public void updateQuickScript(ScriptItem scriptItem) {
            Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, scriptItem.getFileName());
            quickScriptButtons[index].setToolTipText(scriptItem.getToolTip());
            quickScriptButtons[index].setText("<" + scriptKey + ">");

            setQuickScriptMenu(scriptItem, index);

            logResult(StringUtil.format(ScriptingUtils.getBundleString("SCW_SAVE_QUICK_SCRIPT"), scriptItem, scriptKey));
        }

        public void updateQuickScript() {
            ScriptItem scriptItem = scriptList.getSelectedValue();
            updateQuickScript(scriptItem);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            updateQuickScript();
        }
    }

    private void setupRunButtons(JPanel panel) {
        JButton btnRunScript = new JButton();
        Mnemonics.setLocalizedText(btnRunScript, ScriptingUtils.getBundleString("SCW_RUN_SCRIPT"));
        btnRunScript.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRunScript.setHorizontalAlignment(SwingConstants.LEFT);
        btnRunScript.addActionListener(a -> runScript());
        panel.add(btnRunScript);

        JButton btnCancelScript = new JButton();
        Mnemonics.setLocalizedText(btnCancelScript, ScriptingUtils.getBundleString("SCW_CANCEL_SCRIPT"));
        btnCancelScript.setToolTipText(ScriptingUtils.getBundleString("SCW_CANCEL_BUTTON_TOOLTIP"));
        btnCancelScript.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancelScript.setHorizontalAlignment(SwingConstants.LEFT);
        btnCancelScript.addActionListener(e -> cancelCurrentScript());
        panel.add(btnCancelScript);

        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            final int index = i;
            final int scriptKey = scriptKey(index);
            quickScriptButtons[i] = new JButton(String.valueOf(scriptKey));

            // Run a script from the quick button bar
            quickScriptButtons[i].addActionListener(a -> {
                if (Preferences.existsPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey)) {
                    runQuickScript(index);
                } else {
                    logResultRB("SCW_NO_SCRIPT_BOUND", scriptKey);
                }
            });

            JPopupMenu quickScriptPopup = new JPopupMenu();

            // Add a script to the quick script button bar
            final JMenuItem addQuickScriptMenuItem = new JMenuItem(ScriptingUtils.getBundleString("SCW_ADD_SCRIPT"));
            addQuickScriptMenuItem.addActionListener(new QuickScriptUpdater(index));
            quickScriptPopup.add(addQuickScriptMenuItem);

            // Remove a script from the button bar
            final JMenuItem removeQuickScriptMenuItem = new JMenuItem(
                    ScriptingUtils.getBundleString("SCW_REMOVE_SCRIPT"));
            removeQuickScriptMenuItem.addActionListener(evt -> {
                String scriptName = Preferences
                        .getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "(unknown)");
                logResult(StringUtil.format(ScriptingUtils.getBundleString("SCW_REMOVED_QUICK_SCRIPT"), scriptName,
                        scriptKey));
                Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "");
                quickScriptButtons[index].setToolTipText(ScriptingUtils.getBundleString("SCW_NO_SCRIPT_SET"));
                quickScriptButtons[index].setText(" " + scriptKey + " ");

                unsetQuickScriptMenu(index);
            });
            quickScriptPopup.add(removeQuickScriptMenuItem);

            quickScriptPopup.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // Disable add a script command if script selection empty
                    addQuickScriptMenuItem.setEnabled(!scriptList.isSelectionEmpty());

                    // Disable remove a script command if the quick run button
                    // is not bounded
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

            quickScriptButtons[i].setComponentPopupMenu(quickScriptPopup);

            panel.add(quickScriptButtons[i]);
        }
    }

    private void runScript() {
        txtResult.setText("");

        // Execute a script with a corresponding source file. If the source has
        // changed, we use the text area as source code, but still consider the
        // script file as the "base" (for determining the engine to use and to
        // fetch the localized resources).
        if (currentScriptItem != null && currentScriptItem.getFile() != null) {
            if (!currentScriptItem.getFile().canRead()) {
                logResult(ScriptingUtils.getBundleString("SCW_CANNOT_READ_SCRIPT"));
                return;
            }

            logResult(StringUtil.format(ScriptingUtils.getBundleString("SCW_RUNNING_SCRIPT"),
                    currentScriptItem.getFile().getAbsolutePath()));

            String scriptString = txtScriptEditor.getTextArea().getText();
            if (scriptString.trim().isEmpty()) {
                try {
                    scriptString = currentScriptItem.getText();
                    txtScriptEditor.getTextArea().setText(scriptString);
                } catch (IOException e) {
                    ScriptingUtils.getLogger().atError().setCause(e).setMessageRB("SCW_SCRIPT_LOAD_ERROR")
                            .addArgument(currentScriptItem.getFileName()).log();
                }
            }

            executeScript(currentScriptItem);
        } else {
            // No file is found for this script, it is executed as standalone.
            logResult(StringUtil.format(ScriptingUtils.getBundleString("SCW_RUNNING_EDITOR_SCRIPT")));
            executeScript(new ScriptItem(txtScriptEditor.getTextArea().getText()));
        }

    }

    public void executeScript(ScriptItem scriptItem) {
        executeScript(scriptItem, Collections.emptyMap());
    }

    public void executeScript(ScriptItem scriptItem, Map<String, Object> bindings) {
        executeScript(scriptItem, bindings, true);
    }

    public void executeScript(ScriptItem scriptItem, Map<String, Object> bindings, boolean cancelQueue) {
        executeScripts(Collections.singletonList(scriptItem), bindings, cancelQueue);
    }

    /**
     * Execute scripts sequentially to make sure they don't interrupt each
     * other.
     *
     * @param scriptItems
     *            List of script to execute.
     * @param bindings
     *            Additional bindings to pass to the executed script.
     * @param cancelQueue
     *            If true, the run queue is cleared before running the scripts.
     */
    public void executeScripts(final List<ScriptItem> scriptItems, final Map<String, Object> bindings,
                               boolean cancelQueue) {
        if (cancelQueue) {
            cancelScriptQueue();
        }

        for (ScriptItem scriptItem : scriptItems) {
            try {
                String scriptString = scriptItem.getText();
                queuedWorkers.add(createScriptWorker(scriptString, scriptItem, bindings));
            } catch (IOException e) {
                // TODO: Do we really want to handle the exception here, like
                // this?
                // This method can be called in instances when the Scripting
                // Window is not visible, so it might make more sense to let the
                // caller handle the exception.
                logResultRB(e, "SCW_SCRIPT_LOAD_ERROR", scriptItem.getFileName());
            }
        }

        executeScriptWorkers();
    }

    public void executeScripts(final List<ScriptItem> scriptItems) {
        executeScripts(scriptItems, Collections.emptyMap());
    }

    public void executeScripts(final List<ScriptItem> scriptItems, final Map<String, Object> bindings) {
        executeScripts(scriptItems, bindings, false);
    }

    private void executeScriptWorkers() {
        final ScriptWorker scriptWorker = queuedWorkers.poll();

        if (scriptWorker == null) {
            return;
        }

        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (!"state".equals(event.getPropertyName())) {
                    return;
                }

                if (SwingWorker.StateValue.DONE == event.getNewValue()) {
                    scriptWorker.removePropertyChangeListener(this);
                    executeScriptWorkers();
                }
            }
        };
        scriptWorker.addPropertyChangeListener(propertyChangeListener);
        currentScriptWorker = scriptWorker;
        scriptWorker.execute();
    }

    public ScriptWorker createScriptWorker(String scriptString, ScriptItem scriptItem,
            Map<String, Object> additionalBindings) {

        if (!scriptString.endsWith("\n")) {
            scriptString += "\n";
        }

        Map<String, Object> bindings = new HashMap<>(additionalBindings);
        bindings.put(ScriptRunner.VAR_CONSOLE, new IScriptLogger() {
            @Override
            public void print(Object o) {
                logResult(o.toString(), false);
            }

            @Override
            public void println(Object o) {
                logResult(o.toString(), true);
            }

            @Override
            public void clear() {
                txtResult.setText("");
            }
        });

        return new ScriptWorker(this, scriptString, scriptItem, bindings);
    }

    /**
     * Cancel the currently running script, if any.
     * <p>
     * <b>Note!</b> Canceling the worker does not do anything in and of itself.
     * The running script must poll for interruption with e.g.
     * {@link java.lang.Thread#interrupted()}.
     *
     * @see <a href="http://stackoverflow.com/a/24875881/448068">StackOverflow
     *      answer about interrupting scripts</a>
     */
    public void cancelCurrentScript() {
        if (currentScriptWorker != null) {
            currentScriptWorker.cancel(true);
        }
    }

    public void cancelScriptQueue() {
        cancelCurrentScript();
        queuedWorkers.clear();
    }

    void logResultRB(Throwable t, String key, Object... args) {
        logResultToWindow(MessageFormat.format(ScriptingUtils.getBundleString(key), args) + "\n" + t.getMessage(), true);
        ScriptingUtils.getLogger().atError().setCause(t).setMessageRB(key).addArgument(args).log();
    }

    void logResultRB(String key, Object... args) {
        logResultToWindow(MessageFormat.format(ScriptingUtils.getBundleString(key), args), true);
        ScriptingUtils.getLogger().atError().setMessageRB(key).addArgument(args).log();
    }

    void logResult(String s, Throwable t) {
        logResultToWindow(s + "\n" + t.getMessage(), true);
        ScriptingUtils.getLogger().atInfo().setCause(t).setMessage(s).log();
    }

    void logResult(String s) {
        logResult(s, true);
    }

    private void logResult(String s, boolean newLine) {
        if (frame != null) {
            logResultToWindow(s, newLine);
        }
        ScriptingUtils.getLogger().atInfo().setMessage(s).log();
    }

    /**
     * Print log text to the Scripting Window's console area. A trailing line
     * break will be added if the parameter newLine is true.
     */
    private void logResultToWindow(String s, boolean newLine) {
        Document doc = txtResult.getDocument();
        try {
            doc.insertString(doc.getLength(), s + (newLine ? "\n" : ""), null);
        } catch (BadLocationException e1) {
            /* empty */
        }
    }

    private void setScriptsDirectory(String scriptsDir) {
        File dir;
        try {
            dir = new File(scriptsDir).getCanonicalFile();
        } catch (IOException ex) {
            dir = new File(scriptsDir);
        }

        if (!dir.isDirectory()) {
            updateQuickScripts();
            return;
        }
        scriptsDirectory = dir;
        Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, scriptsDir);
        if (frame != null) {
            OSXIntegration.setProxyIcon(frame.getRootPane(), scriptsDirectory);
        }

        if (monitor != null) {
            monitor.stop();
            monitor.start(scriptsDirectory);
        }
    }

    private void updateQuickScripts() {
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            int key = scriptKey(i);
            String scriptName = Preferences.getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + key,
                    null);

            if (scriptsDirectory != null && !StringUtil.isEmpty(scriptName)) {
                setQuickScriptMenu(new ScriptItem(new File(scriptsDirectory, scriptName)), i);
                quickScriptButtons[i].setToolTipText(scriptName);
                quickScriptButtons[i].setText("<" + key + ">");
            } else {
                unsetQuickScriptMenu(i);

                if (quickScriptButtons.length < i || quickScriptButtons[i] == null) {
                    return;
                }

                quickScriptButtons[i].setToolTipText(ScriptingUtils.getBundleString("SCW_NO_SCRIPT_SET"));
                quickScriptButtons[i].setText(String.valueOf(key));
            }
        }
    }

    void setScriptItems(Collection<ScriptItem> items) {
        scriptList.setListData(items.toArray(new ScriptItem[0]));
    }

    private void onListSelectionChanged() {
        if (scriptList.isSelectionEmpty()) {
            return;
        }
        currentScriptItem = scriptList.getSelectedValue();
        displayScriptItem();
    }

    /** Display the content of a script item in the Script Editor area. */
    private void displayScriptItem() {
        try {
            txtScriptEditor.setHighlighting(FilenameUtils
                    .getExtension(currentScriptItem.getFileName().toLowerCase(Locale.ENGLISH)));
            txtScriptEditor.getTextArea().setText(currentScriptItem.getText());
            txtScriptEditor.getTextArea().setCaretPosition(0);
        } catch (IOException ex) {
            logResult(ScriptingUtils.getBundleString("SCW_CANNOT_READ_SCRIPT"));
        }
    }

    public String getSelectedText() {
        return txtScriptEditor.getTextArea().getSelectedText();
    }

    // Menu Actions
    private class OpenScriptAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            // If a project is opened, set the file chooser to the project root
            // instead of the default script path
            File openFileDir = Core.getProject().isProjectLoaded()
                    ? Core.getProject().getProjectProperties().getProjectRootDir()
                    : scriptsDirectory;

            JFileChooser chooser = new JFileChooser(openFileDir);
            chooser.setDialogTitle(ScriptingUtils.getBundleString("SCW_SCRIPTS_OPEN_SCRIPT_TITLE"));
            chooser.setDialogTitle("Select a Script File");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                // we should write the result into the directory text field
                // File file = chooser.getSelectedFile();
                // setScriptsDirectory(file);
                currentScriptItem = new ScriptItem(chooser.getSelectedFile());
                displayScriptItem();
            }
        }
    }

    private class NewScriptAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            currentScriptItem = null;
            scriptList.clearSelection();
            // TODO Check if the current script needs saving before clearing the
            // editor.
            txtScriptEditor.getTextArea().setText("");
            txtScriptEditor.getTextArea().setCaretPosition(0);
            // Avoid selecting scripts in the list after hitting Ctrl+N
            txtScriptEditor.getTextArea().grabFocus();
        }
    }

    private class RunScriptAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            runScript();
        }
    }

    private class SaveScriptAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (currentScriptItem == null || currentScriptItem.getFile() == null) {
                JFileChooser chooser = new JFileChooser(scriptsDirectory);
                chooser.setDialogTitle(ScriptingUtils.getBundleString("SCW_SAVE_SCRIPT"));
                int result = chooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    setCurrentScriptItem(chooser.getSelectedFile());
                }
            }
        }

        private void setCurrentScriptItem(File selectedFile) {
            try {
                currentScriptItem = new ScriptItem(selectedFile);
                currentScriptItem.setText(txtScriptEditor.getTextArea().getText());
                logResultRB("SCW_SAVE_OK", Objects.requireNonNull(currentScriptItem.getFile()).getAbsolutePath());
            } catch (IOException ex) {
                logResultRB(ex, "SCW_SAVE_ERROR");
            }
        }

    }

    private class SelectScriptFolderAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(scriptsDirectory);
            chooser.setDialogTitle(ScriptingUtils.getBundleString("SCW_SCRIPTS_FOLDER_CHOOSE_TITLE"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                // we should write the result into the directory text field
                File file = chooser.getSelectedFile();
                setScriptsDirectory(file.getPath());
            }
        }
    }

    private class ExploreScriptFolderAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                // Normalize file name in case it is displayed
                scriptsDirectory = scriptsDirectory.getCanonicalFile();
            } catch (Exception ex) {
                // Ignore
            }
            if (!scriptsDirectory.exists()) {
                Core.getMainWindow().showStatusMessageRB("LFC_ERROR_FILE_DOESNT_EXIST", scriptsDirectory);
                return;
            }
            try {
                DesktopWrapper.open(scriptsDirectory);
            } catch (Exception ex) {
                ScriptingUtils.getLogger().atError().setCause(ex).setMessageRB("RPF_ERROR").log();
                Core.getMainWindow().displayErrorRB(ex, "RPF_ERROR");
            }
        }
    }

    private JMenuBar createMenuBar() {

        menuBar = new JMenuBar();
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, ScriptingUtils.getBundleString("SCW_MENU_TITLE"));

        JMenuItem item;

        // https://sourceforge.net/p/omegat/feature-requests/1314/
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_LOAD_FILE"));
        item.addActionListener(new OpenScriptAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_NEW_SCRIPT"));
        item.addActionListener(new NewScriptAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_SAVE_SCRIPT"));
        item.addActionListener(new SaveScriptAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_RUN_SCRIPT"));
        item.addActionListener(new RunScriptAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_MENU_SET_SCRIPTS_FOLDER"));
        item.addActionListener(new SelectScriptFolderAction());
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_MENU_ACCESS_FOLDER"));
        item.addActionListener(new ExploreScriptFolderAction());
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_MENU_CLOSE"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        item.addActionListener(e -> {
            frame.setVisible(false);
            frame.dispose();
        });
        menu.add(item);

        PropertiesShortcuts.getMainMenuShortcuts().bindKeyStrokes(menu);

        menuBar.add(menu);

        // Edit Menu
        txtScriptEditor.enhanceMenu(menuBar);

        buildSetsMenu(menuBar);

        menu = new JMenu();
        Mnemonics.setLocalizedText(menu, ScriptingUtils.getBundleString("SCW_MENU_HELP"));
        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_MENU_JAVADOC"));
        item.addActionListener(e -> {
            try {
                Help.showJavadoc();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage(),
                        ScriptingUtils.getBundleString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                ScriptingUtils.getLogger().atInfo().setCause(ex).setMessage("Failed to open Javadoc").log();
            }
        });
        menu.add(item);
        menuBar.add(menu);

        return menuBar;
    }

    private class SaveSetAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String setName = JOptionPane.showInputDialog(frame, ScriptingUtils.getBundleString("SCW_SAVE_SET_MSG"),
                    ScriptingUtils.getBundleString("SCW_MENU_SAVE_SET"), JOptionPane.QUESTION_MESSAGE);

            if (setName == null) {
                return;
            }

            try {
                ScriptSet.saveSet(new File(scriptsDirectory, setName + ".set"), setName, quickScripts);
                buildSetsMenu(menuBar);
            } catch (IOException e1) {
                ScriptingUtils.getLogger().atError().setCause(e1).setMessage("Failed to save script set").log();
            }
        }
    }

    private class LoadSetAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            JMenuItem source = (JMenuItem) e.getSource();
            ScriptSet set = (ScriptSet) source.getClientProperty("set");

            // Unset all previous scripts
            for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
                Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey(i), "");
                unsetQuickScriptMenu(i);
            }

            for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
                ScriptItem si = set.getScriptItem(scriptKey(i));

                if (si != null) {
                    Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey(i),
                            si.getFileName());
                    new QuickScriptUpdater(i).updateQuickScript(si);
                    updateQuickScripts();
                }
            }

        }
    }

    protected void buildSetsMenu(JMenuBar mb) {

        setsMenu.removeAll();

        Mnemonics.setLocalizedText(setsMenu, ScriptingUtils.getBundleString("SCW_MENU_SETS"));

        JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, ScriptingUtils.getBundleString("SCW_MENU_SAVE_SET"));
        item.addActionListener(new SaveSetAction());
        setsMenu.add(item);
        setsMenu.addSeparator();

        if (scriptsDirectory == null) {
            return;
        }
        var scripts = scriptsDirectory.listFiles(script -> script.getName().endsWith(".set"));
        if (scripts != null) {
            for (File script : scripts) {
                ScriptSet set = new ScriptSet(script);

                JMenuItem setMenuItem = new JMenuItem();
                setMenuItem.setText(set.getTitle());
                setMenuItem.putClientProperty("set", set);
                setMenuItem.addActionListener(new LoadSetAction());

                setsMenu.add(setMenuItem);
            }
        }

        mb.add(setsMenu);
    }

    protected static final int NUMBERS_OF_QUICK_SCRIPTS = 12;

    private JList<ScriptItem> scriptList;
    private JEditorPane txtResult;
    private AbstractScriptEditor txtScriptEditor;
    private JMenuBar menuBar;

    private ScriptWorker currentScriptWorker;
    private final Queue<ScriptWorker> queuedWorkers = new LinkedList<>();

    protected ScriptsMonitor monitor;

    private File scriptsDirectory;
    private ScriptItem currentScriptItem;

    private final JMenu setsMenu = new JMenu();

    private final String[] quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];
    private final JMenuItem[] quickMenus = new JMenuItem[NUMBERS_OF_QUICK_SCRIPTS];
    private final JButton[] quickScriptButtons = new JButton[NUMBERS_OF_QUICK_SCRIPTS];
}
