/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.omegat.core.LegacyTM;
import org.omegat.core.StringEntry;
import org.omegat.core.TransMemory;
import org.omegat.core.glossary.GlossaryManager;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.ProjectProperties;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileData;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.TMXReader;


/**
 * CommandThread is a thread to asynchronously do the stuff
 *
 * @author Keith Godfrey
 */
public class CommandThread extends Thread
{
    
    /**
     * One and only CommandThread object in the OmegaT.
     * <p>
     * <small>
     * mihmax: Threading nightmare, IMHO.
     * </small>
     */
    public static CommandThread core;
    
    public CommandThread(MainWindow tf)
    {
        setName("Command thread"); // NOI18N
        setPriority(MIN_PRIORITY);
        
        m_transFrame = tf;
        m_projWin = tf.getProjectFrame();
        
        m_config = new ProjectProperties();
        m_strEntryHash = new HashMap(4096);
        m_strEntryList = new ArrayList();
        m_srcTextEntryArray = new ArrayList(4096);
        m_tmList = new ArrayList();
        m_legacyTMs = new ArrayList();
        m_orphanedList = new ArrayList();
        m_modifiedFlag = false;
        
        m_extensionList = new ArrayList(32);
        m_extensionMapList = new ArrayList(32);
        
        m_requestQueue = new LinkedList();
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
                        save();
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
            String msg = OStrings.CT_FATAL_ERROR;
            m_transFrame.fatalError(msg, re);
        }
    }
    
    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////
    // message handling for external requests
    
    public void messageBoardPost(RequestPacket pack)
    {
        messageBoard(true, pack);
    }
    
    private void messageBoardCheck(RequestPacket pack)
    {
        messageBoard(false, pack);
    }
    
    private void messageBoard(boolean post, RequestPacket pack)
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
                pack.set((RequestPacket)
                m_requestQueue.removeFirst());
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
            save();
        }
        
        m_strEntryHash.clear();
        
        m_legacyTMs.clear();
        
        m_tmList.clear();
        m_orphanedList.clear();
        
        m_extensionList.clear();
        m_extensionMapList.clear();
        
        m_strEntryList.clear();
        m_srcTextEntryArray.clear();
        
        if (m_projWin != null)
        {
            if (m_projWin.isVisible())
                m_projWin.setVisible(false);
            m_projWin.reset();
        }
        
        m_totalWords = 0;
        m_partialWords = 0;
        m_currentWords = 0;
        numberofTranslatedSegments = 0;
    }
    
    private void requestLoad(RequestPacket pack)
    {
        MainWindow tf = (MainWindow) pack.obj;
        // load new project
        try
        {
            cleanUp();
            
            String evtStr;
            
            evtStr = OStrings.CT_LOADING_PROJECT;
            MessageRelay.uiMessageSetMessageText(tf, evtStr);
            if (!loadProject((String)pack.parameter))
            {
                // loading of project cancelled
                evtStr = OStrings.CT_CANCEL_LOAD;
                MessageRelay.uiMessageSetMessageText(tf, evtStr);
                return;
            }
//			if (numEntries() <= 0)
//				throw new IOException("The project is empty");
            tf.finishLoadProject();
            MessageRelay.uiMessageDisplayEntry(tf);
            if (m_saveCount == -1)
            {
                m_saveThread.start();
                m_saveCount = 1;
            }
            
            // Building up glossary
            evtStr = OStrings.CT_LOADING_GLOSSARY;
            MessageRelay.uiMessageSetMessageText(tf, evtStr);
            m_glossary.buildGlossary(m_strEntryList);
            MessageRelay.uiMessageSetMessageText(tf, OStrings.CT_LOADING_PROJECT);
            
            // load in translation database files
            try
            {
                loadTM();
            }
            catch (IOException e)
            {
                String msg = OStrings.TF_TM_LOAD_ERROR;
                displayError(msg, e);
                // allow project load to resume
            }
            
            // evaluate strings for fuzzy matching
            buildNearList();
            
            // Project Loaded...
            MessageRelay.uiMessageSetMessageText(tf, "");  // NOI18N
            
            /*
             temporary removed
                        evtStr = OStrings.CT_LOADING_WORDCOUNT;
                        MessageRelay.uiMessageSetMessageText(tf, evtStr);
                        buildWordCounts();
                        MessageRelay.uiMessageFuzzyInfo(tf);
                        MessageRelay.uiMessageSetMessageText(tf, "");  // NOI18N
             */
            
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
                displayError(OStrings.TF_LOAD_ERROR, e);
            else
                StaticUtils.log("Project Load aborted by user.");               // NOI18N
        }
    }
    
    
    private boolean projectClosing = false;
    /**
     * Signals to the core thread that a project is being closed now,
     * and if it's still being loaded, core thread shouldn't throw
     * any error.
     */
    public void signalProjectClosing()
    {
        projectClosing = true;
    }
    
    /**
     * Saves a TMX file to disk
     */
    private void buildTMXFile(String filename) throws IOException
    {
        int i;
        String s;
        String t;
        
        // build translation database files
        StringEntry se;
        
        // we got this far, so assume lang codes are proper
        String sourceLocale = Preferences.getPreference(Preferences.SOURCE_LOCALE);
        String targetLocale = Preferences.getPreference(Preferences.TARGET_LOCALE);
        String segmenting;
        if( m_config.isSentenceSegmentingEnabled() )
            segmenting = TMXReader.SEG_SENTENCE;
        else
            segmenting = TMXReader.SEG_PARAGRAPH;
        
        FileOutputStream fos = new FileOutputStream(filename);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");          // NOI18N
        BufferedWriter out = new BufferedWriter(osw);
        
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");              // NOI18N
        out.write("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">\n");                     // NOI18N
        out.write("<tmx version=\"1.1\">\n");                                   // NOI18N
        out.write("  <header\n");                                               // NOI18N
        out.write("    creationtool=\"OmegaT\"\n");                             // NOI18N
        out.write("    creationtoolversion=\"1.6\"\n");                         // NOI18N
        out.write("    segtype=\""+segmenting+"\"\n");                          // NOI18N
        out.write("    o-tmf=\"OmegaT TMX\"\n");                                // NOI18N
        out.write("    adminlang=\"EN-US\"\n");                                 // NOI18N
        out.write("    srclang=\"" + sourceLocale + "\"\n");                    // NOI18N
        out.write("    datatype=\"plaintext\"\n");                              // NOI18N
        out.write("  >\n");                                                     // NOI18N
        out.write("  </header>\n");                                             // NOI18N
        out.write("  <body>\n");                                                // NOI18N
        
        for (i=0; i<m_strEntryList.size(); i++)
        {
            se = (StringEntry) m_strEntryList.get(i);
            String translation = se.getTranslation();
            if( translation.length()==0 )
                continue;
            s = StaticUtils.makeValidXML(se.getSrcText());
            t = StaticUtils.makeValidXML(translation);
            out.write("    <tu>\n");                                            // NOI18N
            out.write("      <tuv lang=\"" + sourceLocale + "\">\n");           // NOI18N
            out.write("        <seg>" + s + "</seg>\n");                        // NOI18N
            out.write("      </tuv>\n");                                        // NOI18N
            out.write("      <tuv lang=\"" + targetLocale + "\">\n");           // NOI18N
            out.write("        <seg>" + t + "</seg>\n");                        // NOI18N
            out.write("      </tuv>\n");                                        // NOI18N
            out.write("    </tu>\n");                                           // NOI18N
        }
        TransMemory transMem;
        for (i=0; i<m_orphanedList.size(); i++)
        {
            transMem = (TransMemory) m_orphanedList.get(i);
            if( transMem.target.length()==0 )
                continue;
            s = StaticUtils.makeValidXML(transMem.source);
            t = StaticUtils.makeValidXML(transMem.target);
            out.write("    <tu>\n");                                            // NOI18N
            out.write("      <tuv lang=\"" + sourceLocale + "\">\n");           // NOI18N
            out.write("        <seg>" + s + "</seg>\n");                        // NOI18N
            out.write("      </tuv>\n");                                        // NOI18N
            out.write("      <tuv lang=\"" + targetLocale + "\">\n");           // NOI18N
            out.write("        <seg>" + t + "</seg>\n");                        // NOI18N
            out.write("      </tuv>\n");                                        // NOI18N
            out.write("    </tu>\n");                                           // NOI18N
        }
        out.write("  </body>\n");                                               // NOI18N
        out.write("</tmx>\n");                                                  // NOI18N
        out.close();
    }
    
    // scan project and build list of entries which are suspected of
    //  having changed (i.e. invalid) tag structures
    public ArrayList validateTags()
    {
        int i, j;
        String s;
        String t;
        ArrayList srcTags = new ArrayList(32);
        ArrayList locTags = new ArrayList(32);
        ArrayList suspects = new ArrayList(16);
        
        StringEntry se;
        SourceTextEntry ste;
        
        for (i=0; i<numEntries(); i++)
        {
            ste = (SourceTextEntry) m_srcTextEntryArray.get(i);
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
                    s = (String) srcTags.get(j);
                    t = (String) locTags.get(j);
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
    
    // build all translated files and create a new TM file
    public void compileProject()
    throws IOException, TranslationException
    {
        if (m_strEntryHash.size() == 0)
            return;
        
        // save project first
        save();
        
        try
        {
            String fname = m_config.getProjectRoot() + m_config.getProjectName() +
            OConsts.TMX_EXTENSION;
            buildTMXFile(fname);
        }
        catch (IOException e)
        {
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX"));
        }
        
        // build mirror directory of source tree
        ArrayList fileList = new ArrayList(256);
        String srcRoot = m_config.getSourceRoot();
        String locRoot = m_config.getTargetRoot();
        StaticUtils.buildDirList(fileList, new File(srcRoot));
        
        for(int i=0; i<fileList.size(); i++)
        {
            String filename = (String) fileList.get(i);
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
        
        for(int i=0; i<fileList.size(); i++)
        {
            String filename = (String) fileList.get(i);
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
            m_transFrame.setMessageText(OStrings.CT_COMPILE_FILE_MX + midName);
            
            fm.translateFile(srcRoot, midName, locRoot);
        }
        m_transFrame.setMessageText(OStrings.CT_COMPILE_DONE_MX);
    }
    
    /** Saves the translation memory and preferences */
    public void save()
    {
        if( isProjectModified() )
            forceSave(false);
    }
    
    public void markAsDirty()
    {
        m_modifiedFlag = true;
    }
    
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
            buildTMXFile(s);
            m_modifiedFlag = false;
        }
        catch (IOException e)
        {
            String msg = OStrings.CT_ERROR_SAVING_PROJ;
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
    }
    
    /**
     * Creates a new Source Text Entry
     * (mapping between source file and a TM).
     * Also if there's no entry for <code>srcText</code> string yet,
     * then adds a new String Entry to internal in-memory TM.
     */
    public void addEntry(String srcText)
    {
        // if the source string is empty, don't add it to TM
        if( srcText.length()==0 || srcText.trim().length()==0 )
            return;
        
        SourceTextEntry srcTextEntry;
        StringEntry strEntry;
        
        srcTextEntry = new SourceTextEntry();
        strEntry = (StringEntry) m_strEntryHash.get(srcText);
        if (strEntry == null)
        {
            // entry doesn't exist yet - create and store it
            strEntry = new StringEntry(srcText);
            m_strEntryList.add(strEntry);
            m_strEntryHash.put(srcText, strEntry);
        }
        
        srcTextEntry.set(strEntry, m_curFile, numEntries());
        m_srcTextEntryArray.add(srcTextEntry);
        
    }
    
    public void createProject()
    {
        // create project directories
        // save project files (.proj .handlers .ignore)
        try
        {
            if (!m_config.createNew())
                return;	// cancel pressed
            
            // create project root directory
            File proj = new File(m_config.getProjectRoot());
            if (!proj.isDirectory())
            {
                if (!proj.mkdirs())
                {
                    String msg = OStrings.CT_ERROR_CREATE;
                    throw new IOException(msg);
                }
            }
            
            // create internal directory
            File internal = new File(m_config.getProjectInternal());
            if (!internal.isDirectory())
            {
                if (!internal.mkdirs())
                {
                    String msg = OStrings.CT_ERROR_CREATE;
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
                    String msg = OStrings.CT_ERROR_CREATE + " (.../src/)";      // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create glos dir
            File glos = new File(m_config.getGlossaryRoot());
            if (!glos.isDirectory())
            {
                if (!glos.mkdirs())
                {
                    String msg = OStrings.CT_ERROR_CREATE + " (.../glos/)";     // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create TM dir
            File tm = new File(m_config.getTMRoot());
            if (!tm.isDirectory())
            {
                if (!tm.mkdirs())
                {
                    String msg = OStrings.CT_ERROR_CREATE + " (.../tm/)";       // NOI18N
                    throw new IOException(msg);
                }
            }
            
            // create loc dir
            File loc = new File(m_config.getTargetRoot());
            if (!loc.isDirectory())
            {
                if (!loc.mkdirs())
                {
                    String msg = OStrings.CT_ERROR_CREATE + " (.../target/)"; // NOI18N
                    throw new IOException(msg);
                }
            }
            
            m_config.buildProjFile();
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
    
    private void loadTranslations()
    {
        File proj;
        // load translation file (project_name.bin)
        try
        {
            proj = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);
            if (!proj.exists())
            {
                StaticUtils.log(OStrings.getString("CT_ERROR_CANNOT_FIND_TMX")+
                "'" + proj + "'"); // NOI18N
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
    private boolean loadProject(String projectRoot)
    throws IOException, InterruptedIOException, TranslationException
    {
        int i;
        int j;
        if (!m_config.loadExisting(projectRoot))
            return false;
        
        projectClosing = false;
        
        // first load glossary files
        m_glossary = new GlossaryManager();
        m_glossary.loadGlossaryFiles(new File(m_config.getGlossaryRoot()));
        
        // now open source files
        FilterMaster fm = FilterMaster.getInstance();
        
        ArrayList srcFileList = new ArrayList(256);
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        // keep track of how many entries are in each file
        for (i=0; i<srcFileList.size(); i++)
        {
            String filename = (String) srcFileList.get(i);
            
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(
            m_config.getSourceRoot().length());
            
            
            m_transFrame.setMessageText(OStrings.CT_LOAD_FILE_MX + filepath);
            
            m_curFile = new ProjectFileData();
            m_curFile.name = filename;
            m_curFile.firstEntry = m_srcTextEntryArray.size();
            boolean fileLoaded = fm.loadFile(filename);
            m_curFile.lastEntry = m_srcTextEntryArray.size()-1;
            // BUGFIX FOR: Empty files are displayed in a file list window
            //             http://sourceforge.net/support/tracker.php?aid=1256026
            //             added condition m_curFile.lastEntry>=m_curFile.firstEntry
            if( fileLoaded && (m_curFile.lastEntry>=m_curFile.firstEntry) )
            {
                m_projWin.addFile(filepath, numEntries());
            }
        }
        m_transFrame.setMessageText(OStrings.getString("CT_LOAD_SRC_COMPLETE"));
        m_curFile = null;
        loadTranslations();
        m_projWin.setNumberofUniqueSegments(m_strEntryList.size());
        m_projWin.buildDisplay();
        m_projWin.setVisible(true);
        m_projWin.toFront();
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
        for(int i=0; i<m_legacyTMs.size(); i++)
        {
            LegacyTM tm = (LegacyTM)m_legacyTMs.get(i);
            matcher.match(m_strEntryList, tm.getName(), tm.getStrings());
        }
    }
    
    private void loadTM() throws IOException
    {
        // build strEntryList for each file
        // send to buildNearList
        String [] fileList;
        File f;
        int i;
        String fname;
        ArrayList strEntryList = new ArrayList(m_strEntryList.size());
        
        // foreach lang
        // foreach file
        // build string entry list
        // call build near list (entry list, status+filename)
        //buildNearList(m_strEntryList, status + " (" + fname + ")");
        
        String ext;
        strEntryList.clear();
        f = new File(m_config.getTMRoot());
        fileList = f.list();
        for (i=0; i<fileList.length; i++)
        {
            strEntryList.clear();
            fname = fileList[i];
            ext = fname.substring(fname.lastIndexOf('.'));
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
    
    private void loadTMXFile(String fname, String encoding, boolean isProject)
    throws IOException
    {
        try
        {
            TMXReader tmx = new TMXReader(encoding);
            tmx.loadFile(fname);
            
            int num = tmx.numSegments();
            ArrayList strEntryList = new ArrayList(num);
            ArrayList strOrphaneList = null;
            
            // RFE 1001918 - backing up project's TMX upon successful read
            if( isProject )
                LFileCopy.copy(fname, fname+".bak");                            // NOI18N
            
            // If a legacy TM, creating one
            // and adding to the list of legacy TMs
            if( isProject )
            {
                strOrphaneList = new ArrayList();
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
                    StringEntry se = (StringEntry) m_strEntryHash.get(src);
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
        catch (TranslationException e)
        {
            throw new IOException(OStrings.getString("CT_ERROR_PARSEERROR")+
            "'" + fname + "'\n" +  e); // NOI18N
        }
    }
    
    /**
     * Writes the error info to the log and
     * displays an error message.
     */
    void displayError(String msg, Throwable e)
    {
        StaticUtils.log(OStrings.LD_ERROR + " " + msg); // NOI18N
        e.printStackTrace(StaticUtils.getLogStream());
        e.printStackTrace();
        StaticUtils.log("----------------------------"); // NOI18N
        if( m_transFrame!=null )
            MessageRelay.uiMessageDisplayError(m_transFrame, msg, e);
    }
    
    /*
     temporary removed
        private void buildWordCounts()
        {
                ListIterator it;
                StringEntry se;
                LinkedList pl;
                m_totalWords = 0;
                m_partialWords = 0;
                int words;
                it = m_strEntryList.listIterator();
                while(it.hasNext())
                {
                        se = (StringEntry) it.next();
                        pl = se.getParentList();
                        words = se.getWordCount();
                        m_partialWords += words;
                        m_totalWords += words * pl.size();
                }
     
                // now dump file based word counts to disk
                String fn = m_config.getProjectInternal() + OConsts.WORD_CNT_FILE_EXT;
                FileWriter ofp = null;
                try
                {
                        ofp = new FileWriter(fn);
                        ofp.write(OStrings.getString("CT_WORD_COUNT_UNIQUE")+
                                        m_partialWords + "\n"); // NOI18N
                        ofp.write(OStrings.getString("CT_WORD_COUNT_TOTAL")	+
                                        m_totalWords + "\n"); // NOI18N
                        it = m_srcTextEntryArray.listIterator();
                        SourceTextEntry ste;
                        String curFile = ""; // NOI18N
                        String file;
                        int totWords = 0;
                        while(it.hasNext())
                        {
                            ste = (SourceTextEntry) it.next();
                            file = ste.getSrcFile().name;
                            if (curFile.compareTo(file) != 0)
                            {
                                if (curFile.length() > 0)
                                    ofp.write(curFile + "\t" + totWords +"\n");// NOI18N
                                curFile = file;
                                totWords = 0;
                            }
                            words = ste.getStrEntry().getWordCount();
                            totWords += words;
                            m_currentWords += words;
                        }
                        if (curFile.length() > 0)
                        {
                                ofp.write(curFile + "\t" + totWords +"\n\n"); // NOI18N
                        }
                        ofp.write(OStrings.getString("CT_WORD_REMAINING") +
                                        m_currentWords + "\n"); // NOI18N
                        ofp.close();
                }
                catch (IOException e)
                {
                        try { if (ofp != null) ofp.close();	}
                        catch (IOException e2) {
            }
                }
        }
     */
    
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
            return (SourceTextEntry) m_srcTextEntryArray.get(num);
        }
        catch( IndexOutOfBoundsException iobe )
        {
            StringEntry str = new StringEntry(
                    OStrings.getString("TF_INTRO_EMPTYPROJECT"));
            str.setTranslation(" ");                                            // NOI18N
            
            ProjectFileData file = new ProjectFileData();
            file.name = OStrings.getString("TF_INTRO_EMPTYPROJECT_FILENAME");
            file.firstEntry = 0;
            file.lastEntry = 0;
            
            SourceTextEntry res = new SourceTextEntry();
            res.set(str, file, 0);
            
            return res;
        }
    }
    
    public StringEntry getStringEntry(String srcText)
    {
        return (StringEntry) m_strEntryHash.get(srcText);
    }
    
    ////////////////////////////////////////////////////////
    // simple project info
    
    public String	sourceRoot()
    { return m_config.getSourceRoot();		}
    
    public int		numEntries()
    { return m_srcTextEntryArray.size(); }
    public MainWindow getTransFrame()
    { return m_transFrame;	}
    
    public ArrayList	getTransMemory()
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

    
    /** The number of unique translated segments. */
    private int numberofTranslatedSegments;
    /** Signals that the next increase doesn't count -- it's orphane */
    private boolean _dontCountNext = false;
    
    /** Returns the number of unique translated segments. */
    public int getNumberofTranslatedSegments()
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
    
    private LinkedList m_requestQueue;
    
    private int m_totalWords;
    private int m_partialWords;
    private int m_currentWords;
    
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
    private HashMap     m_strEntryHash; 
    private ArrayList	m_strEntryList;
    private ArrayList	m_srcTextEntryArray;
    
    /** the list of legacy TMX files, each object is the list of string entries */
    private List m_legacyTMs;
    
    private ArrayList	m_tmList;
    private ArrayList	m_orphanedList;
    
    private ArrayList	m_extensionList;
    private ArrayList	m_extensionMapList;
    
    private GlossaryManager m_glossary;
}
