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
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StaticUtils;
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

    /** Unique segments list. Used for save TMX. TODO: remove */
    private List<StringEntry> m_strEntryList;

    /** List of all segments in project. */
    private List<SourceTextEntry> m_srcTextEntryArray;

    /** the list of legacy TMX files, each object is the list of string entries */
    private List<LegacyTM> m_legacyTMs;

    /** Entries from all /tm/*.tmx files and orphaned from project_save.tmx. TODO: remove*/
    private List<TransMemory> m_tmList;
    
    /** Orphaned entries from project_save.tmx. TODO: remove*/
    private List<TransMemory> m_orphanedList;

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
        m_strEntryList = new ArrayList<StringEntry>();
        m_srcTextEntryArray = new ArrayList<SourceTextEntry>(4096);
        m_tmList = new ArrayList<TransMemory>();
        m_legacyTMs = new ArrayList<LegacyTM>();
        m_orphanedList = new ArrayList<TransMemory>();
        
        if (isNewProject) {
            createProject(props);
        }
        loadProject(props);
        
        // make required collections unmodifiable
        m_srcTextEntryArray = Collections.unmodifiableList(m_srcTextEntryArray);
        m_legacyTMs = Collections.unmodifiableList(m_legacyTMs);
        m_tmList = Collections.unmodifiableList(m_tmList);
        m_orphanedList = Collections.unmodifiableList(m_orphanedList);
        m_strEntryList = Collections.unmodifiableList(m_strEntryList);
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
        LoadContext context = new LoadContext();
        try
        {
            Preferences.setPreference(Preferences.CURRENT_FOLDER, new File(
                    props.getProjectRoot()).getParentFile().getAbsolutePath());
            Preferences.save();
            
            m_config = props;
            
            Core.getMainWindow().showStatusMessageRB("CT_LOADING_PROJECT");
            
            loadSourceFiles(context);
            
            loadTranslations(context);
            
            // load in translation database files
            try
            {
                loadTM(context);
            }
            catch (IOException e)
            {
                Log.logErrorRB(e, "TF_TM_LOAD_ERROR");
                Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR");
                // allow project load to resume
            }

            // build word count
            String stat = CalcStandardStatistics.buildProjectStats(
                    m_srcTextEntryArray, m_config);
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
            context = null;
            m_strEntryList.clear();
            m_srcTextEntryArray.clear();
            m_legacyTMs.clear();
            m_tmList.clear();
            m_orphanedList.clear();

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
        StatisticsInfo info = new StatisticsInfo();
        info.numberOfUniqueSegments = m_strEntryList.size();
        info.numberofTranslatedSegments = numberofTranslatedSegments;
        info.numberOfSegmentsTotal = m_srcTextEntryArray.size();
        return info;
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
            Map<String,String> tmx = TMXWriter.prepareTMXData(false, m_strEntryList, m_orphanedList, false, null);
            
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
        
        LoadContext context = new LoadContext();
        // prepare context data. TODO: remove if not need in future
        for (int i = 0; i < m_srcTextEntryArray.size(); i++) {
            SourceTextEntry ste = m_srcTextEntryArray.get(i);
            StringEntry se = ste.getStrEntry();
            StringEntry hse = context.m_strEntryHash.get(se.getSrcText());
            if (hse == null) {
                context.m_strEntryHash.put(se.getSrcText(), se);
            }
        }
        
        TranslateFilesCallback translateFilesCallback = new TranslateFilesCallback(context);
        
        for(String filename : fileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
	        Core.getMainWindow().showStatusMessageRB("CT_COMPILE_FILE_MX",
                    midName);

            fm.translateFile(srcRoot, midName, locRoot, processedFiles, translateFilesCallback);
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
            
            Map<String,String> tmx = TMXWriter.prepareTMXData(true, m_strEntryList, m_orphanedList, false, null);            
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
        String stat = CalcStandardStatistics.buildProjectStats(
                m_srcTextEntryArray, m_config);
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
    private void loadTranslations(final LoadContext context)
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
            loadTMXFile(context, tmxFile.getAbsolutePath(), "UTF-8", true); // NOI18N
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
    private void loadSourceFiles(final LoadContext context)
            throws IOException, InterruptedIOException, TranslationException
    {
        FilterMaster fm = FilterMaster.getInstance();
        
        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        Set<File> processedFiles = new HashSet<File>();
        
        List<FileInfo> pfl = new ArrayList<FileInfo>();
        
        int firstEntry = 0;
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
            
            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(context);
            
            ProjectFileData m_curFile = new ProjectFileData();
            m_curFile.name = filename;
            m_curFile.firstEntry = m_srcTextEntryArray.size();
            
            loadFilesCallback.setCurrentFile(m_curFile);
            
            boolean fileLoaded = fm.loadFile(filename, processedFiles, loadFilesCallback);
            
            m_curFile.lastEntry = m_srcTextEntryArray.size()-1;

            if( fileLoaded && (m_curFile.lastEntry>=m_curFile.firstEntry) )
            {
                FileInfo fi=new FileInfo();
                fi.filePath=filepath;
                fi.firstEntryIndex=m_srcTextEntryArray.size();
                fi.firstEntryIndexInGlobalList=firstEntry;
                fi.size=m_srcTextEntryArray.size()-firstEntry;
                pfl.add(fi);
                firstEntry=m_srcTextEntryArray.size();
            }
        }
        projectFilesList = Collections.unmodifiableList(pfl);
        Core.getMainWindow().showStatusMessageRB("CT_LOAD_SRC_COMPLETE");
    }
    
    /** Locates and loads external TMX files with legacy translations. */
    private void loadTM(final LoadContext context) throws IOException
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
            
            if (ext.equalsIgnoreCase(OConsts.TMX_EXTENSION))
                loadTMXFile(context, fname, "UTF-8", false); // NOI18N
            else if (ext.equalsIgnoreCase(OConsts.TMW_EXTENSION))
                loadTMXFile(context, fname, "ISO-8859-1", false); // NOI18N
        }
    }
    
    /** 
     * Loads TMX file.
     * Either the one of the project with project's translation,
     * or the legacy ones.
     */
    private void loadTMXFile(final LoadContext context, String fname, String encoding, boolean isProject)
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
        List<StringEntry> strOrphaneList = null;

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

        // If a legacy TM, creating one
        // and adding to the list of legacy TMs
        if( isProject )
        {
            strOrphaneList = new ArrayList<StringEntry>();
            LegacyTM tm = new LegacyTM(
                    OStrings.getString("CT_ORPHAN_STRINGS"), strOrphaneList);
            m_legacyTMs.add(tm);
        }
        else
        {
            LegacyTM tm = new LegacyTM(new File(fname).getName(), strEntryList);
            m_legacyTMs.add(tm);
        }

        for (int i=0; i<num; i++)
        {
            String src = tmx.getSourceSegment(i);
            String trans = tmx.getTargetSegment(i);

            if (isProject)
            {
                StringEntry se = context.m_strEntryHash.get(src);
                if( se==null )
                {
                    // loading a project save file and the
                    //	old entry can't be found - source files
                    //	must have changed
                    // remember it anyways
                    TransMemory tm = new TransMemory(src, trans, fname);
                    m_orphanedList.add(tm);
                    m_tmList.add(tm);
                    se = new StringEntry(src);
                    se.setTranslation(trans); // orphane translation don't count
                    strOrphaneList.add(se);
                }
                else
                {
                    numberofTranslatedSegments += se.setTranslation(trans);
                }
            }
            else
            {
                // not in a project - remember this as a translation
                //	memory string and add it to near list
                m_tmList.add(new TransMemory(src, trans, fname));
                StringEntry se = new StringEntry(src);
                se.setTranslation(trans); // external TMXes don't count
                strEntryList.add(se);
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
        return m_srcTextEntryArray;
    }

    ////////////////////////////////////////////////////////
    // simple project info
    
    /**
     * {@inheritDoc}
     */
    public List<TransMemory>	getTransMemory()
    { return m_tmList;		}
    
    /////////////////////////////////////////////////////////
    
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
    

    /** The number of unique translated segments. */
    private int numberofTranslatedSegments;

    /**
     * {@inheritDoc}
     */
    public List<StringEntry> getUniqueEntries() {
        return Collections.unmodifiableList(new ArrayList<StringEntry>(m_strEntryList));
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTranslation(final SourceTextEntry entry, final String trans) {
        numberofTranslatedSegments += entry.setTranslation(trans);
        m_modifiedFlag = true;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<LegacyTM> getMemory() {
        return m_legacyTMs;
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getProjectFiles() {
        return projectFilesList;
    }
        
    private class LoadFilesCallback extends ParseEntry {  
        private final LoadContext context;
        /**
         * Keeps track of file specific data to feed to SourceTextEntry objects
         * so they can have a bigger picture of what's where.
         */
        private ProjectFileData m_curFile;
        private LegacyTM legacyFileTM;

        public LoadFilesCallback(final LoadContext context) {
            super(m_config);
            this.context = context;
        }
        
        protected void setCurrentFile(ProjectFileData file) {
            m_curFile = file;            
            legacyFileTM = null;
        }
        /**
         * Processes a single entry. This method doesn't perform any changes on
         * the passed string.
         * 
         * @param src
         *                Translatable source string
         * @return Translation of the source string. If there's no translation,
         *         returns the source string itself.
         */
        protected String processSingleEntry(String src) {
            StringEntry se = context.m_strEntryHash.get(src);
            addEntry(src);

            if (se == null) {
                return src;
            } else {
                String s = se.getTranslation();
                if (s == null || s.length() == 0)
                    s = src;
                return s;
            }
        }
        /**
         * Creates a new Source Text Entry
         * (mapping between source file and a TM).
         * Also if there's no entry for <code>srcText</code> string yet,
         * then adds a new String Entry to internal in-memory TM.
         */
        private void addEntry(String srcText)
        {
            // if the source string is empty, don't add it to TM
            if( srcText.length()==0 || srcText.trim().length()==0 )
                return;
            
            StringEntry strEntry = context.m_strEntryHash.get(srcText);
            if (strEntry == null)
            {
                // entry doesn't exist yet - create and store it
                strEntry = new StringEntry(srcText);
                m_strEntryList.add(strEntry);
                context.m_strEntryHash.put(srcText, strEntry);
            }
            SourceTextEntry srcTextEntry = new SourceTextEntry(strEntry, m_curFile, m_srcTextEntryArray.size());
            m_srcTextEntryArray.add(srcTextEntry);
        }

        protected void addSegment(String id, int segmentIndex,
                String segmentSource, String segmentTranslation, String comment) {
            // if the source string is empty, don't add it to TM
            if (segmentSource.length() == 0
                    || segmentSource.trim().length() == 0)
                return;
            StringEntry strEntry = context.m_strEntryHash.get(segmentSource);
            if (strEntry == null) {
                // entry doesn't exist yet - create and store it
                strEntry = new StringEntry(segmentSource);
                strEntry.setTranslation(segmentTranslation);
                m_strEntryList.add(strEntry);
                context.m_strEntryHash.put(segmentSource, strEntry);
            }
            SourceTextEntry srcTextEntry = new SourceTextEntry(strEntry, m_curFile, m_srcTextEntryArray.size());
            m_srcTextEntryArray.add(srcTextEntry);
        }
        @Override
        public String getTranslation(String id, String source) {
            StringEntry se = context.m_strEntryHash.get(source);
            
            if (se == null) {
                return source;
            } else {
                String s = se.getTranslation();
                if (s == null || s.length() == 0)
                    s = source;
                return s;
            }
        }

        public void addLegacyTMXEntry(String source, String translation) {
            if (legacyFileTM == null) {
                String fn = StaticUtils.makeFilenameRelative(m_curFile.name,
                        m_config.getSourceRoot());
                legacyFileTM = new LegacyTM(fn, new ArrayList<StringEntry>());
                getMemory().add(legacyFileTM);
            }
            StringEntry en = new StringEntry(source);
            en.setTranslation(translation);
            legacyFileTM.getStrings().add(en);
        }
    };

    private class TranslateFilesCallback extends ParseEntry {
        private final LoadContext context;
        public TranslateFilesCallback(final LoadContext context) {
            super(m_config);
            this.context = context;
        }
        /**
         * Processes a single entry. This method doesn't perform any changes on
         * the passed string.
         * 
         * @param src
         *                Translatable source string
         * @return Translation of the source string. If there's no translation,
         *         returns the source string itself.
         */
        protected String processSingleEntry(String src) {
            StringEntry se = context.m_strEntryHash.get(src);

            if (se == null) {
                return src;
            } else {
                String s = se.getTranslation();
                if (s == null || s.length() == 0)
                    s = src;
                return s;
            }
        }
        public void addLegacyTMXEntry(String source, String translation) {
        }
    };

    /**
     * Class for store some information which required only on loading.
     * 
     * @author Alex Buloichik (alex73mail@gmail.com)
     */
    private class LoadContext {
        /** maps text to strEntry obj */
        Map<String, StringEntry> m_strEntryHash = new HashMap<String, StringEntry>(
                4096);
    }
}
