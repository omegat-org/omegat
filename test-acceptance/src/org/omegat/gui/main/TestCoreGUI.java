/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.io.FileUtils;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.omegat.TestMainInitializer;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.threads.IAutoSave;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.gui.FontUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;
import org.omegat.util.gui.UIScale;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

public abstract class TestCoreGUI extends AssertJSwingJUnitTestCase {

    public static final String PLUGINS_LIST_FILE = "test-acceptance/plugins.properties";

    protected FrameFixture window;
    protected JFrame frame;

    @Override
    protected void onSetUp() throws Exception {
        Path tmp = Files.createTempDirectory("omegat");
        FileUtils.forceDeleteOnExit(tmp.toFile());
        RuntimePreferences.setConfigDir(tmp.toString());
        TestMainInitializer.initClassloader();
        PluginUtils.loadPlugins(Collections.emptyMap());
        Preferences.initSegmentation();
        Preferences.initFilters();
        Preferences.init();
        Core.setProject(new NotLoadedProject());
        frame = GuiActionRunner.execute(() -> {
            UIDesignManager.initialize();
            TestMainWindow mw = new TestMainWindow(TestMainWindowMenuHandler.class);
            TestCoreInitializer.initMainWindow(mw);
            TestCoreInitializer.initAutoSave(autoSave);

            CoreEvents.fireApplicationStartup();
            SwingUtilities.invokeLater(() -> {
                // setVisible can't be executed directly, because we need to
                // call all application startup listeners for initialize UI
                Core.getMainWindow().getApplicationFrame().setVisible(true);
            });
            return mw.getApplicationFrame();
        });

        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @SuppressWarnings("serial")
    static class TestMainWindow implements IMainWindow {
        private final JFrame applicationFrame;
        private FontUIResource font;
        public final BaseMainWindowMenu menu;
        public final DockingDesktop desktop;

        TestMainWindow(Class<? extends BaseMainWindowMenuHandler> mainWindowMenuHandler) throws IOException {
            applicationFrame = new JFrame();
            applicationFrame.setPreferredSize(new Dimension(1920, 1040));
            font = FontUtil.getScaledFont();
            try {
                BaseMainWindowMenuHandler handler = mainWindowMenuHandler
                        .getDeclaredConstructor(IMainWindow.class).newInstance(this);
                menu = new TestMainWindowMenu(this, handler);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            applicationFrame.setJMenuBar(menu.mainMenu);
            applicationFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            desktop = new DockingDesktop();
            desktop.addDockableStateWillChangeListener(event -> {
                if (event.getFutureState().isClosed()) {
                    event.cancel();
                }
            });
            applicationFrame.getContentPane().add(desktop, BorderLayout.CENTER);
            MainWindowStatusBar mainWindowStatusBar = new MainWindowStatusBar();
            applicationFrame.getContentPane().add(mainWindowStatusBar, BorderLayout.SOUTH);

            StaticUIUtils.setWindowIcon(applicationFrame);

            updateTitle();

            CoreEvents.registerProjectChangeListener(eventType -> {
                updateTitle();
                /*
                if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                    closeSearchWindows();
                }
                */
            });

            CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
                public void onApplicationStartup() {
                    initializeScreenLayout();
                    resetDesktopLayout();
                    // Ensure any "closed" Dockables are visible. These can be newly
                    // added panes not included in an older layout file, or e.g. panes
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

            applicationFrame.pack();
        }

        /**
         * Sets the title of the main window appropriately
         */
        private void updateTitle() {
            String s = OStrings.getDisplayNameAndVersion();
            applicationFrame.setTitle(s);
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

        @Override
        public JFrame getApplicationFrame() {
            return applicationFrame;
        }

        @Override
        public void lockUI() {
        }

        @Override
        public void unlockUI() {
        }

        @Override
        public Font getApplicationFont() {
            return font;
        }

        @Override
        public void showStatusMessageRB(final String messageKey, final Object... params) {
        }

        @Override
        public void showTimedStatusMessageRB(final String messageKey, final Object... params) {
        }

        @Override
        public void showProgressMessage(final String messageText) {
        }

        @Override
        public void showLengthMessage(final String messageText) {
        }

        @Override
        public void showLockInsertMessage(final String messageText, final String toolTip) {
        }

        @Override
        public void displayWarningRB(final String warningKey, final Object... params) {
        }

        @Override
        public void displayWarningRB(final String warningKey, final String supercedesKey,
                final Object... params) {
        }

        @Override
        public void displayErrorRB(final Throwable ex, final String errorKey, final Object... params) {
        }

        @Override
        public void showErrorDialogRB(final String title, final String message, final Object... args) {
        }

        @Override
        public int showConfirmDialog(final Object message, final String title, final int optionType,
                final int messageType) throws HeadlessException {
            return 0;
        }

        @Override
        public void showMessageDialog(final String message) {
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

        @Override
        public IMainMenu getMainMenu() {
            return menu;
        }

        @Override
        public DockingDesktop getDesktop() {
            return desktop;
        }

        @Override
        public void resetDesktopLayout() {
            try (InputStream in = MainWindowUI.class.getResourceAsStream("DockingDefaults.xml")) {
                desktop.readXML(in);
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

    static IAutoSave autoSave = new IAutoSave() {
        public void enable() {
        }

        public void disable() {
        }
    };

    static class TestMainWindowMenu extends BaseMainWindowMenu {

        TestMainWindowMenu(IMainWindow mainWindow, BaseMainWindowMenuHandler mainWindowMenuHandler) {
            super(mainWindow, mainWindowMenuHandler);
            initComponents();
        }

        @Override
        void createMenuBar() {
            mainMenu.add(projectMenu);
            mainMenu.add(editMenu);
            mainMenu.add(gotoMenu);
            mainMenu.add(viewMenu);
            mainMenu.add(toolsMenu);
            mainMenu.add(optionsMenu);
            mainMenu.add(helpMenu);
        }
    }
}
