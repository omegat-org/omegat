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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.SourceTextEntry;
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
 * CommandThread is a thread to asynchronously do the stuff.
 * 
 * This is most important part for synchronization. All components which uses
 * project data should be synchronized around IDataEngine instance. It will be
 * good to change synchronization to read/write mode in future.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Bartko Zoltan
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CommandThread implements IDataEngine
{
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(CommandThread.class
            .getName());
    /**
     * One and only CommandThread object in the OmegaT.
     * <p>
     * <small>
     * mihmax: Threading nightmare, IMHO.
     * </small>
     */
    public static CommandThread core;
    
    public CommandThread()
    {                
        m_config = null;
        m_strEntryHash = new HashMap<String, StringEntry>(4096);
        m_strEntryList = new ArrayList<StringEntry>();
        m_srcTextEntryArray = new ArrayList<SourceTextEntry>(4096);
        m_tmList = new ArrayList<TransMemory>();
        m_legacyTMs = new ArrayList<LegacyTM>();
        m_orphanedList = new ArrayList<TransMemory>();
        m_modifiedFlag = false;
    }
    
    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////
    // message handling for external requests
   
    /**
     * Clears all hashes, lists etc.
     */
    private void cleanUp()
    {
        m_strEntryHash.clear();
        
        m_legacyTMs.clear();
        
        m_tmList.clear();
        m_orphanedList.clear();
        
        m_strEntryList.clear();
        m_srcTextEntryArray.clear();
        
        numberofTranslatedSegments = 0;
        
        // reset token list cache
        Core.getTokenizer().clearCache();
    }
    
    public void saveProjectProperties() throws IOException {
        ProjectFileStorage.writeProjectFile(m_config);
        Preferences.setPreference(Preferences.SOURCE_LOCALE, m_config.getSourceLanguage().toString());
        Preferences.setPreference(Preferences.TARGET_LOCALE, m_config.getTargetLanguage().toString());
    }
    /**
     * {@inheritDoc}
     * TODO: change to File parameter
     */
    public synchronized void loadProject(final ProjectProperties props) throws Exception {
        UIThreadsUtil.mustNotBeSwingThread();
        // load new project
        try
        {
            m_config = props;
            
            cleanUp();
            
            Core.getMainWindow().showStatusMessage(OStrings.getString("CT_LOADING_PROJECT"));
            
            projectClosing = false;
            
            loadSourceFiles(props.getProjectRoot());
            
            loadTranslations();
            
            projectLoaded = true;
            
            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
                          
            // load in translation database files
            try
            {
                loadTM();
            }
            catch (IOException e)
            {
                Log.logErrorRB(e, "TF_TM_LOAD_ERROR");
                Core.getMainWindow().displayErrorRB(e, "TF_TM_LOAD_ERROR");
                // allow project load to resume
            }
            
            // build word count
            Statistics.buildProjectStats(m_strEntryList, m_srcTextEntryArray, m_config, numberofTranslatedSegments);
            
            // Project Loaded...
            Core.getMainWindow().showStatusMessage("");
        }
        catch( Exception e )
        {
            // any error
            if( !projectClosing ) {
                Log.logErrorRB(e, "TF_LOAD_ERROR");
                Core.getMainWindow().displayErrorRB(e, "TF_LOAD_ERROR");
            }
            else
                Log.logRB("CT_CANCEL_LOAD");               // NOI18N
        }
        // Fix for bug 1571944 @author Henry Pijffers (henry.pijffers@saxnot.com)
        catch (OutOfMemoryError oome) {
            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            m_strEntryHash.clear();
            m_strEntryHash = null;
            m_strEntryList.clear();
            m_strEntryList = null;
            m_srcTextEntryArray.clear();
            m_srcTextEntryArray = null;
            m_legacyTMs.clear();
            m_legacyTMs = null;
            m_tmList.clear();
            m_tmList = null;
            m_orphanedList.clear();
            m_orphanedList = null;

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Log.logErrorRB("OUT_OF_MEMORY");
            Log.log(oome);

            // Just quit, we can't help it anyway
            System.exit(0);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized boolean isProjectLoaded() {
        return projectLoaded;
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized StatisticsInfo getStatistics() {
        StatisticsInfo info = new StatisticsInfo();
        info.numberOfUniqueSegments = m_strEntryList.size();
        info.numberofTranslatedSegments = numberofTranslatedSegments;
        info.numberOfSegmentsTotal = m_srcTextEntryArray.size();
        return info;
    }
    
    private boolean projectClosing = false;
    /**
     * Signals to the core thread that a project is being closed now,
     * and if it's still being loaded, core thread shouldn't throw
     * any error.
     */
    public synchronized void closeProject() {
        projectLoaded = false;
        projectClosing = true;
        cleanUp();

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
    }
    
    /** Builds all translated files and creates fresh TM files. */
    public synchronized void compileProject()
            throws IOException, TranslationException
    {
        // build 3 TMX files:
        // - OmegaT-specific, with inline OmegaT formatting tags
        // - TMX Level 1, without formatting tags
        // - TMX Level 2, with OmegaT formatting tags wrapped in TMX inline tags
        try
        {
                // build TMX with OmegaT tags
                String fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.OMEGAT_TMX
                        + OConsts.TMX_EXTENSION;
                TMXWriter.buildTMXFile(fname, false, false, false, m_config, m_strEntryList, m_orphanedList);

                // build TMX level 1 compliant file
                fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL1_TMX
                        + OConsts.TMX_EXTENSION;
                TMXWriter.buildTMXFile(fname, true, false, false, m_config, m_strEntryList, m_orphanedList);

                // build three-quarter-assed TMX level 2 file
                fname = m_config.getProjectRoot() + m_config.getProjectName() + OConsts.LEVEL2_TMX
                        + OConsts.TMX_EXTENSION;
                TMXWriter.buildTMXFile(fname, false, false, true, m_config, m_strEntryList, m_orphanedList);
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
        
        for(String filename : fileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
            String message = StaticUtils.format(OStrings.getString("CT_COMPILE_FILE_MX"), new Object[] { midName });
	    Core.getMainWindow().showStatusMessage(message);

            fm.translateFile(srcRoot, midName, locRoot, processedFiles);
        }
        Core.getMainWindow().showStatusMessage(OStrings.getString("CT_COMPILE_DONE_MX"));

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);
    }
    
    /** Saves the translation memory and preferences */
    public synchronized void saveProject()
    {
        if( isProjectModified() )
            forceSave(false);
    }
    
    public synchronized void markAsDirty()
    {
        m_modifiedFlag = true;
    }
    
    /** Does actually save the Project's TMX file and preferences. */
    private void forceSave(boolean corruptionDanger)
    {
        UIThreadsUtil.mustNotBeSwingThread();
        
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
            
            TMXWriter.buildTMXFile(s, false, true, false, m_config, m_strEntryList, m_orphanedList);
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
        Statistics.buildProjectStats(m_strEntryList, m_srcTextEntryArray, m_config, numberofTranslatedSegments);

        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
    }
    
    /**
     * Creates a new Source Text Entry
     * (mapping between source file and a TM).
     * Also if there's no entry for <code>srcText</code> string yet,
     * then adds a new String Entry to internal in-memory TM.
     */
    public synchronized void addEntry(String srcText)
    {
        // if the source string is empty, don't add it to TM
        if( srcText.length()==0 || srcText.trim().length()==0 )
            return;
        
        StringEntry strEntry = m_strEntryHash.get(srcText);
        if (strEntry == null)
        {
            // entry doesn't exist yet - create and store it
            strEntry = new StringEntry(srcText);
            m_strEntryList.add(strEntry);
            m_strEntryHash.put(srcText, strEntry);
        }
        SourceTextEntry srcTextEntry = new SourceTextEntry(strEntry, m_curFile, m_srcTextEntryArray.size());
        m_srcTextEntryArray.add(srcTextEntry);
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized void createProject(final File newProjectDir, final ProjectProperties newProps)
    {
        UIThreadsUtil.mustBeSwingThread();
        m_config = newProps;
        try
        {
            createDirectory(m_config.getProjectRoot(), null);
            createDirectory(m_config.getProjectInternal(), null);
            createDirectory(m_config.getSourceRoot(), "src");
            createDirectory(m_config.getGlossaryRoot(), "glos");
            createDirectory(m_config.getTMRoot(), "tm");
            createDirectory(m_config.getTargetRoot(), "target");
            
            saveProjectProperties();
            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.CREATE);
        }
        catch(IOException e)
        {
            // trouble in tinsletown...
            Log.logErrorRB(e, "CT_ERROR_CREATING_PROJECT");
            Core.getMainWindow().displayErrorRB(e, "CT_ERROR_CREATING_PROJECT");
        }
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
        File proj;
        try
        {
            proj = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);
            if (!proj.exists())
            {
                Log.logErrorRB("CT_ERROR_CANNOT_FIND_TMX", new Object[] {proj}); // NOI18N
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
            Core.getMainWindow().showStatusMessage(OStrings.getString("CT_LOAD_TMX"));
            loadTMXFile(proj.getAbsolutePath(), "UTF-8", true); // NOI18N
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
    private void loadSourceFiles(String projectRoot)
            throws IOException, InterruptedIOException, TranslationException
    {
        FilterMaster fm = FilterMaster.getInstance();
        
        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        Set<File> processedFiles = new HashSet<File>();
        
        projectFilesList.clear();
        
        int firstEntry = 0;
        for (String filename : srcFileList)
        {
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(m_config.getSourceRoot().length());
            
            Core.getMainWindow().showStatusMessage(StaticUtils.format(
                OStrings.getString("CT_LOAD_FILE_MX"), new Object[] {filepath}));
            
            m_curFile = new ProjectFileData();
            m_curFile.name = filename;
            m_curFile.firstEntry = m_srcTextEntryArray.size();
            
            boolean fileLoaded = fm.loadFile(filename, processedFiles);
            
            m_curFile.lastEntry = m_srcTextEntryArray.size()-1;

            if( fileLoaded && (m_curFile.lastEntry>=m_curFile.firstEntry) )
            {
                FileInfo fi=new FileInfo();
                fi.filePath=filepath;
                fi.firstEntryIndex=m_srcTextEntryArray.size();
                fi.size=m_srcTextEntryArray.size()-firstEntry;
                projectFilesList.add(fi);
                firstEntry=m_srcTextEntryArray.size();
            }
        }
        Core.getMainWindow().showStatusMessage(OStrings.getString("CT_LOAD_SRC_COMPLETE"));
        m_curFile = null;
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
            
            if (ext.equalsIgnoreCase(OConsts.TMX_EXTENSION))
                loadTMXFile(fname, "UTF-8", false); // NOI18N
            else if (ext.equalsIgnoreCase(OConsts.TMW_EXTENSION))
                loadTMXFile(fname, "ISO-8859-1", false); // NOI18N
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
        TMXReader tmx = new TMXReader(encoding, 
                m_config.getSourceLanguage(), m_config.getTargetLanguage());

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
                StringEntry se = m_strEntryHash.get(src);
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
                    dontCountNextIncrement(); // orphane translation don't count
                    se.setTranslation(trans);
                    strOrphaneList.add(se);
                }
                else
                {
                    se.setTranslation(trans);
                }
            }
            else
            {
                // not in a project - remember this as a translation
                //	memory string and add it to near list
                m_tmList.add(new TransMemory(src, trans, fname));
                StringEntry se = new StringEntry(src);
                dontCountNextIncrement();   // external TMXes don't count
                se.setTranslation(trans);
                strEntryList.add(se);
            }
        }
    }
    
    /** Formats date (in milliseconds) to YYYYMMDDHHMM form. */
    private static String millisToDateTime(final long millis) {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }

    /**
     * Returns a Source Text Entry of a certain number.
     * <p>
     * Source text entry is an individual segment for translation pulled
     * directly from the input files. There can be many SourceTextEntries having
     * identical source language strings.
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public SourceTextEntry getSTE(int num)
    {
        try
        {
            return m_srcTextEntryArray.get(num);
        }
        catch( IndexOutOfBoundsException iobe )
        {
            LOGGER.log(Level.SEVERE, "Invalid entry index: "+num, iobe);
            throw iobe;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public List<SourceTextEntry> getAllEntries() {
        return m_srcTextEntryArray;
    }

    /**
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public StringEntry getStringEntry(String srcText)
    {
        return m_strEntryHash.get(srcText);
    }
    
    ////////////////////////////////////////////////////////
    // simple project info
    
    /**
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public String	sourceRoot()
    { return m_config.getSourceRoot();		}
    
    /**
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public List<TransMemory>	getTransMemory()
    { return m_tmList;		}
    
    /////////////////////////////////////////////////////////
    
    private ProjectProperties m_config;
    
    /**
     * Returns the active Project's Properties.
     * 
     * TODO: rewrite for synchronize ProjectProperties
     */
    public ProjectProperties getProjectProperties()
    {
        return m_config;
    }
    
    private boolean m_modifiedFlag;
    
    /**
     * Returns whether the project was modified.
     */
    public synchronized boolean isProjectModified()
    {
        return m_modifiedFlag;
    }
    

    /** The number of unique translated segments. */
    private int numberofTranslatedSegments;
    /** Signals that the next increase doesn't count -- it's orphane */
    private boolean _dontCountNext = false;
    
   
    /**
     * Sygnals that the number of translated segments decreased 
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public synchronized void decreaseTranslated()
    {
        numberofTranslatedSegments--;
    }
    
    /** 
     * Sygnals that the next increase is false -- it's orphane 
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public synchronized void dontCountNextIncrement()
    {
        _dontCountNext = true;
    }
    
    /** 
     * Sygnals that the number of translated segments increased 
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public synchronized void increaseTranslated()
    {
        if( _dontCountNext )
            _dontCountNext = false;
        else
        {
            numberofTranslatedSegments++;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public List<StringEntry> getAllTranslations() {
        return Collections.unmodifiableList(new ArrayList<StringEntry>(m_strEntryList));
    }
    
    /**
     * {@inheritDoc}
     * 
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public List<LegacyTM> getMemory() {
        return m_legacyTMs;
    }

    /**
     * Can be called from any thread. Caller must be synchronized around
     * IDataEngine.
     */
    public List<FileInfo> getProjectFiles() {
        return projectFilesList;
    }
    
    // project name of strings loaded from TM - store globally so to not
    // pass seperately on each function call
    
    /**
     * True if project loaded. TODO: use m_config for that in future.
     */
    private boolean projectLoaded = false;

    /** 
     * Keeps track of file specific data to feed to SourceTextEntry objects
     * so they can have a bigger picture of what's where.
     */
    private ProjectFileData	m_curFile;
    
    /** maps text to strEntry obj */
    private Map<String,StringEntry> m_strEntryHash;
    
    /** Unique segments list. Used for save TMX.  */
    private List<StringEntry>	m_strEntryList;
    
    /** List of all segments in project. */
    private List<SourceTextEntry>	m_srcTextEntryArray;
    
    /** the list of legacy TMX files, each object is the list of string entries */
    private List<LegacyTM> m_legacyTMs;
    
    /** Segments count in project files. */
    private List<FileInfo> projectFilesList = new ArrayList<FileInfo>();
    
    private List<TransMemory>	m_tmList;
    private List<TransMemory>	m_orphanedList;
}
