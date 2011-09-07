/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008 Alex Buloichik
               2009-2010 Didier Briel
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.ITokenizer;
import org.omegat.core.matching.Tokenizer;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.UIThreadsUtil;

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
 */
public class RealProject implements IProject {
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(RealProject.class.getName());

    protected static final String AUTO_TMX_DIR = "auto/";

    protected final ProjectProperties m_config;

    private FileChannel lockChannel;
    private FileLock lock;

    private boolean m_modifiedFlag;

    /** List of all segments in project. */
    private List<SourceTextEntry> allProjectEntries = new ArrayList<SourceTextEntry>(4096);

    private final StatisticsInfo hotStat = new StatisticsInfo();

    private final ITokenizer sourceTokenizer, targetTokenizer;

    private DirectoryMonitor tmMonitor;
    /**
     * If project uses project-specific file filters, the filterMaster is set, containing the file filter settings
     */
    private FilterMaster filterMaster;
    /**
     * If project uses project-specific segmentation rules, the srx is set;
     */
    private SRX srx;

    /**
     * Storage for all translation memories, which shouldn't be changed and saved, i.e. for /tm/*.tmx files,
     * aligned data from source files.
     * 
     * This map recreated each time when files changed. So, you can free use it without thinking about
     * synchronization.
     */
    private Map<String, ExternalTMX> transMemories = new TreeMap<String, ExternalTMX>();

    private ProjectTMX projectTMX;

    /** Segments count in project files. */
    private final List<FileInfo> projectFilesList = new ArrayList<FileInfo>();

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
        m_config = props;

