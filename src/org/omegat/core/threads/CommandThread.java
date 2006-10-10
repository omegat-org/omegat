/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               Home page: http://www.omegat.org/omegat/omegat.html
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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.LegacyTM;
import org.omegat.core.StringEntry;
import org.omegat.core.TransMemory;
import org.omegat.core.glossary.GlossaryManager;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.ProjectFrame;
import org.omegat.core.ProjectProperties;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileData;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.TMXReader;

/**
 * CommandThread is a thread to asynchronously do the stuff
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
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
            
            // build word count
			buildProjectStats();
            
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
    private void buildTMXFile(String filename, boolean forceValidTMX, boolean addOrphans)
            throws IOException {
        buildTMXFile(filename, forceValidTMX, addOrphans, false);
    }

    private void buildTMXFile(String filename, boolean forceValidTMX, boolean addOrphans, boolean levelTwo) 
            throws IOException
    {
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
        PrintWriter out = new PrintWriter(osw); // PW is easier to use than Buff.Writer
        
        // Write TMX header
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");              // NOI18N
        out.println("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">");                     // NOI18N
        //out.println("<tmx version=\"1.1\">");                                   // NOI18N
        out.println(levelTwo ? "<tmx version=\"1.4\">" : "<tmx version=\"1.1\">"); // NOI18N
        out.println("  <header");                                               // NOI18N
        out.println("    creationtool=\"OmegaT\"");                             // NOI18N
        out.println("    creationtoolversion=\""+TMXReader.CTV_OMEGAT_1_6_RC12+"\"");  // NOI18N
        out.println("    segtype=\"" + segmenting + "\"");                      // NOI18N
        out.println("    o-tmf=\"OmegaT TMX\"");                                // NOI18N
        out.println("    adminlang=\"EN-US\"");                                 // NOI18N
        out.println("    srclang=\"" + sourceLocale + "\"");                    // NOI18N
        out.println("    datatype=\"plaintext\"");                              // NOI18N
        out.println("  >");                                                     // NOI18N
        out.println("  </header>");                                             // NOI18N
        out.println("  <body>");                                                // NOI18N

        // Write TUs
        String source = null;
        String target = null;
        for (int i = 0; i < m_strEntryList.size(); i++)
        {
            se = (StringEntry) m_strEntryList.get(i);
            source = forceValidTMX ? StaticUtils.stripTags(se.getSrcText())
                                   : se.getSrcText();
            target = forceValidTMX ? StaticUtils.stripTags(se.getTranslation())
                                   : se.getTranslation();
            if (target.length() == 0)
                continue;
            source = StaticUtils.makeValidXML(source);
            target = StaticUtils.makeValidXML(target);

            // TO DO: This *possibly* converts occurrences in the actual text of &lt;fX&gt;
            //        which it should not.
            if (levelTwo) {
               source = makeLevelTwo(source);
               target = makeLevelTwo(target);
            }
            out.println("    <tu>");                                            // NOI18N
            //out.println("      <tuv lang=\"" + sourceLocale + "\">");           // NOI18N
            out.println("      <tuv xml:lang=\"" + sourceLocale + "\">");           // NOI18N
            out.println("        <seg>" + source + "</seg>");                   // NOI18N
            out.println("      </tuv>");                                        // NOI18N
            //out.println("      <tuv lang=\"" + targetLocale + "\">");           // NOI18N
            out.println("      <tuv xml:lang=\"" + targetLocale + "\">");           // NOI18N
            out.println("        <seg>" + target + "</seg>");                   // NOI18N
            out.println("      </tuv>");                                        // NOI18N
            out.println("    </tu>");                                           // NOI18N
        }
        
        // Write orphan strings
        if (addOrphans) {
            TransMemory transMem;
            for (int i = 0; i < m_orphanedList.size(); i++)
            {
                transMem = (TransMemory) m_orphanedList.get(i);
                if (transMem.target.length() == 0)
                    continue;
                source = forceValidTMX ? StaticUtils.stripTags(transMem.source)
                                       : transMem.source;
                target = forceValidTMX ? StaticUtils.stripTags(transMem.target)
                                       : transMem.target;
                if (levelTwo) {
                    source = makeLevelTwo(source);
                    target = makeLevelTwo(target);
                }
                if (target.length() == 0)
                    continue;
                source = StaticUtils.makeValidXML(source);
                target = StaticUtils.makeValidXML(target);
                out.println("    <tu>");                                            // NOI18N
                //out.println("      <tuv lang=\"" + sourceLocale + "\">");           // NOI18N
                out.println("      <tuv xml:lang=\"" + sourceLocale + "\">");           // NOI18N
                out.println("        <seg>" + source + "</seg>");                   // NOI18N
                out.println("      </tuv>");                                        // NOI18N
                //out.println("      <tuv lang=\"" + targetLocale + "\">");           // NOI18N
                out.println("      <tuv xml:lang=\"" + targetLocale + "\">");           // NOI18N
                out.println("        <seg>" + target + "</seg>");                   // NOI18N
                out.println("      </tuv>");                                        // NOI18N
                out.println("    </tu>");                                           // NOI18N
            }
        }
        
        // Write TMX footer
        out.println("  </body>");                                               // NOI18N
        out.println("</tmx>");                                                  // NOI18N
        
        // Close output stream
        out.close();
    }

    /**
      * Creates three-quarted-assed TMX level 2 segments from OmegaT internal segments
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private String makeLevelTwo(String segment) {
       // Create a storage buffer for the result
       StringBuffer result = new StringBuffer(segment.length() * 2);

       // Create a pattern matcher for numbers
       Matcher numberMatch = Pattern.compile("\\d+").matcher("");

       // Find all single tags
       //Matcher match = Pattern.compile("&lt;[a-zA-Z\-]+\\d+/&gt;").matcher(segment);
       Matcher match = Pattern.compile("&lt;[\\S&&[^/\\d]]+\\d+/&gt;").matcher(segment);
       int previousMatchEnd = 0;
       while (match.find()) {
          // Get the OmegaT tag number
          numberMatch.reset(match.group());
          numberMatch.find();
          String tagNumber = numberMatch.group(); // Should *always* find one, but test this

          // Wrap the OmegaT tag in TMX tags in the result
          result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
          result.append("<ph x='");    // TMX start tag + i attribute
          result.append(tagNumber);    // OmegaT tag number used as x attribute
          result.append("'>");
          result.append(match.group()); // OmegaT tag
          result.append("</ph>");      // TMX end tag

          // Store the current match's end positions
          previousMatchEnd = match.end();
       }

       // Append the text from the last match (single tag) to the end of the segment
       result.append(segment.substring(previousMatchEnd, segment.length()));
       segment = result.toString(); // Store intermediate result back in segment
       result.setLength(0); // Clear result buffer

       // Find all start tags
       match = Pattern.compile("&lt;[\\S&&[^/\\d]]+\\d+&gt;").matcher(segment);
       previousMatchEnd = 0;
       while (match.find()) {
          // Get the OmegaT tag and tag number
          String tag = match.group();
          numberMatch.reset(tag);
          numberMatch.find();
          String tagNumber = numberMatch.group(); // Should *always* find one, but test this

          // Check if the corresponding end tag is in this segment too
          String endTag = "&lt;/" + tag.substring(4);
          boolean paired = segment.contains(endTag);

          // Wrap the OmegaT tag in TMX tags in the result
          result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
          if (paired) {
             result.append("<bpt i='"); // TMX start tag + i attribute
             result.append(tagNumber);  // OmegaT tag number used as i attribute
             result.append("'");
          }
          else {
             result.append("<it pos='begin'"); // TMX start tag
          }
          result.append(" x='");    // TMX x attribute
          result.append(tagNumber); // OmegaT tag number used as x attribute
          result.append("'>");
          result.append(tag);       // OmegaT tag
          result.append(paired ? "</bpt>" : "</it>"); // TMX end tag

          // Store the current match's end positions
          previousMatchEnd = match.end();
       }

       // Append the text from the last match (start tag) to the end of the segment
       result.append(segment.substring(previousMatchEnd, segment.length()));
       segment = result.toString(); // Store intermediate result back in segment
       result.setLength(0); // Clear result buffer

       // Find all end tags
       match = Pattern.compile("&lt;/[\\S&&[^\\d]]+\\d+&gt;").matcher(segment);
       previousMatchEnd = 0;
       while (match.find()) {
          // Get the OmegaT tag and tag number
          String tag = match.group();
          numberMatch.reset(tag);
          numberMatch.find();
          String tagNumber = numberMatch.group(); // Should *always* find one, but test this

          // Check if the corresponding start tag is in this segment too
          String startTag = "&lt;" + tag.substring(5);
          boolean paired = segment.contains(startTag);

          // Wrap the OmegaT tag in TMX tags in the result
          result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
          result.append(paired ? "<ept i='" : "<it pos='end' x='"); // TMX start tag + i/x attribute
          result.append(tagNumber);                                 // OmegaT tag number used as i/x attribute
          result.append("'>");
          result.append(tag);                                       // OmegaT tag
          result.append(paired ? "</ept>" : "</it>");               // TMX end tag

          // Store the current match's end positions
          previousMatchEnd = match.end();
       }

       // Append the text from the last match (end tag) to the end of the segment
       result.append(segment.substring(previousMatchEnd, segment.length()));
       //segment = result.toString(); // Store intermediate result back in segment
       //result.setLength(0); // Clear result buffer

       // Done, return result
       return result.toString();
    }

    /**
      * Replaces the tag by its proper TMX level 2 representation, even if three-quarter-assed
      */
    private String replaceDual(String text, String tag) {
       // NOTE: This implementation may be only half-assed. Please consider making
       //       it at least three-quarter-assed, of fully-assed if possible :)

       String result = text;

       // Replace all occurrences of <fX> / </fX>, where X is any number
       // TO DO/NOTE/FIX: 50 is an arbitrary number, chosen simply because
       //                 currently I can't think of a better way to detect
       //                 the end of all possible occurrences. Please improve.
       for (int i = 0; i <= 50; i++) {
          // Check for occurrences of <fI>, where I = i and f = tag
          int location = result.indexOf("&lt;" + tag + i + "&gt;");
          if (location > -1) {
             int locationEnd = result.indexOf(';', location + 5);
             result =   result.substring(0, location)                      // start of string, up to <fI>
                      + "<bpt i='" + i + "'>&lt;" + tag + i + "&gt;</bpt>" // replacement for <fI>
                      + result.substring(locationEnd + 1);                 // end of string, after <fI>
          }

          // Check for occurrences of </fI>, where I = i
          location = result.indexOf("&lt;/" + tag + i + "&gt;");
          if (location > -1) {
             int locationEnd = result.indexOf(';', location + 5);
             result =   result.substring(0, location)                       // start of string, up to </fI>
                      + "<ept i='" + i + "'>&lt;/" + tag + i + "&gt;</ept>" // replacement for <fI>
                      + result.substring(locationEnd + 1);                  // end of string, after </fI>
          }
       }

       return result;
    }
    
    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     */
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
    
    /** Builds all translated files and creates fresh TM files. */
    public void compileProject()
            throws IOException, TranslationException
    {
        if (m_strEntryHash.size() == 0)
            return;
        
        // save project first
        save();

        // build 2 TMX files, one with OmegaT formatting tags,
        // one without, making it TMX level 1 compliant        
        try
        {
            // build TMX with OmegaT tags
            String fname = m_config.getProjectRoot() + m_config.getProjectName() +
                OConsts.OMEGAT_TMX + OConsts.TMX_EXTENSION;
            buildTMXFile(fname, false, false);
            
            // build TMX level 1 compliant file
            fname = m_config.getProjectRoot() + m_config.getProjectName() +
                OConsts.LEVEL1_TMX + OConsts.TMX_EXTENSION;
            buildTMXFile(fname, true, false);

            // build three-quarter-assed TMX level 2 file
            fname = m_config.getProjectRoot() + m_config.getProjectName() +
                OConsts.LEVEL2_TMX + OConsts.TMX_EXTENSION;
            buildTMXFile(fname, false, false, true);
        }
        catch (IOException e)
        {
            StaticUtils.log(OStrings.getString("CT_ERROR_CREATING_TMX"));
            StaticUtils.log(e.getMessage());
            e.printStackTrace(StaticUtils.getLogStream());
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") +
                    "\n" +                                                      // NOI18N
                    e.getMessage());
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
        
        Set processedFiles = new HashSet();
        
        for(int i=0; i<fileList.size(); i++)
        {
            String filename = (String) fileList.get(i);
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            // shorten filename to that which is relative to src root
            String midName = filename.substring(srcRoot.length());
            m_transFrame.setMessageText(OStrings.CT_COMPILE_FILE_MX + midName);
            
            fm.translateFile(srcRoot, midName, locRoot, processedFiles);
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
            buildTMXFile(s, false, true);
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
        
        StringEntry strEntry = (StringEntry) m_strEntryHash.get(srcText);
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

    /** Finds and loads project's TMX file with translations (project_save.tmx). */
    private void loadTranslations()
    {
        File proj;
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
            m_transFrame.setMessageText(OStrings.getString("CT_LOAD_TMX"));
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
        if (!m_config.loadExisting(m_transFrame, projectRoot))
            return false;
        
        projectClosing = false;
        
        // first load glossary files
        m_glossary = new GlossaryManager();
        m_glossary.loadGlossaryFiles(new File(m_config.getGlossaryRoot()));
        
        // now open source files
        FilterMaster fm = FilterMaster.getInstance();
        
        ArrayList srcFileList = new ArrayList();
        File root = new File(m_config.getSourceRoot());
        StaticUtils.buildFileList(srcFileList, root, true);
        
        Set processedFiles = new HashSet();
        
        for (int i=0; i<srcFileList.size(); i++)
        {
            String filename = (String) srcFileList.get(i);
            File file = new File(filename);
            if (processedFiles.contains(file))
                continue;
            
            // strip leading path information;
            // feed file name to project window
            String filepath = filename.substring(m_config.getSourceRoot().length());
            
            m_transFrame.setMessageText(OStrings.CT_LOAD_FILE_MX + filepath);
            
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
        m_transFrame.setMessageText(OStrings.getString("CT_LOAD_SRC_COMPLETE"));
        m_curFile = null;
        loadTranslations();
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
    
    /** Locates and loads external TMX files with legacy translations. */
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
        tmx.loadFile(fname, isProject);

        int num = tmx.numSegments();
        ArrayList strEntryList = new ArrayList(num);
        ArrayList strOrphaneList = null;

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

    /** Formats date (in milliseconds) to YYYYMMDDHHMM form. */
    private String millisToDateTime(long millis)
    {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(millis);
        
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH)+1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        
        return pad2(year) + pad2(month) + pad2(day) + 
                pad2(hour) + pad2(minute);
    }
    
    /** Make the number at least two digits long (prepends 0). */
    private String pad2(int n)
    {
        if (n<10)
            return "0" + Integer.toString(n);                                   // NOI18N
        else
            return Integer.toString(n);
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
            List tmxs = Arrays.asList(tmxFolder.listFiles(new FilenameFilter()
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
                Collections.sort(tmxs, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        File f1 = (File)o1; 
                        File f2 = (File)o2;
                        if( f1.lastModified()==f2.lastModified() )
                            return 0;
                        else if ( f1.lastModified()>f2.lastModified() )
                            return -1;
                        else
                            return 1;
                    }
                });
                for(int i=MAX_BACKUPS; i<tmxs.size(); i++)
                    ((File)tmxs.get(i)).delete();
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
        StaticUtils.log(OStrings.LD_ERROR + " " + msg); // NOI18N
        e.printStackTrace(StaticUtils.getLogStream());
        e.printStackTrace();
        StaticUtils.log("----------------------------"); // NOI18N
        if( m_transFrame!=null )
            MessageRelay.uiMessageDisplayError(m_transFrame, msg, e);
    }

    /** Computes the number of words in a string. */
    private int numberOfWords(String str)
    {
        int len = str.length();
        if (len==0)
            return 0;
        int nTokens = 0;
        BreakIterator breaker = StaticUtils.getWordBreaker();
        breaker.setText(str);
        
        String tokenPrev;
        String tokenStr = new String();
        
        int start = breaker.first();
        for (int end = breaker.next(); end!=BreakIterator.DONE; 
                start = end, end = breaker.next())
        {
            tokenPrev = tokenStr;
            tokenStr = str.substring(start,end);
            boolean word = false;
            for (int i=0; i<tokenStr.length(); i++)
            {
                char ch = tokenStr.charAt(i);
                if (Character.isLetterOrDigit(ch))
                {
                    word = true;
                    break;
                }
            }
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches())
            {
                nTokens++;
            }
        }
        return nTokens;
    }
    
    /** Computes the number of characters excluding spaces in a string. */
    private int numberOfCharactersWithoutSpaces(String str)
    {
        int chars = 0;
        for (int i=0; i<str.length(); i++)
        {
            if (!Character.isSpaceChar(str.charAt(i)))
                chars++;
        }
        return chars;
    }
    
    /**
     * Builds a file with statistic info about the project.
     * The total word & character count of the project, the total number of 
     * unique segments, plus the details for each file.
     */
    private void buildProjectStats()
    {
        int I_WORDS = 0, I_WORDSLEFT=1, I_CHARSNSP=2, I_CHARSNSPLEFT=3, I_CHARS=4, I_CHARSLEFT=5;
        
        int totalWords = 0,
                uniqueWords = 0,
                totalCharsNoSpaces = 0,
                uniqueCharsNoSpaces = 0,
                totalChars = 0,
                uniqueChars = 0;                
        for (int i=0; i<m_strEntryList.size(); i++)
        {
            StringEntry se = (StringEntry) m_strEntryList.get(i);
            String src = se.getSrcText();
            int dups = se.getParentList().size();
            
            int words = numberOfWords(src);
            uniqueWords += words;
            totalWords += words * dups;
            
            int charsNoSpaces = numberOfCharactersWithoutSpaces(src);
            uniqueCharsNoSpaces += charsNoSpaces;
            totalCharsNoSpaces += charsNoSpaces * dups;
            
            int chars = src.length();
            uniqueChars += chars;
            totalChars += chars * dups;
        }

        int remainingSegments = getNumberOfUniqueSegments()-getNumberofTranslatedSegments(),
                remainingWords = 0,
                remainingCharsNoSpaces = 0,
                remainingChars = 0;
        SortedMap counts = new TreeMap();
        for (int i = 0; i < m_srcTextEntryArray.size(); i++)
        {
            SourceTextEntry ste = (SourceTextEntry) m_srcTextEntryArray.get(i);
            String fileName = ste.getSrcFile().name;
            fileName = StaticUtils.makeFilenameRelative(fileName, getProjectProperties().getSourceRoot());
            int[] numbers; // [0] - words, [1] - left words
            if( counts.containsKey(fileName) )
                numbers = (int[]) counts.get(fileName);
            else
                numbers = new int[] {0, 0, 0, 0, 0, 0};

            String src = ste.getSrcText();
            int words = numberOfWords(src);
            numbers[I_WORDS] += words;
            int charsNoSpaces = numberOfCharactersWithoutSpaces(src);
            numbers[I_CHARSNSP] += charsNoSpaces;
            int chars = src.length();
            numbers[I_CHARS] += chars;

            if( !ste.isTranslated() )
            {
                remainingWords += words;
                numbers[I_WORDSLEFT] += words;
                remainingCharsNoSpaces += charsNoSpaces;
                numbers[I_CHARSNSPLEFT] += charsNoSpaces;
                remainingChars += chars;
                numbers[I_CHARSLEFT] += chars;
            }
            counts.put(fileName, numbers);
        }

        try
        {
            // removing old stats
            try
            {
                File oldstats = new File(m_config.getProjectInternal()+"word_counts"); // NOI18N
                if (oldstats.exists())
                    oldstats.delete();
            }
            catch (Exception e) {}
            
            // now dump file based word counts to disk
            String fn = m_config.getProjectInternal() + OConsts.STATS_FILENAME;
            Writer ofp = new OutputStreamWriter(new FileOutputStream(fn), OConsts.UTF8);
            ofp.write(OStrings.getString("CT_STATS_Project_Statistics") +
                    "\n\n");                                                    // NOI18N

            ofp.write(OStrings.getString("CT_STATS_Total") +
                    "\n");                                                      // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Segments") +
                    "\t"+getNumberOfSegmentsTotal()+"\n");                      // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Words") +
                    "\t" +totalWords+ "\n");                                    // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") +
                    "\t" +totalCharsNoSpaces+ "\n");                            // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters") +
                    "\t" +totalChars+ "\n");                                    // NOI18N

            ofp.write(OStrings.getString("CT_STATS_Unique") +
                    "\n");                                                      // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Segments") +
                    "\t"+getNumberOfUniqueSegments()+"\n");                     // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Words") +
                    "\t" +uniqueWords+ "\n");                                   // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") +
                    "\t" +uniqueCharsNoSpaces+ "\n");                           // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters") +
                    "\t" +uniqueChars+ "\n");                                   // NOI18N
            
            ofp.write(OStrings.getString("CT_STATS_Unique_Remaining") +
                    "\n");                                                      // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Segments") +
                    "\t"+remainingSegments+"\n");                               // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Words") +
                    "\t" +remainingWords+ "\n");                                // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") +
                    "\t" +remainingCharsNoSpaces+ "\n");                        // NOI18N
            ofp.write("\t"+                                                     // NOI18N
                    OStrings.getString("CT_STATS_Characters") +
                    "\t" +remainingChars+ "\n");                                // NOI18N
            
            ofp.write("\n");                                                    // NOI18N
            ofp.write(OStrings.getString("CT_STATS_FILE_Statistics") +
                    "\n\n");                                                    // NOI18N
            
            ofp.write(OStrings.getString("CT_STATS_FILE_Name") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Words") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Words") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters") +
                    "\t" +                                                      // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters") +
                    "\n");                                                      // NOI18N
            
            Iterator it = counts.keySet().iterator();
            while( it.hasNext() )
            {
                String filename = (String) it.next();
                int[] numbers = (int[]) counts.get(filename);
                ofp.write(filename + 
                        "\t" + numbers[I_WORDS] + "\t" + numbers[I_WORDSLEFT] +         // NOI18N
                        "\t" + numbers[I_CHARSNSP] + "\t" + numbers[I_CHARSNSPLEFT] +   // NOI18N
                        "\t" + numbers[I_CHARS] + "\t" + numbers[I_CHARSLEFT] +         // NOI18N
                        "\n");                                                          // NOI18N
            }
            
            ofp.close();
        }
        catch (IOException e) {}
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
            return (SourceTextEntry) m_srcTextEntryArray.get(num);
        }
        catch( IndexOutOfBoundsException iobe )
        {
            StringEntry str = new StringEntry(
                    OStrings.getString("TF_INTRO_EMPTYPROJECT"));
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

