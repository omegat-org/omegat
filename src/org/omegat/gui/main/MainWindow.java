/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula
 Portions copyright 2008 Alex Buloichik
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

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.ProjectProperties;
import org.omegat.core.StringEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.SearchWindow;
import org.omegat.gui.TagValidationFrame;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;
import org.omegat.util.WikiGet;
import org.omegat.util.gui.DockingUI;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.Styles;

import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;

/**
 * The main window of OmegaT application.
 *
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindow extends JFrame implements WindowListener, ComponentListener, IMainWindow, IProjectEventListener, IApplicationEventListener
{
    protected final MainWindowMenu menu;
    
    /** Creates new form MainWindow */
    public MainWindow()
    {
        m_searches = new HashSet<SearchWindow>();
        menu = new MainWindowMenu(this, new MainWindowMenuHandler(this));
        initComponents();
        createMainComponents();
        initDocking();
        additionalUIInit();
        oldInit();
        loadInstantStart();
        CoreEvents.registerApplicationEventListener(this);
        CoreEvents.registerProjectChangeListener(this);
    }
    
    public void onApplicationStartup() {
        onProjectChanged();
    }
    
    public void onApplicationShutdown() {
    }

    private void createMainComponents()
    {
        editor = new EditorTextArea(this);
        matches = new MatchesTextArea(this);
        glossary = new GlossaryTextArea();

        String fontName = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
        int fontSize = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE, OConsts.TF_FONT_SIZE_DEFAULT);

        m_font = new Font(fontName, Font.PLAIN, fontSize);
        editor.setFont(m_font);
        matches.setFont(m_font);
        glossary.setFont(m_font);
        m_autoSpellChecking = Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
    }
    
    private void initDocking()
    {
        DockingUI.initialize();

        editorScroller = new DockableScrollPane("EDITOR", " ", editor, false);  // NOI18N
        editorScroller.setMinimumSize(new Dimension(100, 100));
        matchesScroller = new DockableScrollPane("MATCHES",                     // NOI18N
                OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches"), matches, true);
        glossaryScroller = new DockableScrollPane("GLOSSARY",                   // NOI18N
                OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Glossary"), glossary, true);
        
        desktop = new DockingDesktop();
        desktop.addDockableStateWillChangeListener(new DockableStateWillChangeListener()
        {
            public void dockableStateWillChange(DockableStateWillChangeEvent event)
            {
                if (event.getFutureState().isClosed())
                    event.cancel();
            }
        });
        desktop.addDockable(editorScroller);
        desktop.split(editorScroller, matchesScroller, DockingConstants.SPLIT_RIGHT);
        desktop.split(matchesScroller, glossaryScroller, DockingConstants.SPLIT_BOTTOM);
        desktop.setDockableWidth(editorScroller, 0.6);
        desktop.setDockableHeight(matchesScroller, 0.7);
        
        getContentPane().add(desktop, BorderLayout.CENTER);
    }
    
    /**
     * Some additional actions to initialize UI,
     * not doable via NetBeans Form Editor
     */
    private void additionalUIInit()
    {
        updateTitle();
        loadWindowIcon();
        m_projWin = new ProjectFrame(this);
        m_projWin.setFont(m_font);

        statusLabel.setText(new String()+' ');
        
        loadScreenLayout();
        updateCheckboxesOnStart();
        uiUpdateOnProjectClose();
    }

    /**
     * Loads and set main window's icon.
     */
    private void loadWindowIcon()
    {
        try
        {
            URL resource = getClass().getResource("/org/omegat/gui/resources/OmegaT_small.gif");  // NOI18N
            ImageIcon imageicon = new ImageIcon(resource);
            Image image = imageicon.getImage();
            setIconImage(image);
        }
        catch( Exception e )
        {
            Log.log(e);
        }
    }
    
    /**
     * Sets the title of the main window appropriately
     */
    private void updateTitle()
    {
        String s = OStrings.getDisplayVersion();
        if(isProjectLoaded())
        {
            s += " :: " + m_activeProj;                                         // NOI18N
            try
            {
                //String file = m_activeFile.substring(CommandThread.core.sourceRoot().length());
                String file = getActiveFileName();
 //               Log.log("file = "+file);
                // RFE [1764103] Editor window name 
                editorScroller.setName(StaticUtils.format( 
                OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), 
                                   new Object[] {file})); 
            } catch( Exception e ) { }
        }
        // Fix for bug [1730935] Editor window still shows filename after closing project and
        // RFE [1604238]: instant start display in the main window
        else
        {
            loadInstantStart();
        }
        setTitle(s);
    }
    
    /**
     * Old Initialization.
     */
    public void oldInit()
    {
        m_curEntryNum = -1;
        m_activeProj = new String();
        //m_activeFile = new String();
        
        ////////////////////////////////
        
        enableEvents(0);
        
        // check this only once as it can be changed only at compile time
        // should be OK, but localization might have messed it up
        String start = OConsts.segmentStartStringFull;
        int zero = start.lastIndexOf('0');
        m_segmentTagHasNumber = (zero > 4) && // 4 to reserve room for 10000 digit
                (start.charAt(zero - 1) == '0') &&
                (start.charAt(zero - 2) == '0') &&
                (start.charAt(zero - 3) == '0');
    }
    
    /** Updates menu checkboxes from preferences on start */
    private void updateCheckboxesOnStart() {
        if (Preferences.isPreference(Preferences.USE_TAB_TO_ADVANCE)) {
            menu.optionsTabAdvanceCheckBoxMenuItem.setSelected(true);
            m_advancer = KeyEvent.VK_TAB;
        } else {
            m_advancer = KeyEvent.VK_ENTER;
        }
        menu.optionsAlwaysConfirmQuitCheckBoxMenuItem.setSelected(Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT));
        
        if (Preferences.isPreference(Preferences.MARK_TRANSLATED_SEGMENTS)) {
            menu.viewMarkTranslatedSegmentsCheckBoxMenuItem.setSelected(true);
            m_translatedAttributeSet = Styles.TRANSLATED;
        } else if (Preferences.isPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS)) {
            menu.viewMarkUntranslatedSegmentsCheckBoxMenuItem.setSelected(true);
            m_unTranslatedAttributeSet = Styles.UNTRANSLATED;
        } else {
            m_translatedAttributeSet = Styles.PLAIN;
            m_unTranslatedAttributeSet = Styles.PLAIN;
        }
        if (Preferences.isPreference(Preferences.DISPLAY_SEGMENT_SOURCES)) {
            menu.viewDisplaySegmentSourceCheckBoxMenuItem.setSelected(true);
            m_displaySegmentSources = true;
        }
    }
    
    private boolean layoutInitialized = false;
    
    /**
     * Initialized the sizes of OmegaT window.
     * <p>
     * Assume screen size is 800x600 if width less than 900, and
     * 1024x768 if larger. Assume task bar at bottom of screen.
     * If screen size saved, recover that and use instead
     * (18may04).
     */
    private void loadScreenLayout()
    {
        // main window
        try
        {
            String dx = Preferences.getPreference(Preferences.MAINWINDOW_X);
            String dy = Preferences.getPreference(Preferences.MAINWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.MAINWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.MAINWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        }
        catch (NumberFormatException nfe)
        {
            // size info missing - put window in default position
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle scrSize = env.getMaximumWindowBounds();
            if (scrSize.width < 900)
            {
                // assume 800x600
                setSize(580, 536);
                setLocation(0, 0);
            }
            else
            {
                // assume 1024x768 or larger
                setSize(690, 700);
                setLocation(0, 0);
            }
        }

        String layout = Preferences.getPreference(Preferences.MAINWINDOW_LAYOUT);
        if (layout.length()>0)
        {
            byte[] bytes = StaticUtils.uudecode(layout);
            try
            {
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                desktop.readXML(in);
                in.close();
            } catch (Exception e) { }
        }
        
        layoutInitialized = true;
    }
    
    public void filelistWindowClosed()
    {
    }
    
    /** Loads Instant start article */
    private void loadInstantStart()
    {
        try
        {
            String language = detectInstantStartLanguage();
            String filepath =
                    StaticUtils.installDir()
                    + File.separator + OConsts.HELP_DIR + File.separator
                    + language + File.separator
                    + OConsts.HELP_INSTANT_START;
            JTextPane instantArticlePane = new JTextPane();
            instantArticlePane.setEditable(false);
            instantArticlePane.setPage("file:///"+filepath);                    // NOI18N
            editorScroller.setViewportView(instantArticlePane);
            editorScroller.setName(OStrings.getString("DOCKING_INSTANT_START_TITLE"));
        }
        catch (IOException e)
        {
            editorScroller.setViewportView(editor);
        }
    }
    
    /**
      * Detects the language of the instant start guide
      * (checks if present in default locale's language).
      *
      * If there is no instant start guide in the default
      * locale's language, "en" (English) is returned, otherwise
      * the acronym for the default locale's language.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private String detectInstantStartLanguage() {
        // Get the system language and country
        String language = java.util.Locale.getDefault().getLanguage().toLowerCase();
        String country  = java.util.Locale.getDefault().getCountry().toUpperCase();

        // Check if there's a translation for the full locale (lang + country)
        File isg = new File(StaticUtils.installDir()
            + File.separator + OConsts.HELP_DIR
            + File.separator + language + "_" + country
            + File.separator + OConsts.HELP_INSTANT_START);
        if (isg.exists())
            return language + "_" + country;

        // Check if there's a translation for the language only
        isg = new File(StaticUtils.installDir()
            + File.separator + OConsts.HELP_DIR
            + File.separator + language
            + File.separator + OConsts.HELP_INSTANT_START);
        if(isg.exists())
            return language;

        // Default to English, if no translation exists
        return "en";                                                        // NOI18N
    }
    
    /**
     * Stores screen layout (width, height, position, etc).
     */
    public void saveScreenLayout()
    {
        if (!layoutInitialized)
            return;
        
        Preferences.setPreference(Preferences.MAINWINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.MAINWINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.MAINWINDOW_X, getX());
        Preferences.setPreference(Preferences.MAINWINDOW_Y, getY());
        
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            desktop.writeXML(out);
            out.close();
            byte[] buf = out.toByteArray();
            String layout = StaticUtils.uuencode(buf);
            Preferences.setPreference(Preferences.MAINWINDOW_LAYOUT, layout);
        } 
        catch (Exception e) 
        {
            Preferences.setPreference(Preferences.MAINWINDOW_LAYOUT, new String());
        }
    }
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // command handling
    
    void doValidateTags()
    {
        List<SourceTextEntry> suspects = CommandThread.core.validateTags();
        if (suspects.size() > 0)
        {
            // create a tag validation window if necessary
            if (m_tagWin == null) {
                m_tagWin = new TagValidationFrame(this);
                m_tagWin.addWindowListener(this);
                m_tagWin.setFont(m_font);
            } else {
                // close tag validation window if present
                m_tagWin.dispose();
            }

            // display list of suspect strings
            m_tagWin.setVisible(true);
            m_tagWin.displayStringList(suspects);
        }
        else
        {
            // close tag validation window if present
            if (m_tagWin != null)
                m_tagWin.dispose();

            // show dialog saying all is OK
            JOptionPane.showMessageDialog(this,
                    OStrings.getString("TF_NOTICE_OK_TAGS"),
                    OStrings.getString("TF_NOTICE_TITLE_TAGS"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public synchronized void doNextEntry()
    {
        if (!isProjectLoaded())
            return;
        
        commitEntry();
        
        m_curEntryNum++;
        if (m_curEntryNum > m_xlLastEntry)
        {
            if (m_curEntryNum >= CommandThread.core.numEntries())
                m_curEntryNum = 0;
            loadDocument();
        }
        
        activateEntry();
    }
    
    public synchronized void doPrevEntry()
    {
        if (!isProjectLoaded())
            return;
        
        commitEntry();
        
        m_curEntryNum--;
        if (m_curEntryNum < m_xlFirstEntry)
        {
            if (m_curEntryNum < 0)
                m_curEntryNum = CommandThread.core.numEntries() - 1;
            // empty project bugfix:
            if (m_curEntryNum < 0)
                m_curEntryNum = 0;
            loadDocument();
        }
        activateEntry();
    }
    
    
    
    /**
     * Finds the next untranslated entry in the document.
     * <p>
     * Since 1.6.0 RC9 also looks from the beginning of the document
     * if there're no untranslated till the end of document.
     * This way it look at entire project like Go To Next Segment does.
     *
     * @author Henry Pijffers
     * @author Maxym Mykhalchuk
     */
    synchronized void doNextUntranslatedEntry()
    {
        // check if a document is loaded
        if (isProjectLoaded() == false)
            return;
        
        // save the current entry
        commitEntry();
        
        // get the total number of entries
        int numEntries = CommandThread.core.numEntries();
        
        boolean found = false;
        int curEntryNum;
        
        // iterate through the list of entries,
        // starting at the current entry,
        // until an entry with no translation is found
        for(curEntryNum = m_curEntryNum+1; curEntryNum < numEntries; curEntryNum++)
        {
            // get the next entry
            SourceTextEntry entry = CommandThread.core.getSTE(curEntryNum);
            
            // check if the entry is not null, and whether it contains a translation
            if (entry!=null && entry.getTranslation().length()==0)
            {
                // we've found it
                found = true;
                // stop searching
                break;
            }
        }
        
        // if we haven't found untranslated entry till the end,
        // trying to search for it from the beginning
        if (!found)
        {
            for(curEntryNum = 0; curEntryNum < m_curEntryNum; curEntryNum++)
            {
                // get the next entry
                SourceTextEntry entry = CommandThread.core.getSTE(curEntryNum);

                // check if the entry is not null, and whether it contains a translation
                if (entry!=null && entry.getTranslation().length()==0)
                {
                    // we've found it
                    found = true;
                    // stop searching
                    break;
                }
            }
        }
        
        if (found)
        {
            // mark the entry
            m_curEntryNum = curEntryNum;

            // load the document, if the segment is not in the current document
            if (m_curEntryNum < m_xlFirstEntry || m_curEntryNum > m_xlLastEntry)
                loadDocument();
        }
        
        // activate the entry
        activateEntry();
    }
    
    
   
    /** insert current fuzzy match at cursor position */

    public synchronized void doInsertTrans()
    {
        if (!isProjectLoaded())
            return;
        
        int activeMatch = matches.getActiveMatch();
        if (activeMatch < 0)
            return;
        
        if (activeMatch >= m_curEntry.getStrEntry().getNearListTranslated().size())
            return;
        
        NearString near = m_curEntry.getStrEntry().getNearListTranslated().get(activeMatch);
        doInsertText(near.str.getTranslation());
    }
    
    /** inserts text at the cursor position */
    synchronized void doInsertText(String text)
    {
        synchronized (editor) {
//            int pos = editor.getCaretPosition();
//            editor.select(pos, pos);
// Removing the two lines above implements:
// RFE [ 1579488 ] overwriting with Ctrl+i
            editor.replaceSelection(text);
        }
    }
    
    /** replace entire edit area with active fuzzy match */
    public synchronized void doRecycleTrans()
    {
        if (!isProjectLoaded())
            return;
        
        int activeMatch = matches.getActiveMatch();
        if (activeMatch < 0)
            return;

        if (activeMatch >= m_curEntry.getStrEntry().getNearListTranslated().size())
            return;
        
        NearString near = m_curEntry.getStrEntry().getNearListTranslated().get(activeMatch);
        doReplaceEditText(near.str.getTranslation());
    }
    
    /** replaces the entire edit area with a given text */
    synchronized void doReplaceEditText(String text)
    {
        synchronized (editor) {
            // build local offsets
            int start = getTranslationStart();
            int end = getTranslationEnd();

            // remove text
            editor.select(start, end);
            editor.replaceSelection(text);
        }
    }
    
    /** Closes the project. */
    public void doCloseProject()
    {
        Preferences.save();
        
        if (isProjectLoaded())
            doSave();
        m_projWin.reset();
        synchronized (this) {m_projectLoaded = false;}

        synchronized (this) {
            editor.setText(OStrings.getString("TF_INTRO_MESSAGE"));
        }
        matches.clear();
        glossary.clear();
        
        updateTitle();
        uiUpdateOnProjectClose();
        
        Core.getDataEngine().closeProject();
        progressLabel.setText(OStrings.getString("MW_PROGRESS_DEFAULT"));
        setLengthLabel(OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
    }
    
    public void onProjectChanged() {
        if (Core.getDataEngine().isProjectLoaded()) {
            menu.onProjectStatusChanged(true);
        } else {
            menu.onProjectStatusChanged(false);
        }
    }
    
    /** Updates UI (enables/disables menu items) upon <b>closing</b> project */
    private void uiUpdateOnProjectClose()
    {
        synchronized (editor) {
            editor.setEditable(false);
        }

        // hide project file list
        m_projWin.uiUpdateImportButtonStatus();
        m_projWin.setVisible(false);

        // dispose other windows
        if (m_tagWin != null)
            m_tagWin.dispose();
        for (SearchWindow sw : m_searches) {
            sw.dispose();
        }
        m_searches.clear();
    }
    
    /** Updates UI (enables/disables menu items) upon <b>opening</b> project */
    private void uiUpdateOnProjectOpen()
    {
        synchronized (editor) {
            editor.setEditable(true);
        }
        
        updateTitle();
        m_projWin.buildDisplay();
        
        m_projWin.uiUpdateImportButtonStatus();
        
        m_projWin.setVisible(true);
    }
    
    /**
     * Notifies Main Window that the CommandThread has finished loading the 
     * project.
     * <p>
     * Current implementation commits and re-activates current entry to show 
     * fuzzy matches.
     * <p>
     * Calling Main Window back to notify that project is successfully loaded.
     * Part of bugfix for 
     * <a href="http://sourceforge.net/support/tracker.php?aid=1370838">[1370838]
     * First segment does not trigger matches after load</a>.
     */
    public synchronized void projectLoaded()
    {
        Thread runlater = new Thread()
        {
            public void run()
            {
                updateFuzzyInfo();    // just display the matches, don't commit/activate!
                updateGlossaryInfo(); // and glossary matches
                // commitEntry(false); // part of fix for bug 1409309
                // activateEntry();
            }
        };
        SwingUtilities.invokeLater(runlater);
    }

    void doSave()
    {
        if (!isProjectLoaded())
            return;
        
        showStatusMessage(OStrings.getString("MW_STATUS_SAVING"));
        
        Core.getDataEngine().saveProject();
        
        showStatusMessage(OStrings.getString("MW_STATUS_SAVED"));
    }
    
    /**
     * Creates a new Project.
     */
    void doCreateProject()
    {
        Core.getDataEngine().createProject();
        try
        {
            String projectRoot = CommandThread.core.getProjectProperties().getProjectRoot();
            if( new File(projectRoot).exists() )
                doLoadProject(projectRoot);
        }
        catch( Exception e )
        {
            // do nothing
        }
    }
    
    /**
     * Loads a new project.
     */
    void doLoadProject()
    {
        if (isProjectLoaded())
        {
            displayError( "Please close the project first!", new Exception( "Another project is open")); // NOI18N
            return;
        }

        matches.clear();
        glossary.clear();
        history.clear();
        editorScroller.setViewportView(editor);

        RequestPacket load;
        load = new RequestPacket(RequestPacket.LOAD, this);
        CommandThread.core.messageBoardPost(load);
    }
    
    /**
     * Loads the same project as was open in OmegaT before.
     * @param projectRoot previously closed project's root
     */
    public void doLoadProject(String projectRoot)
    {
        if (isProjectLoaded())
        {
            displayError( "Please close the project first!", new Exception( "Another project is open")); // NOI18N
            return;
        }

        matches.clear();
        glossary.clear();
        history.clear();
        editorScroller.setViewportView(editor);

        RequestPacket load;
        load = new RequestPacket(RequestPacket.LOAD, this, projectRoot);
        CommandThread.core.messageBoardPost(load);
    }

    /**
     * Reloads, i.e. closes and loads the same project.
     */
    public void doReloadProject()
    {
        ProjectProperties config = CommandThread.core.getProjectProperties();
        String projectRoot = config.getProjectRoot();
        doCloseProject();
        doLoadProject(projectRoot);
    }
    
    
    /**
     * Imports the file/files/folder into project's source files.
     * @author Kim Bruning
     * @author Maxym Mykhalchuk
     */
    public void doImportSourceFiles()
    {
        OmegaTFileChooser chooser=new OmegaTFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        int result=chooser.showOpenDialog(this);
        if( result==OmegaTFileChooser.APPROVE_OPTION )
        {
            String projectsource = CommandThread.core.getProjectProperties().getSourceRoot();
            File sourcedir = new File(projectsource);
            File[] selFiles=chooser.getSelectedFiles();
            try
            {
                for(int i=0;i<selFiles.length;i++)
                {
                    File selSrc=selFiles[i];
                    if( selSrc.isDirectory() )
                    {
                        List<String> files = new ArrayList<String>();
                        StaticUtils.buildFileList(files, selSrc, true);
                        String selSourceParent = selSrc.getParent();
                        for(String filename : files)
                        {
                            String midName = filename.substring(selSourceParent.length());
                            File src=new File(filename);
                            File dest=new File(sourcedir, midName);
                            LFileCopy.copy(src, dest);
                        }
                    }
                    else
                    {
                        File dest=new File(sourcedir, selFiles[i].getName());
                        LFileCopy.copy(selSrc, dest);
                    }
                }
                doReloadProject();
            }
            catch(IOException ioe)
            {
                displayError(OStrings.getString("MAIN_ERROR_File_Import_Failed"), ioe);
            }
        }
        
    }

    /** 
    * Does wikiread 
    * @author Kim Bruning
    */
    public void doWikiImport()
    {
        String remote_url = JOptionPane.showInputDialog(this,
                OStrings.getString("TF_WIKI_IMPORT_PROMPT"), 
		OStrings.getString("TF_WIKI_IMPORT_TITLE"),
		JOptionPane.OK_CANCEL_OPTION);
        String projectsource = 
                CommandThread.core.getProjectProperties().getSourceRoot();
         // [1762625] Only try to get MediaWiki page if a string has been entered 
        if ( (remote_url != null ) && (remote_url.trim().length() > 0) )
        {
            WikiGet.doWikiGet(remote_url, projectsource);
            doReloadProject();
        }
    }

    public synchronized void doGotoEntry(int entryNum)
    {
        if (!isProjectLoaded())
            return;
        
        commitEntry();
        
        m_curEntryNum = entryNum - 1;
        if (m_curEntryNum < m_xlFirstEntry)
        {
            if (m_curEntryNum < 0)
                m_curEntryNum = CommandThread.core.numEntries() - 1;
            // empty project bugfix:
            if (m_curEntryNum < 0)
                m_curEntryNum = 0;
            loadDocument();
        }
        else if (m_curEntryNum > m_xlLastEntry)
        {
            if (m_curEntryNum >= CommandThread.core.numEntries())
                m_curEntryNum = 0;
            loadDocument();
        }
        activateEntry();
    }
    
    public synchronized void doGotoEntry(String str)
    {
        int num;
        try
        {
            num = Integer.parseInt(str);
            doGotoEntry(num);
        }
        catch (NumberFormatException e)
        {
        }
    }
    
    public synchronized void finishLoadProject()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public synchronized void run()
            {
                m_activeProj = CommandThread.core.getProjectProperties().getProjectName();
                //m_activeFile = new String();
                m_curEntryNum = 0;
                
                loadDocument();
                synchronized (this) {m_projectLoaded = true;}
                
                uiUpdateOnProjectOpen();
            }
        });
    }
    
    public void searchWindowClosed(SearchWindow searchWindow) {
        m_searches.remove(searchWindow);
    }

    /**
     * Show message in status bar.
     * 
     * @param str
     *                message text
     */
    public void showStatusMessage(String str) {
        if (str.length() == 0)
            str = new String() + ' ';
        statusLabel.setText(str);
    }

    public void setLengthLabel(String str)
    {
        lengthLabel.setText(str);
    }
    
    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    // internal routines

    /**
     * Displays all segments in current document.
     * <p>
     * Displays translation for each segment if it's available,
     * otherwise displays source text.
     * Also stores length of each displayed segment plus its starting offset.
     */
    synchronized void loadDocument()
    {
        m_docReady = false;

        synchronized (editor) {
            // clear old text
            editor.setText(new String());

            // update the title and the project window
            if (isProjectLoaded())
                updateTitle();
            m_projWin.buildDisplay();
            
            m_curEntry = CommandThread.core.getSTE(m_curEntryNum);

            m_xlFirstEntry = m_curEntry.getFirstInFile();
            m_xlLastEntry = m_curEntry.getLastInFile();
            int xlEntries = 1+m_xlLastEntry-m_xlFirstEntry;

            DocumentSegment docSeg;
            m_docSegList = new DocumentSegment[xlEntries];

            int totalLength = 0;
            
            AbstractDocument xlDoc = (AbstractDocument)editor.getDocument();
            AttributeSet attributes = m_translatedAttributeSet;
            
            // if the source should be displayed, too
            AttributeSet srcAttributes = m_unTranslatedAttributeSet;
            
            // how to display the source segment
            if (m_displaySegmentSources)
                srcAttributes = Styles.GREEN;
            
            for (int i=0; i<xlEntries; i++)
            {
                docSeg = new DocumentSegment();

                SourceTextEntry ste = CommandThread.core.getSTE(i+m_xlFirstEntry);
                String sourceText = ste.getSrcText();
                String text = ste.getTranslation();
                
                boolean doSpellcheck = false;
                // set text and font
                if( text.length()==0 )
                {
                    if (!m_displaySegmentSources) {
                    // no translation available - use source text
                    text = ste.getSrcText();
                        attributes = m_unTranslatedAttributeSet;
                }
                } else {
                   doSpellcheck = true;
                   attributes = m_translatedAttributeSet;
                }
                try {
                    if (m_displaySegmentSources) {
                        xlDoc.insertString(totalLength, sourceText+"\n", srcAttributes);
                        totalLength += sourceText.length()+1;
                    }

                    xlDoc.insertString(totalLength,text,attributes);

                    // mark the incorrectly set words, if needed
                    if (doSpellcheck && m_autoSpellChecking) {
                        checkSpelling(totalLength, text);
                    }
                    
                    totalLength += text.length();
                    													// NOI18N
                    xlDoc.insertString(totalLength, "\n\n", Styles.PLAIN);
                    
                    totalLength += 2;
                    
                    if (m_displaySegmentSources) {
                        text = sourceText + "\n" + text;
                    }
                    
                    text += "\n\n";	

                } catch(BadLocationException ble)
                {
                    Log.log(IMPOSSIBLE);
                    Log.log(ble);
                }

                docSeg.length = text.length();
                m_docSegList[i] = docSeg;
            }
        } // synchronized (editor)

        Thread.yield();
    }
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // display oriented code
    
    /**
     * Displays fuzzy matching info if it's available.
     */
    private void updateFuzzyInfo()
    {
        if (!isProjectLoaded())
            return;
        
        StringEntry curEntry = m_curEntry.getStrEntry();
        matches.setMatches(curEntry.getNearListTranslated());
    }
    
    /**
     * Displays glossary terms for the current segment.
     */
    private void updateGlossaryInfo()
    {
        StringEntry curEntry = m_curEntry.getStrEntry();
        glossary.setGlossaryEntries(curEntry.getGlossaryEntries());
    }
    
    /** Is any segment edited currently? */
    private boolean entryActivated = false;
    
    private static final String IMPOSSIBLE = "Should not have happened, " +     // NOI18N
            "report to http://sf.net/tracker/?group_id=68187&atid=520347";      // NOI18N
    
    /**
     * Commits the translation.
     * Reads current entry text and commit it to memory if it's changed.
     * Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     *            if appropriate option is set in Workflow options dialog.
     */
    private synchronized void commitEntry() {
        commitEntry(true);
    }
    
    /**
     * Commits the translation.
     * Reads current entry text and commit it to memory if it's changed.
     * Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     *            if appropriate option is set in Workflow options dialog.
     *
     * @param forceCommit If false, the translation will not be saved
     */
    synchronized void commitEntry(boolean forceCommit) {
        if (!isProjectLoaded())
            return;

        if (!entryActivated)
            return;
        entryActivated = false;

        synchronized (editor) {
            AbstractDocument xlDoc = (AbstractDocument)editor.getDocument();

            AttributeSet attributes = m_translatedAttributeSet;
            
            int start = getTranslationStart();
            int end = getTranslationEnd();
            String display_string;
            String new_translation;
            
            boolean doCheckSpelling = true;
            
            // the list of incorrect words returned eventually by the 
            // spellchecker
            List<Token> wordList = null;
            int flags = IS_NOT_TRANSLATED;
            
            if (start == end)
            {
                new_translation = new String();
                doCheckSpelling = false;
                
                if (!m_displaySegmentSources) {    
                display_string  = m_curEntry.getSrcText();
                    attributes = m_unTranslatedAttributeSet;    
                } else {
                    display_string = new String();
            }
            }
            else
            {
                try
                {
                    new_translation = xlDoc.getText(start, end - start);
                    if (   new_translation.equals(m_curEntry.getSrcText())
                        && !Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC)) {
                        attributes = m_unTranslatedAttributeSet;
                        doCheckSpelling = false;  
                    } else {
                        attributes = m_translatedAttributeSet;
                        flags = 0;
                }
                }
                catch(BadLocationException ble)
                {
                    Log.log(IMPOSSIBLE);
                    Log.log(ble);
                    new_translation = new String();
                    doCheckSpelling = false;
                }
                display_string = new_translation;
            }

            int startOffset = m_segmentStartOffset;
            int totalLen = m_sourceDisplayLength + OConsts.segmentStartStringFull.length() +
                    new_translation.length() + OConsts.segmentEndStringFull.length() + 2;

            int localCur = m_curEntryNum - m_xlFirstEntry;
            DocumentSegment docSeg = m_docSegList[localCur];
            docSeg.length = display_string.length() + "\n\n".length();              // NOI18N
            String segmentSource = null;
    
            if (m_displaySegmentSources) {
                int increment = m_sourceDisplayLength + 1; 
                startOffset += increment;
                //totalLen -= increment;
                docSeg.length += increment;
                segmentSource = m_curEntry.getSrcText();
            }
            
            docSeg.length = replaceEntry(m_segmentStartOffset, totalLen, 
                    segmentSource, display_string, flags);
            
            if (doCheckSpelling && m_autoSpellChecking) {
                wordList = checkSpelling(startOffset, display_string);
            }

            if (forceCommit) { // fix for 
                String old_translation = m_curEntry.getTranslation();
                // update memory
                if (   new_translation.equals(m_curEntry.getSrcText())
                    && !Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC))
                    m_curEntry.setTranslation(new String());
                else
                    m_curEntry.setTranslation(new_translation);
    
                // update the length parameters of all changed segments
                // update strings in display
                if (!m_curEntry.getTranslation().equals(old_translation))
                {
                    // find all identical strings and redraw them
    
                    // build offsets of all strings
                    int localEntries = 1+m_xlLastEntry-m_xlFirstEntry;
                    int[] offsets = new int[localEntries];
                    int currentOffset = 0;
                    for (int i=0; i<localEntries; i++)
                    {
                        offsets[i]=currentOffset;
                        docSeg = m_docSegList[i];
                        currentOffset += docSeg.length;
                    }
    
                    // starting from the last (guaranteed by sorting ParentList)
                    for (SourceTextEntry ste : m_curEntry.getStrEntry().getParentList())
                    {
                        int entry = ste.entryNum();
                        if (entry>m_xlLastEntry)
                            continue;
                        else if (entry<m_xlFirstEntry)
                            break;
                        else if (entry==m_curEntryNum)
                            continue;
    
                        int localEntry = entry-m_xlFirstEntry;
                        int offset = offsets[localEntry];
                        int replacementLength = docSeg.length;
    
                        // replace old text w/ new
                        docSeg = m_docSegList[localEntry];
                        docSeg.length = replaceEntry(offset, docSeg.length, 
                                segmentSource, display_string, flags);
                        
                        int supplement = 0;
                        
                        if (m_displaySegmentSources) {
                            supplement = ste.getSrcText().length() + "\n".length();
                        }
                        
                        if (doCheckSpelling && wordList != null) {
                            for (Token token : wordList) {
                                int tokenStart = token.getOffset();
                                int tokenEnd = tokenStart + token.getLength();
                                String word = token.getTextFromString(display_string);

                                try {
                                    xlDoc.replace(
                                            offset+supplement+tokenStart,
                                            token.getLength(),
                                            word,
                                            Styles.applyStyles(attributes,Styles.MISSPELLED)
                                            );
                                } catch (BadLocationException ble) {
                                    //Log.log(IMPOSSIBLE);
                            Log.log(ble);
                        }
                    }
                }
            }
                }
            }
            editor.cancelUndo();
        } // synchronize (editor)
    }

    public final int WITH_END_MARKERS = 1;
    public final int IS_NOT_TRANSLATED = 2;
    
    /**
     * replace the text in the editor and return the new length
     */
    public synchronized int replaceEntry(int offset, int length, 
            String source, String translation, int flags) {
        synchronized(editor) {
            AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();
            
            int result = 0;
            
            AttributeSet attr = 
                    ((flags & IS_NOT_TRANSLATED) == IS_NOT_TRANSLATED ?
                        m_unTranslatedAttributeSet : m_translatedAttributeSet);
            
            try {
                xlDoc.remove(offset, length);
                
                xlDoc.insertString(offset,"\n\n",Styles.PLAIN);
                result = 2;
                if ((flags & WITH_END_MARKERS) == WITH_END_MARKERS) {
                    String endStr = OConsts.segmentEndStringFull;
                    xlDoc.insertString(offset, endStr, Styles.PLAIN);
                    // make the text bold
                    xlDoc.replace(offset+endStr.indexOf(OConsts.segmentEndString),
                            OConsts.segmentEndString.length(),
                            OConsts.segmentEndString, Styles.BOLD);
                    result += endStr.length(); 
                }
                // modify the attributes only if absolutely necessary
                if (translation != null && !translation.equals("")) {
                    xlDoc.insertString(offset, translation, attr);
                    result += translation.length();
                }
                
                if ((flags & WITH_END_MARKERS) == WITH_END_MARKERS) {
                    // insert a plain space
                    xlDoc.insertString(offset," ",Styles.PLAIN);
                    String startStr = new String(OConsts.segmentStartString);
                    // <HP-experiment>
                    
                            try {
                        if (m_segmentTagHasNumber)
                        {
                            // put entry number in first tag
                            String num = String.valueOf(m_curEntryNum + 1);
                            int zero = startStr.lastIndexOf('0');
                            startStr = startStr.substring(0, zero-num.length()+1) + num + 
                                    startStr.substring(zero+1, startStr.length());
                        }
                    }
                    catch (Exception exception) {
                        Log.log("ERROR: exception while putting segment # in start tag:");
                        Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                        Log.log(exception);
                        // FIX: since these are localised, don't assume number appears, keep try/catch block
                    }
                    // </HP-experiment>
                    /*startStr = "<segment "+Integer.toString(m_curEntryNum + 1)+">";*/
                    xlDoc.insertString(offset, startStr, Styles.BOLD);
                    result += startStr.length();
                }
                if (source != null) {
                    if ((flags & WITH_END_MARKERS) != WITH_END_MARKERS) {
                        source += "\n";
                    }
                    xlDoc.insertString(offset, source, Styles.GREEN);
                    result += source.length();
                }
            } catch (BadLocationException ble) {
                Log.log(IMPOSSIBLE);
                Log.log(ble);
            }
            
            return result;
        }
    }
    /**
     * Activates the current entry by displaying source text and embedding
     * displayed text in markers.
     * <p>
     * Also moves document focus to current entry,
     * and makes sure fuzzy info displayed if available.
     */
    public synchronized void activateEntry()
    {
        if (!isProjectLoaded())
            return;
       int translatedInFile = 0;
        for (int _i = m_xlFirstEntry; _i <= m_xlLastEntry; _i++)
        {
            if (CommandThread.core.getSTE(_i).isTranslated())
                translatedInFile++;
        }
        
        progressLabel.setText(" " + Integer.toString(translatedInFile) + "/" + 
                Integer.toString(m_xlLastEntry - m_xlFirstEntry + 1) +
                " (" + Integer.toString(CommandThread.core.getNumberofTranslatedSegments()) + "/" +
                Integer.toString(CommandThread.core.getNumberOfUniqueSegments()) + ", " +
                Integer.toString(CommandThread.core.getNumberOfSegmentsTotal()) + ") ");
        
        setLengthLabel(" " + Integer.toString(m_curEntry.getSrcText().length()) + "/" +
            Integer.toString(m_curEntry.getTranslation().length()) + " ");
          
        synchronized (editor) {
            history.insertNew(m_curEntryNum);

            // update history menu items
            menu.gotoHistoryBackMenuItem.setEnabled(history.hasPrev());
            menu.gotoHistoryForwardMenuItem.setEnabled(history.hasNext());

            // recover data about current entry
            // <HP-experiment>
            if (m_curEntryNum < m_xlFirstEntry) {
                Log.log("ERROR: Current entry # lower than first entry #");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                // FIX: m_curEntryNum = m_xlFirstEntry;
            }
            if (m_curEntryNum > m_xlLastEntry) {
                Log.log("ERROR: Current entry # greater than last entry #");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                // FIX: m_curEntryNum = m_xlLastEntry;
            }
            // </HP-experiment>
            m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
            String srcText = m_curEntry.getSrcText();

            m_sourceDisplayLength = srcText.length();

            // sum up total character offset to current segment start
            m_segmentStartOffset = 0;
            int localCur = m_curEntryNum - m_xlFirstEntry;
            // <HP-experiment>
            DocumentSegment docSeg = null; // <HP-experiment> remove once done experimenting
            try {
                for (int i=0; i<localCur; i++)
                {
                    //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                    docSeg = m_docSegList[i];
                    m_segmentStartOffset += docSeg.length; // length includes \n
                }

                //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                docSeg = m_docSegList[localCur];
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while calculating character offset:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: for (int i=0; i<localCur && i < m_docSegList.length; i++)
            }
            // </HP-experiment>

            // -2 to move inside newlines at end of segment
            m_segmentEndInset = editor.getTextLength() - (m_segmentStartOffset + docSeg.length-2);

            String translation = m_curEntry.getTranslation();

            if( translation==null || translation.length()==0 )
            {
                translation=m_curEntry.getSrcText();

                // if "Leave translation empty" is set
                // then we don't insert a source text into target
                //
                // RFE "Option: not copy source text into target field"
                //      http://sourceforge.net/support/tracker.php?aid=1075972
                if( Preferences.isPreference(Preferences.DONT_INSERT_SOURCE_TEXT) )
                {
                    translation = new String();
                }

                // if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
                // RFE "Option: Insert best match (80%+) into target field"
                //      http://sourceforge.net/support/tracker.php?aid=1075976
                if( Preferences.isPreference(Preferences.BEST_MATCH_INSERT) )
                {
                    String percentage_s = Preferences.getPreferenceDefault(
                            Preferences.BEST_MATCH_MINIMAL_SIMILARITY, Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
                    // <HP-experiment>
                    int percentage = 0;
                    try {
                        //int
                        percentage = Integer.parseInt(percentage_s);
                    }
                    catch (Exception exception) {
                        Log.log("ERROR: exception while parsing percentage:");
                        Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                        Log.log(exception);
                        return; // deliberately breaking, to simulate previous behaviour
                        // FIX: unknown, but expect number parsing errors
                    }
                    // </HP-experiment>
                    List<NearString> near = m_curEntry.getStrEntry().getNearListTranslated();
                    if( near.size()>0 )
                    {
                        NearString thebest = near.get(0);
                        if( thebest.score >= percentage )
                        {
                            int old_tr_len = translation.length();
                            translation = Preferences.getPreferenceDefault(
                                    Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                    OStrings.getString("WF_DEFAULT_PREFIX")) +
                                    thebest.str.getTranslation();
                            }
                            }
                            }
                        }

            int replacedLength = replaceEntry(m_segmentStartOffset, 
                    docSeg.length, srcText, translation, WITH_END_MARKERS);

            // <HP-experiment>
            try {
                updateFuzzyInfo();
                updateGlossaryInfo();
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while updating match and glossary info:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>

            StringEntry curEntry = m_curEntry.getStrEntry();
            int nearLength = curEntry.getNearListTranslated().size();

            // <HP-experiment>
            try {
                if (nearLength > 0 && m_glossaryLength > 0)
                {
                    // display text indicating both categories exist
                    Object obj[] = {
                        new Integer(nearLength),
                                new Integer(m_glossaryLength) };
                                showStatusMessage(StaticUtils.format(
                                        OStrings.getString("TF_NUM_NEAR_AND_GLOSSARY"), obj));
                }
                else if (nearLength > 0)
                {
                    Object obj[] = { new Integer(nearLength) };
                    showStatusMessage(StaticUtils.format(
                            OStrings.getString("TF_NUM_NEAR"), obj));
                }
                else if (m_glossaryLength > 0)
                {
                    Object obj[] = { new Integer(m_glossaryLength) };
                    showStatusMessage(StaticUtils.format(
                            OStrings.getString("TF_NUM_GLOSSARY"), obj));
                }
                else
                    showStatusMessage(new String());                                       // NOI18N
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while setting message text:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>

            int offsetPrev = 0;
            int localNum = m_curEntryNum-m_xlFirstEntry;
            // <HP-experiment>
            try {
                for (int i=Math.max(0, localNum-3); i<localNum; i++)
                {
                    docSeg = m_docSegList[i];
                    offsetPrev += docSeg.length;
                }
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while calculating previous offset:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>
            final int lookPrev = m_segmentStartOffset - offsetPrev;

            int offsetNext = 0;
            int localLast = m_xlLastEntry-m_xlFirstEntry;
            // <HP-experiment>
            try {
                for (int i=localNum+1; i<(localNum+4) && i<=localLast; i++)
                {
                    docSeg = m_docSegList[i];
                    offsetNext += docSeg.length;
                }
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while calculating next offset:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>
            final int lookNext = m_segmentStartOffset + replacedLength + offsetNext;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        editor.setCaretPosition(lookNext);
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    editor.setCaretPosition(lookPrev);
                                    SwingUtilities.invokeLater(new Runnable()
                                    {
                                        public void run()
                                        {
                                            checkCaret();
                                        }
                                    });
                                }
                                catch(IllegalArgumentException iae)
                                {} // eating silently
                            }
                        });
                    }
                    catch(IllegalArgumentException iae)
                    {} // eating silently
                }
            });

            if (!m_docReady)
            {
                m_docReady = true;
            }
            editor.cancelUndo();
            
            editor.checkSpelling(true);
        } // synchronize (editor)

        entryActivated = true;
    }

    /**
     * Displays a warning message.
     *
     * @param msg the message to show
     * @param e exception occured. may be null
     */
    public void displayWarning(String msg, Throwable e)
    {
	showStatusMessage(msg);
        String fulltext = msg;
        if( e!=null )
            fulltext+= "\n" + e.toString();                                     // NOI18N
        JOptionPane.showMessageDialog(this, fulltext, OStrings.getString("TF_WARNING"),
                JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Displays an error message.
     *
     * @param msg the message to show
     * @param e exception occured. may be null
     */
    public void displayError(String msg, Throwable e)
    {
	showStatusMessage(msg);
        String fulltext = msg;
        if( e!=null )
            fulltext+= "\n" + e.toString();                                     // NOI18N
        JOptionPane.showMessageDialog(this, fulltext, OStrings.getString("TF_ERROR"),
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Make sure there's one character in the direction indicated for
     * delete operation.
     *
     * @param forward
     * @return true if space is available
     */
    public synchronized boolean checkCaretForDelete(boolean forward)
    {
        synchronized (editor) {
            int pos = editor.getCaretPosition();

            // make sure range doesn't overlap boundaries
            checkCaret();

            if (forward)
            {
                // make sure we're not at end of segment
                // -1 for space before tag, -2 for newlines
                int end = editor.getTextLength() - m_segmentEndInset -
                        OConsts.segmentEndStringFull.length();
                int spos = editor.getSelectionStart();
                int epos = editor.getSelectionEnd();
                if( pos>=end && spos>=end && epos>=end )
                    return false;
            }
            else
            {
                // make sure we're not at start of segment
                int start = getTranslationStart();
                int spos = editor.getSelectionStart();
                int epos = editor.getSelectionEnd();
                if( pos<=start && epos<=start && spos<=start )
                    return false;
            }
        } // synchronized (editor)

        return true;
    }

    /**
     * Calculate the position of the start of the current translation
     */
    public synchronized int getTranslationStart() {
        synchronized(editor) {
            return m_segmentStartOffset + m_sourceDisplayLength +
                   OConsts.segmentStartStringFull.length(); 
        }
    }
    
    /**
     * Calculcate the position of the end of the current translation
     */
    public synchronized int getTranslationEnd() {
        synchronized(editor) {
           return editor.getTextLength() - m_segmentEndInset -
                    OConsts.segmentEndStringFull.length();
        }
    }
    
    /**
     * Checks the spelling of the segment.
     * @param start : the starting position
     * @param text : the text to check
     */
    private synchronized List<Token> checkSpelling(int start, String text) {
        // we have the translation and it should be spellchecked
        List<Token> wordlist = StaticUtils.tokenizeText(text);
        List<Token> wrongWordList = new ArrayList<Token>();
        
        AbstractDocument xlDoc = (AbstractDocument)editor.getDocument();
        AttributeSet attributes = m_translatedAttributeSet;

        SpellChecker spellchecker = CommandThread.core.getSpellchecker();

        for (Token token : wordlist) {
            int tokenStart = token.getOffset();
            int tokenEnd = tokenStart + token.getLength();
            String word = text.substring(tokenStart, tokenEnd);

            if (!spellchecker.isCorrect(word)) {
                try {
                    xlDoc.replace(
                            start+tokenStart,
                            token.getLength(),
                            word,
                            Styles.applyStyles(attributes,Styles.MISSPELLED)
                            );
                } catch (BadLocationException ble) {
                    //Log.log(IMPOSSIBLE);
                    Log.log(ble);
                }
                wrongWordList.add(token);
            }
        }
        return wrongWordList;
    }
    
    /**
     * Checks whether the selection & caret is inside editable text,
     * and changes their positions accordingly if not.
     */
    public synchronized void checkCaret()
    {
        synchronized (editor) {
            //int pos = m_editor.getCaretPosition();
            int spos = editor.getSelectionStart();
            int epos = editor.getSelectionEnd();
            /*int start = m_segmentStartOffset + m_sourceDisplayLength +
                    OConsts.segmentStartStringFull.length();*/
            int start = getTranslationStart();
            // -1 for space before tag, -2 for newlines
            /*int end = editor.getTextLength() - m_segmentEndInset -
                    OConsts.segmentEndStringFull.length();*/
            int end = getTranslationEnd();
    
            if (spos != epos)
            {
                // dealing with a selection here - make sure it's w/in bounds
                if (spos < start)
                {
                    editor.setSelectionStart(start);
                }
                else if (spos > end)
                {
                    editor.setSelectionStart(end);
                }
                if (epos > end)
                {
                    editor.setSelectionEnd(end);
                }
                else if (epos < start)
                {
                    editor.setSelectionStart(start);
                }
            }
            else
            {
                // non selected text
                if (spos < start)
                {
                    editor.setCaretPosition(start);
                }
                else if (spos > end)
                {
                    editor.setCaretPosition(end);
                }
            }
        } // synchronized (editor)
    }

    public void fatalError(String msg, Throwable re)
    {
        Log.log(msg);
        if (re != null)
            Log.log(re);

        // try for 10 seconds to shutdown gracefully
        CommandThread.core.interrupt();
        for( int i=0; i<100 && CommandThread.core!=null; i++ )
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        Runtime.getRuntime().halt(1);
    }
    
    /** Tells whether the project is loaded. */
    public synchronized boolean isProjectLoaded()
    {
        return m_projectLoaded;
    }
    
    /** The font for main window (source and target text) and for match and glossary windows */
    Font m_font;
    
    /** first entry number in current file. */
    public int		m_xlFirstEntry;
    /** last entry number in current file. */
    public int		m_xlLastEntry;
    
    // starting offset and length of source lang in current segment
    public int		m_segmentStartOffset;
    public int		m_sourceDisplayLength;
    public int		m_segmentEndInset;
    // text length of glossary, if displayed
    private int		m_glossaryLength;
    
    // boolean set after safety check that org.omegat.OConsts.segmentStartStringFull
    //	contains empty "0000" for segment number
    private boolean	m_segmentTagHasNumber;
    
    // indicates the document is loaded and ready for processing
    public boolean	m_docReady;
    
    /** text segments in current document. */
    public DocumentSegment[] m_docSegList;
    
    public char	m_advancer;
    
    SourceTextEntry		m_curEntry;
    
   //private String  m_activeFile;
    
    private String getActiveFileFullPath() 
    {
        return CommandThread.core.getSTE(m_curEntryNum).getSrcFile().name;
    }
    
    public String getActiveFileName() 
    {
        String result = getActiveFileFullPath().substring(
                CommandThread.core.sourceRoot().length());
 //       Log.log("active file name="+result);
        return result;
    }    
    private String  m_activeProj;
    public int      m_curEntryNum;

    TagValidationFrame m_tagWin;
    ProjectFrame m_projWin;
    public ProjectFrame getProjectFrame()
    {
        return m_projWin;
    }
    
    /**
     * the attribute set used for translated segments
     */
    AttributeSet m_translatedAttributeSet;
    
    /**
     * the attribute set used for translated segments
     */
    AttributeSet m_unTranslatedAttributeSet;
    
    /**
     * return the attribute set of translated segments
     */
    public AttributeSet getTranslatedAttributeSet() {
        return m_translatedAttributeSet;
    }
    
    /**
     * display the segmetn sources or not
     */
    boolean m_displaySegmentSources;
    
    public boolean displaySegmentSources() {
        return m_displaySegmentSources;
    }
    
    Set<SearchWindow> m_searches; // set of all open search windows
    
    public boolean m_projectLoaded;
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        statusPanel2 = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        addComponentListener(this);
        
        statusPanel.setLayout(new java.awt.BorderLayout());

        statusLabel.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        statusPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, java.util.ResourceBundle.getBundle("org/omegat/Bundle").getString("MW_PROGRESS_DEFAULT"));
        progressLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/omegat/Bundle").getString("MW_PROGRESS_TOOLTIP"));
        progressLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        progressLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusPanel2.add(progressLabel);

        org.openide.awt.Mnemonics.setLocalizedText(lengthLabel, java.util.ResourceBundle.getBundle("org/omegat/Bundle").getString("MW_SEGMENT_LENGTH_DEFAULT"));
        lengthLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/omegat/Bundle").getString("MW_SEGMENT_LENGTH_TOOLTIP"));
        lengthLabel.setAlignmentX(1.0F);
        lengthLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lengthLabel.setFocusable(false);
        statusPanel2.add(lengthLabel);

        statusPanel.add(statusPanel2, java.awt.BorderLayout.EAST);

        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        setJMenuBar(menu.initComponents());

        pack();
    }

    public void componentHidden(java.awt.event.ComponentEvent evt) {
    }

    public void componentMoved(java.awt.event.ComponentEvent evt) {
        if (evt.getSource() == MainWindow.this) {
            MainWindow.this.formComponentMoved(evt);
        }
    }

    public void componentResized(java.awt.event.ComponentEvent evt) {
        if (evt.getSource() == MainWindow.this) {
            MainWindow.this.formComponentResized(evt);
        }
    }

    public void componentShown(java.awt.event.ComponentEvent evt) {
    }

    public void windowActivated(java.awt.event.WindowEvent evt) {
    }

    public void windowClosed(java.awt.event.WindowEvent evt) {
    }

    public void windowClosing(java.awt.event.WindowEvent evt) {
        if (evt.getSource() == MainWindow.this) {
            menu.mainWindowMenuHandler.projectExitMenuItemActionPerformed();
        }
    }

    public void windowDeactivated(java.awt.event.WindowEvent evt) {
    }

    public void windowDeiconified(java.awt.event.WindowEvent evt) {
    }

    public void windowIconified(java.awt.event.WindowEvent evt) {
    }

    public void windowOpened(java.awt.event.WindowEvent evt) {
    }


    public void formComponentMoved(java.awt.event.ComponentEvent evt)
    {
        saveScreenLayout();
    }
    
    public void formComponentResized(java.awt.event.ComponentEvent evt)
    {
        saveScreenLayout();
    }
    
    boolean m_autoSpellChecking;
    
    public boolean autoSpellCheckingOn() {
        return m_autoSpellChecking;
    }
    
    private javax.swing.JLabel lengthLabel;    
    private javax.swing.JLabel progressLabel;    
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel statusPanel2;    

    DockingDesktop desktop;

    private DockableScrollPane editorScroller;
    public EditorTextArea editor;
    
    private DockableScrollPane matchesScroller;
    MatchesTextArea matches;
    
    private DockableScrollPane glossaryScroller;
    GlossaryTextArea glossary;
    
    SegmentHistory history = SegmentHistory.getInstance();
}
