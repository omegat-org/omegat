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
               2015 Yu Tang, Aaron Madlon-Kay, Hiroshi Miura
               2017-2018 Thomas Cordonnier
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

package org.omegat.gui.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;

import org.omegat.gui.editor.IEditor;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchMode;
import org.omegat.core.search.Searcher;
import org.omegat.core.threads.SearchThread;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor.CaretPosition;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.filter.ReplaceFilter;
import org.omegat.gui.editor.filter.SearchFilter;
import org.omegat.util.Java8Compat;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;
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
 * @author Piotr Kulik
 * @author Hiroshi Miura
 * @author Thomas Cordonnier
 */
@SuppressWarnings("serial")
public class SearchWindowController {

    private final SearchWindowForm form;
    private final SearchMode mode;
    private final int initialEntry;
    private final CaretPosition initialCaret;

    public SearchWindowController(SearchMode mode) {
        form = new SearchWindowForm();
        form.setJMenuBar(new SearchWindowMenu(this));
        Font f = Core.getMainWindow().getApplicationFont();
        setFont(f);

        this.mode = mode;
        initialEntry = Core.getEditor().getCurrentEntryNumber();
        initialCaret = Core.getEditor().getCurrentPositionInEntryTranslationInEditor();

        if (Platform.isMacOSX()) {
            OSXIntegration.enableFullScreen(form);
        }
        dateFormat = new SimpleDateFormat(SAVED_DATE_FORMAT);

        form.m_searchField.setModel(new DefaultComboBoxModel<>(HistoryManager.getSearchItems()));
        if (form.m_searchField.getModel().getSize() > 0) {
            form.m_searchField.setSelectedIndex(-1);
        }

        form.m_replaceField.setModel(new DefaultComboBoxModel<>(HistoryManager.getReplaceItems()));
        if (form.m_replaceField.getModel().getSize() > 0) {
            form.m_replaceField.setSelectedIndex(-1);
        }

        // box DateBox
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        Date latestDate = initDate;

        dateFromModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
        form.m_dateFromSpinner.setModel(dateFromModel);

        dateToModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
        form.m_dateToSpinner.setModel(dateToModel);

        // Box Number of results
        SpinnerNumberModel numberModel = new SpinnerNumberModel(OConsts.ST_MAX_SEARCH_RESULTS, 1,
                Integer.MAX_VALUE, 1);
        form.m_numberOfResults.setModel(numberModel);

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
            form.m_numberLabel.setVisible(false);
            form.m_numberOfResults.setVisible(false);
            form.m_panelSearch.setVisible(false);
            form.m_panelReplace.setVisible(true);
            form.m_excludeOrphans.setVisible(false);
            break;
        }
        setComponentNames();
        CoreEvents.registerFontChangedEventListener(this::setFont);
    }

    private void setComponentNames() {
        form.m_searchLabel.setName("SearchWindowForm.m_searchLabel");
        form.m_searchField.setName("SearchWindowForm.m_searchField");
        form.m_searchExactSearchRB.setName("SearchWindowForm.m_searchExactSearchRB");
        form.m_searchKeywordSearchRB.setName("SearchWindowForm.m_searchKeywordSearchRB");
        form.m_searchRegexpSearchRB.setName("SearchWindowForm.m_searchRegexpSearchRB");
        form.m_searchCase.setName("SearchWindowForm.m_searchCase");
        form.m_searchSpaceMatchNbsp.setName("SearchWindowForm.m_searchSpaceMatchNbsp");
        form.m_searchSource.setName("SearchWindowForm.m_searchSource");
        form.m_searchTranslation.setName("SearchWindowForm.m_searchTranslation");
        form.m_searchTranslatedUntranslated.setName("SearchWindowForm.m_searchTranslatedUntranslated");
        form.m_searchTranslated.setName("SearchWindowForm.m_searchTranslated");
        form.m_searchButton.setName("SearchWindowForm.m_searchButton");
        form.m_viewer.setName("SearchWindowForm.m_viewer");
    }

    public SearchMode getMode() {
        return mode;
    }

    final void initActions() {

        // ///////////////////////////////////
        // action listeners
        form.m_dismissButton.addActionListener(e -> doCancel());
        form.m_filterButton.addActionListener(e -> doFilter());
        form.m_replaceButton.addActionListener(e -> doReplace());
        form.m_replaceAllButton.addActionListener(e -> doReplaceAll());

        form.m_searchButton.addActionListener(e -> doSearch());
        form.m_advancedButton
                .addActionListener(e -> setAdvancedOptionsVisible(!form.m_advancedVisiblePane.isVisible()));

        form.m_authorCB.addActionListener(e -> enableDisableAuthor());

        form.m_authorField.addActionListener(e -> doSearch());

        form.m_dateToCB.addActionListener(e -> enableDisableDateTo());

        form.m_dateToButton.addActionListener(e -> doResetDateTo());

        form.m_dateFromButton.addActionListener(e -> doResetDateFrom());

        form.m_dateFromCB.addActionListener(e -> enableDisableDateFrom());

        form.m_dirButton.addActionListener(e -> doBrowseDirectory());

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

        ActionListener searchFieldRequestFocus = e -> form.m_searchField.requestFocus();

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

        form.m_autoSyncWithEditor.addActionListener(e -> {
            // update auto-sync flag in EntryListPane
            EntryListPane viewer = (EntryListPane) form.m_viewer;
            viewer.setAutoSyncWithEditor(form.m_autoSyncWithEditor.isSelected());
        });

        form.m_rbDir.addActionListener(e -> {
            updateOptionStatus();

            // move focus to dir edit field if dir search is selected
            // otherwise move focus to search field
            if (form.m_rbDir.isSelected()) {
                form.m_dirField.requestFocus();
            } else {
                form.m_searchField.requestFocus();
            }
        });
        form.m_rbProject.addActionListener(e -> {
            updateOptionStatus();
            form.m_searchField.requestFocus();
        });

        form.m_numberOfResults.addChangeListener(e -> {
            // move focus to search edit field
            form.m_searchField.requestFocus();
        });

        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // save user preferences
                savePreferences();

                if (thread != null) {
                    thread.fin();
                }

                IEditor editor = Core.getEditor();
                // Skip when test
                if (editor instanceof EditorController) {
                    // back to the initial segment
                    int currentEntry = editor.getCurrentEntryNumber();
                    if (initialEntry > 0 && form.m_backToInitialSegment.isSelected()
                            && initialEntry != currentEntry) {
                        boolean isSegDisplayed = isSegmentDisplayed(initialEntry);
                        if (isSegDisplayed) {
                            // Restore caretPosition too
                            editor.gotoEntry(initialEntry, initialCaret);
                        } else {
                            // The segment is not displayed (maybe filter on).
                            // Ignore caretPosition.
                            editor.gotoEntry(initialEntry);
                        }
                    }
                }
            }
        });
    }

    private void setFont(Font f) {
        form.setFont(f);
        form.m_searchField.setFont(f);
        form.m_replaceField.setFont(f);
        form.m_viewer.setFont(f);
        form.revalidate();
    }

    private void configureHistoryComboBox(final JComboBox<String> box) {
        final JTextField field = (JTextField) box.getEditor().getEditorComponent();
        InputMap map = field.getInputMap();

        final UndoManager undoManager = new UndoManager();
        field.getDocument().addUndoableEditListener(undoManager);

        // Invalidate replacement if search or replace strings change.
        // Otherwise you can accidentally do the wrong thing like:
        // 1. Search for "foo"
        // 2. Enter "bar" in replacement field
        // 3. Hit "Replace all"
        // => You replaced "foo" with "" because you didn't re-search after
        // entering "bar"
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                invalidateReplacement();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                invalidateReplacement();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                invalidateReplacement();
            }

            private void invalidateReplacement() {
                form.m_replaceButton.setEnabled(false);
                form.m_replaceAllButton.setEnabled(false);
            }
        });

        // Set up undo/redo handling
        KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Java8Compat.getMenuShortcutKeyMaskEx(),
                false);
        map.put(undoKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });
        KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Java8Compat.getMenuShortcutKeyMaskEx(),
                false);
        map.put(redoKey, new AbstractAction() {
            @Override
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

    /**
     * Loads the position and size of the search window and the button selection
     * state.
     */
    private void loadPreferences() {
        // set default size and position
        form.setSize(800, 700);
        StaticUIUtils.persistGeometry(form, Preferences.SEARCHWINDOW_GEOMETRY_PREFIX);

        // search dir options
        if (Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_FILES, false)) {
            form.m_rbDir.setSelected(true);
        } else {
            form.m_rbProject.setSelected(true);
        }
        form.m_dirField.setText(Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_DIR, ""));
        form.m_recursiveCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_RECURSIVE, true));

        // search type
        SearchExpression.SearchExpressionType searchType = Preferences.getPreferenceEnumDefault(
                Preferences.SEARCHWINDOW_SEARCH_TYPE, SearchExpression.SearchExpressionType.EXACT);
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
        form.m_searchCase
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_CASE_SENSITIVE, false));

        // nbsp as space
        form.m_searchSpaceMatchNbsp.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP, false));

        // search source
        form.m_searchSource
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_SOURCE, true));
        form.m_searchTranslation.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_TRANSLATION, true));

        SearchExpression.SearchState searchState = Preferences.getPreferenceEnumDefault(
                Preferences.SEARCHWINDOW_SEARCH_STATE, SearchExpression.SearchState.TRANSLATED_UNTRANSLATED);
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
        form.m_replaceCase.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_CASE_SENSITIVE_REPLACE, false));

        // nbsp as space
        form.m_replaceSpaceMatchNbsp.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP_REPLACE, false));

        // replace type
        SearchExpression.SearchExpressionType replaceType = Preferences.getPreferenceEnumDefault(
                Preferences.SEARCHWINDOW_REPLACE_TYPE, SearchExpression.SearchExpressionType.EXACT);
        switch (replaceType) {
        case EXACT:
        default:
            form.m_replaceExactSearchRB.setSelected(true);
            break;
        case REGEXP:
            form.m_replaceRegexpSearchRB.setSelected(true);
            break;
        }

        form.m_replaceUntranslated.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_REPLACE_UNTRANSLATED, true));

        form.m_searchNotesCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_NOTES, true));
        form.m_searchCommentsCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_COMMENTS, true));

        form.m_cbSearchInGlossaries
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_GLOSSARY_SEARCH, true));
        form.m_cbSearchInMemory
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_MEMORY_SEARCH, true));
        form.m_cbSearchInTMs
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_TM_SEARCH, true));

        // all results
        form.m_allResultsCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_ALL_RESULTS, false));
        form.m_fileNamesCB
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_FILE_NAMES, false));

        // editor related options
        form.m_autoSyncWithEditor
                .setSelected(Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_AUTO_SYNC, true));
        form.m_backToInitialSegment.setSelected(
                Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_BACK_TO_INITIAL_SEGMENT, true));

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
        Preferences.setPreference(Preferences.SEARCHWINDOW_SPACE_MATCH_NBSP,
                form.m_searchSpaceMatchNbsp.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_SOURCE, form.m_searchSource.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_TRANSLATION,
                form.m_searchTranslation.isSelected());

        if (form.m_searchTranslatedUntranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.TRANSLATED_UNTRANSLATED);
        } else if (form.m_searchTranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.TRANSLATED);
        } else if (form.m_searchUntranslated.isSelected()) {
            Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_STATE,
                    SearchExpression.SearchState.UNTRANSLATED);
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
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_COMMENTS,
                form.m_searchCommentsCB.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_GLOSSARY_SEARCH,
                form.m_cbSearchInGlossaries.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_MEMORY_SEARCH,
                form.m_cbSearchInMemory.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_TM_SEARCH, form.m_cbSearchInTMs.isSelected());

        Preferences.setPreference(Preferences.SEARCHWINDOW_ALL_RESULTS, form.m_allResultsCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_FILE_NAMES, form.m_fileNamesCB.isSelected());
        // advanced search options
        Preferences.setPreference(Preferences.SEARCHWINDOW_ADVANCED_VISIBLE,
                form.m_advancedVisiblePane.isVisible());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_AUTHOR, form.m_authorCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_AUTHOR_NAME, form.m_authorField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM, form.m_dateFromCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_FROM_VALUE,
                dateFormat.format(dateFromModel.getDate()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO, form.m_dateToCB.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE,
                dateFormat.format(dateToModel.getDate()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_NUMBER_OF_RESULTS,
                ((Integer) form.m_numberOfResults.getValue()));
        Preferences.setPreference(Preferences.SEARCHWINDOW_EXCLUDE_ORPHANS,
                form.m_excludeOrphans.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_FULLHALFWIDTH_INSENSITIVE,
                form.m_fullHalfWidthInsensitive.isSelected());

        // search dir options
        Preferences.setPreference(Preferences.SEARCHWINDOW_DIR, form.m_dirField.getText());
        Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_FILES, form.m_rbDir.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_RECURSIVE, form.m_recursiveCB.isSelected());

        // editor related options
        Preferences.setPreference(Preferences.SEARCHWINDOW_AUTO_SYNC, form.m_autoSyncWithEditor.isSelected());
        Preferences.setPreference(Preferences.SEARCHWINDOW_BACK_TO_INITIAL_SEGMENT,
                form.m_backToInitialSegment.isSelected());

        // Search/replace history
        HistoryManager.save();

        // need to explicitly save preferences
        // because project might not be open
        Preferences.save();
    }

    /**
     * Reset search options to their default values. Search terms are left
     * unchanged, as are any settings that don't affect the search results (such
     * as syncing with editor).
     */
    public void resetOptions() {
        form.m_rbProject.setSelected(true);
        form.m_recursiveCB.setSelected(true);
        form.m_searchExactSearchRB.setSelected(true);
        form.m_searchCase.setSelected(false);

        form.m_searchSpaceMatchNbsp.setSelected(false);

        form.m_searchSource.setSelected(true);
        form.m_searchTranslation.setSelected(true);

        form.m_searchTranslatedUntranslated.setSelected(true);
        form.m_replaceCase.setSelected(false);

        form.m_replaceSpaceMatchNbsp.setSelected(false);
        form.m_replaceExactSearchRB.setSelected(true);

        form.m_replaceUntranslated.setSelected(true);

        form.m_searchNotesCB.setSelected(true);
        form.m_searchCommentsCB.setSelected(true);

        form.m_cbSearchInGlossaries.setSelected(true);
        form.m_cbSearchInMemory.setSelected(true);
        form.m_cbSearchInTMs.setSelected(true);

        form.m_allResultsCB.setSelected(false);
        form.m_fileNamesCB.setSelected(false);

        updateOptionStatus();

        form.m_authorCB.setSelected(false);
        form.m_dateFromCB.setSelected(false);
        form.m_dateToCB.setSelected(false);
        form.m_numberOfResults.setValue(OConsts.ST_MAX_SEARCH_RESULTS);
        form.m_excludeOrphans.setSelected(false);
        form.m_fullHalfWidthInsensitive.setSelected(false);

        updateAdvancedOptionStatus();
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
                form.m_resultsLabel.setText(
                        StringUtil.format(OStrings.getString("SW_NR_OF_RESULTS"), viewer.getNrEntries()));
                boolean haveResults = !searcher.getSearchResults().isEmpty();
                form.m_filterButton.setEnabled(haveResults);
                form.m_replaceButton.setEnabled(haveResults);
                form.m_replaceAllButton.setEnabled(haveResults);
                if (!haveResults) {
                    // RFE#1143
                    // https://sourceforge.net/p/omegat/feature-requests/1143/
                    focusSearchField();
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
        if (dir == null) {
            return;
        }
        String str = dir.getAbsolutePath() + File.separator;
        form.m_dirField.setText(str);
    }

    private void doFilter() {
        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndLeave(); // Otherwise, the current segment
                                           // being edited is lost
        Core.getEditor().setFilter(new SearchFilter(viewer.getEntryList()));
    }

    private void doReplace() {
        String replaceString = form.m_replaceField.getEditor().getItem().toString();
        replaceString = StringUtil.normalizeUnicode(replaceString);
        HistoryManager.addReplaceItem(replaceString);
        form.m_replaceField.setModel(new DefaultComboBoxModel<>(HistoryManager.getReplaceItems()));

        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndLeave(); // Otherwise, the current segment
                                           // being edited is lost
        Core.getEditor().setFilter(new ReplaceFilter(viewer.getEntryList(), viewer.getSearcher()));
    }

    private void doReplaceAll() {
        String replaceString = form.m_replaceField.getEditor().getItem().toString();
        replaceString = StringUtil.normalizeUnicode(replaceString);
        HistoryManager.addReplaceItem(replaceString);
        form.m_replaceField.setModel(new DefaultComboBoxModel<>(HistoryManager.getReplaceItems()));

        EntryListPane viewer = (EntryListPane) form.m_viewer;
        Core.getEditor().commitAndDeactivate(); // Otherwise, the current
                                                // segment being edited is lost
        int count = viewer.getEntryList().size();
        String msg = MessageFormat.format(OStrings.getString("SW_REPLACE_ALL_CONFIRM"), count);
        int r = JOptionPane.showConfirmDialog(form, msg, OStrings.getString("CONFIRM_DIALOG_TITLE"),
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            new ReplaceFilter(viewer.getEntryList(), viewer.getSearcher()).replaceAll();
        }
        Core.getEditor().activateEntry();
        form.m_replaceButton.setEnabled(false);
        form.m_replaceAllButton.setEnabled(false);
    }

    private void doSearch() {
        UIThreadsUtil.mustBeSwingThread();
        if (thread != null) {
            // stop old search thread
            thread.fin();
        }

        EntryListPane viewer = (EntryListPane) form.m_viewer;

        String queryString = form.m_searchField.getEditor().getItem().toString();
        queryString = StringUtil.normalizeUnicode(queryString);

        HistoryManager.addSearchItem(queryString);
        form.m_searchField.setModel(new DefaultComboBoxModel<>(HistoryManager.getSearchItems()));
        form.m_searchField.requestFocus();

        viewer.reset();
        String root = null;
        if (form.m_rbDir.isSelected()) {
            // make sure it's a valid directory name
            root = form.m_dirField.getText();
            if (!root.endsWith(File.separator)) {
                root += File.separator;
            }
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
            s.widthInsensitive = form.m_fullHalfWidthInsensitive.isSelected();
            s.excludeOrphans = form.m_excludeOrphans.isSelected();
            s.replacement = null;
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
            s.widthInsensitive = form.m_fullHalfWidthInsensitive.isSelected();
            s.excludeOrphans = true;
            s.replacement = form.m_replaceField.getEditor().getItem().toString();
            break;
        }

        s.searchNotes = form.m_searchNotesCB.isSelected();
        s.searchComments = form.m_searchCommentsCB.isSelected();
        s.searchAuthor = form.m_authorCB.isSelected();
        s.author = form.m_authorField.getText();
        s.searchDateAfter = form.m_dateFromCB.isSelected();
        s.dateAfter = dateFromModel.getDate().getTime();
        s.searchDateBefore = form.m_dateToCB.isSelected();
        s.dateBefore = dateToModel.getDate().getTime();
        s.numberOfResults = mode == SearchMode.SEARCH ? ((Integer) form.m_numberOfResults.getValue())
                : Integer.MAX_VALUE;

        Searcher searcher = new Searcher(Core.getProject(), s);
        // start the search in a separate thread
        thread = new SearchThread(this, searcher);
        thread.start();
    }

    void doCancel() {
        UIThreadsUtil.mustBeSwingThread();
        if (thread != null) {
            thread.fin();
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
            setSearchText(query);
        }
        form.setVisible(true);
        form.setState(JFrame.NORMAL);
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
        dateFromModel.setEnd(now);
        dateFromModel.setValue(now);
    }

    private void doResetDateTo() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        dateToModel.setEnd(now);
        dateToModel.setValue(now);
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

    /**
     * Get the currently selected text in the search results pane
     *
     * @return Selected text, or {@code null} if none
     */
    public String getViewerSelection() {
        return form.m_viewer.getSelectedText();
    }

    /**
     * Set the content of the search query
     *
     * @param text
     *            The query text
     */
    public void setSearchText(String text) {
        JComboBox<String> field = form.m_searchField;
        JTextField editor = (JTextField) field.getEditor().getEditorComponent();
        editor.setText(text);
    }

    /**
     * Focus the search field and select the current query text
     */
    public void focusSearchField() {
        JComboBox<String> field = form.m_searchField;
        field.requestFocus();
        field.getEditor().selectAll();
    }

    private JComboBox<String> getActiveField() {
        return form.m_replaceField.hasFocus() ? form.m_replaceField : form.m_searchField;
    }

    private JTextField getActiveFieldEditor() {
        return (JTextField) getActiveField().getEditor().getEditorComponent();
    }

    /**
     * Insert the specified text into the currently active (focused) search
     * field: either Search or Replace
     *
     * @param text
     *            The text to insert
     */
    public void insertIntoActiveField(String text) {
        JTextField editor = getActiveFieldEditor();
        int offset = editor.getCaretPosition();
        try {
            editor.getDocument().insertString(offset, text, null);
        } catch (BadLocationException ignore) {
        }
    }

    /**
     * Replace the text of the currently active (focused) search field: either
     * Search or Replace
     *
     * @param text
     *            The text to set
     */
    public void replaceCurrentFieldText(String text) {
        getActiveFieldEditor().setText(text);
    }

    /**
     * Get the search window frame
     *
     * @return search window frame
     */
    public JFrame getWindow() {
        return form;
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
            if (!StringUtil.isEmpty(dateFromValue)) {
                dateFromModel.setValue(dateFormat.parse(dateFromValue));
            }
            // to date
            form.m_dateToCB.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_DATE_TO));
            String dateToValue = Preferences.getPreference(Preferences.SEARCHWINDOW_DATE_TO_VALUE);
            if (!StringUtil.isEmpty(dateToValue)) {
                dateToModel.setValue(dateFormat.parse(dateToValue));
            }
        } catch (ParseException e) {
            // use safe settings in case of parsing error
            form.m_dateFromCB.setSelected(false);
            form.m_dateToCB.setSelected(false);
        }

        // Number of results
        form.m_numberOfResults.setValue(Preferences.getPreferenceDefault(
                Preferences.SEARCHWINDOW_NUMBER_OF_RESULTS, OConsts.ST_MAX_SEARCH_RESULTS));

        form.m_excludeOrphans.setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_EXCLUDE_ORPHANS));
        form.m_fullHalfWidthInsensitive
                .setSelected(Preferences.isPreference(Preferences.SEARCHWINDOW_FULLHALFWIDTH_INSENSITIVE));

        // if advanced options are enabled (e.g. author/date search),
        // let the user see them anyway. This is important because
        // search results will be affected by these settings
        if (form.m_authorCB.isSelected() || form.m_dateFromCB.isSelected() || form.m_dateToCB.isSelected()
                || form.m_excludeOrphans.isSelected() || form.m_fullHalfWidthInsensitive.isSelected()) {
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

    public void addWindowListener(WindowListener listener) {
        form.addWindowListener(listener);
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
        UIThreadsUtil.executeInSwingThread(() -> {
            String msg;
            if (params != null) {
                msg = StringUtil.format(OStrings.getString(errorKey), params);
            } else {
                msg = OStrings.getString(errorKey);
            }

            String fulltext = msg;
            if (ex != null) {
                fulltext += "\n" + ex.getLocalizedMessage();
            }
            JOptionPane.showMessageDialog(form, fulltext, OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private SimpleDateFormat dateFormat;
    private SpinnerDateModel dateFromModel, dateToModel;

    private SearchThread thread;

    private static final String SAVED_DATE_FORMAT = "yyyy/MM/dd HH:mm";

}
