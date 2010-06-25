/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei
               Home page: http://www.omegat.org/
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

package org.omegat.gui.search;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import javax.swing.undo.UndoManager;

import org.omegat.core.Core;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.core.threads.SearchThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.UIThreadsUtil;
import org.openide.awt.Mnemonics;

/**
 * This is a window that appears when user'd like to search for something.
 * For each new user's request new window is created.
 * Actual search is done by SearchThread.
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 */
public class SearchWindow extends JFrame
{
    //public SearchWindow(MainWindow par, SearchThread th, String startText)
    public SearchWindow(MainWindow par, String startText)
    {
        //super(par, false);
        m_parent = par;

        m_dateFormat = new SimpleDateFormat(SAVED_DATE_FORMAT);

        //box Search bSearch
        m_searchLabel = new JLabel();
        m_searchField = new MFindField();

        if (startText != null)
            m_searchField.setText(startText);
        m_searchButton = new JButton();
        Box bSearch = Box.createHorizontalBox();
        bSearch.add(m_searchLabel);
        bSearch.add(m_searchField);
        bSearch.add(Box.createHorizontalStrut(H_MARGIN));
        bSearch.add(m_searchButton);

        m_exactSearchRB   = new JRadioButton();
        m_keywordSearchRB = new JRadioButton();
        m_regexpSearchRB  = new JRadioButton();
        m_resultsLabel    = new JLabel();

        ButtonGroup bg = new ButtonGroup();
        bg.add(m_exactSearchRB);
        bg.add(m_keywordSearchRB);
        bg.add(m_regexpSearchRB);

        //box Radio Box bRB
        Box bRB = Box.createHorizontalBox();
        bRB.add(m_exactSearchRB);
        bRB.add(Box.createHorizontalStrut(H_MARGIN));
        bRB.add(m_keywordSearchRB);
        bRB.add(Box.createHorizontalStrut(H_MARGIN));
        bRB.add(m_regexpSearchRB);
        bRB.add(Box.createHorizontalStrut(H_MARGIN));
        bRB.add(m_resultsLabel);
        bRB.add(Box.createHorizontalGlue());

        m_caseCB         = new JCheckBox();
        m_searchSourceCB = new JCheckBox();
        m_searchTargetCB = new JCheckBox();
        m_tmSearchCB     = new JCheckBox();
        m_allResultsCB    = new JCheckBox();

        //box OptionsBox bOB
        Box bOB = Box.createHorizontalBox();
        bOB.add(m_caseCB);
        bOB.add(Box.createHorizontalStrut(H_MARGIN));
        bOB.add(m_searchSourceCB);
        bOB.add(Box.createHorizontalStrut(H_MARGIN));
        bOB.add(m_searchTargetCB);
        bOB.add(Box.createHorizontalStrut(H_MARGIN));
        bOB.add(m_tmSearchCB);
        bOB.add(Box.createHorizontalStrut(H_MARGIN));
        bOB.add(m_allResultsCB);

        m_advancedButton  = new JButton();

        // Box for advanced options button
        Box bAO = Box.createHorizontalBox();
        bAO.add(m_advancedButton);
        bAO.add(Box.createHorizontalGlue());

        //box AuthorBox
        m_authorCB = new JCheckBox();
        m_authorField = new MFindField();
        m_authorField.setEditable(false);
        bAB = Box.createHorizontalBox();
        bAB.add(m_authorCB);
        bAB.add(m_authorField);

        //box DateBox
        Calendar calendar= Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        Date latestDate = initDate;
        m_dateFromModel = new SpinnerDateModel(initDate,
                earliestDate,
                latestDate,
                Calendar.YEAR);
        m_dateFromCB       = new JCheckBox();
        m_dateFromSpinner  = new JSpinner(m_dateFromModel);
        m_dateFromButton   = new JButton();
        bDB = Box.createHorizontalBox();
        bDB.add(m_dateFromCB);
        bDB.add(m_dateFromSpinner);
        bDB.add(m_dateFromButton);
        bDB.add(Box.createHorizontalStrut(H_MARGIN));
        bDB.add(Box.createHorizontalGlue());

        m_dateToModel = new SpinnerDateModel(initDate,
                earliestDate,
                latestDate,
                Calendar.YEAR);
        m_dateToCB       = new JCheckBox();
        m_dateToSpinner  = new JSpinner(m_dateToModel);
        m_dateToButton   = new JButton();
        bDB.add(m_dateToCB);
        bDB.add(m_dateToSpinner);
        bDB.add(m_dateToButton);

        m_viewer = new EntryListPane(par);
        JScrollPane viewerScroller = new JScrollPane(m_viewer);

        //box Directory bDir
        m_dirLabel = new JLabel();
        m_dirField = new JTextField();
        m_dirField.setEditable(false);
        m_dirButton = new JButton();
        Box bDir = Box.createHorizontalBox();
        bDir.add(m_dirLabel);
        bDir.add(m_dirField);
        bDir.add(Box.createHorizontalStrut(H_MARGIN));
        bDir.add(m_dirButton);

        m_dirCB = new JCheckBox();
        m_dirCB.setSelected(false);
        m_recursiveCB = new JCheckBox();
        m_recursiveCB.setSelected(true);
        m_recursiveCB.setEnabled(false);

        m_removeFilterButton = new JButton();
        m_removeFilterButton.setEnabled(false);
        m_filterButton = new JButton();
        m_filterButton.setEnabled(false);
        m_dismissButton = new JButton();

        //box CheckBox
        Box bCB = Box.createHorizontalBox();
        bCB.add(m_dirCB);
        bCB.add(Box.createHorizontalStrut(H_MARGIN));
        bCB.add(m_recursiveCB);
        bCB.add(Box.createHorizontalGlue());
        bCB.add(m_removeFilterButton);
        bCB.add(m_filterButton);
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
        
        // search type (exact/keyword/regex) + results counter
        gridbag.setConstraints(bRB, c);
        cp.add(bRB);

        // search options (case/source/target/TM/all results)
        gridbag.setConstraints(bOB, c);
        cp.add(bOB);

        //author search
        gridbag.setConstraints(bAB, c);
        cp.add(bAB);

        //date search
        gridbag.setConstraints(bDB, c);
        cp.add(bDB);

        // advanced options button
        gridbag.setConstraints(bAO, c);
        cp.add(bAO);

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
        m_filterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doFilter();
            }
        });
        m_removeFilterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRemoveFilter();
            }
        });
        
        m_searchButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSearch();
            }
        });
        
        m_advancedButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                toggleAdvancedOptions();
            }
        });
        
        m_authorCB.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                enableDisableAuthor();
            }
        });

        m_dateToCB.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                enableDisableDateTo();
            }
        });

        m_dateToButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                doResetDateTo();
            }
        });

        m_dateFromButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                doResetDateFrom();
            }
        });

        m_dateFromCB.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                enableDisableDateFrom();
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
        put(escape, "ESCAPE");                                                  
        getRootPane().getActionMap().put("ESCAPE", escapeAction);               

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

        m_regexpSearchRB.addActionListener(new ActionListener() {
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

        m_searchSourceCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // move focus to search edit field
                m_searchField.requestFocus();
            }
        });

        m_searchTargetCB.addActionListener(new ActionListener() {
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

        m_allResultsCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

        m_viewer.setFont(); // Otherwise, the user-defined font is not used
        m_viewer.setText(OStrings.getString("SW_VIEWER_TEXT"));
        
        if (!Core.getProject().isProjectLoaded())
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
        m_thread = new SearchThread(this);
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
            setSize(800, 700);
        }

        // search dir options
        String searchFiles = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_FILES);
        if (StringUtil.isEmpty(searchFiles))
            searchFiles = "false";
        m_dirCB.setSelected(Boolean.valueOf(searchFiles).booleanValue());
        String searchDir = Preferences.getPreference(Preferences.SEARCHWINDOW_DIR);
        if (!StringUtil.isEmpty(searchDir))
            m_dirField.setText(searchDir);
        m_dirField.setEditable(m_dirCB.isSelected());
        String recursive = Preferences.getPreference(Preferences.SEARCHWINDOW_RECURSIVE);
        if (StringUtil.isEmpty(recursive))
            recursive = "true";
        m_recursiveCB.setSelected(Boolean.valueOf(recursive).booleanValue());
        m_recursiveCB.setEnabled(m_dirCB.isSelected());

        // search type
        String searchType = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE);
        if (StringUtil.isEmpty(searchType))
            searchType = SEARCH_TYPE_EXACT;
        m_exactSearchRB.setSelected(searchType.equals(SEARCH_TYPE_EXACT));
        m_keywordSearchRB.setSelected(searchType.equals(SEARCH_TYPE_KEYWORD));
        m_regexpSearchRB.setSelected(searchType.equals(SEARCH_TYPE_REGEXP));

        // case sensitivity
        String caseSens = Preferences.getPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE);
        if (StringUtil.isEmpty(caseSens))
            caseSens = "false";
        m_caseCB.setSelected(Boolean.valueOf(caseSens).booleanValue());

        // search source
        String searchSource = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_SOURCE);
        if (StringUtil.isEmpty(searchSource))
            searchSource = "true";
        m_searchSourceCB.setSelected(Boolean.valueOf(searchSource).booleanValue());

        // search target
        String searchTarget = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_TARGET);
        if (StringUtil.isEmpty(searchTarget))
            searchTarget = "true";
        m_searchTargetCB.setSelected(Boolean.valueOf(searchTarget).booleanValue());

        // TM search
        String tmSearch = Preferences.getPreference(Preferences.SEARCHWINDOW_TM_SEARCH);
        if (StringUtil.isEmpty(tmSearch))
            tmSearch = "true"; 
        m_tmSearchCB.setSelected(Boolean.valueOf(tmSearch).booleanValue());
        m_tmSearch = Boolean.valueOf(tmSearch).booleanValue();

        // all results
        String allResults = Preferences.getPreference(Preferences.SEARCHWINDOW_ALL_RESULTS);
        if (StringUtil.isEmpty(allResults))
            allResults = "false";
        m_allResultsCB.setSelected(Boolean.valueOf(allResults).booleanValue());

        // update the enabled/selected status of normal options
        updateOptionStatus();

        // load advanced options settings from user preferences
        loadAdvancedOptionPreferences();
        // update advanced options status
        updateAdvancedOptionStatus();
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
        else if (m_regexpSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_REGEXP);

        // search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE,
                                  Boolean.toString(m_caseCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_SOURCE,
                                  Boolean.toString(m_searchSourceCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TARGET,
                                  Boolean.toString(m_searchTargetCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_TM_SEARCH,
                                  Boolean.toString(m_tmSearch)); // don't use radio button status!
        Preferences.setPreference(Preferences.SEARCHWINDOW_ALL_RESULTS,
                                  Boolean.toString(m_allResultsCB.isSelected()));
        // advanced search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE,
                                  Boolean.toString(m_advancedVisible));
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR,
                                  Boolean.toString(m_authorCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME, m_authorField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM,
                                  Boolean.toString(m_dateFromCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE,
                                  m_dateFormat.format(m_dateFromModel.getDate()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO,
                                  Boolean.toString(m_dateToCB.isSelected()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE,
                                  m_dateFormat.format(m_dateToModel.getDate()));

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
    
    public void displayResults() {
        m_viewer.finalize();
        m_resultsLabel.setText(
            StaticUtils.format(OStrings.getString("SW_NR_OF_RESULTS"),
                               new Object[] {new Integer(m_viewer.getNrEntries())})
        );
        m_filterButton.setEnabled(true);

        // save user preferences
        savePreferences();

    }
    
    public void addEntries(List<SearchResultEntry> entries) {
        for (SearchResultEntry e : entries) {
            m_viewer.addEntry(e.getEntryNum(), e.getPreamble(),
                    e.getSrcPrefix(), e.getSrcText(), e.getTranslation(),
                    e.getSrcMatch(), e.getTargetMatch());
        }
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
    
    @Override
    public void processWindowEvent(WindowEvent w)
    {
        int evt = w.getID();
        if (evt == WindowEvent.WINDOW_CLOSING || evt == WindowEvent.WINDOW_CLOSED)
        {
            // save user preferences
            savePreferences();

            // notify main window
            m_parent.removeSearchWindow(this);

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
        
        if (!curDir.equals(""))											
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

    private void doFilter() {
        Core.getEditor().addFilter(m_viewer.getEntryList());
        m_removeFilterButton.setEnabled(true);
    }

    private void doRemoveFilter() {
        m_removeFilterButton.setEnabled(false);
        Core.getEditor().removeFilter();
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
                if (m_dirCB.isSelected())
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
                                   m_regexpSearchRB.isSelected(),
                                   m_caseCB.isSelected(),
                                   m_tmSearchCB.isSelected(),
                                   m_allResultsCB.isSelected(),
                                   m_searchSourceCB.isSelected(),
                                   m_searchTargetCB.isSelected(),
                                   m_authorCB.isSelected(),
                                   m_authorField.getText(),
                                   m_dateFromCB.isSelected(),
                                   m_dateFromModel.getDate().getTime(),
                                   m_dateToCB.isSelected(),
                                   m_dateToModel.getDate().getTime()
                                   );
        }
    }

    private void doCancel()
    {
        dispose();
    }

    private void toggleAdvancedOptions()
    {
        m_advancedVisible = !m_advancedVisible;
        updateAdvancedOptionStatus();
    }

    private void enableDisableAuthor()
    {
        boolean editable = m_authorCB.isSelected();
        m_authorField.setEditable(editable);

        if (editable) {
            // move focus to author field
            m_authorField.requestFocus();
        } else {
            // move focus to search edit field
            m_searchField.requestFocus();
        }
    }

    private void enableDisableDateFrom()
    {
        boolean enable = m_dateFromCB.isSelected();
        m_dateFromSpinner.setEnabled(enable);
        m_dateFromButton.setEnabled(enable);

        if (enable) {
            // move focus to date spinner
            m_dateFromSpinner.requestFocus();
        } else {
            // move focus to search edit field
            m_searchField.requestFocus();
        }
    }

    private void doResetDateFrom()
    {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        m_dateFromModel.setEnd(now);
        m_dateFromModel.setValue(now);
    }

    private void doResetDateTo()
    {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        m_dateToModel.setEnd(now);
        m_dateToModel.setValue(now);
    }

    private void enableDisableDateTo() 
    {
        boolean enable = m_dateToCB.isSelected();
        m_dateToSpinner.setEnabled(enable);
        m_dateToButton.setEnabled(enable);

        if (enable) {
            // move focus to date spinner
            m_dateToSpinner.requestFocus();
        } else {
            // move focus to search edit field
            m_searchField.requestFocus();
        }
    }

    private void loadAdvancedOptionPreferences()
    {
        // advanced options visibility
        String advancedVisible = Preferences.getPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE);
        if (StringUtil.isEmpty(advancedVisible))
            advancedVisible = "false";
        m_advancedVisible = Boolean.valueOf(advancedVisible).booleanValue();

        // author options
        String searchAuthor = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR);
        if (StringUtil.isEmpty(searchAuthor))
            searchAuthor = "false";
        m_authorCB.setSelected(Boolean.valueOf(searchAuthor).booleanValue());
        String authorName = Preferences.getPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME);
        if (!StringUtil.isEmpty(authorName))
            m_authorField.setText(authorName);

        // date options
        try {
            // from date
            String dateFrom = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_FROM);
            if (StringUtil.isEmpty(dateFrom))
                dateFrom = "false";
            m_dateFromCB.setSelected(Boolean.valueOf(dateFrom).booleanValue());
            String dateFromValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE);
            if (!StringUtil.isEmpty(dateFromValue))
                m_dateFromModel.setValue(m_dateFormat.parse(dateFromValue));

            // to date
            String dateTo = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_TO);
            if (StringUtil.isEmpty(dateTo))
                dateTo = "false";
            m_dateToCB.setSelected(Boolean.valueOf(dateTo).booleanValue());
            String dateToValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE);
            if (!StringUtil.isEmpty(dateToValue))
                m_dateToModel.setValue(m_dateFormat.parse(dateToValue));
        } catch (ParseException e) {
            // use safe settings in case of parsing error
            m_dateFromCB.setSelected(false);
            m_dateToCB.setSelected(false);
        }

        // if advanced options are enabled (e.g. author/date search),
        // let the user see them anyway. This is important because
        // search results will be affected by these settings
        if (m_authorCB.isSelected() || m_dateFromCB.isSelected() || m_dateToCB.isSelected()) {
            m_advancedVisible = true;
        }
    }

    private void updateAdvancedOptionStatus()
    {

        bAB.setVisible(m_advancedVisible);
        bDB.setVisible(m_advancedVisible);

        m_authorField.setEditable(m_authorCB.isSelected());
        m_dateFromSpinner.setEnabled(m_dateFromCB.isSelected());
        m_dateFromButton.setEnabled(m_dateFromCB.isSelected());
        m_dateToSpinner.setEnabled(m_dateToCB.isSelected());
        m_dateToButton.setEnabled(m_dateToCB.isSelected());
    }

    private void updateUIText()
    {
        setTitle(OStrings.getString("SW_TITLE"));

        Mnemonics.setLocalizedText(m_searchLabel, OStrings.getString("SW_SEARCH_TEXT"));
        Mnemonics.setLocalizedText(m_searchButton, OStrings.getString("BUTTON_SEARCH"));

        Mnemonics.setLocalizedText(m_exactSearchRB, OStrings.getString("SW_EXACT_SEARCH"));
        Mnemonics.setLocalizedText(m_keywordSearchRB, OStrings.getString("SW_WORD_SEARCH"));
        Mnemonics.setLocalizedText(m_regexpSearchRB, OStrings.getString("SW_REGEXP_SEARCH"));
        
        Mnemonics.setLocalizedText(m_searchSourceCB, OStrings.getString("SW_SEARCH_SOURCE"));
        Mnemonics.setLocalizedText(m_searchTargetCB, OStrings.getString("SW_SEARCH_TARGET"));

        Mnemonics.setLocalizedText(m_advancedButton, OStrings.getString("SW_ADVANCED_OPTIONS"));
        Mnemonics.setLocalizedText(m_authorCB, OStrings.getString("SW_AUTHOR"));
        Mnemonics.setLocalizedText(m_dateFromButton, OStrings.getString("SW_NOW"));
        Mnemonics.setLocalizedText(m_dateFromCB, OStrings.getString("SW_CHANGED_AFTER"));
        Mnemonics.setLocalizedText(m_dateToCB, OStrings.getString("SW_CHANGED_BEFORE"));
        Mnemonics.setLocalizedText(m_dateToButton, OStrings.getString("SW_NOW"));

        Mnemonics.setLocalizedText(m_caseCB, OStrings.getString("SW_CASE_SENSITIVE"));
        Mnemonics.setLocalizedText(m_tmSearchCB, OStrings.getString("SW_SEARCH_TM"));
        Mnemonics.setLocalizedText(m_allResultsCB, OStrings.getString("SW_ALL_RESULTS"));

        Mnemonics.setLocalizedText(m_dirLabel, OStrings.getString("SW_LOCATION"));
        Mnemonics.setLocalizedText(m_dirCB, OStrings.getString("SW_DIR_SEARCH"));
        Mnemonics.setLocalizedText(m_recursiveCB, OStrings.getString("SW_DIR_RECURSIVE"));
        Mnemonics.setLocalizedText(m_dirButton, OStrings.getString("SW_BROWSE"));
        
        Mnemonics.setLocalizedText(m_dismissButton, OStrings.getString("BUTTON_CLOSE"));
        Mnemonics.setLocalizedText(m_filterButton, OStrings.getString("BUTTON_FILTER"));
        Mnemonics.setLocalizedText(m_removeFilterButton, OStrings.getString("BUTTON_REMOVEFILTER"));
    }
    /**
     * Display message dialog with the error as message
     * @param ex
     *                exception to show
     * @param errorKey
     *                error message key in resource bundle
     * @param params
     *                error text parameters
     */
    public void displayErrorRB(final Throwable ex, final String errorKey,
            final Object... params) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                String msg;
                if (params != null) {
                    msg = StaticUtils.format(OStrings.getString(errorKey),
                            params);
                } else {
                    msg = OStrings.getString(errorKey);
                }

                String fulltext = msg;
                if (ex != null)
                    fulltext += "\n" + ex.getLocalizedMessage(); 
                JOptionPane.showMessageDialog(SearchWindow.this, fulltext,
                        OStrings.getString("TF_ERROR"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
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
            getInputMap().put(undo, "UNDO");                                                  
            getActionMap().put("UNDO", undoAction);               

            //  Handle redo (CtrlCmd+Y);
            KeyStroke redo = StaticUtils.onMacOSX()
                                 ? KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_MASK, false)
                                 : KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false);
            Action redoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    redo();
                }
            };
            getInputMap().put(redo, "REDO");                                                  
            getActionMap().put("REDO", redoAction);               
        }

        @Override
        protected Document createDefaultModel() {
            PlainDocument doc = new PlainDocument(new StringContent());
            //doc.addDocumentListener(this);
            undoManager = new UndoManager();
            doc.addUndoableEditListener(undoManager);
            return doc;
        }

        @Override
        protected void processKeyEvent(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                    e.getID() == KeyEvent.KEY_PRESSED)
            {
                if (!getText().equals(""))								
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

    private SimpleDateFormat m_dateFormat;

    private JLabel      m_searchLabel;
    private JTextField  m_searchField;
    private JButton     m_searchButton;

    private JRadioButton m_exactSearchRB;
    private JRadioButton m_keywordSearchRB;
    private JRadioButton m_regexpSearchRB;
    private JLabel       m_resultsLabel;
    private JButton      m_advancedButton;

    private JCheckBox    m_searchSourceCB;
    private JCheckBox    m_searchTargetCB;

    private boolean      m_advancedVisible;

    private Box          bAB;
    private JCheckBox    m_authorCB;
    private JTextField   m_authorField;

    private Box          bDB;
    private JCheckBox    m_dateFromCB;
    private JButton      m_dateFromButton;
    private JSpinner     m_dateFromSpinner;
    private SpinnerDateModel m_dateFromModel;

    private JCheckBox    m_dateToCB;
    private JButton      m_dateToButton;
    private JSpinner     m_dateToSpinner;
    private SpinnerDateModel m_dateToModel;

    private JCheckBox m_caseCB;
    private JCheckBox m_tmSearchCB;
    private JCheckBox m_allResultsCB;

    private boolean m_tmSearch = true;

    private JLabel     m_dirLabel;
    private JTextField m_dirField;
    private JButton    m_dirButton;
    private JCheckBox  m_dirCB;
    private JCheckBox  m_recursiveCB;

    private JButton m_dismissButton;
    private JButton m_filterButton;
    private JButton m_removeFilterButton;

    private EntryListPane m_viewer;

    private SearchThread m_thread;

    private final static String SEARCH_TYPE_EXACT   = "EXACT";
    private final static String SEARCH_TYPE_KEYWORD = "KEYWORD";
    private final static String SEARCH_TYPE_REGEXP  = "REGEXP";

    private final static String SAVED_DATE_FORMAT   = "yyyy/MM/dd HH:mm";

    private final static int H_MARGIN = 10;
}
