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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.omegat.convert.ConvertProject;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.dialogs.NewTeamProject;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
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
            File projectRoot;
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB(null);

                final NewTeamProject dialog = new NewTeamProject(Core.getMainWindow().getApplicationFrame());
                dialog.setVisible(true);

                if (!dialog.ok) {
                    Core.getMainWindow().showStatusMessageRB("TEAM_CANCELLED");
                    return null;
                }

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);
                Core.getMainWindow().showStatusMessageRB("CT_DOWNLOADING_PROJECT");

                // retrieve omegat.project
                projectRoot = new File(dialog.txtDirectory.getText());
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
                remoteRepositoryProvider.copyFilesFromRepoToProject("omegat.project");

                // update repo into
                ProjectProperties props = ProjectFileStorage.loadProjectProperties(projectRoot);
                props.setRepositories(repos);
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
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Core.getEditor().requestFocus();
                                projectOpen(projectRoot);
                            }
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

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {

                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
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
                    props = ProjectFileStorage.loadProjectProperties(projectRootFolder.getCanonicalFile());
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    mainWindow.setCursor(oldCursor);
                    return null;
                }

                    try {
                        boolean needToSaveProperties = false;
                        if (props.hasRepositories()) {
                            // team project - non-exist directories could be created from repo
                            props.autocreateDirectories();
                        } else {
                            // not a team project - ask for non-exist directories
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
                        }

                        ProjectFactory.loadProject(props, true);
                        if (needToSaveProperties) {
                            Core.getProject().saveProjectProperties();
                        }
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        mainWindow.setCursor(oldCursor);
                        return null;
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

                Core.getProject().saveProject();
                ProjectFactory.closeProject();

                ProjectFactory.loadProject(props, true);
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

        new SwingWorker<Object, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Object doInBackground() throws Exception {
                Core.getProject().saveProject();
                ProjectFactory.closeProject();

                ProjectFactory.loadProject(newProps, true);
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
                Core.getProject().saveProject(true);
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
