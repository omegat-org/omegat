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
               2023 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.search.SearchWindowController;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.FontUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;
import org.omegat.util.gui.UIScale;
import org.omegat.util.gui.UIThreadsUtil;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.FloatingDialog;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;

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
public class MainWindow implements IMainWindow {

    public static final String UI_LAYOUT_FILE = "uiLayout" + OStrings.getBrandingToken() + ".xml";
    private final JFrame applicationFrame;
    protected BaseMainWindowMenu menu;

    /**
     * The font for main window (source and target text) and for match and
     * glossary windows
     */
    private FontUIResource font;

    /** Set of all open search windows. */
    private final List<SearchWindowController> searches = new ArrayList<>();

    protected MainWindowStatusBar mainWindowStatusBar;

    protected DockingDesktop desktop;

    /** Creates new form MainWindow */
    public MainWindow() throws IOException {
        applicationFrame = new JFrame();
        applicationFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        initMainMenu();

        // load default font from preferences
        font = FontUtil.getScaledFont();
        initDockingAndStatusBar();
        StaticUIUtils.setWindowIcon(applicationFrame);

        CoreEvents.registerProjectChangeListener(eventType -> {
            updateTitle();
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                closeSearchWindows();
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                initializeScreenLayout();
                loadScreenLayoutFromPreferences();
                // Ensure any "closed" Dockables are visible. These can be newly
                // added panes not included in an older layout file, or e.g.
                // panes
                // installed by plugins.
                UIDesignManager.ensureDockablesVisible(desktop);
                UIDesignManager.removeUnusedMenuSeparators(menu.getOptionsMenu().getPopupMenu());
            }

            public void onApplicationShutdown() {
            }
        });

        UIScale.addPropertyChangeListener(evt -> CoreEvents.fireFontChanged(FontUtil.getScaledFont()));
        CoreEvents.registerFontChangedEventListener(
                newFont -> font = (newFont instanceof FontUIResource) ? (FontUIResource) newFont
                        : new FontUIResource(newFont));

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

    @SuppressWarnings("unchecked")
    private void initMainMenu() {
        MainWindowMenuHandler mainWindowMenuHandler = new MainWindowMenuHandler(this);

        // Load Menu extension
        Object menuClass = UIManager.get(UIDesignManager.menuClassID);
        if (menuClass != null) {
            BaseMainWindowMenu menu1;
            try {
                menu1 = ((Class<? extends BaseMainWindowMenu>) menuClass)
                        .getDeclaredConstructor(MainWindow.class, MainWindowMenuHandler.class)
                        .newInstance(this, mainWindowMenuHandler);
            } catch (Exception e) {
                // fall back to default when loading failed.
                menu1 = new MainWindowMenu(this, mainWindowMenuHandler);
            }
            menu = menu1;
        } else {
            // Default menu.
            menu = new MainWindowMenu(this, mainWindowMenuHandler);
        }
        applicationFrame.setJMenuBar(menu.mainMenu);

        applicationFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mainWindowMenuHandler.projectExitMenuItemActionPerformed();
            }

