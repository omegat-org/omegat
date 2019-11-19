/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2016 Alex Buloichik
               2011 Martin Fleurke
               2012 Thomas Cordonnier
               2013 Yu Tang
               2014 Aaron Madlon-Kay, Piotr Kulik
               2015 Aaron Madlon-Kay, Yu Tang
               2016 Alex Buloichik             
               2017 Didier Briel
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.omegat.CLIParameters;
import org.omegat.convert.ConvertProject;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.dialogs.ChooseMedProject;
import org.omegat.gui.dialogs.FileCollisionDialog;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.dialogs.NewTeamProject;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.FileUtil;
import org.omegat.util.FileUtil.ICollisionCallback;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RecentProjects;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.WikiGet;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.OpenProjectFileChooser;
import org.omegat.util.gui.UIThreadsUtil;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Handler for project UI commands, like open, save, compile, etc.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Thomas Cordonnier
 * @author Aaron Madlon-Kay
 * @author Didier Briel
 */
public final class ProjectUICommands {

    private ProjectUICommands() {
    }

    public static void projectCreate() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }

        // ask for new project dir
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
        if (ndcResult != OmegaTFileChooser.APPROVE_OPTION) {
            // user press 'Cancel' in project creation dialog
            return;
        }
        final File dir = ndc.getSelectedFile();
        if (!ensureProjectDir(dir)) {
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {

                // ask about new project properties
                ProjectProperties props = new ProjectProperties(dir);
                props.setSourceLanguage(Preferences.getPreferenceDefault(Preferences.SOURCE_LOCALE, "EN-US"));
                props.setTargetLanguage(Preferences.getPreferenceDefault(Preferences.TARGET_LOCALE, "EN-GB"));
                ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(
                        Core.getMainWindow().getApplicationFrame(),
                        props, dir.getAbsolutePath(),
                        ProjectPropertiesDialog.Mode.NEW_PROJECT);
                newProjDialog.setVisible(true);
                newProjDialog.dispose();

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                final ProjectProperties newProps = newProjDialog.getResult();
                if (newProps == null) {
                    // user clicks on 'Cancel'
                    dir.delete();
                    mainWindow.setCursor(oldCursor);
                    return null;
                }

                final String projectRoot = newProps.getProjectRoot();

                if (!StringUtil.isEmpty(projectRoot)) {
                    // create project
                    try {
                        ProjectFactory.createProject(newProps);
                        RecentProjects.add(dir.getAbsolutePath());
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    }
                }

                mainWindow.setCursor(oldCursor);
                return null;
            }
        }.execute();
    }

    public static void projectOpenMED() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }

        // ask for MED file
        ChooseMedProject ndm = new ChooseMedProject();
        int ndmResult = ndm.showOpenDialog(Core.getMainWindow().getApplicationFrame());
        if (ndmResult != OmegaTFileChooser.APPROVE_OPTION) {
            // user press 'Cancel' in project creation dialog
            return;
        }
        final File med = ndm.getSelectedFile();

        // ask for new project dir
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
        if (ndcResult != OmegaTFileChooser.APPROVE_OPTION) {
            // user press 'Cancel' in project creation dialog
            return;
        }
        final File dir = ndc.getSelectedFile();
        if (!ensureProjectDir(dir)) {
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {

                final ProjectProperties newProps = new ProjectProperties(dir);
                ProjectMedProcessing.extractFromMed(med, newProps);
                // create project
                try {
                    ProjectFactory.createProject(newProps);
                    RecentProjects.add(dir.getAbsolutePath());
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }

                return null;
            }

            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(Core.getEditor()::requestFocus);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectCreateMED() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        // ask for new MED file
        ChooseMedProject ndm = new ChooseMedProject();
        // default name
        String zipName = null;
        try {
            File origin = ProjectMedProcessing.getOriginMedFile(Core.getProject().getProjectProperties());
            if (origin != null) {
                zipName = origin.getName();
            }
        } catch (Exception ex) {
        }
        if (zipName == null) {
            zipName = Core.getProject().getProjectProperties().getProjectName() + "-MED.zip";
        }
        ndm.setSelectedFile(new File(
                Core.getProject().getProjectProperties().getProjectRootDir().getParentFile(), zipName));
        int ndmResult = ndm.showSaveDialog(Core.getMainWindow().getApplicationFrame());
        if (ndmResult != OmegaTFileChooser.APPROVE_OPTION) {
            // user press 'Cancel' in project creation dialog
            return;
        }
        // add .zip extension if there is no
        final File med = ndm.getSelectedFile().getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")
                ? ndm.getSelectedFile() : new File(ndm.getSelectedFile().getAbsolutePath() + ".zip");

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                mainWindow.showStatusMessageRB("MW_STATUS_SAVING");

                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    try {
                        Core.getProject().compileProject(".*");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                ProjectMedProcessing.createMed(med, Core.getProject().getProjectProperties());

                mainWindow.showStatusMessageRB("MW_STATUS_SAVED");
                mainWindow.setCursor(oldCursor);
                return null;
            }

            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(Core.getEditor()::requestFocus);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectTeamCreate() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }
        new SwingWorker<Void, Void>() {
            File projectRoot;
            protected Void doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB(null);

                final NewTeamProject dialog = new NewTeamProject(Core.getMainWindow().getApplicationFrame());
                dialog.setVisible(true);

                if (!dialog.ok) {
                    Core.getMainWindow().showStatusMessageRB("TEAM_CANCELLED");
                    return null;
                }

                File dir = new File(dialog.getSaveLocation());
                if (!ensureProjectDir(dir)) {
                    return null;
                }

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);
                Core.getMainWindow().showStatusMessageRB("CT_DOWNLOADING_PROJECT");

                // retrieve omegat.project
                projectRoot = dir;
                List<RepositoryDefinition> repos = new ArrayList<RepositoryDefinition>();
                RepositoryDefinition repo = new RepositoryDefinition();
                repos.add(repo);
                repo.setType(dialog.getRepoType());
                repo.setUrl(dialog.getRepoUrl());
                RepositoryMapping mapping = new RepositoryMapping();
                mapping.setLocal("");
                mapping.setRepository("");
                repo.getMapping().add(mapping);

                RemoteRepositoryProvider remoteRepositoryProvider = new RemoteRepositoryProvider(projectRoot,
                        repos);
                remoteRepositoryProvider.switchAllToLatest();
                for (String file : new String[] { OConsts.FILE_PROJECT,
                        OConsts.DEFAULT_INTERNAL + '/' + FilterMaster.FILE_FILTERS,
                        OConsts.DEFAULT_INTERNAL + '/' + SRX.CONF_SENTSEG }) {
                    remoteRepositoryProvider.copyFilesFromRepoToProject(file);
                }

                ProjectProperties props = ProjectFileStorage.loadProjectProperties(projectRoot);
                if (props.getRepositories() == null) { // We assume it's a 3.6 style project with no repository mapping,
                    props.setRepositories(repos);      // so we add root repository mapping
                }
                // We write in all cases, because we might have added default excludes, for instance
                ProjectFileStorage.writeProjectFile(props);

                //String projectFileURL = dialog.txtRepositoryOrProjectFileURL.getText();
                //File localDirectory = new File(dialog.txtDirectory.getText());
