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
               2014 Aaron Madlon-Kay, Piotr Kulik
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
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchMode;
import org.omegat.core.search.Searcher;
import org.omegat.core.threads.SearchThread;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.EditorController.CaretPosition;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.filter.ReplaceFilter;
import org.omegat.gui.editor.filter.SearchFilter;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;
import org.openide.awt.Mnemonics;

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
 * @author Piotr Kulik
 */
@SuppressWarnings("serial")
public class SearchWindowController {

    private final SearchWindowForm form;
    private final SearchMode mode;
    private final int initialEntry;
    private final CaretPosition initialCaret;

    public SearchWindowController(MainWindow par, SearchMode mode) {
        form = new SearchWindowForm();
        form.setJMenuBar(new SearchWindowMenu(form, this));
        this.mode = mode;
        initialEntry = Core.getEditor().getCurrentEntryNumber();
        initialCaret = getCurrentPositionInEntryTranslationInEditor(Core.getEditor());

        m_parent = par;

        m_dateFormat = new SimpleDateFormat(SAVED_DATE_FORMAT);
        
        form.m_searchField.setModel(new DefaultComboBoxModel(HistoryManager.getSearchItems()));
        if (form.m_searchField.getModel().getSize() > 0) {
            form.m_searchField.setSelectedIndex(-1);
        }
        
        form.m_replaceField.setModel(new DefaultComboBoxModel(HistoryManager.getReplaceItems()));
        if (form.m_replaceField.getModel().getSize() > 0) {
            form.m_replaceField.setSelectedIndex(-1);
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

        switch (mode) {
        case SEARCH:
            form.setTitle(OStrings.getString("SW_TITLE"));
            form.m_replaceLabel.setVisible(false);
            form.m_replaceField.setVisible(false);
            form.m_replaceAllButton.setVisible(false);
            form.m_replaceButton.setVisible(false);
            form.m_panelSearch.setVisible(true);
            form.m_panelReplace.setVisible(false);
            break;
        case REPLACE:
            form.setTitle(OStrings.getString("SW_TITLE_REPLACE"));
            form.m_SearchInPane.setVisible(false);
            form.m_allResultsCB.setVisible(false);
            form.m_fileNamesCB.setVisible(false);
            form.m_filterButton.setVisible(false);
            form.m_numberLabel.setVisible(false);;
            form.m_numberOfResults.setVisible(false);
            form.m_panelSearch.setVisible(false);
            form.m_panelReplace.setVisible(true);
            break;
        }
    }

    final void initActions() {

        // ///////////////////////////////////
        // action listeners
        form.m_dismissButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });
        form.m_filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFilter();
            }
        });
        form.m_replaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doReplace();
            }
        });
        form.m_replaceAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doReplaceAll();
            }
        });

        form.m_searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        form.m_advancedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAdvancedOptionsVisible(!form.m_advancedVisiblePane.isVisible());
            }
        });

        form.m_authorCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableDisableAuthor();
            }
        });
        
        ((MFindField) form.m_authorField).enterActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        };

        form.m_dateToCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableDisableDateTo();
            }
        });

        form.m_dateToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doResetDateTo();
            }
        });

        form.m_dateFromButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doResetDateFrom();
            }
        });

        form.m_dateFromCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableDisableDateFrom();
            }
        });

        form.m_dirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBrowseDirectory();
            }
        });

        StaticUIUtils.setEscapeAction(form, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        // Set search and replace combo boxes' actions, undo, key handling
        configureHistoryComboBox(form.m_searchField);
        configureHistoryComboBox(form.m_replaceField);        

        // need to control check boxes and radio buttons manually
        //
        // keyword search can only be used when searching current project
        // TM search only works with exact/regex search on current project
        // file search only works with exact/regex search
        //
        // keep track of settings and only show what are valid choices

        form.m_searchExactSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_searchKeywordSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_searchRegexpSearchRB.addActionListener(searchFieldRequestFocus);

        form.m_searchCase.addActionListener(searchFieldRequestFocus);
        form.m_searchSpaceMatchNbsp.addActionListener(searchFieldRequestFocus);

        form.m_searchSource.addActionListener(searchFieldRequestFocus);
        form.m_searchTranslation.addActionListener(searchFieldRequestFocus);

        form.m_searchTranslatedUntranslated.addActionListener(searchFieldRequestFocus);
        form.m_searchTranslated.addActionListener(searchFieldRequestFocus);
        form.m_searchUntranslated.addActionListener(searchFieldRequestFocus);

        form.m_searchNotesCB.addActionListener(searchFieldRequestFocus);
        form.m_searchCommentsCB.addActionListener(searchFieldRequestFocus);

        form.m_cbSearchInGlossaries.addActionListener(searchFieldRequestFocus);
        form.m_cbSearchInMemory.addActionListener(searchFieldRequestFocus);
        form.m_cbSearchInTMs.addActionListener(searchFieldRequestFocus);

        form.m_allResultsCB.addActionListener(searchFieldRequestFocus);
        form.m_fileNamesCB.addActionListener(searchFieldRequestFocus);

        form.m_autoSyncWithEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // update auto-sync flag in EntryListPane
                EntryListPane viewer = (EntryListPane) form.m_viewer;
                viewer.setAutoSyncWithEditor(form.m_autoSyncWithEditor.isSelected());
            }
        });

        form.m_rbDir.addActionListener(new ActionListener() {
            @Override
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
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOptionStatus();
                form.m_searchField.requestFocus();
            }
        });

        form.m_numberOfResults.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // move focus to search edit field
                form.m_searchField.requestFocus();
            }
        });

        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // save user preferences
                savePreferences();

                // notify main window
                m_parent.removeSearchWindow(SearchWindowController.this);

                if (m_thread != null) {
                    m_thread.fin();
                }

                // back to the initial segment
                int currentEntry = Core.getEditor().getCurrentEntryNumber();
                if (initialEntry > 0 && form.m_backToInitialSegment.isSelected() && initialEntry != currentEntry) {
                    boolean isSegDisplayed = isSegmentDisplayed(initialEntry);
                    if (isSegDisplayed) {
                        // Restore caretPosition too
                        ((EditorController) Core.getEditor()).gotoEntry(initialEntry, initialCaret);
                    } else {
                        // The segment is not displayed (maybe filter on). Ignore caretPosition.
                        Core.getEditor().gotoEntry(initialEntry);
                    }
                }
            }
        });
    }
    
    private void configureHistoryComboBox(final JComboBox box) {
        final JTextField field = (JTextField) box.getEditor().getEditorComponent();
        InputMap map = field.getInputMap();
        
        final UndoManager undoManager = new UndoManager();
        field.getDocument().addUndoableEditListener(undoManager);
        
        // Set up undo/redo handling
        KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
        map.put(undoKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });
        KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
        map.put(redoKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });
        
        // Close dialog with Esc key
        map.put(KeyStroke.getKeyStroke("ESCAPE"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (box.isPopupVisible()) {
                    box.hidePopup();
                } else {
                    doCancel();
                }
            }
        });
        
        // Perform search on Enter key (if search field not empty)
        field.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!form.m_searchField.getEditor().getItem().toString().isEmpty()) {
                    doSearch();
                }
            }
        });
    }

    ActionListener searchFieldRequestFocus = new ActionListener() {
        @Override
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
        SearchExpression.SearchExpressionType searchType = Preferences.getPreferenceEnumDefault(
                Preferences.SEARCHWINDOW_SEARCH_TYPE,
                SearchExpression.SearchExpressionType.EXACT);
        switch (searchType) {
        case EXACT:
        default:
            form.m_searchExactSearchRB.setSelected(true);
            break;
        case KEYWORD:
            form.m_searchKeywordSearchRB.setSelected(true);
            break;
        case REGEXP:
            form.m_searchRegexpSearchRB.setSelected(true);
            break;
        }

        // case sensitivity
        form.m_searchCase.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_CASE_SENSITIVE, false));

        // nbsp as space
        form.m_searchSpaceMatchNbsp.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP, false));

        // search source
        form.m_searchSource.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_SEARCH_SOURCE, true));
        form.m_searchTranslation.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_SEARCH_TRANSLATION, true));

        SearchExpression.SearchState searchState = SearchExpression.SearchState.valueOf(Preferences
                .getPreferenceEnumDefault(Preferences.SEARCHWINDOW_SEARCH_STATE,
                        SearchExpression.SearchState.TRANSLATED_UNTRANSLATED).name());
        switch (searchState) {
        case TRANSLATED_UNTRANSLATED:
        default:
            form.m_searchTranslatedUntranslated.setSelected(true);
            break;
        case TRANSLATED:
            form.m_searchTranslated.setSelected(true);
            break;
        case UNTRANSLATED:
            form.m_searchUntranslated.setSelected(true);
            break;
        }

        // case sensitivity
        form.m_replaceCase.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_CASE_SENSITIVE_REPLACE, false));

                // nbsp as space
        form.m_replaceSpaceMatchNbsp.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP_REPLACE, false));

        // replace type
        SearchExpression.SearchExpressionType replaceType = SearchExpression.SearchExpressionType
                .valueOf(Preferences.getPreferenceEnumDefault(Preferences.SEARCHWINDOW_REPLACE_TYPE,
                        SearchExpression.SearchExpressionType.EXACT).name());
        switch (replaceType) {
        case EXACT:
        default:
            form.m_replaceExactSearchRB.setSelected(true);
            break;
        case REGEXP:
            form.m_replaceRegexpSearchRB.setSelected(true);
            break;
        }

        form.m_replaceUntranslated.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_REPLACE_UNTRANSLATED, true));

        form.m_searchNotesCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_NOTES, true));
        form.m_searchCommentsCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_COMMENTS, true));

        form.m_cbSearchInGlossaries.setSelected(Preferences.isPreferenceDefault(
                Preferences.SEARCHWINDOW_GLOSSARY_SEARCH, true));
        form.m_cbSearchInMemory.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_MEMORY_SEARCH,
                true));
        form.m_cbSearchInTMs.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_TM_SEARCH, true));

        // all results
        form.m_allResultsCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_ALL_RESULTS, false));
        form.m_fileNamesCB.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_FILE_NAMES, false));

        // editor related options
        form.m_autoSyncWithEditor.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_AUTO_SYNC, false));
        form.m_backToInitialSegment.setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_BACK_TO_INITIAL_SEGMENT, false));

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
        if (form.m_searchExactSearchRB.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE,
                    SearchExpression.SearchExpressionType.EXACT);
        } else if (form.m_searchKeywordSearchRB.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE,
                    SearchExpression.SearchExpressionType.KEYWORD);
        } else if (form.m_searchRegexpSearchRB.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TYPE,
                    SearchExpression.SearchExpressionType.REGEXP);
        }

        // search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE, form.m_searchCase.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP, form.m_searchSpaceMatchNbsp.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_SOURCE, form.m_searchSource.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TRANSLATION, form.m_searchTranslation.isSelected());

        if (form.m_searchTranslatedUntranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.TRANSLATED_UNTRANSLATED.name());
        } else if (form.m_searchTranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.TRANSLATED.name());
        } else if (form.m_searchUntranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.UNTRANSLATED.name());
        }

        // replace options
        Preferences.setPreference(Preferences.SEARCHWINDOW_CASE_SENSITIVE_REPLACE,
                form.m_replaceCase.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP_REPLACE,
                form.m_replaceSpaceMatchNbsp.isSelected());
        if (form.m_replaceExactSearchRB.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_REPLACE_TYPE,
                    SearchExpression.SearchExpressionType.EXACT);
        } else if (form.m_replaceRegexpSearchRB.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_REPLACE_TYPE,
                    SearchExpression.SearchExpressionType.REGEXP);
        }
        Preferences.setPreference(Preferences.SEARCHWINDOW_REPLACE_UNTRANSLATED,
                form.m_replaceUntranslated.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_NOTES, form.m_searchNotesCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_COMMENTS, form.m_searchCommentsCB.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_GLOSSARY_SEARCH, form.m_cbSearchInGlossaries.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_MEMORY_SEARCH, form.m_cbSearchInMemory.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_TM_SEARCH, form.m_cbSearchInTMs.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_ALL_RESULTS, form.m_allResultsCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_FILE_NAMES, form.m_fileNamesCB.isSelected());
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
        Preferences.setPreference(Preferences.SEARCHWINDOW_EXCLUDE_ORPHANS, form.m_excludeOrphans.isSelected());

        // search dir options
        Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, form.m_dirField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_FILES, form.m_rbDir.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_RECURSIVE, form.m_recursiveCB.isSelected());

        // editor related options
        Preferences.setPreference(Preferences.SEARCHWINDOW_AUTO_SYNC, form.m_autoSyncWithEditor.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_BACK_TO_INITIAL_SEGMENT, form.m_backToInitialSegment.isSelected());

        // Search/replace history
        HistoryManager.save();
        
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
    public void displaySearchResult(final Searcher searcher) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            @Override
            public void run() {
                EntryListPane viewer = (EntryListPane) form.m_viewer;
                viewer.displaySearchResult(searcher, ((Integer) form.m_numberOfResults.getValue()));
                form.m_resultsLabel.setText(StringUtil.format(OStrings.getString("SW_NR_OF_RESULTS"),
                        viewer.getNrEntries()));
                form.m_filterButton.setEnabled(true);
                form.m_replaceButton.setEnabled(true);
                form.m_replaceAllButton.setEnabled(true);
                if (searcher.getSearchResults().isEmpty()) {
                    // RFE#1143 https://sourceforge.net/p/omegat/feature-requests/1143/
                    form.m_searchField.requestFocus();
                    form.m_searchField.getEditor().selectAll();
                } else {
                    viewer.requestFocus();                    
                }
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
        Core.getEditor().setFilter(new SearchFilter(viewer.getEntryList()));
    }

    private void doReplace() {
        String replaceString = form.m_replaceField.getEditor().getItem().toString();
        HistoryManager.addReplaceItem(replaceString);
        form.m_replaceField.setModel(new DefaultComboBoxModel(HistoryManager.getReplaceItems()));
        
        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndLeave(); // Otherwise, the current segment being edited is lost
        Core.getEditor()
                .setFilter(
                        new ReplaceFilter(viewer.getEntryList(), viewer.getSearcher(), replaceString));
    }

    private void doReplaceAll() {
        String replaceString = form.m_replaceField.getEditor().getItem().toString();
        HistoryManager.addReplaceItem(replaceString);
        form.m_replaceField.setModel(new DefaultComboBoxModel(HistoryManager.getReplaceItems()));
        
        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndDeactivate(); // Otherwise, the current segment being edited is lost
        int count = viewer.getEntryList().size();
        String msg = MessageFormat.format(OStrings.getString("SW_REPLACE_ALL_CONFIRM"), count);
        int r = JOptionPane.showConfirmDialog(form, msg, OStrings.getString("CONFIRM_DIALOG_TITLE"),
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            new ReplaceFilter(viewer.getEntryList(), viewer.getSearcher(), replaceString).replaceAll();
        }
        Core.getEditor().activateEntry();
        form.m_replaceButton.setEnabled(false);
        form.m_replaceAllButton.setEnabled(false);
    }

    private void doSearch() {
        UIThreadsUtil.mustBeSwingThread();
        if (m_thread != null) {
            // stop old search thread
            m_thread.fin();
        }

        EntryListPane viewer = (EntryListPane) form.m_viewer;

        String queryString = form.m_searchField.getEditor().getItem().toString();
        
        HistoryManager.addSearchItem(queryString);
        form.m_searchField.setModel(new DefaultComboBoxModel(HistoryManager.getSearchItems()));
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
                String error = StringUtil.format(OStrings.getString("SW_ERROR_BAD_DIR"),
                        form.m_dirField.getText());
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

        if (StringUtil.isEmpty(queryString)) {
            form.setTitle(OStrings.getString("SW_TITLE"));
        } else {
            form.setTitle(queryString + " - OmegaT");
        }

        SearchExpression s = new SearchExpression();
        s.mode = mode;
        s.text = queryString;
        s.rootDir = root;
        s.recursive = form.m_recursiveCB.isSelected();

        switch (mode) {
        case SEARCH:
            if (form.m_searchExactSearchRB.isSelected()) {
                s.searchExpressionType = SearchExpression.SearchExpressionType.EXACT;
            } else if (form.m_searchKeywordSearchRB.isSelected()) {
                s.searchExpressionType = SearchExpression.SearchExpressionType.KEYWORD;
            } else if (form.m_searchRegexpSearchRB.isSelected()) {
                s.searchExpressionType = SearchExpression.SearchExpressionType.REGEXP;
            }
            s.caseSensitive = form.m_searchCase.isSelected();
            s.spaceMatchNbsp = form.m_searchSpaceMatchNbsp.isSelected();
            s.glossary = mode == SearchMode.SEARCH ? form.m_cbSearchInGlossaries.isSelected() : false;
            s.memory = mode == SearchMode.SEARCH ? form.m_cbSearchInMemory.isSelected() : true;
            s.tm = mode == SearchMode.SEARCH ? form.m_cbSearchInTMs.isSelected() : false;
            s.allResults = mode == SearchMode.SEARCH ? form.m_allResultsCB.isSelected() : true;
            s.fileNames = mode == SearchMode.SEARCH ? form.m_fileNamesCB.isSelected() : true;
            s.searchSource = form.m_searchSource.isSelected();
            s.searchTarget = form.m_searchTranslation.isSelected();
            if (form.m_searchTranslatedUntranslated.isSelected()) {
                s.searchTranslated = true;
                s.searchUntranslated = true;
            } else if (form.m_searchTranslated.isSelected()) {
                s.searchTranslated = true;
                s.searchUntranslated = false;
            } else if (form.m_searchUntranslated.isSelected()) {
                s.searchTranslated = false;
                s.searchUntranslated = true;
            }
            break;
        case REPLACE:
            if (form.m_replaceExactSearchRB.isSelected()) {
                s.searchExpressionType = SearchExpression.SearchExpressionType.EXACT;
            } else if (form.m_replaceRegexpSearchRB.isSelected()) {
                s.searchExpressionType = SearchExpression.SearchExpressionType.REGEXP;
            }
            s.caseSensitive = form.m_replaceCase.isSelected();
            s.spaceMatchNbsp = form.m_replaceSpaceMatchNbsp.isSelected();
            s.glossary = false;
            s.memory = true;
            s.tm = false;
            s.allResults = true;
            s.fileNames = Core.getProject().getProjectFiles().size() > 1;
            s.searchSource = false;
            s.searchTarget = false;
            s.searchTranslated = false;
            s.searchUntranslated = false;
            s.replaceTranslated = true;
            s.replaceUntranslated = form.m_replaceUntranslated.isSelected();
            break;
        }

        s.searchNotes = form.m_searchNotesCB.isSelected();
        s.searchComments = form.m_searchCommentsCB.isSelected();
        s.searchAuthor = form.m_authorCB.isSelected();
        s.author = form.m_authorField.getText();
        s.searchDateAfter = form.m_dateFromCB.isSelected();
        s.dateAfter = m_dateFromModel.getDate().getTime();
        s.searchDateBefore = form.m_dateToCB.isSelected();
        s.dateBefore = m_dateToModel.getDate().getTime();
        s.numberOfResults = mode == SearchMode.SEARCH ? ((Integer) form.m_numberOfResults.getValue())
                : Integer.MAX_VALUE;
        s.excludeOrphans = form.m_excludeOrphans.isSelected();

        Searcher searcher = new Searcher(Core.getProject(), s);
        // start the search in a separate thread
        m_thread = new SearchThread(this, searcher);
        m_thread.start();
    }

    void doCancel() {
        UIThreadsUtil.mustBeSwingThread();
        if (m_thread != null) {
            m_thread.fin();
        }
        form.dispose();
    }

    public void dispose() {
        form.dispose();
    }

    /**
     * Make Search window visible on screen, with optional initial query (may be
     * null).
     * 
     * @param query
     *            Initial query string (may be empty or null)
     */
    public void makeVisible(String query) {
        if (!StringUtil.isEmpty(query)) {
            ((JTextField) form.m_searchField.getEditor().getEditorComponent()).setText(query);
        }
        form.setVisible(true);
        form.m_searchField.requestFocus();
    }

    private boolean isSegmentDisplayed(int entry) {
        IEditorFilter filter = Core.getEditor().getFilter();
        if (filter == null) {
            return true;
        } else {
            SourceTextEntry ste = Core.getProject().getAllEntries().get(entry - 1);
            return filter.allowed(ste);
        }
    }

    private void setAdvancedOptionsVisible(boolean visible) {
        form.m_advancedVisiblePane.setVisible(visible);
        Mnemonics.setLocalizedText(form.m_advancedButton,
                visible ? OStrings.getString("SW_HIDE_ADVANCED_OPTIONS")
                        : OStrings.getString("SW_SHOW_ADVANCED_OPTIONS"));
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
        setAdvancedOptionsVisible(Preferences.isPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE));

        // author options
        form.m_authorCB.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR));
        form.m_authorField.setText(Preferences.getPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME));

        // date options
        try {
            // from date
            form.m_dateFromCB.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_DATE_FROM));
            String dateFromValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE);
            if (!StringUtil.isEmpty(dateFromValue))
                m_dateFromModel.setValue(m_dateFormat.parse(dateFromValue));

            // to date
            form.m_dateToCB.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_DATE_TO));
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

        form.m_excludeOrphans.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_EXCLUDE_ORPHANS));
        
        // if advanced options are enabled (e.g. author/date search),
        // let the user see them anyway. This is important because
        // search results will be affected by these settings
        if (form.m_authorCB.isSelected() || form.m_dateFromCB.isSelected() || form.m_dateToCB.isSelected()
                || form.m_excludeOrphans.isSelected()) {
            setAdvancedOptionsVisible(true);
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

    private CaretPosition getCurrentPositionInEntryTranslationInEditor(IEditor editor) {
        if (editor instanceof EditorController) {
            EditorController c = (EditorController) editor;
            int selectionEnd = c.getCurrentPositionInEntryTranslation();
            String selection = c.getSelectedText();
            String translation = c.getCurrentTranslation();

            if (StringUtil.isEmpty(translation) || StringUtil.isEmpty(selection)) {
                // no translation or no selection
                return new CaretPosition(selectionEnd);
            } else {
                // get selected range
                int selectionStart = selectionEnd;
                int pos = 0;
                do {
                    pos = translation.indexOf(selection, pos);
                    if (pos == selectionEnd) {
                        selectionStart = pos;
                        selectionEnd = pos + selection.length();
                        break;
                    } else if ((pos + selection.length()) == selectionEnd) {
                        selectionStart = pos;
                        break;
                    }
                    pos++;
                } while (pos > 0);
                return new CaretPosition(selectionStart, selectionEnd);
            }
        } else {
            return CaretPosition.startOfEntry();
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
            @Override
            public void run() {
                String msg;
                if (params != null) {
                    msg = StringUtil.format(OStrings.getString(errorKey), params);
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

    private final static String SAVED_DATE_FORMAT = "yyyy/MM/dd HH:mm";

}
