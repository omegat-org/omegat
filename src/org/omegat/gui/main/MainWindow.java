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
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.omegat.core.Core;
import org.omegat.core.data.CommandThread;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.StringEntry;
import org.omegat.core.matching.NearString;
import org.omegat.gui.editor.EditorTextArea;
import org.omegat.gui.filelist.ProjectFrame;
import org.omegat.gui.matches.MatchesTextArea;
import org.omegat.gui.search.SearchWindow;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.WikiGet;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.ResourcesUtil;

import com.vlsolutions.swing.docking.DockingDesktop;

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
public class MainWindow extends JFrame implements IMainWindow {
    public final MainWindowMenu menu;
    
    /** Creates new form MainWindow */
    public MainWindow()
    {
        m_searches = new HashSet<SearchWindow>();
        menu = new MainWindowMenu(this, new MainWindowMenuHandler(this));

        setJMenuBar(menu.initComponents());
        getContentPane().add(MainWindowUI.createStatusBar(this), BorderLayout.SOUTH);
        pack();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                menu.mainWindowMenuHandler.projectExitMenuItemActionPerformed();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                MainWindowUI.saveScreenLayout(MainWindow.this);
            }
            public void componentResized(ComponentEvent e) {
                MainWindowUI.saveScreenLayout(MainWindow.this);
            }
        });

        MainWindowUI.createMainComponents(this);

        getContentPane().add(MainWindowUI.initDocking(this), BorderLayout.CENTER);

        additionalUIInit();
        oldInit();
        
        MainWindowUI.loadInstantStart(editorScroller, editor);
    }
    
    /**
     * {@inheritDoc}
     */
    public JFrame getApplicationFrame() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public Font getApplicationFont() {
        return m_font;
    }

    /**
     * Some additional actions to initialize UI,
     * not doable via NetBeans Form Editor
     */
    private void additionalUIInit()
    {
        updateTitle();
        
        setIconImage(ResourcesUtil.getIcon("/org/omegat/gui/resources/OmegaT_small.gif").getImage());

        m_projWin = new ProjectFrame(this);
        m_projWin.setFont(m_font);

        statusLabel.setText(new String()+' ');
        
        MainWindowUI.loadScreenLayout(this);
        uiUpdateOnProjectClose();
    }

    /**
     * Sets the title of the main window appropriately
     */
    public void updateTitle()
    {
        String s = OStrings.getDisplayVersion();
        if(isProjectLoaded())
        {
            s += " :: " + m_activeProj;                                         // NOI18N
            try
            {
                //String file = m_activeFile.substring(CommandThread.core.sourceRoot().length());
                String file = Core.getEditor().getCurrentFile();
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
            MainWindowUI.loadInstantStart(editorScroller, editor);
        }
        setTitle(s);
    }
    
    /**
     * Old Initialization.
     */
    public void oldInit()
    {
        m_activeProj = new String();
        //m_activeFile = new String();
        
        ////////////////////////////////
        
        enableEvents(0);
    }
    
    boolean layoutInitialized = false;
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // command handling

    
   
    /** insert current fuzzy match at cursor position */

    public synchronized void doInsertTrans()
    {
        if (!isProjectLoaded())
            return;
        
        NearString near = Core.getMatcher().getActiveMatch();
        if (near != null) {
            Core.getEditor().insertText(near.str.getTranslation());
        }
    }

    /** replace entire edit area with active fuzzy match */
    public synchronized void doRecycleTrans()
    {
        if (!isProjectLoaded())
            return;
        
        NearString near = Core.getMatcher().getActiveMatch();
        if (near != null) {
            Core.getEditor().replaceEditText(near.str.getTranslation());
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
        showProgressMessage(OStrings.getString("MW_PROGRESS_DEFAULT"));
        showLengthMessage(OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
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
        Core.getEditor().clearHistory();
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
        Core.getEditor().clearHistory();
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
    
    public synchronized void finishLoadProject()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public synchronized void run()
            {
                m_activeProj = CommandThread.core.getProjectProperties().getProjectName();
                //m_activeFile = new String();
                Core.getEditor().setFirstEntry();
                
                Core.getEditor().loadDocument();
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
    
    /**
     * Show message in progress bar.
     * 
     * @param messageText
     *                message text
     */
    public void showProgressMessage(String messageText) {
        progressLabel.setText(messageText);
    }

    /**
     * Show message in length label.
     * 
     * @param messageText
     *                message text
     */
    public void showLengthMessage(String messageText) {
        lengthLabel.setText(messageText);
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // display oriented code
    
    /**
     * Displays fuzzy matching info if it's available.
     */
    public void updateFuzzyInfo()
    {
        if (!isProjectLoaded())
            return;
        
        StringEntry curEntry = Core.getEditor().getCurrentEntry().getStrEntry();
        Core.getMatcher().showMatches(curEntry);
    }
    
    /**
     * Displays glossary terms for the current segment.
     */
    public void updateGlossaryInfo()
    {
        StringEntry curEntry = Core.getEditor().getCurrentEntry().getStrEntry();
        glossary.setGlossaryEntries(curEntry.getGlossaryEntries());
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
    
    private String  m_activeProj;

    ProjectFrame m_projWin;
    public ProjectFrame getProjectFrame()
    {
        return m_projWin;
    }
    
    Set<SearchWindow> m_searches; // set of all open search windows
    
    public boolean m_projectLoaded;

    public boolean m_autoSpellChecking;
    
    public boolean autoSpellCheckingOn() {
        return m_autoSpellChecking;
    }
    
    JLabel lengthLabel;    
    JLabel progressLabel;    
    JLabel statusLabel;

    DockingDesktop desktop;

    DockableScrollPane editorScroller;
    public EditorTextArea editor;
    
    DockableScrollPane matchesScroller;
    public MatchesTextArea matches;
    
    DockableScrollPane glossaryScroller;
    GlossaryTextArea glossary;
}
