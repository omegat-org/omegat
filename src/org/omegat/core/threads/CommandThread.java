/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
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

package org.omegat.core.threads;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.LegacyTM;
import org.omegat.core.ProjectProperties;
import org.omegat.core.StringEntry;
import org.omegat.core.TransMemory;
import org.omegat.core.data.IDataEngine;
import org.omegat.core.data.Statistics;
import org.omegat.core.glossary.GlossaryManager;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileData;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.TMXReader;
import org.omegat.util.TMXWriter;

/**
 * CommandThread is a thread to asynchronously do the stuff
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Bartko Zoltan
 */
public class CommandThread extends Thread implements IDataEngine
{
    
    /**
     * One and only CommandThread object in the OmegaT.
     * <p>
     * <small>
     * mihmax: Threading nightmare, IMHO.
     * </small>
     */
    public static CommandThread core;
    
    /**
     * the spell checker instance
     */
    private static final SpellChecker m_spellchecker = SpellChecker.getInstance();
    
    /**
     * return the spell checker instance
     */
    public SpellChecker getSpellchecker() {
        return m_spellchecker;
    }
    
    public CommandThread(MainWindow tf)
    {
        setName("Command thread"); // NOI18N
        setPriority(MIN_PRIORITY);
        
        m_transFrame = tf;
        m_projWin = tf.getProjectFrame();
        
        m_config = new ProjectProperties();
        m_strEntryHash = new HashMap<String, StringEntry>(4096);
        m_strEntryList = new ArrayList<StringEntry>();
        m_srcTextEntryArray = new ArrayList<SourceTextEntry>(4096);
        m_tmList = new ArrayList<TransMemory>();
        m_legacyTMs = new ArrayList<LegacyTM>();
        m_orphanedList = new ArrayList<TransMemory>();
        m_modifiedFlag = false;
        
        m_requestQueue = new LinkedList<RequestPacket>();
        m_saveCount = -1;
        m_saveThread = null;
    }
    
    public void run()
    {
        RequestPacket pack = new RequestPacket();
        m_saveThread = new SaveThread();
        try
        {
            while( !interrupted() )
            {
                try
                {
                    sleep(100); // otherwise CPU usage goes 100%
                } 
                catch (InterruptedException ex)
                {
                    interrupt();
                }
                
                pack.reset();
                messageBoardCheck(pack);
                switch (pack.type)
                {
                    case RequestPacket.NO_OP:
                        // do nothing
                        break;
                        
                    case RequestPacket.LOAD:
                        requestLoad(pack);
                        break;
                        
                    case RequestPacket.SAVE:
                        Core.getDataEngine().saveProject();
                        break;
                }
            }
            Preferences.save();
            
            m_saveThread.interrupt();
            
            // sleeping a bit
            try
            { 
                sleep(100); 
            }
            catch (InterruptedException e)
            {
                interrupt();
            }
            core = null;
        }
        catch (RuntimeException re)
        {
            forceSave(true);
            String msg = OStrings.getString("CT_FATAL_ERROR");
            m_transFrame.fatalError(msg, re);
        }
    }
    
    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////
    // message handling for external requests
    
    public synchronized void messageBoardPost(RequestPacket pack)
    {
        messageBoard(true, pack);
    }
    
    private synchronized void messageBoardCheck(RequestPacket pack)
    {
        messageBoard(false, pack);
    }
    
    private synchronized void messageBoard(boolean post, RequestPacket pack)
    {
        if (CommandThread.core == null)
            return;
        if (post)
        {
            m_requestQueue.add(pack);
        }
        else
        {
            if (m_requestQueue.size() > 0)
            {
                pack.set(m_requestQueue.removeFirst());
            }
        }
    }
    
    /**
     * Clears all hashes, lists etc.
     */
    public void cleanUp()
    {
        if (m_strEntryList.size() > 0)
        {
            // disable future saves
            if (m_saveCount >= 0)
                m_saveCount = 1;
            saveProject();
        }
        
        m_strEntryHash.clear();
        
        m_legacyTMs.clear();
        
        m_tmList.clear();
        m_orphanedList.clear();
        
        m_strEntryList.clear();
        m_srcTextEntryArray.clear();
        
        if (m_projWin != null)
        {
            if (m_projWin.isVisible())
                m_projWin.setVisible(false);
            m_projWin.reset();
        }
        
        numberofTranslatedSegments = 0;
        
        // clean up the spell checker
        m_spellchecker.destroy();
    }
    
