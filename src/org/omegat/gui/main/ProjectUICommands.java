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
               2021 ISHIKAWA,chiaki
               2022 Hiroshi Miura
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

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
import org.omegat.gui.dialogs.NewTeamProjectController;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.FileUtil;
import org.omegat.util.FileUtil.ICollisionCallback;
import org.omegat.util.HttpConnectionUtils;
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
 * @author ISHIKAWA, chiaki
 * @author Hiroshi Miura
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
                props.setSourceLanguage(Preferences.getPreferenceDefault(Preferences.SOURCE_LOCALE, "AR-LB"));
                props.setTargetLanguage(Preferences.getPreferenceDefault(Preferences.TARGET_LOCALE, "UK-UA"));
                ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(
                        Core.getMainWindow().getApplicationFrame(), props, dir.getAbsolutePath(),
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
        } catch (Exception ignored) {
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
                ? ndm.getSelectedFile()
                : new File(ndm.getSelectedFile().getAbsolutePath() + ".zip");

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
            IMainWindow mainWindow;
            Cursor oldCursor;

            protected Void doInBackground() throws Exception {
                mainWindow = Core.getMainWindow();
                mainWindow.showStatusMessageRB(null);
                NewTeamProjectController newTeamProjectController = new NewTeamProjectController(mainWindow);
                File dir = newTeamProjectController.show();
                if (dir == null || !ensureProjectDir(dir)) {
                    return null;
                }

                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);
                Core.getMainWindow().showStatusMessageRB("CT_DOWNLOADING_PROJECT");

                // retrieve omegat.project
                projectRoot = dir;
                List<RepositoryDefinition> repos = new ArrayList<>();
                RepositoryDefinition repo = newTeamProjectController.getRepo();
                repos.add(repo);
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
                    remoteRepositoryProvider.copyFilesFromReposToProject(file);
                }

                ProjectProperties props = ProjectFileStorage.loadProjectProperties(projectRoot);
                if (props.getRepositories() == null) {
                    // We assume it's a project with no repository mapping,
                    // so we add root repository mapping
                    props.setRepositories(repos);
                } else {
                    RepositoryDefinition remoteRepo = getRootGitRepositoryMapping(props.getRepositories());
                    if (isRepositoryEquals(remoteRepo, repo)) {
                        // when remote repository config is different with
                        // opening url, respect local one
                        setRootGitRepositoryMapping(props.getRepositories(), repo);
                    }
                }
                // We write in all cases, because we might have added default
                // excludes, for instance
                ProjectFileStorage.writeProjectFile(props);
                mainWindow.setCursor(oldCursor);
                oldCursor = null;
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
                    if (oldCursor != null)
                        mainWindow.setCursor(oldCursor);
                }
            }
        }.execute();
    }

    /**
     * Open project. Does nothing if there is already a project open.
     * Convenience method for {@link #projectOpen(File, boolean)}.
     *
     * @param projectDirectory
     *            Open project stored in projectDirectory
     */
    public static void projectOpen(File projectDirectory) {
        projectOpen(projectDirectory, false);
    }

    /**
     * Open project. Does nothing if a project is already open and closeCurrent
     * is false.
     *
     * @param projectDirectory
     *            project directory or null if user must choose it
     * @param closeCurrent
     *            whether to close the current project first, if any
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

        final File projectRootFolder = selectProjectRootFolder(projectDirectory);
        if (projectRootFolder == null) {
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                IMainWindow mainWindow = Core.getMainWindow();
                Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                Cursor oldCursor = mainWindow.getCursor();
                mainWindow.setCursor(hourglassCursor);

                if (convertOldProjectIfNeed(projectRootFolder)) {
                    projectOpenImpl(projectRootFolder);
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

    private static File selectProjectRootFolder(File projectDirectory) {
        File projectRootFolder;
        if (projectDirectory == null) {
            // select existing project file - open it
            OmegaTFileChooser pfc = new OpenProjectFileChooser();
            if (OmegaTFileChooser.APPROVE_OPTION != pfc
                    .showOpenDialog(Core.getMainWindow().getApplicationFrame())) {
                return null;
            }
            projectRootFolder = pfc.getSelectedFile();
        } else {
            projectRootFolder = projectDirectory;
        }
        return projectRootFolder;
    }

    private static boolean convertOldProjectIfNeed(File projectRootFolder) {
        try {
            // convert old projects if needed
            ConvertProject.convert(projectRootFolder);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_CONVERT_PROJECT");
            Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_CONVERT_PROJECT");
            return false;
        }
        return true;
    }

    private static ProjectProperties checkProjectProperties(File projectRootFolder) {
        // check if project okay
        ProjectProperties props;
        try {
            props = ProjectFileStorage.loadProjectProperties(projectRootFolder.getAbsoluteFile());
            // Here, 'props' is the current project setting read from local copy
            // of omegat.project
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE_BACK");
            Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE_BACK");
            return null;
        }
        return props;
    }

    private static void projectOpenImpl(File projectRootFolder) {
        IMainWindow mainWindow = Core.getMainWindow();
        try {
            // open LOCAL copy of "omegat.project"
            ProjectProperties props = checkProjectProperties(projectRootFolder);
            // When failed to load, we try with backup file
            if (props == null) {
                File backupProjectFile = FileUtil
                        .getRecentBackup(new File(projectRootFolder.getAbsoluteFile(), OConsts.FILE_PROJECT));
                if (backupProjectFile == null) {
                    throw new KnownException("PROJECT_INVALID");
                }
                props = ProjectFileStorage.loadPropertiesFile(projectRootFolder.getAbsoluteFile(),
                        backupProjectFile);
            }
            boolean needToSaveProperties = false;
            // newProjectFile represent a `omegat.project.NEW` file.
            // It is created when modification of
            // properties in remote.
            File newProjectFile = null;
            if (props.isTeamProject()) {
                /*
                 * <p> Every time we reopen the project, we copy omegat.project
                 * from the remote project, We take following strategy and
                 * procedure to open the project.
                 *
                 * 1. When opening a teamwork project as local only non-teamwork
                 * by passing 'no-team' to command line, skip teamwork
                 * treatment.
                 *
                 * 2. Save the currently effective repository mapping from LOCAL
                 * to variable 'repos'.
                 *
                 * 3. Update project.properties from REMOTE copy of
                 * omegat.project that has postfix .NEW by calling
                 * loadPropertiesFile(... ) with "omegat.project.NEW". It
                 * respects a local root repository URL than remote mapping
                 * configuration
                 *
                 * 4. Handles mappings of four cases.
                 *
                 * a. no mapping
                 *
                 * b. no remote mapping, there are local mapping(s) the locally
                 * defined mapping(s) are merged into local omegat.project.
                 *
                 * c. remote mapping, no local mapping(s)
                 *
                 * d. remote and local mappings Local mapping changes are
                 * overwritten except for root repository mapping.
                 *
                 * 5. We save the original project file with as
                 * omegat.project.timestamp.bak
                 *
                 * @note: We may want to make sure that the remote
                 * props.GetRepositories match the previous current setup, but
                 * this does not seem to be the intention of the current mapping
                 * usage.
                 */
                if (!Core.getParams().containsKey(CLIParameters.NO_TEAM)) {
                    ProjectProperties localProps = props;
                    List<RepositoryDefinition> localRepos = props.getRepositories();
                    mainWindow.showStatusMessageRB("TEAM_OPEN");
                    try {
                        RemoteRepositoryProvider remoteRepositoryProvider = new RemoteRepositoryProvider(
                                props.getProjectRootDir(), props.getRepositories(), props);
                        remoteRepositoryProvider.switchToVersion(OConsts.FILE_PROJECT, null);
                        remoteRepositoryProvider.copyFilesFromReposToProject(OConsts.FILE_PROJECT, ".NEW",
                                false);
                        newProjectFile = new File(projectRootFolder.getAbsoluteFile(),
                                OConsts.FILE_PROJECT + ".NEW");
                        props = ProjectFileStorage.loadPropertiesFile(projectRootFolder.getAbsoluteFile(),
                                new File(projectRootFolder.getAbsoluteFile(), OConsts.FILE_PROJECT + ".NEW"));
                        // Here, 'props' is the REMOTE project setting read from
                        // the remote omegat.project
                        if (props.getRepositories() == null) {
                            // We have a project without mapping
                            // So we restore the mapping we just lost
                            Log.logInfoRB("TF_REMOTE_PROJECT_LACKS_GIT_SETTING");
                            props.setRepositories(localRepos);
                        } else {
                            // use mapping from remote configuration but
                            // override repository URL when project URL is git
                            // type when there is difference between local and
                            // remote config.
                            RepositoryDefinition localRootRepository = getRootGitRepositoryMapping(
                                    localRepos);
                            RepositoryDefinition newRepository = getRootGitRepositoryMapping(
                                    props.getRepositories());
                            if (!isRepositoryEquals(localRootRepository, newRepository)) {
                                setRootGitRepositoryMapping(props.getRepositories(), localRootRepository);
                            }
                        }
                        needToSaveProperties = !isIdenticalOmegatProjectProperties(props, localProps);
                    } catch (IRemoteRepository2.NetworkException ignore) {
                        // Do nothing. Network errors are handled in
                        // RealProject.
                    } catch (Exception e) {
                        Log.logErrorRB(e, "TF_PROJECT_PROPERTIES_ERROR");
                        throw e;
                    }
                }
                // non-exist directories could be created
                props.autocreateDirectories();
            } else {
                // not a team project
                File projectFile = new File(projectRootFolder, OConsts.FILE_PROJECT);
                props.autocreateDirectories();
                while (!props.isProjectValid()) {
                    // something wrong with the project.
                    // We display open dialog to fix it.
                    ProjectPropertiesDialog prj = new ProjectPropertiesDialog(
                            Core.getMainWindow().getApplicationFrame(), props, projectFile.getAbsolutePath(),
                            ProjectPropertiesDialog.Mode.RESOLVE_DIRS);
                    prj.setVisible(true);
                    props = prj.getResult();
                    prj.dispose();
                    if (props == null) {
                        // user clicks on 'Cancel'
                        return;
                    }
                    needToSaveProperties = true;
                }
            }
            // Critical section, create backup and save
            // properties.
            final ProjectProperties propsP = props;
            final boolean finalNeedToSaveProperties = needToSaveProperties;
            final File finalNewProjectFile = newProjectFile;
            final boolean onlineMode = true;
            Core.executeExclusively(true, () -> {
                // loading modified new project property
                boolean succeeded = ProjectFactory.loadProject(propsP, onlineMode);
                if (!succeeded) {
                    return;
                }
                File projectFile = new File(projectRootFolder, OConsts.FILE_PROJECT);
                // make backup and save omegat.project file when required
                if (finalNeedToSaveProperties) {
                    File backup = FileUtil.backupFile(projectFile);
                    FileUtil.removeOldBackups(projectFile, OConsts.MAX_BACKUPS);
                    Log.logWarningRB("PP_REMOTE_PROJECT_CONTENT_OVERRIDES_THE_CURRENT_PROJECT",
                            backup.getName());
                    Core.getProject().saveProjectProperties();
                } else if (FileUtil.getRecentBackup(projectFile) == null) {
                    File backup = new File(projectRootFolder, FileUtil.getBackupFilename(projectFile));
                    ProjectFileStorage.writeProjectFile(backup, propsP);
                } else if (finalNewProjectFile != null) {
                    FileUtils.deleteQuietly(finalNewProjectFile);
                }
            });
            RecentProjects.add(projectRootFolder.getAbsolutePath());
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
        }
    }

    /**
     * Detect whether local `omegat.project` is identical with remote one.
     * 
     * @param that
     *            remote omegat.project.
     * @param my
     *            local omegat.project.
     * @return true if identical, otherwise false.
     */
    private static boolean isIdenticalOmegatProjectProperties(ProjectProperties that, ProjectProperties my) {
        if (my == that) {
            return true;
        }
        if (that == null || my == null) {
            return false;
        }
        for (int i = 0; i < my.getSourceRootExcludes().size(); i++) {
            if (!my.getSourceRootExcludes().get(i).equals(that.getSourceRootExcludes().get(i))) {
                return false;
            }
        }
        if (my.getRepositories().size() != that.getRepositories().size()) {
            return false;
        }
        for (int i = 0; i < my.getRepositories().size(); i++) {
            if (!new EqualsBuilder()
                    .append(my.getRepositories().get(i).getType(), that.getRepositories().get(i).getType())
                    .append(my.getRepositories().get(i).getUrl(), that.getRepositories().get(i).getUrl())
                    .append(my.getRepositories().get(i).getBranch(),
                            that.getRepositories().get(i).getBranch())
                    .append(my.getRepositories().get(i).getMapping().size(),
                            that.getRepositories().get(i).getMapping().size())
                    .isEquals()) {
                return false;
            }
            if (my.getRepositories().get(i).getMapping().size() != that.getRepositories().get(i).getMapping()
                    .size()) {
                return false;
            }
            for (int j = 0; j < my.getRepositories().get(i).getMapping().size(); j++) {
                RepositoryMapping thisMap = my.getRepositories().get(i).getMapping().get(j);
                RepositoryMapping thatMap = that.getRepositories().get(i).getMapping().get(j);
                if (!new EqualsBuilder().append(thisMap.getLocal(), thatMap.getLocal())
                        .append(thisMap.getRepository(), thatMap.getRepository()).isEquals()) {
                    return false;
                }
            }
        }
        return new EqualsBuilder()
                .append(my.isSentenceSegmentingEnabled(), that.isSentenceSegmentingEnabled())
                .append(my.isSupportDefaultTranslations(), that.isSupportDefaultTranslations())
                .append(my.isRemoveTags(), that.isRemoveTags())
                .append(my.getProjectName(), that.getProjectName())
                .append(my.getSourceLanguage().getLocaleCode(), that.getSourceLanguage().getLocaleCode())
                .append(my.getTargetLanguage().getLocaleCode(), that.getTargetLanguage().getLocaleCode())
                .append(my.getSourceTokenizer().getCanonicalName(),
                        that.getSourceTokenizer().getCanonicalName())
                .append(my.getTargetTokenizer().getCanonicalName(),
                        that.getTargetTokenizer().getCanonicalName())
                .append(my.getExportTmLevels(), that.getExportTmLevels())
                .append(my.getExternalCommand(), that.getExternalCommand())
                .append(my.getProjectRootDir(), that.getProjectRootDir())
                .append(my.getSourceDir().getUnderRoot(), that.getSourceDir().getUnderRoot())
                .append(my.getTargetDir().getUnderRoot(), that.getTargetDir().getUnderRoot())
                .append(my.getGlossaryDir().getUnderRoot(), that.getGlossaryDir().getUnderRoot())
                .append(my.getWritableGlossaryFile().getUnderRoot(),
                        that.getWritableGlossaryFile().getUnderRoot())
                .append(my.getTmDir().getUnderRoot(), that.getTmDir().getUnderRoot())
                .append(my.getExportTMRoot(), that.getExportTMRoot())
                .append(my.getDictRoot(), that.getDictRoot()).isEquals();
    }

    private static RepositoryDefinition getRootGitRepositoryMapping(List<RepositoryDefinition> repos) {
        RepositoryDefinition repositoryDefinition = null;
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/")
                    && definition.getMapping().get(0).getRepository().equals("/")
                    && definition.getType().equals("git")) {
                repositoryDefinition = definition;
                break;
            }
        }
        return repositoryDefinition;
    }

    private static void setRootGitRepositoryMapping(List<RepositoryDefinition> repos,
            RepositoryDefinition repositoryDefinition) {
        if (repositoryDefinition == null) {
            return;
        }
        RepositoryDefinition originalRepositoryDefinition = getRootGitRepositoryMapping(repos);
        if (originalRepositoryDefinition == null) {
            return;
        }
        originalRepositoryDefinition.setType(repositoryDefinition.getType());
        originalRepositoryDefinition.setUrl(repositoryDefinition.getUrl());
        originalRepositoryDefinition.setBranch(repositoryDefinition.getBranch());
    }

    private static boolean isRepositoryEquals(RepositoryDefinition a, RepositoryDefinition b) {
        if (a == null || b == null) {
            return false;
        }
        return new EqualsBuilder().append(a.getType(), b.getType()).append(a.getUrl(), b.getUrl())
                .append(a.getBranch(), b.getBranch()).isEquals();
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

    /**
     * Project reload.
     * <p>
     * When select Project&gt;Reload jump to here.
     */
    public static void projectReload() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        // commit the current entry first
        Core.getEditor().commitAndLeave();

        if (Core.getProject().getProjectProperties().isTeamProject()) {
            projectReloadRemote();
        } else {
            projectReloadLocal();
        }
    }

    /**
     * Reload project from remote repository.
     * <p>
     * When the project is a team project, it reloads `omegat.project` from
     * remote project and open the project from start. When the project is local
     * project, it acts as same as just reload.
     */
    private static void projectReloadRemote() {
        ProjectProperties props = Core.getProject().getProjectProperties();
        final File projectDirectory = props.getProjectRootDir();
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

    /**
     * Reload local project files.
     * <p>
     * This does not reload remote project when team mode. It is useful when
     * user added source files in local.
     *
     */
    private static void projectReloadLocal() {
        final ProjectProperties props = Core.getProject().getProjectProperties();

        new SwingWorker<Void, Void>() {
            final int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

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

    /**
     * Save project.
     * <p>
     * When the project is a team project, it also commit files and push to
     * remote.
     */
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

    /**
     * Close project.
     * <p>
     * When the project is a team project, it commits and push first.
     */
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
                                        ? OStrings.getString("MW_PROGRESS_DEFAULT")
                                        : OStrings.getProgressBarDefaultPrecentageText());

                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    processSwingWorkerException(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
                // Restore global prefs in case project had project-specific
                // ones
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
            final int previousCurEntryNum = Core.getEditor().getCurrentEntryNumber();

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

    /**
     * Open remote project specified by url.
     * 
     * @param url
     *            remote project repository.
     */
    public static void projectRemote(String url) {
        File projectDir;
        try {
            projectDir = projectRemoteOpen(url);
        } catch (IOException ex) {
            Log.logErrorRB(ex, "TEAM_REMOTE_RETRIEVE_ERROR", url);
            Core.getMainWindow().displayErrorRB(ex, "TEAM_REMOTE_RETRIEVE_ERROR", url);
            return;
        }
        projectOpen(projectDir);
    }

    private static File projectRemoteOpen(String url) throws IOException {
        String dir = url.replace("/", "_").replace(':', '_');
        File projectDir = new File(StaticUtils.getConfigDir() + "/remoteProjects/" + dir);
        File projectFile = new File(projectDir, OConsts.FILE_PROJECT);
        boolean res = projectDir.mkdirs();
        if (!res) {
            throw new IOException("Failed to create project directory.");
        }
        byte[] data = HttpConnectionUtils.getURLasByteArray(url);
        if (data == null) {
            throw new IOException("Data retrieval error");
        }
        FileUtils.writeByteArrayToFile(projectFile, data);
        return projectDir;
    }

    /**
     * Copy the specified files to the specified destination. The project will
     * be reloaded afterward.
     * <p>
     * Convenience method for
     * {@link #projectImportFiles(String, File[], boolean)}.
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
     * Copy the specified files to the specified destination, then reload if
     * indicated. Note that a modal dialog will be shown if any of the specified
     * files would be overwritten.
     *
     * @param destination
     *            The path to copy the files to
     * @param toImport
     *            Files to copy to destination path
     * @param doReload
     *            If true, the project will be reloaded after the files are
     *            successfully copied
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
    }

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
                OStrings.getString("TF_WIKI_IMPORT_PROMPT"), OStrings.getString("TF_WIKI_IMPORT_TITLE"),
                JOptionPane.WARNING_MESSAGE);
        String projectsource = Core.getProject().getProjectProperties().getSourceRoot();
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            // [1762625] Only try to get MediaWiki page if a string has been
            // entered
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
        // Use NIO methods because File.canRead/canWrite give incorrect
        // responses on Windows
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
