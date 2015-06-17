/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik, Didier Briel
               2013 Yu Tang, Aaron Madlon-Kay
               2014 Piotr Kulik
               2015 Yu Tang, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import javax.swing.WindowConstants;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.gui.common.OmegaTIcons;
import org.omegat.gui.dialogs.FileCollisionDialog;
import org.omegat.gui.filelist.ProjectFilesListController;
import org.omegat.gui.matches.IMatcher;
import org.omegat.gui.search.SearchWindowController;
import org.omegat.util.FileUtil;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.WikiGet;
import org.omegat.util.FileUtil.ICollisionCallback;
import org.omegat.util.gui.DockingUI;
import org.omegat.util.gui.OmegaTFileChooser;
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
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 * @author Piotr Kulik
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements IMainWindow {
    public final MainWindowMenu menu;

    protected ProjectFilesListController m_projWin;

    /**
     * The font for main window (source and target text) and for match and
     * glossary windows
     */
    private Font m_font;

    /** Set of all open search windows. */
    private final Set<SearchWindowController> m_searches = new HashSet<SearchWindowController>();

    protected JLabel lengthLabel;
    protected JLabel progressLabel;
    protected JLabel statusLabel;

    protected DockingDesktop desktop;

    /** Creates new form MainWindow */
    public MainWindow() {
        menu = new MainWindowMenu(this, new MainWindowMenuHandler(this));

        setJMenuBar(menu.initComponents());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                menu.mainWindowMenuHandler.projectExitMenuItemActionPerformed();
            }
            @Override
            public void windowDeactivated(WindowEvent we) {
                Core.getEditor().windowDeactivated();
                super.windowDeactivated(we);
            }
        });

        // load default font from preferences
        String fontName = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
        int fontSize = Preferences.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE,
                OConsts.TF_FONT_SIZE_DEFAULT);
        m_font = new Font(fontName, Font.PLAIN, fontSize);

        MainWindowUI.createMainComponents(this, m_font);

        getContentPane().add(MainWindowUI.initDocking(this), BorderLayout.CENTER);
        pack();
        getContentPane().add(MainWindowUI.createStatusBar(this), BorderLayout.SOUTH);
        
        OmegaTIcons.setIconImages(this);

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

    /** insert current fuzzy match or selection at cursor position */
    public void doInsertTrans() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        String text = getSelectedTextInMatcher();
        if (StringUtil.isEmpty(text)) {
            NearString near = Core.getMatcher().getActiveMatch();
            if (near != null) {
                text = near.translation;
            }
        }
        if (!StringUtil.isEmpty(text)) {
            Core.getEditor().insertText(text);
            Core.getEditor().requestFocus();
        }
    }

    /** replace entire edit area with active fuzzy match or selection */
    public void doRecycleTrans() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        String selection = getSelectedTextInMatcher();
        if (!StringUtil.isEmpty(selection)) {
            Core.getEditor().replaceEditText(selection);
            Core.getEditor().requestFocus();
            return;
        }

        NearString near = Core.getMatcher().getActiveMatch();
        if (near != null) {
            String translation = near.translation;
            if (Preferences.isPreference(Preferences.CONVERT_NUMBERS)) {
                translation = Core.getMatcher().substituteNumbers(Core.getEditor().getCurrentEntry().getSrcText(),
                        near.source, near.translation);
            }
            if (near.comesFrom == NearString.MATCH_SOURCE.TM
                    && ExternalTMX.isInPath(new File(Core.getProject().getProjectProperties().getTMRoot(), "mt"),
                            new File(near.projs[0]))) {
                Core.getEditor().replaceEditTextAndMark(translation);
            } else {
                Core.getEditor().replaceEditText(translation);
            }
            Core.getEditor().requestFocus();
        }
    }

    private String getSelectedTextInMatcher() {
        IMatcher matcher = Core.getMatcher();
        return matcher instanceof JTextComponent
                ? ((JTextComponent) matcher).getSelectedText()
                : null;
    }

    protected void addSearchWindow(SearchWindowController newSearchWindow) {
        synchronized (m_searches) {
            m_searches.add(newSearchWindow);
        }
    }

    public void removeSearchWindow(SearchWindowController searchWindow) {
        synchronized (m_searches) {
            m_searches.remove(searchWindow);
        }
    }

    private void closeSearchWindows() {
        synchronized (m_searches) {
            // dispose other windows
            for (SearchWindowController sw : m_searches) {
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
    public void doPromptImportSourceFiles() {
        OmegaTFileChooser chooser = new OmegaTFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle(OStrings.getString("TF_FILE_IMPORT_TITLE"));

        int result = chooser.showOpenDialog(this);
        if (result == OmegaTFileChooser.APPROVE_OPTION) {
            File[] selFiles = chooser.getSelectedFiles();
            importFiles(Core.getProject().getProjectProperties().getSourceRoot(), selFiles);
        }
    }
    
    public void importFiles(String destination, File[] toImport) {
        importFiles(destination, toImport, true);
    }
    
    public void importFiles(String destination, File[] toImport, boolean doReload) {
        try {
            FileUtil.copyFilesTo(new File(destination), toImport, new CollisionCallback());
            if (doReload) {
                ProjectUICommands.projectReload();
            }
        } catch (IOException ioe) {
            displayErrorRB(ioe, "MAIN_ERROR_File_Import_Failed");
        }
    }
    
    private class CollisionCallback implements ICollisionCallback {
        private boolean isCanceled = false;
        private boolean yesToAll = false;
        
        @Override
        public boolean shouldReplace(File file, int index, int total) {
            if (isCanceled) {
                return false;
            }
            if (yesToAll) {
                return true;
            }
            FileCollisionDialog dialog = new FileCollisionDialog(MainWindow.this);
            dialog.setFilename(file.getName());
            dialog.enableApplyToAll(total - index > 1);
            dialog.pack();
            dialog.setVisible(true);
            isCanceled = dialog.userDidCancel();
            if (isCanceled) {
                return false;
            }
            yesToAll = dialog.isApplyToAll() && dialog.shouldReplace();
            return yesToAll || dialog.shouldReplace();
        }
        
        @Override
        public boolean isCanceled() {
            return isCanceled;
        }
    };

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
        final String msg = getLocalizedString(messageKey, params);
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(msg);
            }
        });
    }
    
    private String getLocalizedString(String messageKey, Object... params) {
        if (messageKey == null) {
            return " ";
        } else if (params == null) {
            return OStrings.getString(messageKey);
        } else {
            return StaticUtils.format(OStrings.getString(messageKey), params);
        }
    }

    /**
     * Same as {@link #showStatusMessageRB(String, Object...)} but 
     * this will clear the message after ten seconds.
     * 
     * @param messageKey
     *            message key in resource bundle
     * @param params
     *            message parameters for formatting
     */
    public void showTimedStatusMessageRB(String messageKey, Object... params) {
        showStatusMessageRB(messageKey, params);

        if (messageKey == null) {
            return;
        }

        // clear the message after 10 seconds
        final String localizedString = getLocalizedString(messageKey, params);
        ActionListener clearStatus = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String text = statusLabel.getText();
                if (localizedString.equals(text)) {
                    statusLabel.setText(null);
                }
            }
        };

        final int DELAY = 10000; // milliseconds
        final Timer timer = new Timer(DELAY, clearStatus);
        timer.setRepeats(false);  // one-time only
        timer.start();
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

    /* Set progress bar tooltip text.
     * 
     * @param tooltipText
     *            tooltip text
     */
    public void setProgressToolTipText(String toolTipText) {
        progressLabel.setToolTipText(toolTipText);
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
    
    private JLabel lastDialogText;
    private String lastDialogKey;

    /**
     * {@inheritDoc}
     */
    public void displayWarningRB(String warningKey, Object... params) {
        displayWarningRB(warningKey, null, params);
    };
    
    /**
     * {@inheritDoc}
     */
    public void displayWarningRB(final String warningKey, final String supercedesKey, final Object... params) {
        UIThreadsUtil.executeInSwingThread(new Runnable() {
            public void run() {
                String msg;
                if (params != null) {
                    msg = StaticUtils.format(OStrings.getString(warningKey), params);
                } else {
                    msg = OStrings.getString(warningKey);
                }
                
                if (supercedesKey != null && lastDialogText != null && supercedesKey.equals(lastDialogKey)) {
                    Window w = SwingUtilities.getWindowAncestor(lastDialogText);
                    if (w != null) {
                        w.dispose();
                    }
                }
                
                lastDialogText = new JLabel(msg);
                lastDialogKey = warningKey;

                statusLabel.setText(msg);
                
                JOptionPane.showMessageDialog(MainWindow.this, lastDialogText, OStrings.getString("TF_WARNING"),
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

    /**
     * {@inheritDoc}
     * @see JOptionPane.showConfirmDialog
     */
    public int showConfirmDialog(Object message, String title, int optionType,
            int messageType) throws HeadlessException {
        return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