    private void requestLoad(RequestPacket pack)
    {
        MainWindow tf = (MainWindow) pack.obj;
        // load new project
        try
        {
            cleanUp();
            
            String evtStr;
            
            evtStr = OStrings.getString("CT_LOADING_PROJECT");
            MessageRelay.uiMessageSetMessageText(tf, evtStr);
            if (!Core.getDataEngine().loadProject((String)pack.parameter))
            {
                // loading of project cancelled
                evtStr = OStrings.getString("CT_CANCEL_LOAD");
                MessageRelay.uiMessageSetMessageText(tf, evtStr);
                return;
            }
            tf.finishLoadProject();
            MessageRelay.uiMessageDisplayEntry(tf);
            if (m_saveCount == -1)
            {
                m_saveThread.start();
                m_saveCount = 1;
            }
            
            // Building up glossary
            evtStr = OStrings.getString("CT_LOADING_GLOSSARIES");
            MessageRelay.uiMessageSetMessageText(tf, evtStr);
            m_glossary.buildGlossary(m_strEntryList);
            MessageRelay.uiMessageSetMessageText(tf, OStrings.getString("CT_LOADING_PROJECT"));
            
            // load in translation database files
            try
            {
                loadTM();
            }
            catch (IOException e)
            {
                String msg = OStrings.getString("TF_TM_LOAD_ERROR");
                displayError(msg, e);
                // allow project load to resume
            }
            
            // evaluate strings for fuzzy matching
            buildNearList();
            
            // build word count
            Statistics.buildProjectStats(m_strEntryList, m_srcTextEntryArray, m_config, numberofTranslatedSegments);
            
            // Project Loaded...
            MessageRelay.uiMessageSetMessageText(tf, "");  // NOI18N
            
            // Calling Main Window back to notify that project
            // is successfully loaded.
            // Part of bugfix for
            //           First segment does not trigger matches after load
            //           http://sourceforge.net/support/tracker.php?aid=1370838
            m_transFrame.projectLoaded();
            
            // enable normal saves
            m_saveCount = 2;
        }
        catch (InterruptedException e1)
        {
            // user said cancel - this is OK
        }
        catch( Exception e )
        {
            // any error
            if( !projectClosing )
                displayError(OStrings.getString("TF_LOAD_ERROR"), e);
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
            m_glossary = null;

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Log.logErrorRB("OUT_OF_MEMORY");
            Log.log(oome);
            m_transFrame.displayError(OStrings.getString("OUT_OF_MEMORY"), oome);

            // Just quit, we can't help it anyway
            System.exit(0);
        }
    }
    
    /**
     * True if project loaded. TODO: use m_config for that in future.
     */
    private boolean projectLoaded = false;

    /**
     * {@inheritDoc}
     */
    public boolean isProjectLoaded() {
        return projectLoaded;
    }
    
    
    private boolean projectClosing = false;
    /**
     * Signals to the core thread that a project is being closed now,
     * and if it's still being loaded, core thread shouldn't throw
     * any error.
     */
    public void closeProject() {
        projectLoaded = false;
        projectClosing = true;
        cleanUp();

        CoreEvents.fireProjectChange();
    }
    
    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     */
    public List<SourceTextEntry> validateTags()
    {
        int j;
        String s;
        String t;
        List<String> srcTags = new ArrayList<String>(32);
        List<String> locTags = new ArrayList<String>(32);
        List<SourceTextEntry> suspects = new ArrayList<SourceTextEntry>(16);
        
        StringEntry se;
        
        for (SourceTextEntry ste : m_srcTextEntryArray)
        {
            se = ste.getStrEntry();
            s = se.getSrcText();
            t = se.getTranslation();
            
            // if there's no translation, skip the string
            // bugfix for http://sourceforge.net/support/tracker.php?aid=1209839
            if( t==null || t.length()==0 )
                continue;
            
            // extract tags from src and loc string
            StaticUtils.buildTagList(s, srcTags);
            StaticUtils.buildTagList(t, locTags);
            
            // make sure lists match
            // for now, insist on exact match
            if (srcTags.size() != locTags.size())
                suspects.add(ste);
            else
            {
                // compare one by one
                for (j=0; j<srcTags.size(); j++)
                {
                    s = srcTags.get(j);
                    t = locTags.get(j);
                    if (!s.equals(t))
                    {
                        suspects.add(ste);
                        break;
                    }
                }
            }
            
            srcTags.clear();
            locTags.clear();
        }
        return suspects;
    }
    
