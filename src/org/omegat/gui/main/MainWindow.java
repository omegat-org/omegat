/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik, Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.WindowConstants;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.gui.filelist.ProjectFrame;
import org.omegat.gui.search.SearchWindow;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.WikiGet;
import org.omegat.util.gui.DockingUI;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.UIThreadsUtil;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.FloatingDialog;

/**
 * The main window of OmegaT application (unless the application is started in
 * consoleMode).
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

    protected ProjectFrame m_projWin;

    /**
     * The font for main window (source and target text) and for match and
     * glossary windows
     */
    private Font m_font;

    /** Set of all open search windows. */
    private final Set<SearchWindow> m_searches = new HashSet<SearchWindow>();

    protected JLabel lengthLabel;
    protected JLabel progressLabel;
    protected JLabel statusLabel;

    protected DockingDesktop desktop;

    /** Creates new form MainWindow */
    public MainWindow() {
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

        // load default font from preferences
        String fontName = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
        int fontSize = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE,
                OConsts.TF_FONT_SIZE_DEFAULT);
        m_font = new Font(fontName, Font.PLAIN, fontSize);

        MainWindowUI.createMainComponents(this, m_font);

        getContentPane().add(MainWindowUI.initDocking(this), BorderLayout.CENTER);

        setIconImage(ResourcesUtil.getIcon("/org/omegat/gui/resources/OmegaT_small.gif").getImage());

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                updateTitle();
                if (eventType == PROJECT_CHANGE_TYPE.CLOSE) {
                    closeSearchWindows();
                }
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                MainWindowUI.resetDesktopLayout(MainWindow.this);
                MainWindowUI.loadScreenLayout(MainWindow.this);

                DockingUI.removeUnusedMenuSeparators(menu.getOptionsMenu().getPopupMenu());
            }

            public void onApplicationShutdown() {
            }
        });

        updateTitle();
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
     * {@inheritDoc}
     */
    public IMainMenu getMainMenu() {
        return menu;
    }

    /**
     * Set new font to application.
     * 
     * @param newFont
     *            new font
     */
    protected void setApplicationFont(final Font newFont) {
        m_font = newFont;
        Preferences.setPreference(OConsts.TF_SRC_FONT_NAME, newFont.getName());
        Preferences.setPreference(OConsts.TF_SRC_FONT_SIZE, newFont.getSize());

        CoreEvents.fireFontChanged(newFont);
    }

    /**
     * {@inheritDoc}
     */
    public void addDockable(Dockable pane) {
        desktop.addDockable(pane);
    }

    /**
     * Sets the title of the main window appropriately
     */
    private void updateTitle() {
        String s = OStrings.getDisplayVersion();
        if (Core.getProject().isProjectLoaded()) {
            s += " :: " + Core.getProject().getProjectProperties().getProjectName();
        }
        setTitle(s);
    }

    /** insert current fuzzy match at cursor position */
    public void doInsertTrans() {
        if (!Core.getProject().isProjectLoaded())
            return;

        NearString near = Core.getMatcher().getActiveMatch();
        if (near != null) {
            Core.getEditor().insertText(near.translation);
        }
    }

    /** replace entire edit area with active fuzzy match */
    public void doRecycleTrans() {
        if (!Core.getProject().isProjectLoaded())
            return;

        NearString near = Core.getMatcher().getActiveMatch();
        if (near != null) {
            Core.getEditor().replaceEditText(near.translation);
        }
    }

    protected void addSearchWindow(SearchWindow newSearchWindow) {
        synchronized (m_searches) {
            m_searches.add(newSearchWindow);
        }
    }

    public void removeSearchWindow(SearchWindow searchWindow) {
        synchronized (m_searches) {
            m_searches.remove(searchWindow);
        }
    }

    private void closeSearchWindows() {
        synchronized (m_searches) {
            // dispose other windows
            for (SearchWindow sw : m_searches) {
                sw.dispose();
            }
            m_searches.clear();
        }
    }

    /**
     * Imports the file/files/folder into project's source files.
     * 
     * @author Kim Bruning
     * @author Maxym Mykhalchuk
     */
    public void doImportSourceFiles() {
        OmegaTFileChooser chooser = new OmegaTFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle(OStrings.getString("TF_FILE_IMPORT_TITLE"));

        int result = chooser.showOpenDialog(this);
        if (result == OmegaTFileChooser.APPROVE_OPTION) {
            String projectsource = Core.getProject().getProjectProperties().getSourceRoot();
            File sourcedir = new File(projectsource);
            File[] selFiles = chooser.getSelectedFiles();
            try {
                for (int i = 0; i < selFiles.length; i++) {
                    File selSrc = selFiles[i];
                    if (selSrc.isDirectory()) {
                        List<String> files = new ArrayList<String>();
                        StaticUtils.buildFileList(files, selSrc, true);
                        String selSourceParent = selSrc.getParent();
                        for (String filename : files) {
                            String midName = filename.substring(selSourceParent.length());
                            File src = new File(filename);
                            File dest = new File(sourcedir, midName);
                            LFileCopy.copy(src, dest);
                        }
                    } else {
                        File dest = new File(sourcedir, selFiles[i].getName());
                        LFileCopy.copy(selSrc, dest);
                    }
                }
                ProjectUICommands.projectReload();
            } catch (IOException ioe) {
                displayErrorRB(ioe, "MAIN_ERROR_File_Import_Failed");
            }
        }
    }

    /**
     * Does wikiread
     * 
     * @author Kim Bruning
     */
    public void doWikiImport() {
        String remote_url = JOptionPane.showInputDialog(this, OStrings.getString("TF_WIKI_IMPORT_PROMPT"),
                OStrings.getString("TF_WIKI_IMPORT_TITLE"), JOptionPane.OK_CANCEL_OPTION);
        String projectsource = Core.getProject().getProjectProperties().getSourceRoot();
        // [1762625] Only try to get MediaWiki page if a string has been entered
        if ((remote_url != null) && (remote_url.trim().length() > 0)) {
            WikiGet.doWikiGet(remote_url, projectsource);
            ProjectUICommands.projectReload();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void showStatusMessageRB(final String messageKey, final Object... params) {
        final String msg;
        if (messageKey == null) {
            msg = new String() + ' ';
        } else {
            if (params != null) {
                msg = StaticUtils.format(OStrings.getString(messageKey), params);
            } else {
                msg = OStrings.getString(messageKey);
            }
        }
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                statusLabel.setText(msg);
            }
        });
    }

    /**
     * Show message in progress bar.
     * 
     * @param messageText
     *            message text
     */
    public void showProgressMessage(String messageText) {
        progressLabel.setText(messageText);
    }

    /**
     * Show message in length label.
     * 
     * @param messageText
     *            message text
     */
    public void showLengthMessage(String messageText) {
        lengthLabel.setText(messageText);
    }

    // /////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////
    // display oriented code

    /**
     * Displays a warning message.
     * 
     * @param msg
     *            the message to show
     * @param e
     *            exception occurred. may be null
     */
    public void displayWarning(final String msg, final Throwable e) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                statusLabel.setText(msg);
                String fulltext = msg;
                if (e != null)
                    fulltext += "\n" + e.toString();
                JOptionPane.showMessageDialog(MainWindow.this, fulltext, OStrings.getString("TF_WARNING"),
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void displayErrorRB(final Throwable ex, final String errorKey, final Object... params) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                String msg;
                if (params != null) {
                    msg = StaticUtils.format(OStrings.getString(errorKey), params);
                } else {
                    msg = OStrings.getString(errorKey);
                }

                statusLabel.setText(msg);
                String fulltext = msg;
                if (ex != null)
                    fulltext += "\n" + ex.toString();
                JOptionPane.showMessageDialog(MainWindow.this, fulltext, OStrings.getString("TF_ERROR"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void lockUI() {
        UIThreadsUtil.mustBeSwingThread();

        // lock application frame
        setEnabled(false);
        for (Frame f : Frame.getFrames()) {
            f.setEnabled(false);
        }
        // lock undocked dockables
        for (DockableState dock : desktop.getDockables()) {
            if (!dock.isDocked()) {
                dock.getDockable().getComponent().setEnabled(false);
                for (Container parent = dock.getDockable().getComponent().getParent(); parent != null; parent = parent
                        .getParent()) {
                    if (parent instanceof FloatingDialog) {
                        parent.setEnabled(false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unlockUI() {
        UIThreadsUtil.mustBeSwingThread();

        // unlock undocked dockables
        for (DockableState dock : desktop.getDockables()) {
            if (!dock.isDocked()) {
                for (Container parent = dock.getDockable().getComponent().getParent(); parent != null; parent = parent
                        .getParent()) {
                    if (parent instanceof FloatingDialog) {
                        parent.setEnabled(true);
                        break;
                    }
                }
                dock.getDockable().getComponent().setEnabled(true);
            }
        }
        for (Frame f : Frame.getFrames()) {
            f.setEnabled(true);
        }
        // unlock application frame
        setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public void showErrorDialogRB(String message, Object[] args, String title) {

        JOptionPane.showMessageDialog(this.getApplicationFrame(),
                StaticUtils.format(OStrings.getString(message), args), OStrings.getString(title),
                JOptionPane.ERROR_MESSAGE);
    }
}