            @Override
            public void windowDeactivated(WindowEvent we) {
                Core.getEditor().windowDeactivated();
            }
        });

        // Load toolbar extension
        Object toolbarClass = UIManager.get(UIDesignManager.toolbarClassID);
        if (toolbarClass != null) {
            try {
                applicationFrame.getContentPane()
                        .add((Component) ((Class<?>) toolbarClass)
                                .getDeclaredConstructor(MainWindow.class, MainWindowMenuHandler.class)
                                .newInstance(this, mainWindowMenuHandler), BorderLayout.NORTH);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException ignored) {
            }
        }
    }

    /**
     * Create docking desktop panel and status bar.
     */
    private void initDockingAndStatusBar() {
        desktop = new DockingDesktop();
        desktop.addDockableStateWillChangeListener(new DockableStateWillChangeListener() {
            public void dockableStateWillChange(DockableStateWillChangeEvent event) {
                if (event.getFutureState().isClosed()) {
                    event.cancel();
                }
            }
        });
        applicationFrame.getContentPane().add(desktop, BorderLayout.CENTER);
        mainWindowStatusBar = new MainWindowStatusBar();
        applicationFrame.getContentPane().add(mainWindowStatusBar, BorderLayout.SOUTH);
        applicationFrame.pack();
    }

    /**
     * Installs a {@link IProjectEventListener} that handles loading, storing,
     * and restoring the main window layout when a project-specific layout is
     * present.
     */
    private static void handlePerProjectLayouts(final MainWindow mainWindow) {
        PerProjectLayoutHandler handler = new PerProjectLayoutHandler(mainWindow);
        CoreEvents.registerProjectChangeListener(handler);
        CoreEvents.registerApplicationEventListener(handler);
    }

    /**
     * Initialize the size of OmegaT window, then load the layout prefs.
     */
    public void initializeScreenLayout() {
        /*
         * (23dec22) Set a reasonable default window size assuming a
         * standard"pro" laptop resolution of 1920x1080. Smaller screens do not
         * need to be considered since OmegaT will just use the whole window
         * size in such cases.
         */

        // Check the real available space accounting for macOS DOCK, Windows
        // Toolbar, etc.
        Rectangle localAvailableSpace = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int screenWidth = localAvailableSpace.width;
        int screenHeight = localAvailableSpace.height;
        int omegatWidth = OConsts.OMEGAT_WINDOW_WIDTH;
        int omegatHeight = OConsts.OMEGAT_WINDOW_HEIGHT;

        if (omegatWidth > screenWidth) {
            omegatWidth = screenWidth;
        }

        if (omegatHeight > screenHeight) {
            omegatHeight = screenHeight;
        }

        // Attempt to center the OmegaT main window on the screen
        int omegatLeftPosition = (screenWidth - omegatWidth) / 2;

        Rectangle defaultWindowSize = new Rectangle(omegatLeftPosition, 0, omegatWidth, omegatHeight);
        applicationFrame.setBounds(defaultWindowSize);
    }

    /**
     * {@inheritDoc}
     */
    public JFrame getApplicationFrame() {
        return applicationFrame;
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

    @Override
    public void setCursor(final Cursor cursor) {
        applicationFrame.setCursor(cursor);
    }

    @Override
    public Cursor getCursor() {
        return applicationFrame.getCursor();
    }

    /**
     * Sets the title of the main window appropriately
     */
    private void updateTitle() {
        String s = OStrings.getDisplayNameAndVersion();
        if (Core.getProject().isProjectLoaded()) {
            s += " :: " + Core.getProject().getProjectProperties().getProjectName();
        }
        applicationFrame.setTitle(s);
    }

    @Override
    public void addSearchWindow(final SearchWindowController newSearchWindow) {
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

    public List<SearchWindowController> getSearchWindows() {
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
                mainWindowStatusBar.setStatusLabel(msg);
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
            String text = mainWindowStatusBar.getStatusLabel();
            if (localizedString.equals(text)) {
                mainWindowStatusBar.setStatusLabel(null);
            }
        });
        timer.setRepeats(false); // one-time only
        timer.start();
    }

    /**
     * Show message in progress bar.
     *
     * @param messageText
     *            message text
     */
    public void showProgressMessage(String messageText) {
        mainWindowStatusBar.setProgressLabel(messageText);
    }

    /*
     * Set progress bar tooltip text.
     *
     * @param tooltipText tooltip text
     */
    public void setProgressToolTipText(String toolTipText) {
        mainWindowStatusBar.setProgressToolTip(toolTipText);
    }

    /**
     * Show message in length label.
     *
     * @param messageText
     *            message text
     */
    public void showLengthMessage(String messageText) {
        mainWindowStatusBar.setLengthLabel(messageText);
    }

    public void showLockInsertMessage(String messageText, String toolTip) {
        mainWindowStatusBar.setLockInsertLabel(messageText);
        mainWindowStatusBar.setLockInsertToolTipText(toolTip);
    }

    // /////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////
    // display oriented code

    private JPanel lastDialogText;
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
    public void displayWarningRB(final String warningKey, final String supercedesKey,
            final Object... params) {
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

            lastDialogText = new JPanel();
            lastDialogText.setLayout(new BoxLayout(lastDialogText, BoxLayout.PAGE_AXIS));
            lastDialogText.setBorder(BorderFactory.createEmptyBorder());
            String[] messages = msg.split("\\n");
            Arrays.stream(messages).forEach(m -> {
                lastDialogText.add(new JLabel(m));
            });
            lastDialogKey = warningKey;

            mainWindowStatusBar.setStatusLabel(messages[0]);

            JOptionPane.showMessageDialog(applicationFrame, lastDialogText, OStrings.getString("TF_WARNING"),
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

            String[] messages = msg.split("\\n");
            mainWindowStatusBar.setStatusLabel(messages[0]);
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
            pane.setSize(new Dimension(900, 400));
            Arrays.stream(messages).forEach(m -> {
                JLabel jlabel = new JLabel(m);
                jlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                pane.add(jlabel);
            });

            if (ex != null && ex.getLocalizedMessage() != null) {
                pane.add(Box.createRigidArea(new Dimension(0, 5)));
                JTextArea message = new JTextArea();
                message.setBorder(BorderFactory.createEmptyBorder());
                message.setText(ex.getLocalizedMessage());
                message.setLineWrap(true);
                message.setEditable(false);
                JScrollPane jScrollPane = new JScrollPane(message,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                jScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                jScrollPane.setPreferredSize(new Dimension(800, 200));
                jScrollPane.getVerticalScrollBar().setValue(0);
                pane.add(jScrollPane);
                pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                JButton jbutton = new JButton(OStrings.getString("TF_ERROR_COPY_CLIPBOARD"));
                // Copy to clipboard action
                jbutton.addActionListener(l -> {
                    String clipboardMsg = String.format("%s%n---%n%s%n---%n%s%n", msg,
                            ex.getLocalizedMessage(), StaticUtils.getSupportInfo());
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(clipboardMsg), null);
                });
                jbutton.setAlignmentX(Component.LEFT_ALIGNMENT);
                pane.add(jbutton);
            }

            JOptionPane.showMessageDialog(applicationFrame, pane, OStrings.getString("TF_ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * {@inheritDoc}
     */
    public void lockUI() {
        UIThreadsUtil.mustBeSwingThread();

        // lock application frame
        applicationFrame.setEnabled(false);
        for (Frame f : Frame.getFrames()) {
            f.setEnabled(false);
        }
        // lock undocked dockables
        for (DockableState dock : desktop.getDockables()) {
            if (!dock.isDocked()) {
                dock.getDockable().getComponent().setEnabled(false);
                for (Container parent = dock.getDockable().getComponent()
                        .getParent(); parent != null; parent = parent.getParent()) {
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
                for (Container parent = dock.getDockable().getComponent()
                        .getParent(); parent != null; parent = parent.getParent()) {
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
        applicationFrame.setEnabled(true);
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
    public int showConfirmDialog(Object message, String title, int optionType, int messageType)
            throws HeadlessException {
        return JOptionPane.showConfirmDialog(applicationFrame, message, title, optionType, messageType);
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(applicationFrame, message);
    }

    /**
     * get DockableDesktop object.
     */
    public DockingDesktop getDesktop() {
        return desktop;
    }

    /**
     * Load the main window layout from the global preferences file. Will reset
     * to defaults if global preferences are not present or if an error occurs.
     */
    public void loadScreenLayoutFromPreferences() {
        File uiLayoutFile = new File(StaticUtils.getConfigDir(), UI_LAYOUT_FILE);
        if (uiLayoutFile.exists()) {
            loadScreenLayout(uiLayoutFile);
        } else {
            resetDesktopLayout();
        }
    }

    /**
     * Load the main window layout from the specified file. Will reset to
     * defaults if an error occurs.
     */
    public void loadScreenLayout(File uiLayoutFile) {
        try (InputStream in = new FileInputStream(uiLayoutFile)) {
            desktop.readXML(in);
        } catch (Exception ex) {
            Log.log(ex);
            resetDesktopLayout();
        }
    }

    /**
     * Stores main window docking layout to disk.
     */
    public void saveScreenLayout() {
        File uiLayoutFile = new File(StaticUtils.getConfigDir(), UI_LAYOUT_FILE);
        saveScreenLayout(uiLayoutFile);
    }

    /**
     * Stores main window layout to the specified output file.
     */
    public void saveScreenLayout(File uiLayoutFile) {
        try (OutputStream out = new FileOutputStream(uiLayoutFile)) {
            desktop.writeXML(out);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * Restores main window layout to the default values (distinct from global
     * preferences).
     */
    @Override
    public void resetDesktopLayout() {
        try (InputStream in = MainWindowUI.class.getResourceAsStream("DockingDefaults.xml")) {
            desktop.readXML(in);
        } catch (Exception e) {
            Log.log(e);
        }
    }

    static class PerProjectLayoutHandler implements IProjectEventListener, IApplicationEventListener {

        private final MainWindow mainWindow;
        private boolean didApplyPerProjectLayout = false;

        PerProjectLayoutHandler(MainWindow mainWindow) {
            this.mainWindow = mainWindow;
        }

        @Override
        public void onApplicationStartup() {
        }

        @Override
        public void onApplicationShutdown() {
            // Project is not closed before shutdown, so we need to handle this
            // separately
            // from the onProjectChanged events.
            if (Core.getProject().isProjectLoaded() && didApplyPerProjectLayout) {
                mainWindow.loadScreenLayoutFromPreferences();
                didApplyPerProjectLayout = false;
            }
        }

        @Override
        public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
            if (eventType == PROJECT_CHANGE_TYPE.CLOSE && didApplyPerProjectLayout) {
                mainWindow.loadScreenLayoutFromPreferences();
                didApplyPerProjectLayout = false;
                return;
            }
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            File perProjLayout = getPerProjectLayout();
            if (!perProjLayout.isFile()) {
                return;
            }
            switch (eventType) {
            case LOAD:
                mainWindow.saveScreenLayout();
                mainWindow.loadScreenLayout(perProjLayout);
                didApplyPerProjectLayout = true;
                break;
            case SAVE:
                mainWindow.saveScreenLayout(perProjLayout);
                break;
            default:
            }
        }

        private File getPerProjectLayout() {
            return new File(Core.getProject().getProjectProperties().getProjectInternal(), UI_LAYOUT_FILE);
        }
    }
}
