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
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.openide.awt.AbstractMnemonicsAction;

import org.omegat.Main;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.NearString.MATCH_SOURCE;
import org.omegat.core.search.SearchMode;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.filters2.master.PluginUtils;
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
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.Styles;

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
public final class MainWindowMenuHandler {

    MainWindowMenuHandler() {
    }

    /**
     * Create a new project.
     */
    @SuppressWarnings("serial")
    public static class ProjectNewMenuItemAction extends AbstractMnemonicsAction {
        public ProjectNewMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_CREATE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectNewMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectCreate();
        }
    }

    /**
     * Create a new team project.
     */
    @SuppressWarnings("serial")
    public static class ProjectTeamNewMenuItemAction extends AbstractMnemonicsAction {
        public ProjectTeamNewMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_TEAM_CREATE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectTeamNewMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectTeamCreate();
        }
    }

    /**
     * Open project.
     */
    @SuppressWarnings("serial")
    public static class ProjectOpenMenuItemAction extends AbstractMnemonicsAction {
        public ProjectOpenMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_OPEN"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectOpenMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectOpen(null);
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectClearRecentMenuItemAction extends AbstractMnemonicsAction {
        public ProjectClearRecentMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_CLEAR_RECENT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectClearRecentMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            RecentProjects.clear();
        }
    }

    /**
     * Open MED project.
     */
    @SuppressWarnings("serial")
    public static class ProjectMedOpenMenuItemAction extends AbstractMnemonicsAction {
        public ProjectMedOpenMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_MED_OPEN"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectMedOpenMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectOpenMED();
        }
    }

    /**
     * Create MED project.
     */
    @SuppressWarnings("serial")
    public static class ProjectMedCreateMenuItemAction extends AbstractMnemonicsAction {
        public ProjectMedCreateMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_MED_CREATE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectMedCreateMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectCreateMED();
        }
    }

    /**
     * Imports the file/files/folder into project's source files.
     */
    @SuppressWarnings("serial")
    public static class ProjectImportMenuItemAction extends AbstractMnemonicsAction {
        public ProjectImportMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_IMPORT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectImportMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.doPromptImportSourceFiles();
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectWikiImportMenuItemAction extends AbstractMnemonicsAction {
        public ProjectWikiImportMenuItemAction() {
            super(OStrings.getString("TF_MENU_WIKI_IMPORT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectWikiImportMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.doWikiImport();
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectReloadMenuItemAction extends AbstractMnemonicsAction {
        public ProjectReloadMenuItemAction() {
            super(OStrings.getString("TF_MENU_PROJECT_RELOAD"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectReloadMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectReload();
        }
    }

    /**
     * Close project.
     */
    @SuppressWarnings("serial")
    public static class ProjectCloseMenuItemAction extends AbstractMnemonicsAction {
        public ProjectCloseMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_CLOSE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectCloseMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectClose();
        }
    }

    /**
     * Save project.
     */
    @SuppressWarnings("serial")
    public static class ProjectSaveMenuItemAction extends AbstractMnemonicsAction {
        public ProjectSaveMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_SAVE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectSaveMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectSave();
        }
    }

    /**
     * Create translated documents.
     */
    @SuppressWarnings("serial")
    public static class ProjectCompileMenuItemAction extends AbstractMnemonicsAction {
        public ProjectCompileMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_COMPILE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectCompileMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!checkTags()) {
                return;
            }

            ProjectUICommands.projectCompile();
        }
    }

    /**
     * Check whether tags are OK
     * 
     * @return false is there is a tag issue, true otherwise
     */
    private static boolean checkTags() {
        if (Preferences.isPreference(Preferences.TAGS_VALID_REQUIRED)) {
            List<ErrorReport> stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getIssues().showAll(OStrings.getString("TF_MESSAGE_COMPILE"));
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("serial")
    public static class ProjectCommitTargetFilesAction extends AbstractMnemonicsAction {
        public ProjectCommitTargetFilesAction() {
            super(OStrings.getString("TF_MENU_FILE_TARGET"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectCommitTargetFiles");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!checkTags()) {
                return;
            }

            ProjectUICommands.projectCompileAndCommit();

        }
    }

    /**
     * Commit source files
     */
    @SuppressWarnings("serial")
    public static class ProjectCommitSourceFilesAction extends AbstractMnemonicsAction {
        public ProjectCommitSourceFilesAction() {
            super(OStrings.getString("TF_MENU_FILE_COMMIT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectCommitSourceFiles");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectCommitSourceFiles();
        }
    }

    /**
     * Create current translated document.
     */
    @SuppressWarnings("serial")
    public static class ProjectSingleCompileMenuItemAction extends AbstractMnemonicsAction {
        public ProjectSingleCompileMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_SINGLE_COMPILE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectSingleCompileMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
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
    }

    /** Edits project's properties */
    @SuppressWarnings("serial")
    public static class ProjectEditMenuItemAction extends AbstractMnemonicsAction {
        public ProjectEditMenuItemAction() {
            super(OStrings.getString("MW_PROJECTMENU_EDIT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectEditMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            ProjectUICommands.projectEditProperties();
        }
    }

    @SuppressWarnings("serial")
    public static class ViewFileListMenuItemAction extends AbstractMnemonicsAction {
        public ViewFileListMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_PROJWIN"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewFileListMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            IProjectFilesList projWin = Core.getProjectFilesList();
            if (projWin != null) {
                projWin.setActive(!projWin.isActive());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessRootMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessRootMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_ROOT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessRootMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getProjectRoot();
            openFile(new File(path));

        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessDictionaryMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessDictionaryMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_DICTIONARY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessDictionaryMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getDictRoot();
            openFile(new File(path));
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessGlossaryMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessGlossaryMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_GLOSSARY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessGlossaryMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getGlossaryRoot();
            openFile(new File(path));
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessSourceMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessSourceMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_SOURCE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessSourceMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getSourceRoot();
            openFile(new File(path));

        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessTargetMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessTargetMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_TARGET"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessTargetMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getTargetRoot();
            openFile(new File(path));
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessTMMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessTMMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_TM"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessTMMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getTMRoot();
            openFile(new File(path));
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessExportTMMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessExportTMMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_EXPORT_TM"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessExportTMMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getExportTMRoot();
            openFile(new File(path));

        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessCurrentSourceDocumentMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessCurrentSourceDocumentMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_CURRENT_SOURCE_DOCUMENT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessCurrentSourceDocumentMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String root = Core.getProject().getProjectProperties().getSourceRoot();
            String path = Core.getEditor().getCurrentFile();
            if (StringUtil.isEmpty(path)) {
                return;
            }
            File toOpen = new File(root, path);
            int modifier = e.getModifiers();
            if ((modifier & ActionEvent.ALT_MASK) != 0) {
                toOpen = toOpen.getParentFile();
            }
            openFile(toOpen);
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessCurrentTargetDocumentMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessCurrentTargetDocumentMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_CURRENT_TARGET_DOCUMENT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessCurrentTargetDocumentMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String root = Core.getProject().getProjectProperties().getTargetRoot();
            String path = Core.getEditor().getCurrentTargetFile();
            if (StringUtil.isEmpty(path)) {
                return;
            }
            File toOpen = new File(root, path);
            int modifier = e.getModifiers();
            if ((modifier & ActionEvent.ALT_MASK) != 0) {
                toOpen = toOpen.getParentFile();
            }
            openFile(toOpen);
        }
    }

    @SuppressWarnings("serial")
    public static class ProjectAccessWriteableGlossaryMenuItemAction extends AbstractMnemonicsAction {
        public ProjectAccessWriteableGlossaryMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_ACCESS_WRITEABLE_GLOSSARY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectAccessWriteableGlossaryMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String path = Core.getProject().getProjectProperties().getWriteableGlossary();
            if (StringUtil.isEmpty(path)) {
                return;
            }
            File toOpen = new File(path);
            int modifier = e.getModifiers();
            if ((modifier & ActionEvent.ALT_MASK) != 0) {
                toOpen = toOpen.getParentFile();
            }
            openFile(toOpen);

        }
    }

    /** Quits OmegaT */
    @SuppressWarnings("serial")
    public static class ProjectExitMenuItemAction extends AbstractMnemonicsAction {
        public ProjectExitMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_QUIT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectExitMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            prepareForExit(() -> System.exit(0));
        }
    }

    /** Restart OmegaT */
    @SuppressWarnings("serial")
    public static class ProjectRestartMenuItemAction extends AbstractMnemonicsAction {
        public ProjectRestartMenuItemAction() {
            super(OStrings.getString("TF_MENU_FILE_RESTART"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "projectRestartMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            String projectDir = Core.getProject().isProjectLoaded()
                    ? Core.getProject().getProjectProperties().getProjectRoot()
                    : null;
            prepareForExit(() -> {
                Main.restartGUI(projectDir);
            });
        }
    }

    protected static void projectExitAction() {
        prepareForExit(() -> System.exit(0));
    }

    private static void openFile(File path) {
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

    private static void prepareForExit(Runnable onCompletion) {
        // Bug #902: commit the current entry first
        // We do it before checking project status, so that it can eventually
        // change it
        if (Core.getProject().isProjectLoaded()) {
            Core.getEditor().commitAndLeave();
        }

        boolean projectModified = false;
        if (Core.getProject().isProjectLoaded()) {
            projectModified = Core.getProject().isProjectModified();
        }
        // RFE 1302358
        // Add Yes/No Warning before OmegaT quits
        if (projectModified || Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT)) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
                    Core.getMainWindow().getApplicationFrame(), OStrings.getString("MW_QUIT_CONFIRM"),
                    OStrings.getString("CONFIRM_DIALOG_TITLE"), JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }

        SegmentExportImport.flushExportedSegments();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (Core.getProject().isProjectLoaded()) {
                    // Save the list of learned and ignore words
                    ISpellChecker sc = Core.getSpellChecker();
                    sc.saveWordLists();
                    try {
                        Core.executeExclusively(true, () -> {
                            Core.getProject().saveProject(true);
                            ProjectFactory.closeProject();
                        });
                    } catch (KnownException ex) {
                        // hide exception on shutdown
                    }
                }

                CoreEvents.fireApplicationShutdown();

                PluginUtils.unloadPlugins();

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    MainWindowUI.saveScreenLayout((MainWindow) Core.getMainWindow());

                    Preferences.save();

                    onCompletion.run();
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    @SuppressWarnings("serial")
    public static class EditUndoMenuItemAction extends AbstractMnemonicsAction {

        public EditUndoMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_UNDO"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editUndoMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused == Core.getNotes()) {
                Core.getNotes().undo();
            } else {
                Core.getEditor().undo();
            }

        }
    }

    /** Quits OmegaT */
    @SuppressWarnings("serial")
    public static class EditRedoMenuItemAction extends AbstractMnemonicsAction {
        public EditRedoMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_REDO"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editRedoMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused == Core.getNotes()) {
                Core.getNotes().redo();
            } else {
                Core.getEditor().redo();
            }
        }
    }

    @SuppressWarnings("serial")
    public static class EditOverwriteTranslationMenuItemAction extends AbstractMnemonicsAction {

        public EditOverwriteTranslationMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_RECYCLE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editOverwriteTranslationMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            MainWindow.doRecycleTrans();
        }
    }

    @SuppressWarnings("serial")
    public static class EditInsertTranslationMenuItemAction extends AbstractMnemonicsAction {
        public EditInsertTranslationMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editInsertTranslationMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            MainWindow.doInsertTrans();
        }
    }

    @SuppressWarnings("serial")
    public static class EditOverwriteMachineTranslationMenuItemAction extends AbstractMnemonicsAction {

        public EditOverwriteMachineTranslationMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_OVERWRITE_MACHITE_TRANSLATION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editOverwriteMachineTranslationMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            MachineTranslationInfo tr = Core.getMachineTranslatePane().getDisplayedTranslation();
            if (tr == null) {
                Core.getMachineTranslatePane().forceLoad();
            } else if (!StringUtil.isEmpty(tr.result)) {
                Core.getEditor().replaceEditText(tr.result, String.format("MT:[%s]", tr.translatorName));
            }
        }
    }

    /**
     * replaces entire edited segment text with a the source text of a segment
     * at cursor position
     */
    @SuppressWarnings("serial")
    public static class EditOverwriteSourceMenuItemAction extends AbstractMnemonicsAction {

        public EditOverwriteSourceMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_SOURCE_OVERWRITE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editOverwriteSourceMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
            if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
                toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
            }
            Core.getEditor().replaceEditText(toInsert);
        }
    }

    /** inserts the source text of a segment at cursor position */
    @SuppressWarnings("serial")
    public static class EditInsertSourceMenuItemAction extends AbstractMnemonicsAction {

        public EditInsertSourceMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_SOURCE_INSERT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editInsertSourceMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
            if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
                toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
            }
            Core.getEditor().insertText(toInsert);
        }
    }

    /** select the source text of the current segment */
    @SuppressWarnings("serial")
    public static class EditSelectSourceMenuItemAction extends AbstractMnemonicsAction {

        public EditSelectSourceMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_SOURCE_SELECT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectSourceMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            Core.getEditor().selectSourceText();
        }
    }

    @SuppressWarnings("serial")
    public static class EditExportSelectionMenuItemAction extends AbstractMnemonicsAction {

        public EditExportSelectionMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_EXPORT_SELECTION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editExportSelectionMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
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
    }

    @SuppressWarnings("serial")
    public static class EditSearchDictionaryMenuItemAction extends AbstractMnemonicsAction {
        public EditSearchDictionaryMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_SEARCH_DICTIONARY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSearchDictionaryMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
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
    }

    @SuppressWarnings("serial")
    public static class EditCreateGlossaryEntryMenuItemAction extends AbstractMnemonicsAction {
        public EditCreateGlossaryEntryMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_CREATE_GLOSSARY_ENTRY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editCreateGlossaryEntryMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            Core.getGlossary().showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
        }
    }

    @SuppressWarnings("serial")
    public static class EditFindInProjectMenuItemAction extends AbstractMnemonicsAction {
        public EditFindInProjectMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_FIND"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editFindInProjectMenuItem");
            putValue(Action.SMALL_ICON, Objects.requireNonNullElseGet(
                    UIManager.getIcon("OmegaT.newUI.search.icon"),
                    () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.search.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            MainWindowUI.createSearchWindow(SearchMode.SEARCH, getTrimmedSelectedTextInMainWindow());
        }
    }

    public static void findInProjectReuseLastWindow() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String text = getTrimmedSelectedTextInMainWindow();
        if (!MainWindowUI.reuseSearchWindow(text)) {
            MainWindowUI.createSearchWindow(SearchMode.SEARCH, text);
        }
    }

    @SuppressWarnings("serial")
    public static class EditReplaceInProjectMenuItemAction extends AbstractMnemonicsAction {

        public EditReplaceInProjectMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_REPLACE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editReplaceInProjectMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            MainWindowUI.createSearchWindow(SearchMode.REPLACE, getTrimmedSelectedTextInMainWindow());
        }
    }

    private static String getTrimmedSelectedTextInMainWindow() {
        String selection = null;
        Component component = Core.getMainWindow().getApplicationFrame().getMostRecentFocusOwner();
        if (component instanceof JTextComponent) {
            selection = ((JTextComponent) component).getSelectedText();
            if (!StringUtil.isEmpty(selection)) {
                selection = EditorUtils.removeDirectionChars(selection);
                selection = selection.trim();
            }
        }
        return selection;
    }

    /** Set active match to #1. */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzy1MenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzy1MenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_1"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzy1MenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setActiveMatch(0);
        }
    }

    /** Set active match to #2. */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzy2MenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzy2MenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_2"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzy2MenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setActiveMatch(1);
        }
    }

    /** Set active match to #3. */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzy3MenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzy3MenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_3"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzy3MenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setActiveMatch(2);
        }
    }

    /** Set active match to #4. */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzy4MenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzy4MenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_4"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzy4MenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setActiveMatch(3);
        }
    }

    /** Set active match to #5. */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzy5MenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzy5MenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_5"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzy5MenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setActiveMatch(4);
        }
    }

    /** Set active match to the next one */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzyNextMenuItemAction extends AbstractMnemonicsAction {
        public EditSelectFuzzyNextMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_NEXT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzyNextMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setNextActiveMatch();
        }
    }

    /** Set active match to the previous one */
    @SuppressWarnings("serial")
    public static class EditSelectFuzzyPrevMenuItemAction extends AbstractMnemonicsAction {

        public EditSelectFuzzyPrevMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_COMPARE_PREV"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editSelectFuzzyPrevMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getMatcher().setPrevActiveMatch();
        }
    }

    @SuppressWarnings("serial")
    public static class InsertCharsLRMAction extends AbstractMnemonicsAction {
        public InsertCharsLRMAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS_LRM"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "insertCharsLRM");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().insertText("\u200E");

        }
    }

    @SuppressWarnings("serial")
    public static class InsertCharsRLMAction extends AbstractMnemonicsAction {
        public InsertCharsRLMAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS_RLM"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "insertCharsRLM");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().insertText("\u200F");
        }
    }

    @SuppressWarnings("serial")
    public static class InsertCharsLREAction extends AbstractMnemonicsAction {
        public InsertCharsLREAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS_LRE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "insertCharsLRE");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().insertText("\u202A");
        }
    }

    @SuppressWarnings("serial")
    public static class InsertCharsRLEAction extends AbstractMnemonicsAction {
        public InsertCharsRLEAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS_RLE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "insertCharsRLE");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().insertText("\u202B");
        }
    }

    @SuppressWarnings("serial")
    public static class InsertCharsPDFAction extends AbstractMnemonicsAction {

        public InsertCharsPDFAction() {
            super(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS_PDF"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "insertCharsPDF");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().insertText("\u202C");
        }
    }

    @SuppressWarnings("serial")
    public static class EditMultipleDefaultAction extends AbstractMnemonicsAction {
        public EditMultipleDefaultAction() {
            super(OStrings.getString("MULT_MENU_DEFAULT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editMultipleDefault");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().setAlternateTranslationForCurrentEntry(false);
        }
    }

    @SuppressWarnings("serial")
    public static class EditMultipleAlternateAction extends AbstractMnemonicsAction {
        public EditMultipleAlternateAction() {
            super(OStrings.getString("MULT_MENU_MULTIPLE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editMultipleAlternate");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().setAlternateTranslationForCurrentEntry(true);
        }
    }

    @SuppressWarnings("serial")
    public static class EditRegisterUntranslatedMenuItemAction extends AbstractMnemonicsAction {
        public EditRegisterUntranslatedMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_UNTRANSLATED_TRANSLATION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editRegisterUntranslatedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().registerUntranslated();
        }
    }

    @SuppressWarnings("serial")
    public static class EditRegisterEmptyMenuItemAction extends AbstractMnemonicsAction {
        public EditRegisterEmptyMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_EMPTY_TRANSLATION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editRegisterEmptyMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().registerEmptyTranslation();
        }
    }

    @SuppressWarnings("serial")
    public static class EditRegisterIdenticalMenuItemAction extends AbstractMnemonicsAction {
        public EditRegisterIdenticalMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_IDENTICAL_TRANSLATION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editRegisterIdenticalMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().registerIdenticalTranslation();
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsPreferencesMenuItemAction extends AbstractMnemonicsAction {

        public OptionsPreferencesMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_PREFERENCES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsPreferencesMenuItem");
            putValue(Action.SMALL_ICON, Objects.requireNonNullElseGet(
                    UIManager.getIcon("OmegaT.newUI.settings.icon"),
                    () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.settings.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame());
        }
    }

    @SuppressWarnings("serial")
    public static class CycleSwitchCaseMenuItemAction extends AbstractMnemonicsAction {
        public CycleSwitchCaseMenuItemAction() {
            super(OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_CYCLE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "cycleSwitchCaseMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.CYCLE);
        }
    }

    @SuppressWarnings("serial")
    public static class SentenceCaseMenuItemAction extends AbstractMnemonicsAction {
        public SentenceCaseMenuItemAction() {
            super(OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_SENTENCE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "sentenceCaseMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.SENTENCE);
        }
    }

    @SuppressWarnings("serial")
    public static class TitleCaseMenuItemAction extends AbstractMnemonicsAction {
        public TitleCaseMenuItemAction() {
            super(OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_TITLE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "titleCaseMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.TITLE);
        }
    }

    @SuppressWarnings("serial")
    public static class UpperCaseMenuItemAction extends AbstractMnemonicsAction {
        public UpperCaseMenuItemAction() {
            super(OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_UPPER"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "upperCaseMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.UPPER);
        }
    }

    @SuppressWarnings("serial")
    public static class LowerCaseMenuItemAction extends AbstractMnemonicsAction {
        public LowerCaseMenuItemAction() {
            super(OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_LOWER"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "lowerCaseMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.LOWER);
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextUntranslatedMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextUntranslatedMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_UNTRANS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextUntranslatedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextUntranslatedEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextUniqueMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextUniqueMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_NEXT_UNIQUE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextUniqueMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextUniqueEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextTranslatedMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextTranslatedMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_TRANS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextTranslatedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextTranslatedEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextSegmentMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextSegmentMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_NEXT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextSegmentMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoPreviousSegmentMenuItemAction extends AbstractMnemonicsAction {
        public GotoPreviousSegmentMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_PREV"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoPreviousSegmentMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().prevEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextXAutoMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextXAutoMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_NEXT_XAUTO"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextXAutoMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextXAutoEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoPrevXAutoMenuItemAction extends AbstractMnemonicsAction {
        public GotoPrevXAutoMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_PREV_XAUTO"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoPrevXAutoMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().prevXAutoEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextXEnforcedMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextXEnforcedMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_NEXT_XENFORCED", OStrings.getLocale()));
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextXEnforcedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextXEnforcedEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoPrevXEnforcedMenuItemAction extends AbstractMnemonicsAction {
        public GotoPrevXEnforcedMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_PREV_XENFORCED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoPrevXEnforcedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().prevXEnforcedEntry();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNextNoteMenuItemAction extends AbstractMnemonicsAction {
        public GotoNextNoteMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_NEXT_NOTE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNextNoteMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().nextEntryWithNote();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoPreviousNoteMenuItemAction extends AbstractMnemonicsAction {
        public GotoPreviousNoteMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_PREV_NOTE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoPreviousNoteMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().prevEntryWithNote();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoNotesPanelMenuItemAction extends AbstractMnemonicsAction {
        public GotoNotesPanelMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_NOTES_PANEL"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoNotesPanelMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getNotes().requestFocus();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoEditorPanelMenuItemAction extends AbstractMnemonicsAction {
        public GotoEditorPanelMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_EDITOR_PANEL"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoEditorPanelMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().requestFocus();
        }
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     */
    @SuppressWarnings("serial")
    public static class GotoSegmentMenuItemAction extends AbstractMnemonicsAction {
        public GotoSegmentMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_GOTO"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoSegmentMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            // Create a dialog for input
            GoToSegmentDialog dialog = new GoToSegmentDialog(Core.getMainWindow().getApplicationFrame());
            dialog.setVisible(true);

            int jumpTo = dialog.getResult();

            if (jumpTo != -1) {
                Core.getEditor().gotoEntry(jumpTo);
            }
        }
    }

    @SuppressWarnings("serial")
    public static class GotoHistoryBackMenuItemAction extends AbstractMnemonicsAction {

        public GotoHistoryBackMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_BACK_IN_HISTORY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoHistoryBackMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().gotoHistoryBack();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoHistoryForwardMenuItemAction extends AbstractMnemonicsAction {
        public GotoHistoryForwardMenuItemAction() {
            super(OStrings.getString("TF_MENU_GOTO_FORWARD_IN_HISTORY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoHistoryForwardMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().gotoHistoryForward();
        }
    }

    @SuppressWarnings("serial")
    public static class GotoMatchSourceSegmentAction extends AbstractMnemonicsAction {
        public GotoMatchSourceSegmentAction() {
            super(OStrings.getString("TF_MENU_GOTO_SELECTED_MATCH_SOURCE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "gotoMatchSourceSegment");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            NearString ns = Core.getMatcher().getActiveMatch();
            if (ns != null && ns.comesFrom == MATCH_SOURCE.MEMORY) {
                Core.getEditor().gotoEntry(ns.source, ns.key);
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkTranslatedSegmentsCheckBoxMenuItemAction extends AbstractMnemonicsAction {

        public ViewMarkTranslatedSegmentsCheckBoxMenuItemAction() {
            super(OStrings.getString("TF_MENU_DISPLAY_MARK_TRANSLATED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkTranslatedSegmentsCheckBoxMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkTranslated(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkUntranslatedSegmentsCheckBoxMenuItemAction extends AbstractMnemonicsAction {

        public ViewMarkUntranslatedSegmentsCheckBoxMenuItemAction() {
            super(OStrings.getString("TF_MENU_DISPLAY_MARK_UNTRANSLATED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkUntranslatedSegmentsCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_UNTRANSLATED.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkUntranslated(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkParagraphStartCheckBoxMenuItemAction extends AbstractMnemonicsAction {

        public ViewMarkParagraphStartCheckBoxMenuItemAction() {
            super(OStrings.getString("TF_MENU_DISPLAY_MARK_PARAGRAPH"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkParagraphStartCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newTextIcon(Styles.EditorColor.COLOR_PARAGRAPH_START.getColor(), '\u00b6'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings()
                        .setMarkParagraphDelimitations(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewDisplaySegmentSourceCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewDisplaySegmentSourceCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_DISPLAY_SEGMENT_SOURCES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewDisplaySegmentSourceCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_SOURCE.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setDisplaySegmentSources(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkNonUniqueSegmentsCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkNonUniqueSegmentsCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_NON_UNIQUE_SEGMENTS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkNonUniqueSegmentsCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newTextIcon(Styles.EditorColor.COLOR_NON_UNIQUE.getColor(), 'M'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkNonUniqueSegments(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkNotedSegmentsCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkNotedSegmentsCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_NOTED_SEGMENTS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkNotedSegmentsCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_NOTED.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkNotedSegments(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkNBSPCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkNBSPCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_NBSP"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkNBSPCheckBoxMenuItem");
            putValue(Action.SMALL_ICON, MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_NBSP.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkNBSP(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkWhitespaceCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkWhitespaceCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_WHITESPACE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkWhitespaceCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_WHITESPACE.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkWhitespace(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkBidiCheckBoxMenuItemAction extends AbstractMnemonicsAction {

        public ViewMarkBidiCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_BIDI"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkBidiCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_BIDIMARKERS.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkBidi(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkAutoPopulatedCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkAutoPopulatedCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_AUTOPOPULATED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkAutoPopulatedCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkAutoPopulated(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkGlossaryMatchesCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkGlossaryMatchesCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_GLOSSARY_MARK"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkGlossaryMatchesCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_TRANSTIPS.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkGlossaryMatches(((JCheckBoxMenuItem) o).isSelected());
            }

        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkLanguageCheckerCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkLanguageCheckerCheckBoxMenuItemAction() {
            super(OStrings.getString("LT_OPTIONS_MENU_ENABLED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkLanguageCheckerCheckBoxMenuItem");
            putValue(Action.SMALL_ICON,
                    MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setMarkLanguageChecker(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewMarkFontFallbackCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public ViewMarkFontFallbackCheckBoxMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MARK_FONT_FALLBACK"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewMarkFontFallbackCheckBoxMenuItem");
            putValue(Action.SMALL_ICON, MainMenuIcons.newTextIcon(UIManager.getColor("Label.foreground"),
                    new Font("Serif", Font.ITALIC, 16), 'F'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Core.getEditor().getSettings().setDoFontFallback(((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ViewDisplayModificationInfoNoneRadioButtonMenuItemAction
            extends AbstractMnemonicsAction {
        public ViewDisplayModificationInfoNoneRadioButtonMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MODIFICATION_INFO_NONE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewDisplayModificationInfoNoneRadioButtonMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().getSettings()
                    .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE);
        }
    }

    @SuppressWarnings("serial")
    public static class ViewDisplayModificationInfoSelectedRadioButtonMenuItemAction
            extends AbstractMnemonicsAction {
        public ViewDisplayModificationInfoSelectedRadioButtonMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MODIFICATION_INFO_SELECTED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewDisplayModificationInfoSelectedRadioButtonMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().getSettings()
                    .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED);
        }
    }

    @SuppressWarnings("serial")
    public static class ViewDisplayModificationInfoAllRadioButtonMenuItemAction
            extends AbstractMnemonicsAction {
        public ViewDisplayModificationInfoAllRadioButtonMenuItemAction() {
            super(OStrings.getString("MW_VIEW_MENU_MODIFICATION_INFO_ALL"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewDisplayModificationInfoAllRadioButtonMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getEditor().getSettings()
                    .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL);
        }
    }

    @SuppressWarnings("serial")
    public static class ToolsCheckIssuesMenuItemAction extends AbstractMnemonicsAction {
        public ToolsCheckIssuesMenuItemAction() {
            super(OStrings.getString("TF_MENU_TOOLS_CHECK_ISSUES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "toolsCheckIssuesMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            if (!Preferences.isPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK)) {
                IssueProvidersSelectorController dialog = new IssueProvidersSelectorController();
                if (!dialog.show(Core.getMainWindow().getApplicationFrame())) {
                    return;
                }
            }
            Core.getIssues().showAll();

        }
    }

    @SuppressWarnings("serial")
    public static class ToolsCheckIssuesCurrentFileMenuItemAction extends AbstractMnemonicsAction {
        public ToolsCheckIssuesCurrentFileMenuItemAction() {
            super(OStrings.getString("TF_MENU_TOOLS_CHECK_ISSUES_CURRENT_FILE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "toolsCheckIssuesCurrentFileMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Core.getIssues().showForFiles(Pattern.quote(Core.getEditor().getCurrentFile()));
        }
    }

    /**
     * Identify all the placeholders in the source text and automatically
     * inserts them into the target text.
     */
    @SuppressWarnings("serial")
    public static class EditTagPainterMenuItemAction extends AbstractMnemonicsAction {
        public EditTagPainterMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_TAGPAINT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editTagPainterMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            // insert tags
            for (Tag tag : TagUtil.getAllTagsMissingFromTarget()) {
                Core.getEditor().insertTag(tag.tag);
            }
        }
    }

    @SuppressWarnings("serial")
    public static class EditTagNextMissedMenuItemAction extends AbstractMnemonicsAction {
        public EditTagNextMissedMenuItemAction() {
            super(OStrings.getString("TF_MENU_EDIT_TAG_NEXT_MISSED"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "editTagNextMissedMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            // insert next tag
            List<Tag> tags = TagUtil.getAllTagsMissingFromTarget();
            if (tags.isEmpty()) {
                return;
            }
            Core.getEditor().insertTag(tags.get(0).tag);
        }
    }

    @SuppressWarnings("serial")
    public static class ToolsShowStatisticsStandardMenuItemAction extends AbstractMnemonicsAction {
        public ToolsShowStatisticsStandardMenuItemAction() {
            super(OStrings.getString("TF_MENU_TOOLS_STATISTICS_STANDARD"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "toolsShowStatisticsStandardMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                    StatisticsWindow.STAT_TYPE.STANDARD).setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    public static class ToolsShowStatisticsMatchesMenuItemAction extends AbstractMnemonicsAction {
        public ToolsShowStatisticsMatchesMenuItemAction() {
            super(OStrings.getString("TF_MENU_TOOLS_STATISTICS_MATCHES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "toolsShowStatisticsMatchesMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                    StatisticsWindow.STAT_TYPE.MATCHES).setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    public static class ToolsShowStatisticsMatchesPerFileMenuItemAction extends AbstractMnemonicsAction {
        public ToolsShowStatisticsMatchesPerFileMenuItemAction() {
            super(OStrings.getString("TF_MENU_TOOLS_STATISTICS_MATCHES_PER_FILE"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "toolsShowStatisticsMatchesPerFileMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                    StatisticsWindow.STAT_TYPE.MATCHES_PER_FILE).setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsAutoCompleteShowAutomaticallyItemAction extends AbstractMnemonicsAction {
        public OptionsAutoCompleteShowAutomaticallyItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_AUTOCOMPLETE_SHOW_AUTOMATICALLY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsAutoCompleteShowAutomaticallyItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY,
                        ((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsAutoCompleteHistoryCompletionMenuItemAction extends AbstractMnemonicsAction {
        public OptionsAutoCompleteHistoryCompletionMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_AUTOCOMPLETE_HISTORY_COMPLETION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsAutoCompleteHistoryCompletionMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.AC_HISTORY_COMPLETION_ENABLED,
                        ((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsAutoCompleteHistoryPredictionMenuItemAction extends AbstractMnemonicsAction {
        public OptionsAutoCompleteHistoryPredictionMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_AUTOCOMPLETE_HISTORY_PREDICTION"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsAutoCompleteHistoryPredictionMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED,
                        ((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsMTAutoFetchCheckboxMenuItemAction extends AbstractMnemonicsAction {
        public OptionsMTAutoFetchCheckboxMenuItemAction() {
            super(OStrings.getString("MT_AUTO_FETCH"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsMTAutoFetchCheckboxMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.MT_AUTO_FETCH, ((JCheckBoxMenuItem) o).isSelected());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsGlossaryFuzzyMatchingCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public OptionsGlossaryFuzzyMatchingCheckBoxMenuItemAction() {
            super(OStrings.getString("TF_OPTIONSMENU_GLOSSARY_FUZZY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsGlossaryFuzzyMatchingCheckBoxMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.GLOSSARY_STEMMING,
                        ((JCheckBoxMenuItem) o).isSelected());
                Preferences.save();
            }
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsDictionaryFuzzyMatchingCheckBoxMenuItemAction extends AbstractMnemonicsAction {
        public OptionsDictionaryFuzzyMatchingCheckBoxMenuItemAction() {
            super(OStrings.getString("TF_OPTIONSMENU_DICTIONARY_FUZZY"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsDictionaryFuzzyMatchingCheckBoxMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            Object o = e.getSource();
            if (o instanceof JCheckBoxMenuItem) {
                Preferences.setPreference(Preferences.DICTIONARY_FUZZY_MATCHING,
                        ((JCheckBoxMenuItem) o).isSelected());
                Preferences.save();
            }
        }
    }

    /**
     * Displays the filters setup dialog to allow customizing file filters in
     * detail.
     */
    @SuppressWarnings("serial")
    public static class OptionsSetupFileFiltersMenuItemAction extends AbstractMnemonicsAction {
        public OptionsSetupFileFiltersMenuItemAction() {
            super(OStrings.getString("TF_MENU_DISPLAY_GLOBAL_FILTERS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsSetupFileFiltersMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                    FiltersCustomizerController.class);
        }
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the
     * segmentation rules in detail.
     */
    @SuppressWarnings("serial")
    public static class OptionsSentsegMenuItemAction extends AbstractMnemonicsAction {
        public OptionsSentsegMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_GLOBAL_SENTSEG"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsSentsegMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                    SegmentationCustomizerController.class);
        }
    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse
     * workflow options.
     */
    @SuppressWarnings("serial")
    public static class OptionsWorkflowMenuItemAction extends AbstractMnemonicsAction {
        public OptionsWorkflowMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_WORKFLOW"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsWorkflowMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new PreferencesWindowController().show(Core.getMainWindow().getApplicationFrame(),
                    EditingBehaviorController.class);
        }
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     */
    @SuppressWarnings("serial")
    public static class ViewRestoreGUIMenuItemAction extends AbstractMnemonicsAction {
        public ViewRestoreGUIMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_RESTORE_GUI"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "viewRestoreGUIMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()); // FIXME
        }
    }

    @SuppressWarnings("serial")
    public static class OptionsAccessConfigDirMenuItemAction extends AbstractMnemonicsAction {
        public OptionsAccessConfigDirMenuItemAction() {
            super(OStrings.getString("MW_OPTIONSMENU_ACCESS_CONFIG_DIR"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "optionsAccessConfigDirMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            openFile(new File(StaticUtils.getConfigDir()));
        }
    }

    /**
     * Show help.
     */
    @SuppressWarnings("serial")
    public static class HelpContentsMenuItemAction extends AbstractMnemonicsAction {
        public HelpContentsMenuItemAction() {
            super(OStrings.getString("TF_MENU_HELP_CONTENTS"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "helpContentsMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            try {
                Help.showHelp();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                        ex.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                Log.log(ex);
            }
        }
    }

    /**
     * Shows About dialog
     */
    @SuppressWarnings("serial")
    public static class HelpAboutMenuItemAction extends AbstractMnemonicsAction {

        public HelpAboutMenuItemAction() {
            super(OStrings.getString("TF_MENU_HELP_ABOUT"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "helpAboutMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new AboutDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
        }
    }

    /**
     * Shows Last changes
     */
    @SuppressWarnings("serial")
    public static class HelpLastChangesMenuItemAction extends AbstractMnemonicsAction {
        public HelpLastChangesMenuItemAction() {
            super(OStrings.getString("TF_MENU_HELP_LAST_CHANGES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "helpLastChangesMenuItem");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new LastChangesDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
        }
    }

    /**
     * Show log
     */
    @SuppressWarnings("serial")
    public static class HelpLogMenuItemAction extends AbstractMnemonicsAction {
        public HelpLogMenuItemAction() {
            super(OStrings.getString("TF_MENU_HELP_LOG"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "helpLogMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            new LogDialog(Core.getMainWindow().getApplicationFrame()).setVisible(true);
        }
    }

    /**
     * Check for updates
     */
    @SuppressWarnings("serial")
    public static class HelpUpdateCheckMenuItemAction extends AbstractMnemonicsAction {
        public HelpUpdateCheckMenuItemAction() {
            super(OStrings.getString("TF_MENU_HELP_CHECK_FOR_UPDATES"), OStrings.getLocale());
            putValue(Action.ACTION_COMMAND_KEY, "helpUpdateCheckMenuItem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
            VersionCheckDialog.checkAndShowResultAsync(Core.getMainWindow().getApplicationFrame());
        }
    }
}
