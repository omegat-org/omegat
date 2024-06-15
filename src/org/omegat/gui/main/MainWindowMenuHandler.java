/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009 Didier Briel, Alex Buloichik
               2010 Wildrich Fourie, Didier Briel
               2012 Wildrich Fourie, Guido Leenders, Didier Briel
               2013 Zoltan Bartko, Didier Briel, Yu Tang
               2014 Aaron Madlon-Kay
               2015 Yu Tang, Aaron Madlon-Kay, Didier Briel
               2017 Didier Briel
               2019 Thomas Cordonnier
               2024 Hiroshi Miura
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

package org.omegat.gui.main;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.NearString.MATCH_SOURCE;
import org.omegat.core.search.SearchMode;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.GoToSegmentDialog;
import org.omegat.gui.dialogs.LastChangesDialog;
import org.omegat.gui.dialogs.LogDialog;
import org.omegat.gui.dialogs.VersionCheckDialog;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.gui.editor.EditorUtils;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.SegmentExportImport;
import org.omegat.gui.exttrans.MachineTranslationInfo;
import org.omegat.gui.filelist.IProjectFilesList;
import org.omegat.gui.filters2.FiltersCustomizerController;
import org.omegat.gui.issues.IssueProvidersSelectorController;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.EditingBehaviorController;
import org.omegat.gui.segmentation.SegmentationCustomizerController;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.help.Help;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RecentProjects;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.gui.DesktopWrapper;

