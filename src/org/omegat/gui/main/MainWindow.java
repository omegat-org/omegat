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

package org.omegat.gui.main;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.StringEntry;
import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.TagValidationFrame;
import org.omegat.gui.HelpFrame;
import org.omegat.gui.OmegaTFileChooser;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.ProjectProperties;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.WorkflowOptionsDialog;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.StaticUtils;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RequestPacket;

/**
 * The main window of OmegaT application.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 */
public class MainWindow extends JFrame implements java.awt.event.ActionListener, java.awt.event.WindowListener, java.awt.event.ComponentListener
{
    /** Creates new form MainWindow */
    public MainWindow()
    {
        initComponents();
        additionalUIInit();
        oldInit();
        loadInstantStart();
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
        matchWindow = new MatchGlossaryWindow(this);
        
        xlPane = new MainPane();
        mainScroller.setViewportView(xlPane);
        xlPane.setMainWindow(this);
        
        initScreenLayout();
        updateCheckboxesOnStart();
        uiUpdateOnProjectClose();
        initUIShortcuts();
        
        try
        {
            // MacOSX-specific
            net.roydesign.mac.MRJAdapter.addQuitApplicationListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doQuit();
                }
            });
            net.roydesign.mac.MRJAdapter.addAboutListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doAbout();
                }
            });
        }
        catch(NoClassDefFoundError e)
        {
            e.printStackTrace(StaticUtils.getLogStream());
        }
        
        // all except MacOSX
		if( !System.getProperty("os.name").toLowerCase().startsWith("mac os x") )   // NOI18N
        {
            projectMenu.add(separator2inProjectMenu);
            projectMenu.add(projectExitMenuItem);
        }
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
            e.printStackTrace(StaticUtils.getLogStream());
        }
    }
    
    /**
     * Sets the shortcut keys.
     * Need to do it here (manually), because on MacOSX the shortcut key is CMD,
     * and on other OSes it's Ctrl.
     */
    private void initUIShortcuts()
    {
        setAccelerator(projectNewMenuItem, KeyEvent.VK_N);
        setAccelerator(projectOpenMenuItem, KeyEvent.VK_O);
        setAccelerator(projectSaveMenuItem, KeyEvent.VK_S);
        setAccelerator(projectEditMenuItem, KeyEvent.VK_E);
        setAccelerator(projectExitMenuItem, KeyEvent.VK_Q);
        
        setAccelerator(editUndoMenuItem , KeyEvent.VK_Z);
        setAccelerator(editRedoMenuItem , KeyEvent.VK_Y);
        setAccelerator(editOverwriteTranslationMenuItem , KeyEvent.VK_R);
        setAccelerator(editInsertTranslationMenuItem , KeyEvent.VK_I);
        setAccelerator(editOverwriteSourceMenuItem , KeyEvent.VK_R, true);
        setAccelerator(editInsertSourceMenuItem , KeyEvent.VK_I, true);
        setAccelerator(editFindInProjectMenuItem , KeyEvent.VK_F);
        setAccelerator(editSelectFuzzy1MenuItem , KeyEvent.VK_1);
        setAccelerator(editSelectFuzzy2MenuItem , KeyEvent.VK_2);
        setAccelerator(editSelectFuzzy3MenuItem , KeyEvent.VK_3);
        setAccelerator(editSelectFuzzy4MenuItem , KeyEvent.VK_4);
        setAccelerator(editSelectFuzzy5MenuItem , KeyEvent.VK_5);
        
        setAccelerator(gotoNextUntranslatedMenuItem , KeyEvent.VK_U);
        setAccelerator(gotoNextSegmentMenuItem , KeyEvent.VK_N);
        setAccelerator(gotoPreviousSegmentMenuItem , KeyEvent.VK_P);
        
        setAccelerator(viewMatchWindowCheckBoxMenuItem , KeyEvent.VK_M);
        setAccelerator(viewFileListCheckBoxMenuItem , KeyEvent.VK_L);
        
        setAccelerator(toolsValidateTagsMenuItem , KeyEvent.VK_T);
    }
    
    /**
     * Utility method to set Ctrl + key accelerators for menu items.
     * @param key integer specifiyng the key code (e.g. KeyEvent.VK_Z)
     */
    private void setAccelerator(JMenuItem item, int key)
    {
        setAccelerator(item, key, false);
    }
    
    /**
     * Utility method to set Ctrl + key accelerators for menu items.
     * @param key integer specifiyng the key code (e.g. KeyEvent.VK_Z)
     */
    private void setAccelerator(JMenuItem item, int key, boolean shift)
    {
        int shiftmask = shift ? Event.SHIFT_MASK : 0;
        item.setAccelerator(KeyStroke.getKeyStroke(key,
                shiftmask | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    
    
    /**
     * Sets the title of the main window appropriately
     */
    private void updateTitle()
    {
        String s = OStrings.OMEGAT_VERSION;
        if( m_projectLoaded )
        {
            try
            {
                String file = m_activeFile.substring(
                        CommandThread.core.sourceRoot().length());
                s += " :: " + m_activeProj + " :: " + file;							// NOI18N
            }
            catch( Exception e )
            {
                // nothing...
            }
        }
        setTitle(s);
    }
    
    /**
     * Old Initialization.
     */
    public void oldInit()
    {
        m_curEntryNum = -1;
        m_curNear = null;
        m_activeProj = "";														// NOI18N
        m_activeFile = "";														// NOI18N
        m_docSegList = new ArrayList();
        
        ////////////////////////////////
        
        enableEvents(0);
        
        String fontName = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
        String fontSize = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE, OConsts.TF_FONT_SIZE_DEFAULT);
        int fontSizeInt = 12;
        try
        {
            fontSizeInt = Integer.parseInt(fontSize);
        }
        catch (NumberFormatException nfe)
        {
        }
        m_font = new Font(fontName, Font.PLAIN, fontSizeInt);
        xlPane.setFont(m_font);
        
        matchWindow.getMatchGlossaryPane().setFont(m_font);
        
        // check this only once as it can be changed only at compile time
        // should be OK, but customization might have messed it up
        String start = OStrings.TF_CUR_SEGMENT_START;
        int zero = start.lastIndexOf('0');
        m_segmentTagHasNumber = (zero > 4) && // 4 to reserve room for 10000 digit
                (start.charAt(zero - 1) == '0') &&
                (start.charAt(zero - 2) == '0') &&
                (start.charAt(zero - 3) == '0');
    }
    
    /** Updates menu checkboxes from preferences on start */
    private void updateCheckboxesOnStart()
    {
        if (Preferences.isPreference(Preferences.USE_TAB_TO_ADVANCE))
        {
            optionsTabAdvanceCheckBoxMenuItem.setSelected(true);
            m_advancer = KeyEvent.VK_TAB;
        }
        else
            m_advancer = KeyEvent.VK_ENTER;
        
        optionsAlwaysConfirmQuitCheckBoxMenuItem.setSelected(
                Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT));
    }
    
    /**
     * Initialized the sizes of OmegaT window.
     * <p>
     * Assume screen size is 800x600 if width less than 900, and
     * 1024x768 if larger. Assume task bar at bottom of screen.
     * If screen size saved, recover that and use instead
     * (18may04).
     */
    private void initScreenLayout()
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
        
        // match/glossary window
        try
        {
            String dw = Preferences.getPreference(Preferences.MATCHWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.MATCHWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            matchWindow.setSize(w, h);
            String dx = Preferences.getPreference(Preferences.MATCHWINDOW_X);
            String dy = Preferences.getPreference(Preferences.MATCHWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            matchWindow.setLocation(x, y);
        }
        catch (NumberFormatException nfe)
        {
            // size info missing - put window in default position
            GraphicsEnvironment env =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle scrSize = env.getMaximumWindowBounds();
            if (scrSize.width < 900)
            {
                // assume 800x600
                matchWindow.setSize(200, 536);
                matchWindow.setLocation(590, 0);
            }
            else
            {
                // assume 1024x768 or larger
                matchWindow.setSize(300, 700);
                matchWindow.setLocation(700, 0);
            }
        }
        
        // match/glossary window divider
        try
        {
            String divs = Preferences.getPreference(Preferences.MATCHWINDOW_DIVIDER);
            int div = Integer.parseInt(divs);
            matchWindow.setDividerLocation(div);
        }
        catch (NumberFormatException nfe)
        {
            // divider info missing - put in default position - middle
            int div = matchWindow.getHeight() / 2;
            matchWindow.setDividerLocation(div);
        }
        
        screenLayoutLoaded = true;
    }
    
    /** Loads Instant start article */
    private void loadInstantStart()
    {
        try
        {
            String lang = HelpFrame.detectDocLanguage();
            String filepath =
                    StaticUtils.installDir()
                    + File.separator + OConsts.HELP_DIR + File.separator
                    + lang + File.separator
                    + OConsts.HELP_INSTANT_START;
            JTextPane instantArticlePane = new JTextPane();
            instantArticlePane.setEditable(false);
            instantArticlePane.setPage("file:///"+filepath);                    // NOI18N
            mainScroller.setViewportView(instantArticlePane);
        }
        catch (IOException e)
        {
            mainScroller.setViewportView(xlPane);
        }
    }
    
    /**
     * Stores screen layout (width, height, position, etc).
     */
    public void storeScreenLayout()
    {
        if( screenLayoutLoaded )
        {
            Preferences.setPreference(Preferences.MAINWINDOW_WIDTH, getWidth());
            Preferences.setPreference(Preferences.MAINWINDOW_HEIGHT, getHeight());
            Preferences.setPreference(Preferences.MAINWINDOW_X, getX());
            Preferences.setPreference(Preferences.MAINWINDOW_Y, getY());
            
            Preferences.setPreference(Preferences.MATCHWINDOW_DIVIDER, matchWindow.getDividerLocation());
            
            Preferences.setPreference(Preferences.MATCHWINDOW_WIDTH, matchWindow.getWidth());
            Preferences.setPreference(Preferences.MATCHWINDOW_HEIGHT, matchWindow.getHeight());
            Preferences.setPreference(Preferences.MATCHWINDOW_X, matchWindow.getX());
            Preferences.setPreference(Preferences.MATCHWINDOW_Y, matchWindow.getY());
        }
    }
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // command handling
    
    /** Shows About dialog */
    private void doAbout()
    {
        new AboutDialog(this).setVisible(true);        
    }
    
    /** Shows About dialog */
    private void doQuit()
    {
        storeScreenLayout();
        Preferences.save();
        
        if (m_projectLoaded)
        {
            commitEntry();
        }
        
        boolean projectModified = false;
        if (m_projectLoaded)
        {
            projectModified = CommandThread.core.isProjectModified();
            doSave();
        }
        // RFE 1302358
        // Add Yes/No Warning before OmegaT quits
        if (projectModified ||
                Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT))
        {
            if( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this,
                    OStrings.getString("MW_QUIT_CONFIRM"),
                    OStrings.getString("CONFIRM_DIALOG_TITLE"),
                    JOptionPane.YES_NO_OPTION) )
            {
                if(m_projectLoaded)
                    activateEntry();
                return;
            }
        }
        
        // shut down
        if( CommandThread.core!=null )
            CommandThread.core.interrupt();
        
        // waiting for CommandThread to finish for 1 minute
        for( int i=0; i<600 && CommandThread.core!=null; i++ )
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        
        System.exit(0);
    }
    
    private void doValidateTags()
    {
        ArrayList suspects = CommandThread.core.validateTags();
        if (suspects.size() > 0)
        {
            // create list of suspect strings - use org.omegat.gui.TagValidationFrame for now
            TagValidationFrame cf = new TagValidationFrame(this);
            cf.setVisible(true);
            cf.displayStringList(suspects);
        }
        else
        {
            // show dialog saying all is OK
            JOptionPane.showMessageDialog(this,
                    OStrings.TF_NOTICE_OK_TAGS,
                    OStrings.TF_NOTICE_TITLE_TAGS,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void doNextEntry()
    {
        if (!m_projectLoaded)
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
    
    public void doPrevEntry()
    {
        if (!m_projectLoaded)
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
     *
     * @author Henry Pjiffers
     * @author Maxym Mykhalchuk
     */
    public void doNextUntranslatedEntry()
    {
        // check if a document is loaded
        if (m_projectLoaded == false)
            return;
        
        // save the current entry
        commitEntry();
        
        // get the current entry number and the total number of entries
        int curEntryNum = m_curEntryNum;
        int numEntries = CommandThread.core.numEntries();
        
        // iterate through the list of entries,
        // starting at the current entry,
        // until an entry with no translation is found
        //
        // P.S. going to the next entry anyway, even if it's not translated
        curEntryNum++;
        
        SourceTextEntry entry = null;
        while (curEntryNum < numEntries)
        {
            // get the next entry
            entry = CommandThread.core.getSTE(curEntryNum);
            
            // check if the entry is not null, and whether it contains a translation
            if (   (entry != null)
            && (entry.getTranslation().length() == 0))
            {
                // mark the entry
                m_curEntryNum = curEntryNum;
                
                // load the document, if the segment is not in the current document
                if (m_curEntryNum > m_xlLastEntry)
                    loadDocument();
                
                // stop searching
                break;
            }
            
            // next entry
            curEntryNum++;
        }
        
        // activate the entry
        activateEntry();
    }
    
    
    /** inserts the source text of a segment at cursor position */
    private void doInsertSource()
    {
        if (!m_projectLoaded)
            return;
        
        String s = m_curEntry.getSrcText();
        int pos = xlPane.getCaretPosition();
        xlPane.select(pos, pos);
        xlPane.replaceSelection(s);
    }
    
    /** replaces entire edited segment text with a the source text of a segment at cursor position */
    private void doOverwriteSource()
    {
        if (!m_projectLoaded)
            return;
        
        String s = m_curEntry.getSrcText();
        doReplaceEditText(s);
    }
    
    /** insert current fuzzy match at cursor position */
    private void doInsertTrans()
    {
        if (!m_projectLoaded)
            return;
        
        if (m_curNear == null)
            return;
        
        StringEntry se = m_curNear.str;
        String s = se.getTranslation();
        int pos = xlPane.getCaretPosition();
        xlPane.select(pos, pos);
        xlPane.replaceSelection(s);
    }
    
    
    /** replace entire edit area with active fuzzy match */
    public void doRecycleTrans()
    {
        if (!m_projectLoaded)
            return;
        
        if (m_curNear == null)
            return;
        
        StringEntry se = m_curNear.str;
        doReplaceEditText(se.getTranslation());
    }
    
    /** replaces the entire edit area with a given text */
    private void doReplaceEditText(String text)
    {
        // build local offsets
        int start = m_segmentStartOffset + m_sourceDisplayLength +
                OStrings.TF_CUR_SEGMENT_START.length();
        int end = xlPane.getTextLength() - m_segmentEndInset -
                OStrings.TF_CUR_SEGMENT_END.length();
        
        // remove text
        xlPane.select(start, end);
        xlPane.replaceSelection(text);
    }
    
    /** Closes the project. */
    public void doCloseProject()
    {
        Preferences.save();
        
        if (m_projectLoaded)
        {
            commitEntry();
            doSave();
        }
        m_projWin.reset();
        m_projectLoaded = false;
        xlPane.setText(OStrings.TF_INTRO_MESSAGE);                              // NOI18N
        
        matchWindow.getMatchGlossaryPane().reset();
        
        updateTitle();
        uiUpdateOnProjectClose();
        
        CommandThread.core.signalProjectClosing();
        CommandThread.core.cleanUp();
    }
    
    /** Updates UI (enables/disables menu items) upon <b>closing</b> project */
    private void uiUpdateOnProjectClose()
    {
        projectNewMenuItem.setEnabled(true);
        projectOpenMenuItem.setEnabled(true);
        projectNewMenuItem.setEnabled(true);
        
        projectImportMenuItem.setEnabled(false);
        projectReloadMenuItem.setEnabled(false);
        projectCloseMenuItem.setEnabled(false);
        projectSaveMenuItem.setEnabled(false);
        projectEditMenuItem.setEnabled(false);
        projectCompileMenuItem.setEnabled(false);
        
        editMenu.setEnabled(false);
        gotoMenu.setEnabled(false);
        toolsValidateTagsMenuItem.setEnabled(false);
        
        xlPane.setEditable(false);
        m_projWin.uiUpdateImportButtonStatus();
        
        m_projWin.setVisible(false);
        viewFileListCheckBoxMenuItem.setSelected(false);
    }
    
    /** Updates UI (enables/disables menu items) upon <b>opening</b> project */
    private void uiUpdateOnProjectOpen()
    {
        projectNewMenuItem.setEnabled(false);
        projectOpenMenuItem.setEnabled(false);
        projectNewMenuItem.setEnabled(false);
        
        projectImportMenuItem.setEnabled(true);
        projectReloadMenuItem.setEnabled(true);
        projectCloseMenuItem.setEnabled(true);
        projectSaveMenuItem.setEnabled(true);
        projectEditMenuItem.setEnabled(true);
        projectCompileMenuItem.setEnabled(true);
        
        editMenu.setEnabled(true);
        gotoMenu.setEnabled(true);
        toolsValidateTagsMenuItem.setEnabled(true);
        
        xlPane.setEditable(true);
        m_projWin.uiUpdateImportButtonStatus();
        
        m_projWin.setVisible(true);
        viewFileListCheckBoxMenuItem.setSelected(true);
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
    public void projectLoaded()
    {
        Thread runlater = new Thread()
        {
            public void run()
            {
                commitEntry();
                activateEntry();
            }
        };
        SwingUtilities.invokeLater(runlater);
    }
    
    /** Edits project's properties */
    private void doEditProject()
    {
        ProjectProperties config = CommandThread.core.getProjectProperties();
        boolean changed = false;
        try
        {
            changed = config.editProject();
        }
        catch( IOException ioe )
        {
            displayWarning( OStrings.getString("MW_ERROR_PROJECT_NOT_EDITABLE"), ioe);
        }
        
        if( changed )
        {
            int res = JOptionPane.showConfirmDialog(this,
                    OStrings.getString("MW_REOPEN_QUESTION"),
                    OStrings.getString("MW_REOPEN_TITLE"),
                    JOptionPane.YES_NO_OPTION);
            if( res==JOptionPane.YES_OPTION )
                doReloadProject();
        }
    }
    
    /**
     * Displays the font dialog to allow selecting
     * the font for source, target text (in main window)
     * and for match and glossary windows.
     */
    private void doFont()
    {
        FontSelectionDialog dlg = new FontSelectionDialog(this, m_font);
        dlg.setVisible(true);
        if( dlg.getReturnStatus()==FontSelectionDialog.RET_OK_CHANGED )
        {
            // fonts have changed
            // first commit current translation
            commitEntry();
            m_font = dlg.getSelectedFont();
            xlPane.setFont(m_font);
            
            matchWindow.getMatchGlossaryPane().setFont(m_font);
            
            Preferences.setPreference(OConsts.TF_SRC_FONT_NAME, m_font.getName());
            Preferences.setPreference(OConsts.TF_SRC_FONT_SIZE, m_font.getSize());
            activateEntry();
        }
    }
    
    /**
     * Displays the filters setup dialog to allow
     * customizing file filters in detail.
     */
    private void setupFilters()
    {
        FiltersCustomizer dlg = new FiltersCustomizer(this);
        dlg.setVisible(true);
        if( dlg.getReturnStatus()==FiltersCustomizer.RET_OK )
        {
            // saving config
            FilterMaster.getInstance().saveConfig();
            
            if(isProjectLoaded())
            {
                // asking to reload a project
                int res = JOptionPane.showConfirmDialog(this,
                        OStrings.getString("MW_REOPEN_QUESTION"),
                        OStrings.getString("MW_REOPEN_TITLE"),
                        JOptionPane.YES_NO_OPTION);
                if( res==JOptionPane.YES_OPTION )
                    doReloadProject();
            }
        }
        else
        {
            // reloading config from disk
            FilterMaster.getInstance().loadConfig();
        }
    }
    
    /**
     * Displays the segmentation setup dialog to allow
     * customizing the segmentation rules in detail.
     */
    private void setupSegmentation()
    {
        SegmentationCustomizer segment_window = new SegmentationCustomizer(this);
        segment_window.setVisible(true);
        
        if( segment_window.getReturnStatus()==SegmentationCustomizer.RET_OK 
                && isProjectLoaded())
        {
            // asking to reload a project
            int res = JOptionPane.showConfirmDialog(this,
                    OStrings.getString("MW_REOPEN_QUESTION"),
                    OStrings.getString("MW_REOPEN_TITLE"),
                    JOptionPane.YES_NO_OPTION);
            if( res==JOptionPane.YES_OPTION )
                doReloadProject();
        }
    }
    
    /**
     * Displays the workflow setup dialog to allow
     * customizing the diverse workflow options.
     */
    private void setupWorkflow()
    {
        new WorkflowOptionsDialog(this).setVisible(true);
    }
    
    private void doSave()
    {
        if (!m_projectLoaded)
            return;
        
        setMessageText( OStrings.getString("MW_STATUS_SAVING"));
        
        CommandThread.core.save();
        
        setMessageText( OStrings.getString("MW_STATUS_SAVED"));
    }
    
    /**
     * Creates a new Project.
     */
    private void doCreateProject()
    {
        CommandThread.core.createProject();
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
    private void doLoadProject()
    {
        if (m_projectLoaded)
        {
            displayError( "Please close the project first!", new Exception( "Another project is open")); // NOI18N
            return;
        }
        
        matchWindow.getMatchGlossaryPane().reset();
        mainScroller.setViewportView(xlPane);
        
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
        if (m_projectLoaded)
        {
            displayError( "Please close the project first!", new Exception( "Another project is open")); // NOI18N
            return;
        }
        
        matchWindow.getMatchGlossaryPane().reset();
        mainScroller.setViewportView(xlPane);
        
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
                        ArrayList files = new ArrayList();
                        StaticUtils.buildFileList(files, selSrc, true);
                        String selSourceParent = selSrc.getParent();
                        for(int j=0; j<files.size(); j++)
                        {
                            String filename = (String)files.get(j);
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
    
    public void doGotoEntry(int entryNum)
    {
        if (!m_projectLoaded)
            return;
        
        commitEntry();
        
        m_curEntryNum = entryNum - 1;
        if (m_curEntryNum < m_xlFirstEntry)
        {
            if (m_curEntryNum < 0)
                m_curEntryNum = CommandThread.core.numEntries();
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
    
    public void doGotoEntry(String str)
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
    
    public void finishLoadProject()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                m_activeProj = CommandThread.core.getProjectProperties().getProjectName();
                m_activeFile = "";														// NOI18N
                m_curEntryNum = 0;
                loadDocument();
                m_projectLoaded = true;
                
                uiUpdateOnProjectOpen();
            }
        });
    }
    
    private void doCompileProject()
    {
        if (!m_projectLoaded)
            return;
        try
        {
            CommandThread.core.compileProject();
        }
        catch(IOException e)
        {
            displayError(OStrings.TF_COMPILE_ERROR, e);
        }
        catch(TranslationException te)
        {
            displayError(OStrings.TF_COMPILE_ERROR, te);
        }
    }
    
    private void doFind()
    {
        String selection = xlPane.getSelectedText();
        if (selection != null)
        {
            selection.trim();
        }
        
        SearchThread srch = new SearchThread(this, selection);
        srch.start();
    }
    
    /* updates status label */
    public void setMessageText(String str)
    {
        if( str.equals("") )													// NOI18N
            str = " ";															// NOI18N
        statusLabel.setText(str);
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
    private void loadDocument()
    {
        m_docReady = false;
        
        // clear old text
        xlPane.setText("");													// NOI18N
        m_docSegList.clear();
        
        m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
        
        m_xlFirstEntry = m_curEntry.getFirstInFile();
        m_xlLastEntry = m_curEntry.getLastInFile();
        
        DocumentSegment docSeg;
        StringBuffer textBuf = new StringBuffer();
        
        for (int entryNum=m_xlFirstEntry; entryNum<=m_xlLastEntry; entryNum++)
        {
            docSeg = new DocumentSegment();
            
            SourceTextEntry ste = CommandThread.core.getSTE(entryNum);
            String text = ste.getTranslation();
            // set text and font
            if( text.length()==0 )
            {
                // no translation available - use source text
                text = ste.getSrcText();
            }
            text += "\n\n";														// NOI18N
            
            textBuf.append(text);
            
            docSeg.length = text.length();
            m_docSegList.add(docSeg);
        }
        xlPane.setText(textBuf.toString());
    }
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // display oriented code
    
    /**
     * Displays fuzzy matching info if it's available.
     */
    public void updateFuzzyInfo(int nearNum)
    {
        if (!m_projectLoaded)
            return;
        
        StringEntry curEntry = m_curEntry.getStrEntry();
        List nearList = curEntry.getNearListTranslated();
        // see if there are any matches
        if( nearList.size()<=0 )
        {
            m_curNear = null;
            matchWindow.getMatchGlossaryPane().updateMatchText();
            return;
        }
        
        if( nearNum>=nearList.size() )
            return;
        
        m_curNear = (NearString) nearList.get(nearNum);
        
        NearString ns;
        int ctr = 0;
        int offset;
        int start = -1;
        int end = -1;
        ListIterator li = nearList.listIterator();
        
        while( li.hasNext() )
        {
            ns = (NearString) li.next();
            
            offset = matchWindow.getMatchGlossaryPane().addMatchTerm(
                    ns.str.getSrcText(), ns.str.getTranslation(), ns.score, ns.proj);
            
            if( ctr==nearNum )
            {
                start = offset;
            }
            else if( ctr==nearNum + 1 )
            {
                end = offset;
            }
            
            ctr++;
        }
        
        MatchGlossaryPane matchpane = matchWindow.getMatchGlossaryPane();
        matchpane.hiliteRange(start, end);
        matchpane.updateMatchText();
        matchpane.formatNearText(m_curNear.str.getSrcTokenList(), m_curNear.attr);
    }
    
    /**
     * Displays glossary terms for the current segment.
     */
    private void updateGlossaryInfo()
    {
        // add glossary terms and fuzzy match info to match window
        StringEntry curEntry = m_curEntry.getStrEntry();
        if (curEntry.getGlossaryEntries().size() > 0)
        {
            // TODO do something with glossary terms
            m_glossaryLength = curEntry.getGlossaryEntries().size();
            ListIterator li = curEntry.getGlossaryEntries().listIterator();
            while (li.hasNext())
            {
                GlossaryEntry glos = (GlossaryEntry) li.next();
                matchWindow.getMatchGlossaryPane().addGlosTerm(glos.getSrcText(), glos.getLocText(),
                        glos.getCommentText());
            }
            
        }
        else
        {
            m_glossaryLength = 0;
        }
        
        matchWindow.getMatchGlossaryPane().updateGlossaryText();
    }
    
    /** Is any segment edited currently? */
    private boolean entryActivated = false;
    
    /** Plain text. */
    private final static AttributeSet PLAIN;
    /** Bold text. */
    private final static MutableAttributeSet BOLD;
    /** Bold text on green background. */
    private final static MutableAttributeSet GREEN;
    static
    {
        PLAIN = SimpleAttributeSet.EMPTY;
        BOLD = new SimpleAttributeSet();
        StyleConstants.setBold(BOLD, true);
        GREEN = new SimpleAttributeSet();
        StyleConstants.setBold(GREEN, true);
        StyleConstants.setBackground(GREEN, new Color(192, 255, 192));
    }
    
    /**
     * Commits the translation.
     * Reads current entry text and commit it to memory if it's changed.
     * Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     *            if appropriate option is set in Workflow options dialog.
     */
    private void commitEntry()
    {
        if (!m_projectLoaded)
            return;
        
        if (!entryActivated)
            return;
        entryActivated = false;
        
        int start = m_segmentStartOffset + m_sourceDisplayLength +
                OStrings.TF_CUR_SEGMENT_START.length();
        int end = xlPane.getTextLength() - m_segmentEndInset -
                OStrings.TF_CUR_SEGMENT_END.length();
        String display_string;
        String new_translation;
        if (start == end)
        {
            new_translation =  "";                                              // NOI18N
            display_string = m_curEntry.getSrcText();
        }
        else
        {
            try
            {
                new_translation = xlPane.getText(start, end - start);
            }
            catch(BadLocationException ble)
            {
                StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
                StaticUtils.log(ble.getMessage());
                ble.printStackTrace();
                ble.printStackTrace(StaticUtils.getLogStream());
                new_translation = "";                                           // NOI18N
            }
            display_string = new_translation;
        }
        
        int totalLen = m_sourceDisplayLength + OStrings.TF_CUR_SEGMENT_START.length() +
                new_translation.length() + OStrings.TF_CUR_SEGMENT_END.length();
        try
        {
            xlPane.getDocument().remove(m_segmentStartOffset, totalLen);
            xlPane.getDocument().insertString(m_segmentStartOffset, display_string, PLAIN);
        }
        catch(BadLocationException ble)
        {
            StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
            StaticUtils.log(ble.getMessage());
            ble.printStackTrace();
            ble.printStackTrace(StaticUtils.getLogStream());
        }
        
        String old_translation = m_curEntry.getTranslation();
        // update memory
        if (new_translation.equals(m_curEntry.getSrcText()))
        {
            if  (Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC))
                m_curEntry.setTranslation(new_translation);
            else
                m_curEntry.setTranslation("");                                  // NOI18N
        }
        else
            m_curEntry.setTranslation(new_translation);
        
        DocumentSegment docSeg = (DocumentSegment)
        m_docSegList.get(m_curEntryNum - m_xlFirstEntry);
        docSeg.length = display_string.length() + "\n\n".length();							// NOI18N
        
        // update the length parameters of all changed segments
        // update strings in display
        if (!m_curEntry.getTranslation().equals(old_translation))
        {
            // find all identical strings and redraw them
            
            // build offsets of all strings
            int[] offsets = new int[m_xlLastEntry-m_xlFirstEntry];
            int currentOffset = 0;
            for (int i=0; i<(m_xlLastEntry-m_xlFirstEntry); i++)
            {
                offsets[i]=currentOffset;
                docSeg = (DocumentSegment) m_docSegList.get(i);
                currentOffset += docSeg.length;
            }

            // starting from the last (guaranteed by sorting ParentList)
            Iterator it = m_curEntry.getStrEntry().getParentList().iterator();
            while (it.hasNext())
            {
                SourceTextEntry ste = (SourceTextEntry) it.next();
                int entry = ste.entryNum();
                if (entry>m_xlLastEntry)
                    continue;
                else if (entry<m_xlFirstEntry)
                    break;
                
                int localEntry = entry-m_xlFirstEntry;
                int offset = offsets[localEntry];
                
                // replace old text w/ new
                docSeg = (DocumentSegment) m_docSegList.get(localEntry);
                try
                {
                    xlPane.getDocument().remove(offset, docSeg.length);
                    xlPane.getDocument().insertString(offset, 
                            display_string + "\n\n", PLAIN);                    // NOI18N
                }
                catch(BadLocationException ble)
                {
                    StaticUtils.log("Should not have happened, report to ???!");     // NOI18N
                    StaticUtils.log(ble.getMessage());
                    ble.printStackTrace();
                    ble.printStackTrace(StaticUtils.getLogStream());
                }
                docSeg.length = display_string.length() + "\n\n".length();      // NOI18N
            }
        }
        xlPane.cancelUndo();
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
        if (!m_projectLoaded)
            return;
        
        entryActivated = true;
        
        int i;
        DocumentSegment docSeg;
        
        // recover data about current entry
        m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
        String srcText = m_curEntry.getSrcText();
        
        m_sourceDisplayLength = srcText.length();
        
        // sum up total character offset to current segment start
        m_segmentStartOffset = 0;
        for (i=m_xlFirstEntry; i<m_curEntryNum; i++)
        {
            docSeg = (DocumentSegment) m_docSegList.get(i-m_xlFirstEntry);
            m_segmentStartOffset += docSeg.length; // length includes \n
        }
        
        docSeg = (DocumentSegment) m_docSegList.get(m_curEntryNum - m_xlFirstEntry);
        // -2 to move inside newlines at end of segment
        m_segmentEndInset = xlPane.getTextLength() - (m_segmentStartOffset + docSeg.length-2);
        
        // get label tags
        String startStr = OStrings.TF_CUR_SEGMENT_START;
        String endStr = OStrings.TF_CUR_SEGMENT_END;
        if (m_segmentTagHasNumber)
        {
            // put entry number in first tag
            String num = String.valueOf(m_curEntryNum + 1);
            int zero = startStr.lastIndexOf('0');
            startStr = startStr.substring(0, zero-num.length()+1) + num + 
                    startStr.substring(zero+1, startStr.length()-1);
        }
        
        String translation = m_curEntry.getTranslation();
        
        // append to end of segment first
        try
        {
            xlPane.getDocument().insertString(m_segmentStartOffset + 
                    docSeg.length - 2, endStr, BOLD);
        }
        catch(BadLocationException ble)
        {
            StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
            StaticUtils.log(ble.getMessage());
            ble.printStackTrace();
            ble.printStackTrace(StaticUtils.getLogStream());
        }
        
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
                try
                {
                    xlPane.getDocument().remove(m_segmentStartOffset, translation.length());
                }
                catch(BadLocationException ble)
                {
                    StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
                    StaticUtils.log(ble.getMessage());
                    ble.printStackTrace();
                    ble.printStackTrace(StaticUtils.getLogStream());
                }
                translation = "";                                               // NOI18N
            }
            
            // if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
            // RFE "Option: Insert best match (80%+) into target field"
            //      http://sourceforge.net/support/tracker.php?aid=1075976
            if( Preferences.isPreference(Preferences.BEST_MATCH_INSERT) )
            {
                String percentage_s = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_MINIMAL_SIMILARITY, Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
                int percentage = Integer.parseInt(percentage_s);
                List near = m_curEntry.getStrEntry().getNearListTranslated();
                if( near.size()>0 )
                {
                    NearString thebest = (NearString)near.get(0);
                    if( thebest.score >= percentage )
                    {
                        if(translation.length()>0)
                        try
                        {
                            xlPane.getDocument().remove(m_segmentStartOffset, translation.length());
                        }
                        catch(BadLocationException ble)
                        {
                            StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
                            StaticUtils.log(ble.getMessage());
                            ble.printStackTrace();
                            ble.printStackTrace(StaticUtils.getLogStream());
                        }
                        
                        translation = Preferences.getPreferenceDefault(
                                Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                OStrings.getString("WF_DEFAULT_PREFIX")) +
                                thebest.str.getTranslation();
                        try
                        {
                            xlPane.getDocument().insertString(
                                    m_segmentStartOffset, translation, PLAIN);
                        }
                        catch(BadLocationException ble)
                        {
                            StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
                            StaticUtils.log(ble.getMessage());
                            ble.printStackTrace();
                            ble.printStackTrace(StaticUtils.getLogStream());
                        }
                    }
                }
            }
        }
        
        try
        {
            xlPane.getDocument().insertString(m_segmentStartOffset, " ", PLAIN); // NOI18N
            xlPane.getDocument().insertString(m_segmentStartOffset, startStr, BOLD);
            xlPane.getDocument().insertString(m_segmentStartOffset, srcText, GREEN);
        }
        catch(BadLocationException ble)
        {
            StaticUtils.log("Should not have happened, report to ???!");        // NOI18N
            StaticUtils.log(ble.getMessage());
            ble.printStackTrace();
            ble.printStackTrace(StaticUtils.getLogStream());
        }
        
        if (m_curEntry.getSrcFile().name.compareTo(m_activeFile) != 0)
        {
            m_activeFile = m_curEntry.getSrcFile().name;
            updateTitle();
        }
        
        updateFuzzyInfo(0);
        updateGlossaryInfo();
        
        StringEntry curEntry = m_curEntry.getStrEntry();
        int nearLength = curEntry.getNearListTranslated().size();
        
        if (nearLength > 0 && m_glossaryLength > 0)
        {
            // display text indicating both categories exist
            Object obj[] = {
                new Integer(nearLength),
                        new Integer(m_glossaryLength) };
                        setMessageText(MessageFormat.format(
                                OStrings.TF_NUM_NEAR_AND_GLOSSARY, obj));
        }
        else if (nearLength > 0)
        {
            Object obj[] = { new Integer(nearLength) };
            setMessageText(MessageFormat.format(
                    OStrings.TF_NUM_NEAR, obj));
        }
        else if (m_glossaryLength > 0)
        {
            Object obj[] = { new Integer(m_glossaryLength) };
            setMessageText(MessageFormat.format(
                    OStrings.TF_NUM_GLOSSARY, obj));
        }
        else
            setMessageText("");													// NOI18N

        try
        {
            if( m_segmentStartOffset < 100)
                xlPane.setCaretPosition(0);
            
            xlPane.setCaretPosition(m_segmentStartOffset + 
                    srcText.length() + startStr.length() + 1 );
        }
        catch(IllegalArgumentException iae)
        {
            // it's OK
        }
        
        if (!m_docReady)
        {
            m_docReady = true;
        }
        xlPane.cancelUndo();
    }
    
    /**
     * Displays a warning message.
     *
     * @param msg the message to show
     * @param e exception occured. may be null
     */
    public void displayWarning(String msg, Throwable e)
    {
        setMessageText(msg);
        String fulltext = msg;
        if( e!=null )
            fulltext+= "\n" + e.toString();                                     // NOI18N
        JOptionPane.showMessageDialog(this, fulltext, OStrings.TF_WARNING,
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
        setMessageText(msg);
        String fulltext = msg;
        if( e!=null )
            fulltext+= "\n" + e.toString();                                     // NOI18N
        JOptionPane.showMessageDialog(this, fulltext, OStrings.TF_ERROR,
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Make sure there's one character in the direction indicated for
     * delete operation.
     *
     * @param forward
     * @return true if space is available
     */
    public boolean checkCaretForDelete(boolean forward)
    {
        int pos = xlPane.getCaretPosition();
        
        // make sure range doesn't overlap boundaries
        checkCaret();
        
        if (forward)
        {
            // make sure we're not at end of segment
            // -1 for space before tag, -2 for newlines
            int end = xlPane.getTextLength() - m_segmentEndInset -
                    OStrings.TF_CUR_SEGMENT_END.length();
            int spos = xlPane.getSelectionStart();
            int epos = xlPane.getSelectionEnd();
            if( pos>=end && spos>=end && epos>=end )
                return false;
        }
        else
        {
            // make sure we're not at start of segment
            int start = m_segmentStartOffset + m_sourceDisplayLength +
                    OStrings.TF_CUR_SEGMENT_START.length();
            int spos = xlPane.getSelectionStart();
            int epos = xlPane.getSelectionEnd();
            if( pos<=start && epos<=start && spos<=start )
                return false;
        }
        return true;
    }
    
    /**
     * Checks whether the selection & caret is inside editable text,
     * and changes their positions accordingly if not.
     */
    public void checkCaret()
    {
        //int pos = m_xlPane.getCaretPosition();
        int spos = xlPane.getSelectionStart();
        int epos = xlPane.getSelectionEnd();
        int start = m_segmentStartOffset + m_sourceDisplayLength +
                OStrings.TF_CUR_SEGMENT_START.length();
        // -1 for space before tag, -2 for newlines
        int end = xlPane.getTextLength() - m_segmentEndInset -
                OStrings.TF_CUR_SEGMENT_END.length();
        
        if (spos != epos)
        {
            // dealing with a selection here - make sure it's w/in bounds
            if (spos < start)
            {
                xlPane.setSelectionStart(start);
            }
            else if (spos > end)
            {
                xlPane.setSelectionStart(end);
            }
            if (epos > end)
            {
                xlPane.setSelectionEnd(end);
            }
            else if (epos < start)
            {
                xlPane.setSelectionStart(start);
            }
        }
        else
        {
            // non selected text
            if (spos < start)
            {
                xlPane.setCaretPosition(start);
            }
            else if (spos > end)
            {
                xlPane.setCaretPosition(end);
            }
        }
    }
    
    public void fatalError(String msg, Throwable re)
    {
        StaticUtils.log(msg);
        if (re != null)
        {
            re.printStackTrace(StaticUtils.getLogStream());
            re.printStackTrace();
        }
        
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
    
    /**
     * Overrides parent method to show Match/Glossary viewer
     * simultaneously with the main frame.
     */
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        matchWindow.setVisible(b);
        matchWindow.getMatchGlossaryPane().setFont(m_font);
        toFront();
    }
    
    /** Tells whether the project is loaded. */
    public boolean isProjectLoaded()
    { 
        return m_projectLoaded;
    }
    
    /** The font for main window (source and target text) and for match and glossary windows */
    private Font m_font;
    
    // first and last entry numbers in current file
    public int		m_xlFirstEntry;
    public int		m_xlLastEntry;
    
    // starting offset and length of source lang in current segment
    public int		m_segmentStartOffset;
    public int		m_sourceDisplayLength;
    public int		m_segmentEndInset;
    // text length of glossary, if displayed
    private int		m_glossaryLength;
    
    // boolean set after safety check that org.omegat.OStrings.TF_CUR_SEGMENT_START
    //	contains empty "0000" for segment number
    private boolean	m_segmentTagHasNumber;
    
    // indicates the document is loaded and ready for processing
    public boolean	m_docReady;
    
    // list of text segments in current doc
    public ArrayList	m_docSegList;
    
    public char	m_advancer;
    
    private SourceTextEntry		m_curEntry;
    
    private String	m_activeFile;
    private String	m_activeProj;
    public int m_curEntryNum;
    private NearString m_curNear;
    
    private ProjectFrame	m_projWin;
    public ProjectFrame getProjectFrame()
    {
        return m_projWin;
    }
    
    public boolean m_projectLoaded;
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        separator2inProjectMenu = new javax.swing.JSeparator();
        projectExitMenuItem = new javax.swing.JMenuItem();
        statusLabel = new javax.swing.JLabel();
        mainScroller = new javax.swing.JScrollPane();
        mainMenu = new javax.swing.JMenuBar();
        projectMenu = new javax.swing.JMenu();
        projectNewMenuItem = new javax.swing.JMenuItem();
        projectOpenMenuItem = new javax.swing.JMenuItem();
        projectImportMenuItem = new javax.swing.JMenuItem();
        projectReloadMenuItem = new javax.swing.JMenuItem();
        projectCloseMenuItem = new javax.swing.JMenuItem();
        separator4inProjectMenu = new javax.swing.JSeparator();
        projectSaveMenuItem = new javax.swing.JMenuItem();
        separator5inProjectMenu = new javax.swing.JSeparator();
        projectCompileMenuItem = new javax.swing.JMenuItem();
        separator1inProjectMenu = new javax.swing.JSeparator();
        projectEditMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editUndoMenuItem = new javax.swing.JMenuItem();
        editRedoMenuItem = new javax.swing.JMenuItem();
        separator1inEditMenu = new javax.swing.JSeparator();
        editOverwriteTranslationMenuItem = new javax.swing.JMenuItem();
        editInsertTranslationMenuItem = new javax.swing.JMenuItem();
        separator4inEditMenu = new javax.swing.JSeparator();
        editOverwriteSourceMenuItem = new javax.swing.JMenuItem();
        editInsertSourceMenuItem = new javax.swing.JMenuItem();
        separator2inEditMenu = new javax.swing.JSeparator();
        editFindInProjectMenuItem = new javax.swing.JMenuItem();
        separator3inEditMenu = new javax.swing.JSeparator();
        editSelectFuzzy1MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy2MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy3MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy4MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy5MenuItem = new javax.swing.JMenuItem();
        gotoMenu = new javax.swing.JMenu();
        gotoNextUntranslatedMenuItem = new javax.swing.JMenuItem();
        gotoNextSegmentMenuItem = new javax.swing.JMenuItem();
        gotoPreviousSegmentMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewMatchWindowCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewFileListCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        toolsMenu = new javax.swing.JMenu();
        toolsValidateTagsMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        optionsTabAdvanceCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        optionsAlwaysConfirmQuitCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        separator1inOptionsMenu = new javax.swing.JSeparator();
        optionsFontSelectionMenuItem = new javax.swing.JMenuItem();
        optionsSetupFileFiltersMenuItem = new javax.swing.JMenuItem();
        optionsSentsegMenuItem = new javax.swing.JMenuItem();
        optionsWorkflowMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        helpAboutMenuItem = new javax.swing.JMenuItem();

        org.openide.awt.Mnemonics.setLocalizedText(projectExitMenuItem, OStrings.getString("TF_MENU_FILE_QUIT"));
        projectExitMenuItem.addActionListener(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addComponentListener(this);
        addWindowListener(this);

        getContentPane().add(statusLabel, java.awt.BorderLayout.SOUTH);

        mainScroller.setBorder(null);
        mainScroller.setMinimumSize(new java.awt.Dimension(100, 100));
        getContentPane().add(mainScroller, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(projectMenu, OStrings.getString("TF_MENU_FILE"));
        org.openide.awt.Mnemonics.setLocalizedText(projectNewMenuItem, OStrings.getString("TF_MENU_FILE_CREATE"));
        projectNewMenuItem.addActionListener(this);

        projectMenu.add(projectNewMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectOpenMenuItem, OStrings.getString("TF_MENU_FILE_OPEN"));
        projectOpenMenuItem.addActionListener(this);

        projectMenu.add(projectOpenMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectImportMenuItem, OStrings.getString("TF_MENU_FILE_IMPORT"));
        projectImportMenuItem.addActionListener(this);

        projectMenu.add(projectImportMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectReloadMenuItem, OStrings.getString("TF_MENU_PROJECT_RELOAD"));
        projectReloadMenuItem.addActionListener(this);

        projectMenu.add(projectReloadMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectCloseMenuItem, OStrings.getString("TF_MENU_FILE_CLOSE"));
        projectCloseMenuItem.addActionListener(this);

        projectMenu.add(projectCloseMenuItem);

        projectMenu.add(separator4inProjectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(projectSaveMenuItem, OStrings.getString("TF_MENU_FILE_SAVE"));
        projectSaveMenuItem.addActionListener(this);

        projectMenu.add(projectSaveMenuItem);

        projectMenu.add(separator5inProjectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(projectCompileMenuItem, OStrings.getString("TF_MENU_FILE_COMPILE"));
        projectCompileMenuItem.addActionListener(this);

        projectMenu.add(projectCompileMenuItem);

        projectMenu.add(separator1inProjectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(projectEditMenuItem, OStrings.getString("MW_PROJECTMENU_EDIT"));
        projectEditMenuItem.addActionListener(this);

        projectMenu.add(projectEditMenuItem);

        mainMenu.add(projectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editMenu, OStrings.getString("TF_MENU_EDIT"));
        org.openide.awt.Mnemonics.setLocalizedText(editUndoMenuItem, OStrings.getString("TF_MENU_EDIT_UNDO"));
        editUndoMenuItem.addActionListener(this);

        editMenu.add(editUndoMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editRedoMenuItem, OStrings.getString("TF_MENU_EDIT_REDO"));
        editRedoMenuItem.addActionListener(this);

        editMenu.add(editRedoMenuItem);

        editMenu.add(separator1inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editOverwriteTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_RECYCLE"));
        editOverwriteTranslationMenuItem.addActionListener(this);

        editMenu.add(editOverwriteTranslationMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editInsertTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_INSERT"));
        editInsertTranslationMenuItem.addActionListener(this);

        editMenu.add(editInsertTranslationMenuItem);

        editMenu.add(separator4inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editOverwriteSourceMenuItem, OStrings.getString("TF_MENU_EDIT_SOURCE_OVERWRITE"));
        editOverwriteSourceMenuItem.addActionListener(this);

        editMenu.add(editOverwriteSourceMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editInsertSourceMenuItem, OStrings.getString("TF_MENU_EDIT_SOURCE_INSERT"));
        editInsertSourceMenuItem.addActionListener(this);

        editMenu.add(editInsertSourceMenuItem);

        editMenu.add(separator2inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editFindInProjectMenuItem, OStrings.getString("TF_MENU_EDIT_FIND"));
        editFindInProjectMenuItem.addActionListener(this);

        editMenu.add(editFindInProjectMenuItem);

        editMenu.add(separator3inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy1MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_1"));
        editSelectFuzzy1MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy1MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy2MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_2"));
        editSelectFuzzy2MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy2MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy3MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_3"));
        editSelectFuzzy3MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy3MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy4MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_4"));
        editSelectFuzzy4MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy4MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy5MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_5"));
        editSelectFuzzy5MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy5MenuItem);

        mainMenu.add(editMenu);

        org.openide.awt.Mnemonics.setLocalizedText(gotoMenu, OStrings.getString("MW_GOTOMENU"));
        org.openide.awt.Mnemonics.setLocalizedText(gotoNextUntranslatedMenuItem, OStrings.getString("TF_MENU_EDIT_UNTRANS"));
        gotoNextUntranslatedMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextUntranslatedMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(gotoNextSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_NEXT"));
        gotoNextSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextSegmentMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(gotoPreviousSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_PREV"));
        gotoPreviousSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoPreviousSegmentMenuItem);

        mainMenu.add(gotoMenu);

        org.openide.awt.Mnemonics.setLocalizedText(viewMenu, OStrings.getString("MW_VIEWMENU"));
        viewMatchWindowCheckBoxMenuItem.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(viewMatchWindowCheckBoxMenuItem, OStrings.getString("TF_MENU_FILE_MATCHWIN"));
        viewMatchWindowCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewMatchWindowCheckBoxMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(viewFileListCheckBoxMenuItem, OStrings.getString("TF_MENU_FILE_PROJWIN"));
        viewFileListCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewFileListCheckBoxMenuItem);

        mainMenu.add(viewMenu);

        org.openide.awt.Mnemonics.setLocalizedText(toolsMenu, OStrings.getString("TF_MENU_TOOLS"));
        org.openide.awt.Mnemonics.setLocalizedText(toolsValidateTagsMenuItem, OStrings.getString("TF_MENU_TOOLS_VALIDATE"));
        toolsValidateTagsMenuItem.addActionListener(this);

        toolsMenu.add(toolsValidateTagsMenuItem);

        mainMenu.add(toolsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(optionsMenu, OStrings.getString("MW_OPTIONSMENU"));
        org.openide.awt.Mnemonics.setLocalizedText(optionsTabAdvanceCheckBoxMenuItem, OStrings.getString("TF_MENU_DISPLAY_ADVANCE"));
        optionsTabAdvanceCheckBoxMenuItem.addActionListener(this);

        optionsMenu.add(optionsTabAdvanceCheckBoxMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsAlwaysConfirmQuitCheckBoxMenuItem, OStrings.getString("MW_OPTIONSMENU_ALWAYS_CONFIRM_QUIT"));
        optionsAlwaysConfirmQuitCheckBoxMenuItem.addActionListener(this);

        optionsMenu.add(optionsAlwaysConfirmQuitCheckBoxMenuItem);

        optionsMenu.add(separator1inOptionsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(optionsFontSelectionMenuItem, OStrings.getString("TF_MENU_DISPLAY_FONT"));
        optionsFontSelectionMenuItem.addActionListener(this);

        optionsMenu.add(optionsFontSelectionMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsSetupFileFiltersMenuItem, OStrings.getString("TF_MENU_DISPLAY_FILTERS"));
        optionsSetupFileFiltersMenuItem.addActionListener(this);

        optionsMenu.add(optionsSetupFileFiltersMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsSentsegMenuItem, OStrings.getString("MW_OPTIONSMENU_SENTSEG"));
        optionsSentsegMenuItem.addActionListener(this);

        optionsMenu.add(optionsSentsegMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsWorkflowMenuItem, OStrings.getString("MW_OPTIONSMENU_WORKFLOW"));
        optionsWorkflowMenuItem.addActionListener(this);

        optionsMenu.add(optionsWorkflowMenuItem);

        mainMenu.add(optionsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(helpMenu, OStrings.getString("TF_MENU_HELP"));
        helpContentsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        org.openide.awt.Mnemonics.setLocalizedText(helpContentsMenuItem, OStrings.getString("TF_MENU_HELP_CONTENTS"));
        helpContentsMenuItem.addActionListener(this);

        helpMenu.add(helpContentsMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(helpAboutMenuItem, OStrings.getString("TF_MENU_HELP_ABOUT"));
        helpAboutMenuItem.addActionListener(this);

        helpMenu.add(helpAboutMenuItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt)
    {
        if (evt.getSource() == projectExitMenuItem)
        {
            MainWindow.this.projectExitMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectNewMenuItem)
        {
            MainWindow.this.projectNewMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectOpenMenuItem)
        {
            MainWindow.this.projectOpenMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectImportMenuItem)
        {
            MainWindow.this.projectImportMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectReloadMenuItem)
        {
            MainWindow.this.projectReloadMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCloseMenuItem)
        {
            MainWindow.this.projectCloseMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectSaveMenuItem)
        {
            MainWindow.this.projectSaveMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCompileMenuItem)
        {
            MainWindow.this.projectCompileMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectEditMenuItem)
        {
            MainWindow.this.projectEditMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editUndoMenuItem)
        {
            MainWindow.this.editUndoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editRedoMenuItem)
        {
            MainWindow.this.editRedoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editOverwriteTranslationMenuItem)
        {
            MainWindow.this.editOverwriteTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editInsertTranslationMenuItem)
        {
            MainWindow.this.editInsertTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editOverwriteSourceMenuItem)
        {
            MainWindow.this.editOverwriteSourceMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editInsertSourceMenuItem)
        {
            MainWindow.this.editInsertSourceMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editFindInProjectMenuItem)
        {
            MainWindow.this.editFindInProjectMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy1MenuItem)
        {
            MainWindow.this.editSelectFuzzy1MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy2MenuItem)
        {
            MainWindow.this.editSelectFuzzy2MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy3MenuItem)
        {
            MainWindow.this.editSelectFuzzy3MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy4MenuItem)
        {
            MainWindow.this.editSelectFuzzy4MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy5MenuItem)
        {
            MainWindow.this.editSelectFuzzy5MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextUntranslatedMenuItem)
        {
            MainWindow.this.gotoNextUntranslatedMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextSegmentMenuItem)
        {
            MainWindow.this.gotoNextSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoPreviousSegmentMenuItem)
        {
            MainWindow.this.gotoPreviousSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == viewMatchWindowCheckBoxMenuItem)
        {
            MainWindow.this.viewMatchWindowCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == viewFileListCheckBoxMenuItem)
        {
            MainWindow.this.viewFileListCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == toolsValidateTagsMenuItem)
        {
            MainWindow.this.toolsValidateTagsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsTabAdvanceCheckBoxMenuItem)
        {
            MainWindow.this.optionsTabAdvanceCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsAlwaysConfirmQuitCheckBoxMenuItem)
        {
            MainWindow.this.optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsFontSelectionMenuItem)
        {
            MainWindow.this.optionsFontSelectionMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsSetupFileFiltersMenuItem)
        {
            MainWindow.this.optionsSetupFileFiltersMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsSentsegMenuItem)
        {
            MainWindow.this.optionsSentsegMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsWorkflowMenuItem)
        {
            MainWindow.this.optionsWorkflowMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpContentsMenuItem)
        {
            MainWindow.this.helpContentsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpAboutMenuItem)
        {
            MainWindow.this.helpAboutMenuItemActionPerformed(evt);
        }
    }

    public void componentHidden(java.awt.event.ComponentEvent evt)
    {
    }

    public void componentMoved(java.awt.event.ComponentEvent evt)
    {
        if (evt.getSource() == MainWindow.this)
        {
            MainWindow.this.formComponentMoved(evt);
        }
    }

    public void componentResized(java.awt.event.ComponentEvent evt)
    {
        if (evt.getSource() == MainWindow.this)
        {
            MainWindow.this.formComponentResized(evt);
        }
    }

    public void componentShown(java.awt.event.ComponentEvent evt)
    {
    }

    public void windowActivated(java.awt.event.WindowEvent evt)
    {
    }

    public void windowClosed(java.awt.event.WindowEvent evt)
    {
    }

    public void windowClosing(java.awt.event.WindowEvent evt)
    {
        if (evt.getSource() == MainWindow.this)
        {
            MainWindow.this.formWindowClosing(evt);
        }
    }

    public void windowDeactivated(java.awt.event.WindowEvent evt)
    {
    }

    public void windowDeiconified(java.awt.event.WindowEvent evt)
    {
    }

    public void windowIconified(java.awt.event.WindowEvent evt)
    {
    }

    public void windowOpened(java.awt.event.WindowEvent evt)
    {
    }
    // </editor-fold>//GEN-END:initComponents

    private void optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed
        Preferences.setPreference(Preferences.ALWAYS_CONFIRM_QUIT,
                optionsAlwaysConfirmQuitCheckBoxMenuItem.isSelected());
    }//GEN-LAST:event_optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed

    private void editOverwriteSourceMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editOverwriteSourceMenuItemActionPerformed
    {//GEN-HEADEREND:event_editOverwriteSourceMenuItemActionPerformed
        doOverwriteSource();
    }//GEN-LAST:event_editOverwriteSourceMenuItemActionPerformed

    private void editInsertSourceMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editInsertSourceMenuItemActionPerformed
    {//GEN-HEADEREND:event_editInsertSourceMenuItemActionPerformed
        doInsertSource();
    }//GEN-LAST:event_editInsertSourceMenuItemActionPerformed
    
    private void projectImportMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectImportMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectImportMenuItemActionPerformed
        doImportSourceFiles();
    }//GEN-LAST:event_projectImportMenuItemActionPerformed
    
    private void projectReloadMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectReloadMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectReloadMenuItemActionPerformed
        doReloadProject();
    }//GEN-LAST:event_projectReloadMenuItemActionPerformed
    
    private void formComponentMoved(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentMoved
    {//GEN-HEADEREND:event_formComponentMoved
        storeScreenLayout();
    }//GEN-LAST:event_formComponentMoved
    
    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
        storeScreenLayout();
    }//GEN-LAST:event_formComponentResized
    
    private void optionsWorkflowMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsWorkflowMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsWorkflowMenuItemActionPerformed
        setupWorkflow();
    }//GEN-LAST:event_optionsWorkflowMenuItemActionPerformed
    
    private void optionsSentsegMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsSentsegMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsSentsegMenuItemActionPerformed
        setupSegmentation();
    }//GEN-LAST:event_optionsSentsegMenuItemActionPerformed
    
    private void projectEditMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectEditMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectEditMenuItemActionPerformed
        doEditProject();
    }//GEN-LAST:event_projectEditMenuItemActionPerformed
    
    private void optionsTabAdvanceCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed
        Preferences.setPreference(Preferences.USE_TAB_TO_ADVANCE,
                optionsTabAdvanceCheckBoxMenuItem.isSelected());
        if( optionsTabAdvanceCheckBoxMenuItem.isSelected() )
            m_advancer = KeyEvent.VK_TAB;
        else
            m_advancer = KeyEvent.VK_ENTER;
    }//GEN-LAST:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed
    
    private void helpContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpContentsMenuItemActionPerformed
    {//GEN-HEADEREND:event_helpContentsMenuItemActionPerformed
        HelpFrame hf = HelpFrame.getInstance();
        hf.setVisible(true);
        hf.toFront();
    }//GEN-LAST:event_helpContentsMenuItemActionPerformed
    
    private void optionsSetupFileFiltersMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsSetupFileFiltersMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsSetupFileFiltersMenuItemActionPerformed
        setupFilters();
    }//GEN-LAST:event_optionsSetupFileFiltersMenuItemActionPerformed
    
    private void optionsFontSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsFontSelectionMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsFontSelectionMenuItemActionPerformed
        doFont();
    }//GEN-LAST:event_optionsFontSelectionMenuItemActionPerformed
    
    private void toolsValidateTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toolsValidateTagsMenuItemActionPerformed
    {//GEN-HEADEREND:event_toolsValidateTagsMenuItemActionPerformed
        doValidateTags();
    }//GEN-LAST:event_toolsValidateTagsMenuItemActionPerformed
    
    private void editSelectFuzzy5MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy5MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy5MenuItemActionPerformed
        updateFuzzyInfo(4);
    }//GEN-LAST:event_editSelectFuzzy5MenuItemActionPerformed
    
    private void editSelectFuzzy4MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy4MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy4MenuItemActionPerformed
        updateFuzzyInfo(3);
    }//GEN-LAST:event_editSelectFuzzy4MenuItemActionPerformed
    
    private void editSelectFuzzy3MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy3MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy3MenuItemActionPerformed
        updateFuzzyInfo(2);
    }//GEN-LAST:event_editSelectFuzzy3MenuItemActionPerformed
    
    private void editSelectFuzzy2MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy2MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy2MenuItemActionPerformed
        updateFuzzyInfo(1);
    }//GEN-LAST:event_editSelectFuzzy2MenuItemActionPerformed
    
    private void editSelectFuzzy1MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy1MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy1MenuItemActionPerformed
        updateFuzzyInfo(0);
    }//GEN-LAST:event_editSelectFuzzy1MenuItemActionPerformed
    
    private void editFindInProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editFindInProjectMenuItemActionPerformed
    {//GEN-HEADEREND:event_editFindInProjectMenuItemActionPerformed
        doFind();
    }//GEN-LAST:event_editFindInProjectMenuItemActionPerformed
    
    private void editInsertTranslationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editInsertTranslationMenuItemActionPerformed
    {//GEN-HEADEREND:event_editInsertTranslationMenuItemActionPerformed
        doInsertTrans();
    }//GEN-LAST:event_editInsertTranslationMenuItemActionPerformed
    
    private void editOverwriteTranslationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editOverwriteTranslationMenuItemActionPerformed
    {//GEN-HEADEREND:event_editOverwriteTranslationMenuItemActionPerformed
        doRecycleTrans();
    }//GEN-LAST:event_editOverwriteTranslationMenuItemActionPerformed
    
    private void gotoNextUntranslatedMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoNextUntranslatedMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoNextUntranslatedMenuItemActionPerformed
        doNextUntranslatedEntry();
    }//GEN-LAST:event_gotoNextUntranslatedMenuItemActionPerformed
    
    private void gotoPreviousSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoPreviousSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoPreviousSegmentMenuItemActionPerformed
        doPrevEntry();
    }//GEN-LAST:event_gotoPreviousSegmentMenuItemActionPerformed
    
    private void gotoNextSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoNextSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoNextSegmentMenuItemActionPerformed
        doNextEntry();
    }//GEN-LAST:event_gotoNextSegmentMenuItemActionPerformed
    
    private void editRedoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editRedoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editRedoMenuItemActionPerformed
        try
        {
            xlPane.redoOneEdit();
        }
        catch (CannotRedoException cue)
        { }
    }//GEN-LAST:event_editRedoMenuItemActionPerformed
    
    private void editUndoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editUndoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editUndoMenuItemActionPerformed
        try
        {
            xlPane.undoOneEdit();
        }
        catch( CannotUndoException cue )
        { }
    }//GEN-LAST:event_editUndoMenuItemActionPerformed

    /** Informs Main Window class that the user closed the Match/Glossary window */
    public void filelistWindowClosed()
    {
        viewFileListCheckBoxMenuItem.setSelected(false);
    }
    
    private void viewFileListCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewFileListCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_viewFileListCheckBoxMenuItemActionPerformed
        if( m_projWin==null )
        {
            viewFileListCheckBoxMenuItem.setSelected(false);
            return;
        }
        
        if( viewFileListCheckBoxMenuItem.isSelected() )
        {
            m_projWin.buildDisplay();
            m_projWin.setVisible(true);
            m_projWin.toFront();
        }
        else
        {
            m_projWin.setVisible(false);
        }
    }//GEN-LAST:event_viewFileListCheckBoxMenuItemActionPerformed
    
    /** Informs Main Window class that the user closed the Match/Glossary window */
    public void matchWindowClosed()
    {
        viewMatchWindowCheckBoxMenuItem.setSelected(false);
    }
    
    private void viewMatchWindowCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewMatchWindowCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_viewMatchWindowCheckBoxMenuItemActionPerformed
        if( viewMatchWindowCheckBoxMenuItem.isSelected() )
        {
            matchWindow.setVisible(true);
            toFront();
        }
        else
        {
            matchWindow.setVisible(false);
        }
    }//GEN-LAST:event_viewMatchWindowCheckBoxMenuItemActionPerformed
    
    private void projectCompileMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectCompileMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectCompileMenuItemActionPerformed
        doCompileProject();
    }//GEN-LAST:event_projectCompileMenuItemActionPerformed
    
    private void projectCloseMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectCloseMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectCloseMenuItemActionPerformed
        doCloseProject();
    }//GEN-LAST:event_projectCloseMenuItemActionPerformed
    
    private void projectSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectSaveMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectSaveMenuItemActionPerformed
        doSave();
    }//GEN-LAST:event_projectSaveMenuItemActionPerformed
    
    private void projectOpenMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectOpenMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectOpenMenuItemActionPerformed
        doLoadProject();
    }//GEN-LAST:event_projectOpenMenuItemActionPerformed
    
    private void projectNewMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectNewMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectNewMenuItemActionPerformed
        doCreateProject();
    }//GEN-LAST:event_projectNewMenuItemActionPerformed
    
    private void projectExitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectExitMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectExitMenuItemActionPerformed
        doQuit();
    }//GEN-LAST:event_projectExitMenuItemActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        doQuit();
    }//GEN-LAST:event_formWindowClosing
    
    private void helpAboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpAboutMenuItemActionPerformed
        doAbout();
    }//GEN-LAST:event_helpAboutMenuItemActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem editFindInProjectMenuItem;
    private javax.swing.JMenuItem editInsertSourceMenuItem;
    private javax.swing.JMenuItem editInsertTranslationMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editOverwriteSourceMenuItem;
    private javax.swing.JMenuItem editOverwriteTranslationMenuItem;
    private javax.swing.JMenuItem editRedoMenuItem;
    private javax.swing.JMenuItem editSelectFuzzy1MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy2MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy3MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy4MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy5MenuItem;
    private javax.swing.JMenuItem editUndoMenuItem;
    private javax.swing.JMenu gotoMenu;
    private javax.swing.JMenuItem gotoNextSegmentMenuItem;
    private javax.swing.JMenuItem gotoNextUntranslatedMenuItem;
    private javax.swing.JMenuItem gotoPreviousSegmentMenuItem;
    private javax.swing.JMenuItem helpAboutMenuItem;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JScrollPane mainScroller;
    private javax.swing.JCheckBoxMenuItem optionsAlwaysConfirmQuitCheckBoxMenuItem;
    private javax.swing.JMenuItem optionsFontSelectionMenuItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem optionsSentsegMenuItem;
    private javax.swing.JMenuItem optionsSetupFileFiltersMenuItem;
    private javax.swing.JCheckBoxMenuItem optionsTabAdvanceCheckBoxMenuItem;
    private javax.swing.JMenuItem optionsWorkflowMenuItem;
    private javax.swing.JMenuItem projectCloseMenuItem;
    private javax.swing.JMenuItem projectCompileMenuItem;
    private javax.swing.JMenuItem projectEditMenuItem;
    private javax.swing.JMenuItem projectExitMenuItem;
    private javax.swing.JMenuItem projectImportMenuItem;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JMenuItem projectNewMenuItem;
    private javax.swing.JMenuItem projectOpenMenuItem;
    private javax.swing.JMenuItem projectReloadMenuItem;
    private javax.swing.JMenuItem projectSaveMenuItem;
    private javax.swing.JSeparator separator1inEditMenu;
    private javax.swing.JSeparator separator1inOptionsMenu;
    private javax.swing.JSeparator separator1inProjectMenu;
    private javax.swing.JSeparator separator2inEditMenu;
    private javax.swing.JSeparator separator2inProjectMenu;
    private javax.swing.JSeparator separator3inEditMenu;
    private javax.swing.JSeparator separator4inEditMenu;
    private javax.swing.JSeparator separator4inProjectMenu;
    private javax.swing.JSeparator separator5inProjectMenu;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem toolsValidateTagsMenuItem;
    private javax.swing.JCheckBoxMenuItem viewFileListCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem viewMatchWindowCheckBoxMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    
    private MainPane xlPane;
    private MatchGlossaryWindow matchWindow;
    private boolean screenLayoutLoaded = false;
}
