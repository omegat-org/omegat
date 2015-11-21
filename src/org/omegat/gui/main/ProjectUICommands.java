/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2010 Alex Buloichik
               2011 Martin Fleurke
               2012 Thomas Cordonnier
               2013 Yu Tang
               2014 Aaron Madlon-Kay, Piotr Kulik
               2015 Aaron Madlon-Kay, Yu Tang
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

package org.omegat.gui.main;

import java.awt.Cursor;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.team.GITRemoteRepository;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.RepositoryUtils;
import org.omegat.core.team.SVNRemoteRepository;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.dialogs.NewTeamProject;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RecentProjects;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.OpenProjectFileChooser;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Handler for project UI commands, like open, save, compile, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Thomas Cordonnier
 * @author Aaron Madlon-Kay
 */
public class ProjectUICommands {
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

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {

                dir.mkdirs();

                // ask about new project properties
                ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(
                        new ProjectProperties(dir), dir.getAbsolutePath(),
                        ProjectPropertiesDialog.Mode.NEW_PROJECT);
                newProjDialog.setVisible(true);
                newProjDialog.dispose();

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
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
                        Core.getProject().saveProjectProperties();
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    }
                }
                
                RecentProjects.add(dir.getAbsolutePath());
                
                mainWindow.setCursor(oldCursor);
                return null;
            }
        }.execute();
    }

    public static void projectTeamCreate() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }
        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB(null);

                final NewTeamProject dialog = new NewTeamProject(Core.getMainWindow().getApplicationFrame());
                dialog.setVisible(true);

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                final IRemoteRepository repository;
                File localDirectory = new File(dialog.txtDirectory.getText());
                try {
                    if (!dialog.ok) {
                        Core.getMainWindow().showStatusMessageRB("TEAM_CANCELLED");
                        mainWindow.setCursor(oldCursor);
                        return null;
                    }
                    if (dialog.repoType != null) {
                        repository = dialog.repoType.getConstructor(File.class).newInstance(localDirectory);
                        repository.setCredentials(dialog.credentials);
                    } else {
                        mainWindow.setCursor(oldCursor);
                        return null;
                    }

                    //do checkoutFullProject. This can throw IRemoteRepository.AuthenticationException,
                    //so we wrap it in a AskCredentials object that will show credentials dialog.
                    new RepositoryUtils.AskCredentials() {
                        public void callRepository() throws Exception {
                            Core.getMainWindow().showStatusMessageRB("TEAM_CHECKOUT");
                            repository.checkoutFullProject(dialog.txtRepositoryURL.getText());
                            Core.getMainWindow().showStatusMessageRB(null);
                        }
                    }.execute(repository);
                } catch (IRemoteRepository.BadRepositoryException bre) {
                    Core.getMainWindow().showErrorDialogRB("TF_ERROR", "TEAM_BADREPOSITORY_ERROR",
                            bre.getMessage());
                    mainWindow.setCursor(oldCursor);
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Core.getMainWindow().displayErrorRB(ex, "TEAM_CHECKOUT_ERROR");
                    mainWindow.setCursor(oldCursor);
                    return null;
                } finally {
                    if (dialog.credentials != null) {
                        dialog.credentials.clear();
                    }
                }

                try {
                    ProjectProperties props = ProjectFileStorage.loadProjectProperties(localDirectory);
                    //empty directories might not exist in VCS. Some project folders can be empty. Let's try to make them if needed.
                    File[] projectFolders = {new File(props.getGlossaryRoot()), new File(props.getTMRoot()), new File(props.getTMAutoRoot()),new File(props.getDictRoot()), new File(props.getTargetRoot())};
                    for (File f : projectFolders) {
                        try {
                            if (!f.exists()) {
                                f.mkdir();
                            }
                        } catch (Exception e) {
                            Log.logErrorRB(e, "TEAM_MISSING_FOLDER", f.getName());
                        };
                    }
                    //load project
                    ProjectFactory.loadProject(props, repository, true);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
                
                RecentProjects.add(localDirectory.getAbsolutePath());
                
                mainWindow.setCursor(oldCursor);
                return null;
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

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                // check if project okay
                ProjectProperties props;
                try {
                    props = ProjectFileStorage.loadProjectProperties(projectRootFolder);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    mainWindow.setCursor(oldCursor);
                    return null;
                }

                final IRemoteRepository repository;
                // check for team-project
                try {
                    if (Core.getParams().containsKey("no-team")) {
                        // disable team functionality
                        repository = null;
                    } else if (SVNRemoteRepository.isSVNDirectory(projectRootFolder)) {
                        // SVN selected
                        repository = new SVNRemoteRepository(projectRootFolder);
                    } else if (GITRemoteRepository.isGITDirectory(projectRootFolder)) {
                        repository = new GITRemoteRepository(projectRootFolder);
                    } else {
                        repository = null;
                    }
                } catch (Exception e) {
                    return null;
                }

                if (repository != null) {
                    boolean onlineMode = true;
                    try {
                        File tmxFile = new File(props.getProjectInternal() + OConsts.STATUS_EXTENSION);
                        File GlossaryFile = new File(props.getWriteableGlossary());
                        if (repository.isChanged(tmxFile) || repository.isChanged(GlossaryFile)) {
                            Log.logWarningRB("TEAM_NOCHECKOUT");
                            Core.getMainWindow().showErrorDialogRB("TEAM_NOCHECKOUT_TITLE", "TEAM_NOCHECKOUT");
                        } else {
                            new RepositoryUtils.AskCredentials() {
                                public void callRepository() throws Exception {
                                    Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE");
                                    repository.updateFullProject();
                                    Core.getMainWindow().showStatusMessageRB(null);
                                }
                            }.execute(repository);
                        }
                    } catch (IRemoteRepository.NetworkException ex) {
                        onlineMode = false;
                        Log.logInfoRB("VCS_OFFLINE");
                        Core.getMainWindow().displayWarningRB("VCS_OFFLINE");
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "TEAM_CHECKOUT_ERROR", ex.getMessage());
                        Core.getMainWindow().displayErrorRB(ex, "TEAM_CHECKOUT_ERROR", ex.getMessage());
                        mainWindow.setCursor(oldCursor);
                        return null;
                    }
                    try {
                        ProjectFactory.loadProject(props, repository, onlineMode);
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        mainWindow.setCursor(oldCursor);
                        return null;
                    }
                } else {
                    try {
                        boolean needToSaveProperties = false;
                        while (!props.isProjectValid()) {
                            needToSaveProperties = true;
                            // something wrong with the project - display open dialog
                            // to fix it
                            ProjectPropertiesDialog prj = new ProjectPropertiesDialog(props, new File(
                                    projectRootFolder, OConsts.FILE_PROJECT).getAbsolutePath(),
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

                        ProjectFactory.loadProject(props, repository, true);
                        if (needToSaveProperties) {
                            Core.getProject().saveProjectProperties();
                        }
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        mainWindow.setCursor(oldCursor);
                        return null;
                    }
                }

				RecentProjects.add(projectRootFolder.getAbsolutePath());

                mainWindow.setCursor(oldCursor);
                return null;
            }
            
            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Core.getEditor().requestFocus();
                        }
                    });
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectReload() {
        performProjectMenuItemPreConditions();

        final ProjectProperties props = Core.getProject().getProjectProperties();

        new SwingWorker<Object, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Object doInBackground() throws Exception {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);
                final IRemoteRepository repository = Core.getProject().getRepository();

                Core.getProject().saveProject();
                ProjectFactory.closeProject();

                if (repository != null) {
                    new RepositoryUtils.AskCredentials() {
                        public void callRepository() throws Exception {
                            Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE");
                            repository.updateFullProject();
                            Core.getMainWindow().showStatusMessageRB(null);
                        }
                    }.execute(repository);
                }

                ProjectFactory.loadProject(props, repository, true);
                mainWindow.setCursor(oldCursor);
                return null;
            }

            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // activate entry later - after project will be
                            // loaded
                            Core.getEditor().gotoEntry(previousCurEntryNum);
                            Core.getEditor().requestFocus();
                        }
                    });
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectSave() {
        performProjectMenuItemPreConditions();

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                mainWindow.showStatusMessageRB("MW_STATUS_SAVING");

                Core.getProject().saveProject();

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
        performProjectMenuItemPreConditions();

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVING");

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                Preferences.save();

                Core.getProject().saveProject();

                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVED");
                mainWindow.setCursor(oldCursor);

                // fix - reset progress bar to defaults
                Core.getMainWindow().showLengthMessage(OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
                Core.getMainWindow().showProgressMessage(
                        Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                                MainWindowUI.STATUS_BAR_MODE.DEFAULT) == MainWindowUI.STATUS_BAR_MODE.DEFAULT
                        ? OStrings.getString("MW_PROGRESS_DEFAULT") : OStrings.getProgressBarDefaultPrecentageText());

                return null;
            }

            protected void done() {
                try {
                    get();
                    ProjectFactory.closeProject();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectEditProperties() {
        performProjectMenuItemPreConditions();

        // displaying the dialog to change paths and other properties
        ProjectPropertiesDialog prj = new ProjectPropertiesDialog(Core.getProject().getProjectProperties(),
                Core.getProject().getProjectProperties().getProjectName(),
                Core.getProject().getRepository() == null ? ProjectPropertiesDialog.Mode.EDIT_PROJECT
                        : ProjectPropertiesDialog.Mode.EDIT_TEAM_PROJECT);
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

        new SwingWorker<Object, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Object doInBackground() throws Exception {
                Core.getProject().saveProject();
                IRemoteRepository repo = Core.getProject().getRepository();
                ProjectFactory.closeProject();

                ProjectFactory.loadProject(newProps, repo, true);
                Core.getProject().saveProjectProperties();
                return null;
            }

            protected void done() {
                try {
                    get();
                    // Make sure to update Editor title
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // activate entry later - after project will be
                            // loaded
                            Core.getEditor().gotoEntry(previousCurEntryNum);
                            Core.getEditor().requestFocus();
                        }
                    });
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectCompile() {
        performProjectMenuItemPreConditions();

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getProject().saveProject(false);
                Core.getProject().compileProject(".*");
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
        performProjectMenuItemPreConditions();

        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                Core.getProject().saveProject(false);
                Core.getProject().compileProject(sourcePattern);
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

    private static void performProjectMenuItemPreConditions() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();
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
