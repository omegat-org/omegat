/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2010 Alex Buloichik
               2011 Martin Fleurke
               2012 Thomas Cordonnier
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingworker.SwingWorker;
import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team.GITRemoteRepository;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.RepositoryUtils;
import org.omegat.core.team.SVNRemoteRepository;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.dialogs.NewTeamProject;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DockingUI;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.OpenProjectFileChooser;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Handler for project UI commands, like open, save, compile, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Thomas Cordonnier
 */
public class ProjectUICommands {
    public static void projectCreate() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                // ask for new project dir
                NewProjectFileChooser ndc = new NewProjectFileChooser();
                int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
                if (ndcResult != OmegaTFileChooser.APPROVE_OPTION) {
                    // user press 'Cancel' in project creation dialog
                    return null;
                }
                File dir = ndc.getSelectedFile();
                dir.mkdirs();

                // ask about new project properties
                ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(
                        new ProjectProperties(dir), dir.getAbsolutePath(),
                        ProjectPropertiesDialog.NEW_PROJECT);
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

                if (projectRoot != null && projectRoot.length() > 0) {
                    // create project
                    try {
                        ProjectFactory.createProject(newProps);
                        Core.getProject().saveProjectProperties();
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

    public static void projectTeamCreate() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }
        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB(null);

                final NewTeamProject dialog = displayTeamDialog();

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
                    if (dialog.rbSVN.isSelected()) {
                        // SVN selected
                        repository = new SVNRemoteRepository(localDirectory);
                    } else if (dialog.rbGIT.isSelected()) {
                        // GIT selected
                        repository = new GITRemoteRepository(localDirectory);
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
                    Object[] args = { bre.getMessage() };
                    Core.getMainWindow().showErrorDialogRB("TEAM_BADREPOSITORY_ERROR", args, "TF_ERROR");
                    mainWindow.setCursor(oldCursor);
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Core.getMainWindow().displayErrorRB(ex, "TEAM_CHECKOUT_ERROR");
                    mainWindow.setCursor(oldCursor);
                    return null;
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
                            Log.logErrorRB(e, "TEAM_MISSING_FOLDER", new Object[] {f.getName()});
                        };
                    }
                    //load project
                    ProjectFactory.loadProject(props, repository, true);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
                mainWindow.setCursor(oldCursor);
                return null;
            }
        }.execute();
    }

    public static NewTeamProject displayTeamDialog() {
        final NewTeamProject dialog = new NewTeamProject(Core.getMainWindow().getApplicationFrame(), true);
        DockingUI.displayCentered(dialog);
        dialog.btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.rbGIT.setSelected(false);
                dialog.rbSVN.setSelected(false);
                dialog.dispose();
            }
        });
        dialog.btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                dialog.ok = true;
            }
        });
        dialog.btnDirectory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NewProjectFileChooser ndc = new NewProjectFileChooser();
                int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
                if (ndcResult == OmegaTFileChooser.APPROVE_OPTION) {
                    dialog.txtDirectory.setText(ndc.getSelectedFile().getPath());
                }
            }
        });

        DocumentListener newTeamProjectOkHider = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkNewTeamProjectDialog(dialog);
            }

            public void removeUpdate(DocumentEvent e) {
                checkNewTeamProjectDialog(dialog);
            }

            public void insertUpdate(DocumentEvent e) {
                checkNewTeamProjectDialog(dialog);
            }
        };

        dialog.txtDirectory.getDocument().addDocumentListener(newTeamProjectOkHider);
        dialog.txtRepositoryURL.getDocument().addDocumentListener(newTeamProjectOkHider);
        dialog.rbGIT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkNewTeamProjectDialog(dialog);
            }
        });
        dialog.rbSVN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkNewTeamProjectDialog(dialog);
            }
        });

        dialog.setVisible(true);
        return dialog;
    }

    public static void checkNewTeamProjectDialog(NewTeamProject dialog) {
        boolean enabled = dialog.rbGIT.isSelected() || dialog.rbSVN.isSelected();
        enabled &= !StringUtil.isEmpty(dialog.txtRepositoryURL.getText());
        enabled &= !StringUtil.isEmpty(dialog.txtDirectory.getText());
        dialog.btnOk.setEnabled(enabled);
    }

    /**
     * Open project.
     * 
     * @param projectDirectory
     *            project directory or null if user must choose it
     */
    public static void projectOpen(final File projectDirectory) {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                final File projectRootFolder;
                if (projectDirectory == null) {
                    // select existing project file - open it
                    OmegaTFileChooser pfc = new OpenProjectFileChooser();
                    if (OmegaTFileChooser.APPROVE_OPTION != pfc.showOpenDialog(Core.getMainWindow()
                            .getApplicationFrame())) {
                        return null;
                    }
                    projectRootFolder = pfc.getSelectedFile();
                } else {
                    projectRootFolder = projectDirectory;
                }

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
                if (SVNRemoteRepository.isSVNDirectory(projectRootFolder)) {
                    // SVN selected
                    repository = new SVNRemoteRepository(projectRootFolder);
                } else if (GITRemoteRepository.isGITDirectory(projectRootFolder)) {
                    repository = new GITRemoteRepository(projectRootFolder);
                } else {
                    repository = null;
                }

                if (repository != null) {
                    boolean onlineMode = true;
                    try {
                        File tmxFile = new File(props.getProjectInternal() + OConsts.STATUS_EXTENSION);
                        File GlossaryFile = new File(props.getWriteableGlossary());
                        if (repository.isChanged(tmxFile) || repository.isChanged(GlossaryFile)) {
                            Log.logWarningRB("TEAM_NOCHECKOUT");
                            Core.getMainWindow().showErrorDialogRB("TEAM_NOCHECKOUT", null,
                                    "TEAM_NOCHECKOUT_TITLE");
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
                                    ProjectPropertiesDialog.RESOLVE_DIRS);
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
                IRemoteRepository repository = Core.getProject().getRepository();

                Core.getProject().saveProject();
                ProjectFactory.closeProject();

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
                ProjectPropertiesDialog.EDIT_PROJECT);
        prj.setVisible(true);
        final ProjectProperties newProps = prj.getResult();
        prj.dispose();
        if (newProps == null) {
            return;
        }
        FilterMaster.saveConfig(newProps.getProjectFilters(), newProps.getProjectInternal());

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

                ProjectFactory.loadProject(newProps, null, true);
                Core.getProject().saveProjectProperties();
                return null;
            }

            protected void done() {
                try {
                    get();
                    Core.getEditor().gotoEntry(previousCurEntryNum);
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
                Core.getProject().saveProject();
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
