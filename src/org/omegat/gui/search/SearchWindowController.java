/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Didier Briel
               2012 Didier Briel
               2013 Aaron Madlon-Kay, Alex Buloichik
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

package org.omegat.gui.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.omegat.core.Core;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.core.threads.SearchThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a window that appears when user'd like to search for something. For
 * each new user's request new window is created. Actual search is done by
 * SearchThread.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 * @author Aaron Madlon-Kay
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SearchWindowController {

    SearchWindowForm form;

    public SearchWindowController(MainWindow par, String startText) {
        form = new SearchWindowForm();

        m_parent = par;

        m_dateFormat = new SimpleDateFormat(SAVED_DATE_FORMAT);

        if (startText != null) {
            form.m_searchField.setText(startText);
        }

        // box DateBox
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        Date latestDate = initDate;

        m_dateFromModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
        form.m_dateFromSpinner.setModel(m_dateFromModel);

        m_dateToModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
        form.m_dateToSpinner.setModel(m_dateToModel);

        // Box Number of results
        SpinnerNumberModel m_numberModel = new SpinnerNumberModel(OConsts.ST_MAX_SEARCH_RESULTS, 1, Integer.MAX_VALUE,
                1);
        form.m_numberOfResults.setModel(m_numberModel);

        loadPreferences();

        ((EntryListPane) form.m_viewer).setFont(); // Otherwise, the
                                                   // user-defined font is not
                                                   // used

        if (!Core.getProject().isProjectLoaded()) {
            // restrict user to file only access
            form.m_rbDir.setSelected(true);
            form.m_rbProject.setEnabled(false);
        } else {
            form.m_rbProject.setSelected(true);
        }
        // update enabled/selected status of options
        updateOptionStatus();

        initActions();

        form.setVisible(true);
        form.m_searchField.requestFocus();
    }

    void initActions() {

        // ///////////////////////////////////
        // action listeners
        form.m_dismissButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });
        form.m_filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doFilter();
            }
        });
        form.m_removeFilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRemoveFilter();
            }
        });

        form.m_searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        ((MFindField) form.m_searchField).enterActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        };

        form.m_advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleAdvancedOptions();
            }
        });

        form.m_authorCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableDisableAuthor();
            }
        });

        form.m_dateToCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableDisableDateTo();
            }
        });

        form.m_dateToButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doResetDateTo();
            }
        });

        form.m_dateFromButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doResetDateFrom();
            }
        });

        form.m_dateFromCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableDisableDateFrom();
            }
        });

        form.m_dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectory();
            }
        });

        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        };
        form.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        form.getRootPane().getActionMap().put("ESCAPE", escapeAction);
        
        // Make Ctrl+F re-focus on search field
        KeyStroke find = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
        Action focusAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                form.m_searchField.requestFocus();
            }
        };
        form.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(find, "CTRL+F");
        form.getRootPane().getActionMap().put("CTRL+F", focusAction);

        // need to control check boxes and radio buttons manually
        //
        // keyword search can only be used when searching current project
        // TM search only works with exact/regex search on current project
        // file search only works with exact/regex search
        //
        // keep track of settings and only show what are valid choices

        form.m_exactSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_keywordSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_regexpSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_caseCB.addActionListener(searchFieldRequestFocus);

        form.m_searchSourceCB.addActionListener(searchFieldRequestFocus);

        form.m_searchTargetCB.addActionListener(searchFieldRequestFocus);

        form.m_searchNotesCB.addActionListener(searchFieldRequestFocus);

        form.m_cbSearchInGlossaries.addActionListener(searchFieldRequestFocus);
        form.m_cbSearchInMemory.addActionListener(searchFieldRequestFocus);
        form.m_cbSearchInTMs.addActionListener(searchFieldRequestFocus);

        form.m_allResultsCB.addActionListener(searchFieldRequestFocus);

        form.m_rbDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateOptionStatus();

                // move focus to dir edit field if dir search is selected
                // otherwise move focus to search field
                if (form.m_rbDir.isSelected())
                    form.m_dirField.requestFocus();
                else
                    form.m_searchField.requestFocus();
            }
        });
        form.m_rbProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateOptionStatus();
                form.m_searchField.requestFocus();
            }
        });

        form.m_numberOfResults.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // move focus to search edit field
                form.m_searchField.requestFocus();
            }
        });

        form.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                // save user preferences
                savePreferences();

                // notify main window
                m_parent.removeSearchWindow(SearchWindowController.this);

                if (m_thread != null) {
                    m_thread.fin();
                }
            }
        });
    }

    ActionListener searchFieldRequestFocus = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // move focus to search edit field
            form.m_searchField.requestFocus();
        }
    };

    /**
     * Loads the position and size of the search window and the button selection
     * state.
     */
    private void loadPreferences() {
        // window size and position
        try {
            String dx = Preferences.getPreference(Preferences.SEARCHWINDOW_X);
            String dy = Preferences.getPreference(Preferences.SEARCHWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            form.setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.SEARCHWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.SEARCHWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            form.setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            form.setSize(800, 700);
        }

        // search dir options
        if (Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_FILES, false)) {
            form.m_rbDir.setSelected(true);
        } else {
            form.m_rbProject.setSelected(true);
        }
        form.m_dirField.setText(Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_DIR, ""));
        form.m_recursiveCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_RECURSIVE, true));

        // search type
        String searchType = Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_EXACT);
        form.m_exactSearchRB.setSelected(searchType.equals(SEARCH_TYPE_EXACT));
        form.m_keywordSearchRB.setSelected(searchType.equals(SEARCH_TYPE_KEYWORD));
        form.m_regexpSearchRB.setSelected(searchType.equals(SEARCH_TYPE_REGEXP));

        // case sensitivity
        form.m_caseCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_CASE_SENSITIVE, false));

        // search source
        form.m_searchSourceCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_SOURCE, true));

        // search target
        form.m_searchTargetCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_TARGET, true));

        form.m_cbTranslated.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_TRANSLATED,
                false));

        form.m_searchNotesCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_NOTES, true));

        form.m_cbSearchInGlossaries.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_GLOSSARY_SEARCH, true));
        form.m_cbSearchInMemory.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_MEMORY_SEARCH,
                true));
        form.m_cbSearchInTMs.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_TM_SEARCH, true));

        // all results
        form.m_allResultsCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_ALL_RESULTS, false));

        // update the enabled/selected status of normal options
        updateOptionStatus();

        // load advanced options settings from user preferences
        loadAdvancedOptionPreferences();
        // update advanced options status
        updateAdvancedOptionStatus();
    }

    /**
     * Saves the size and position of the search window and the button selection
     * state
     */
    private void savePreferences() {
        // window size and position
        Preferences.setPreference(Preferences.SEARCHWINDOW_WIDTH, form.getWidth());
        Preferences.setPreference(Preferences.SEARCHWINDOW_HEIGHT, form.getHeight());
        Preferences.setPreference(Preferences.SEARCHWINDOW_X, form.getX());
        Preferences.setPreference(Preferences.SEARCHWINDOW_Y, form.getY());

        // search type
        if (form.m_exactSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_EXACT);
        else if (form.m_keywordSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_KEYWORD);
        else if (form.m_regexpSearchRB.isSelected())
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE, SEARCH_TYPE_REGEXP);

        // search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE, form.m_caseCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_SOURCE, form.m_searchSourceCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TARGET, form.m_searchTargetCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TRANSLATED, form.m_cbTranslated.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_NOTES, form.m_searchNotesCB.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_GLOSSARY_SEARCH, form.m_cbSearchInGlossaries.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_MEMORY_SEARCH, form.m_cbSearchInMemory.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_TM_SEARCH, form.m_cbSearchInTMs.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_ALL_RESULTS, form.m_allResultsCB.isSelected());
        // advanced search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE, form.m_advancedVisiblePane.isVisible());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR, form.m_authorCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME, form.m_authorField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM, form.m_dateFromCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE,
                m_dateFormat.format(m_dateFromModel.getDate()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO, form.m_dateToCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE, m_dateFormat.format(m_dateToModel.getDate()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_NUMBER_OF_RESULTS,
                ((Integer) form.m_numberOfResults.getValue()));

        // search dir options
        Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, form.m_dirField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_FILES, form.m_rbDir.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_RECURSIVE, form.m_recursiveCB.isSelected());

        // need to explicitly save preferences
        // because project might not be open
        Preferences.save();
    }

    /**
     * Updates the enabled/selected status of the options in the dialog.
     */
    private void updateOptionStatus() {
        setEnabled(form.m_SearchInProjectPane, form.m_rbProject.isSelected());
        form.m_rbProject.setEnabled(true);
        setEnabled(form.m_SearchInDirPane, form.m_rbDir.isSelected());
        form.m_rbDir.setEnabled(true);
    }

    // //////////////////////////////////////////////////////////////
    // interface for displaying text in viewer

    /**
     * Show search result for user
     */
    public void displaySearchResult(final List<SearchResultEntry> entries) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                EntryListPane viewer = (EntryListPane) form.m_viewer;
                viewer.displaySearchResult(entries, ((Integer) form.m_numberOfResults.getValue()));
                form.m_resultsLabel.setText(StaticUtils.format(OStrings.getString("SW_NR_OF_RESULTS"),
                        new Object[] { new Integer(viewer.getNrEntries()) }));
                form.m_filterButton.setEnabled(true);
                viewer.requestFocus();
            }
        });
    }

    // /////////////////////////////////////////////////////////////
    // internal functions

    private void doBrowseDirectory() {
        OmegaTFileChooser browser = new OmegaTFileChooser();
        // String str = OStrings.getString("BUTTON_SELECT");
        // browser.setApproveButtonText(str);
        browser.setDialogTitle(OStrings.getString("SW_TITLE"));
        browser.setFileSelectionMode(OmegaTFileChooser.DIRECTORIES_ONLY);
        String curDir = form.m_dirField.getText();

        if (!curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                browser.setCurrentDirectory(dir);
            }
        }

        browser.showOpenDialog(form);
        File dir = browser.getSelectedFile();
        if (dir == null)
            return;

        String str = dir.getAbsolutePath() + File.separator;
        form.m_dirField.setText(str);
    }

    private void doFilter() {
        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndLeave(); // Otherwise, the current segment being edited is lost
        Core.getEditor().addFilter(viewer.getEntryList());
        form.m_removeFilterButton.setEnabled(true);
    }

    private void doRemoveFilter() {
        form.m_removeFilterButton.setEnabled(false);
        Core.getEditor().removeFilter();
    }

    private void doSearch() {
        UIThreadsUtil.mustBeSwingThread();
        if (m_thread != null) {
            // stop old search thread
            m_thread.fin();
        }

        EntryListPane viewer = (EntryListPane) form.m_viewer;

        form.m_searchField.requestFocus();

        viewer.reset();
        String root = null;
        if (form.m_rbDir.isSelected()) {
            // make sure it's a valid directory name
            root = form.m_dirField.getText();
            if (!root.endsWith(File.separator))
                root += File.separator;
            File f = new File(root);
            if (!f.exists() || !f.isDirectory()) {
                String error = StaticUtils.format(OStrings.getString("SW_ERROR_BAD_DIR"),
                        new Object[] { form.m_dirField.getText() });
                form.m_viewer.setText(error);
                Log.log(error);
                return;
            }
            // if (m_dirCB.isSelected()) {
            // Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, root);
            // // need to explicitly save preferences because project
            // // might not be open
            // Preferences.save();
            // }
        }

        // save user preferences
        savePreferences();

        if (StringUtil.isEmpty(form.m_searchField.getText())) {
            form.setTitle(OStrings.getString("SW_TITLE"));
        } else {
            form.setTitle(form.m_searchField.getText() + " - OmegaT");
        }

        SearchExpression s = new SearchExpression();
        s.text = form.m_searchField.getText();
        s.rootDir = root;
        s.recursive = form.m_recursiveCB.isSelected();
        s.exact = form.m_exactSearchRB.isSelected();
        s.keyword = form.m_keywordSearchRB.isSelected();
        s.regex = form.m_regexpSearchRB.isSelected();
        s.caseSensitive = form.m_caseCB.isSelected();
        s.glossary = form.m_cbSearchInGlossaries.isSelected();
        s.memory = form.m_cbSearchInMemory.isSelected();
        s.tm = form.m_cbSearchInTMs.isSelected();
        s.allResults = form.m_allResultsCB.isSelected();
        s.searchSource = form.m_searchSourceCB.isSelected();
        s.searchTarget = form.m_searchTargetCB.isSelected();
        s.searchTranslatedOnly = form.m_cbTranslated.isSelected();
        s.searchNotes = form.m_searchNotesCB.isSelected();
        s.searchAuthor = form.m_authorCB.isSelected();
        s.author = form.m_authorField.getText();
        s.searchDateAfter = form.m_dateFromCB.isSelected();
        s.dateAfter = m_dateFromModel.getDate().getTime();
        s.searchDateBefore = form.m_dateToCB.isSelected();
        s.dateBefore = m_dateToModel.getDate().getTime();
        s.numberOfResults = ((Integer) form.m_numberOfResults.getValue());

        // start the search in a separate thread
        m_thread = new SearchThread(this, s);
        m_thread.start();
    }

    private void doCancel() {
        UIThreadsUtil.mustBeSwingThread();
        if (m_thread != null) {
            m_thread.fin();
        }
        form.dispose();
    }

    public void dispose() {
        form.dispose();
    }

    private void toggleAdvancedOptions() {
        form.m_advancedVisiblePane.setVisible(!form.m_advancedVisiblePane.isVisible());
        updateAdvancedOptionStatus();
    }

    private void enableDisableAuthor() {
        boolean editable = form.m_authorCB.isSelected();
        form.m_authorField.setEditable(editable);

        if (editable) {
            // move focus to author field
            form.m_authorField.requestFocus();
        } else {
            // move focus to search edit field
            form.m_searchField.requestFocus();
        }
    }

    private void enableDisableDateFrom() {
        boolean enable = form.m_dateFromCB.isSelected();
        form.m_dateFromSpinner.setEnabled(enable);
        form.m_dateFromButton.setEnabled(enable);

        if (enable) {
            // move focus to date spinner
            form.m_dateFromSpinner.requestFocus();
        } else {
            // move focus to search edit field
            form.m_searchField.requestFocus();
        }
    }

    private void doResetDateFrom() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        m_dateFromModel.setEnd(now);
        m_dateFromModel.setValue(now);
    }

    private void doResetDateTo() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        m_dateToModel.setEnd(now);
        m_dateToModel.setValue(now);
    }

    private void enableDisableDateTo() {
        boolean enable = form.m_dateToCB.isSelected();
        form.m_dateToSpinner.setEnabled(enable);
        form.m_dateToButton.setEnabled(enable);

        if (enable) {
            // move focus to date spinner
            form.m_dateToSpinner.requestFocus();
        } else {
            // move focus to search edit field
            form.m_searchField.requestFocus();
        }
    }

    private void loadAdvancedOptionPreferences() {
        // advanced options visibility
        String advancedVisible = Preferences.getPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE);
        if (StringUtil.isEmpty(advancedVisible))
            advancedVisible = "false";
        form.m_advancedVisiblePane.setVisible(Boolean.valueOf(advancedVisible).booleanValue());

        // author options
        String searchAuthor = Preferences.getPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR);
        if (StringUtil.isEmpty(searchAuthor))
            searchAuthor = "false";
        form.m_authorCB.setSelected(Boolean.valueOf(searchAuthor).booleanValue());
        String authorName = Preferences.getPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME);
        if (!StringUtil.isEmpty(authorName))
            form.m_authorField.setText(authorName);

        // date options
        try {
            // from date
            String dateFrom = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_FROM);
            if (StringUtil.isEmpty(dateFrom))
                dateFrom = "false";
            form.m_dateFromCB.setSelected(Boolean.valueOf(dateFrom).booleanValue());
            String dateFromValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE);
            if (!StringUtil.isEmpty(dateFromValue))
                m_dateFromModel.setValue(m_dateFormat.parse(dateFromValue));

            // to date
            String dateTo = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_TO);
            if (StringUtil.isEmpty(dateTo))
                dateTo = "false";
            form.m_dateToCB.setSelected(Boolean.valueOf(dateTo).booleanValue());
            String dateToValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE);
            if (!StringUtil.isEmpty(dateToValue))
                m_dateToModel.setValue(m_dateFormat.parse(dateToValue));
        } catch (ParseException e) {
            // use safe settings in case of parsing error
            form.m_dateFromCB.setSelected(false);
            form.m_dateToCB.setSelected(false);
        }

        // Number of results
        form.m_numberOfResults.setValue(Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_NUMBER_OF_RESULTS,
                OConsts.ST_MAX_SEARCH_RESULTS));

        // if advanced options are enabled (e.g. author/date search),
        // let the user see them anyway. This is important because
        // search results will be affected by these settings
        if (form.m_authorCB.isSelected() || form.m_dateFromCB.isSelected() || form.m_dateToCB.isSelected()) {
            form.m_advancedVisiblePane.setVisible(true);
        }
    }

    private void updateAdvancedOptionStatus() {
        form.m_authorField.setEditable(form.m_authorCB.isSelected());
        form.m_dateFromSpinner.setEnabled(form.m_dateFromCB.isSelected());
        form.m_dateFromButton.setEnabled(form.m_dateFromCB.isSelected());
        form.m_dateToSpinner.setEnabled(form.m_dateToCB.isSelected());
        form.m_dateToButton.setEnabled(form.m_dateToCB.isSelected());
    }

    /**
     * Set enabled/disabled component and all his children.
     * 
     * @param component
     * @param enabled
     */
    private void setEnabled(Container component, boolean enabled) {
        component.setEnabled(enabled);
        for (int i = 0; i < component.getComponentCount(); i++) {
            Component c = component.getComponent(i);
            c.setEnabled(enabled);
            if (c instanceof Container) {
                setEnabled((Container) c, enabled);
            }
        }
    }

    /**
     * Display message dialog with the error as message
     * 
     * @param ex
     *            exception to show
     * @param errorKey
     *            error message key in resource bundle
     * @param params
     *            error text parameters
     */
    public void displayErrorRB(final Throwable ex, final String errorKey, final Object... params) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                String msg;
                if (params != null) {
                    msg = StaticUtils.format(OStrings.getString(errorKey), params);
                } else {
                    msg = OStrings.getString(errorKey);
                }

                String fulltext = msg;
                if (ex != null)
                    fulltext += "\n" + ex.getLocalizedMessage();
                JOptionPane.showMessageDialog(form, fulltext, OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    MainWindow m_parent;

    private SimpleDateFormat m_dateFormat;
    private SpinnerDateModel m_dateFromModel, m_dateToModel;

    private SearchThread m_thread;

    private final static String SEARCH_TYPE_EXACT = "EXACT";
    private final static String SEARCH_TYPE_KEYWORD = "KEYWORD";
    private final static String SEARCH_TYPE_REGEXP = "REGEXP";

    private final static String SAVED_DATE_FORMAT = "yyyy/MM/dd HH:mm";

}