//                try {
//                    localDirectory.mkdirs();
//                    byte[] projectFile = WikiGet.getURLasByteArray(projectFileURL);
//                    FileUtils.writeByteArrayToFile(new File(localDirectory, OConsts.FILE_PROJECT), projectFile);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    Core.getMainWindow().displayErrorRB(ex, "TEAM_CHECKOUT_ERROR");
//                    mainWindow.setCursor(oldCursor);
//                    return null;
//                }

//                projectOpen(localDirectory);

                mainWindow.setCursor(oldCursor);
                return null;
            }
            @Override
            protected void done() {
                Core.getMainWindow().showProgressMessage(" ");
                try {
                    get();
                    if (projectRoot != null) {
                        // don't ask open if user cancelled previous dialog
                        SwingUtilities.invokeLater(() -> {
                            Core.getEditor().requestFocus();
                            projectOpen(projectRoot);
                        });

                    }
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_DOWNLOAD_TEAM_PROJECT");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_DOWNLOAD_TEAM_PROJECT");
                }
            }
        }.execute();
    }

    /**
     * Open project. Does nothing if there is already a project open.
     * Convenience method for {@link #projectOpen(File, boolean)}.
     *
     * @param projectDirectory
     */
    public static void projectOpen(File projectDirectory) {
        projectOpen(projectDirectory, false);
    }

    /**
     * Open project. Does nothing if a project is already open and closeCurrent is false.
     *
     * @param projectDirectory
     *            project directory or null if user must choose it
     * @param closeCurrent
     *            whether or not to close the current project first, if any
     */
    public static void projectOpen(final File projectDirectory, boolean closeCurrent) {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            if (closeCurrent) {
                // Register to try again after closing the current project.
                CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
                    public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                        if (eventType == PROJECT_CHANGE_TYPE.CLOSE) {
                            projectOpen(projectDirectory, false);
                            CoreEvents.unregisterProjectChangeListener(this);
                        }
                    }
                });
                projectClose();
            }
            return;
        }

        final File projectRootFolder;
        if (projectDirectory == null) {
            // select existing project file - open it
            OmegaTFileChooser pfc = new OpenProjectFileChooser();
            if (OmegaTFileChooser.APPROVE_OPTION != pfc.showOpenDialog(Core.getMainWindow()
                    .getApplicationFrame())) {
                return;
            }
            projectRootFolder = pfc.getSelectedFile();
        } else {
            projectRootFolder = projectDirectory;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                try {
                    // convert old projects if need
                    ConvertProject.convert(projectRootFolder);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_CONVERT_PROJECT");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_CONVERT_PROJECT");
                    mainWindow.setCursor(oldCursor);
                    return null;
                }
                
                // check if project okay
                ProjectProperties props;
                try {
                    props = ProjectFileStorage.loadProjectProperties(projectRootFolder.getAbsoluteFile());
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    mainWindow.setCursor(oldCursor);
                    return null;
                }

                try {
                    File projectFile = new File(projectRootFolder, OConsts.FILE_PROJECT);
                    boolean needToSaveProperties = false;
                    File restoreOnFail = null;
                    if (props.hasRepositories()) { // This is a remote project
                        if (!Core.getParams().containsKey(CLIParameters.NO_TEAM)) {
                            // Save repository mapping
                            List<RepositoryDefinition> repos = props.getRepositories();
                            // Update project.properties
                            mainWindow.showStatusMessageRB("TEAM_OPEN");
                            try {
                                RemoteRepositoryProvider remoteRepositoryProvider = 
                                        new RemoteRepositoryProvider(props.getProjectRootDir(),
                                        props.getRepositories());
                                remoteRepositoryProvider.switchToVersion(OConsts.FILE_PROJECT, null);
                                restoreOnFail = FileUtil.backupFile(projectFile);
                                // Overwrite omegat.project
                                remoteRepositoryProvider.copyFilesFromRepoToProject(OConsts.FILE_PROJECT);
                                // Reload project properties
                                props = ProjectFileStorage.loadProjectProperties(projectRootFolder.getAbsoluteFile());
                                if (props.getRepositories() == null) { // We have a 3.6 style project,
                                    props.setRepositories(repos);      // so we restore the mapping we just lost
                                    needToSaveProperties = true;
                                }
                            } catch (IRemoteRepository2.NetworkException e) {
                                // Do nothing. Network errors are handled in RealProject.
                            } catch (Exception e) {
                                Log.logErrorRB(e, "TF_PROJECT_PROPERTIES_ERROR");
                                throw e;
                            }
                        }                       
                        // team project - non-exist directories could be created from repo
                        props.autocreateDirectories();
                    } else {
                        // not a team project - ask for non-exist directories
                        while (!props.isProjectValid()) {
                            needToSaveProperties = true;
                            // something wrong with the project - display open dialog
                            // to fix it
                            ProjectPropertiesDialog prj = new ProjectPropertiesDialog(
                                    Core.getMainWindow().getApplicationFrame(), props,
                                    projectFile.getAbsolutePath(),
                                    ProjectPropertiesDialog.Mode.RESOLVE_DIRS);
                            prj.setVisible(true);
                            props = prj.getResult();
                            prj.dispose();
                            if (props == null) {
                                // user clicks on 'Cancel'
                                mainWindow.setCursor(oldCursor);
                                return null;
                            }
                        }
                    }

                    final ProjectProperties propsP = props;
                    final File restoreOnFailFinal = restoreOnFail;
                    Core.executeExclusively(true, () -> {
                        boolean succeeded = ProjectFactory.loadProject(propsP, true);
                        if (restoreOnFailFinal != null) {
                            if (succeeded) {
                                Files.deleteIfExists(restoreOnFailFinal.toPath());
                            } else {
                                Files.move(restoreOnFailFinal.toPath(), projectFile.toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    });
                    if (needToSaveProperties) {
                        Core.getProject().saveProjectProperties();
                    }
                    RecentProjects.add(projectRootFolder.getAbsolutePath());
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }

                mainWindow.setCursor(oldCursor);
                return null;
            }

            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(Core.getEditor()::requestFocus);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    /**
     * Prompt the user to reload the current project
     */
    public static void promptReload() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        // asking to reload a project
        int res = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                OStrings.getString("MW_REOPEN_QUESTION"), OStrings.getString("MW_REOPEN_TITLE"),
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            projectReload();
        }
    }

    public static void projectReload() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        final ProjectProperties props = Core.getProject().getProjectProperties();

        new SwingWorker<Void, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Void doInBackground() throws Exception {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    ProjectFactory.closeProject();

                    ProjectFactory.loadProject(props, true);
                });
                mainWindow.setCursor(oldCursor);
                return null;
            }

            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(() -> {
                        // activate entry later - after project will be loaded
                        Core.getEditor().gotoEntry(previousCurEntryNum);
                        Core.getEditor().requestFocus();
                    });
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectSave() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                mainWindow.showStatusMessageRB("MW_STATUS_SAVING");

                Core.executeExclusively(true, () -> Core.getProject().saveProject(true));

                mainWindow.showStatusMessageRB("MW_STATUS_SAVED");
                mainWindow.setCursor(oldCursor);
                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectClose() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVING");

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                Preferences.save();

                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    ProjectFactory.closeProject();
                });

                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVED");
                mainWindow.setCursor(oldCursor);

                // fix - reset progress bar to defaults
                Core.getMainWindow().showLengthMessage(OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
                Core.getMainWindow().showProgressMessage(
                        Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                                MainWindowUI.StatusBarMode.DEFAULT) == MainWindowUI.StatusBarMode.DEFAULT
                        ? OStrings.getString("MW_PROGRESS_DEFAULT") : OStrings.getProgressBarDefaultPrecentageText());

                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
                // Restore global prefs in case project had project-specific ones
                Core.setFilterMaster(new FilterMaster(Preferences.getFilters()));
                Core.setSegmenter(new Segmenter(Preferences.getSRX()));
            }
        }.execute();
    }

    public static void projectEditProperties() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        // displaying the dialog to change paths and other properties
        ProjectPropertiesDialog prj = new ProjectPropertiesDialog(Core.getMainWindow().getApplicationFrame(),
                Core.getProject().getProjectProperties(),
                Core.getProject().getProjectProperties().getProjectName(),
                ProjectPropertiesDialog.Mode.EDIT_PROJECT);
        prj.setVisible(true);
        final ProjectProperties newProps = prj.getResult();
        prj.dispose();
        if (newProps == null) {
            return;
        }

        int res = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                OStrings.getString("MW_REOPEN_QUESTION"), OStrings.getString("MW_REOPEN_TITLE"),
                JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Void, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Void doInBackground() throws Exception {
                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    ProjectFactory.closeProject();

                    ProjectFactory.loadProject(newProps, true);
                });
                return null;
            }

            protected void done() {
                try {
                    get();
                    // Make sure to update Editor title
                    SwingUtilities.invokeLater(() -> {
                        // activate entry later - after project will be loaded
                        Core.getEditor().gotoEntry(previousCurEntryNum);
                        Core.getEditor().requestFocus();
                    });
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectCompile() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    try {
                        Core.getProject().compileProject(".*");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "TF_COMPILE_ERROR");
                }
            }
        }.execute();
    }

    public static void projectSingleCompile(final String sourcePattern) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(false);
                    try {
                        Core.getProject().compileProject(sourcePattern);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "TF_COMPILE_ERROR");
                }
            }
        }.execute();
    }

    public static void projectCompileAndCommit() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                Core.executeExclusively(true, () -> {
                    Core.getProject().saveProject(true);
                    try {
                        Core.getProject().compileProjectAndCommit(".*", true, true);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "TF_COMPILE_ERROR");
                }
            }
        }.execute();
    }

    public static void projectCommitSourceFiles() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // Commit the current entry first
        Core.getEditor().commitAndLeave();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Core.executeExclusively(true, () -> {
                    try {
                        Core.getProject().commitSourceFiles();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "TF_COMMIT_ERROR");
                }
            }
        }.execute();
    }

    public static void projectRemote(String url) {
        String dir = url.replace("/", "_").replace(':', '_');
        File projectDir = new File(StaticUtils.getConfigDir() + "/remoteProjects/" + dir);
        File projectFile = new File(projectDir, OConsts.FILE_PROJECT);

        byte[] data;
        try {
            projectDir.mkdirs();
            data = WikiGet.getURLasByteArray(url);
            FileUtils.writeByteArrayToFile(projectFile, data);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "TEAM_REMOTE_RETRIEVE_ERROR", url);
            Core.getMainWindow().displayErrorRB(ex, "TEAM_REMOTE_RETRIEVE_ERROR", url);
            return;
        }

        projectOpen(projectDir);
    }

    /**
     * Copy the specified files to the specified destination. The project will be reloaded afterward.
     * <p>
     * Convenience method for {@link #projectImportFiles(String, File[], boolean)}.
     *
     * @param destination
     *            The path to copy the files to
     * @param toImport
     *            Files to copy to destination path
     */
    public static void projectImportFiles(String destination, File[] toImport) {
        projectImportFiles(destination, toImport, true);
    }

    /**
     * Copy the specified files to the specified destination, then reload if indicated. Note that a modal
     * dialog will be shown if any of the specified files would be overwritten.
     *
     * @param destination
     *            The path to copy the files to
     * @param toImport
     *            Files to copy to destination path
     * @param doReload
     *            If true, the project will be reloaded after the files are successfully copied
     */
    public static void projectImportFiles(String destination, File[] toImport, boolean doReload) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        try {
            FileUtil.copyFilesTo(new File(destination), toImport, new CollisionCallback());
            if (doReload) {
                projectReload();
            }
        } catch (IOException ioe) {
            Core.getMainWindow().displayErrorRB(ioe, "MAIN_ERROR_File_Import_Failed");
        }
    }

    private static class CollisionCallback implements ICollisionCallback {
        private boolean isCanceled = false;
        private boolean yesToAll = false;

        @Override
        public boolean shouldReplace(File file, int index, int total) {
            if (isCanceled) {
                return false;
            }
            if (yesToAll) {
                return true;
            }
            FileCollisionDialog dialog = new FileCollisionDialog(Core.getMainWindow().getApplicationFrame());
            dialog.setFilename(file.getName());
            dialog.enableApplyToAll(total - index > 1);
            dialog.pack();
            dialog.setVisible(true);
            isCanceled = dialog.userDidCancel();
            if (isCanceled) {
                return false;
            }
            yesToAll = dialog.isApplyToAll() && dialog.shouldReplace();
            return yesToAll || dialog.shouldReplace();
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }
    };

    /**
     * Imports the file/files/folder into project's source files.
     */
    public static void doPromptImportSourceFiles() {
        OmegaTFileChooser chooser = new OmegaTFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle(OStrings.getString("TF_FILE_IMPORT_TITLE"));

        int result = chooser.showOpenDialog(Core.getMainWindow().getApplicationFrame());
        if (result == OmegaTFileChooser.APPROVE_OPTION) {
            File[] selFiles = chooser.getSelectedFiles();
            projectImportFiles(Core.getProject().getProjectProperties().getSourceRoot(), selFiles);
        }
    }

    /**
     * Does wikiread
     */
    public static void doWikiImport() {
        String remoteUrl = JOptionPane.showInputDialog(Core.getMainWindow().getApplicationFrame(),
                OStrings.getString("TF_WIKI_IMPORT_PROMPT"),
                OStrings.getString("TF_WIKI_IMPORT_TITLE"), JOptionPane.OK_CANCEL_OPTION);
        String projectsource = Core.getProject().getProjectProperties().getSourceRoot();
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            // [1762625] Only try to get MediaWiki page if a string has been entered
            return;
        }
        try {
            WikiGet.doWikiGet(remoteUrl, projectsource);
            projectReload();
        } catch (Exception ex) {
            Log.log(ex);
            Core.getMainWindow().displayErrorRB(ex, "TF_WIKI_IMPORT_FAILED");
        }
    }

    private static boolean ensureProjectDir(File dir) {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            Log.logErrorRB("CT_ERROR_CREATING_PROJECT_DIR", dir);
            Core.getMainWindow().displayWarningRB("CT_ERROR_CREATING_PROJECT");
            return false;
        }
        Path path = dir.toPath();
        // Use NIO methods because File.canRead/canWrite give incorrect responses on Windows
        if (!Files.isWritable(path) || !Files.isReadable(path)) {
            Log.logErrorRB("CT_ERROR_PROJECT_DIR_PERMISSIONS", path);
            Core.getMainWindow().displayWarningRB("CT_ERROR_CREATING_PROJECT");
            return false;
        }
        return true;
    }

    private static void processSwingWorkerException(Exception ex, String errorCode) {
        if (ex instanceof ExecutionException) {
            Log.logErrorRB(ex.getCause(), errorCode);
            if (ex.getCause() instanceof KnownException) {
                KnownException e = (KnownException) ex.getCause();
                Core.getMainWindow().displayErrorRB(e.getCause(), e.getMessage(), e.getParams());
            } else {
                Core.getMainWindow().displayErrorRB(ex.getCause(), errorCode);
            }
        } else {
            Log.logErrorRB(ex, errorCode);
            Core.getMainWindow().displayErrorRB(ex, errorCode);
        }
    }
}
