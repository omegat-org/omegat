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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.omegat.TestMainInitializer;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.threads.IAutoSave;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Preferences;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.gui.UIDesignManager;

public abstract class TestCoreGUI extends AssertJSwingJUnitTestCase {

    protected FrameFixture window;
    protected JFrame frame;
    private TestMainWindow mainWindow;

    protected File tmpDir;

    /**
     * Close the project.
     * <p>
     *     block until the close action finished.
     */
    protected void closeProject() {
        CountDownLatch latch = new CountDownLatch(1);
        Core.getProject().closeProject();
        CoreEvents.registerProjectChangeListener(event -> {
            if (!Core.getProject().isProjectLoaded()) {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        assertFalse("Project should not be loaded.", Core.getProject().isProjectLoaded());
    }

    /**
     * Open project from the specified path.
     *
     * @param projectPath project root path.
     * @throws Exception when error occurred.
     */
    protected void openSampleProject(String projectPath) throws Exception {
        openSampleProject(Path.of(projectPath));
    }

    /**
     * Opens a sample project from the specified path for testing purposes.
     *
     * @param projectPath the path to the sample project to be opened
     * @throws Exception if an error occurs while opening the project
     */
    protected void openSampleProject(Path projectPath) throws Exception {
       // 0. Prepare project folder
        tmpDir = Files.createTempDirectory("omegat-sample-project-").toFile();
        File projSrc = projectPath.toFile();
        FileUtils.copyDirectory(projSrc, tmpDir);
        FileUtils.forceDeleteOnExit(tmpDir);
        // 1. Prepare preference for the test;
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, false);
        assertFalse(Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true));
        // 2. Open a sample project.
        SwingUtilities.invokeAndWait(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            CoreEvents.registerProjectChangeListener(event -> {
                if (Core.getProject().isProjectLoaded()) {
                    latch.countDown();
                }
            });
            ProjectUICommands.projectOpen(tmpDir);
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        });
        // 3. check Project loaded
        assertTrue("Sample project should be loaded.", Core.getProject().isProjectLoaded());
    }

    /**
     * Clean up OmegaT main window.
     * @throws Exception
     */
    @Override
    protected void onTearDown() throws Exception {
        Core.setProject(new NotLoadedProject());
        TestCoreInitializer.initMainWindow(null);
        mainWindow.getApplicationFrame().setVisible(false);
        window.cleanUp();
    }

    private static boolean initialized = false;

    /**
     * set up OmegaT main window.
     * @throws Exception
     */
    @Override
    protected void onSetUp() throws Exception {
        initialize();
        mainWindow = GuiActionRunner.execute(() -> {
            TestMainWindow mw = new TestMainWindow(TestMainWindowMenuHandler.class);
            TestCoreInitializer.initMainWindow(mw);
            CoreEvents.fireApplicationStartup();
            SwingUtilities.invokeLater(() -> {
                // setVisible can't be executed directly, because we need to
                // call all application startup listeners for initialize UI
                mw.getApplicationFrame().setVisible(true);
            });
            return mw;
        });
        frame = Objects.requireNonNull(mainWindow).getApplicationFrame();
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    /**
     * Initialize OmegaT Core startup only once.
     * @throws Exception when error occurred.
     */
    protected void initialize() throws Exception {
        if (!initialized) {
            Path tmp = Files.createTempDirectory("omegat");
            FileUtils.forceDeleteOnExit(tmp.toFile());
            RuntimePreferences.setConfigDir(tmp.toString());
            TestMainInitializer.initClassloader();
            // same order as Main.main
            Preferences.init();
            PluginUtils.loadPlugins(Collections.emptyMap());
            FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
            Preferences.initFilters();
            Preferences.initSegmentation();
            TestCoreInitializer.initAutoSave(autoSave);
            UIDesignManager.initialize();
            Core.setProject(new NotLoadedProject());
            initialized = true;
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
