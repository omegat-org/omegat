/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang
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
package org.omegat.gui.scripting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.common.OmegaTIcons;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

/**
 * Scripting window
 * 
 * @author Briac Pilpre
 * @author Alex Buloichik
 * @author Yu Tang
 */
public class ScriptingWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    static ScriptingWindow window;

    // XXX Still needed ?
    /**
     * @deprecated
     */
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                window = new ScriptingWindow();
            }

            @Override
            public void onApplicationShutdown() {
            }
        });
    }

    @Override
    public void dispose() {
        savePreferences();
        monitor.stop();
        super.dispose();
    }

    /**
     * @deprecated
     */
    public static void unloadPlugins() {
        if (window != null) {
            window.dispose();
        }
    }

    public ScriptingWindow() {
        setTitle(OStrings.getString("SCW_TITLE"));

        OmegaTIcons.setIconImages(this);

        StaticUIUtils.setEscapeClosable(this);

        addScriptCommandToOmegaT();
        addRunShortcutToOmegaT();

        initWindowLayout();

        monitor = new ScriptsMonitor(this, m_scriptList, getAvailableScriptExtensions());
        monitor.start(m_scriptsDirectory);

        logResult(listScriptEngine().toString());

    }

    private List<String> getAvailableScriptExtensions() {
        ArrayList<String> extensions = new ArrayList<String>();
        for (ScriptEngineFactory engine : manager.getEngineFactories()) {
            for (String ext : engine.getExtensions()) {
                extensions.add(ext);
            }
        }

        return extensions;
    }

    private StringBuilder listScriptEngine() {
        StringBuilder sb = new StringBuilder(OStrings.getString("SCW_LIST_ENGINES") + "\n");
        for (ScriptEngineFactory engine : manager.getEngineFactories()) {
            sb.append(" - ");
            sb.append(engine.getEngineName());
            sb.append(" ");
            sb.append(engine.getLanguageName());
            sb.append(" v.");
            sb.append(engine.getLanguageVersion());
            sb.append(" (").append(OStrings.getString("SCW_EXTENSIONS")).append(" ");
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

        return sb;
    }

    private void addScriptCommandToOmegaT() {
        JMenu toolsMenu = Core.getMainWindow().getMainMenu().getToolsMenu();
        toolsMenu.add(new JSeparator());

        JMenuItem scriptMenu = new JMenuItem();
        Mnemonics.setLocalizedText(scriptMenu, OStrings.getString("TF_MENU_TOOLS_SCRIPTING"));
        scriptMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScriptingWindow.this.setVisible(true);
            }
        });

        toolsMenu.add(scriptMenu);

        File scriptDir = new File(getScriptsDir());
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            JMenuItem menuItem = new JMenuItem();
            m_quickMenus[i] = menuItem;

            String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey(i), null);

            if (scriptName != null || "".equals(scriptName)) {
                setQuickScriptMenu(new ScriptItem(new File(scriptDir, scriptName)), i);
            } else {
                unsetQuickScriptMenu(i);
            }

            // Since the script is run while editing a segment, the shortcut should not interfere
            // with the segment content, so we set it to a Function key.
            m_quickMenus[i].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (i + 1)));

            toolsMenu.add(menuItem);
        }
    }

    private int scriptKey(int i) {
        if (NUMBERS_OF_QUICK_SCRIPTS != 10) {
            return i + 1;
        }
        return i + 1 == NUMBERS_OF_QUICK_SCRIPTS ? 0 : i + 1;
    }

    private void unsetQuickScriptMenu(int index) {
        m_quickScripts[index] = null;
        removeAllQuickScriptActionListenersFrom(m_quickMenus[index]);
        m_quickMenus[index].setEnabled(false);
        Mnemonics.setLocalizedText(m_quickMenus[index], "&" + scriptKey(index) + " - " + OStrings.getString("SCW_SCRIPTS_NONE"));
    }

    private void setQuickScriptMenu(ScriptItem scriptItem, int index) {
        m_quickScripts[index] = scriptItem.getName();

        removeAllQuickScriptActionListenersFrom(m_quickMenus[index]);
        m_quickMenus[index].addActionListener(new QuickScriptActionListener(index));

        // Since the script is run while editing a segment, the shortcut should not interfere
        // with the segment content, so we set it to a Function key.
        m_quickMenus[index].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (index + 1)));
        m_quickMenus[index].setEnabled(true);
        if ("".equals(scriptItem.getDescription())) {
            m_quickMenus[index].setToolTipText(scriptItem.getDescription());
        }

        Mnemonics.setLocalizedText(m_quickMenus[index], "&" + scriptKey(index) + " - " + scriptItem.getScriptName());
    }

    private void removeAllQuickScriptActionListenersFrom(JMenuItem menu) {
        if (menu == null) {
            return;
        }

        for (ActionListener l: menu.getActionListeners()) {
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

        if (m_quickScripts[index] == null) {
            logResult(OStrings.getString("SCW_NO_SCRIPT_SELECTED"));
            return;
        }

        logResult(StaticUtils.format(OStrings.getString("SCW_QUICK_RUN"), (index + 1)));
        ScriptItem scriptFile = new ScriptItem(new File(m_scriptsDirectory, m_quickScripts[index]));

        executeScriptFile(scriptFile, true);
    }

    private void addRunShortcutToOmegaT() {
        JRootPane appliRootPane = Core.getMainWindow().getApplicationFrame().getRootPane();
        appliRootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK, false), "RUN_CURRENT_SCRIPT");
        appliRootPane.getActionMap().put("RUN_CURRENT_SCRIPT", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                runScript();
            }
        });
    }

    private void initWindowLayout() {
        loadPreferences();
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panelNorth = new JPanel();
        FlowLayout fl_panelNorth = (FlowLayout) panelNorth.getLayout();
        fl_panelNorth.setAlignment(FlowLayout.LEFT);
        getContentPane().add(panelNorth, BorderLayout.NORTH);

        setupDirectorySelection(panelNorth);

        m_scriptsDirectory = new File(m_txtScriptsDir.getText());

        m_scriptList = new JList();
        JScrollPane scrollPaneList = new JScrollPane(m_scriptList);

        m_scriptList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    onListSelectionChanged();
                }
            }
        });

        m_txtResult = new JEditorPane();
        JScrollPane scrollPaneResults = new JScrollPane(m_txtResult);

        m_txtScriptEditor = new JTextArea();
        //m_txtScriptEditor.setEditable(false);
        JPopupMenu editorPopUp = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(OStrings.getString("SCW_SAVE_SCRIPT"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    m_currentScriptItem.setText(m_txtScriptEditor.getText());
                    logResult(StaticUtils.format(OStrings.getString("SCW_SAVE_OK"),
                            m_currentScriptItem.getAbsolutePath()));
                } catch (IOException e) {
                    logResult(OStrings.getString("SCW_SAVE_ERROR"));
                    logResult(e.getMessage());
                }
            }
        });
        editorPopUp.add(menuItem);

        m_txtScriptEditor.setComponentPopupMenu(editorPopUp);
        m_txtScriptEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                m_txtScriptEditor.getFont().getSize()));
        JScrollPane scrollPaneEditor = new JScrollPane(m_txtScriptEditor);

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneEditor, scrollPaneResults);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setDividerLocation(430);
        Dimension minimumSize1 = new Dimension(100, 50);
        scrollPaneEditor.setMinimumSize(minimumSize1);
        scrollPaneResults.setMinimumSize(minimumSize1);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneList, splitPane1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPaneList.setMinimumSize(minimumSize);
        scrollPaneResults.setMinimumSize(minimumSize);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel();
        FlowLayout fl_panelSouth = (FlowLayout) panelSouth.getLayout();
        fl_panelSouth.setAlignment(FlowLayout.LEFT);
        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        setupRunButtons(panelSouth);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void setupDirectorySelection(JPanel panel) {
        JLabel lblScriptsDirectory = new JLabel(OStrings.getString("SCW_SCRIPTS_FOLDER"));
        panel.add(lblScriptsDirectory);

        m_txtScriptsDir = new JTextField();
        panel.add(m_txtScriptsDir);

        m_txtScriptsDir.setText(getScriptsDir());

        m_txtScriptsDir.setColumns(40);
        m_txtScriptsDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                directoryTextFieldActionPerformed(evt);
            }
        });

        JButton btnBrowse = new JButton();
        Mnemonics.setLocalizedText(btnBrowse, OStrings.getString("SCW_SCRIPTS_FOLDER_CHOOSE"));
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                directoryChooserButtonActionPerformed(evt);
            }
        });

        panel.add(btnBrowse);
    }

    private String getScriptsDir() {
        return Preferences.getPreferenceDefault(Preferences.SCRIPTS_DIRECTORY,
                new File(DEFAULT_SCRIPTS_DIR).getAbsolutePath());
    }

    private void setupRunButtons(JPanel panel) {
        m_btnRunScript = new JButton();
        Mnemonics.setLocalizedText(m_btnRunScript, OStrings.getString("SCW_RUN_SCRIPT"));
        m_btnRunScript.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_btnRunScript.setHorizontalAlignment(SwingConstants.LEFT);
        m_btnRunScript.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                runScript();
            }
        });
        panel.add(m_btnRunScript);

        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            final int index = i;
            final int scriptKey = scriptKey(index);
            m_quickScriptButtons[i] = new JButton("" + scriptKey + "");

            String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey, null);

            if (scriptName != null || "".equals(scriptName)) {
                m_quickScriptButtons[i].setToolTipText(scriptName);
                m_quickScriptButtons[i].setText("<" + scriptKey + ">");
            } else {
                m_quickScriptButtons[i].setToolTipText(OStrings.getString("SCW_NO_SCRIPT_SET"));
            }

            // Run a script from the quick button bar
            m_quickScriptButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (Preferences.existsPreference("scripts_quick_" + scriptKey)) {
                        runQuickScript(index);
                    } else {
                        logResult(StaticUtils.format(OStrings.getString("SCW_NO_SCRIPT_BOUND"), scriptKey));
                    }
                }
            });

            JPopupMenu quickScriptPopup = new JPopupMenu();

            // Add a script to the quick script button bar
            final JMenuItem addQuickScriptMenuItem = new JMenuItem(OStrings.getString("SCW_ADD_SCRIPT"));
            addQuickScriptMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    ScriptItem scriptItem = (ScriptItem) m_scriptList.getSelectedValue();
                    Preferences.setPreference("scripts_quick_" + scriptKey, scriptItem.getName());
                    m_quickScriptButtons[index].setToolTipText(scriptItem.getToolTip());
                    m_quickScriptButtons[index].setText("<" + scriptKey + ">");

                    setQuickScriptMenu(scriptItem, index);

                    logResult(StaticUtils.format(OStrings.getString("SCW_SAVE_QUICK_SCRIPT"), scriptItem, scriptKey));
                }
            });
            quickScriptPopup.add(addQuickScriptMenuItem);

            // Remove a script from the button bar
            final JMenuItem removeQuickScriptMenuItem = new JMenuItem(OStrings.getString("SCW_REMOVE_SCRIPT"));
            removeQuickScriptMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey, "(unknown)");
                    logResult(StaticUtils.format(OStrings.getString("SCW_REMOVED_QUICK_SCRIPT"), scriptName, scriptKey));
                    Preferences.setPreference("scripts_quick_" + scriptKey, "");
                    m_quickScriptButtons[index].setToolTipText(OStrings.getString("SCW_NO_SCRIPT_SET"));
                    m_quickScriptButtons[index].setText(" " + scriptKey + " ");

                    unsetQuickScriptMenu(index);
                }
            });
            quickScriptPopup.add(removeQuickScriptMenuItem);

            quickScriptPopup.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // Disable add a script command if script selection empty
                    addQuickScriptMenuItem.setEnabled(!m_scriptList.isSelectionEmpty());

                    // Disable remove a script command if the quick run button is not bounded
                    String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey, null);
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

            m_quickScriptButtons[i].setComponentPopupMenu(quickScriptPopup);

            panel.add(m_quickScriptButtons[i]);
        }
    }

    private void runScript() {

        if (m_currentScriptItem == null) {
            logResult(OStrings.getString("SCW_NO_SCRIPT_SELECTED"));
            return;
        }

        if (!m_currentScriptItem.canRead()) {
            logResult(OStrings.getString("SCW_CANNOT_READ_SCRIPT"));
            return;
        }

        m_txtResult.setText("");
        logResult(StaticUtils.format(OStrings.getString("SCW_RUNNING_SCRIPT"),
                m_currentScriptItem.getAbsolutePath()));

        executeScriptFile(m_currentScriptItem, false);

    }

    public void executeScriptFile(ScriptItem scriptItem, boolean forceFromFile) {
        executeScriptFile(scriptItem, forceFromFile, null);
    }

    public static Object executeScriptFileHeadless(ScriptItem scriptItem, boolean forceFromFile,
            Map<String, Object> additionalBindings) {
        ScriptEngineManager manager = new ScriptEngineManager(ScriptingWindow.class.getClassLoader());
        ScriptEngine scriptEngine = manager.getEngineByExtension(getFileExtension(scriptItem.getName()));

        if (scriptEngine == null) {
            scriptEngine = manager.getEngineByName(DEFAULT_SCRIPT);
        }

        SimpleBindings bindings = new SimpleBindings();
        bindings.put(VAR_PROJECT, Core.getProject());
        bindings.put(VAR_EDITOR, Core.getEditor());
        bindings.put(VAR_GLOSSARY, Core.getGlossary());
        bindings.put(VAR_MAINWINDOW, Core.getMainWindow());
        bindings.put(VAR_RESOURCES, scriptItem.getResourceBundle());

        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }

        Object eval = null;
        try {
            eval = scriptEngine.eval(scriptItem.getText(), bindings);
            if (eval != null) {
                Log.logRB("SCW_SCRIPT_RESULT");
                Log.log(eval.toString());
            }
        } catch (Throwable e) {
            Log.logErrorRB(e, "SCW_SCRIPT_ERROR");
        }

        return eval;
    }

    public void executeScriptFile(ScriptItem scriptItem, boolean forceFromFile, Map<String, Object> additionalBindings) {
        ScriptLogger scriptLogger = new ScriptLogger(m_txtResult);

        ScriptEngine scriptEngine = manager.getEngineByExtension(getFileExtension(scriptItem.getName()));

        if (scriptEngine == null) {
            scriptEngine = manager.getEngineByName(DEFAULT_SCRIPT);
        }

        //logResult(StaticUtils.format(OStrings.getString("SCW_SELECTED_LANGUAGE"), scriptEngine.getFactory().getEngineName()));
        SimpleBindings bindings = new SimpleBindings();
        bindings.put(VAR_PROJECT, Core.getProject());
        bindings.put(VAR_EDITOR, Core.getEditor());
        bindings.put(VAR_GLOSSARY, Core.getGlossary());
        bindings.put(VAR_MAINWINDOW, Core.getMainWindow());
        bindings.put(VAR_CONSOLE, scriptLogger);
        bindings.put(VAR_RESOURCES, scriptItem.getResourceBundle());

        if (additionalBindings != null) {
            bindings.putAll(additionalBindings);
        }

        // evaluate JavaScript code from String
        try {
            String scriptString;
            if (forceFromFile) {
                scriptString = scriptItem.getText();
            } else if ("".equals(m_txtScriptEditor.getText().trim())) {
                scriptString = scriptItem.getText();
                m_txtScriptEditor.setText(scriptString);
            } else {
                scriptString = m_txtScriptEditor.getText();
            }

            if (!scriptString.endsWith("\n")) {
                scriptString += "\n";
            }

            Object eval = scriptEngine.eval(scriptString, bindings);
            if (eval != null) {
                logResult(OStrings.getString("SCW_SCRIPT_RESULT"));
                logResult(eval.toString());
            }
        } catch (Throwable e) {
            logResult(OStrings.getString("SCW_SCRIPT_ERROR"));
            logResult(e.getMessage());
            //e.printStackTrace();
        }
    }

    private void logResult(String s) {
        logResult(m_txtResult, s + "\n");
    }

    private void logResult(JEditorPane e, String s) {
        Document doc = e.getDocument();
        try {
            doc.insertString(doc.getLength(), s, null);
        } catch (BadLocationException e1) {
            /* empty */
        }
    }

    private void directoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
        updateScriptsDirectory();
    }

    private void directoryChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // open a dialog box to choose the directory
        m_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        m_fileChooser.setDialogTitle(OStrings.getString("SCW_SCRIPTS_FOLDER_CHOOSE_TITLE"));
        int result = m_fileChooser.showOpenDialog(ScriptingWindow.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // we should write the result into the directory text field
            File file = m_fileChooser.getSelectedFile();
            m_txtScriptsDir.setText(file.getAbsolutePath());
        }
        updateScriptsDirectory();
    }

    private void updateScriptsDirectory() {
        String scriptsDir = m_txtScriptsDir.getText();

        m_scriptsDirectory = new File(scriptsDir);
        Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, scriptsDir);

        monitor.stop();
        monitor.start(m_scriptsDirectory);
    }

    /**
     * Loads the position and size of the script window
     */
    private void loadPreferences() {
        // window size and position
        try {
            String dx = Preferences.getPreference(Preferences.SCRIPTWINDOW_X);
            String dy = Preferences.getPreference(Preferences.SCRIPTWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.SCRIPTWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.SCRIPTWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            setBounds(50, 80, 1150, 650);
        }
    }

    /**
     * Saves the size and position of the script window
     */
    private void savePreferences() {
        // window size and position
        Preferences.setPreference(Preferences.SCRIPTWINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.SCRIPTWINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.SCRIPTWINDOW_X, getX());
        Preferences.setPreference(Preferences.SCRIPTWINDOW_Y, getY());
    }

    public HighlightPainter getPainter() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Mark> getMarksForEntry(String sourceText, String translationText, boolean isActive)
            throws Exception {
        return Collections.EMPTY_LIST;
    }

    private void onListSelectionChanged() {
        if (m_scriptList.isSelectionEmpty()) {
            return;
        }
        try {
            m_currentScriptItem = (ScriptItem) m_scriptList.getSelectedValue();
            m_txtScriptEditor.setText(m_currentScriptItem.getText());
            m_txtScriptEditor.setCaretPosition(0);
        } catch (IOException e) {
            logResult(OStrings.getString("SCW_CANNOT_READ_SCRIPT"));
        }
    }

    /**
     * Returns the filename without the extension
     */
    protected static String getBareFileName(String fileName) {
        if (fileName == null) {
            return null;
        }

        String bare = fileName;
        int i = fileName.lastIndexOf('.');

        if (i >= 0 && i != -1) {
            bare = fileName.substring(0, i);
        }

        return bare;
    }

    /**
     * Returns the extension of file.
     */
    protected static String getFileExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');

        if (i >= 0 && i != -1) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }

    public static final String DEFAULT_SCRIPT = "javascript";
    public static final String VAR_CONSOLE = "console";
    public static final String VAR_MAINWINDOW = "mainWindow";
    public static final String VAR_GLOSSARY = "glossary";
    public static final String VAR_EDITOR = "editor";
    public static final String VAR_PROJECT = "project";
    public static final String VAR_RESOURCES = "res";

    private static final String DEFAULT_SCRIPTS_DIR = "scripts";

    private static final int NUMBERS_OF_QUICK_SCRIPTS = 12;

    private JList m_scriptList;
    private JEditorPane m_txtResult;
    private JTextArea m_txtScriptEditor;
    private JButton m_btnRunScript;

    private final ScriptEngineManager manager = new ScriptEngineManager(getClass().getClassLoader());

    protected ScriptsMonitor monitor;

    private File m_scriptsDirectory;
    private ScriptItem m_currentScriptItem;
    private JTextField m_txtScriptsDir;
    private final JFileChooser m_fileChooser = new JFileChooser();

    private final String[] m_quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];
    private final JMenuItem[] m_quickMenus = new JMenuItem[NUMBERS_OF_QUICK_SCRIPTS];
    private final JButton[] m_quickScriptButtons = new JButton[NUMBERS_OF_QUICK_SCRIPTS];

}
