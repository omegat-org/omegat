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
               2016 Didier Briel
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.gui.filelist.ProjectFilesListController;
import org.omegat.gui.matches.IMatcher;
import org.omegat.gui.search.SearchWindowController;
import org.omegat.util.FileUtil;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;
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
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements IMainWindow {
    public final MainWindowMenu menu;

    protected ProjectFilesListController projWin;

    /**
     * The font for main window (source and target text) and for match and
     * glossary windows
     */
    private Font font;

    /** Set of all open search windows. */
    private final List<SearchWindowController> searches = new ArrayList<>();

    protected JLabel lengthLabel;
    protected JLabel progressLabel;
    protected JLabel statusLabel;
    protected JLabel lockInsertLabel;

    protected DockingDesktop desktop;

    /** Creates new form MainWindow */
    public MainWindow() throws IOException {

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
            }
        });

        // load default font from preferences
        String fontName = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_NAME, Preferences.TF_FONT_DEFAULT);
        int fontSize = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_SIZE,
                Preferences.TF_FONT_SIZE_DEFAULT);
        font = new Font(fontName, Font.PLAIN, fontSize);

        MainWindowUI.createMainComponents(this, font);

        getContentPane().add(MainWindowUI.initDocking(this), BorderLayout.CENTER);
        pack();
        getContentPane().add(MainWindowUI.createStatusBar(this), BorderLayout.SOUTH);

        StaticUIUtils.setWindowIcon(this);

        CoreEvents.registerProjectChangeListener(eventType -> {
            updateTitle();
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                closeSearchWindows();
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                MainWindowUI.initializeScreenLayout(MainWindow.this);

                UIDesignManager.removeUnusedMenuSeparators(menu.getOptionsMenu().getPopupMenu());
            }

            public void onApplicationShutdown() {
            }
        });

        CoreEvents.registerFontChangedEventListener(newFont -> font = newFont);

        MainWindowUI.handlePerProjectLayouts(this);

        updateTitle();

        // Set up prompt to reload if segmentation or filters settings change
        Preferences.addPropertyChangeListener(evt -> {
            if (Core.getProject().isProjectLoaded()) {
                String prop = evt.getPropertyName();
                if (prop.equals(Preferences.PROPERTY_SRX)
                        && Core.getProject().getProjectProperties().getProjectSRX() == null) {
                    SwingUtilities.invokeLater(ProjectUICommands::promptReload);
                } else if (prop.equals(Preferences.PROPERTY_FILTERS)
                        && Core.getProject().getProjectProperties().getProjectFilters() == null) {
                    SwingUtilities.invokeLater(ProjectUICommands::promptReload);
                }
            }
        });
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
        return font;
    }

    /**
     * {@inheritDoc}
     */
    public IMainMenu getMainMenu() {
        return menu;
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
        String s = OStrings.getDisplayNameAndVersion();
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
        boolean fromMT = false;
        if (StringUtil.isEmpty(text)) {
            NearString near = Core.getMatcher().getActiveMatch();
            if (near != null) {
                text = near.translation;
                if (Preferences.isPreference(Preferences.CONVERT_NUMBERS)) {
                    text = Core.getMatcher().substituteNumbers(Core.getEditor().getCurrentEntry().getSrcText(),
                        near.source, near.translation);
                }

                if (near.comesFrom == NearString.MATCH_SOURCE.TM
                    && FileUtil.isInPath(new File(Core.getProject().getProjectProperties().getTMRoot(), "mt"),
                    new File(near.projs[0]))) {
                    fromMT = true;
                }
            }
        }
        if (!StringUtil.isEmpty(text)) {
            if (fromMT) {
                Core.getEditor().insertTextAndMark(text);
            } else {
                Core.getEditor().insertText(text);
            }
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
                    && FileUtil.isInPath(new File(Core.getProject().getProjectProperties().getTMRoot(), "mt"),
                            new File(near.projs[0]))) {
                Core.getEditor().replaceEditTextAndMark(translation, "TM:[tm/mt]");
            } else {
                Core.getEditor().replaceEditText(translation, "TM:[generic]");
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

    protected void addSearchWindow(final SearchWindowController newSearchWindow) {
        newSearchWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                removeSearchWindow(newSearchWindow);
            }
        });
        synchronized (searches) {
            searches.add(newSearchWindow);
        }
    }

    private void removeSearchWindow(SearchWindowController searchWindow) {
        synchronized (searches) {
            searches.remove(searchWindow);
        }
    }

    private void closeSearchWindows() {
        synchronized (searches) {
            // dispose other windows
            for (SearchWindowController sw : searches) {
                sw.dispose();
            }
            searches.clear();
        }
    }

    protected List<SearchWindowController> getSearchWindows() {
        return Collections.unmodifiableList(searches);
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
            return StringUtil.format(OStrings.getString(messageKey), params);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showTimedStatusMessageRB(String messageKey, Object... params) {
        showStatusMessageRB(messageKey, params);

        if (messageKey == null) {
            return;
        }

        // clear the message after 10 seconds
        String localizedString = getLocalizedString(messageKey, params);
        Timer timer = new Timer(10_000, evt -> {
            String text = statusLabel.getText();
            if (localizedString.equals(text)) {
                statusLabel.setText(null);
            }
        });
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

    public void showLockInsertMessage(String messageText, String toolTip) {
        lockInsertLabel.setText(messageText);
        lockInsertLabel.setToolTipText(toolTip);
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
        UIThreadsUtil.executeInSwingThread(() -> {
            String msg;
            if (params != null) {
                msg = StringUtil.format(OStrings.getString(warningKey), params);
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
        });
    }

    /**
     * {@inheritDoc}
     */
    public void displayErrorRB(final Throwable ex, final String errorKey, final Object... params) {
        UIThreadsUtil.executeInSwingThread(() -> {
            String msg;
            if (params != null) {
                msg = StringUtil.format(OStrings.getString(errorKey), params);
            } else {
                msg = OStrings.getString(errorKey);
            }

            statusLabel.setText(msg);
            String fulltext = msg;
            if (ex != null) {
                fulltext += "\n" + ex.toString();
            }
            JOptionPane.showMessageDialog(MainWindow.this, fulltext, OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
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
    public void showErrorDialogRB(String title, String message, Object... args) {

        JOptionPane.showMessageDialog(this.getApplicationFrame(),
                StringUtil.format(OStrings.getString(message), args), OStrings.getString(title),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     *
     * @see JOptionPane#showConfirmDialog(java.awt.Component, Object, String,
     *      int, int)
     */
    public int showConfirmDialog(Object message, String title, int optionType,
            int messageType) throws HeadlessException {
        return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
