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
import org.omegat.gui.search.SearchWindowManager;
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
 */
public final class MainWindowMenuHandler extends BaseMainWindowMenuHandler {

    private final MainWindow mainWindow;

    public MainWindowMenuHandler(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Create new project.
     */
    public void projectNewMenuItemActionPerformed() {
        ProjectUICommands.projectCreate();
    }

    /**
     * Create new team project.
     */
    public void projectTeamNewMenuItemActionPerformed() {
        ProjectUICommands.projectTeamCreate();
    }

    /**
     * Open project.
     */
    public void projectOpenMenuItemActionPerformed() {
        ProjectUICommands.projectOpen(null);
    }

    public void projectClearRecentMenuItemActionPerformed() {
        RecentProjects.clear();
    }

    /**
     * Open MED project.
     */
    public void projectMedOpenMenuItemActionPerformed() {
        ProjectUICommands.projectOpenMED();
    }

    /**
     * Create MED project.
     */
    public void projectMedCreateMenuItemActionPerformed() {
        ProjectUICommands.projectCreateMED();
    }

    /**
     * Imports the file/files/folder into project's source files.
     */
    public void projectImportMenuItemActionPerformed() {
        ProjectUICommands.doPromptImportSourceFiles();
    }

    public void projectWikiImportMenuItemActionPerformed() {
        ProjectUICommands.doWikiImport();
    }

    public void projectReloadMenuItemActionPerformed() {
        ProjectUICommands.projectReload();
    }

    /**
     * Close project.
     */
    public void projectCloseMenuItemActionPerformed() {
        ProjectUICommands.projectClose();
    }

    /**
     * Save project.
     */
    public void projectSaveMenuItemActionPerformed() {
        ProjectUICommands.projectSave();
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed() {
        if (ProjectUICommands.areTagsValid()) {
            ProjectUICommands.projectCompile();
        }
    }

    public void projectCommitTargetFilesActionPerformed() {
        if (ProjectUICommands.areTagsValid()) {
            ProjectUICommands.projectCompileAndCommit();
        }
    }

    /**
     * Commit source files
     */
    public void projectCommitSourceFilesActionPerformed() {
        ProjectUICommands.projectCommitSourceFiles();
    }

    /**
     * Create current translated document.
     */
    public void projectSingleCompileMenuItemActionPerformed() {
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
    public void projectEditMenuItemActionPerformed() {
        ProjectUICommands.projectEditProperties();
    }

    public void viewFileListMenuItemActionPerformed() {
        IProjectFilesList projWin = Core.getProjectFilesList();
        if (projWin == null) {
            mainWindow.menu.viewFileListMenuItem.setSelected(false);
            return;
        }

        projWin.setActive(!projWin.isActive());
    }

    public void projectAccessRootMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getProjectRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessDictionaryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getDictRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessGlossaryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getGlossaryRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getSourceRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessTargetMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getTargetRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessTMMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getTMRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessExportTMMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String path = Core.getProject().getProjectProperties().getExportTMRoot();
        ProjectUICommands.openFile(new File(path));
    }

    public void projectAccessCurrentSourceDocumentMenuItemActionPerformed(int modifier) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String root = Core.getProject().getProjectProperties().getSourceRoot();
        String path = Core.getEditor().getCurrentFile();
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File toOpen = new File(root, path);
        if ((modifier & ActionEvent.ALT_MASK) != 0) {
            toOpen = toOpen.getParentFile();
        }
        ProjectUICommands.openFile(toOpen);
    }

    public void projectAccessCurrentTargetDocumentMenuItemActionPerformed(int modifier) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String root = Core.getProject().getProjectProperties().getTargetRoot();
        String path = Core.getEditor().getCurrentTargetFile();
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File toOpen = new File(root, path);
        if ((modifier & ActionEvent.ALT_MASK) != 0) {
            toOpen = toOpen.getParentFile();
        }
        ProjectUICommands.openFile(toOpen);
    }

    public void projectAccessWriteableGlossaryMenuItemActionPerformed(int modifier) {
        ProjectUICommands.openWriteableGlossaryFile((modifier & ActionEvent.ALT_MASK) != 0);
    }

    /** Quits OmegaT */
    public void projectExitMenuItemActionPerformed() {
        ProjectUICommands.projectExit();
    }

