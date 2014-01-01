/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.glossary.GlossaryTextArea;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.LFileCopy;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.openide.awt.Mnemonics;

public class ScriptingWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    static ScriptingWindow window;

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

    public static void unloadPlugins() {
        if (window != null) {
            window.dispose();
        }
    }

    public ScriptingWindow() {
        setTitle(OStrings.getString("SCW_TITLE"));

        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        // END HP
        
        addScriptCommandToOmegaT();
        addRunShortcutToOmegaT();
        
        m_availableEngines = getAvailableEngines();

        initWindowLayout();

        StringBuilder sb = new StringBuilder(OStrings.getString("SCW_LIST_ENGINES") + "\n");
        for (String engine : m_availableEngines.keySet()) {
            sb.append(" - ");
            sb.append(engine);
            sb.append("\n");
        }
        
        logResult(sb.toString());

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

            String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey(i), null);

            
            if (scriptName != null || "".equals(scriptName)) {
                setQuickScriptMenu(scriptName, i);
            } else {
                unsetQuickScriptMenu(scriptName, i);
            }
            
            // Since the script is run while editing a segment, the shortcut should not interfere
            // with the segment content, so we set it to a Function key.
            m_quickMenus[i].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (i+1)));

            toolsMenu.add(menuItem);
        }
    }
    
    private int scriptKey(int i) {
        if (NUMBERS_OF_QUICK_SCRIPTS != 10) {
            return i+1;
        } 
        return i+1 == NUMBERS_OF_QUICK_SCRIPTS ? 0 : i + 1;
    }
    
    private void unsetQuickScriptMenu(String scriptName, int index) {
        m_quickScripts[index] = null;
        m_quickMenus[index].setEnabled(false);
        Mnemonics.setLocalizedText(m_quickMenus[index], "&" + scriptKey(index) + " - " + OStrings.getString("SCW_SCRIPTS_NONE"));
    }
    
    private void setQuickScriptMenu(String scriptName, int index) {
        m_quickScripts[index] = scriptName;
        
        m_quickMenus[index].addActionListener(new QuickScriptActionListener(index));
        
        // Since the script is run while editing a segment, the shortcut should not interfere
        // with the segment content, so we set it to a Function key.
        m_quickMenus[index].setAccelerator(KeyStroke.getKeyStroke("shift ctrl F" + (index+1)));
        m_quickMenus[index].setEnabled(true);

        Mnemonics.setLocalizedText( m_quickMenus[index], "&" + scriptKey(index) + " - " + scriptName);
    }

    private class QuickScriptActionListener implements ActionListener {
        private int index;

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

        logResult(StaticUtils.format(OStrings.getString("SCW_QUICK_RUN"), (index+1)));
        ScriptFile scriptFile = new ScriptFile(m_scriptsDirectory, m_quickScripts[index]);
        
        executeScriptFile(scriptFile, true);
    }

    private void addRunShortcutToOmegaT() {
        JRootPane appliRootPane = Core.getMainWindow().getApplicationFrame().getRootPane();
        appliRootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK , false), "RUN_CURRENT_SCRIPT");
        appliRootPane.getActionMap().put("RUN_CURRENT_SCRIPT",  new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript();
            }
        });
    }

    private void initWindowLayout() {
        setBounds(80, 80, 910, 550);

        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panelNorth = new JPanel();
        FlowLayout fl_panelNorth = (FlowLayout) panelNorth.getLayout();
        fl_panelNorth.setAlignment(FlowLayout.LEFT);
        getContentPane().add(panelNorth, BorderLayout.NORTH);
        
        JLabel lblScriptsDirectory = new JLabel(OStrings.getString("SCW_SCRIPTS_FOLDER"));
        panelNorth.add(lblScriptsDirectory);
        
        m_txtScriptsDir = new JTextField();
        panelNorth.add(m_txtScriptsDir);
        
        m_txtScriptsDir.setText(Preferences.getPreferenceDefault(Preferences.SCRIPTS_DIRECTORY, 
                new File(".", DEFAULT_SCRIPTS_DIR).getAbsolutePath()));
        
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

        panelNorth.add(btnBrowse);

        m_scriptsDirectory = new File(m_txtScriptsDir.getText());
        
        m_scriptList = new JList();
        updateScriptsList(false);
        JScrollPane scrollPaneList = new JScrollPane(m_scriptList);

        m_scriptList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (m_scriptList.isSelectionEmpty()) {
                    return;
                }
                try {
                    m_currentScriptFile = new ScriptFile(m_scriptsDirectory,
                            m_scriptList.getSelectedValue().toString());
                    m_txtScriptEditor.setText(m_currentScriptFile.getText());
                    m_txtScriptEditor.setCaretPosition(0);
                } catch (IOException e) {
                    logResult(OStrings.getString("SCW_CANNOT_READ_SCRIPT"));
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
                    m_currentScriptFile.setText(m_txtScriptEditor.getText());
                    logResult(StaticUtils.format(OStrings.getString("SCW_SAVE_OK"), 
                            m_currentScriptFile.getAbsolutePath()));
                } catch (IOException e) {
                    logResult(OStrings.getString("SCW_SAVE_ERROR"));
                    logResult(e.getMessage());
                }
            }
        });
        editorPopUp.add(menuItem);
        
        m_txtScriptEditor.setComponentPopupMenu(editorPopUp);
        JScrollPane scrollPaneEditor = new JScrollPane(m_txtScriptEditor);
        

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneEditor, scrollPaneResults);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setDividerLocation(200);
        Dimension minimumSize1 = new Dimension(100, 50);
        scrollPaneEditor.setMinimumSize(minimumSize1);
        scrollPaneResults.setMinimumSize(minimumSize1);
        

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneList, splitPane1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        
        Dimension minimumSize = new Dimension(100, 50);
        scrollPaneList.setMinimumSize(minimumSize);
        scrollPaneResults.setMinimumSize(minimumSize);
        
        getContentPane().add(splitPane, BorderLayout.CENTER);

        // Refresh the file list with F5        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), REFRESH_SCRIPT_DIR);
        getRootPane().getActionMap().put(REFRESH_SCRIPT_DIR, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScriptsList(true);
            }
        });

        JPanel panelSouth = new JPanel();
        FlowLayout fl_panelSouth = (FlowLayout) panelSouth.getLayout();
        fl_panelSouth.setAlignment(FlowLayout.RIGHT);
        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        
        for (int i = 0; i < NUMBERS_OF_QUICK_SCRIPTS; i++) {
            final int index = i;
            final int scriptKey = scriptKey(index);
            m_quickScriptButtons[i] = new JButton(" " + scriptKey + " ");

            String scriptName = Preferences.getPreferenceDefault("scripts_quick_" + scriptKey, null);
            
            if (scriptName != null || "".equals(scriptName)) {
                m_quickScriptButtons[i].setToolTipText(scriptName);
                m_quickScriptButtons[i].setText("<" + scriptKey + ">");
            } else {
                m_quickScriptButtons[i].setToolTipText(OStrings.getString("SCW_NO_SCRIPT_SET"));
            }
            
            // Run a script from the quick button bar
            m_quickScriptButtons[i].addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent a) {
                  if (Preferences.existsPreference("scripts_quick_" + scriptKey)) {
                      logResult(StaticUtils.format(OStrings.getString("SCW_QUICK_RUN"), scriptKey));   
                      runQuickScript(index);
                  } else {
                      logResult(StaticUtils.format(OStrings.getString("SCW_NO_SCRIPT_BOUND"), scriptKey));
                  }
                }
            });
            
            JPopupMenu quickScriptPopup = new JPopupMenu();

            // Add a script to the quick script button bar
            JMenuItem addQuickScriptMenuItem = new JMenuItem(OStrings.getString("SCW_ADD_SCRIPT"));
            addQuickScriptMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String sn = m_scriptList.getSelectedValue().toString();
                    Preferences.setPreference("scripts_quick_" + scriptKey, sn);
                    m_quickScriptButtons[index].setToolTipText( sn);
                    m_quickScriptButtons[index].setText("<" + scriptKey + ">");
                    
                    setQuickScriptMenu(sn, index);
                    
                    logResult(StaticUtils.format(OStrings.getString("SCW_SAVE_QUICK_SCRIPT"), sn, scriptKey));
                }
            });
            quickScriptPopup.add(addQuickScriptMenuItem);
            
            // Remove a script from the button bar
            JMenuItem removeQuickScriptMenuItem = new JMenuItem(OStrings.getString("SCW_REMOVE_SCRIPT"));
            removeQuickScriptMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String sn = m_scriptList.getSelectedValue().toString();
                    logResult(StaticUtils.format(OStrings.getString("SCW_REMOVED_QUICK_SCRIPT"), sn, scriptKey));
                    Preferences.setPreference("scripts_quick_" + scriptKey, "");
                    m_quickScriptButtons[index].setToolTipText(OStrings.getString("SCW_NO_SCRIPT_SET"));
                    m_quickScriptButtons[index].setText(" " + scriptKey + " ");
                    
                    unsetQuickScriptMenu(sn, index);
                }
            });
            quickScriptPopup.add(removeQuickScriptMenuItem);


            m_quickScriptButtons[i].setComponentPopupMenu(quickScriptPopup);
            
            panelSouth.add(m_quickScriptButtons[i]);
        }
        panelSouth.add(new JSeparator());
        
        m_btnRunScript = new JButton();
        Mnemonics.setLocalizedText(m_btnRunScript, OStrings.getString("SCW_RUN_SCRIPT"));
        m_btnRunScript.setAlignmentX(Component.RIGHT_ALIGNMENT);
        m_btnRunScript.setHorizontalAlignment(SwingConstants.LEFT);
        m_btnRunScript.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                runScript();
            }
        });
        panelSouth.add(m_btnRunScript);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void runScript() {
        
        if (m_currentScriptFile == null) {
            logResult(OStrings.getString("SCW_NO_SCRIPT_SELECTED"));
            return;
        }

        if (! m_currentScriptFile.canRead()) {
            logResult(OStrings.getString("SCW_CANNOT_READ_SCRIPT"));
            return;
        }

        m_txtResult.setText("");
        logResult(StaticUtils.format(OStrings.getString("SCW_RUNNING_SCRIPT"), 
                m_currentScriptFile.getAbsolutePath()));
        
        executeScriptFile(m_currentScriptFile, false);

    }

    private void executeScriptFile(ScriptFile scriptFile, boolean forceFromFile) {
        BSFManager manager = new BSFManager();
        manager.setClassLoader(this.getClass().getClassLoader());
        
        ScriptLogger scriptLogger = new ScriptLogger(m_txtResult);

        String language = DEFAULT_SCRIPT;
        try {

            language = BSFManager.getLangFromFilename(scriptFile.getName());
            logResult(StaticUtils.format(OStrings.getString("SCW_SELECTED_LANGUAGE"), language));
        } catch (BSFException e1) {
            // append(OStrings.getString("SCW_RUN_SCRIPT") + " " + e1.getMessage());
        }
         
        try {
            manager.declareBean(VAR_PROJECT, Core.getProject(), IProject.class);
            manager.declareBean(VAR_EDITOR, Core.getEditor(), IEditor.class);
            manager.declareBean(VAR_GLOSSARY, Core.getGlossary(), GlossaryTextArea.class);
            manager.declareBean(VAR_MAINWINDOW, Core.getMainWindow(), IMainWindow.class);
            manager.declareBean(VAR_CONSOLE, scriptLogger, ScriptLogger.class);
        } catch (BSFException e1) {
            logResult(OStrings.getString("SCW_SCRIPT_ERROR"));
            logResult(e1.getMessage());
        }


        // evaluate JavaScript code from String
        try {
            String scriptString;
            if (forceFromFile) {
                scriptString = scriptFile.getText();
            } else if ("".equals(m_txtScriptEditor.getText().trim())) {
                scriptString = scriptFile.getText();
                m_txtScriptEditor.setText(scriptString);   
            } else {
                scriptString = m_txtScriptEditor.getText();
            }

            if (! scriptString.endsWith("\n")) {
                scriptString += "\n";
            }
            manager.exec(language.toLowerCase(), scriptFile.getName(), -1, -1, scriptString);
            manager.terminate();
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

        updateScriptsList(true);
    }
    
    private void updateScriptsList(boolean withMsg) {
        String[] scriptList = new String[]{  };
        
        if (m_scriptsDirectory.exists() && m_scriptsDirectory.isDirectory()) {
            scriptList = m_scriptsDirectory.list(); 
            Arrays.sort(scriptList);
        }
        
        m_scriptList.setListData(scriptList);
        
        if (withMsg) {
            logResult(m_txtResult,  StaticUtils.format(OStrings.getString("SCW_REFRESH_SCRIPT_DIR"), 
                    m_scriptsDirectory.getAbsolutePath()) + "\n");
        }
    }
    

    public HighlightPainter getPainter() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Mark> getMarksForEntry(String sourceText, String translationText, boolean isActive)
            throws Exception {
        return Collections.EMPTY_LIST;
    }

    private Map<String, String> getAvailableEngines() {

        Map<String, String> availableEngines = new HashMap<String, String>();
        for (Entry<String, String> e : engines.entrySet()) {
            try {
                Class.forName(e.getValue());
                availableEngines.put(e.getKey(), e.getValue());
            } catch (Throwable ex) {
                /* empty */
            }
        }

        return availableEngines;
    }

    // Taken from languages.properties in the bsf.jar file.
    private static final HashMap<String, String> engines = new HashMap<String, String>();
    static {
        engines.put("JavaScript", "org.apache.bsf.engines.javascript.JavaScriptEngine");
        engines.put("Jacl", "org.apache.bsf.engines.jacl.JaclEngine");
        engines.put("NetRexx", "org.apache.bsf.engines.netrexx.NetRexxEngine");
        engines.put("Java", "org.apache.bsf.engines.java.JavaEngine");
        engines.put("JavaClass", "org.apache.bsf.engines.javaclass.JavaClassEngine");
        engines.put("BML", "org.apache.bml.ext.BMLEngine");
        engines.put("VBScript", "org.apache.bsf.engines.activescript.ActiveScriptEngine");
        engines.put("JScript", "org.apache.bsf.engines.activescript.ActiveScriptEngine");
        engines.put("PerlScript", "org.apache.bsf.engines.activescript.ActiveScriptEngine");
        engines.put("Perl", "org.apache.bsf.engines.perl.PerlEngine");
        engines.put("JPython", "org.apache.bsf.engines.jpython.JPythonEngine");
        engines.put("Jython", "org.apache.bsf.engines.jython.JythonEngine");
        engines.put("LotusScript", "org.apache.bsf.engines.lotusscript.LsEngine");
        engines.put("XSLT", "org.apache.bsf.engines.xslt.XSLTEngine");
        engines.put("Pnuts", "pnuts.ext.PnutsBSFEngine");
        engines.put("BeanBasic", "org.apache.bsf.engines.beanbasic.BeanBasicEngine");
        engines.put("BeanShell", "bsh.util.BeanShellBSFEngine");
        engines.put("Ruby", "org.jruby.javasupport.bsf.JRubyEngine");
        engines.put("JudoScript", "com.judoscript.BSFJudoEngine");
        engines.put("Groovy", "org.codehaus.groovy.bsf.GroovyEngine");
        engines.put("ObjectScript", "oscript.bsf.ObjectScriptEngine");
        engines.put("Prolog", "ubc.cs.JLog.Extras.BSF.JLogBSFEngine");
        engines.put("Rexx", "org.rexxla.bsf.engines.rexx.RexxEngine");
    }

    private static final String DEFAULT_SCRIPT = "JavaScript";
    private static final String VAR_CONSOLE = "console";
    private static final String VAR_MAINWINDOW = "mainWindow";
    private static final String VAR_GLOSSARY = "glossary";
    private static final String VAR_EDITOR = "editor";
    private static final String VAR_PROJECT = "project";

    private static final String DEFAULT_SCRIPTS_DIR = "scripts";
    private static final String REFRESH_SCRIPT_DIR = "refreshScriptDir";
    
    private static final int NUMBERS_OF_QUICK_SCRIPTS = 12;
    
    private JList m_scriptList;
    private JEditorPane m_txtResult;
    private JTextArea m_txtScriptEditor;
    private JButton m_btnRunScript;

    private File m_scriptsDirectory;
    private ScriptFile m_currentScriptFile;
    private Map<String, String> m_availableEngines;
    private JTextField m_txtScriptsDir;
    private JFileChooser m_fileChooser = new JFileChooser();

    private String[] m_quickScripts = new String[NUMBERS_OF_QUICK_SCRIPTS];
    private JMenuItem[] m_quickMenus = new JMenuItem[NUMBERS_OF_QUICK_SCRIPTS];
    private JButton[] m_quickScriptButtons = new JButton[NUMBERS_OF_QUICK_SCRIPTS];

    /**
     * An abstract representation of script file.
     * The content is treated as UTF-8.
     */
    private class ScriptFile extends File {

        private final String BOM = "\uFEFF";
        private boolean startsWithBOM = false;
        private String lineBreak = System.getProperty("line.separator");

        public ScriptFile(String pathname) {
            super(pathname);
        }

        public ScriptFile(File parent, String child) {
            super(parent, child);
        }

        public String getText() throws FileNotFoundException, IOException {
            String ret = "";
            LinebreakPreservingReader lpin = null;
            try {
                lpin = getUTF8LinebreakPreservingReader(this);
                StringBuilder sb = new StringBuilder();
                String s = lpin.readLine();
                startsWithBOM = s.startsWith(BOM);
                if (startsWithBOM) {
                    s = s.substring(1);  // eat BOM
                }
                while (s != null) {
                    sb.append(s);
                    String br = lpin.getLinebreak();
                    if (! br.isEmpty()) {
                        lineBreak = br;
                        sb.append('\n');
                    }
                    s = lpin.readLine();
                }
                ret = sb.toString();
            } finally {
                if (lpin != null) {
                    try {
                        lpin.close();
                    } catch (IOException ex) {
                        // Eat exception silently
                    }
                }
            }
            return ret;
        }

        private LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
            InputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, OConsts.UTF8);
            BufferedReader in = new BufferedReader(isr);
            return new LinebreakPreservingReader(in);
        }

        public void setText(String text) throws UnsupportedEncodingException, IOException {
            text = text.replaceAll("\n", lineBreak);
            if (startsWithBOM) {
                text = BOM + text;
            }

            InputStream is = new ByteArrayInputStream(text.getBytes(OConsts.UTF8));
            LFileCopy.copy(is, this);
        }
    }

}