/**
 * Handler for main menu items.
 *
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Wildrich Fourie
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public final class MainWindowMenuHandler {

    public MainWindowMenuHandler() {
    }

    public MainWindowMenuHandler(final MainWindow mainWindow) {
    }

    /**
     * Create a new project.
     */
    public void projectNewMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectCreate();
    }

    /**
     * Create new team project.
     */
    public void projectTeamNewMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectTeamCreate();
    }

    /**
     * Open project.
     */
    public void projectOpenMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectOpen(null);
    }

    public void projectClearRecentMenuItemActionPerformed(ActionEvent evt) {
        RecentProjects.clear();
    }

    /**
     * Open MED project.
     */
    public void projectMedOpenMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectOpenMED();
    }

    /**
     * Create MED project.
     */
    public void projectMedCreateMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectCreateMED();
    }

    /**
     * Imports the file/files/folder into project's source files.
     */
    public void projectImportMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.doPromptImportSourceFiles();
    }

    public void projectWikiImportMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.doWikiImport();
    }

    public void projectReloadMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectReload();
    }

    /**
     * Close project.
     */
    public void projectCloseMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectClose();
    }

    /**
     * Save project.
     */
    public void projectSaveMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectSave();
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed(ActionEvent evt) {
        if (!checkTags()) {
            return;
        }

        ProjectUICommands.projectCompile();
    }

    /**
     * Check whether tags are OK
     * 
     * @return false is there is a tag issue, true otherwise
     */
    private boolean checkTags() {
        if (Preferences.isPreference(Preferences.TAGS_VALID_REQUIRED)) {
            List<ErrorReport> stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getIssues().showAll(OStrings.getString("TF_MESSAGE_COMPILE"));
                return false;
            }
        }
        return true;
    }

    public void projectCommitTargetFilesActionPerformed(ActionEvent evt) {
        if (!checkTags()) {
            return;
        }

        ProjectUICommands.projectCompileAndCommit();
    }

    /**
     * Commit source files
     */
    public void projectCommitSourceFilesActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectCommitSourceFiles();
    }

    /**
     * Create current translated document.
     */
    public void projectSingleCompileMenuItemActionPerformed(ActionEvent evt) {
        String midName = Core.getEditor().getCurrentFile();
        if (StringUtil.isEmpty(midName)) {
            return;
        }

        String sourcePattern = Pattern.quote(midName);
        if (Preferences.isPreference(Preferences.TAGS_VALID_REQUIRED)) {
            List<ErrorReport> stes = Core.getTagValidation().listInvalidTags(sourcePattern);
            if (!stes.isEmpty()) {
                Core.getIssues().showForFiles(midName, OStrings.getString("TF_MESSAGE_COMPILE"));
                return;
            }
        }

        ProjectUICommands.projectSingleCompile(sourcePattern);
    }

    /** Edits project's properties */
    public void projectEditMenuItemActionPerformed(ActionEvent evt) {
        ProjectUICommands.projectEditProperties();
    }

    public void viewFileListMenuItemActionPerformed(ActionEvent evt) {
        IProjectFilesList projWin = Core.getProjectFilesList();
        if (projWin == null) {
            Object o = evt.getSource();
            if (o instanceof JMenuItem) {
                ((JMenuItem) o).setSelected(false);
            }
            return;
        }
        projWin.setActive(!projWin.isActive());
    }

    public void projectAccessRootMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getProjectRoot();
        openFile(new File(path));
    }

    public void projectAccessDictionaryMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getDictRoot();
        openFile(new File(path));
    }

    public void projectAccessGlossaryMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getGlossaryRoot();
        openFile(new File(path));
    }

    public void projectAccessSourceMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getSourceRoot();
        openFile(new File(path));
    }

    public void projectAccessTargetMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getTargetRoot();
        openFile(new File(path));
    }

    public void projectAccessTMMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getTMRoot();
        openFile(new File(path));
    }

    public void projectAccessExportTMMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getExportTMRoot();
        openFile(new File(path));
    }

    public void projectAccessCurrentSourceDocumentMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String root = Core.getProject().getProjectProperties().getSourceRoot();
        String path = Core.getEditor().getCurrentFile();
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File toOpen = new File(root, path);
        if ((evt.getModifiers() & ActionEvent.ALT_MASK) != 0) {
            toOpen = toOpen.getParentFile();
        }
        openFile(toOpen);
    }

    public void projectAccessCurrentTargetDocumentMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String root = Core.getProject().getProjectProperties().getTargetRoot();
        String path = Core.getEditor().getCurrentTargetFile();
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File toOpen = new File(root, path);
        if ((evt.getModifiers() & ActionEvent.ALT_MASK) != 0) {
            toOpen = toOpen.getParentFile();
        }
        openFile(toOpen);
    }

    public void projectAccessWriteableGlossaryMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getWriteableGlossary();
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File toOpen = new File(path);
        if ((evt.getModifiers() & ActionEvent.ALT_MASK) != 0) {
            toOpen = toOpen.getParentFile();
        }
        openFile(toOpen);
    }

    private void openFile(File path) {
        try {
            path = path.getCanonicalFile(); // Normalize file name in case it is
                                            // displayed
        } catch (Exception ex) {
            // Ignore
        }
        if (!path.exists()) {
            Core.getMainWindow().showStatusMessageRB("LFC_ERROR_FILE_DOESNT_EXIST", path);
            return;
        }
        try {
            DesktopWrapper.open(path);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "RPF_ERROR");
            Core.getMainWindow().displayErrorRB(ex, "RPF_ERROR");
        }
    }

    /** Quits OmegaT */
    public void projectExitMenuItemActionPerformed(ActionEvent evt) {
        MainWindowUI.projectExit();
    }

    /** Restart OmegaT */
    public void projectRestartMenuItemActionPerformed(ActionEvent evt) {
        String projectDir = Core.getProject().isProjectLoaded()
                ? Core.getProject().getProjectProperties().getProjectRoot()
                : null;
        MainWindowUI.projectRestart(projectDir);
    }

    public void editUndoMenuItemActionPerformed(ActionEvent evt) {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().undo();
        } else {
            Core.getEditor().undo();
        }
    }

    public void editRedoMenuItemActionPerformed(ActionEvent evt) {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().redo();
        } else {
            Core.getEditor().redo();
        }
    }

    public void editOverwriteTranslationMenuItemActionPerformed(ActionEvent evt) {
        MainWindow.doRecycleTrans();
    }

    public void editInsertTranslationMenuItemActionPerformed(ActionEvent evt) {
        MainWindow.doInsertTrans();
    }

    public void editOverwriteMachineTranslationMenuItemActionPerformed(ActionEvent evt) {
        MachineTranslationInfo tr = Core.getMachineTranslatePane().getDisplayedTranslation();
        if (tr == null) {
            Core.getMachineTranslatePane().forceLoad();
        } else if (!StringUtil.isEmpty(tr.result)) {
            Core.getEditor().replaceEditText(tr.result, String.format("MT:[%s]", tr.translatorName));
        }
    }

    /**
     * replaces entire edited segment text with a the source text of a segment
     * at cursor position
     */
    public void editOverwriteSourceMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
        if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
            toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
        }
        Core.getEditor().replaceEditText(toInsert);
    }

    /** inserts the source text of a segment at cursor position */
    public void editInsertSourceMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
        if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
            toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
        }
        Core.getEditor().insertText(toInsert);
    }

    /** select the source text of the current segment */
    public void editSelectSourceMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getEditor().selectSourceText();
    }

    public void editExportSelectionMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String selection = Core.getEditor().getSelectedText();
        if (selection == null) {
            SourceTextEntry ste = Core.getEditor().getCurrentEntry();
            TMXEntry te = Core.getProject().getTranslationInfo(ste);
            if (te.isTranslated()) {
                selection = te.translation;
            } else {
                selection = ste.getSrcText();
            }
        }
        SegmentExportImport.exportCurrentSelection(selection);
    }

    public void editSearchDictionaryMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String selection = Core.getEditor().getSelectedText();
        if (selection == null) {
            SourceTextEntry ste = Core.getEditor().getCurrentEntry();
            selection = ste.getSrcText();
        }
        Core.getDictionaries().searchText(selection);
    }

    public void editCreateGlossaryEntryMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getGlossary().showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
    }

    public void editFindInProjectMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        MainWindowUI.createSearchWindow(SearchMode.SEARCH);
    }

    public void editReplaceInProjectMenuItemActionPerformed(ActionEvent evt) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        MainWindowUI.createSearchWindow(SearchMode.REPLACE);
    }

    /** Set active match to #1. */
    public void editSelectFuzzy1MenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setActiveMatch(0);
    }

    /** Set active match to #2. */
    public void editSelectFuzzy2MenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setActiveMatch(1);
    }

    /** Set active match to #3. */
    public void editSelectFuzzy3MenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setActiveMatch(2);
    }

    /** Set active match to #4. */
    public void editSelectFuzzy4MenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setActiveMatch(3);
    }

    /** Set active match to #5. */
    public void editSelectFuzzy5MenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setActiveMatch(4);
    }

    /** Set active match to the next one */
    public void editSelectFuzzyNextMenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setNextActiveMatch();
    }

    /** Set active match to the previous one */
    public void editSelectFuzzyPrevMenuItemActionPerformed(ActionEvent evt) {
        Core.getMatcher().setPrevActiveMatch();
    }

    public void insertCharsLRMActionPerformed(ActionEvent evt) {
        Core.getEditor().insertText("\u200E");
    }

    public void insertCharsRLMActionPerformed(ActionEvent evt) {
        Core.getEditor().insertText("\u200F");
    }

    public void insertCharsLREActionPerformed(ActionEvent evt) {
        Core.getEditor().insertText("\u202A");
    }

    public void insertCharsRLEActionPerformed(ActionEvent evt) {
        Core.getEditor().insertText("\u202B");
    }

    public void insertCharsPDFActionPerformed(ActionEvent evt) {
        Core.getEditor().insertText("\u202C");
    }

    public void editMultipleDefaultActionPerformed(ActionEvent evt) {
        Core.getEditor().setAlternateTranslationForCurrentEntry(false);
    }

    public void editMultipleAlternateActionPerformed(ActionEvent evt) {
        Core.getEditor().setAlternateTranslationForCurrentEntry(true);
    }

    public void editRegisterUntranslatedMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().registerUntranslated();
    }

    public void editRegisterEmptyMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().registerEmptyTranslation();
    }

    public void editRegisterIdenticalMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().registerIdenticalTranslation();
    }

    public void optionsPreferencesMenuItemActionPerformed(ActionEvent evt) {
        PreferencesWindowController pwc = new PreferencesWindowController();
        pwc.show(Core.getMainWindow().getApplicationFrame());
    }

    public void cycleSwitchCaseMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.CYCLE);
    }

    public void sentenceCaseMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.SENTENCE);
    }

    public void titleCaseMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.TITLE);
    }

    public void upperCaseMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.UPPER);
    }

    public void lowerCaseMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.LOWER);
    }

    public void gotoNextUntranslatedMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextUntranslatedEntry();
    }

    public void gotoNextUniqueMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextUniqueEntry();
    }

    public void gotoNextTranslatedMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextTranslatedEntry();
    }

    public void gotoNextSegmentMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextEntry();
    }

    public void gotoPreviousSegmentMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().prevEntry();
    }

    public void gotoNextXAutoMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextXAutoEntry();
    }

    public void gotoPrevXAutoMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().prevXAutoEntry();
    }

    public void gotoNextXEnforcedMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextXEnforcedEntry();
    }

    public void gotoPrevXEnforcedMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().prevXEnforcedEntry();
    }

    public void gotoNextNoteMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().nextEntryWithNote();
    }

    public void gotoPreviousNoteMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().prevEntryWithNote();
    }

    public void gotoNotesPanelMenuItemActionPerformed(ActionEvent evt) {
        Core.getNotes().requestFocus();
    }

    public void gotoEditorPanelMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().requestFocus();
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     */
    public void gotoSegmentMenuItemActionPerformed(ActionEvent evt) {
        // Create a dialog for input
        GoToSegmentDialog dialog = new GoToSegmentDialog(Core.getMainWindow().getApplicationFrame());
        dialog.setVisible(true);

        int jumpTo = dialog.getResult();

        if (jumpTo != -1) {
            Core.getEditor().gotoEntry(jumpTo);
        }
    }

    public void gotoHistoryBackMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().gotoHistoryBack();
    }

    public void gotoHistoryForwardMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().gotoHistoryForward();
    }

    public void gotoMatchSourceSegmentActionPerformed(ActionEvent evt) {
        NearString ns = Core.getMatcher().getActiveMatch();
        if (ns != null && ns.comesFrom == MATCH_SOURCE.MEMORY) {
            Core.getEditor().gotoEntry(ns.source, ns.key);
        }
    }

    public void viewMarkTranslatedSegmentsCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkTranslated(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkUntranslatedSegmentsCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkUntranslated(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkParagraphStartCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings()
                    .setMarkParagraphDelimitations(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewDisplaySegmentSourceCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setDisplaySegmentSources(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkNonUniqueSegmentsCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkNonUniqueSegments(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkNotedSegmentsCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkNotedSegments(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkNBSPCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkNBSP(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkWhitespaceCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkWhitespace(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkBidiCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkBidi(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkAutoPopulatedCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkAutoPopulated(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkGlossaryMatchesCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkGlossaryMatches(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkLanguageCheckerCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setMarkLanguageChecker(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewMarkFontFallbackCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Core.getEditor().getSettings().setDoFontFallback(((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void viewDisplayModificationInfoNoneRadioButtonMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE);
    }

    public void viewDisplayModificationInfoSelectedRadioButtonMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED);
    }

    public void viewDisplayModificationInfoAllRadioButtonMenuItemActionPerformed(ActionEvent evt) {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL);
    }

    public void toolsCheckIssuesMenuItemActionPerformed(ActionEvent evt) {
        if (!Preferences.isPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK)) {
            IssueProvidersSelectorController dialog = new IssueProvidersSelectorController();
            if (!dialog.show(Core.getMainWindow().getApplicationFrame())) {
                return;
            }
        }
        Core.getIssues().showAll();
    }

    public void toolsCheckIssuesCurrentFileMenuItemActionPerformed(ActionEvent evt) {
        Core.getIssues().showForFiles(Pattern.quote(Core.getEditor().getCurrentFile()));
    }

    /**
     * Identify all the placeholders in the source text and automatically
     * inserts them into the target text.
     */
    public void editTagPainterMenuItemActionPerformed(ActionEvent evt) {
        // insert tags
        for (Tag tag : TagUtil.getAllTagsMissingFromTarget()) {
            Core.getEditor().insertTag(tag.tag);
        }
    }

    public void editTagNextMissedMenuItemActionPerformed(ActionEvent evt) {
        // insert next tag
        List<Tag> tags = TagUtil.getAllTagsMissingFromTarget();
        if (tags.isEmpty()) {
            return;
        }
        Core.getEditor().insertTag(tags.get(0).tag);
    }

    public void toolsShowStatisticsStandardMenuItemActionPerformed(ActionEvent evt) {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.STANDARD)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesMenuItemActionPerformed(ActionEvent evt) {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.MATCHES)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesPerFileMenuItemActionPerformed(ActionEvent evt) {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                StatisticsWindow.STAT_TYPE.MATCHES_PER_FILE).setVisible(true);
    }

    public void optionsAutoCompleteShowAutomaticallyItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY,
                    ((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void optionsAutoCompleteHistoryCompletionMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.AC_HISTORY_COMPLETION_ENABLED,
                    ((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void optionsAutoCompleteHistoryPredictionMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED,
                    ((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void optionsMTAutoFetchCheckboxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.MT_AUTO_FETCH, ((JCheckBoxMenuItem) o).isSelected());
        }
    }

    public void optionsGlossaryFuzzyMatchingCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.GLOSSARY_STEMMING, ((JCheckBoxMenuItem) o).isSelected());
            Preferences.save();
        }
    }

    public void optionsDictionaryFuzzyMatchingCheckBoxMenuItemActionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.DICTIONARY_FUZZY_MATCHING,
                    ((JCheckBoxMenuItem) o).isSelected());
            Preferences.save();
        }
    }

    /**
     * Displays the filters setup dialog to allow customizing file filters in
     * detail.
     */
    public void optionsSetupFileFiltersMenuItemActionPerformed(ActionEvent evt) {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                FiltersCustomizerController.class);
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the
     * segmentation rules in detail.
     */
    public void optionsSentsegMenuItemActionPerformed(ActionEvent evt) {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                SegmentationCustomizerController.class);

    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse
     * workflow options.
     */
    public void optionsWorkflowMenuItemActionPerformed(ActionEvent evt) {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                EditingBehaviorController.class);
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     */
    public void viewRestoreGUIMenuItemActionPerformed(ActionEvent evt) {
        Core.getMainWindow().resetDesktopLayout();
    }

    public void optionsAccessConfigDirMenuItemActionPerformed(ActionEvent evt) {
        openFile(new File(StaticUtils.getConfigDir()));
    }

    /**
     * Show help.
     */
    public void helpContentsMenuItemActionPerformed(ActionEvent evt) {
        try {
            Help.showHelp();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    ex.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }
    }

    /**
     * Shows About dialog
     */
    public void helpAboutMenuItemActionPerformed(ActionEvent evt) {
        new AboutDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Shows Last changes
     */
    public void helpLastChangesMenuItemActionPerformed(ActionEvent evt) {
        new LastChangesDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Show log
     */
    public void helpLogMenuItemActionPerformed(ActionEvent evt) {
        new LogDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Check for updates
     */
    public void helpUpdateCheckMenuItemActionPerformed(ActionEvent evt) {
        VersionCheckDialog.checkAndShowResultAsync(Core.getMainWindow().getApplicationFrame());
    }
}
