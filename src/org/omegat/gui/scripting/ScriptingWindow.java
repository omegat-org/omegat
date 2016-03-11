/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
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
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

/**
 * Scripting window
 * 
 * @author Briac Pilpre
 * @author Alex Buloichik
 * @author Yu Tang
 * @author Aaron Madlon-Kay
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

        initWindowLayout();

        addScriptCommandToOmegaT();
        addRunShortcutToOmegaT();
        setScriptsDirectory(Preferences.getPreferenceDefault(Preferences.SCRIPTS_DIRECTORY, DEFAULT_SCRIPTS_DIR));

        monitor = new ScriptsMonitor(this);
        if (m_scriptsDirectory != null) {
            monitor.start(m_scriptsDirectory);
        }

        logResult(listScriptEngines());

    }

    private String listScriptEngines() {
        StringBuilder sb = new StringBuilder(OStrings.getString("SCW_LIST_ENGINES") + "\n");
        for (ScriptEngineFactory engine : ScriptRunner.MANAGER.getEngineFactories()) {
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

        return sb.toString();
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

        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            JMenuItem menuItem = new JMenuItem();
            m_quickMenus[i] = menuItem;

            unsetQuickScriptMenu(i);
            
            // Since the script is run while editing a segment, the shortcut should not interfere
            // with the segment content, so we set it to a Function key.
            m_quickMenus[i].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (i + 1)));

            toolsMenu.add(menuItem);
        }
    }

    private int scriptKey(int i) {
        return i + 1;
    }

    private void unsetQuickScriptMenu(int index) {
        m_quickScripts[index] = null;
        removeAllQuickScriptActionListenersFrom(m_quickMenus[index]);
        m_quickMenus[index].setEnabled(false);
        Mnemonics.setLocalizedText(m_quickMenus[index], "&" + scriptKey(index) + " - " + OStrings.getString("SCW_SCRIPTS_NONE"));
    }

    private void setQuickScriptMenu(ScriptItem scriptItem, int index) {
        m_quickScripts[index] = scriptItem.getFile().getName();

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

        logResult(StringUtil.format(OStrings.getString("SCW_QUICK_RUN"), (index + 1)));
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

        m_scriptList = new JList<>();
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
        menuItem.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    m_currentScriptItem.setText(m_txtScriptEditor.getText());
                    logResult(StringUtil.format(OStrings.getString("SCW_SAVE_OK"),
                            m_currentScriptItem.getFile().getAbsolutePath()));
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
        if (m_scriptsDirectory != null) {
            m_txtScriptsDir.setText(m_scriptsDirectory.getPath());
        }

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
            m_quickScriptButtons[i] = new JButton(String.valueOf(scriptKey));

            // Run a script from the quick button bar
            m_quickScriptButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (Preferences.existsPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey)) {
                        runQuickScript(index);
                    } else {
                        logResult(StringUtil.format(OStrings.getString("SCW_NO_SCRIPT_BOUND"), scriptKey));
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
                    Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey,
                            scriptItem.getFile().getName());
                    m_quickScriptButtons[index].setToolTipText(scriptItem.getToolTip());
                    m_quickScriptButtons[index].setText("<" + scriptKey + ">");

                    setQuickScriptMenu(scriptItem, index);

                    logResult(StringUtil.format(OStrings.getString("SCW_SAVE_QUICK_SCRIPT"), scriptItem, scriptKey));
                }
            });
            quickScriptPopup.add(addQuickScriptMenuItem);

            // Remove a script from the button bar
            final JMenuItem removeQuickScriptMenuItem = new JMenuItem(OStrings.getString("SCW_REMOVE_SCRIPT"));
            removeQuickScriptMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String scriptName = Preferences.getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "(unknown)");
                    logResult(StringUtil.format(OStrings.getString("SCW_REMOVED_QUICK_SCRIPT"), scriptName, scriptKey));
                    Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, "");
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
                    String scriptName = Preferences.getPreferenceDefault(Preferences.SCRIPTS_QUICK_PREFIX + scriptKey, null);
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

        if (!m_currentScriptItem.getFile().canRead()) {
            logResult(OStrings.getString("SCW_CANNOT_READ_SCRIPT"));
            return;
        }

        m_txtResult.setText("");
        logResult(StringUtil.format(OStrings.getString("SCW_RUNNING_SCRIPT"),
                m_currentScriptItem.getFile().getAbsolutePath()));

        executeScriptFile(m_currentScriptItem, false);

    }

    public void executeScriptFile(ScriptItem scriptItem, boolean forceFromFile) {
        executeScriptFile(scriptItem, forceFromFile, null);
    }

    public void executeScriptFile(ScriptItem scriptItem, boolean forceFromFile,
            Map<String, Object> additionalBindings) {
        try {
            String scriptString;
            if (forceFromFile) {
                scriptString = scriptItem.getText();
            } else if (m_txtScriptEditor.getText().trim().isEmpty()) {
                scriptString = scriptItem.getText();
                m_txtScriptEditor.setText(scriptString);
            } else {
                scriptString = m_txtScriptEditor.getText();
            }

            if (!scriptString.endsWith("\n")) {
                scriptString += "\n";
            }

            Map<String, Object> bindings = new HashMap<String, Object>();
            if (additionalBindings != null) {
                bindings.putAll(additionalBindings);
            }
            bindings.put(ScriptRunner.VAR_CONSOLE, new IScriptLogger() {
                @Override
                public void print(Object o) {
                    Document doc = m_txtResult.getDocument();

                    try {
                        doc.insertString(doc.getLength(), o.toString(), null);
                    } catch (BadLocationException e) {
                        /* empty */
                    }
                }
                @Override
                public void println(Object o) {
                    print(o.toString() + "\n");
                }
                @Override
                public void clear() {
                    m_txtResult.setText("");
                }
            });

            String result = ScriptRunner.executeScript(scriptString, scriptItem, bindings);
            logResult(result);
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
        setScriptsDirectory(m_txtScriptsDir.getText());
    }

    private void directoryChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // open a dialog box to choose the directory
        m_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        m_fileChooser.setDialogTitle(OStrings.getString("SCW_SCRIPTS_FOLDER_CHOOSE_TITLE"));
        int result = m_fileChooser.showOpenDialog(ScriptingWindow.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // we should write the result into the directory text field
            File file = m_fileChooser.getSelectedFile();            
            setScriptsDirectory(file);
        }
    }

    private void setScriptsDirectory(String scriptsDir) {
        File dir;
        try {
            dir = new File(scriptsDir).getCanonicalFile();
        } catch (IOException ex) {
            dir = new File(scriptsDir).getAbsoluteFile();
        }
        setScriptsDirectory(dir);
    }
    
    private void setScriptsDirectory(File scriptsDir) {
        
        if (!scriptsDir.isDirectory()) {
            updateQuickScripts();
            return;
        }
        m_scriptsDirectory = scriptsDir;
        Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, scriptsDir.getPath());
        OSXIntegration.setProxyIcon(getRootPane(), m_scriptsDirectory);

        if (m_txtScriptsDir != null) {
            m_txtScriptsDir.setText(scriptsDir.getPath());
        }
        
        if (monitor != null) {
            monitor.stop();
            monitor.start(m_scriptsDirectory);
        }
        updateQuickScripts();
    }
    
    private void updateQuickScripts() {
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            int key = scriptKey(i);
            String scriptName = Preferences.getPreferenceDefault(
                    Preferences.SCRIPTS_QUICK_PREFIX + key, null);
    
            if (m_scriptsDirectory != null && !StringUtil.isEmpty(scriptName)) {
                setQuickScriptMenu(new ScriptItem(new File(m_scriptsDirectory, scriptName)), i);
                m_quickScriptButtons[i].setToolTipText(scriptName);
                m_quickScriptButtons[i].setText("<" + key + ">");
            } else {
                unsetQuickScriptMenu(i);
                m_quickScriptButtons[i].setToolTipText(OStrings.getString("SCW_NO_SCRIPT_SET"));
                m_quickScriptButtons[i].setText(String.valueOf(key));
            }
        }
    }

    void setScriptItems(Collection<ScriptItem> items) {
        m_scriptList.setListData(items.toArray(new ScriptItem[items.size()]));
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

    public List<Mark> getMarksForEntry(String sourceText, String translationText, boolean isActive)
            throws Exception {
        return Collections.emptyList();
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

    private static final String DEFAULT_SCRIPTS_DIR = "scripts";

    private static final int NUMBERS_OF_QUICK_SCRIPTS = 12;

    private JList<ScriptItem> m_scriptList;
    private JEditorPane m_txtResult;
    private JTextArea m_txtScriptEditor;
    private JButton m_btnRunScript;

    protected ScriptsMonitor monitor;

    private File m_scriptsDirectory;
    private ScriptItem m_currentScriptItem;
    private JTextField m_txtScriptsDir;
    private final JFileChooser m_fileChooser = new JFileChooser();

    private final String[] m_quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];
    private final JMenuItem[] m_quickMenus = new JMenuItem[NUMBERS_OF_QUICK_SCRIPTS];
    private final JButton[] m_quickScriptButtons = new JButton[NUMBERS_OF_QUICK_SCRIPTS];

}
