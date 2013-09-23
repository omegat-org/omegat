/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008 Alex Buloichik
               2009-2010 Didier Briel
               2012 Alex Buloichik, Guido Leenders, Didier Briel, Martin Fleurke
               2013 Aaron Madlon-Kay, Didier Briel
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

package org.omegat.core.data;

import gen.core.filters.Filters;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.util.Version;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.RepositoryUtils;
import org.omegat.core.threads.CommandMonitor;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.Shortcuts;
import org.omegat.filters2.TranslationException;
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
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.UIThreadsUtil;
import org.xml.sax.SAXParseException;

/**
 * Loaded project implementation. Only translation could be changed after project will be loaded and set by
 * Core.setProject.
 * 
 * All components can read all data directly without synchronization. All synchronization implemented inside
 * RealProject.
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

    protected final ProjectProperties m_config;
    
    private final IRemoteRepository repository;
    private boolean isOnlineMode;

    private FileChannel lockChannel;
    private FileLock lock;

    private boolean m_modifiedFlag;

    /** List of all segments in project. */
    private List<SourceTextEntry> allProjectEntries = new ArrayList<SourceTextEntry>(4096);

    private final StatisticsInfo hotStat = new StatisticsInfo();

    private final ITokenizer sourceTokenizer, targetTokenizer;

    private DirectoryMonitor tmMonitor;
    
    private DirectoryMonitor tmOtherLanguagesMonitor;

    /**
     * Storage for all translation memories, which shouldn't be changed and saved, i.e. for /tm/*.tmx files,
     * aligned data from source files.
     * 
     * This map recreated each time when files changed. So, you can free use it without thinking about
     * synchronization.
     */
    private Map<String, ExternalTMX> transMemories = new TreeMap<String, ExternalTMX>();
    
    /**
     * Storage for all translation memories of translations to other languages.
     */
    private Map<Language, ProjectTMX> otherTargetLangTMs = new TreeMap<Language, ProjectTMX>();

    protected ProjectTMX projectTMX;

    // Sets of exist entries for check orphaned
    private Set<String> existSource = new HashSet<String>();
    private Set<EntryKey> existKeys = new HashSet<EntryKey>();

    /** Segments count in project files. */
    protected final List<FileInfo> projectFilesList = new ArrayList<FileInfo>();

    /** This instance returned if translation not exist. */
    private static final TMXEntry EMPTY_TRANSLATION = new TMXEntry("", null, true);
    
    private boolean allowTranslationEqualToSource = Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC);

    /**
     * A list of external processes. Allows previously-started, hung or long-running processes to be
     * forcibly terminated when compiling the project anew or when closing the project.
     */
    private Stack<Process> processCache = new Stack<Process>();

    /**
     * Create new project instance. It required to call {@link #createProject() createProject} or
     * {@link #loadProject() loadProject} methods just after constructor before use project.
     * 
     * @param props
     *            project properties
     * @param isNewProject
     *            true if project need to be created
     */
    public RealProject(final ProjectProperties props) {
        this(props, null);
    }

    public RealProject(final ProjectProperties props, IRemoteRepository repository) {
        m_config = props;
        this.repository = repository;

        sourceTokenizer = createTokenizer(Core.getParams().get(ITokenizer.CLI_PARAM_SOURCE), props.getSourceTokenizer());
        configTokenizer(Core.getParams().get(ITokenizer.CLI_PARAM_SOURCE_BEHAVIOR), sourceTokenizer);
        Log.log("Source tokenizer: " + sourceTokenizer.getClass().getName() + " (" + sourceTokenizer.getBehavior() + ")");
        targetTokenizer = createTokenizer(Core.getParams().get(ITokenizer.CLI_PARAM_TARGET), props.getTargetTokenizer());
        configTokenizer(Core.getParams().get(ITokenizer.CLI_PARAM_TARGET_BEHAVIOR), targetTokenizer);
        Log.log("Target tokenizer: " + targetTokenizer.getClass().getName() + " (" + targetTokenizer.getBehavior() + ")");
    }
    
    public IRemoteRepository getRepository() {
        return repository;
    }

    public void saveProjectProperties() throws Exception {
        unlockProject();
        try {
            ProjectFileStorage.writeProjectFile(m_config);
        } finally {
            lockProject();
        }
        Preferences.setPreference(Preferences.SOURCE_LOCALE, m_config.getSourceLanguage().toString());
        Preferences.setPreference(Preferences.TARGET_LOCALE, m_config.getTargetLanguage().toString());
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

            createDirectory(m_config.getProjectRoot(), null);
            createDirectory(m_config.getProjectInternal(), OConsts.DEFAULT_INTERNAL);
            createDirectory(m_config.getSourceRoot(), OConsts.DEFAULT_SOURCE);
            createDirectory(m_config.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY);
            createDirectory(m_config.getTMRoot(), OConsts.DEFAULT_TM);
            createDirectory(m_config.getTMAutoRoot(), OConsts.AUTO_TM);
            createDirectory(m_config.getDictRoot(), OConsts.DEFAULT_DICT);
            createDirectory(m_config.getTargetRoot(), OConsts.DEFAULT_TARGET);
            //createDirectory(m_config.getTMOtherLangRoot(), OConsts.DEFAULT_OTHERLANG);

            saveProjectProperties();

            // set project specific segmentation rules if they exist
            Segmenter.srx = m_config.getProjectSRX();
            if (Segmenter.srx == null) {
                Segmenter.srx = Preferences.getSRX();
            }

            loadTranslations();

            loadTM();

            loadOtherLanguages();

            allProjectEntries = Collections.unmodifiableList(allProjectEntries);
        } catch (Exception e) {
            // trouble in tinsletown...
            Log.logErrorRB(e, "CT_ERROR_CREATING_PROJECT");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_CREATING_PROJECT");
        }
        Log.logInfoRB("LOG_DATAENGINE_CREATE_END");
    }

    /**
     * Load exist project in a "big" sense -- loads project's properties, glossaries, tms, source files etc.
     */
    public void loadProject(boolean onlineMode) {
        Log.logInfoRB("LOG_DATAENGINE_LOAD_START");
        UIThreadsUtil.mustNotBeSwingThread();

        // load new project
        try {
            if (!lockProject()) {
                throw new KnownException("PROJECT_LOCKED");
            }
            isOnlineMode = onlineMode;

            Preferences.setPreference(Preferences.CURRENT_FOLDER, new File(m_config.getProjectRoot())
                    .getParentFile().getAbsolutePath());
            Preferences.save();

            Core.getMainWindow().showStatusMessageRB("CT_LOADING_PROJECT");

            // set project specific file filters if they exist
            Filters filterMasterConfig = FilterMaster.loadConfig(m_config.getProjectInternal());
            if (filterMasterConfig == null) {
                filterMasterConfig = FilterMaster.loadConfig(StaticUtils.getConfigDir());
            }
            if (filterMasterConfig == null) {
                filterMasterConfig = FilterMaster.createDefaultFiltersConfig();
            }
            Core.setFilterMaster(new FilterMaster(filterMasterConfig));

            // set project specific segmentation rules if they exist
            Segmenter.srx = m_config.getProjectSRX();
            if (Segmenter.srx == null) {
                Segmenter.srx = Preferences.getSRX();
            }

            loadSourceFiles();

            loadTranslations();

            importTranslationsFromSources();

            loadTM();

            loadOtherLanguages();

            // build word count
            String stat = CalcStandardStatistics.buildProjectStats(this, hotStat);
            String fn = m_config.getProjectInternal() + OConsts.STATS_FILENAME;
            Statistics.writeStat(fn, stat);

            allProjectEntries = Collections.unmodifiableList(allProjectEntries);

            // Project Loaded...
            Core.getMainWindow().showStatusMessageRB(null);

            m_modifiedFlag = false;
        } catch (Exception e) {
            Log.logErrorRB(e, "TF_LOAD_ERROR");
            Core.getMainWindow().displayErrorRB(e, "TF_LOAD_ERROR");
        }
        // Fix for bug 1571944 @author Henry Pijffers
        // (henry.pijffers@saxnot.com)
        catch (OutOfMemoryError oome) {
            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            allProjectEntries.clear();
            projectFilesList.clear();
            transMemories.clear();
            projectTMX = null;

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Object[] args = { Runtime.getRuntime().maxMemory() / 1024 / 1024 };
            Log.logErrorRB("OUT_OF_MEMORY", args);
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB("OUT_OF_MEMORY", args, "TF_ERROR");
            // Just quit, we can't help it anyway
            System.exit(0);
        }

        Log.logInfoRB("LOG_DATAENGINE_LOAD_END");
    }

    /**
     * Align project.
     */
    public Map<String, TMXEntry> align(final ProjectProperties props, final File translatedDir)
            throws Exception {
        FilterMaster fm = Core.getFilterMaster();
        
        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);

        AlignFilesCallback alignFilesCallback = new AlignFilesCallback(props);

        String srcRoot = m_config.getSourceRoot();
        for (String filename : srcFileList) {
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());

            fm.alignFile(srcRoot, midName, translatedDir.getPath(), new FilterContext(props),
                    alignFilesCallback);
        }
        return alignFilesCallback.data;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProjectLoaded() {
        return projectTMX != null;
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
        if (repository != null) {
            if (!repository.isFilesLockingAllowed()) {
                return true;
            }
        }
        try {
            File lockFile = new File(m_config.getProjectRoot(), OConsts.FILE_PROJECT);
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = lockChannel.tryLock();
        } catch (Throwable ex) {
            Log.log(ex);
        }
        if (lock == null) {
            try {
                lockChannel.close();
            } catch (Throwable ex) {
            }
            lockChannel = null;
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
        if (repository != null) {
            if (!repository.isFilesLockingAllowed()) {
                return;
            }
        }
        try {
            if (lock != null) {
                lock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
        } catch (Throwable ex) {
            Log.log(ex);
        }
    }

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     * Convenience method. Assumes we want to run external post-processing commands.
     * 
     * @param sourcePattern
     *            The regexp of files to create
     * @throws IOException
     * @throws TranslationException
     */
    public void compileProject(String sourcePattern) throws IOException, TranslationException {
        compileProject(sourcePattern, true);
    }
    
    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     * 
     * @param sourcePattern
     *            The regexp of files to create
     * @param doPostProcessing
     *            Whether or not we should perform external post-processing.
     * @throws IOException
     * @throws TranslationException
     */
    public void compileProject(String sourcePattern, boolean doPostProcessing) throws IOException, TranslationException {
        Log.logInfoRB("LOG_DATAENGINE_COMPILE_START");
        UIThreadsUtil.mustNotBeSwingThread();

        Pattern FILE_PATTERN = Pattern.compile(sourcePattern);

        // build 3 TMX files:
        // - OmegaT-specific, with inline OmegaT formatting tags
        // - TMX Level 1, without formatting tags
        // - TMX Level 2, with OmegaT formatting tags wrapped in TMX inline tags
        try {
            // build TMX with OmegaT tags
            String fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.OMEGAT_TMX
                    + OConsts.TMX_EXTENSION;

            projectTMX.exportTMX(m_config, new File(fname), false, false, false);

            // build TMX level 1 compliant file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL1_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.exportTMX(m_config, new File(fname), true, false, false);

            // build three-quarter-assed TMX level 2 file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL2_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.exportTMX(m_config, new File(fname), false, true, false);
        } catch (Exception e) {
            Log.logErrorRB("CT_ERROR_CREATING_TMX");
            Log.log(e);
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") + "\n" + e.getMessage());
        }

        // build mirror directory of source tree
        List<String> fileList = new ArrayList<String>(256);
        String srcRoot = m_config.getSourceRoot();
        String locRoot = m_config.getTargetRoot();
        StaticUtils.buildDirList(fileList, new File(srcRoot));

        for (String filename : fileList) {
            String destFileName = locRoot + filename.substring(srcRoot.length());
            File destFile = new File(destFileName);
            if (!destFile.exists()) {
                // target directory doesn't exist - create it
                if (!destFile.mkdir()) {
                    throw new IOException(OStrings.getString("CT_ERROR_CREATING_TARGET_DIR") + destFileName);
                }
            }
        }

        // build translated files
        FilterMaster fm = Core.getFilterMaster();

        fileList.clear();
        StaticUtils.buildFileList(fileList, new File(srcRoot), true);

        TranslateFilesCallback translateFilesCallback = new TranslateFilesCallback();

        for (String filename : fileList) {
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
            Matcher fileMatch = FILE_PATTERN.matcher(midName);
            if (fileMatch.matches()) {
                Core.getMainWindow().showStatusMessageRB("CT_COMPILE_FILE_MX", midName);
                translateFilesCallback.fileStarted(midName);
                fm.translateFile(srcRoot, midName, locRoot, new FilterContext(m_config),
                        translateFilesCallback);
                translateFilesCallback.fileFinished();
            }
        }
        Core.getMainWindow().showStatusMessageRB("CT_COMPILE_DONE_MX");

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);
        
        if (doPostProcessing) {

            // Kill any processes still not complete
            flushProcessCache();

            if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
                doExternalCommand(m_config.getExternalCommand());
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
        
        if (command == null || command.length() == 0) {
            return;
        }
        
        Core.getMainWindow().showStatusMessageRB("CT_START_EXTERNAL_CMD");
        
        CommandVarExpansion expander = new CommandVarExpansion(command);
        command = expander.expandVariables(m_config);
        Log.log("Executing command: " + command);
        try {
            Process p = Runtime.getRuntime().exec(StaticUtils.parseCLICommand(command));
            processCache.push(p);
            CommandMonitor stdout = CommandMonitor.StdoutMonitor(p);
            CommandMonitor stderr = CommandMonitor.StderrMonitor(p);
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

    /** Saves the translation memory and preferences */
    public synchronized void saveProject() {
        Log.logInfoRB("LOG_DATAENGINE_SAVE_START");
        UIThreadsUtil.mustNotBeSwingThread();

        Core.getAutoSave().disable();
        try {

            Core.getMainWindow().getMainMenu().getProjectMenu().setEnabled(false);
            try {
                Preferences.save();

                String s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;

                try {
                    saveProjectProperties();

                    projectTMX.save(m_config, s, isProjectModified());

                    if (repository != null) {
                        Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE");
                        rebaseProject();
                    }

                    m_modifiedFlag = false;
                } catch (KnownException ex) {
                    throw ex;
                } catch (Exception e) {
                    Log.logErrorRB(e, "CT_ERROR_SAVING_PROJ");
                    Core.getMainWindow().displayErrorRB(e, "CT_ERROR_SAVING_PROJ");
                }

                // update statistics
                String stat = CalcStandardStatistics.buildProjectStats(this, hotStat);
                String fn = m_config.getProjectInternal() + OConsts.STATS_FILENAME;
                Statistics.writeStat(fn, stat);
            } finally {
                Core.getMainWindow().getMainMenu().getProjectMenu().setEnabled(true);
            }

            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
        } finally {
            Core.getAutoSave().enable();
        }
        Log.logInfoRB("LOG_DATAENGINE_SAVE_END");
    }

    /**
     * Rebase changes in project to remote HEAD and upload changes to remote if possible.
     *
     * How it works.
     *
     * On each moment we have 3 versions of translation(project_save.tmx file) or writable glossary:
     *
     * 1. BASE - version which current translator downloaded from remote repository previously(on previous
     * synchronization or startup).
     *
     * 2. WORKING - current version in translator's OmegaT. It doesn't exist it remote repository yet. It's
     * inherited from BASE version, i.e. BASE + local changes.
     *
     * 3. HEAD - latest version in repository, which other translators committed. It's also inherited from BASE
     * version, i.e. BASE + remote changes.
     *
     * In the ideal world, we could just calculate diff between WORKING and BASE - it will be our local changes
     * after latest synchronization, then rebase these changes on the HEAD revision, then commit into remote
     * repository.
     *
     * But we have some real world limitations: a) computers and networks work slowly, i.e. this synchronization
     * will require some seconds, but translator should be able to edit translation in this time. b) we have to
     * handle network errors, c) other translators can commit own data in the same time.
     *
     * So, in the real world synchronization works by these steps:
     *
     * 1. Download HEAD revision from remote repository and load it in memory.
     *
     * 2. Load BASE revision from local disk.
     *
     * 3. Calculate diff between WORKING and BASE, then rebase it on the top of HEAD revision. This step
     * synchronized around memory TMX, so, all edits are stopped. Since it's enough fast step, it's okay.
     *
     * 4. Upload new revision into repository.
     *
     * @author Alex Buloichik <alex73mail@gmail.com>
     * @author Martin Fleurke
     */
    private void rebaseProject() throws Exception {
        File filenameTMXwithLocalChangesOnBase, filenameTMXwithLocalChangesOnHead;
        ProjectTMX baseTMX, headTMX;

        File filenameGlossarywithLocalChangesOnBase, filenameGlossarywithLocalChangesOnHead;
        List<GlossaryEntry> glossaryEntries = null;
        List<GlossaryEntry> baseGlossaryEntries = null;
        List<GlossaryEntry> headGlossaryEntries = null;
        final boolean updateGlossary;

        final String projectTMXFilename = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
        final File projectTMXFile = new File(projectTMXFilename);

        //list of files to update. This is TMX and possibly the writable glossary file.
        File[] modifiedFiles;
        //do we have local changes?
        boolean needUpload = false;

        final String glossaryFilename = m_config.getWriteableGlossary();
        final File glossaryFile = new File(glossaryFilename);

        //Get current status in memory
        //tmx is already in memory, as 'this.projectTMX'
        //Writable glossary is also in memory, but 'outside our reach' and may change if we mess with the file on disk.
        //Therefore load glossary in memory from file:
        if (repository.isUnderVersionControl(glossaryFile)) {
            //glossary is under version control
            glossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
            modifiedFiles = new File[]{projectTMXFile, glossaryFile};
            updateGlossary = true;
        } else {
            modifiedFiles = new File[]{projectTMXFile};
            updateGlossary = false;
        }
        if (isProjectModified() || repository.isChanged(glossaryFile) || repository.isChanged(projectTMXFile)) {
            needUpload = true;
        }

        //get revisions of files
        String baseRevTMX = repository.getBaseRevisionId(projectTMXFile);
        String baseRevGlossary = null;
        if (updateGlossary) {
            baseRevGlossary = repository.getBaseRevisionId(glossaryFile);
        }

        //save current status to file in case we encounter errors.
        // save into ".new" file
        filenameTMXwithLocalChangesOnBase = new File(projectTMXFilename + "-based_on_" + baseRevTMX + OConsts.NEWFILE_EXTENSION);
        filenameGlossarywithLocalChangesOnBase = null;
        projectTMX.exportTMX(m_config, filenameTMXwithLocalChangesOnBase, false, false, true); //overwrites file if it exists
        if (updateGlossary) {
            filenameGlossarywithLocalChangesOnBase = new File(glossaryFilename + "-based_on_" + baseRevGlossary + OConsts.NEWFILE_EXTENSION);
            if (filenameGlossarywithLocalChangesOnBase.exists()) {
                //empty file first, because we append to it.
                filenameGlossarywithLocalChangesOnBase.delete();
                filenameGlossarywithLocalChangesOnBase.createNewFile();
            }
            for (GlossaryEntry ge : glossaryEntries) {
                GlossaryReaderTSV.append(filenameGlossarywithLocalChangesOnBase, ge);
            }
        }

        // restore BASE revision
        repository.restoreBase(modifiedFiles);
        // load base revision
        baseTMX = new ProjectTMX(m_config.getSourceLanguage(), m_config.getTargetLanguage(), m_config.isSentenceSegmentingEnabled(), projectTMXFile, null);
        if (updateGlossary) {
            baseGlossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
        }

        //Maybe user has made local changes to other files. We don't want that. 
        //Every translator in a project should work on the SAME=equal project.
        //Now is a good time to 'clean' the project.
        //Note that we can replace restoreBase with reset for the same functionality.
        //Here I keep a separate call, just to make it clear what and why we are doing
        //and to allow to make this feature optional in future releases
        unlockProject(); // So that we are able to replace omegat.project
        try {
            repository.reset();
        } finally {
            lockProject(); // we restore the lock
        }

        /* project is now in a bad state!
         * If an error is raised before we reach the end of this function, we have backups in .new files.
         * The user can use them to fix stuff, because
         * -The tmx is still in memory, and on save it will be updated (but we can't assume that).
         * -the glossary will be updated to the base, so we lost that.
         */

        // update to HEAD revision from repository and load
        try {
            //NB: if glossary is updated, this will cause the GlossaryManager to reload the file :(
            //I can live with that; we will update it a few lines down, so loss of info is only temporary. Only reloading of big files might take resources.
            repository.download(modifiedFiles);
            //download succeeded, we are online!
            setOnlineMode();
        } catch (IRemoteRepository.NetworkException ex) {
            //network problems, we are offline.
            setOfflineMode();
            //not on HEAD, so upload will fail
            needUpload = false;
            //go on to restore changes
        } catch (Exception ex) {
            //not on HEAD, so upload will fail
            needUpload = false;
            //go on to restore changes
        }
        String headRevTMX = repository.getBaseRevisionId(projectTMXFile);

        if (headRevTMX.equals(baseRevTMX)) {
            // don't need rebase
            filenameTMXwithLocalChangesOnHead = filenameTMXwithLocalChangesOnBase;
            filenameTMXwithLocalChangesOnBase = null;
            //free up some memory
            baseTMX = null;
        } else {
            // need rebase
            headTMX = new ProjectTMX(m_config.getSourceLanguage(), m_config.getTargetLanguage(), m_config.isSentenceSegmentingEnabled(), projectTMXFile, null);
            //calculate delta with base, and apply delta on head and replace translations with new head.
            projectTMX.calculateDeltaAndApply(baseTMX, headTMX);
            filenameTMXwithLocalChangesOnHead = new File(projectTMXFilename + "-based_on_" + headRevTMX + OConsts.NEWFILE_EXTENSION);
            projectTMX.exportTMX(m_config, filenameTMXwithLocalChangesOnHead, false, false, true);
            //free memory
            headTMX = null;
        }
        if (updateGlossary) {
            String headRevGlossary = repository.getBaseRevisionId(glossaryFile);
            if (headRevGlossary.equals(baseRevGlossary)) {
                // don't need rebase
                filenameGlossarywithLocalChangesOnHead = filenameGlossarywithLocalChangesOnBase;
                filenameGlossarywithLocalChangesOnBase = null;
                //free up some memory
                baseGlossaryEntries = null;
            } else {
                headGlossaryEntries = GlossaryReaderTSV.read(glossaryFile, true);
                List<GlossaryEntry> deltaAddedGlossaryLocal = new ArrayList<GlossaryEntry>(glossaryEntries);
                deltaAddedGlossaryLocal.removeAll(baseGlossaryEntries);
                List<GlossaryEntry> deltaRemovedGlossaryLocal = new ArrayList<GlossaryEntry>(baseGlossaryEntries);
                deltaRemovedGlossaryLocal.removeAll(glossaryEntries);
                headGlossaryEntries.addAll(deltaAddedGlossaryLocal);
                headGlossaryEntries.removeAll(deltaRemovedGlossaryLocal);

                filenameGlossarywithLocalChangesOnHead = new File(glossaryFilename + "-based_on_" + headRevGlossary + OConsts.NEWFILE_EXTENSION);
                for (GlossaryEntry ge : headGlossaryEntries) {
                    GlossaryReaderTSV.append(filenameGlossarywithLocalChangesOnHead, ge);
                }

                //free memory
                headGlossaryEntries = null;
                baseGlossaryEntries = null;
                glossaryEntries = null;
            }
        } else {
            filenameGlossarywithLocalChangesOnHead = null;
        }

        /* project_save.tmx / writableGlossary are now the head version (or still the base version, if offline)
         * the old situation is in based_on_<base>.new files
         * the new situation is in based_on_<head>.new files
         */

        projectTMXFile.delete(); //delete head version (or base version, if offline)
        // Rename new file into TMX file
        if (!filenameTMXwithLocalChangesOnHead.renameTo(projectTMXFile)) {
            throw new IOException("Error rename new file to tmx");
        }
        if (filenameTMXwithLocalChangesOnBase != null) {
            // Remove temp backup file
            if (!filenameTMXwithLocalChangesOnBase.delete()) {
                throw new IOException("Error remove old file");
            }
        }
        if (updateGlossary) {
            glossaryFile.delete();
            if (!filenameGlossarywithLocalChangesOnHead.renameTo(glossaryFile)) {
                throw new IOException("Error rename new file to glossary");
            }
            if (filenameGlossarywithLocalChangesOnBase != null) {
                // Remove temp backup file
                if (!filenameGlossarywithLocalChangesOnBase.delete()) {
                    throw new IOException("Error remove old glossary file");
                }
            }
        }

        // upload updated
        if (needUpload) {
            final String author = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                    System.getProperty("user.name"));
            try {
                new RepositoryUtils.AskCredentials() {
                    public void callRepository() throws Exception {
                        repository.upload(projectTMXFile, "Translated by " + author);
                        if (updateGlossary) {
                            repository.upload(glossaryFile, "Added glossaryitem(s) by " + author);
                        }
                    }
                }.execute(repository);
                setOnlineMode();
            } catch (IRemoteRepository.NetworkException ex) {
                setOfflineMode();
            } catch (Exception ex) {
                throw new KnownException(ex, "TEAM_SYNCHRONIZATION_ERROR");
            }
        }

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

        final File tmxFile = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);

        try {
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_TMX");

            projectTMX = new ProjectTMX(m_config.getSourceLanguage(), m_config.getTargetLanguage(), m_config.isSentenceSegmentingEnabled(), tmxFile, checkOrphanedCallback);
            if (tmxFile.exists()) {
                // RFE 1001918 - backing up project's TMX upon successful read
                FileUtil.backupFile(tmxFile);
                FileUtil.removeOldBackups(tmxFile);
            }
        } catch (SAXParseException ex) {
            Log.logErrorRB(ex, "TMXR_FATAL_ERROR_WHILE_PARSING", ex.getLineNumber(), ex.getColumnNumber());
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB(ex, "TMXR_EXCEPTION_WHILE_PARSING", tmxFile.getAbsolutePath(),
                    Log.getLogLocation());
            throw ex;
        }
    }

    /**
     * Load source files for project.
     * 
     * @param projectRoot
     *            project root dir
     */
    private void loadSourceFiles() throws IOException, InterruptedIOException, TranslationException {
        long st = System.currentTimeMillis();
        FilterMaster fm = Core.getFilterMaster();

        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        Collections.sort(srcFileList, new FileNameComparator());

        for (String filename : srcFileList) {
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(m_config.getSourceRoot().length());

            Core.getMainWindow().showStatusMessageRB("CT_LOAD_FILE_MX", filepath);

            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(existSource, existKeys);

            FileInfo fi = new FileInfo();
            fi.filePath = filepath;

            loadFilesCallback.setCurrentFile(fi);

            IFilter filter = fm.loadFile(filename, new FilterContext(m_config), loadFilesCallback);

            loadFilesCallback.fileFinished();

            if (filter != null && (fi.entries.size() > 0)) {
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

    /**
     * {@inheritDoc}
     */
    protected void findNonUniqueSegments() {
        Map<String, SourceTextEntry> exists = new HashMap<String, SourceTextEntry>(16384);

        for (FileInfo fi : projectFilesList) {
            for (int i = 0; i < fi.entries.size(); i++) {
                SourceTextEntry ste = fi.entries.get(i);
                SourceTextEntry prevSte = exists.get(ste.getSrcText());

                if (prevSte == null) {
                    // didn't processed the same entry yet
                    ste.duplicate = SourceTextEntry.DUPLICATE.NONE;
                } else {
                    if (prevSte.duplicate == SourceTextEntry.DUPLICATE.NONE) {
                        // already processed,but this is first duplicate
                        prevSte.duplicate = SourceTextEntry.DUPLICATE.FIRST;
                        ste.duplicate = SourceTextEntry.DUPLICATE.NEXT;
                    } else {
                        // already processed, and this is not first duplicate
                        ste.duplicate = SourceTextEntry.DUPLICATE.NEXT;
                    }
                }

                if (prevSte == null) {
                    exists.put(ste.getSrcText(), ste);
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
        Map<String, String> allowToImport = new HashMap<String, String>();
        
        for (FileInfo fi : projectFilesList) {
            for (int i = 0; i < fi.entries.size(); i++) {
                SourceTextEntry ste = fi.entries.get(i);
                if (ste.getSourceTranslation() == null || ste.isSourceTranslationFuzzy() ||
                   ste.getSrcText().equals(ste.getSourceTranslation()) && !allowTranslationEqualToSource) {
                    // There is no translation in source file, or translation is fuzzy
                    // or translation = source and Allow translation to be equal to source is false
                    continue;
                }

                // project with default translations
                if (m_config.isSupportDefaultTranslations()) {
                    // can we import as default translation ?
                    TMXEntry enDefault = projectTMX.getDefaultTranslation(ste.getSrcText());
                    if (enDefault == null) {
                        // default not exist yet - yes, we can
                        TMXEntry tr = new TMXEntry(ste.getSrcText(), ste.getSourceTranslation(), true);
                        projectTMX.setTranslation(ste, tr, true);
                        allowToImport.put(ste.getSrcText(), ste.getSourceTranslation());
                    } else {
                        // default translation already exist - did we just
                        // imported it ?
                        String justImported = allowToImport.get(ste.getSrcText());
                        // can we import as alternative translation ?
                        if (justImported != null && !ste.getSourceTranslation().equals(justImported)) {
                            // we just imported default and it doesn't equals to
                            // current - import as alternative
                            TMXEntry tr = new TMXEntry(ste.getSrcText(), ste.getSourceTranslation(), false);
                            projectTMX.setTranslation(ste, tr, false);
                        }
                    }
                } else { // project without default translations
                    // can we import as alternative translation ?
                    TMXEntry en = projectTMX.getMultipleTranslation(ste.getKey());
                    if (en == null) {
                        // not exist yet - yes, we can
                        TMXEntry tr = new TMXEntry(ste.getSrcText(), ste.getSourceTranslation(), false);
                        projectTMX.setTranslation(ste, tr, false);
                    }
                }
            }
        }
    }

    /**
     * Locates and loads external TMX files with legacy translations. Uses directory monitor for check file
     * updates.
     */
    private void loadTM() throws IOException {
        final File tmRoot = new File(m_config.getTMRoot());
        tmMonitor = new DirectoryMonitor(tmRoot, new DirectoryMonitor.Callback() {
            public void fileChanged(File file) {
                if (!file.getName().endsWith(OConsts.TMX_EXTENSION)
                        && !file.getName().endsWith(OConsts.TMX_GZ_EXTENSION)) {
                    // not a TMX file
                    return;
                }
                if (file.getPath().startsWith(m_config.getTMOtherLangRoot()) ) {
                    //tmx in other language, which is already shown in editor. Skip it.
                    return;
                }
                // create new translation memories map
                Map<String, ExternalTMX> newTransMemories = new TreeMap<String, ExternalTMX>(transMemories);
                if (file.exists()) {
                    try {
                        ExternalTMX newTMX = new ExternalTMX(m_config, file,
                                Preferences.isPreference(Preferences.EXT_TMX_SHOW_LEVEL2),
                                Preferences.isPreference(Preferences.EXT_TMX_USE_SLASH));
                        newTransMemories.put(file.getPath(), newTMX);

                        //
                        // Please note the use of "/". FileUtil.computeRelativePath rewrites all other
                        // directory separators into "/".
                        //
                        if (FileUtil.computeRelativePath(tmRoot, file).startsWith(OConsts.AUTO_TM + "/")) {
                            appendFromAutoTMX(newTMX);
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
            }
        });
        tmMonitor.checkChanges();
        tmMonitor.start();
    }
    
    /**
     * Locates and loads external TMX files with legacy translations. Uses directory monitor for check file
     * updates.
     */
    private void loadOtherLanguages() throws IOException {
        final File tmOtherLanguagesRoot = new File(m_config.getTMOtherLangRoot());
        tmOtherLanguagesMonitor = new DirectoryMonitor(tmOtherLanguagesRoot, new DirectoryMonitor.Callback() {
            public void fileChanged(File file) {
                if (!file.getName().matches("[A-Z]{2}([-_][A-Z]{2})?\\.tmx")) {
                    // not a TMX file in XX_XX.tmx format
                    return;
                }
                Language targetLanguage = new Language(file.getName().substring(0, file.getName().length()-4));
                // create new translation memories map
                Map<Language, ProjectTMX> newOtherTargetLangTMs = new TreeMap<Language, ProjectTMX>(otherTargetLangTMs);
                if (file.exists()) {
                    try {
                        ProjectTMX newTMX = new ProjectTMX(m_config.getSourceLanguage(), targetLanguage,
                            m_config.isSentenceSegmentingEnabled(), file, checkOrphanedCallback);
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
            }
        });
        tmOtherLanguagesMonitor.checkChanges();
        tmOtherLanguagesMonitor.start();
    }

    /**
     * Append new translation from auto TMX.
     */
    private void appendFromAutoTMX(ExternalTMX tmx) {
        Set<String> existSources = new HashSet<String>(allProjectEntries.size());
        for (SourceTextEntry ste : allProjectEntries) {
            existSources.add(ste.getSrcText());
        }
        synchronized (projectTMX) {
            for (TMXEntry e : tmx.getEntries()) {
                if (existSources.contains(e.source)) {
                    // source exist
                    if (!projectTMX.defaults.containsKey(e.source)) {
                        // translation not exist
                        projectTMX.defaults.put(e.source, e);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<SourceTextEntry> getAllEntries() {
        return allProjectEntries;
    }

    public TMXEntry getTranslationInfo(SourceTextEntry ste) {
        TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
        if (r == null) {
            r = projectTMX.getDefaultTranslation(ste.getSrcText());
        }
        if (r == null) {
            r = EMPTY_TRANSLATION;
        }
        return r;
    }

    /**
     * Returns the active Project's Properties.
     */
    public ProjectProperties getProjectProperties() {
        return m_config;
    }

    /**
     * Returns whether the project was modified. I.e. translations were changed since last save.
     */
    public boolean isProjectModified() {
        return m_modifiedFlag;
    }

    /**
     * {@inheritDoc}
     */
    public void setTranslation(final SourceTextEntry entry, final String trans, String note, boolean isDefault) {

        TMXEntry prevTrEntry = isDefault ? projectTMX.getDefaultTranslation(entry.getSrcText()) : projectTMX
                .getMultipleTranslation(entry.getKey());
        
        String creator;
        long creationDate;
        String changer = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                System.getProperty("user.name"));
        long changeDate = System.currentTimeMillis();

        if (prevTrEntry == null) {
            // there was no translation yet
            prevTrEntry = EMPTY_TRANSLATION;
            creationDate = changeDate;
            creator = changer;
        } else {
            creationDate = prevTrEntry.creationDate;
            creator = prevTrEntry.creator;
        }
        
        TMXEntry newTrEntry;
        if (StringUtil.equalsWithNulls(prevTrEntry.translation, trans)) {
            // translation not changed
            if (StringUtil.nvl(prevTrEntry.note, "").equals(StringUtil.nvl(note, ""))) {
                // note was not changed also
                return;
            }
            // only note was changed
            newTrEntry = new TMXEntry(prevTrEntry.source, prevTrEntry.translation, prevTrEntry.changer,
                    prevTrEntry.changeDate, prevTrEntry.creator, prevTrEntry.creationDate,
                    (StringUtil.isEmpty(note) ? null : note), prevTrEntry.defaultTranslation, null);
        } else {
            // translation was changed
            if (trans == null && StringUtil.isEmpty(note)) {
                // no translation, no note
                newTrEntry = null;
            } else {
                newTrEntry = new TMXEntry(entry.getSrcText(), trans, changer, changeDate,
                        creator, creationDate, (StringUtil.isEmpty(note) ? null : note), isDefault, null);
            }
        }

        m_modifiedFlag = true;

        projectTMX.setTranslation(entry, newTrEntry, isDefault);

        /**
         * Calculate how to statistics should be changed.
         */
        int diff = prevTrEntry.translation == null ? 0 : -1;
        diff += trans == null ? 0 : +1;
        hotStat.numberofTranslatedSegments += diff;
    }

    public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
        Map.Entry<String, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<String, TMXEntry>> set = projectTMX.defaults.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<String, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
        Map.Entry<EntryKey, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<EntryKey, TMXEntry>> set = projectTMX.alternatives.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<EntryKey, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }
    
    public boolean isOrphaned(String source) {
        return !checkOrphanedCallback.existSourceInProject(source);
    }

    public boolean isOrphaned(EntryKey entry) {
        return !checkOrphanedCallback.existEntryInProject(entry);
    }

    public Map<String, ExternalTMX> getTransMemories() {
        return transMemories;
    }

    public Map<Language,ProjectTMX> getOtherTargetLanguageTMs() {
        return otherTargetLangTMs;
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
        if (cmdLine != null && cmdLine.length() > 0) {
            try {
                return (ITokenizer) this.getClass().getClassLoader().loadClass(cmdLine).newInstance();
            } catch (ClassNotFoundException e) {
                Log.log(e.toString());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (ITokenizer) projectPref.newInstance();
        } catch (Throwable e) {
            Log.log(e);
        }
        
        return new DefaultTokenizer();
    }

    /**
     * Set the tokenizer's behavior. Behaviors are prioritized:
     * <ol><li>Behavior specified on command line via <code>--ITokenizerBehavior</code>
     * and <code>--ITokenizerTargetBehavior</code>
     * <li>Behavior specified in OmegaT preferences</li>
     * <li>Per-tokenizer default setting</li>
     * </ol>
     * @param cmdLine Lucene {@link Version} specified on command line
     * @param tokenizer The tokenizer to configure
     */
    protected void configTokenizer(String cmdLine, ITokenizer tokenizer) {
        // Set from command line.
        if (cmdLine != null && cmdLine.length() > 0) {
            try {
                tokenizer.setBehavior(Version.valueOf(cmdLine));
                return;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        
        // Set from OmegaT prefs.
        String vString = Preferences.getPreferenceDefault(
                Preferences.TOK_BEHAVIOR_PREFIX + tokenizer.getClass().getName(),
                null);
         if (vString != null && vString.length() > 0) {
             try {
                 tokenizer.setBehavior(Version.valueOf(vString));
                 return;
             }  catch (Throwable e) {
                 throw new RuntimeException(e);
             }
         }
         
         // Use tokenizer default as last resort.
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getProjectFiles() {
        return Collections.unmodifiableList(projectFilesList);
    }

    /**
     * This method converts directory separators into unix-style. It required to have the same filenames in
     * the alternative translation in Windows and Unix boxes.
     * 
     * Also it can use --alternate-filename-from and --alternate-filename-to command line parameters for
     * change filename in entry key. It allows to have many versions of one file in one project.
     * 
     * @param filename
     *            filesystem's filename
     * @return normalized filename
     */
    protected String patchFileNameForEntryKey(String filename) {
        String f = Core.getParams().get("alternate-filename-from");
        String t = Core.getParams().get("alternate-filename-to");
        String fn = filename.replace('\\', '/');
        if (f != null && t != null) {
            fn = fn.replaceAll(f, t);
        }
        return fn;
    }

    protected class LoadFilesCallback extends ParseEntry {
        private FileInfo fileInfo;
        private String entryKeyFilename;

        private final Set<String> existSource;
        private final Set<EntryKey> existKeys;

        public LoadFilesCallback(final Set<String> existSource, final Set<EntryKey> existKeys) {
            super(m_config);
            this.existSource = existSource;
            this.existKeys = existKeys;
        }

        public void setCurrentFile(FileInfo fi) {
            fileInfo = fi;
            super.setCurrentFile(fi);
            entryKeyFilename = patchFileNameForEntryKey(fileInfo.filePath);
        }

        public void fileFinished() {
            super.fileFinished();

            fileInfo = null;
        }

        /**
         * {@inheritDoc}
         */
        protected void addSegment(String id, short segmentIndex, String segmentSource,
                Shortcuts shortcutDetails, String segmentTranslation, boolean segmentTranslationFuzzy,
                String comment, String prevSegment, String nextSegment, String path) {
            // if the source string is empty, don't add it to TM
            if (segmentSource.length() == 0 || segmentSource.trim().length() == 0) {
                throw new RuntimeException("Segment must not be empty");
            }

            EntryKey ek = new EntryKey(entryKeyFilename, segmentSource, id, prevSegment, nextSegment, path);

            SourceTextEntry srcTextEntry = new SourceTextEntry(ek, allProjectEntries.size() + 1, comment,
                    segmentTranslation, shortcutDetails);
            srcTextEntry.setSourceTranslationFuzzy(segmentTranslationFuzzy);
            allProjectEntries.add(srcTextEntry);
            fileInfo.entries.add(srcTextEntry);

            existSource.add(segmentSource);
            existKeys.add(srcTextEntry.getKey());
        }
    };

    private class TranslateFilesCallback extends TranslateEntry {
        private String currentFile;

        public TranslateFilesCallback() {
            super(m_config);
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
    };

    static class AlignFilesCallback implements IAlignCallback {
        public AlignFilesCallback(ProjectProperties props) {
            super();
            this.config = props;
        }

        Map<String, TMXEntry> data = new HashMap<String, TMXEntry>();
        private ProjectProperties config;

        @Override
        public void addTranslation(String id, String source, String translation, boolean isFuzzy, String path,
                IFilter filter) {
            if (source != null && translation != null) {
                ParseEntry.ParseEntryResult spr = new ParseEntry.ParseEntryResult();
                boolean removeSpaces = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
                String sourceS = ParseEntry.stripSomeChars(source, spr, config.isRemoveTags(), removeSpaces);
                String transS = ParseEntry.stripSomeChars(translation, spr, config.isRemoveTags(), removeSpaces);
                if (config.isSentenceSegmentingEnabled()) {
                    List<String> segmentsSource = Segmenter.segment(config.getSourceLanguage(), sourceS, null, null);
                    List<String> segmentsTranslation = Segmenter
                            .segment(config.getTargetLanguage(), transS, null, null);
                    if (segmentsTranslation.size() != segmentsSource.size()) {
                        if (isFuzzy) {
                            transS = "[" + filter.getFuzzyMark() + "] " + transS;
                        }
                        data.put(sourceS, new TMXEntry(sourceS, transS, true));
                    } else {
                        for (short i = 0; i < segmentsSource.size(); i++) {
                            String oneSrc = segmentsSource.get(i);
                            String oneTrans = segmentsTranslation.get(i);
                            if (isFuzzy) {
                                oneTrans = "[" + filter.getFuzzyMark() + "] " + oneTrans;
                            }
                            data.put(sourceS, new TMXEntry(oneSrc, oneTrans, true));
                        }
                    }
                } else {
                    if (isFuzzy) {
                        transS = "[" + filter.getFuzzyMark() + "] " + transS;
                    }
                    data.put(sourceS, new TMXEntry(sourceS, transS, true));
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

    static class FileNameComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            // Get the local collator and set its strength to PRIMARY
            Collator localCollator = Collator.getInstance(Locale.getDefault());
            localCollator.setStrength(Collator.PRIMARY);
            return localCollator.compare(o1, o2);
        }
    }

    void setOnlineMode() {
        if (!isOnlineMode) {
            Log.logInfoRB("VCS_ONLINE");
            Core.getMainWindow().displayWarningRB("VCS_ONLINE");
        }
        isOnlineMode = true;
    }

    void setOfflineMode() {
        if (isOnlineMode) {
            Log.logInfoRB("VCS_OFFLINE");
            Core.getMainWindow().displayWarningRB("VCS_OFFLINE");
        }
        isOnlineMode = false;
    }

}
