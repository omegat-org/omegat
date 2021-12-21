/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2016 Alex Buloichik
               2009-2010 Didier Briel
               2012 Guido Leenders, Didier Briel, Martin Fleurke
               2013 Aaron Madlon-Kay, Didier Briel
               2014 Aaron Madlon-Kay, Didier Briel
               2015 Aaron Madlon-Kay
               2017-2018 Didier Briel
               2019 Thomas Cordonnier
               2020 Briac Pilpre
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

package org.omegat.core.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.madlonkay.supertmxmerge.StmProperties;
import org.madlonkay.supertmxmerge.SuperTmxMerge;
import org.omegat.CLIParameters;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.TMXEntry.ExternalLinked;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.core.statistics.StatsResult;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.RebaseAndCommit;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.threads.CommandMonitor;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryReaderTSV;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StreamUtil;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;
import org.omegat.util.TagUtil;
import org.omegat.util.gui.UIThreadsUtil;
import org.xml.sax.SAXParseException;

import gen.core.filters.Filters;

/**
 * Loaded project implementation. Only translation could be changed after project will be loaded and set by
 * Core.setProject.
 *
 * All components can read all data directly without synchronization. All synchronization implemented inside
 * RealProject.
 *
 * Since team sync is long operation, auto-saving was split into 3 phrases: get remote data in background, then rebase
 * during segment deactivation, then commit in background.
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Bartko Zoltan (bartkozoltan@bartkozoltan.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Guido Leenders
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class RealProject implements IProject {
    private static final Logger LOGGER = Logger.getLogger(RealProject.class.getName());

    protected final ProjectProperties config;
    protected final RemoteRepositoryProvider remoteRepositoryProvider;

    enum PreparedStatus {
        NONE, PREPARED, PREPARED2, REBASED
    }

    /**
     * Status required for execute prepare/rebase/commit in the correct order.
     */
    private volatile PreparedStatus preparedStatus = PreparedStatus.NONE;
    private volatile RebaseAndCommit.Prepared tmxPrepared;
    private volatile RebaseAndCommit.Prepared glossaryPrepared;

    private boolean isOnlineMode;

    private RandomAccessFile raFile;
    private FileChannel lockChannel;
    private FileLock lock;

    private boolean modified;

    /** List of all segments in project. */
    protected List<SourceTextEntry> allProjectEntries = new ArrayList<>(4096);

    protected ImportFromAutoTMX importHandler;

    private final StatisticsInfo hotStat = new StatisticsInfo();

    private final ITokenizer sourceTokenizer, targetTokenizer;

    private DirectoryMonitor tmMonitor;

    private DirectoryMonitor tmOtherLanguagesMonitor;

    /**
     * Indicates when there is an ongoing save event. Saving might take a while during
     * team sync: if a merge is required the save might be postponed indefinitely while we
     * wait for the user to confirm the current segment.
     */
    private boolean isSaving = false;

    /**
     * Storage for all translation memories, which shouldn't be changed and saved, i.e. for /tm/*.tmx files,
     * aligned data from source files.
     *
     * This map recreated each time when files changed. So, you can free use it without thinking about
     * synchronization.
     */
    private Map<String, ExternalTMX> transMemories = new TreeMap<>();

    /**
     * Storage for all translation memories of translations to other languages.
     */
    private Map<Language, ProjectTMX> otherTargetLangTMs = new TreeMap<>();

    protected ProjectTMX projectTMX;

    /**
     * True if project loaded successfully.
     */
    private boolean loaded = false;

    // Sets of exist entries for check orphaned
    private Set<String> existSource = new HashSet<>();
    private Set<EntryKey> existKeys = new HashSet<>();

    /** Segments count in project files. */
    protected List<FileInfo> projectFilesList = new ArrayList<>();

    /** This instance returned if translation not exist. */
    private static final TMXEntry EMPTY_TRANSLATION;
    static {
        PrepareTMXEntry empty = new PrepareTMXEntry();
        empty.source = "";
        EMPTY_TRANSLATION = new TMXEntry(empty, true, null);
    }

    private final boolean allowTranslationEqualToSource = Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC);

    /**
     * A list of external processes. Allows previously-started, hung or long-running processes to be
     * forcibly terminated when compiling the project anew or when closing the project.
     */
    private final Stack<Process> processCache = new Stack<>();

    /**
     * Create new project instance. It required to call {@link #createProject()}
     * or {@link #loadProject(boolean)} methods just after constructor before
     * use project.
     *
     * @param props
     *            project properties
     */
    public RealProject(final ProjectProperties props) {
        config = props;
        if (config.getRepositories() != null && !Core.getParams().containsKey(CLIParameters.NO_TEAM)) {
            try {
                remoteRepositoryProvider = new RemoteRepositoryProvider(config.getProjectRootDir(), config.getRepositories(), config);
            } catch (Exception ex) {
                // TODO
                throw new RuntimeException(ex);
            }
        } else {
            remoteRepositoryProvider = null;
        }

        sourceTokenizer = createTokenizer(Core.getParams().get(CLIParameters.TOKENIZER_SOURCE),
                props.getSourceTokenizer());
        Log.log("Source tokenizer: " + sourceTokenizer.getClass().getName());
        targetTokenizer = createTokenizer(Core.getParams().get(CLIParameters.TOKENIZER_TARGET),
                props.getTargetTokenizer());
        Log.log("Target tokenizer: " + targetTokenizer.getClass().getName());
    }

    public void saveProjectProperties() throws Exception {
        unlockProject();
        try {
            SRX.saveTo(config.getProjectSRX(), new File(config.getProjectInternal(), SRX.CONF_SENTSEG));
            FilterMaster.saveConfig(config.getProjectFilters(),
                    new File(config.getProjectInternal(), FilterMaster.FILE_FILTERS));
            ProjectFileStorage.writeProjectFile(config);
        } finally {
            lockProject();
        }
        Preferences.setPreference(Preferences.SOURCE_LOCALE, config.getSourceLanguage().toString());
        Preferences.setPreference(Preferences.TARGET_LOCALE, config.getTargetLanguage().toString());
    }

    /**
     * Create new project.
     */
    public void createProject() {
        Log.logInfoRB("LOG_DATAENGINE_CREATE_START");
        UIThreadsUtil.mustNotBeSwingThread();

        try {
            if (!lockProject()) {
                throw new KnownException("PROJECT_LOCKED");
            }

            createDirectory(config.getProjectRoot(), null);
            createDirectory(config.getProjectInternal(), OConsts.DEFAULT_INTERNAL);
            createDirectory(config.getSourceRoot(), OConsts.DEFAULT_SOURCE);
            createDirectory(config.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY);
            createDirectory(config.getTMRoot(), OConsts.DEFAULT_TM);
            createDirectory(config.getTMAutoRoot(), OConsts.AUTO_TM);
            createDirectory(config.getDictRoot(), OConsts.DEFAULT_DICT);
            createDirectory(config.getTargetRoot(), OConsts.DEFAULT_TARGET);
            //createDirectory(m_config.getTMOtherLangRoot(), OConsts.DEFAULT_OTHERLANG);

            saveProjectProperties();

            // Set project specific segmentation rules if they exist, or
            // defaults otherwise.
            SRX srx = config.getProjectSRX();
            Core.setSegmenter(new Segmenter(srx == null ? Preferences.getSRX() : srx));

            loadTranslations();
            setProjectModified(true);
            saveProject(false);

            loadSourceFiles();

            allProjectEntries = Collections.unmodifiableList(allProjectEntries);
            importHandler = new ImportFromAutoTMX(this, allProjectEntries);

            importTranslationsFromSources();

            loadTM();

            loadOtherLanguages();

            loaded = true;

            // clear status message
            Core.getMainWindow().showStatusMessageRB(null);
        } catch (Exception e) {
            // trouble in Tinseltown...
            Log.logErrorRB(e, "CT_ERROR_CREATING_PROJECT");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_CREATING_PROJECT");
        }
        Log.logInfoRB("LOG_DATAENGINE_CREATE_END");
    }

    /**
     * Load exist project in a "big" sense -- loads project's properties, glossaries, tms, source files etc.
     */
    public synchronized void loadProject(boolean onlineMode) {
        Log.logInfoRB("LOG_DATAENGINE_LOAD_START");
        UIThreadsUtil.mustNotBeSwingThread();

        // load new project
        try {
            if (!lockProject()) {
                throw new KnownException("PROJECT_LOCKED");
            }
            isOnlineMode = onlineMode;

            if (RuntimePreferences.isLocationSaveEnabled()) {
                Preferences.setPreference(Preferences.CURRENT_FOLDER,
                        new File(config.getProjectRoot()).getAbsoluteFile().getParent());
                Preferences.save();
            }

            Core.getMainWindow().showStatusMessageRB("CT_LOADING_PROJECT");

            if (remoteRepositoryProvider != null) {
                try {
                    tmxPrepared = null;
                    glossaryPrepared = null;
                    remoteRepositoryProvider.switchAllToLatest();
                } catch (IRemoteRepository2.NetworkException e) {
                    Log.logErrorRB("TEAM_NETWORK_ERROR", e.getCause());
                    setOfflineMode();
                }
                remoteRepositoryProvider.copyFilesFromReposToProject("");

                // After adding filters.xml and segmentation.conf, we must reload them again
                config.loadProjectFilters();
                config.loadProjectSRX();
            }

            loadFilterSettings();
            loadSegmentationSettings();
            loadTranslations();  //load projectsave.tmx
            loadSourceFiles();

            // This MUST happen after calling loadTranslations()
            if (remoteRepositoryProvider != null && isOnlineMode) {
                Core.getMainWindow().showStatusMessageRB("TEAM_REBASE_AND_COMMIT");
                rebaseAndCommitProject(true);
            }

            //after loadSourcefiles, the entries are filled. The list can now (and only now) be readonly.
            allProjectEntries = Collections.unmodifiableList(allProjectEntries);
            //and now we can set the importHandler, used by loadTM
            importHandler = new ImportFromAutoTMX(this, allProjectEntries);

            //imports translation from source files into ProjectTMX
            importTranslationsFromSources();

            //loads external tmx, and auto/enfoce tmx'es (appending to projectTMX)
            loadTM();

            loadOtherLanguages();

            // build word count
            StatsResult stat = CalcStandardStatistics.buildProjectStats(this);
            stat.updateStatisticsInfo(hotStat);
            String fn = config.getProjectInternal() + OConsts.STATS_FILENAME;
            Statistics.writeStat(fn, stat.getTextData(config));

            loaded = true;

            // Project Loaded...
            Core.getMainWindow().showStatusMessageRB(null);

            setProjectModified(false);
        } catch (OutOfMemoryError oome) {
            // Fix for bug 1571944 @author Henry Pijffers
            // (henry.pijffers@saxnot.com)

            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            allProjectEntries.clear();
            projectFilesList.clear();
            transMemories.clear();
            projectTMX = null;

            // There, that should do it, now inform the user
            long memory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            Log.logErrorRB("OUT_OF_MEMORY", memory);
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB("TF_ERROR", "OUT_OF_MEMORY", memory);
            // Just quit, we can't help it anyway
            System.exit(0);
        } catch (Throwable e) {
            Log.logErrorRB(e, "TF_LOAD_ERROR");
            Core.getMainWindow().displayErrorRB(e, "TF_LOAD_ERROR");
            if (!loaded) {
                unlockProject();
            }
        }

        Log.logInfoRB("LOG_DATAENGINE_LOAD_END");
    }

    /**
     * Load filter settings, either from the project or from global options
     */
    private void loadFilterSettings() {
        // Set project specific file filters if they exist, or defaults otherwise.
        // This MUST happen before calling loadTranslations() because the setting to ignore file context
        // for alt translations is a filter setting, and it affects how alt translations are hashed.
        Filters filters = Optional.ofNullable(config.getProjectFilters()).orElse(Preferences.getFilters());
        Core.setFilterMaster(new FilterMaster(filters));
    }

    /**
     * Load segmentation settings, either from the project or from global options
    */
    private void loadSegmentationSettings() {
        // Set project specific segmentation rules if they exist, or defaults otherwise.
        // This MUST happen before calling loadTranslations(), because projectTMX needs a segmenter.
        SRX srx = Optional.ofNullable(config.getProjectSRX()).orElse(Preferences.getSRX());
        Core.setSegmenter(new Segmenter(srx));
    }

    /**
     * Align project.
     */
    public Map<String, TMXEntry> align(final ProjectProperties props, final File translatedDir)
            throws Exception {
        FilterMaster fm = Core.getFilterMaster();

        File root = new File(config.getSourceRoot());
        List<File> srcFileList = FileUtil.buildFileList(root, true);

        AlignFilesCallback alignFilesCallback = new AlignFilesCallback(props);

        String srcRoot = config.getSourceRoot();
        for (File file : srcFileList) {
            // shorten filename to that which is relative to src root
            String midName = file.getPath().substring(srcRoot.length());

            fm.alignFile(srcRoot, midName, translatedDir.getPath(), new FilterContext(props),
                    alignFilesCallback);
        }
        return alignFilesCallback.data;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProjectLoaded() {
        return loaded;
    }

    /**
     * {@inheritDoc}
     */
    public StatisticsInfo getStatistics() {
        return hotStat;
    }

    /**
     * Signals to the core thread that a project is being closed now, and if it's still being loaded, core
     * thread shouldn't throw any error.
     */
    public void closeProject() {
        loaded = false;
        flushProcessCache();
        tmMonitor.fin();
        tmOtherLanguagesMonitor.fin();
        unlockProject();
        Log.logInfoRB("LOG_DATAENGINE_CLOSE");
    }

    /**
     * Lock omegat.project file against rename or move project.
     */
    protected boolean lockProject() {
        if (!RuntimePreferences.isProjectLockingEnabled()) {
            return true;
        }
        try {
            File lockFile = new File(config.getProjectRoot(), OConsts.FILE_PROJECT);
            raFile = new RandomAccessFile(lockFile, "rw");
            lockChannel = raFile.getChannel();
            lock = lockChannel.tryLock();
        } catch (Throwable ex) {
            Log.log(ex);
        }
        if (lock == null) {
            try {
                lockChannel.close();
            } catch (Throwable ignored) {
            }
            lockChannel = null;
            try {
                raFile.close();
            } catch (Throwable ignored) {
            }
            raFile = null;
            return false;
        } else {
            return true;
        }
    }

    /**
     * Unlock omegat.project file against rename or move project.
     */
    protected void unlockProject() {
        if (!RuntimePreferences.isProjectLockingEnabled()) {
            return;
        }
        try {
            if (lock != null) {
                lock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
            if (raFile != null) {
                raFile.close();
            }
        } catch (Throwable ex) {
            Log.log(ex);
        } finally {
            try {
                lockChannel.close();
            } catch (Throwable ignored) {
            }
            try {
                raFile.close();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files. Convenience method. Assumes we
     * want to run external post-processing commands.
     *
     * @param sourcePattern
     *            The regexp of files to create
     * @throws Exception
     */
    public void compileProject(String sourcePattern) throws Exception {
        compileProject(sourcePattern, true);
    }

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     * Calls the actual compile project method, assumes we don't want to commit target files.
     *
     * @param sourcePattern
     *            The regexp of files to create
     * @param doPostProcessing
     *            Whether or not we should perform external post-processing.
     * @throws Exception
     */
    public void compileProject(String sourcePattern, boolean doPostProcessing) throws Exception {
        compileProjectAndCommit(sourcePattern, doPostProcessing, false);
    }

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     *
     * @param sourcePattern
     *            The regexp of files to create
     * @param doPostProcessing
     *            Whether or not we should perform external post-processing.
     * @param commitTargetFiles
     *            Whether or not we should commit target files
     * @throws Exception
     */
    @Override
    public void compileProjectAndCommit(String sourcePattern, boolean doPostProcessing, boolean commitTargetFiles)
            throws Exception {
        Log.logInfoRB("LOG_DATAENGINE_COMPILE_START");
        UIThreadsUtil.mustNotBeSwingThread();

        Pattern filePattern = Pattern.compile(sourcePattern);

        // build 3 TMX files:
        // - OmegaT-specific, with inline OmegaT formatting tags
        // - TMX Level 1, without formatting tags
        // - TMX Level 2, with OmegaT formatting tags wrapped in TMX inline tags
        try {
            // build TMX with OmegaT tags
            String fname = config.getProjectRoot() + config.getProjectName() + OConsts.OMEGAT_TMX
                    + OConsts.TMX_EXTENSION;

            projectTMX.exportTMX(config, new File(fname), false, false, false);

            // build TMX level 1 compliant file
            fname = config.getProjectRoot() + config.getProjectName() + OConsts.LEVEL1_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.exportTMX(config, new File(fname), true, false, false);

            // build three-quarter-assed TMX level 2 file
            fname = config.getProjectRoot() + config.getProjectName() + OConsts.LEVEL2_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.exportTMX(config, new File(fname), false, true, false);
        } catch (Exception e) {
            Log.logErrorRB("CT_ERROR_CREATING_TMX");
            Log.log(e);
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") + "\n" + e.getMessage());
        }

        String srcRoot = config.getSourceRoot();
        String locRoot = config.getTargetRoot();

        // build translated files
        FilterMaster fm = Core.getFilterMaster();

        List<String> pathList = FileUtil.buildRelativeFilesList(new File(srcRoot), Collections.emptyList(),
                config.getSourceRootExcludes());

        TranslateFilesCallback translateFilesCallback = new TranslateFilesCallback();

        int numberOfCompiled = 0;

        for (String midName : pathList) {
            // shorten filename to that which is relative to src root
            Matcher fileMatch = filePattern.matcher(midName);
            if (fileMatch.matches()) {
                File fn = new File(locRoot, midName);
                if (!fn.getParentFile().exists()) {
                    // target directory doesn't exist - create it
                    if (!fn.getParentFile().mkdirs()) {
                        throw new IOException(OStrings.getString("CT_ERROR_CREATING_TARGET_DIR") + fn.getParentFile());
                    }
                }
                Core.getMainWindow().showStatusMessageRB("CT_COMPILE_FILE_MX", midName);
                translateFilesCallback.fileStarted(midName);
                fm.translateFile(srcRoot, midName, locRoot, new FilterContext(config),
                        translateFilesCallback);
                translateFilesCallback.fileFinished();
                numberOfCompiled++;
            }
        }
        if (remoteRepositoryProvider != null && config.getTargetDir().isUnderRoot() && commitTargetFiles && isOnlineMode) {
            tmxPrepared = null;
            glossaryPrepared = null;
            // commit translations
            try {
                Core.getMainWindow().showStatusMessageRB("TF_COMMIT_TARGET_START");
                remoteRepositoryProvider.switchAllToLatest();
                remoteRepositoryProvider.copyFilesFromProjectToRepos(config.getTargetDir().getUnderRoot(), null);
                remoteRepositoryProvider.commitFiles(config.getTargetDir().getUnderRoot(), "Project translation");
                Core.getMainWindow().showStatusMessageRB("TF_COMMIT_TARGET_DONE");
            } catch (Exception e) {
                Log.logErrorRB("TF_COMMIT_TARGET_ERROR");
                Log.log(e);
                throw new IOException(OStrings.getString("TF_COMMIT_TARGET_ERROR") + "\n"
                        + e.getMessage());
            }
        }

        if (numberOfCompiled == 1) {
            Core.getMainWindow().showStatusMessageRB("CT_COMPILE_DONE_MX_SINGULAR");
        } else {
            Core.getMainWindow().showStatusMessageRB("CT_COMPILE_DONE_MX");
        }

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);

        if (doPostProcessing) {

            // Kill any processes still not complete
            flushProcessCache();

            if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
                doExternalCommand(config.getExternalCommand());
            }
            doExternalCommand(Preferences.getPreference(Preferences.EXTERNAL_COMMAND));
        }

        Log.logInfoRB("LOG_DATAENGINE_COMPILE_END");
    }

    /**
     * Set up and execute the user-specified external command.
     * @param command Command to execute
     */
    private void doExternalCommand(String command) {

        if (StringUtil.isEmpty(command)) {
            return;
        }

        Core.getMainWindow().showStatusMessageRB("CT_START_EXTERNAL_CMD");

        CommandVarExpansion expander = new CommandVarExpansion(command);
        command = expander.expandVariables(config);
        Log.log("Executing command: " + command);
        try {
            Process p = Runtime.getRuntime().exec(StaticUtils.parseCLICommand(command));
            processCache.push(p);
            CommandMonitor stdout = CommandMonitor.newStdoutMonitor(p);
            CommandMonitor stderr = CommandMonitor.newStderrMonitor(p);
            stdout.start();
            stderr.start();
        } catch (IOException e) {
            String message;
            Throwable cause = e.getCause();
            if (cause == null) {
                message = e.getLocalizedMessage();
            } else {
                message = cause.getLocalizedMessage();
            }
            Core.getMainWindow().showStatusMessageRB("CT_ERROR_STARTING_EXTERNAL_CMD", message);
        }
    }

    /**
     * Clear cache of previously run external processes, terminating any that haven't finished.
     */
    private void flushProcessCache() {
        while (!processCache.isEmpty()) {
            Process p = processCache.pop();
            try {
                p.exitValue();
            } catch (IllegalThreadStateException ex) {
                p.destroy();
            }
        }
    }

    /**
     * Saves the translation memory and preferences.
     *
     * This method must be executed in the Core.executeExclusively.
     */
    public synchronized void saveProject(boolean doTeamSync) {
        if (isSaving) {
            return;
        }
        isSaving = true;

        Log.logInfoRB("LOG_DATAENGINE_SAVE_START");
        UIThreadsUtil.mustNotBeSwingThread();

        Core.getAutoSave().disable();
        try {

            Core.getMainWindow().getMainMenu().getProjectMenu().setEnabled(false);
            try {
                Preferences.save();

                try {
                    saveProjectProperties();

                    projectTMX.save(config, config.getProjectInternal() + OConsts.STATUS_EXTENSION,
                            isProjectModified());

                    if (remoteRepositoryProvider != null && doTeamSync) {
                        tmxPrepared = null;
                        glossaryPrepared = null;
                        remoteRepositoryProvider.cleanPrepared();
                        Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE");
                        rebaseAndCommitProject(true);
                        setOnlineMode();
                    }

                    setProjectModified(false);
                } catch (KnownException ex) {
                    throw ex;
                } catch (IRemoteRepository2.NetworkException e) {
                    if (isOnlineMode) {
                        Log.logErrorRB("TEAM_NETWORK_ERROR", e.getCause());
                        setOfflineMode();
                    }
                } catch (Exception e) {
                    Log.logErrorRB(e, "CT_ERROR_SAVING_PROJ");
                    Core.getMainWindow().displayErrorRB(e, "CT_ERROR_SAVING_PROJ");
                }

                LastSegmentManager.saveLastSegment();

                // update statistics
                StatsResult stat = CalcStandardStatistics.buildProjectStats(this);
                stat.updateStatisticsInfo(hotStat);
                String fn = config.getProjectInternal() + OConsts.STATS_FILENAME;
                Statistics.writeStat(fn, stat.getTextData(config));
            } finally {
                Core.getMainWindow().getMainMenu().getProjectMenu().setEnabled(true);
            }

            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
        } finally {
            Core.getAutoSave().enable();
        }
        Log.logInfoRB("LOG_DATAENGINE_SAVE_END");

        isSaving = false;
    }

    /**
     * Prepare for future team sync.
     *
     * This method must be executed in the Core.executeExclusively.
     */
    @Override
    public void teamSyncPrepare() throws Exception {
        if (remoteRepositoryProvider == null || preparedStatus != PreparedStatus.NONE || !isOnlineMode) {
            return;
        }
        LOGGER.fine("Prepare team sync");
        tmxPrepared = null;
        glossaryPrepared = null;
        remoteRepositoryProvider.cleanPrepared();

        String tmxPath = config.getProjectInternalRelative() + OConsts.STATUS_EXTENSION;
        if (remoteRepositoryProvider.isUnderMapping(tmxPath)) {
            tmxPrepared = RebaseAndCommit.prepare(remoteRepositoryProvider, config.getProjectRootDir(), tmxPath);
        }

        final String glossaryPath = config.getWritableGlossaryFile().getUnderRoot();
        if (glossaryPath != null && remoteRepositoryProvider.isUnderMapping(glossaryPath)) {
            glossaryPrepared = RebaseAndCommit.prepare(remoteRepositoryProvider, config.getProjectRootDir(),
                    glossaryPath);
        }
        preparedStatus = PreparedStatus.PREPARED;
    }

    @Override
    public boolean isTeamSyncPrepared() {
        return preparedStatus == PreparedStatus.PREPARED;
    }

    /**
     * Fast team sync for execute from SaveThread.
     *
     * This method must be executed in the Core.executeExclusively.
     */
    @Override
    public void teamSync() {
        if (remoteRepositoryProvider == null || preparedStatus != PreparedStatus.PREPARED) {
            return;
        }
        LOGGER.fine("Rebase team sync");
        try {
            preparedStatus = PreparedStatus.PREPARED2;
            synchronized (RealProject.this) {
                projectTMX.save(config, config.getProjectInternal() + OConsts.STATUS_EXTENSION,
                        isProjectModified());
            }
            rebaseAndCommitProject(glossaryPrepared != null);
            preparedStatus = PreparedStatus.REBASED;

            new Thread(() -> {
                try {
                    Core.executeExclusively(true, () -> {
                        if (preparedStatus != PreparedStatus.REBASED) {
                            return;
                        }
                        LOGGER.fine("Commit team sync");
                        try {
                            String newVersion = RebaseAndCommit.commitPrepared(tmxPrepared, remoteRepositoryProvider,
                                    null);
                            if (glossaryPrepared != null) {
                                RebaseAndCommit.commitPrepared(glossaryPrepared, remoteRepositoryProvider, newVersion);
                            }

                            tmxPrepared = null;
                            glossaryPrepared = null;

                            remoteRepositoryProvider.cleanPrepared();
                        } catch (Exception ex) {
                            Log.logErrorRB(ex, "CT_ERROR_SAVING_PROJ");
                        }
                        preparedStatus = PreparedStatus.NONE;
                    });
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "CT_ERROR_SAVING_PROJ");
                }
            }).start();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "CT_ERROR_SAVING_PROJ");
            preparedStatus = PreparedStatus.NONE;
        }
    }

    /**
     * Rebase changes in project to remote HEAD and upload changes to remote if possible.
     * <p>
     * How it works.
     * <p>
     * At each moment we have 3 versions of translation (project_save.tmx file) or writable glossary:
     * <ol>
     * <li>BASE - version which current translator downloaded from remote repository previously(on previous
     * synchronization or startup).
     *
     * <li>WORKING - current version in translator's OmegaT. It doesn't exist it remote repository yet. It's
     * inherited from BASE version, i.e. BASE + local changes.
     *
     * <li>HEAD - latest version in repository, which other translators committed. It's also inherited from
     * BASE version, i.e. BASE + remote changes.
     * </ol>
     * In an ideal world, we could just calculate diff between WORKING and BASE - it will be our local changes
     * after latest synchronization, then rebase these changes on the HEAD revision, then commit into remote
     * repository.
     * <p>
     * But we have some real world limitations:
     * <ul>
     * <li>Computers and networks work slowly, i.e. this synchronization will require some seconds, but
     * translator should be able to edit translation in this time.
     * <li>We have to handle network errors
     * <li>Other translators can commit own data in the same time.
     * </ul>
     * So, in the real world synchronization works by these steps:
     * <ol>
     * <li>Download HEAD revision from remote repository and load it in memory.
     * <li>Load BASE revision from local disk.
     * <li>Calculate diff between WORKING and BASE, then rebase it on the top of HEAD revision. This step
     * synchronized around memory TMX, so, all edits are stopped. Since it's enough fast step, it's okay.
     * <li>Upload new revision into repository.
     * </ol>
     */
    private void rebaseAndCommitProject(boolean processGlossary) throws Exception {
        Log.logInfoRB("TEAM_REBASE_START");

        final String author = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                System.getProperty("user.name"));
        final StringBuilder commitDetails = new StringBuilder("Translated by " + author);
        String tmxPath = config.getProjectInternalRelative() + OConsts.STATUS_EXTENSION;
        if (remoteRepositoryProvider.isUnderMapping(tmxPath)) {
            RebaseAndCommit.rebaseAndCommit(tmxPrepared, remoteRepositoryProvider, config.getProjectRootDir(),
                    tmxPath, new RebaseAndCommit.IRebase() {
                        ProjectTMX baseTMX, headTMX;

                        @Override
                        public void parseBaseFile(File file) throws Exception {
                            baseTMX = new ProjectTMX(config.getSourceLanguage(), config
                                    .getTargetLanguage(), config.isSentenceSegmentingEnabled(), file, null);
                        }

                        @Override
                        public void parseHeadFile(File file) throws Exception {
                            headTMX = new ProjectTMX(config.getSourceLanguage(), config
                                    .getTargetLanguage(), config.isSentenceSegmentingEnabled(), file, null);
                        }

                        @Override
                        public void rebaseAndSave(File out) throws Exception {
                            mergeTMX(baseTMX, headTMX, commitDetails);
                            projectTMX.exportTMX(config, out, false, false, true);
                        }

                        @Override
                        public String getCommentForCommit() {
                            return commitDetails.toString();
                        }

                        @Override
                        public String getFileCharset(File file) throws Exception {
                            return TMXReader2.detectCharset(file);
                        }
                    });
            if (projectTMX != null) {
                // it can be not loaded yet
                ProjectTMX newTMX = new ProjectTMX(config.getSourceLanguage(),
                        config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), new File(
                                config.getProjectInternalDir(), OConsts.STATUS_EXTENSION), null);
                projectTMX.replaceContent(newTMX);
            }
        }

        if (processGlossary) {
            final String glossaryPath = config.getWritableGlossaryFile().getUnderRoot();
            final File glossaryFile = config.getWritableGlossaryFile().getAsFile();
            new File(config.getProjectRootDir(), glossaryPath);
            if (glossaryPath != null && remoteRepositoryProvider.isUnderMapping(glossaryPath)) {
                final List<GlossaryEntry> glossaryEntries;
                if (glossaryFile.exists()) {
                    glossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
                } else {
                    glossaryEntries = Collections.emptyList();
                }
                RebaseAndCommit.rebaseAndCommit(glossaryPrepared, remoteRepositoryProvider,
                        config.getProjectRootDir(), glossaryPath, new RebaseAndCommit.IRebase() {
                            List<GlossaryEntry> baseGlossaryEntries, headGlossaryEntries;

                            @Override
                            public void parseBaseFile(File file) throws Exception {
                                if (file.exists()) {
                                    baseGlossaryEntries = GlossaryReaderTSV.read(file, true);
                                } else {
                                    baseGlossaryEntries = new ArrayList<>();
                                }
                            }

                            @Override
                            public void parseHeadFile(File file) throws Exception {
                                if (file.exists()) {
                                    headGlossaryEntries = GlossaryReaderTSV.read(file, true);
                                } else {
                                    headGlossaryEntries = new ArrayList<>();
                                }
                            }

                            @Override
                            public void rebaseAndSave(File out) throws Exception {
                                List<GlossaryEntry> deltaAddedGlossaryLocal = new ArrayList<>(
                                        glossaryEntries);
                                deltaAddedGlossaryLocal.removeAll(baseGlossaryEntries);
                                List<GlossaryEntry> deltaRemovedGlossaryLocal = new ArrayList<>(
                                        baseGlossaryEntries);
                                deltaRemovedGlossaryLocal.removeAll(glossaryEntries);
                                headGlossaryEntries.addAll(deltaAddedGlossaryLocal);
                                headGlossaryEntries.removeAll(deltaRemovedGlossaryLocal);

                                for (GlossaryEntry ge : headGlossaryEntries) {
                                    GlossaryReaderTSV.append(out, ge);
                                }
                            }

                            @Override
                            public String getCommentForCommit() {
                                final String author = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                                        System.getProperty("user.name"));
                                return "Glossary changes by " + author;
                            }

                            @Override
                            public String getFileCharset(File file) throws Exception {
                                return GlossaryReaderTSV.getFileEncoding(file);
                            }
                        });
            }
        }
        Log.logInfoRB("TEAM_REBASE_END");
    }

    /**
     * Do 3-way merge of:
     *
     * Base: baseTMX
     *
     * File 1: projectTMX (mine)
     *
     * File 2: headTMX (theirs)
     */
    protected void mergeTMX(ProjectTMX baseTMX, ProjectTMX headTMX, StringBuilder commitDetails) {
        StmProperties props = new StmProperties()
                .setLanguageResource(OStrings.getResourceBundle())
                .setParentWindow(Core.getMainWindow().getApplicationFrame())
                // More than this number of conflicts will trigger List View by default.
                .setListViewThreshold(5);
        String srcLang = config.getSourceLanguage().getLanguage();
        String trgLang = config.getTargetLanguage().getLanguage();
        ProjectTMX mergedTMX = SuperTmxMerge.merge(
                new SyncTMX(baseTMX, OStrings.getString("TMX_MERGE_BASE"), srcLang, trgLang),
                new SyncTMX(projectTMX, OStrings.getString("TMX_MERGE_MINE"), srcLang, trgLang),
                new SyncTMX(headTMX, OStrings.getString("TMX_MERGE_THEIRS"), srcLang, trgLang), props);
        projectTMX.replaceContent(mergedTMX);
        Log.logDebug(LOGGER, "Merge report: {0}", props.getReport());
        commitDetails.append('\n');
        commitDetails.append(props.getReport().toString());
    }

    /**
     * Create the given directory if it does not exist yet.
     *
     * @param dir the directory path to create
     * @param dirType the directory name to show in IOException
     * @throws IOException when directory could not be created.
     */
    private void createDirectory(final String dir, final String dirType) throws IOException {
        File d = new File(dir);
        if (!d.isDirectory()) {
            if (!d.mkdirs()) {
                StringBuilder msg = new StringBuilder(OStrings.getString("CT_ERROR_CREATE"));
                if (dirType != null) {
                    msg.append("\n(.../").append(dirType).append("/)");
                }
                throw new IOException(msg.toString());
            }
        }
    }

    // ///////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////
    // protected functions

    /** Finds and loads project's TMX file with translations (project_save.tmx). */
    private void loadTranslations() throws Exception {
        File file = new File(
                config.getProjectInternalDir(), OConsts.STATUS_EXTENSION);

        try {
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_TMX");

            projectTMX = new ProjectTMX(config.getSourceLanguage(), config.getTargetLanguage(),
                    config.isSentenceSegmentingEnabled(), file, checkOrphanedCallback);
            if (file.exists()) {
                // RFE 1001918 - backing up project's TMX upon successful read
                // TODO check for repositories
                FileUtil.backupFile(file);
                FileUtil.removeOldBackups(file, OConsts.MAX_BACKUPS);
            }
        } catch (SAXParseException ex) {
            Log.logErrorRB(ex, "TMXR_FATAL_ERROR_WHILE_PARSING", ex.getLineNumber(), ex.getColumnNumber());
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB(ex, "TMXR_EXCEPTION_WHILE_PARSING", file.getAbsolutePath(), Log.getLogLocation());
            throw ex;
        }
    }

    /**
     * Load source files for project.
     */
    private void loadSourceFiles() throws Exception {
        long st = System.currentTimeMillis();
        FilterMaster fm = Core.getFilterMaster();

        File root = new File(config.getSourceRoot());
        List<String> srcPathList = FileUtil
                .buildRelativeFilesList(root, Collections.emptyList(), config.getSourceRootExcludes()).stream()
                .sorted(StreamUtil.comparatorByList(getSourceFilesOrder())).collect(Collectors.toList());

        for (String filepath : srcPathList) {
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_FILE_MX", filepath);

            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(existSource, existKeys, transMemories);

            FileInfo fi = new FileInfo();
            fi.filePath = filepath;

            loadFilesCallback.setCurrentFile(fi);

            IFilter filter = fm.loadFile(config.getSourceRoot() + filepath, new FilterContext(config),
                    loadFilesCallback);

            loadFilesCallback.fileFinished();

            if (filter != null && !fi.entries.isEmpty()) {
                fi.filterClass = filter.getClass(); //Don't store the instance, because every file gets an instance and
                                                    // then we consume a lot of memory for all instances.
                                                    //See also IFilter "TODO: each filter should be stateless"
                fi.filterFileFormatName = filter.getFileFormatName();
                try {
                    fi.fileEncoding = filter.getInEncodingLastParsedFile();
                } catch (Error e) { // In case a filter doesn't have getInEncodingLastParsedFile() (e.g., Okapi plugin)
                    fi.fileEncoding = "";
                }
                projectFilesList.add(fi);
            }
        }

        findNonUniqueSegments();

        Core.getMainWindow().showStatusMessageRB("CT_LOAD_SRC_COMPLETE");
        long en = System.currentTimeMillis();
        Log.log("Load project source files: " + (en - st) + "ms");
    }

    protected void findNonUniqueSegments() {
        Map<String, SourceTextEntry> exists = new HashMap<>(16384);

        for (FileInfo fi : projectFilesList) {
            for (int i = 0; i < fi.entries.size(); i++) {
                SourceTextEntry ste = fi.entries.get(i);
                SourceTextEntry prevSte = exists.get(ste.getSrcText());

                if (prevSte == null) {
                    // Note first appearance of this STE
                    exists.put(ste.getSrcText(), ste);
                } else {
                    // Note duplicate of already-seen STE
                    if (prevSte.duplicates == null) {
                        prevSte.duplicates = new ArrayList<>();
                    }
                    prevSte.duplicates.add(ste);
                    ste.firstInstance = prevSte;
                }
            }
        }
    }

    /**
     * This method imports translation from source files into ProjectTMX.
     *
     * If there are multiple segments with equals source, then first
     * translations will be loaded as default, all other translations will be
     * loaded as alternative.
     *
     * We shouldn't load translation from source file(even as alternative) when
     * default translation already exists in project_save.tmx. So, only first
     * load will be possible.
     */
    void importTranslationsFromSources() {
        // which default translations we added - allow to add alternatives
        // except the same translation
        Map<String, String> allowToImport = new HashMap<>();

        for (FileInfo fi : projectFilesList) {
            for (int i = 0; i < fi.entries.size(); i++) {
                SourceTextEntry ste = fi.entries.get(i);
                if (ste.getSourceTranslation() == null || ste.isSourceTranslationFuzzy()
                        || ste.getSrcText().equals(ste.getSourceTranslation()) && !allowTranslationEqualToSource) {
                    // There is no translation in source file, or translation is fuzzy
                    // or translation = source and Allow translation to be equal to source is false
                    continue;
                }

                PrepareTMXEntry prepare = new PrepareTMXEntry();
                prepare.source = ste.getSrcText();
                // project with default translations
                TMXEntry en = projectTMX.getMultipleTranslation(ste.getKey());
                if (config.isSupportDefaultTranslations()) {
                    // bug#969 - Alternative translations were not taken into account
                    // if no default translation is set.
                    if (en != null) {
                        prepare.translation = ste.getSourceTranslation();
                        projectTMX.setTranslation(ste, en, false);
                        continue;
                    }
                    // can we import as default translation ?
                    TMXEntry enDefault = projectTMX.getDefaultTranslation(ste.getSrcText());
                    if (enDefault == null) {
                        // default not exist yet - yes, we can
                        prepare.translation = ste.getSourceTranslation();
                        projectTMX.setTranslation(ste, new TMXEntry(prepare, true, null), true);
                        allowToImport.put(ste.getSrcText(), ste.getSourceTranslation());
                    } else {
                        // default translation already exist - did we just
                        // imported it ?
                        String justImported = allowToImport.get(ste.getSrcText());
                        // can we import as alternative translation ?
                        if (justImported != null && !ste.getSourceTranslation().equals(justImported)) {
                            // we just imported default and it doesn't equals to
                            // current - import as alternative
                            prepare.translation = ste.getSourceTranslation();
                            projectTMX.setTranslation(ste, new TMXEntry(prepare, false, null), false);
                        }
                    }
                } else { // project without default translations
                    // can we import as alternative translation ?
                    if (en == null) {
                        // not exist yet - yes, we can
                        prepare.translation = ste.getSourceTranslation();
                        projectTMX.setTranslation(ste, new TMXEntry(prepare, false, null), false);
                    }
                }
            }
        }
    }

    /**
     * Locates and loads external TMX files with legacy translations. Uses directory monitor for check file
     * updates.
     */
    private void loadTM()  {
        File tmRoot = new File(config.getTMRoot());
        tmMonitor = new DirectoryMonitor(tmRoot, file -> {
            if (!ExternalTMFactory.isSupported(file)) {
                // not a TMX file
                return;
            }
            if (file.getPath().replace('\\', '/').startsWith(config.getTMOtherLangRoot())) {
                // tmx in other language, which is already shown in editor. Skip it.
                return;
            }
            // create new translation memories map
            Map<String, ExternalTMX> newTransMemories = new TreeMap<>(new FileUtil.TmFileComparator(config.getTmDir().getAsFile()));
            newTransMemories.putAll(transMemories);
            if (file.exists()) {
                try {
                    ExternalTMX newTMX = ExternalTMFactory.load(file);
                    newTransMemories.put(file.getPath(), newTMX);

                    // Please note the use of "/". FileUtil.computeRelativePath rewrites all other
                    // directory separators into "/".
                    if (FileUtil.computeRelativePath(tmRoot, file).startsWith(OConsts.AUTO_TM + "/")) {
                        appendFromAutoTMX(newTMX, false);
                    } else if (FileUtil.computeRelativePath(tmRoot, file)
                            .startsWith(OConsts.AUTO_ENFORCE_TM + '/')) {
                        appendFromAutoTMX(newTMX, true);
                    }

                } catch (Exception e) {
                    String filename = file.getPath();
                    Log.logErrorRB(e, "TF_TM_LOAD_ERROR", filename);
                    Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR", filename);
                }
            } else {
                newTransMemories.remove(file.getPath());
            }
            transMemories = newTransMemories;
        });
        tmMonitor.checkChanges();
        tmMonitor.start();
    }

    /**
     * Locates and loads external TMX files with translations from same source language into different target languages.
     * (These are used to show to the translator as reference, either to see what other translators did in other languages,
     * or to better understand the source language if he doesn't master the source language, but he does know the extra
     * target language)
     * Uses directory monitor for check file updates.
     */
    private void loadOtherLanguages() {
        File tmOtherLanguagesRoot = new File(config.getTMOtherLangRoot());
        tmOtherLanguagesMonitor = new DirectoryMonitor(tmOtherLanguagesRoot, file -> {
            String name = file.getName();
            if (!name.matches("[A-Z]{2}([-_][A-Z]{2})?\\.tmx")) {
                // not a TMX file in XX_XX.tmx format
                return;
            }
            Language targetLanguage = new Language(name.substring(0, name.length() - ".tmx".length()));
            // create new translation memories map
            Map<Language, ProjectTMX> newOtherTargetLangTMs = new TreeMap<>(otherTargetLangTMs);
            if (file.exists()) {
                try {
                    ProjectTMX newTMX = new ProjectTMX(config.getSourceLanguage(), targetLanguage,
                            config.isSentenceSegmentingEnabled(), file, checkOrphanedCallback);
                    newOtherTargetLangTMs.put(targetLanguage, newTMX);
                } catch (Exception e) {
                    String filename = file.getPath();
                    Log.logErrorRB(e, "TF_TM_LOAD_ERROR", filename);
                    Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR", filename);
                }
            } else {
                newOtherTargetLangTMs.remove(targetLanguage);
            }
            otherTargetLangTMs = newOtherTargetLangTMs;
        });
        tmOtherLanguagesMonitor.checkChanges();
        tmOtherLanguagesMonitor.start();
    }

    /**
     * Append new translation from auto TMX.
     */
    void appendFromAutoTMX(ExternalTMX autoTmx, boolean isEnforcedTMX) {
        synchronized (projectTMX) {
            importHandler.process(autoTmx, isEnforcedTMX);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<SourceTextEntry> getAllEntries() {
        return allProjectEntries;
    }

    public TMXEntry getTranslationInfo(SourceTextEntry ste) {
        if (projectTMX == null) {
            return EMPTY_TRANSLATION;
        }
        TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
        if (r == null) {
            r = projectTMX.getDefaultTranslation(ste.getSrcText());
        }
        if (r == null) {
            r = EMPTY_TRANSLATION;
        }
        return r;
    }

    public AllTranslations getAllTranslations(SourceTextEntry ste) {
        AllTranslations r = new AllTranslations();
        synchronized (projectTMX) {
            r.defaultTranslation = projectTMX.getDefaultTranslation(ste.getSrcText());
            r.alternativeTranslation = projectTMX.getMultipleTranslation(ste.getKey());
            if (r.alternativeTranslation != null) {
                r.currentTranslation = r.alternativeTranslation;
            } else if (r.defaultTranslation != null) {
                r.currentTranslation = r.defaultTranslation;
            } else {
                r.currentTranslation = EMPTY_TRANSLATION;
            }
            if (r.defaultTranslation == null) {
                r.defaultTranslation = EMPTY_TRANSLATION;
            }
            if (r.alternativeTranslation == null) {
                r.alternativeTranslation = EMPTY_TRANSLATION;
            }
        }
        return r;
    }

    /**
     * Returns the active Project's Properties.
     */
    public ProjectProperties getProjectProperties() {
        return config;
    }

    /**
     * Returns whether the project was modified. I.e. translations were changed since last save.
     */
    public boolean isProjectModified() {
        return modified;
    }

    private void setProjectModified(boolean isModified) {
        modified = isModified;
        if (isModified) {
            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.MODIFIED);
        }
    }

    @Override
    public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            ExternalLinked externalLinked, AllTranslations previous) throws OptimisticLockingFail {
        if (trans == null) {
            throw new IllegalArgumentException("RealProject.setTranslation(tr) can't be null");
        }

        synchronized (projectTMX) {
            AllTranslations current = getAllTranslations(entry);
            boolean wasAlternative = current.alternativeTranslation.isTranslated();
            if (defaultTranslation) {
                if (!current.defaultTranslation.equals(previous.defaultTranslation)) {
                    throw new OptimisticLockingFail(previous.getDefaultTranslation().translation,
                            current.getDefaultTranslation().translation, current);
                }
                if (wasAlternative) {
                    // alternative -> default
                    if (!current.alternativeTranslation.equals(previous.alternativeTranslation)) {
                        throw new OptimisticLockingFail(previous.getAlternativeTranslation().translation,
                                current.getAlternativeTranslation().translation, current);
                    }
                    // remove alternative
                    setTranslation(entry, new PrepareTMXEntry(), false, null);
                }
            } else {
                // new is alternative translation
                if (!current.alternativeTranslation.equals(previous.alternativeTranslation)) {
                    throw new OptimisticLockingFail(previous.getAlternativeTranslation().translation,
                            current.getAlternativeTranslation().translation, current);
                }
            }

            setTranslation(entry, trans, defaultTranslation, externalLinked);
        }
    }

    @Override
    public void setTranslation(final SourceTextEntry entry, final PrepareTMXEntry trans, boolean defaultTranslation,
            TMXEntry.ExternalLinked externalLinked) {
        if (trans == null) {
            throw new IllegalArgumentException("RealProject.setTranslation(tr) can't be null");
        }

        TMXEntry prevTrEntry = defaultTranslation ? projectTMX.getDefaultTranslation(entry.getSrcText())
                : projectTMX.getMultipleTranslation(entry.getKey());

        trans.changer = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                System.getProperty("user.name"));
        trans.changeDate = System.currentTimeMillis();

        if (prevTrEntry == null) {
            // there was no translation yet
            prevTrEntry = EMPTY_TRANSLATION;
            trans.creationDate = trans.changeDate;
            trans.creator = trans.changer;
        } else {
            trans.creationDate = prevTrEntry.creationDate;
            trans.creator = prevTrEntry.creator;
        }

        if (StringUtil.isEmpty(trans.note)) {
            trans.note = null;
        }

        trans.source = entry.getSrcText();

        TMXEntry newTrEntry;

        if (trans.translation == null && trans.note == null) {
            // no translation, no note
            newTrEntry = null;
        } else {
            newTrEntry = new TMXEntry(trans, defaultTranslation, externalLinked);
        }

        setProjectModified(true);

        projectTMX.setTranslation(entry, newTrEntry, defaultTranslation);

        /*
         * Calculate how to statistics should be changed.
         */
        int diff = prevTrEntry.translation == null ? 0 : -1;
        diff += trans.translation == null ? 0 : +1;
        hotStat.numberOfTranslatedSegments = Math.max(0,
                Math.min(hotStat.numberOfUniqueSegments, hotStat.numberOfTranslatedSegments + diff));
    }

    @Override
    public void setNote(final SourceTextEntry entry, final TMXEntry oldTE, String note) {
        if (oldTE == null) {
            throw new IllegalArgumentException("RealProject.setNote(tr) can't be null");
        }

        // Disallow empty notes. Use null to represent lack of note.
        if (note != null && note.isEmpty()) {
            note = null;
        }

        TMXEntry prevTrEntry = oldTE.defaultTranslation ? projectTMX
                .getDefaultTranslation(entry.getSrcText()) : projectTMX
                .getMultipleTranslation(entry.getKey());
        if (prevTrEntry != null) {
            PrepareTMXEntry en = new PrepareTMXEntry(prevTrEntry);
            en.note = note;
            projectTMX.setTranslation(entry, new TMXEntry(en, prevTrEntry.defaultTranslation,
                    prevTrEntry.linked), prevTrEntry.defaultTranslation);
        } else {
            PrepareTMXEntry en = new PrepareTMXEntry();
            en.source = entry.getSrcText();
            en.note = note;
            en.translation = null;
            projectTMX.setTranslation(entry, new TMXEntry(en, true, null), true);
        }

        setProjectModified(true);
    }

    public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
        if (projectTMX == null) {
            return;
        }
        Map.Entry<String, TMXEntry>[] entries;
        synchronized (projectTMX) {
            entries = entrySetToArray(projectTMX.defaults.entrySet());
        }
        for (Map.Entry<String, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
        if (projectTMX == null) {
            return;
        }
        Map.Entry<EntryKey, TMXEntry>[] entries;
        synchronized (projectTMX) {
            entries = entrySetToArray(projectTMX.alternatives.entrySet());
        }
        for (Map.Entry<EntryKey, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <K, V> Map.Entry<K, V>[] entrySetToArray(Set<Map.Entry<K, V>> set) {
        // Assign to variable to facilitate suppressing the rawtypes warning
        Map.Entry[] a = new Map.Entry[set.size()];
        return set.toArray(a);
    }

    public boolean isOrphaned(String source) {
        return !checkOrphanedCallback.existSourceInProject(source);
    }

    public boolean isOrphaned(EntryKey entry) {
        return !checkOrphanedCallback.existEntryInProject(entry);
    }

    public Map<String, ExternalTMX> getTransMemories() {
        return Collections.unmodifiableMap(transMemories);
    }

    public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
        return Collections.unmodifiableMap(otherTargetLangTMs);
    }

    /**
     * {@inheritDoc}
     */
    public ITokenizer getSourceTokenizer() {
        return sourceTokenizer;
    }

    /**
     * {@inheritDoc}
     */
    public ITokenizer getTargetTokenizer() {
        return targetTokenizer;
    }

    /**
     * Create tokenizer class. Classes are prioritized:
     * <ol><li>Class specified on command line via <code>--ITokenizer</code>
     * and <code>--ITokenizerTarget</code></li>
     * <li>Class specified in project settings</li>
     * <li>{@link DefaultTokenizer}</li>
     * </ol>
     *
     * @param cmdLine Tokenizer class specified on command line
     * @return Tokenizer implementation
     */
    protected ITokenizer createTokenizer(String cmdLine, Class<?> projectPref) {
        if (!StringUtil.isEmpty(cmdLine)) {
            try {
                return (ITokenizer) this.getClass().getClassLoader().loadClass(cmdLine).getDeclaredConstructor()
                        .newInstance();
            } catch (ClassNotFoundException e) {
                Log.log(e.toString());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (ITokenizer) projectPref.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            Log.log(e);
        }

        return new DefaultTokenizer();
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getProjectFiles() {
        return Collections.unmodifiableList(projectFilesList);
    }

    @Override
    public String getTargetPathForSourceFile(String currentSource) {
        if (StringUtil.isEmpty(currentSource)) {
            return null;
        }
        try {
            return Core.getFilterMaster().getTargetForSource(config.getSourceRoot(),
                    currentSource, new FilterContext(config));
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }

    @Override
    public List<String> getSourceFilesOrder() {
        Path path = Paths.get(config.getProjectInternal(), OConsts.FILES_ORDER_FILENAME);
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setSourceFilesOrder(List<String> filesList) {
        Path path = Paths.get(config.getProjectInternal(), OConsts.FILES_ORDER_FILENAME);
        try (Writer wr = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String f : filesList) {
                wr.write(f);
                wr.write('\n');
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * This method converts directory separators into Unix-style. It required to have the same filenames in
     * the alternative translation in Windows and Unix boxes.
     * <p>
     * Also it can use {@code --alternate-filename-from} and {@code --alternate-filename-to} command line
     * parameters for change filename in entry key. It allows to have many versions of one file in one
     * project.
     * <p>
     * Because the filename can be stored in the project TMX, it also removes any XML-unsafe chars.
     *
     * @param filename
     *            filesystem's filename
     * @return normalized filename
     */
    protected String patchFileNameForEntryKey(String filename) {
        String f = Core.getParams().get(CLIParameters.ALTERNATE_FILENAME_FROM);
        String t = Core.getParams().get(CLIParameters.ALTERNATE_FILENAME_TO);
        String fn = filename.replace('\\', '/');
        if (f != null && t != null) {
            fn = fn.replaceAll(f, t);
        }
        return StringUtil.removeXMLInvalidChars(fn);
    }

    protected class LoadFilesCallback extends ParseEntry {
        private FileInfo fileInfo;
        private String entryKeyFilename;

        private final Set<String> existSource;
        private final Set<EntryKey> existKeys;
        private final Map<String, ExternalTMX> externalTms;

        private ExternalTMFactory.Builder tmBuilder;

        public LoadFilesCallback(Set<String> existSource, Set<EntryKey> existKeys,
                Map<String, ExternalTMX> externalTms) {
            super(config);
            this.existSource = existSource;
            this.existKeys = existKeys;
            this.externalTms = externalTms;
        }

        public void setCurrentFile(FileInfo fi) {
            fileInfo = fi;
            super.setCurrentFile(fi);
            entryKeyFilename = patchFileNameForEntryKey(fileInfo.filePath);
        }

        public void fileFinished() {
            super.fileFinished();

            if (tmBuilder != null && externalTms != null) {
                externalTms.put(entryKeyFilename, tmBuilder.done());
            }

            fileInfo = null;
            tmBuilder = null;
        }

        /**
         * {@inheritDoc}
         */
        protected void addSegment(String id, short segmentIndex, String segmentSource,
                List<ProtectedPart> protectedParts, String segmentTranslation, boolean segmentTranslationFuzzy,
                String[] props, String prevSegment, String nextSegment, String path) {
            // if the source string is empty, don't add it to TM
            if (segmentSource.trim().isEmpty()) {
                throw new RuntimeException("Segment must not be empty");
            }

            EntryKey ek = new EntryKey(entryKeyFilename, segmentSource, id, prevSegment, nextSegment, path);

            protectedParts = TagUtil.applyCustomProtectedParts(segmentSource,
                    PatternConsts.getPlaceholderPattern(), protectedParts);

            //If Allow translation equals to source is not set, we ignore such existing translations
            if (ek.sourceText.equals(segmentTranslation) && !allowTranslationEqualToSource) {
                segmentTranslation = null;
            }
            SourceTextEntry srcTextEntry = new SourceTextEntry(ek, allProjectEntries.size() + 1, props,
                    segmentTranslation, protectedParts, segmentIndex == 0);
            srcTextEntry.setSourceTranslationFuzzy(segmentTranslationFuzzy);

            if (SegmentProperties.isReferenceEntry(props)) {
                if (tmBuilder == null) {
                    tmBuilder = new ExternalTMFactory.Builder(new File(entryKeyFilename).getName());
                }
                tmBuilder.addEntry(segmentSource, segmentTranslation, id, path, props);
            } else {
                allProjectEntries.add(srcTextEntry);
                fileInfo.entries.add(srcTextEntry);

                existSource.add(segmentSource);
                existKeys.add(srcTextEntry.getKey());
            }
        }
    }

    private class TranslateFilesCallback extends TranslateEntry {
        private String currentFile;

        /**
         * Getter for currentFile
         * @return the current file being processed
         */
        @Override
        protected String getCurrentFile() {
            return currentFile;
        }

        TranslateFilesCallback() {
            super(config);
        }

        protected void fileStarted(String fn) {
            currentFile = patchFileNameForEntryKey(fn);
            super.fileStarted();
        }

        protected String getSegmentTranslation(String id, int segmentIndex, String segmentSource,
                String prevSegment, String nextSegment, String path) {
            EntryKey ek = new EntryKey(currentFile, segmentSource, id, prevSegment, nextSegment, path);
            TMXEntry tr = projectTMX.getMultipleTranslation(ek);
            if (tr == null) {
                tr = projectTMX.getDefaultTranslation(ek.sourceText);
            }
            return tr != null ? tr.translation : null;
        }
    }

    static class AlignFilesCallback implements IAlignCallback {
        AlignFilesCallback(ProjectProperties props) {
            super();
            this.config = props;
        }

        Map<String, TMXEntry> data = new HashMap<>();
        private ProjectProperties config;

        @Override
        public void addTranslation(String id, String source, String translation, boolean isFuzzy, String path,
                IFilter filter) {
            if (source != null && translation != null) {
                ParseEntry.ParseEntryResult spr = new ParseEntry.ParseEntryResult();
                boolean removeSpaces = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
                String sourceS = ParseEntry.stripSomeChars(source, spr, config.isRemoveTags(), removeSpaces);
                String transS = ParseEntry.stripSomeChars(translation, spr, config.isRemoveTags(), removeSpaces);

                PrepareTMXEntry tr = new PrepareTMXEntry();
                if (config.isSentenceSegmentingEnabled()) {
                    List<String> segmentsSource = Core.getSegmenter().segment(config.getSourceLanguage(), sourceS, null,
                            null);
                    List<String> segmentsTranslation = Core.getSegmenter()
                            .segment(config.getTargetLanguage(), transS, null, null);
                    if (segmentsTranslation.size() != segmentsSource.size()) {
                        if (isFuzzy) {
                            transS = "[" + filter.getFuzzyMark() + "] " + transS;
                        }
                        tr.source = sourceS;
                        tr.translation = transS;
                        data.put(sourceS, new TMXEntry(tr, true, null));
                    } else {
                        for (short i = 0; i < segmentsSource.size(); i++) {
                            String oneSrc = segmentsSource.get(i);
                            String oneTrans = segmentsTranslation.get(i);
                            if (isFuzzy) {
                                oneTrans = "[" + filter.getFuzzyMark() + "] " + oneTrans;
                            }
                            tr.source = oneSrc;
                            tr.translation = oneTrans;
                            data.put(sourceS, new TMXEntry(tr, true, null));
                        }
                    }
                } else {
                    if (isFuzzy) {
                        transS = "[" + filter.getFuzzyMark() + "] " + transS;
                    }
                    tr.source = sourceS;
                    tr.translation = transS;
                    data.put(sourceS, new TMXEntry(tr, true, null));
                }
            }
        }
    }

    ProjectTMX.CheckOrphanedCallback checkOrphanedCallback = new ProjectTMX.CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return existSource.contains(src);
        }

        public boolean existEntryInProject(EntryKey key) {
            return existKeys.contains(key);
        }
    };

    void setOnlineMode() {
        if (!isOnlineMode) {
            Log.logInfoRB("VCS_ONLINE");
            Core.getMainWindow().displayWarningRB("VCS_ONLINE", "VCS_OFFLINE");
        }
        isOnlineMode = true;
        preparedStatus = PreparedStatus.NONE;
    }

    void setOfflineMode() {
        if (isOnlineMode) {
            Log.logInfoRB("VCS_OFFLINE");
            Core.getMainWindow().displayWarningRB("VCS_OFFLINE", "VCS_ONLINE");
        }
        isOnlineMode = false;
        preparedStatus = PreparedStatus.NONE;
    }

    @Override
    public boolean isRemoteProject() {
        return remoteRepositoryProvider != null;
    }

    @Override
    public void commitSourceFiles() throws Exception {
        if (isRemoteProject() && config.getSourceDir().isUnderRoot())  {
            try {
                Core.getMainWindow().showStatusMessageRB("TF_COMMIT_START");
                remoteRepositoryProvider.switchAllToLatest();
                remoteRepositoryProvider.copyFilesFromProjectToRepos(config.getSourceDir().getUnderRoot(), null);
                remoteRepositoryProvider.commitFiles(config.getSourceDir().getUnderRoot(), "Commit source files");
                Core.getMainWindow().showStatusMessageRB("TF_COMMIT_DONE");
            } catch (Exception e) {
                Log.logErrorRB("TF_COMMIT_ERROR");
                Log.log(e);
                throw new IOException(OStrings.getString("TF_COMMIT_ERROR") + "\n"
                        + e.getMessage(), e);
            }
        }
    }
}