        sourceTokenizer = createTokenizer(true);
        targetTokenizer = createTokenizer(false);
    }

    public void saveProjectProperties() throws Exception {
        unlockProject();
        ProjectFileStorage.writeProjectFile(m_config);
        lockProject();
        Preferences.setPreference(Preferences.SOURCE_LOCALE, m_config.getSourceLanguage().toString());
        Preferences.setPreference(Preferences.TARGET_LOCALE, m_config.getTargetLanguage().toString());
    }

    /**
     * Create new project.
     */
    public void createProject() {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CREATE_START"));
        UIThreadsUtil.mustNotBeSwingThread();

        lockProject();

        try {
            createDirectory(m_config.getProjectRoot(), null);
            createDirectory(m_config.getProjectInternal(), null);
            createDirectory(m_config.getSourceRoot(), "src");
            createDirectory(m_config.getGlossaryRoot(), "glos");
            createDirectory(m_config.getTMRoot(), "tm");
            createDirectory(m_config.getDictRoot(), "dictionary");
            createDirectory(m_config.getTargetRoot(), "target");

            saveProjectProperties();

            loadTM();

            allProjectEntries = Collections.unmodifiableList(allProjectEntries);
        } catch (Exception e) {
            // trouble in tinsletown...
            Log.logErrorRB(e, "CT_ERROR_CREATING_PROJECT");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_CREATING_PROJECT");
        }
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CREATE_END"));
    }

    /**
     * Load exist project in a "big" sense -- loads project's properties, glossaries, tms, source files etc.
     */
    public void loadProject() {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_LOAD_START"));
        UIThreadsUtil.mustNotBeSwingThread();

        lockProject();

        // load new project
        try {
            Preferences.setPreference(Preferences.CURRENT_FOLDER, new File(m_config.getProjectRoot())
                    .getParentFile().getAbsolutePath());
            Preferences.save();

            Core.getMainWindow().showStatusMessageRB("CT_LOADING_PROJECT");

            // sets for collect exist entries for check orphaned
            Set<String> existSource = new HashSet<String>();
            Set<EntryKey> existKeys = new HashSet<EntryKey>();

            //set project specific file filters if they exist
            if (FilterMaster.projectConfigFileExists(m_config.getProjectInternal())) {
                this.filterMaster = FilterMaster.getProjectInstance(m_config.getProjectInternal());
            }
            
            //set project specific segmentation rules if they exist
            if (SRX.projectConfigFileExists(m_config.getProjectInternal())) {
                this.srx = SRX.getProjectSRX(m_config.getProjectInternal());
                Segmenter.srx = this.srx;
            } else {
                Segmenter.srx = SRX.getSRX();
            }

            Map<EntryKey, TMXEntry> sourceTranslations = new HashMap<EntryKey, TMXEntry>();
            loadSourceFiles(existSource, existKeys, sourceTranslations);

            loadTranslations(existSource, existKeys, sourceTranslations);

            existSource = null;
            existKeys = null;

            loadTM();

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

        LOGGER.info(OStrings.getString("LOG_DATAENGINE_LOAD_END"));
    }

    /**
     * Align project.
     */
    public Map<String, TMXEntry> align(final ProjectProperties props, final File translatedDir)
            throws Exception {
        FilterMaster fm = getActiveFilterMaster();

        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);

        AlignFilesCallback alignFilesCallback = new AlignFilesCallback();

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
        return true;
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
        tmMonitor.fin();
        unlockProject();
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CLOSE"));
    }

    /**
     * Lock omegat.project file against rename or move project.
     */
    protected void lockProject() {
        if (!RuntimePreferences.isProjectLockingEnabled()) {
            return;
        }
        try {
            File lockFile = new File(m_config.getProjectRoot(), OConsts.FILE_PROJECT);
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = lockChannel.lock();
        } catch (Exception ex) {
            Log.log(ex);
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
            lock.release();
            lockChannel.close();
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * Builds translated files corresponding to sourcePattern and creates fresh TM files.
     * 
     * @param sourcePattern
     *            The regexp of files to create
     * @throws IOException
     * @throws TranslationException
     */
    public void compileProject(String sourcePattern) throws IOException, TranslationException {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_COMPILE_START"));
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

            projectTMX.save(m_config, new File(fname), false, false, false);

            // build TMX level 1 compliant file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL1_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.save(m_config, new File(fname), true, false, false);

            // build three-quarter-assed TMX level 2 file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL2_TMX
                    + OConsts.TMX_EXTENSION;
            projectTMX.save(m_config, new File(fname), false, true, false);
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
        FilterMaster fm = getActiveFilterMaster();

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

        LOGGER.info(OStrings.getString("LOG_DATAENGINE_COMPILE_END"));
    }

    /** Saves the translation memory and preferences */
    public void saveProject() {
        if (!isProjectModified()) {
            LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_NONEED"));
            return;
        }

        LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_START"));
        UIThreadsUtil.mustNotBeSwingThread();

        Core.getAutoSave().disable();

        Preferences.save();

        String s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;

        // rename existing project file in case a fatal error
        // is encountered during the write procedure - that way
        // everything won't be lost
        File backup = FileUtil.getBackupFile(new File(s));
        File orig = new File(s);
        File newFile = new File(s + OConsts.NEWFILE_EXTENSION);

        try {
            saveProjectProperties();

            projectTMX.save(m_config, newFile, false, false, true);

            if (backup.exists()) {
                if (!backup.delete()) {
                    throw new IOException("Error delete backup file");
                }
            }

            if (orig.exists()) {
                if (!orig.renameTo(backup)) {
                    throw new IOException("Error rename old file to backup");
                }
            }

            if (!newFile.renameTo(orig)) {
                throw new IOException("Error rename new file to tmx");
            }

            m_modifiedFlag = false;
        } catch (Exception e) {
            Log.logErrorRB(e, "CT_ERROR_SAVING_PROJ");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_SAVING_PROJ");
        }

        // update statistics
        String stat = CalcStandardStatistics.buildProjectStats(this, hotStat);
        String fn = m_config.getProjectInternal() + OConsts.STATS_FILENAME;
        Statistics.writeStat(fn, stat);

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);

        Core.getAutoSave().enable();
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_END"));
    }

    /**
     * Create one of project directory and
     * 
     * @param dir
     * @param dirType
     * @throws IOException
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
    private void loadTranslations(final Set<String> existSource, final Set<EntryKey> existKeys,
            final Map<EntryKey, TMXEntry> sourceTranslations) throws Exception {

        final File tmxFile = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);

        ProjectTMX.CheckOrphanedCallback cb = new ProjectTMX.CheckOrphanedCallback() {
            public boolean existSourceInProject(String src) {
                return existSource.contains(src);
            }

            public boolean existEntryInProject(EntryKey key) {
                return existKeys.contains(key);
            }
        };

        try {
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_TMX");

            projectTMX = new ProjectTMX(m_config, tmxFile, cb, sourceTranslations);
            if (tmxFile.exists()) {
                // RFE 1001918 - backing up project's TMX upon successful read
                FileUtil.backupFile(tmxFile);
                FileUtil.removeOldBackups(tmxFile);
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "CT_ERROR_LOADING_PROJECT_FILE");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_LOADING_PROJECT_FILE");
        }
    }

    /**
     * Load source files for project.
     * 
     * @param projectRoot
     *            project root dir
     */
    private void loadSourceFiles(final Set<String> existSource, final Set<EntryKey> existKeys,
            final Map<EntryKey, TMXEntry> sourceTranslations) throws IOException, InterruptedIOException,
            TranslationException {
        long st = System.currentTimeMillis();
        FilterMaster fm = getActiveFilterMaster();

        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        Collections.sort(srcFileList, new FileNameComparator());

        for (String filename : srcFileList) {
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(m_config.getSourceRoot().length());

            Core.getMainWindow().showStatusMessageRB("CT_LOAD_FILE_MX", filepath);

            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(existSource, existKeys,
                    sourceTranslations);

            FileInfo fi = new FileInfo();
            fi.filePath = filepath;

            loadFilesCallback.setCurrentFile(fi);

            boolean fileLoaded = fm.loadFile(filename, new FilterContext(m_config), loadFilesCallback);

            loadFilesCallback.fileFinished();

            if (fileLoaded && (fi.entries.size() > 0)) {
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
     * Locates and loads external TMX files with legacy translations. Uses directory monitor for check file
     * updates.
     */
    private void loadTM() throws IOException {
        final File tmRoot = new File(m_config.getTMRoot());
        tmMonitor = new DirectoryMonitor(tmRoot, new DirectoryMonitor.Callback() {
            public void fileChanged(File file) {
                if (!file.getName().endsWith(OConsts.TMX_EXTENSION)) {
                    // not a TMX file
                    return;
                }
                // create new translation memories map
                Map<String, ExternalTMX> newTransMemories = new TreeMap<String, ExternalTMX>(transMemories);
                if (file.exists()) {
                    try {
                        ExternalTMX newTMX = new ExternalTMX(m_config, file);
                        newTransMemories.put(file.getPath(), newTMX);

                        if (FileUtil.computeRelativePath(tmRoot, file).startsWith(AUTO_TMX_DIR)) {
                            appendFromAutoTMX(newTMX);
                        }
                    } catch (Exception e) {
                        Log.logErrorRB(e, "TF_TM_LOAD_ERROR");
                        Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR");
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
                    if (!projectTMX.translationDefault.containsKey(e.source)) {
                        // translation not exist
                        projectTMX.translationDefault.put(e.source, e);
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

    public TMXEntry getTranslation(SourceTextEntry ste) {
        TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
        if (r == null) {
            r = projectTMX.getDefaultTranslation(ste.getSrcText());
        }
        return r;
    }

    public TMXEntry getDefaultTranslation(SourceTextEntry ste) {
        return projectTMX.getDefaultTranslation(ste.getSrcText());
    }

    public TMXEntry getMultipleTranslation(SourceTextEntry ste) {
        return projectTMX.getMultipleTranslation(ste.getKey());
    }

    /**
     * Returns the active Project's Properties.
     */
    public ProjectProperties getProjectProperties() {
        return m_config;
    }

    /**
     * Returns whether the project was modified.
     */
    public boolean isProjectModified() {
        return m_modifiedFlag;
    }

    /**
     * {@inheritDoc}
     */
    public void setTranslation(final SourceTextEntry entry, final String trans, String note, boolean isDefault) {
        String author = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                System.getProperty("user.name"));

        TMXEntry prevTrEntry = isDefault ? projectTMX.getDefaultTranslation(entry.getSrcText()) : projectTMX
                .getMultipleTranslation(entry.getKey());
        
        if ( note == null ) {
            //note could not be fetched from notes pane, because another sourcetextentry was already loaded, or no ste was loaded yet (on project refresh)
            //keep old note:
            if (prevTrEntry != null) {
                note = prevTrEntry.note;
            }
        }

        // don't change anything if nothing has changed
        if (prevTrEntry == null) {
            if ("".equals(trans) && "".equals(note)) {
                return;
            }
        } else {
            if ( (trans.equals(prevTrEntry.translation))
               && 
                 ( (note == null || note == "") && prevTrEntry.note == null || (note != null && note.equals(prevTrEntry.note)))
               )
            {
                return;
            }
        }

        m_modifiedFlag = true;

        TMXEntry te = new TMXEntry(entry.getSrcText(), trans, author, System.currentTimeMillis(), note);
        projectTMX.setTranslation(entry, te, isDefault);

        String prevTranslation = prevTrEntry != null ? prevTrEntry.translation : null;

        /**
         * Calculate how to statistics should be changed.
         */
        int diff = StringUtil.isEmpty(prevTranslation) ? 0 : -1;
        diff += StringUtil.isEmpty(trans) ? 0 : +1;
        hotStat.numberofTranslatedSegments += diff;
    }

    public Collection<TMXEntry> getAllTranslations() {
        List<TMXEntry> r = new ArrayList<TMXEntry>();

        synchronized (projectTMX) {
            r.addAll(projectTMX.translationDefault.values());
            r.addAll(projectTMX.translationMultiple.values());
        }

        return r;
    }

    public Collection<TMXEntry> getAllOrphanedTranslations() {
        List<TMXEntry> r = new ArrayList<TMXEntry>();

        synchronized (projectTMX) {
            r.addAll(projectTMX.orphanedDefault.values());
            r.addAll(projectTMX.orphanedMultiple.values());
        }

        return r;
    }

    public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
        if (projectTMX.translationDefault == null) {
            return;
        }
        Map.Entry<String, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<String, TMXEntry>> set = projectTMX.translationDefault.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<String, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
        Map.Entry<EntryKey, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<EntryKey, TMXEntry>> set = projectTMX.translationMultiple.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<EntryKey, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public void iterateByOrphanedDefaultTranslations(DefaultTranslationsIterator it) {
        Map.Entry<String, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<String, TMXEntry>> set = projectTMX.orphanedDefault.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<String, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public void iterateByOrphanedMultipleTranslations(MultipleTranslationsIterator it) {
        Map.Entry<EntryKey, TMXEntry>[] entries;
        synchronized (projectTMX) {
            Set<Map.Entry<EntryKey, TMXEntry>> set = projectTMX.orphanedMultiple.entrySet();
            entries = set.toArray(new Map.Entry[set.size()]);
        }
        for (Map.Entry<EntryKey, TMXEntry> en : entries) {
            it.iterate(en.getKey(), en.getValue());
        }
    }

    public Map<String, ExternalTMX> getTransMemories() {
        return transMemories;
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
     * Create tokenizer by class specified in command line, or by default class.
     * 
     * @param forSource
     *            true if tokenizer for source language
     * @return tokenizer implementation
     */
    protected ITokenizer createTokenizer(final boolean forSource) {
        String className;
        if (forSource) {
            className = Core.getParams().get("ITokenizer");
        } else {
            className = Core.getParams().get("ITokenizerTarget");
        }
        ITokenizer t = null;
        try {
            if (className != null) {
                for (Class<?> c : PluginUtils.getTokenizerClasses()) {
                    if (c.getName().equals(className)) {
                        t = (ITokenizer) c.newInstance();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        if (t == null) {
            if (forSource) {
                t = new Tokenizer();
            } else {
                t = sourceTokenizer;
            }
        }
        if (forSource) {
            Log.log("Source tokenizer: " + t.getClass().getName());
        } else {
            Log.log("Target tokenizer: " + t.getClass().getName());
        }
        return t;
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
        private final Map<EntryKey, TMXEntry> sourceTranslations;

        private List<TMXEntry> fileTMXentries;
        
        public LoadFilesCallback(final Set<String> existSource, final Set<EntryKey> existKeys,
                final Map<EntryKey, TMXEntry> sourceTranslations) {
            super(m_config);
            this.existSource = existSource;
            this.existKeys = existKeys;
            this.sourceTranslations = sourceTranslations;
        }

        public void setCurrentFile(FileInfo fi) {
            fileInfo = fi;
            fileTMXentries = new ArrayList<TMXEntry>();
            super.setCurrentFile(fi);
            entryKeyFilename = patchFileNameForEntryKey(fileInfo.filePath);
        }

        public void fileFinished() {
            super.fileFinished();

            if (fileTMXentries.size() > 0) {
                ExternalTMX tmx = new ExternalTMX(fileInfo.filePath, fileTMXentries);
                transMemories.put(tmx.getName(), tmx);
            }

            fileTMXentries = null;
            fileInfo = null;
        }

        /**
         * {@inheritDoc}
         */
        protected void addSegment(String id, short segmentIndex, String segmentSource,
                String segmentTranslation, String comment, String prevSegment, String nextSegment, String path) {
            // if the source string is empty, don't add it to TM
            if (segmentSource.length() == 0 || segmentSource.trim().length() == 0) {
                throw new RuntimeException("Segment must not be empty");
            }

            EntryKey ek = new EntryKey(entryKeyFilename, segmentSource, id, prevSegment, nextSegment, path);

            if (!StringUtil.isEmpty(segmentTranslation)) {
                // projectTMX doesn't exist yet, so we have to store in temp map
                sourceTranslations.put(ek, new TMXEntry(segmentSource, segmentTranslation, null, 0, null));
            }
            SourceTextEntry srcTextEntry = new SourceTextEntry(ek, allProjectEntries.size() + 1, comment);
            allProjectEntries.add(srcTextEntry);
            fileInfo.entries.add(srcTextEntry);

            existSource.add(segmentSource);
            existKeys.add(srcTextEntry.getKey());
        }

        public void addFileTMXEntry(String source, String translation) {
            if (StringUtil.isEmpty(translation)) {
                return;
            }
            fileTMXentries.add(new TMXEntry(source, translation, null, 0, null));
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
        Map<String, TMXEntry> data = new HashMap<String, TMXEntry>();

        public void addTranslation(String id, String source, String translation, boolean isFuzzy,
                String path, IFilter filter) {
            if (source != null && translation != null) {
                ParseEntry.ParseEntryResult spr = new ParseEntry.ParseEntryResult();
                String sourceS = ParseEntry.stripSomeChars(source, spr);
                String transS = ParseEntry.stripSomeChars(translation, spr);
                if (isFuzzy) {
                    transS = "[" + filter.getFuzzyMark() + "] " + transS;
                }

                data.put(sourceS, new TMXEntry(sourceS, transS, null, 0, null));
            }
        }
    }

    static class FileNameComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            // Get the local collator and set its strength to PRIMARY
            Collator localCollator = Collator.getInstance(Locale.getDefault());
            localCollator.setStrength(Collator.PRIMARY);
            return localCollator.compare(o1, o2);
        }
    }
    /**
     * returns project FilterMaster if it exists, else returns null (this means: not using project-specific filters!)
     */
    public FilterMaster getFilterMaster() {
        return this.filterMaster;
    }

    /**
     * Sets the filter config to the project, or removes it. Creates or deletes config file if necessary and (dis)associates FilterMaster to project. Use this to create project specific file filters. 
     * 
     * @param filters the filters config. When null, project specific config is removed.
     */
    public void setConfig(Filters filters) {
        if (filters == null) {
            if (this.filterMaster != null) {
                this.filterMaster.deleteConfig();
                this.filterMaster = null;
            }
            return;
        }
        if (this.filterMaster == null) {
            this.filterMaster = FilterMaster.getProjectInstance(this.getProjectProperties().getProjectInternal());
        }
        this.filterMaster.setConfig(filters);
        this.filterMaster.saveConfig();
    }
    
    /**
     * Returns the filtermaster to use: the projects filter master (if it is set), else the default filtermaster
     * @return the filtermaster to use
     */
    private FilterMaster getActiveFilterMaster() {
        //get project specific file filters if they exist, else get normal filters
        if (this.filterMaster == null) {
            return FilterMaster.getInstance();
        }
        return this.filterMaster;
    }

    public SRX getSRX() {
        return this.srx;
    }
    
    public void setSRX(SRX srx) {
        if (srx == null) {
            if (this.srx != null) {
                this.srx.deleteConfig();
            }
            Segmenter.srx = SRX.getSRX();
        } else {
            Segmenter.srx = srx;
        }
        this.srx = srx;
    }
    
    public String getSegmentationConfigDir() {
        return this.m_config.getProjectInternal();
    }
}
