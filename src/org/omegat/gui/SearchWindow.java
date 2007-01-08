/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import javax.swing.undo.UndoManager;

import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.OmegaTFileChooser;
import org.openide.awt.Mnemonics;

/**
 * This is a window that appears when user'd like to search for something.
 * For each new user's request new window is created.
 * Actual search is done by SearchThread.
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class SearchWindow extends JFrame
{
    //public SearchWindow(MainWindow par, SearchThread th, String startText)
    public SearchWindow(MainWindow par, String startText)
    {
        //super(par, false);
        m_parent = par;

        m_searchLabel = new JLabel();
        m_searchField = new MFindField();

        if (startText != null)
            m_searchField.setText(startText);
        m_searchButton = new JButton();
        Box bSearch = Box.createHorizontalBox();
        bSearch.add(m_searchLabel);
        bSearch.add(m_searchField);
        bSearch.add(Box.createHorizontalStrut(10));
        bSearch.add(m_searchButton);

        m_exactSearchRB   = new JRadioButton();
        m_keywordSearchRB = new JRadioButton();
        m_resultsLabel    = new JLabel();

        ButtonGroup bg = new ButtonGroup();
        bg.add(m_exactSearchRB);
        bg.add(m_keywordSearchRB);

        Box bRB = Box.createHorizontalBox();
        bRB.add(m_exactSearchRB);
        bRB.add(Box.createHorizontalStrut(10));
        bRB.add(m_keywordSearchRB);
        bRB.add(Box.createHorizontalStrut(10));
        bRB.add(m_resultsLabel);

        m_caseCB          = new JCheckBox();
        m_regexCB         = new JCheckBox();
        m_tmSearchCB      = new JCheckBox();

        Box bOB = Box.createHorizontalBox();
        bOB.add(m_caseCB);
        bOB.add(Box.createHorizontalStrut(10));
        bOB.add(m_regexCB);
        bOB.add(Box.createHorizontalStrut(10));
        bOB.add(m_tmSearchCB);

        m_viewer = new EntryListPane(par);
        JScrollPane viewerScroller = new JScrollPane(m_viewer);

        m_dirLabel = new JLabel();
        m_dirField = new JTextField();
        m_dirField.setEditable(false);
        m_dirButton = new JButton();
        Box bDir = Box.createHorizontalBox();
        bDir.add(m_dirLabel);
        bDir.add(m_dirField);
        bDir.add(Box.createHorizontalStrut(10));
        bDir.add(m_dirButton);

        m_dirCB = new JCheckBox();
        m_dirCB.setSelected(false);
        m_recursiveCB = new JCheckBox();
        m_recursiveCB.setSelected(true);
        m_recursiveCB.setEnabled(false);

        m_dismissButton = new JButton();

        Box bCB = Box.createHorizontalBox();
        bCB.add(m_dirCB);
        bCB.add(Box.createHorizontalStrut(10));
        bCB.add(m_recursiveCB);
        bCB.add(Box.createHorizontalGlue());
        bCB.add(m_dismissButton);

        //////////////////////////////////////
        // layout container
        Container cp = getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        cp.setLayout(gridbag);
        
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weighty = 0.0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        // search controls
        gridbag.setConstraints(bSearch, c);
        cp.add(bSearch);
        
        // search type
        gridbag.setConstraints(bRB, c);
        cp.add(bRB);

        // search options (case/regex/TM)
        gridbag.setConstraints(bOB, c);
        cp.add(bOB);

        // view pane
        c.weighty = 3.0;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(viewerScroller, c);
        cp.add(viewerScroller);
        
        // directory controls
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTHWEST;
        gridbag.setConstraints(bDir, c);
        cp.add(bDir);
        
        // directory checkboxes
        gridbag.setConstraints(bCB, c);
        cp.add(bCB);
        
        /////////////////////////////////////
        // action listeners
        m_dismissButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        });
        
        m_searchButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSearch();
            }
        });
        
        m_dirButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doBrowseDirectory();
            }
        });
        
        //  Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
        put(escape, "ESCAPE");                                                  // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction);               // NOI18N

        // need to control check boxes and radio buttons manually
        //
        // keyword search can only be used when searching current project
        // TM search only works with exact/regex search on current project
        // file search only works with exact/regex search
        //
        // keep track of settings and only show what are valid choices

        m_exactSearchRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // move focus to search edit field
                m_searchField.requestFocus();

            }
        });

        m_keywordSearchRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // move focus to search edit field
                m_searchField.requestFocus();
            }
        });

        m_caseCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // move focus to search edit field
                m_searchField.requestFocus();
            }
        });

        m_regexCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // move focus to search edit field
                m_searchField.requestFocus();
            }
        });

        m_tmSearchCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_tmSearch = m_tmSearchCB.isSelected();

                // move focus to search edit field
                m_searchField.requestFocus();
            }
        });

        m_dirCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateOptionStatus();

                // move focus to dir edit field if dir search is selected
                // otherwise move focus to search field
                if (m_dirCB.isSelected())
                    m_dirField.requestFocus();
                else
                    m_searchField.requestFocus();
            }
        });

        updateUIText();
        loadPreferences();

        m_viewer.setText(OStrings.getString("SW_VIEWER_TEXT"));

        if (!par.isProjectLoaded())
        {
            // restrict user to file only access
            m_dirCB.setSelected(true);
            m_dirCB.setEnabled(false);
            m_tmSearchCB.setSelected(false);
            m_tmSearchCB.setEnabled(false);
            m_dirField.setEditable(true);

            // update enabled/selected status of options
            updateOptionStatus();
        }

        m_searchField.requestFocus();

        // start the search in a separate thread
        m_thread = new SearchThread(par, this, startText);
        m_thread.start();
    }
    
    /**
      * Loads the position and size of the search window and the button selection state.
      */
    private void loadPreferences()
    {
        // window size and position
        try {
            String dx = Preferences.getPreference(Preferences.SEARCHWINDOW_X);
            String dy = Preferences.getPreference(Preferences.SEARCHWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.SEARCHWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.SEARCHWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        }
        catch (NumberFormatException nfe) {
            // set default size and position
            setSize(650, 700);
        }

        // search dir options
        String searchFiles = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_FILES);
        if ((searchFiles == null) || (searchFiles.length() == 0))
            searchFiles = "false";
        m_dirCB.setSelected(Boolean.valueOf(searchFiles).booleanValue());
        String searchDir = Preferences.getPreference(Preferences.SEARCHWINDOW_DIR);
        if (!searchDir.equals(""))
            m_dirField.setText(searchDir);
        m_dirField.setEditable(m_dirCB.isSelected());
        String recursive = Preferences.getPreference(Preferences.SEARCHWINDOW_RECURSIVE);
        if ((recursive == null) || (recursive.length() == 0))
            recursive = "true";
        m_recursiveCB.setSelected(Boolean.valueOf(recursive).booleanValue());
        m_recursiveCB.setEnabled(m_dirCB.isSelected());

        // search type
        String searchType = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE);
        if ((searchType == null) || (searchType.length() == 0))
            searchType = SEARCH_TYPE_EXACT;
        m_exactSearchRB.setSelected(searchType.equals(SEARCH_TYPE_EXACT));
        m_keywordSearchRB.setSelected(searchType.equals(SEARCH_TYPE_KEYWORD));

        // case sensitivity
        String caseSens = Preferences.getPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE);
        if ((caseSens == null) || (caseSens.length() == 0))
            caseSens = "false";
        m_caseCB.setSelected(Boolean.valueOf(caseSens).booleanValue());

        // regular expressions
        String regex = Preferences.getPreference(Preferences.SEARCHWINDOW_REG_EXPRESSIONS);
        if ((regex == null) || (regex.length() == 0))
            regex = "false";
        m_regexCB.setSelected(Boolean.valueOf(regex).booleanValue());

        // TM search
        String tmSearch = Preferences.getPreference(Preferences.SEARCHWINDOW_TM_SEARCH);
        if ((tmSearch == null) || (tmSearch.length() == 0))
            tmSearch = "true"; // NOI18N
        m_tmSearchCB.setSelected(Boolean.valueOf(tmSearch).booleanValue());
        m_tmSearch = Boolean.valueOf(tmSearch).booleanValue();

        // update the enabled/selected status of all options
        updateOptionStatus();
    }

    /**
      * Saves the size and position of the search window and the button selection state
      */
    private void savePreferences()
    {
        // window size and position
        Preferences.setPreference(Preferences.SEARCHWINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.SEARCHWINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.SEARCHWINDOW_X, getX());
        Preferences.setPreference(Preferences.SEARCHWINDOW_Y, getY());

        // search type
        if (m_exactSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_EXACT);
        else if (m_keywordSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_KEYWORD);

        // search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE,
                                  Boolean.toString(m_caseCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_REG_EXPRESSIONS,
                                  Boolean.toString(m_regexCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_TM_SEARCH,
                                  Boolean.toString(m_tmSearch)); // don't use radio button status!

        // search dir options
        Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, m_dirField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_FILES,
                                  Boolean.toString(m_dirCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_RECURSIVE,
                                  Boolean.toString(m_recursiveCB.isSelected()));

        // need to explicitly save preferences
        // because project might not be open
        Preferences.save();
    }

    /**
      * Updates the enabled/selected status of the options in the dialog.
      *
      * Called when the search type (exact/regex/keyword) is changed, when
      * the "search files" option is (de)selected, or when the preferences
      * have been loaded after the search dialog is first shown.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private void updateOptionStatus() {
        // disable TM search when searching through dirs
        m_tmSearchCB.setEnabled(!m_dirCB.isSelected());
        m_tmSearchCB.setSelected(   !m_dirCB.isSelected()
                                 && m_tmSearch);

        // set dir search options
        m_recursiveCB.setEnabled(m_dirCB.isSelected());
        m_dirField.setEditable(m_dirCB.isSelected());
    }


    ////////////////////////////////////////////////////////////////
    // interface for displaying text in viewer
    
    public void displayResults()
    {
        m_viewer.finalize();
        m_resultsLabel.setText(
            StaticUtils.format(OStrings.getString("SW_NR_OF_RESULTS"),
                               new Object[] {new Integer(m_viewer.getNrEntries())})
        );
    }
    
    public void addEntry(int num, String preamble, String src, String tar)
    {
        m_viewer.addEntry(num, preamble, src, tar);
    }
    
    public void postMessage(String message)
    {
        m_viewer.addMessage(message);
    }
    
    /////////////////////////////////////////////////////////////////
    // misc public functions
    
    // put keyboard focus on search field
    public void setSearchControlFocus()
    {
        m_searchField.requestFocus();
    }
    
    // called by controlling thread
    public void threadDied()
    {
        m_thread = null;
        dispose();
    }
    
    ///////////////////////////////////////////////////////////////
    // internal functions
    
    public void processWindowEvent(WindowEvent w)
    {
        int evt = w.getID();
        if (evt == WindowEvent.WINDOW_CLOSING || evt == WindowEvent.WINDOW_CLOSED)
        {
            // save user preferences
            savePreferences();

            // notify main window
            m_parent.searchWindowClosed(this);

            if (m_thread != null)
                m_thread.interrupt();
        }
        super.processWindowEvent(w);
    }
    
    private void doBrowseDirectory()
    {
        OmegaTFileChooser browser = new OmegaTFileChooser();
        //String str = OStrings.getString("BUTTON_SELECT");
        //browser.setApproveButtonText(str);
        browser.setDialogTitle(OStrings.getString("SW_TITLE"));
        browser.setFileSelectionMode(OmegaTFileChooser.DIRECTORIES_ONLY);
        String curDir = m_dirField.getText();
        
        if (!curDir.equals(""))											// NOI18N
        {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory())
            {
                browser.setCurrentDirectory(dir);
            }
        }
        
        browser.showOpenDialog(this);
        File dir = browser.getSelectedFile();
        if (dir == null)
            return;
        
        String str = dir.getAbsolutePath() + File.separator;
        m_dirField.setText(str);
    }
    
    private void doSearch()
    {
        if (m_thread == null)
            doCancel();
        else
        {
            m_viewer.reset();
            String root = null;
            if (m_dirCB.isSelected())
            {
                // make sure it's a valid directory name
                root = m_dirField.getText();
                if (!root.endsWith(File.separator))
                    root += File.separator;
                File f = new File(root);
                if (!f.exists() || !f.isDirectory())
                {
                    String error = StaticUtils.format(
                            OStrings.getString("SW_ERROR_BAD_DIR"), 
                            new Object[] {m_dirField.getText()} );
                    m_viewer.setText(error);
                    Log.log(error);
                    return;
                }
                if (CommandThread.core != null && m_dirCB.isSelected())
                {
                    Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, root);
                    // need to explicitly save preferences because project
                    //	might not be open
                    Preferences.save();
                }
            }
            m_thread.requestSearch(m_searchField.getText(),
                                   root,
                                   m_recursiveCB.isSelected(),
                                   m_exactSearchRB.isSelected(),
                                   m_keywordSearchRB.isSelected(),
                                   m_caseCB.isSelected(),
                                   m_regexCB.isSelected(),
                                   m_tmSearchCB.isSelected());
        }
    }
    
    private void doCancel()
    {
        dispose();
    }
    
    private void updateUIText()
    {
        setTitle(OStrings.getString("SW_TITLE"));

        Mnemonics.setLocalizedText(m_searchLabel, OStrings.getString("SW_SEARCH_TEXT"));
        Mnemonics.setLocalizedText(m_searchButton, OStrings.getString("BUTTON_SEARCH"));

        Mnemonics.setLocalizedText(m_exactSearchRB, OStrings.getString("SW_EXACT_SEARCH"));
        Mnemonics.setLocalizedText(m_keywordSearchRB, OStrings.getString("SW_WORD_SEARCH"));

        Mnemonics.setLocalizedText(m_caseCB, OStrings.getString("SW_CASE_SENSITIVE"));
        Mnemonics.setLocalizedText(m_regexCB, OStrings.getString("SW_REG_EXPRESSIONS"));
        Mnemonics.setLocalizedText(m_tmSearchCB, OStrings.getString("SW_SEARCH_TM"));

        Mnemonics.setLocalizedText(m_dirLabel, OStrings.getString("SW_LOCATION"));
        Mnemonics.setLocalizedText(m_dirCB, OStrings.getString("SW_DIR_SEARCH"));
        Mnemonics.setLocalizedText(m_recursiveCB, OStrings.getString("SW_DIR_RECURSIVE"));
        Mnemonics.setLocalizedText(m_dirButton, OStrings.getString("SW_BROWSE"));
        
        Mnemonics.setLocalizedText(m_dismissButton, OStrings.getString("BUTTON_CLOSE"));
    }
    
    class MFindField extends JTextField
    {
        public MFindField() {
            //  Handle undo (CtrlCmd+Z);
            KeyStroke undo = StaticUtils.onMacOSX()
                                 ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_MASK, false)
                                 : KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK, false);
            Action undoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    undo();
                }
            };
            getInputMap().put(undo, "UNDO");                                                  // NOI18N
            getActionMap().put("UNDO", undoAction);               // NOI18N

            //  Handle redo (CtrlCmd+Y);
            KeyStroke redo = StaticUtils.onMacOSX()
                                 ? KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_MASK, false)
                                 : KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false);
            Action redoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    redo();
                }
            };
            getInputMap().put(redo, "REDO");                                                  // NOI18N
            getActionMap().put("REDO", redoAction);               // NOI18N
        }

        protected Document createDefaultModel() {
            PlainDocument doc = new PlainDocument(new StringContent());
            //doc.addDocumentListener(this);
            undoManager = new UndoManager();
            doc.addUndoableEditListener(undoManager);
            return doc;
        }

        protected void processKeyEvent(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                    e.getID() == KeyEvent.KEY_PRESSED)
            {
                if (!getText().equals(""))								// NOI18N
                    doSearch();
            }
            else
            {
                super.processKeyEvent(e);
            }
        }

        protected void undo() {
            if (undoManager.canUndo())
                undoManager.undo();
        }

        protected void redo() {
            if (undoManager.canRedo())
                undoManager.redo();
        }

        private UndoManager undoManager;
    }

    private MainWindow m_parent;

    private JLabel      m_searchLabel;
    private JTextField  m_searchField;
    private JButton     m_searchButton;

    private JRadioButton m_exactSearchRB;
    private JRadioButton m_keywordSearchRB;
    private JLabel       m_resultsLabel;

    private JCheckBox m_caseCB;
    private JCheckBox m_regexCB;
    private JCheckBox m_tmSearchCB;

    private boolean m_tmSearch = true;

    private JLabel     m_dirLabel;
    private JTextField m_dirField;
    private JButton    m_dirButton;
    private JCheckBox  m_dirCB;
    private JCheckBox  m_recursiveCB;

    private JButton m_dismissButton;

    private EntryListPane m_viewer;

    private SearchThread m_thread;

    private final static String SEARCH_TYPE_EXACT   = "EXACT";
    private final static String SEARCH_TYPE_KEYWORD = "KEYWORD";
}
