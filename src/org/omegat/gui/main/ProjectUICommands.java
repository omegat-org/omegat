/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.main;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.OpenProjectFileChooser;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Handler for project UI commands, like open, save, compile, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
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
        File dir = ndc.getSelectedFile();
        dir.mkdirs();

        // ask about new project properties
        ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(new ProjectProperties(dir),
                dir.getAbsolutePath(), ProjectPropertiesDialog.NEW_PROJECT);
        newProjDialog.setVisible(true);
        newProjDialog.dispose();

        final ProjectProperties newProps = newProjDialog.getResult();
        if (newProps == null) {
            // user clicks on 'Cancel'
            dir.delete();
            return;
        }

        final String projectRoot = newProps.getProjectRoot();

        if (projectRoot != null && projectRoot.length() > 0) {
            new SwingWorker<Object, Void>() {
                protected Object doInBackground() throws Exception {
                    ProjectFactory.createProject(newProps);
                    Core.getProject().saveProjectProperties();
                    return null;
                }

                protected void done() {
                    try {
                        get();
                    } catch (Exception ex) {
                        Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                        Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    }
                }
            }.execute();
        }
    }

    /**
     * Open project.
     */
    public static void projectOpen() {
        UIThreadsUtil.mustBeSwingThread();

        if (Core.getProject().isProjectLoaded()) {
            return;
        }

        // select existing project file - open it
        OmegaTFileChooser pfc = new OpenProjectFileChooser();
        if (OmegaTFileChooser.APPROVE_OPTION != pfc
                .showOpenDialog(Core.getMainWindow().getApplicationFrame())) {
            return;
        }

        final File projectRootFolder = pfc.getSelectedFile();

        // check if project okay
        ProjectProperties props;
        try {
            props = ProjectFileStorage.loadProjectProperties(projectRootFolder);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            return;
        }

        boolean needToSaveProperties = false;
        while (!props.verifyProject()) {
            needToSaveProperties = true;
            // something wrong with the project - display open dialog
            // to fix it
            ProjectPropertiesDialog prj = new ProjectPropertiesDialog(props, new File(projectRootFolder,
                    OConsts.FILE_PROJECT).getAbsolutePath(), ProjectPropertiesDialog.RESOLVE_DIRS);
            prj.setVisible(true);
            props = prj.getResult();
            prj.dispose();
            if (props == null) {
                // user clicks on 'Cancel'
                return;
            }
        }

        final boolean saveProperties = needToSaveProperties;
        final ProjectProperties newProps = props;
        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                // TODO: check loading if need to show dialog
                ProjectFactory.loadProject(newProps);
                if (saveProperties) {
                    Core.getProject().saveProjectProperties();
                }
                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectReload() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        Core.getEditor().commitAndDeactivate();

        final ProjectProperties props = Core.getProject().getProjectProperties();

        new SwingWorker<Object, Void>() {
            int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

            protected Object doInBackground() throws Exception {
                Core.getProject().saveProject();
                ProjectFactory.closeProject();

                ProjectFactory.loadProject(props);
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
                        }
                    });
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
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

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVING");

                Core.getProject().saveProject();

                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVED");

                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectClose() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        Core.getEditor().commitAndLeave();

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVING");

                Preferences.save();

                Core.getProject().saveProject();

                Core.getMainWindow().showStatusMessageRB("MW_STATUS_SAVED");

                return null;
            }

            protected void done() {
                try {
                    get();
                    ProjectFactory.closeProject();
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public static void projectEditProperties() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        Core.getEditor().commitAndLeave();

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

                ProjectFactory.loadProject(newProps);
                Core.getProject().saveProjectProperties();
                return null;
            }

            protected void done() {
                try {
                    get();
                    Core.getEditor().gotoEntry(previousCurEntryNum);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
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
                    Log.logErrorRB(ex, "TF_COMPILE_ERROR");
                    Core.getMainWindow().displayErrorRB(ex, "TF_COMPILE_ERROR");
                }
            }
        }.execute();
    }
}
