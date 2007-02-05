/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                            Benjamin Siband, and Kim Bruning
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

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.ProjectProperties;
import org.omegat.core.StringEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.DialogThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.HelpFrame;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.SearchWindow;
import org.omegat.gui.TagValidationFrame;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.gui.dialogs.WorkflowOptionsDialog;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RequestPacket;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.Styles;

import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

import net.roydesign.mac.MRJAdapter;

/**
 * The main window of OmegaT application.
 *
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class MainWindow extends JFrame implements ActionListener, WindowListener, ComponentListener
{
    /** Creates new form MainWindow */
    public MainWindow()
    {
        m_searches = new HashSet();
        initComponents();
        createMainComponents();
        initDocking();
        additionalUIInit();
        oldInit();
        loadInstantStart();
    }

    private void createMainComponents()
    {
        editor = new EditorTextArea(this);
        matches = new MatchesTextArea();
        glossary = new GlossaryTextArea();

        String fontName = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
        int fontSize = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE, OConsts.TF_FONT_SIZE_DEFAULT);

        m_font = new Font(fontName, Font.PLAIN, fontSize);
        editor.setFont(m_font);
        matches.setFont(m_font);
        glossary.setFont(m_font);
    }
    
    private ImageIcon getIcon(String iconName)
    {
        return new ImageIcon( getClass().getResource(
                "/org/omegat/gui/resources/" +                                  // NOI18N
                iconName) );        
    }
    
    private void initDocking()
    {
        DockingUISettings.getInstance().installUI();
        UIManager.put("DockViewTitleBar.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));  // NOI18N
        UIManager.put("DockViewTitleBar.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));  // NOI18N
        UIManager.put("DockViewTitleBar.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));    // NOI18N
        UIManager.put("DockViewTitleBar.attachButtonText", OStrings.getString("DOCKING_HINT_DOCK"));        // NOI18N
        UIManager.put("DockViewTitleBar.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));       // NOI18N
        UIManager.put("DockViewTitleBar.closeButtonText", new String());                                            // NOI18N
        UIManager.put("DockTabbedPane.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));    // NOI18N
        UIManager.put("DockTabbedPane.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));    // NOI18N
        UIManager.put("DockTabbedPane.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));      // NOI18N
        UIManager.put("DockTabbedPane.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));         // NOI18N
        UIManager.put("DockTabbedPane.closeButtonText", new String());
        
        UIManager.put("DockViewTitleBar.titleFont", new JLabel().getFont());    // NOI18N
        
        UIManager.put("DockViewTitleBar.isCloseButtonDisplayed", Boolean.FALSE);// NOI18N
        
        UIManager.put("DockViewTitleBar.hide", getIcon("minimize.gif"));                    // NOI18N
        UIManager.put("DockViewTitleBar.hide.rollover", getIcon("minimize.rollover.gif"));  // NOI18N
        UIManager.put("DockViewTitleBar.hide.pressed", getIcon("minimize.pressed.gif"));    // NOI18N
        UIManager.put("DockViewTitleBar.maximize", getIcon("maximize.gif"));                // NOI18N
        UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("maximize.rollover.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("maximize.pressed.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.restore", getIcon("restore.gif"));                  // NOI18N
        UIManager.put("DockViewTitleBar.restore.rollover", getIcon("restore.rollover.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.restore.pressed", getIcon("restore.pressed.gif"));  // NOI18N
        UIManager.put("DockViewTitleBar.dock", getIcon("restore.gif"));                     // NOI18N
        UIManager.put("DockViewTitleBar.dock.rollover", getIcon("restore.rollover.gif"));   // NOI18N
        UIManager.put("DockViewTitleBar.dock.pressed", getIcon("restore.pressed.gif"));     // NOI18N
        UIManager.put("DockViewTitleBar.float", getIcon("undock.gif"));                     // NOI18N
        UIManager.put("DockViewTitleBar.float.rollover", getIcon("undock.rollover.gif"));   // NOI18N
        UIManager.put("DockViewTitleBar.float.pressed", getIcon("undock.pressed.gif"));     // NOI18N
        UIManager.put("DockViewTitleBar.attach", getIcon("dock.gif"));                      // NOI18N
        UIManager.put("DockViewTitleBar.attach.rollover", getIcon("dock.rollover.gif"));    // NOI18N
        UIManager.put("DockViewTitleBar.attach.pressed", getIcon("dock.pressed.gif"));      // NOI18N
        
        UIManager.put("DockViewTitleBar.menu.hide", getIcon("minimize.gif"));                      // NOI18N
        UIManager.put("DockViewTitleBar.menu.maximize", getIcon("maximize.gif"));                  // NOI18N
        UIManager.put("DockViewTitleBar.menu.restore", getIcon("restore.gif"));                   // NOI18N
        UIManager.put("DockViewTitleBar.menu.dock", getIcon("restore.gif"));                      // NOI18N
        UIManager.put("DockViewTitleBar.menu.float", getIcon("undock.gif"));                     // NOI18N
        UIManager.put("DockViewTitleBar.menu.attach", getIcon("dock.gif"));                    // NOI18N
        
        UIManager.put("DockViewTitleBar.menu.close", getIcon("empty.gif"));                     // NOI18N
        UIManager.put("DockTabbedPane.close", getIcon("empty.gif"));                            // NOI18N
        UIManager.put("DockTabbedPane.close.rollover", getIcon("empty.gif"));                   // NOI18N
        UIManager.put("DockTabbedPane.close.pressed", getIcon("empty.gif"));                    // NOI18N
        UIManager.put("DockTabbedPane.menu.close", getIcon("empty.gif"));                       // NOI18N
        UIManager.put("DockTabbedPane.menu.hide", getIcon("empty.gif"));                        // NOI18N
        UIManager.put("DockTabbedPane.menu.maximize", getIcon("empty.gif"));                    // NOI18N
        UIManager.put("DockTabbedPane.menu.float", getIcon("empty.gif"));                       // NOI18N
        UIManager.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif"));                    // NOI18N
        UIManager.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif"));               // NOI18N
        
        UIManager.put("DockingDesktop.closeActionAccelerator", null);           // NOI18N
        UIManager.put("DockingDesktop.maximizeActionAccelerator", null);        // NOI18N
        UIManager.put("DockingDesktop.dockActionAccelerator", null);            // NOI18N
        UIManager.put("DockingDesktop.floatActionAccelerator", null);           // NOI18N

        UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage());  // NOI18N
        
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

        statusLabel.setText(new String()+' ');
        
        loadScreenLayout();
        updateCheckboxesOnStart();
        uiUpdateOnProjectClose();
        initUIShortcuts();
        
        try
        {
            // MacOSX-specific
            MRJAdapter.addQuitApplicationListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doQuit();
                }
            });
            MRJAdapter.addAboutListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doAbout();
                }
            });
        }
        catch(NoClassDefFoundError e)
        {
            Log.log(e);
        }

        // all except MacOSX
        if(!StaticUtils.onMacOSX())   // NOI18N
        {
            projectMenu.add(separator2inProjectMenu);
            projectMenu.add(projectExitMenuItem);
        }

        // Add Language submenu to Options menu

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
     * Sets the shortcut keys.
     * Need to do it here (manually), because on MacOSX the shortcut key is CMD,
     * and on other OSes it's Ctrl.
     */
    private void initUIShortcuts()
    {
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
        setAccelerator(gotoSegmentMenuItem, KeyEvent.VK_J);
        
        setAccelerator(viewFileListMenuItem, KeyEvent.VK_L);
        
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
        int shiftmask = shift ? KeyEvent.SHIFT_MASK : 0;
        item.setAccelerator(KeyStroke.getKeyStroke(key,
                shiftmask | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
                String file = m_activeFile.substring(CommandThread.core.sourceRoot().length());
                editorScroller.setName(file);
            } catch( Exception e ) { }
        }
        setTitle(s);
    }
    
    /**
     * Old Initialization.
     */
    public void oldInit()
    {
        m_curEntryNum = -1;
        m_activeMatch = -1;
        m_activeProj = new String();
        m_activeFile = new String();
        
        ////////////////////////////////
        
        enableEvents(0);
        
        // check this only once as it can be changed only at compile time
        // should be OK, but localization might have messed it up
        String start = OStrings.getSegmentStartMarker();
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
    
    /** Shows About dialog */
    private void doAbout()
    {
        new AboutDialog(this).setVisible(true);        
    }
    
    /** Quits OmegaT */
    private void doQuit()
    {
        boolean projectModified = false;
        if (isProjectLoaded())
            projectModified = CommandThread.core.isProjectModified();

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
                return;
            }
        }

        saveScreenLayout();
        Preferences.save();

        if (isProjectLoaded())
            doSave();

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
            // create a tag validation window if necessary
            if (m_tagWin == null) {
                m_tagWin = new TagValidationFrame(this);
                m_tagWin.addWindowListener(this);
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

    public void tagValidationWindowClosed() {
        m_tagWin = null;
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
    private synchronized void doNextUntranslatedEntry()
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
    
    
    /** inserts the source text of a segment at cursor position */
    private synchronized void doInsertSource()
    {
        if (!isProjectLoaded())
            return;
        
        doInsertText(m_curEntry.getSrcText());
    }
    
    /** replaces entire edited segment text with a the source text of a segment at cursor position */
    private synchronized void doOverwriteSource()
    {
        if (!isProjectLoaded())
            return;
        
        doReplaceEditText(m_curEntry.getSrcText());
    }
    
    /** insert current fuzzy match at cursor position */
    private synchronized void doInsertTrans()
    {
        if (!isProjectLoaded())
            return;
        
        if (m_activeMatch < 0)
            return;
        
        if (m_activeMatch >= m_curEntry.getStrEntry().getNearListTranslated().size())
            return;
        
        NearString near = (NearString) m_curEntry.getStrEntry().
                getNearListTranslated().get(m_activeMatch);
        doInsertText(near.str.getTranslation());
    }
    
    /** inserts text at the cursor position */
    private synchronized void doInsertText(String text)
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
        
        if (m_activeMatch < 0)
            return;

        if (m_activeMatch >= m_curEntry.getStrEntry().getNearListTranslated().size())
            return;
        
        NearString near = (NearString) m_curEntry.getStrEntry().
                getNearListTranslated().get(m_activeMatch);
        doReplaceEditText(near.str.getTranslation());
    }
    
    /** replaces the entire edit area with a given text */
    private synchronized void doReplaceEditText(String text)
    {
        synchronized (editor) {
            // build local offsets
            int start = m_segmentStartOffset + m_sourceDisplayLength +
                    OStrings.getSegmentStartMarker().length();
            int end = editor.getTextLength() - m_segmentEndInset -
                    OStrings.getSegmentEndMarker().length();

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
        
        CommandThread.core.signalProjectClosing();
        CommandThread.core.cleanUp();
    }
    
    /** Updates UI (enables/disables menu items) upon <b>closing</b> project */
    private void uiUpdateOnProjectClose()
    {
        projectNewMenuItem.setEnabled(true);
        projectOpenMenuItem.setEnabled(true);
        
        projectImportMenuItem.setEnabled(false);
        projectReloadMenuItem.setEnabled(false);
        projectCloseMenuItem.setEnabled(false);
        projectSaveMenuItem.setEnabled(false);
        projectEditMenuItem.setEnabled(false);
        projectCompileMenuItem.setEnabled(false);
        
        editMenu.setEnabled(false);
        editFindInProjectMenuItem.setEnabled(false);
        editInsertSourceMenuItem.setEnabled(false);
        editInsertTranslationMenuItem.setEnabled(false);
        editOverwriteSourceMenuItem.setEnabled(false);
        editOverwriteTranslationMenuItem.setEnabled(false);
        editRedoMenuItem.setEnabled(false);
        editSelectFuzzy1MenuItem.setEnabled(false);
        editSelectFuzzy2MenuItem.setEnabled(false);
        editSelectFuzzy3MenuItem.setEnabled(false);
        editSelectFuzzy4MenuItem.setEnabled(false);
        editSelectFuzzy5MenuItem.setEnabled(false);
        editUndoMenuItem.setEnabled(false);

        gotoMenu.setEnabled(false);
        gotoNextSegmentMenuItem.setEnabled(false);
        gotoNextUntranslatedMenuItem.setEnabled(false);
        gotoPreviousSegmentMenuItem.setEnabled(false);
        gotoSegmentMenuItem.setEnabled(false);

        viewFileListMenuItem.setEnabled(false);
        toolsValidateTagsMenuItem.setEnabled(false);

        synchronized (editor) {
            editor.setEditable(false);
        }

        // hide project file list
        m_projWin.uiUpdateImportButtonStatus();
        m_projWin.setVisible(false);

        // dispose other windows
        if (m_tagWin != null)
            m_tagWin.dispose();
        for (Iterator i = m_searches.iterator(); i.hasNext();) {
            SearchWindow sw = (SearchWindow)i.next();
            sw.dispose();
        }
        m_searches.clear();
    }
    
    /** Updates UI (enables/disables menu items) upon <b>opening</b> project */
    private void uiUpdateOnProjectOpen()
    {
        projectNewMenuItem.setEnabled(false);
        projectOpenMenuItem.setEnabled(false);
        
        projectImportMenuItem.setEnabled(true);
        projectReloadMenuItem.setEnabled(true);
        projectCloseMenuItem.setEnabled(true);
        projectSaveMenuItem.setEnabled(true);
        projectEditMenuItem.setEnabled(true);
        projectCompileMenuItem.setEnabled(true);
        
        editMenu.setEnabled(true);
        editFindInProjectMenuItem.setEnabled(true);
        editInsertSourceMenuItem.setEnabled(true);
        editInsertTranslationMenuItem.setEnabled(true);
        editOverwriteSourceMenuItem.setEnabled(true);
        editOverwriteTranslationMenuItem.setEnabled(true);
        editRedoMenuItem.setEnabled(true);
        editSelectFuzzy1MenuItem.setEnabled(true);
        editSelectFuzzy2MenuItem.setEnabled(true);
        editSelectFuzzy3MenuItem.setEnabled(true);
        editSelectFuzzy4MenuItem.setEnabled(true);
        editSelectFuzzy5MenuItem.setEnabled(true);
        editUndoMenuItem.setEnabled(true);
        
        gotoMenu.setEnabled(true);
        gotoNextSegmentMenuItem.setEnabled(true);
        gotoNextUntranslatedMenuItem.setEnabled(true);
        gotoPreviousSegmentMenuItem.setEnabled(true);
        gotoSegmentMenuItem.setEnabled(true);
        
        viewFileListMenuItem.setEnabled(true);
        toolsValidateTagsMenuItem.setEnabled(true);
        
        synchronized (editor) {
            editor.setEditable(true);
        }
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
    
    /** Edits project's properties */
    private void doEditProject()
    {
        ProjectProperties config = CommandThread.core.getProjectProperties();
        boolean changed = false;
        try
        {
            changed = config.editProject(this);
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
            commitEntry(false); // part of fix for bug 1409309
            m_font = dlg.getSelectedFont();
            synchronized (editor) {
                editor.setFont(m_font);
            }
            matches.setFont(m_font);
            glossary.setFont(m_font);

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
        if (!isProjectLoaded())
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
        if (isProjectLoaded())
        {
            displayError( "Please close the project first!", new Exception( "Another project is open")); // NOI18N
            return;
        }

        matches.clear();
        glossary.clear();
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

    /**
      * Asks the user for a segment number and then displays the segment.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public synchronized void doGotoEntry() {
        // Create a dialog for input
        final JOptionPane input = new JOptionPane(OStrings.getString("MW_PROMPT_SEG_NR_MSG"),
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  JOptionPane.OK_CANCEL_OPTION); // create option pane
        input.setWantsInput(true); // make it require input
        final JDialog dialog = new JDialog(
            this, OStrings.getString("MW_PROMPT_SEG_NR_TITLE"), true); // create dialog
        dialog.setContentPane(input); // add option pane to dialog

        // Make the dialog verify the input
        input.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent event) {
                // Handle the event
                if (dialog.isVisible() && (event.getSource() == input)) {
                    // If user pressed Enter or OK, check the input
                    String property = event.getPropertyName();
                    Object value    = input.getValue();

                    // Don't do the checks if no option has been selected
                    if (value == JOptionPane.UNINITIALIZED_VALUE)
                        return;

                    if (   property.equals(JOptionPane.INPUT_VALUE_PROPERTY)
                        || (   property.equals(JOptionPane.VALUE_PROPERTY)
                            && ((Integer)value).intValue() == JOptionPane.OK_OPTION)) {
                        // Prevent the checks from being done twice
                        input.setValue(JOptionPane.UNINITIALIZED_VALUE);

                        // Get the value entered by the user
                        String inputValue = (String)input.getInputValue();

                        // Check if the user entered a value at all
                        if ((inputValue == null) || (inputValue.trim().length() == 0)) {
                            // Show error message
                            displayErrorMessage();
                            return;
                        }

                        // Check if the user really entered a number
                        int segmentNr = -1;
                        try {
                            // Just parse it. If parsed, it's a number.
                            segmentNr = Integer.parseInt(inputValue);
                        }
                        catch (NumberFormatException e) {
                            // If the exception is thrown, the user didn't enter a number
                            // Show error message
                            displayErrorMessage();
                            return;
                        }

                        // Check if the segment number is within bounds
                        if (segmentNr < 1 || segmentNr > CommandThread.core.numEntries()) {
                            // Tell the user he has to enter a number within certain bounds
                            displayErrorMessage();
                            return;
                        }
                    }

                    // If we're here, the user has either pressed Cancel/Esc,
                    // or has entered a valid number. In all cases, close the dialog.
                    dialog.setVisible(false);
                }
            }

            private void displayErrorMessage() {
                JOptionPane.showMessageDialog(
                    dialog,
                    StaticUtils.format(OStrings.getString("MW_SEGMENT_NUMBER_ERROR"),
                                         new Object[] {new Integer(CommandThread.core.numEntries())}),
                    OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Show the input dialog
        dialog.pack(); // make it look good
        dialog.setLocationRelativeTo(this); // center it on the main window
        dialog.setVisible(true); // show it

        // Get the input value, if any
        Object inputValue = input.getInputValue();
        if ((inputValue != null) && !inputValue.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            // Go to the segment the user requested
            try {
                doGotoEntry((String)inputValue);
            }
            catch (ClassCastException e) {
                // Shouldn't happen, but still... Just eat silently.
            }
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
                m_activeFile = new String();
                m_curEntryNum = 0;
                loadDocument();
                synchronized (this) {m_projectLoaded = true;}
                
                uiUpdateOnProjectOpen();
            }
        });
    }
    
    private void doCompileProject()
    {
        if (!isProjectLoaded())
            return;
        
        try
        {
            CommandThread.core.compileProject();
        }
        catch(IOException e)
        {
            displayError(OStrings.getString("TF_COMPILE_ERROR"), e);
        }
        catch(TranslationException te)
        {
            displayError(OStrings.getString("TF_COMPILE_ERROR"), te);
        }
    }
    
    private void doFind()
    {
        if (!isProjectLoaded())
            return;

        synchronized (editor) {
            String selection = editor.getSelectedText();
            if (selection != null)
                selection.trim();

            //SearchThread srch = new SearchThread(this, selection);
            //srch.start();
            SearchWindow search = new SearchWindow(this, selection);
            search.addWindowListener(this);
            DialogThread dt = new DialogThread(search);
            dt.start();
            m_searches.add(search);
        }
    }

    public void searchWindowClosed(SearchWindow searchWindow) {
        m_searches.remove(searchWindow);
    }

    /**
      * Restores defaults for all dockable parts.
      * May be expanded in the future to reset the entire GUI to its defaults.
      *
      * Note: The current implementation is just a quick hack, due to
      *       insufficient knowledge of the docking framework library.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public void restoreGUI() {
        try {
            String layout = "60#63#120#109#108#32#118#101#114#115#105#111#110#61#34#49#46#48#34#63#62#10#60#68#111#99#107#105#110#103#68#101#115#107#116#111#112#32#118#101#114#115#105#111#110#61#34#50#46#48#34#62#10#60#68#111#99#107#105#110#103#80#97#110#101#108#62#10#60#83#112#108#105#116#32#111#114#105#101#110#116#97#116#105#111#110#61#34#49#34#32#108#111#99#97#116#105#111#110#61#34#48#46#53#57#53#51#48#55#57#49#55#56#56#56#53#54#51#49#34#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#69#68#73#84#79#82#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#83#112#108#105#116#32#111#114#105#101#110#116#97#116#105#111#110#61#34#48#34#32#108#111#99#97#116#105#111#110#61#34#48#46#54#57#55#52#53#50#50#50#57#50#57#57#51#54#51#34#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#77#65#84#67#72#69#83#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#71#76#79#83#83#65#82#89#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#47#83#112#108#105#116#62#10#60#47#83#112#108#105#116#62#10#60#47#68#111#99#107#105#110#103#80#97#110#101#108#62#10#60#84#97#98#71#114#111#117#112#115#62#10#60#47#84#97#98#71#114#111#117#112#115#62#10#60#47#68#111#99#107#105#110#103#68#101#115#107#116#111#112#62#10";
            byte[] bytes = StaticUtils.uudecode(layout);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            desktop.readXML(in);
            in.close();
        } catch (Exception exception) {
            // eat silently, probably a bug in the docking framework
        }
    }

    /* updates status label */
    public void setMessageText(String str)
    {
        if( str.length()==0 )
            str = new String()+' ';
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
    private synchronized void loadDocument()
    {
        m_docReady = false;

        synchronized (editor) {
            // clear old text
            editor.setText(new String());

            m_curEntry = CommandThread.core.getSTE(m_curEntryNum);

            m_xlFirstEntry = m_curEntry.getFirstInFile();
            m_xlLastEntry = m_curEntry.getLastInFile();
            int xlEntries = 1+m_xlLastEntry-m_xlFirstEntry;

            DocumentSegment docSeg;
            StringBuffer textBuf = new StringBuffer();
            m_docSegList = new DocumentSegment[xlEntries];

            for (int i=0; i<xlEntries; i++)
            {
                docSeg = new DocumentSegment();

                SourceTextEntry ste = CommandThread.core.getSTE(i+m_xlFirstEntry);
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
                m_docSegList[i] = docSeg;
            }

            editor.setText(textBuf.toString());
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
        m_activeMatch = 0;
    }
    
    /**
     * Activate match by number.
     */
    private void activateMatch(int activeMatch)
    {
        m_activeMatch = activeMatch;
        matches.setActiveMatch(activeMatch);
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
    private synchronized void commitEntry(boolean forceCommit) {
        if (!isProjectLoaded())
            return;

        if (!entryActivated)
            return;
        entryActivated = false;

        synchronized (editor) {
            AbstractDocument xlDoc = (AbstractDocument)editor.getDocument();

            int start = m_segmentStartOffset + m_sourceDisplayLength +
                OStrings.getSegmentStartMarker().length();
            int end = editor.getTextLength() - m_segmentEndInset -
                OStrings.getSegmentEndMarker().length();
            String display_string;
            String new_translation;
            if (start == end)
            {
                new_translation = new String();
                display_string  = m_curEntry.getSrcText();
            }
            else
            {
                try
                {
                    new_translation = xlDoc.getText(start, end - start);
                }
                catch(BadLocationException ble)
                {
                    Log.log(IMPOSSIBLE);
                    Log.log(ble);
                    new_translation = new String();
                }
                display_string = new_translation;
            }

            int totalLen = m_sourceDisplayLength + OStrings.getSegmentStartMarker().length() +
                    new_translation.length() + OStrings.getSegmentEndMarker().length();
            try
            {
                // see http://sourceforge.net/support/tracker.php?aid=1436607
                // this method calls write locks / unlocks
                xlDoc.replace(m_segmentStartOffset, totalLen, display_string, Styles.PLAIN);
            }
            catch(BadLocationException ble)
            {
                Log.log(IMPOSSIBLE);
                Log.log(ble);
            }

            int localCur = m_curEntryNum - m_xlFirstEntry;
            DocumentSegment docSeg = m_docSegList[localCur];
            docSeg.length = display_string.length() + "\n\n".length();              // NOI18N
    
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
                    Iterator it = m_curEntry.getStrEntry().getParentList().iterator();
                    while (it.hasNext())
                    {
                        SourceTextEntry ste = (SourceTextEntry) it.next();
                        int entry = ste.entryNum();
                        if (entry>m_xlLastEntry)
                            continue;
                        else if (entry<m_xlFirstEntry)
                            break;
                        else if (entry==m_curEntryNum)
                            continue;
    
                        int localEntry = entry-m_xlFirstEntry;
                        int offset = offsets[localEntry];
    
                        // replace old text w/ new
                        docSeg = m_docSegList[localEntry];
                        String ds_nn = display_string + "\n\n";                         // NOI18N
                        try
                        {
                            // see http://sourceforge.net/support/tracker.php?aid=1436607
                            // this method calls write locks / unlocks
                            xlDoc.replace(offset, docSeg.length, ds_nn, Styles.PLAIN);
                        }
                        catch(BadLocationException ble)
                        {
                            Log.log(IMPOSSIBLE);
                            Log.log(ble);
                        }
                        docSeg.length = ds_nn.length();
                    }
                }
            }
            editor.cancelUndo();
        } // synchronize (editor)
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

        synchronized (editor) {
            AbstractDocument xlDoc = (AbstractDocument)editor.getDocument();

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

            // get label tags
            String startStr = OStrings.getSegmentStartMarker();
            String endStr = OStrings.getSegmentEndMarker();
            // <HP-experiment>
            try {
                if (m_segmentTagHasNumber)
                {
                    // put entry number in first tag
                    String num = String.valueOf(m_curEntryNum + 1);
                    int zero = startStr.lastIndexOf('0');
                    startStr = startStr.substring(0, zero-num.length()+1) + num + 
                            startStr.substring(zero+1, startStr.length()-1);
                }
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while putting segment # in start tag:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: since these are localised, don't assume number appears, keep try/catch block
            }
            // </HP-experiment>

            String translation = m_curEntry.getTranslation();

            // append to end of segment first
            try
            {
                int endStrPos = m_segmentStartOffset + docSeg.length - 2;
                xlDoc.insertString(endStrPos, endStr, Styles.BOLD);
            }
            catch(BadLocationException ble)
            {
                Log.log(IMPOSSIBLE);
                Log.log(ble);
            }
            // <HP-experiment>
            catch (Exception exception) {
                Log.log("ERROR: exception while inserting end tag:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>

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
                        xlDoc.remove(m_segmentStartOffset, translation.length());
                    }
                    catch(BadLocationException ble)
                    {
                        Log.log(IMPOSSIBLE);
                        Log.log(ble);
                    }
                    // <HP-experiment>
                    catch (Exception exception) {
                        Log.log("ERROR: exception while removing source text:");
                        Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                        Log.log(exception);
                        return; // deliberately breaking, to simulate previous behaviour
                        // FIX: unknown
                    }
                    // </HP-experiment>
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
                    List near = m_curEntry.getStrEntry().getNearListTranslated();
                    if( near.size()>0 )
                    {
                        NearString thebest = (NearString)near.get(0);
                        if( thebest.score >= percentage )
                        {
                            int old_tr_len = translation.length();
                            translation = Preferences.getPreferenceDefault(
                                    Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                    OStrings.getString("WF_DEFAULT_PREFIX")) +
                                    thebest.str.getTranslation();
                            try
                            {
                                xlDoc.replace(m_segmentStartOffset, old_tr_len, translation, Styles.PLAIN);
                            }
                            catch(BadLocationException ble)
                            {
                                Log.log(IMPOSSIBLE);
                                Log.log(ble);
                            }
                            // <HP-experiment>
                            catch (Exception exception) {
                                Log.log("ERROR: exception while inserting translation:");
                                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                                Log.log(exception);
                                return; // deliberately breaking, to simulate previous behaviour
                                // FIX: unknown
                            }
                            // </HP-experiment>
                        }
                    }
                }
            }

            try
            {
                xlDoc.insertString(m_segmentStartOffset, " ", Styles.PLAIN);        // NOI18N
                xlDoc.insertString(m_segmentStartOffset, startStr, Styles.BOLD);
                xlDoc.insertString(m_segmentStartOffset, srcText, Styles.GREEN);
            }
            catch(BadLocationException ble)
            {
                Log.log(IMPOSSIBLE);
                Log.log(ble);
            }
            // <HP-experiment>
            catch (Exception exception) {
                Log.log("ERROR: exception while inserting translation:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>

            // <HP-experiment>
            try {
                if (m_curEntry.getSrcFile().name.compareTo(m_activeFile) != 0)
                {
                    m_activeFile = m_curEntry.getSrcFile().name;
                    updateTitle();
                }
            }
            catch (Exception exception) {
                Log.log("ERROR: exception while updating title:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown
            }
            // </HP-experiment>

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
                                setMessageText(StaticUtils.format(
                                        OStrings.getString("TF_NUM_NEAR_AND_GLOSSARY"), obj));
                }
                else if (nearLength > 0)
                {
                    Object obj[] = { new Integer(nearLength) };
                    setMessageText(StaticUtils.format(
                            OStrings.getString("TF_NUM_NEAR"), obj));
                }
                else if (m_glossaryLength > 0)
                {
                    Object obj[] = { new Integer(m_glossaryLength) };
                    setMessageText(StaticUtils.format(
                            OStrings.getString("TF_NUM_GLOSSARY"), obj));
                }
                else
                    setMessageText(new String());                                       // NOI18N
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
            final int lookNext = m_segmentStartOffset + srcText.length() + 
                    OStrings.getSegmentStartMarker().length() + 1 + translation.length() + 
                    OStrings.getSegmentEndMarker().length() + offsetNext;

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
        setMessageText(msg);
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
        setMessageText(msg);
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
                        OStrings.getSegmentEndMarker().length();
                int spos = editor.getSelectionStart();
                int epos = editor.getSelectionEnd();
                if( pos>=end && spos>=end && epos>=end )
                    return false;
            }
            else
            {
                // make sure we're not at start of segment
                int start = m_segmentStartOffset + m_sourceDisplayLength +
                        OStrings.getSegmentStartMarker().length();
                int spos = editor.getSelectionStart();
                int epos = editor.getSelectionEnd();
                if( pos<=start && epos<=start && spos<=start )
                    return false;
            }
        } // synchronized (editor)

        return true;
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
            int start = m_segmentStartOffset + m_sourceDisplayLength +
                    OStrings.getSegmentStartMarker().length();
            // -1 for space before tag, -2 for newlines
            int end = editor.getTextLength() - m_segmentEndInset -
                    OStrings.getSegmentEndMarker().length();
    
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
    private Font m_font;
    
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
    
    // boolean set after safety check that org.omegat.OStrings.getSegmentStartMarker()
    //	contains empty "0000" for segment number
    private boolean	m_segmentTagHasNumber;
    
    // indicates the document is loaded and ready for processing
    public boolean	m_docReady;
    
    /** text segments in current document. */
    public DocumentSegment[] m_docSegList;
    
    public char	m_advancer;
    
    private SourceTextEntry		m_curEntry;
    
    private String  m_activeFile;
    private String  m_activeProj;
    public int      m_curEntryNum;
    private int     m_activeMatch;

    private TagValidationFrame  m_tagWin;
    private ProjectFrame	m_projWin;
    public ProjectFrame getProjectFrame()
    {
        return m_projWin;
    }
    
    private Set m_searches; // set of all open search windows
    
    public boolean m_projectLoaded;
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        separator2inProjectMenu = new javax.swing.JSeparator();
        projectExitMenuItem = new javax.swing.JMenuItem();
        statusLabel = new javax.swing.JLabel();
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
        viewFileListMenuItem = new javax.swing.JMenuItem();
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
        gotoSegmentMenuItem = new javax.swing.JMenuItem();
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
        optionsRestoreGUIMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        helpAboutMenuItem = new javax.swing.JMenuItem();

        org.openide.awt.Mnemonics.setLocalizedText(projectExitMenuItem, OStrings.getString("TF_MENU_FILE_QUIT"));
        projectExitMenuItem.addActionListener(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addComponentListener(this);
        addWindowListener(this);

        getContentPane().add(statusLabel, java.awt.BorderLayout.SOUTH);

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

        org.openide.awt.Mnemonics.setLocalizedText(viewFileListMenuItem, OStrings.getString("TF_MENU_FILE_PROJWIN"));
        viewFileListMenuItem.addActionListener(this);

        projectMenu.add(viewFileListMenuItem);

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

        org.openide.awt.Mnemonics.setLocalizedText(gotoSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_GOTO"));
        gotoSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoSegmentMenuItem);

        mainMenu.add(gotoMenu);

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

        org.openide.awt.Mnemonics.setLocalizedText(optionsRestoreGUIMenuItem, OStrings.getString("MW_OPTIONSMENU_RESTORE_GUI"));
        optionsRestoreGUIMenuItem.addActionListener(this);

        optionsMenu.add(optionsRestoreGUIMenuItem);

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

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == projectExitMenuItem) {
            MainWindow.this.projectExitMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectNewMenuItem) {
            MainWindow.this.projectNewMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectOpenMenuItem) {
            MainWindow.this.projectOpenMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectImportMenuItem) {
            MainWindow.this.projectImportMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectReloadMenuItem) {
            MainWindow.this.projectReloadMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCloseMenuItem) {
            MainWindow.this.projectCloseMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectSaveMenuItem) {
            MainWindow.this.projectSaveMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCompileMenuItem) {
            MainWindow.this.projectCompileMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectEditMenuItem) {
            MainWindow.this.projectEditMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == viewFileListMenuItem) {
            MainWindow.this.viewFileListMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editUndoMenuItem) {
            MainWindow.this.editUndoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editRedoMenuItem) {
            MainWindow.this.editRedoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editOverwriteTranslationMenuItem) {
            MainWindow.this.editOverwriteTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editInsertTranslationMenuItem) {
            MainWindow.this.editInsertTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editOverwriteSourceMenuItem) {
            MainWindow.this.editOverwriteSourceMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editInsertSourceMenuItem) {
            MainWindow.this.editInsertSourceMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editFindInProjectMenuItem) {
            MainWindow.this.editFindInProjectMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy1MenuItem) {
            MainWindow.this.editSelectFuzzy1MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy2MenuItem) {
            MainWindow.this.editSelectFuzzy2MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy3MenuItem) {
            MainWindow.this.editSelectFuzzy3MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy4MenuItem) {
            MainWindow.this.editSelectFuzzy4MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy5MenuItem) {
            MainWindow.this.editSelectFuzzy5MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextUntranslatedMenuItem) {
            MainWindow.this.gotoNextUntranslatedMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextSegmentMenuItem) {
            MainWindow.this.gotoNextSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoPreviousSegmentMenuItem) {
            MainWindow.this.gotoPreviousSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoSegmentMenuItem) {
            MainWindow.this.gotoSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == toolsValidateTagsMenuItem) {
            MainWindow.this.toolsValidateTagsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsTabAdvanceCheckBoxMenuItem) {
            MainWindow.this.optionsTabAdvanceCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsAlwaysConfirmQuitCheckBoxMenuItem) {
            MainWindow.this.optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsFontSelectionMenuItem) {
            MainWindow.this.optionsFontSelectionMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsSetupFileFiltersMenuItem) {
            MainWindow.this.optionsSetupFileFiltersMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsSentsegMenuItem) {
            MainWindow.this.optionsSentsegMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsWorkflowMenuItem) {
            MainWindow.this.optionsWorkflowMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsRestoreGUIMenuItem) {
            MainWindow.this.optionsRestoreGUIMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpContentsMenuItem) {
            MainWindow.this.helpContentsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpAboutMenuItem) {
            MainWindow.this.helpAboutMenuItemActionPerformed(evt);
        }
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
            MainWindow.this.formWindowClosing(evt);
        }
    }

    public void windowDeactivated(java.awt.event.WindowEvent evt) {
    }

    public void windowDeiconified(java.awt.event.WindowEvent evt) {
    }

    public void windowIconified(java.awt.event.WindowEvent evt) {
    }

    public void windowOpened(java.awt.event.WindowEvent evt) {
    }// </editor-fold>//GEN-END:initComponents

    private void optionsRestoreGUIMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsRestoreGUIMenuItemActionPerformed
        restoreGUI();
    }//GEN-LAST:event_optionsRestoreGUIMenuItemActionPerformed

    private void viewFileListMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewFileListMenuItemActionPerformed
    {//GEN-HEADEREND:event_viewFileListMenuItemActionPerformed
        if( m_projWin==null )
        {
            viewFileListMenuItem.setSelected(false);
            return;
        }

        // if the project window is not shown or in the background, show it
        if (!m_projWin.isActive()) {
            m_projWin.buildDisplay();
            m_projWin.setVisible(true);
            m_projWin.toFront();
        }
        // otherwise hide it
        else {
            m_projWin.setVisible(false);
        }
    }//GEN-LAST:event_viewFileListMenuItemActionPerformed

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
        saveScreenLayout();
    }//GEN-LAST:event_formComponentMoved
    
    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
        saveScreenLayout();
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
        activateMatch(4);
    }//GEN-LAST:event_editSelectFuzzy5MenuItemActionPerformed
    
    private void editSelectFuzzy4MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy4MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy4MenuItemActionPerformed
        activateMatch(3);
    }//GEN-LAST:event_editSelectFuzzy4MenuItemActionPerformed
    
    private void editSelectFuzzy3MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy3MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy3MenuItemActionPerformed
        activateMatch(2);
    }//GEN-LAST:event_editSelectFuzzy3MenuItemActionPerformed
    
    private void editSelectFuzzy2MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy2MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy2MenuItemActionPerformed
        activateMatch(1);
    }//GEN-LAST:event_editSelectFuzzy2MenuItemActionPerformed
    
    private void editSelectFuzzy1MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy1MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy1MenuItemActionPerformed
        activateMatch(0);
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
    
    private void gotoSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoSegmentMenuItemActionPerformed
        doGotoEntry();
    }//GEN-LAST:event_gotoSegmentMenuItemActionPerformed
    
    private void gotoNextSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoNextSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoNextSegmentMenuItemActionPerformed
        doNextEntry();
    }//GEN-LAST:event_gotoNextSegmentMenuItemActionPerformed
    
    private void editRedoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editRedoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editRedoMenuItemActionPerformed
        try
        {
            synchronized (editor) {editor.redoOneEdit();}
        }
        catch (CannotRedoException cue)
        { }
    }//GEN-LAST:event_editRedoMenuItemActionPerformed
    
    private void editUndoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editUndoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editUndoMenuItemActionPerformed
        try
        {
            synchronized (editor) {editor.undoOneEdit();}
        }
        catch( CannotUndoException cue )
        { }
    }//GEN-LAST:event_editUndoMenuItemActionPerformed

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
        // commit the current entry first
        commitEntry();
        activateEntry();
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
    private javax.swing.JMenuItem gotoSegmentMenuItem;
    private javax.swing.JMenuItem helpAboutMenuItem;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JCheckBoxMenuItem optionsAlwaysConfirmQuitCheckBoxMenuItem;
    private javax.swing.JMenuItem optionsFontSelectionMenuItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem optionsRestoreGUIMenuItem;
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
    private javax.swing.JMenuItem viewFileListMenuItem;
    // End of variables declaration//GEN-END:variables

    private DockingDesktop desktop;

    private DockableScrollPane editorScroller;
    private EditorTextArea editor;
    
    private DockableScrollPane matchesScroller;
    private MatchesTextArea matches;
    
    private DockableScrollPane glossaryScroller;
    private GlossaryTextArea glossary;
}