    /** Builds all translated files and creates fresh TM files. */
    public void compileProject()
            throws IOException, TranslationException
    {
        if (m_strEntryHash.size() == 0)
            return;
        
        // save project first
        saveProject();

        // build 3 TMX files:
        // - OmegaT-specific, with inline OmegaT formatting tags
        // - TMX Level 1, without formatting tags
        // - TMX Level 2, with OmegaT formatting tags wrapped in TMX inline tags
        try
        {
            synchronized (this) {
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

        CoreEvents.fireProjectChange();
    }
    
    /** Saves the translation memory and preferences */
    public void saveProject()
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
        Preferences.save();
        
        if (m_saveCount <= 0)
            return;
        else if (m_saveCount == 1)
            m_saveCount = 0;
        
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
            synchronized (this) {
                TMXWriter.buildTMXFile(s, false, true, false, m_config, m_strEntryList, m_orphanedList);
            }
            m_modifiedFlag = false;
        }
        catch (IOException e)
        {
            String msg = OStrings.getString("CT_ERROR_SAVING_PROJ");
            displayError(msg, e);
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

        CoreEvents.fireProjectChange();
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
        SourceTextEntry srcTextEntry = new SourceTextEntry(strEntry, m_curFile, numEntries());
        m_srcTextEntryArray.add(srcTextEntry);
    }
    
    public void createProject()
    {
        // create project directories
        // save project files (.proj .handlers .ignore)
        try
        {
            if (!m_config.createNew(m_transFrame))
                return;	// cancel pressed
            
            // create project root directory
            File proj = new File(m_config.getProjectRoot());
            if (!proj.isDirectory())
            {
                if (!proj.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE");
                    throw new IOException(msg);
                }
            }
            
            // create internal directory
            File internal = new File(m_config.getProjectInternal());
            if (!internal.isDirectory())
            {
                if (!internal.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE");
                    throw new IOException(msg);
                }
            }
            
            // populate internal directory with project files
            //buildDefaultHandlerFile(hand);
            //buildDefaultIgnoreFile();
            
            // create src dir
            File src = new File(m_config.getSourceRoot());
            if (!src.isDirectory())
            {
                if (!src.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE") + "\n(.../src/)";      // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create glos dir
            File glos = new File(m_config.getGlossaryRoot());
            if (!glos.isDirectory())
            {
                if (!glos.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE") + "\n(.../glos/)";     // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create TM dir
            File tm = new File(m_config.getTMRoot());
            if (!tm.isDirectory())
            {
                if (!tm.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE") + "\n(.../tm/)";       // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create loc dir
            File loc = new File(m_config.getTargetRoot());
            if (!loc.isDirectory())
            {
                if (!loc.mkdirs())
                {
                    String msg = OStrings.getString("CT_ERROR_CREATE") + "\n(.../target/)"; // NOI18N
                    throw new IOException(msg);
                }
            }
            
            m_config.buildProjFile();
            CoreEvents.fireProjectChange();
        }
        catch(IOException e)
        {
            // trouble in tinsletown...
            String msg = OStrings.getString("CT_ERROR_CREATING_PROJECT");
            displayError(msg, e);
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
            String msg = OStrings.getString("CT_ERROR_ACCESS_PROJECT_FILE");
            displayError(msg, se);
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
            String msg = OStrings.getString("CT_ERROR_LOADING_PROJECT_FILE");
            displayError(msg, e);
        }
    }
    
    /**
     * Loads project in a "big" sense -- loads project's properties, glossaryes,
     * tms, source files etc.
     * <p>
     * We may pass here the folder where the project resides
     * or null, in which case FileChooser is brought up to select a project.
     *
     * @param projectRoot The folder where the project resides. If it's null,
     *                     FileChooser is called to select a project.
     */
    public boolean loadProject(String projectRoot)
            throws IOException, InterruptedIOException, TranslationException
    {
        if (!m_config.loadExisting(m_transFrame, projectRoot))
            return false;

        // reset token list cache
        StaticUtils.clearTokenCache();
        
        projectClosing = false;
        
        // first load glossary files
        m_glossary = new GlossaryManager();
        m_glossary.loadGlossaryFiles(new File(m_config.getGlossaryRoot()));
        
        // now open source files
        FilterMaster fm = FilterMaster.getInstance();
        
        List<String> srcFileList = new ArrayList<String>();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        Set<File> processedFiles = new HashSet<File>();
        
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
            // BUGFIX FOR: Empty files are displayed in a file list window
            //             http://sourceforge.net/support/tracker.php?aid=1256026
            //             added condition m_curFile.lastEntry>=m_curFile.firstEntry
            if( fileLoaded && (m_curFile.lastEntry>=m_curFile.firstEntry) )
            {
                m_projWin.addFile(filepath, numEntries());
            }
        }
        Core.getMainWindow().showStatusMessage(OStrings.getString("CT_LOAD_SRC_COMPLETE"));
        m_curFile = null;
        loadTranslations();
//                                  Call is too early 
//        m_projWin.buildDisplay(); for MainWindow.getActiveFileName() 
//                                  and doesn't seem useful
        m_projWin.setVisible(true);
        m_projWin.toFront();
        
        // initialize the spell checker
        m_spellchecker.initialize();
        
        projectLoaded = true;
        
        CoreEvents.fireProjectChange();

        return true;
    }
    
    /**
     * Builds the list of fuzzy matches between the source text strings.
     *
     * @author Maxym Mykhalchuk
     */
    private void buildNearList() throws InterruptedException
    {
        // creating a fuzzy matching engine
        FuzzyMatcher matcher = new FuzzyMatcher(m_transFrame, this);
        
        // matching source strings with each other
        matcher.match(m_strEntryList);
        
        // matching legacy TMX files
        for(LegacyTM tm : m_legacyTMs)
        {
            matcher.match(m_strEntryList, tm.getName(), tm.getStrings());
        }
    }
    
    /** Locates and loads external TMX files with legacy translations. */
    private void loadTM() throws IOException
    {
        // build strEntryList for each file
        // send to buildNearList
        String [] fileList;
        File f;
        int i;
        String fname;
        
        // foreach lang
        // foreach file
        // build string entry list
        // call build near list (entry list, status+filename)
        //buildNearList(m_strEntryList, status + " (" + fname + ")");
        
        String ext;
        f = new File(m_config.getTMRoot());
        fileList = f.list();
        for (i=0; i<fileList.length; i++)
        {
            fname = fileList[i];
            int lastdot = fname.lastIndexOf('.');
            if (lastdot<0)
                lastdot = fname.length();
            ext = fname.substring(lastdot);
            fname = m_config.getTMRoot();
            if (!fname.endsWith(File.separator))
                fname += File.separator;
            fname += fileList[i];
            
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
            
            removeOldBackups(tmxFile);
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
    
    /** Format for TMX files backup suffix. */
    protected static final SimpleDateFormat FORMAT_DATETIME_SUFFIX = new SimpleDateFormat("yyyyMMddHHmm");

    /** Formats date (in milliseconds) to YYYYMMDDHHMM form. */
    private String millisToDateTime(final long millis) {
        synchronized (FORMAT_DATETIME_SUFFIX) {
            return FORMAT_DATETIME_SUFFIX.format(new Date());
        }
    }

    private static final int MAX_BACKUPS = 10;
    /** Removes old backups so that only 10 last are there. */
    private void removeOldBackups(File tmxFile)
    {
        // now removing too old backups
        try
        {
            File tmxFolder = tmxFile.getParentFile();
            // getting all .bak files in the same folder
            List<File> tmxs = Arrays.asList(tmxFolder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".bak");                               // NOI18N
                }
            }));
            
            // removing more than 10 backups
            if (tmxs.size()>MAX_BACKUPS)
            {
                // sorting: old files last
                Collections.sort(tmxs, new Comparator<File>()
                {
                    public int compare(File f1, File f2)
                    {
                        if( f1.lastModified()==f2.lastModified() )
                            return 0;
                        else if ( f1.lastModified()>f2.lastModified() )
                            return -1;
                        else
                            return 1;
                    }
                });
                for(int i=MAX_BACKUPS; i<tmxs.size(); i++)
                    tmxs.get(i).delete();
            }
        }
        catch(Exception e)
        {
            // we don't care
        }
    }
    
    /**
     * Writes the error info to the log and
     * displays an error message.
     */
    void displayError(String msg, Throwable e)
    {
        Log.logRB("LD_ERROR", new Object[] {msg}); // NOI18N
        Log.log(e);
        Log.log("----------------------------"); // NOI18N
        if( m_transFrame!=null )
            MessageRelay.uiMessageDisplayError(m_transFrame, msg, e);
    }

    /**
      * Displays an error message
      *
      * @param message The message to display
      * @param error   The error that caused the message to be displayed (may be null)
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public void displayErrorMessage(String message, Throwable error) {
        if (m_transFrame != null)
            m_transFrame.displayError(message, error);
    }


    /**
     * Returns a Source Text Entry of a certain number.
     * <p>
     * Source text entry is an individual segment for
     * translation pulled directly from the input files.
     * There can be many SourceTextEntries having identical source
     * language strings.
     */
    public SourceTextEntry getSTE(int num)
    {
        try
        {
            return m_srcTextEntryArray.get(num);
        }
        catch( IndexOutOfBoundsException iobe )
        {
            StringEntry str = new StringEntry(OStrings.getString("TF_INTRO_EMPTYPROJECT"));
            str.setTranslation(" ");                                            // NOI18N
            
            ProjectFileData file = new ProjectFileData();
            file.name = sourceRoot() + OStrings.getString("TF_INTRO_EMPTYPROJECT_FILENAME");
            file.firstEntry = 0;
            file.lastEntry = 0;
            
            return new SourceTextEntry(str, file, 0);
        }
    }
    
    public StringEntry getStringEntry(String srcText)
    {
        return m_strEntryHash.get(srcText);
    }
    
    ////////////////////////////////////////////////////////
    // simple project info
    
    public String	sourceRoot()
    { return m_config.getSourceRoot();		}
    
    public int		numEntries()
    { return m_srcTextEntryArray.size(); }
    public MainWindow getTransFrame()
    { return m_transFrame;	}
    
    public List<TransMemory>	getTransMemory()
    { return m_tmList;		}
    
    /////////////////////////////////////////////////////////
    
    private SaveThread	m_saveThread;
    // count=0		save disabled
    // count=1		one more save only
    // count=2		regular mode
    private int m_saveCount;
    
    private ProjectProperties m_config;
    
    /**
     * Returns the active Project's Properties.
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
    
    /** Returns the total number of segments, including duplicates. */
    public int getNumberOfSegmentsTotal()
    {
        return m_srcTextEntryArray.size();
    }
    
    /** Returns the number of unique segments. */
    public int getNumberOfUniqueSegments()
    {
        return m_strEntryList.size();
    }

    /** The number of unique translated segments. */
    private int numberofTranslatedSegments;
    /** Signals that the next increase doesn't count -- it's orphane */
    private boolean _dontCountNext = false;
    
    /** Returns the number of unique translated segments. */
    public synchronized int getNumberofTranslatedSegments()
    {
        return numberofTranslatedSegments;
    }

    /** Sygnals that the number of translated segments decreased */
    public synchronized void decreaseTranslated()
    {
        numberofTranslatedSegments--;
        uiUpdateNumberOfTranslatedSegments();
    }
    
    /** Sygnals that the next increase is false -- it's orphane */
    public synchronized void dontCountNextIncrement()
    {
        _dontCountNext = true;
    }
    
    /** Sygnals that the number of translated segments increased */
    public synchronized void increaseTranslated()
    {
        if( _dontCountNext )
            _dontCountNext = false;
        else
        {
            numberofTranslatedSegments++;
            uiUpdateNumberOfTranslatedSegments();
        }
    }
    
    /** 
     * Asynchronously queries ProjectFrame to update 
     * the number of translated segments. 
     */
    private void uiUpdateNumberOfTranslatedSegments()
    {
        if( m_projWin==null || !m_projWin.isVisible() )
            return;
        
        Thread torun = new Thread()
        {
            public void run()
            {
                m_projWin.updateNumberOfTranslatedSegments();
            }
        };
        torun.setPriority(Thread.MIN_PRIORITY);
        torun.start();
    }
    
    private LinkedList<RequestPacket> m_requestQueue;
    
    // project name of strings loaded from TM - store globally so to not
    // pass seperately on each function call
    
    /** 
     * Keeps track of file specific data to feed to SourceTextEntry objects
     * so they can have a bigger picture of what's where.
     */
    private ProjectFileData	m_curFile;
    
    MainWindow	m_transFrame;
    private ProjectFrame	m_projWin;
    
    /** maps text to strEntry obj */
    private Map<String,StringEntry> m_strEntryHash; 
    private List<StringEntry>	m_strEntryList;
    private List<SourceTextEntry>	m_srcTextEntryArray;
    
    /** the list of legacy TMX files, each object is the list of string entries */
    private List<LegacyTM> m_legacyTMs;
    
    private List<TransMemory>	m_tmList;
    private List<TransMemory>	m_orphanedList;
    
    private GlossaryManager m_glossary;
}