    /** Restart OmegaT */
    public void projectRestartMenuItemActionPerformed() {
        String projectDir = Core.getProject().isProjectLoaded()
                ? Core.getProject().getProjectProperties().getProjectRoot()
                : null;
        ProjectUICommands.projectRestart(projectDir);
    }

    public void editUndoMenuItemActionPerformed() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().undo();
        } else {
            Core.getEditor().undo();
        }
    }

    public void editRedoMenuItemActionPerformed() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().redo();
        } else {
            Core.getEditor().redo();
        }
    }

    public void editOverwriteTranslationMenuItemActionPerformed() {
        MainWindow.doRecycleTrans();
    }

    public void editInsertTranslationMenuItemActionPerformed() {
        MainWindow.doInsertTrans();
    }

    public void editOverwriteMachineTranslationMenuItemActionPerformed() {
        MachineTranslationInfo tr = Core.getMachineTranslatePane().getDisplayedTranslation();
        if (tr == null) {
            Core.getMachineTranslatePane().forceLoad();
        } else if (!StringUtil.isEmpty(tr.result)) {
            Core.getEditor().replaceEditText(tr.result, String.format("MT:[%s]", tr.translatorName));
        }
    }

    /**
     * replaces entire edited segment text with a the source text of a segment at cursor position
     */
    public void editOverwriteSourceMenuItemActionPerformed() {
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
    public void editInsertSourceMenuItemActionPerformed() {
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
    public void editSelectSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getEditor().selectSourceText();
    }

    public void editExportSelectionMenuItemActionPerformed() {
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

    public void editSearchDictionaryMenuItemActionPerformed() {
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

    public void editCreateGlossaryEntryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getGlossary().showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
    }

    /**
     * Used in BaseMainWindowMenu#configuraActions for macOS.
     */
    void findInProjectReuseLastWindow() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String text = Core.getMainWindow().getSelectedText();
        if (text == null) {
            return;
        }
        if (!SearchWindowManager.reuseSearchWindow(text)) {
            SearchWindowManager.createSearchWindow(SearchMode.SEARCH, text);
        }
    }

    public void editFindInProjectMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        SearchWindowManager.createSearchWindow(SearchMode.SEARCH);
    }

    public void editReplaceInProjectMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        SearchWindowManager.createSearchWindow(SearchMode.REPLACE);
    }

    /** Set active match to #1. */
    public void editSelectFuzzy1MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(0);
    }

    /** Set active match to #2. */
    public void editSelectFuzzy2MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(1);
    }

    /** Set active match to #3. */
    public void editSelectFuzzy3MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(2);
    }

    /** Set active match to #4. */
    public void editSelectFuzzy4MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(3);
    }

    /** Set active match to #5. */
    public void editSelectFuzzy5MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(4);
    }

    /** Set active match to the next one */
    public void editSelectFuzzyNextMenuItemActionPerformed() {
        Core.getMatcher().setNextActiveMatch();
    }

    /** Set active match to the previous one */
    public void editSelectFuzzyPrevMenuItemActionPerformed() {
        Core.getMatcher().setPrevActiveMatch();
    }

    public void insertCharsLRMActionPerformed() {
        Core.getEditor().insertText("\u200E");
    }

    public void insertCharsRLMActionPerformed() {
        Core.getEditor().insertText("\u200F");
    }

    public void insertCharsLREActionPerformed() {
        Core.getEditor().insertText("\u202A");
    }

    public void insertCharsRLEActionPerformed() {
        Core.getEditor().insertText("\u202B");
    }

    public void insertCharsPDFActionPerformed() {
        Core.getEditor().insertText("\u202C");
    }

    public void editMultipleDefaultActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(false);
    }

    public void editMultipleAlternateActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(true);
    }

    public void editRegisterUntranslatedMenuItemActionPerformed() {
        Core.getEditor().registerUntranslated();
    }

    public void editRegisterEmptyMenuItemActionPerformed() {
        Core.getEditor().registerEmptyTranslation();
    }

    public void editRegisterIdenticalMenuItemActionPerformed() {
        Core.getEditor().registerIdenticalTranslation();
    }

    public void optionsPreferencesMenuItemActionPerformed() {
        PreferencesWindowController pwc = new PreferencesWindowController();
        pwc.show(Core.getMainWindow().getApplicationFrame());
    }

    public void cycleSwitchCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.CYCLE);
    }

    public void sentenceCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.SENTENCE);
    }

    public void titleCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.TITLE);
    }

    public void upperCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.UPPER);
    }

    public void lowerCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.LOWER);
    }

    public void gotoNextUntranslatedMenuItemActionPerformed() {
        Core.getEditor().nextUntranslatedEntry();
    }

    public void gotoNextUniqueMenuItemActionPerformed() {
        Core.getEditor().nextUniqueEntry();
    }

    public void gotoNextTranslatedMenuItemActionPerformed() {
        Core.getEditor().nextTranslatedEntry();
    }

    public void gotoNextSegmentMenuItemActionPerformed() {
        Core.getEditor().nextEntry();
    }

    public void gotoPreviousSegmentMenuItemActionPerformed() {
        Core.getEditor().prevEntry();
    }

    public void gotoNextXAutoMenuItemActionPerformed() {
        Core.getEditor().nextXAutoEntry();
    }

    public void gotoPrevXAutoMenuItemActionPerformed() {
        Core.getEditor().prevXAutoEntry();
    }

    public void gotoNextXEnforcedMenuItemActionPerformed() {
        Core.getEditor().nextXEnforcedEntry();
    }

    public void gotoPrevXEnforcedMenuItemActionPerformed() {
        Core.getEditor().prevXEnforcedEntry();
    }

    public void gotoNextNoteMenuItemActionPerformed() {
        Core.getEditor().nextEntryWithNote();
    }

    public void gotoPreviousNoteMenuItemActionPerformed() {
        Core.getEditor().prevEntryWithNote();
    }

    public void gotoNotesPanelMenuItemActionPerformed() {
        Core.getNotes().requestFocus();
    }

    public void gotoEditorPanelMenuItemActionPerformed() {
        Core.getEditor().requestFocus();
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     */
    public void gotoSegmentMenuItemActionPerformed() {
        // Create a dialog for input
        GoToSegmentDialog dialog = new GoToSegmentDialog(Core.getMainWindow().getApplicationFrame());
        dialog.setVisible(true);

        int jumpTo = dialog.getResult();

        if (jumpTo != -1) {
            Core.getEditor().gotoEntry(jumpTo);
        }
    }

    public void gotoHistoryBackMenuItemActionPerformed() {
        Core.getEditor().gotoHistoryBack();
    }

    public void gotoHistoryForwardMenuItemActionPerformed() {
        Core.getEditor().gotoHistoryForward();
    }

    public void gotoMatchSourceSegmentActionPerformed() {
        NearString ns = Core.getMatcher().getActiveMatch();
        if (ns != null && ns.comesFrom == MATCH_SOURCE.MEMORY) {
            Core.getEditor().gotoEntry(ns.source, ns.key);
        }
    }

    public void viewMarkTranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkTranslated(mainWindow.menu.viewMarkTranslatedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkUntranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkUntranslated(
                mainWindow.menu.viewMarkUntranslatedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkParagraphStartCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkParagraphDelimitations(
                mainWindow.menu.viewMarkParagraphStartCheckBoxMenuItem.isSelected());
    }

    public void viewDisplaySegmentSourceCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setDisplaySegmentSources(
                mainWindow.menu.viewDisplaySegmentSourceCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNonUniqueSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkNonUniqueSegments(
                mainWindow.menu.viewMarkNonUniqueSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNotedSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkNotedSegments(mainWindow.menu.viewMarkNotedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNBSPCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkNBSP(mainWindow.menu.viewMarkNBSPCheckBoxMenuItem.isSelected());
    }

    public void viewMarkWhitespaceCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkWhitespace(mainWindow.menu.viewMarkWhitespaceCheckBoxMenuItem.isSelected());
    }

    public void viewMarkBidiCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkBidi(mainWindow.menu.viewMarkBidiCheckBoxMenuItem.isSelected());
    }

    public void viewMarkAlternativeTranslationsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings().setMarkAltTranslations(
                mainWindow.menu.viewMarkAlternativeTranslationsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkAutoPopulatedCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkAutoPopulated(mainWindow.menu.viewMarkAutoPopulatedCheckBoxMenuItem.isSelected());
    }

    public void viewMarkGlossaryMatchesCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkGlossaryMatches(mainWindow.menu.viewMarkGlossaryMatchesCheckBoxMenuItem.isSelected());
    }

    public void viewMarkLanguageCheckerCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkLanguageChecker(mainWindow.menu.viewMarkLanguageCheckerCheckBoxMenuItem.isSelected());
    }

    public void viewMarkFontFallbackCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDoFontFallback(mainWindow.menu.viewMarkFontFallbackCheckBoxMenuItem.isSelected());
    }

    public void viewDisplayModificationInfoNoneRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE);
    }

    public void viewDisplayModificationInfoSelectedRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED);
    }

    public void viewDisplayModificationInfoAllRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL);
    }

    public void toolsCheckIssuesMenuItemActionPerformed() {
        if (!Preferences.isPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK)) {
            IssueProvidersSelectorController dialog = new IssueProvidersSelectorController();
            if (!dialog.show(Core.getMainWindow().getApplicationFrame())) {
                return;
            }
        }
        Core.getIssues().showAll();
    }

    public void toolsCheckIssuesCurrentFileMenuItemActionPerformed() {
        Core.getIssues().showForFiles(Pattern.quote(Core.getEditor().getCurrentFile()));
    }

    /**
     * Identify all the placeholders in the source text and automatically
     * inserts them into the target text.
     */
    public void editTagPainterMenuItemActionPerformed() {
        // insert tags
        for (Tag tag : TagUtil.getAllTagsMissingFromTarget()) {
            Core.getEditor().insertTag(tag.tag);
        }
    }

    public void editTagNextMissedMenuItemActionPerformed() {
        // insert next tag
        List<Tag> tags = TagUtil.getAllTagsMissingFromTarget();
        if (tags.isEmpty()) {
            return;
        }
        Core.getEditor().insertTag(tags.get(0).tag);
    }

    public void toolsShowStatisticsStandardMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.STANDARD)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.MATCHES)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesPerFileMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                StatisticsWindow.STAT_TYPE.MATCHES_PER_FILE).setVisible(true);
    }

    public void optionsAutoCompleteShowAutomaticallyItemActionPerformed() {
        Preferences.setPreference(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY,
                mainWindow.menu.optionsAutoCompleteShowAutomaticallyItem.isSelected());
    }

    public void optionsAutoCompleteHistoryCompletionMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.AC_HISTORY_COMPLETION_ENABLED,
                mainWindow.menu.optionsAutoCompleteHistoryCompletionMenuItem.isSelected());
    }

    public void optionsAutoCompleteHistoryPredictionMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED,
                mainWindow.menu.optionsAutoCompleteHistoryPredictionMenuItem.isSelected());
    }

    public void optionsMTAutoFetchCheckboxMenuItemActionPerformed() {
        boolean enabled = mainWindow.menu.optionsMTAutoFetchCheckboxMenuItem.isSelected();
        Preferences.setPreference(Preferences.MT_AUTO_FETCH, enabled);
    }

    public void optionsGlossaryFuzzyMatchingCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.GLOSSARY_STEMMING,
                mainWindow.menu.optionsGlossaryFuzzyMatchingCheckBoxMenuItem.isSelected());
        Preferences.save();
    }

    public void optionsDictionaryFuzzyMatchingCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.DICTIONARY_FUZZY_MATCHING,
                mainWindow.menu.optionsDictionaryFuzzyMatchingCheckBoxMenuItem.isSelected());
        Preferences.save();
    }

    /**
     * Displays the filters setup dialog to allow customizing file filters in
     * detail.
     */
    public void optionsSetupFileFiltersMenuItemActionPerformed() {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                FiltersCustomizerController.class);
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the
     * segmentation rules in detail.
     */
    public void optionsSentsegMenuItemActionPerformed() {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                SegmentationCustomizerController.class);

    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse
     * workflow options.
     */
    public void optionsWorkflowMenuItemActionPerformed() {
        new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                EditingBehaviorController.class);
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     */
    public void viewRestoreGUIMenuItemActionPerformed() {
        Core.getMainWindow().resetDesktopLayout();
    }

    public void optionsAccessConfigDirMenuItemActionPerformed() {
        ProjectUICommands.openFile(new File(StaticUtils.getConfigDir()));
    }

    /**
     * Show help.
     */
    public void helpContentsMenuItemActionPerformed() {
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
    public void helpAboutMenuItemActionPerformed() {
        new AboutDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Shows Last changes
     */
    public void helpLastChangesMenuItemActionPerformed() {
        new LastChangesDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Show log
     */
    public void helpLogMenuItemActionPerformed() {
        new LogDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
    }

    /**
     * Check for updates
     */
    public void helpUpdateCheckMenuItemActionPerformed() {
        VersionCheckDialog.checkAndShowResultAsync(Core.getMainWindow().getApplicationFrame());
    }
}
