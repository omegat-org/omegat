/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2008      Alex Buloichik
 Portions copyright 2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
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

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.FileUtil;
import org.omegat.util.LFileCopy;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader;
import org.omegat.util.TMXWriter;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Loaded project implementation. Only translation could be changed after
 * project will be loaded and set by Core.setProject.
 * 
 * All components can read all data directly without synchronization. All
 * synchronization implemented inside RealProject.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Bartko Zoltan
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RealProject implements IProject
{
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(RealProject.class
            .getName());

    private ProjectProperties m_config;

    private boolean m_modifiedFlag;

    /** List of all segments in project. */
    private List<SourceTextEntry> allProjectEntries;
    
    private final StatisticsInfo hotStat = new StatisticsInfo();
    
    /**
     * Storage for all translation memories, which shouldn't be changed and
     * saved, i.e. for /tm/*.tmx files, aligned data from source files.
     */
    private final Map<String, List<TransMemory>> transMemories;

    /**
     * Storage for orphaned segments.
     */
    private Map<String, TransEntry> orphanedSegments;

    /**
     * Storage for translation for current project.
     */
    private Map<String, TransEntry> translations;

    /** Segments count in project files. */
    private List<FileInfo> projectFilesList;
    
    /**
     * Create new project instance.
     * 
     * @param props
     *                project properties
     * @param isNewProject
     *                true if project need to be created
     */
    public RealProject(final ProjectProperties props, final boolean isNewProject) {
        allProjectEntries = new ArrayList<SourceTextEntry>(4096);
        transMemories = new TreeMap<String, List<TransMemory>>();
        orphanedSegments = new HashMap<String, TransEntry>();
        translations = new HashMap<String, TransEntry>();
        
        if (isNewProject) {
            createProject(props);
        }
        loadProject(props);
        
        // make required collections unmodifiable
        allProjectEntries = Collections.unmodifiableList(allProjectEntries);
    }
    
    public void saveProjectProperties() throws IOException {
        ProjectFileStorage.writeProjectFile(m_config);
        Preferences.setPreference(Preferences.SOURCE_LOCALE, m_config.getSourceLanguage().toString());
        Preferences.setPreference(Preferences.TARGET_LOCALE, m_config.getTargetLanguage().toString());
    }
    
    /**
     * Load exist project in a "big" sense -- loads project's properties, glossaries,
     * tms, source files etc.
     * 
     * @param props properties for new project
     */
    private void loadProject(final ProjectProperties props) {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_LOAD_START"));
        UIThreadsUtil.mustNotBeSwingThread();
        
        // load new project
        try
        {
            Preferences.setPreference(Preferences.CURRENT_FOLDER, new File(
                    props.getProjectRoot()).getParentFile().getAbsolutePath());
            Preferences.save();
            
            m_config = props;
            
            Core.getMainWindow().showStatusMessageRB("CT_LOADING_PROJECT");
            
            loadSourceFiles();
            
            loadTranslations();
            
            // load in translation database files
            try {
                loadTM();
            } catch (IOException e) {
                Log.logErrorRB(e, "TF_TM_LOAD_ERROR");
                Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR");
                // allow project load to resume
            }

            // build word count
            String stat = CalcStandardStatistics.buildProjectStats(this,
                    hotStat);
            String fn = getProjectProperties().getProjectInternal()
                    + OConsts.STATS_FILENAME;
            Statistics.writeStat(fn, stat);
            
            // Project Loaded...
            Core.getMainWindow().showStatusMessageRB(null);
            
            m_modifiedFlag = false;
        }
        catch( Exception e )
        {
                Log.logErrorRB(e, "TF_LOAD_ERROR");
                Core.getMainWindow().displayErrorRB(e, "TF_LOAD_ERROR");
        }
        // Fix for bug 1571944 @author Henry Pijffers (henry.pijffers@saxnot.com)
        catch (OutOfMemoryError oome) {
            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            allProjectEntries.clear();
            transMemories.clear();
            translations.clear();
            orphanedSegments.clear();

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Log.logErrorRB("OUT_OF_MEMORY");
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB(
                    OStrings.getString("OUT_OF_MEMORY"),
                    OStrings.getString("TF_ERROR"));
            // Just quit, we can't help it anyway
            System.exit(0);
        }

        LOGGER.info(OStrings.getString("LOG_DATAENGINE_LOAD_END"));
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
     * Signals to the core thread that a project is being closed now,
     * and if it's still being loaded, core thread shouldn't throw
     * any error.
     */
    public void closeProject() {        
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CLOSE"));
    }
    
    /** Builds all translated files and creates fresh TM files. */
    public void compileProject()
            throws IOException, TranslationException
    {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_COMPILE_START"));
        UIThreadsUtil.mustNotBeSwingThread();

        // build 3 TMX files:
        // - OmegaT-specific, with inline OmegaT formatting tags
        // - TMX Level 1, without formatting tags
        // - TMX Level 2, with OmegaT formatting tags wrapped in TMX inline tags
        try
        {
            Map<String,String> tmx = TMXWriter.prepareTMXData(translations, new TreeMap<String, TransEntry>());
            
            // build TMX with OmegaT tags
            String fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.OMEGAT_TMX
                        + OConsts.TMX_EXTENSION;
            TMXWriter.buildTMXFile(fname, false, false, m_config, tmx);

            // build TMX level 1 compliant file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL1_TMX
                        + OConsts.TMX_EXTENSION;
            TMXWriter.buildTMXFile(fname, true, false, m_config, tmx);

            // build three-quarter-assed TMX level 2 file
            fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL2_TMX
                    + OConsts.TMX_EXTENSION;
            TMXWriter.buildTMXFile(fname, false, true, m_config, tmx);
        }
        catch (IOException e)
        {
            Log.logErrorRB("CT_ERROR_CREATING_TMX");
            Log.log(e);
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") +
                    "\n" +                                                      // NOI18N
                    e.getMessage());
        }
        
        // build mirror directory of source tree
        List<String> fileList = new ArrayList<String>(256);
        String srcRoot = m_config.getSourceRoot();
        String locRoot = m_config.getTargetRoot();
        StaticUtils.buildDirList(fileList, new File(srcRoot));
        
        for(String filename : fileList)
        {
            String destFileName = locRoot + filename.substring(srcRoot.length());
            File destFile = new File(destFileName);
            if (!destFile.exists())
            {
                // target directory doesn't exist - create it
                if (!destFile.mkdir())
                {
                    throw new IOException(
                            OStrings.getString("CT_ERROR_CREATING_TARGET_DIR") 
                            + destFileName);
                }
            }
        }
        
        // build translated files
        FilterMaster fm = FilterMaster.getInstance();
        
        fileList.clear();
        StaticUtils.buildFileList(fileList, new File(srcRoot), true);
        
        Set<File> processedFiles = new HashSet<File>();
        
        TranslateFilesCallback translateFilesCallback = new TranslateFilesCallback();
        
        for(String filename : fileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
	        Core.getMainWindow().showStatusMessageRB("CT_COMPILE_FILE_MX",
                    midName);

	    Language targetLang = getProjectProperties().getTargetLanguage();
            fm.translateFile(srcRoot, midName, targetLang, locRoot, processedFiles, translateFilesCallback);
        }
        Core.getMainWindow().showStatusMessageRB("CT_COMPILE_DONE_MX");

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);
        
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_COMPILE_END"));
    }
    
    /** Saves the translation memory and preferences */
    public void saveProject()
    {
        if (isProjectModified()) {
            forceSave(false);
        } else {
            LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_NONEED"));
        }
    }
    
    /** Does actually save the Project's TMX file and preferences. */
    private void forceSave(boolean corruptionDanger)
    {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_START"));
        UIThreadsUtil.mustNotBeSwingThread();
                
        Core.getAutoSave().disable();
        
        Preferences.save();
        
        String s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
        if (corruptionDanger)
        {
            s += OConsts.STATUS_RECOVER_EXTENSION;
        }
        else
        {
            // rename existing project file in case a fatal error
            //  is encountered during the write procedure - that way
            //  everything won't be lost
            File backup = new File(s + OConsts.BACKUP_EXTENSION);
            File orig = new File(s);
            if (orig.exists())
                orig.renameTo(backup);
        }

        try
        {
            saveProjectProperties();
            
            Map<String,String> tmx = TMXWriter.prepareTMXData(translations, orphanedSegments);            
            TMXWriter.buildTMXFile(s, false, false, m_config, tmx);
            m_modifiedFlag = false;
        }
        catch (IOException e)
        {
            Log.logErrorRB(e, "CT_ERROR_SAVING_PROJ");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_SAVING_PROJ");
            
            // try to rename backup file to original name
            if (!corruptionDanger)
            {
                s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
                File backup = new File(s + OConsts.BACKUP_EXTENSION);
                File orig = new File(s);
                if (backup.exists())
                    backup.renameTo(orig);
            }
        }

        // if successful, delete backup file
        if (!m_modifiedFlag && !corruptionDanger)
        {
            s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
            File backup = new File(s + OConsts.BACKUP_EXTENSION);
            if (backup.exists())
                backup.delete();
        }

        // update statistics
        String stat = CalcStandardStatistics.buildProjectStats(this, hotStat);
        String fn = getProjectProperties().getProjectInternal()
                + OConsts.STATS_FILENAME;
        Statistics.writeStat(fn, stat);

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
        
        Core.getAutoSave().enable();
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_SAVE_END"));
    }
   
    /**
     * Create new project.
     */
    private void createProject(final ProjectProperties newProps)
    {
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CREATE_START"));
        UIThreadsUtil.mustNotBeSwingThread();
        
        m_config = newProps;
        try
        {
            createDirectory(m_config.getProjectRoot(), null);
            createDirectory(m_config.getProjectInternal(), null);
            createDirectory(m_config.getSourceRoot(), "src");
            createDirectory(m_config.getGlossaryRoot(), "glos");
            createDirectory(m_config.getTMRoot(), "tm");
            createDirectory(m_config.getDictRoot(), "dictionary");
            createDirectory(m_config.getTargetRoot(), "target");
            
            saveProjectProperties();            
        }
        catch(IOException e)
        {
            // trouble in tinsletown...
            Log.logErrorRB(e, "CT_ERROR_CREATING_PROJECT");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_CREATING_PROJECT");
        }
        LOGGER.info(OStrings.getString("LOG_DATAENGINE_CREATE_END"));
    }
    
    /**
     * Create one of project directory and  
     * @param dir
     * @param dirType
     * @throws IOException
     */
    private void createDirectory(final String dir, final String dirType) throws IOException {
        File d = new File(dir);
        if (!d.isDirectory())
        {
            if (!d.mkdirs())
            {
                StringBuilder msg = new StringBuilder(OStrings
                        .getString("CT_ERROR_CREATE"));
                if (dirType != null) {
                    msg.append("\n(.../").append(dirType).append("/)");
                }
                throw new IOException(msg.toString());
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    // protected functions

    /** Finds and loads project's TMX file with translations (project_save.tmx). */
    private void loadTranslations()
    {
        final File tmxFile = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);
        try
        {
            if (!tmxFile.exists())
            {
                Log.logErrorRB("CT_ERROR_CANNOT_FIND_TMX", tmxFile.getAbsolutePath()); // NOI18N
                // nothing to do here
                return;
            }
        }
        catch(SecurityException se)
        {
            // file probably exists, but something's wrong
            Log.logErrorRB(se, "CT_ERROR_ACCESS_PROJECT_FILE");
            Core.getMainWindow().displayErrorRB(se, "CT_ERROR_ACCESS_PROJECT_FILE");
            return;
        }
        
        try
        {
            // recover existing translations
            // since the source files may have changed since the last time
            //  they were loaded, load each string then look for it's
            //  owner
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_TMX");
            loadTMXFile(tmxFile.getAbsolutePath(), "UTF-8", true); // NOI18N
        }
        catch (IOException e)
        {
            Log.logErrorRB(e, "CT_ERROR_LOADING_PROJECT_FILE");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_LOADING_PROJECT_FILE");
        }
    }

    /**
     * Load source files for project.
     * 
     * @param projectRoot project root dir
     */
    private void loadSourceFiles()
            throws IOException, InterruptedIOException, TranslationException
    {
        FilterMaster fm = FilterMaster.getInstance();
        
        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        Set<File> processedFiles = new HashSet<File>();
        
        projectFilesList = new ArrayList<FileInfo>();
        
        for (String filename : srcFileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(m_config.getSourceRoot().length());
            
            Core.getMainWindow().showStatusMessageRB("CT_LOAD_FILE_MX",
                    filepath);
            
            LoadFilesCallback loadFilesCallback = new LoadFilesCallback();
            
            FileInfo fi = new FileInfo();
            fi.filePath = filepath;
            
            loadFilesCallback.setCurrentFile(fi);
            
            boolean fileLoaded = fm.loadFile(filename, processedFiles, loadFilesCallback);
            
            if (fileLoaded && (fi.entries.size() > 0)) {
                projectFilesList.add(fi);
            }
        }
        Core.getMainWindow().showStatusMessageRB("CT_LOAD_SRC_COMPLETE");
    }
    
    /** Locates and loads external TMX files with legacy translations. */
    private void loadTM() throws IOException
    {
        File f = new File(m_config.getTMRoot());
        String[] fileList = f.list();
        for (String file : fileList) {
            String fname = file;
            int lastdot = fname.lastIndexOf('.');
            if (lastdot<0)
                lastdot = fname.length();
            String ext = fname.substring(lastdot);
            fname = m_config.getTMRoot();
            if (!fname.endsWith(File.separator))
                fname += File.separator;
            fname += file;
            
            if (ext.equalsIgnoreCase(OConsts.TMX_EXTENSION)) {
                loadTMXFile(fname, "UTF-8", false);
            } else if (ext.equalsIgnoreCase(OConsts.TMW_EXTENSION)) {
                loadTMXFile(fname, "ISO-8859-1", false);
            }
        }
    }
    
    /** 
     * Loads TMX file.
     * Either the one of the project with project's translation,
     * or the legacy ones.
     */
    private void loadTMXFile(String fname, String encoding, boolean isProject)
            throws IOException
    {
        TMXReader tmx = new TMXReader(encoding, m_config
                .getSourceLanguage(), m_config.getTargetLanguage(),
                m_config.isSentenceSegmentingEnabled());

        // Fix for bug 1583560 - force kill causes project_save.tmx destruction
        // Copy TMX file to temp file, so we can read the temp file,
        // and then it doesn't matter if it gets destroyed when doing force kills
        LFileCopy.copy(fname, fname + ".tmp");

        // Load the TMX from the temp file
        tmx.loadFile(fname + ".tmp", isProject);

        // Delete the temp file
        new File(fname + ".tmp").delete();

        int num = tmx.numSegments();
        List<StringEntry> strEntryList = new ArrayList<StringEntry>(num);

        // RFE 1001918 - backing up project's TMX upon successful read
        if( isProject )
        {
            File tmxFile = new File(fname);
            long fileMillis = tmxFile.lastModified();
            if (fileMillis==0L) // IO Error
                fileMillis = new Date().getTime();
            LFileCopy.copy(fname, fname+"."+millisToDateTime(fileMillis)+".bak");   // NOI18N
            
            FileUtil.removeOldBackups(tmxFile);
        }

        // TM for store entries, if not a project_save.tmx
        List<TransMemory> currentTM=null;
        
        // If a legacy TM, creating one
        // and adding to the list of legacy TMs
        if (!isProject) {
            String fn = new File(fname).getName();
            currentTM = transMemories.get(fn);
            if (currentTM == null) {
                // create new TM hash for this file
                currentTM = new ArrayList<TransMemory>(num);
                transMemories.put(fn, currentTM);
            }
        }
        
        Set<String> exist = null;
        if (isProject) {
            // create list of all exist sources
            exist = new HashSet<String>(allProjectEntries.size() / 2);
            for (SourceTextEntry ste : allProjectEntries) {
                exist.add(ste.getSrcText());
            }
        }

        for (int i=0; i<num; i++)
        {
            String src = tmx.getSourceSegment(i);
            String trans = tmx.getTargetSegment(i);
            if (StringUtil.isEmpty(trans)) {
                continue;
            }

            if (isProject) {
                if (exist.contains(src)) {
                    /* Entry found in source files - translation. */

                    translations.put(src, new TransEntry(trans));
                } else {
                    /* Entry found in source files - translation. */
                    orphanedSegments.put(src, new TransEntry(trans));
                }
            }
            else
            {
                // not in a project - remember this as a translation
                //	memory string and add it to near list
                StringEntry se = new StringEntry(src);
                se.setTranslation(trans); // external TMXes don't count
                strEntryList.add(se);
                
                /*
                 * Not project file - just TM from /tm/.
                 */
                currentTM.add(new TransMemory(src, trans));
            }
        }
    }
    
    /** Formats date (in milliseconds) to YYYYMMDDHHMM form. */
    private static String millisToDateTime(final long millis) {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }
    
    /**
     * {@inheritDoc}
     */
    public List<SourceTextEntry> getAllEntries() {
        return allProjectEntries;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<Entry<String, TransEntry>> getTranslationsSet() {
        return Collections.unmodifiableSet(translations.entrySet());
    }
    
    public TransEntry getTranslation(SourceTextEntry ste) {
        return translations.get(ste.getSrcText());
    }

    /**
     * Returns the active Project's Properties.
     */
    public ProjectProperties getProjectProperties()
    {
        return m_config;
    }
    
    /**
     * Returns whether the project was modified.
     */
    public boolean isProjectModified()
    {
        return m_modifiedFlag;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTranslation(final SourceTextEntry entry, String trans) {
        m_modifiedFlag = true;

        TransEntry prevTrEntry = translations.get(entry.getSrcText());
        if (StringUtil.isEmpty(trans)) {
            translations.remove(entry.getSrcText());
        } else {
            translations.put(entry.getSrcText(), new TransEntry(trans));
        }
        String prevTranslation = prevTrEntry != null ? prevTrEntry.translation
                : null;
        
        /**
         * Calculate how to statistics should be changed.
         */
        int diff = StringUtil.isEmpty(prevTranslation) ? 0 : -1;
        diff += StringUtil.isEmpty(trans) ? 0 : +1;
        hotStat.numberofTranslatedSegments += diff;
    }

    public Map<String, List<TransMemory>> getTransMemories() {
        return transMemories;
    }
    
    public Map<String, TransEntry> getOrphanedSegments() {
        return orphanedSegments;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getProjectFiles() {
        return Collections.unmodifiableList(projectFilesList);
    }
        
    private class LoadFilesCallback extends ParseEntry {  
        private FileInfo fileInfo;
        private List<TransMemory> tmForFile;

        public LoadFilesCallback() {
            super(m_config);
        }
        
        protected void setCurrentFile(FileInfo fi) {
            fileInfo = fi;
            tmForFile = null;
        }

        protected void addSegment(String id, short segmentIndex,
                String segmentSource, String segmentTranslation, String comment) {
            // if the source string is empty, don't add it to TM
            if (segmentSource.length() == 0
                    || segmentSource.trim().length() == 0)
                return;
            if (!StringUtil.isEmpty(segmentTranslation)) {
                translations.put(segmentSource, new TransEntry(
                        segmentTranslation));
            }
            SourceTextEntry srcTextEntry = new SourceTextEntry(segmentSource,
                    allProjectEntries.size() + 1);
            allProjectEntries.add(srcTextEntry);
            fileInfo.entries.add(srcTextEntry);
        }

        public void addFileTMXEntry(String source, String translation) {
            if (StringUtil.isEmpty(translation)) {
                return;
            }
            if (tmForFile == null) {
                tmForFile = new ArrayList<TransMemory>();
                transMemories.put(fileInfo.filePath, tmForFile);
            }
            tmForFile.add(new TransMemory(source, translation));
        }
    };

    private class TranslateFilesCallback extends TranslateEntry {
        public TranslateFilesCallback() {
            super(m_config);
        }

        protected String getSegmentTranslation(String id, int segmentIndex,
                String segmentSource) {
            TransEntry tr = translations.get(segmentSource);
            return tr != null ? tr.translation : segmentSource;
        }
    };
}
