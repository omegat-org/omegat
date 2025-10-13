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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.image.ScreenshotTaker;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.jetbrains.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.omegat.TestMainInitializer;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.CoreState;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.threads.IAutoSave;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.dictionaries.DictionariesTextArea;
import org.omegat.gui.glossary.GlossaryTextArea;
import org.omegat.gui.matches.MatchesTextArea;
import org.omegat.gui.properties.SegmentPropertiesArea;
import org.omegat.util.Log;
import org.omegat.util.LocaleRule;
import org.omegat.util.Preferences;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.gui.UIDesignManager;

public abstract class TestCoreGUI extends AssertJSwingJUnitTestCase {

    protected @Nullable FrameFixture window;
    protected @Nullable JFrame frame;
    private @Nullable TestMainWindow mainWindow;
    protected @Nullable File tmpDir;

    protected int timeout = 10;

    /**
     * Close the project.
     * <p>
     *     block until the close action finished.
     */
    protected void closeProject() throws Exception {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        CoreEvents.registerProjectChangeListener(event -> {
            if (!Core.getProject().isProjectLoaded()) {
                latch.countDown();
            }
        });
        if (SwingUtilities.isEventDispatchThread()) {
            ProjectFactory.closeProject();
        } else {
            SwingUtilities.invokeLater(ProjectFactory::closeProject);
        }
        assertTrue("Project should unload within timeout.", latch.await(timeout, TimeUnit.SECONDS));
        assertFalse("Project should not be loaded.", Core.getProject().isProjectLoaded());
    }

    protected void openSampleProjectWaitPropertyPane(Path projectPath) throws Exception {
        SegmentPropertiesArea segmentPropertiesArea = CoreState.getInstance().getSegmentPropertiesArea();
        CountDownLatch latch = new CountDownLatch(1);
        segmentPropertiesArea.addPropertyChangeListener("properties", evt -> latch.countDown());
        openSampleProject(projectPath);
        assertTrue("Segment properties are not loaded.", latch.await(timeout, TimeUnit.SECONDS));
    }

    /**
     * Open project from the specified path and wait until the dictionary is loaded.
     */
    protected void openSampleProjectWaitDictionary(Path projectPath) throws Exception {
        DictionariesTextArea dictionariesTextArea = (DictionariesTextArea) Core.getDictionaries();
        CountDownLatch latch = new CountDownLatch(1);
        dictionariesTextArea.addPropertyChangeListener("displayWords", evt -> latch.countDown());
        openSampleProject(projectPath);
        assertTrue("Dictionary is not loaded.", latch.await(timeout, TimeUnit.SECONDS));
    }

    /**
     * Open project from the specified path and wait until the glossary is loaded.
     * @param projectPath project root path.
     * @throws Exception when error occurred.
     */
    protected void openSampleProjectWaitGlossary(Path projectPath) throws Exception {
        GlossaryTextArea glossaryTextArea = (GlossaryTextArea) Core.getGlossary();
        CountDownLatch latch = new CountDownLatch(1);
        glossaryTextArea.addPropertyChangeListener("entries", evt -> latch.countDown());
        openSampleProject(projectPath);
        try {
            assertTrue("Glossary is not loaded.", latch.await(timeout, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
            // Ignore and check in assertion.
        }
        assertFalse("Glossary should be loaded.", glossaryTextArea.getDisplayedEntries().isEmpty());
    }

    /**
     * Open project from the specified path and wait until the active match is set.
     */
    protected void openSampleProjectWaitMatches(Path projectPath) throws Exception {
        MatchesTextArea matchesTextArea = (MatchesTextArea) Core.getMatcher();
        CountDownLatch latch = new CountDownLatch(1);
        matchesTextArea.addPropertyChangeListener("matches", evt -> SwingUtilities.invokeLater(() -> {
            if (matchesTextArea.getActiveMatch() != null) {
                latch.countDown();
            }
        }));
        openSampleProject(projectPath);
        try {
            assertTrue("Active match not set", latch.await(timeout, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
            fail("Waiting for active match interrupted.");
        }
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
        FileUtils.copyDirectory(projectPath.toFile(), tmpDir);

        FileUtils.forceDeleteOnExit(tmpDir);
        // 1. Prepare preference for the test;
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, false);
        assertFalse(Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true));
        // 2. ject.
        CountDownLatch latch = new CountDownLatch(1);
        CoreEvents.registerProjectChangeListener(event -> {
            if (TestCoreState.getInstance().getProject().isProjectLoaded()) {
                latch.countDown();
            }
        });
        // 3. Open project directly (if we're already on EDT) or via invokeLater
        if (SwingUtilities.isEventDispatchThread()) {
            ProjectUICommands.projectOpen(tmpDir);
        } else {
            SwingUtilities.invokeLater(() -> ProjectUICommands.projectOpen(tmpDir));
        }
        // 4. Wait for project to load
        assertTrue("Project should load within timeout", latch.await(timeout, TimeUnit.SECONDS));
        assertTrue("Sample project should be loaded.", Core.getProject().isProjectLoaded());
    }

    /**
     * Clean up resources after each test.
     */
    @Override
    protected void onTearDown() throws Exception {
        try {
            // Clean up GUI components on EDT
            if (SwingUtilities.isEventDispatchThread()) {
                cleanupGUIComponents();
            } else {
                SwingUtilities.invokeAndWait(this::cleanupGUIComponents);
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "TEST_GUI_CLEANUP_FAILED");
        }

        // Reset core state
        TestCoreInitializer.initMainWindow(null);
        TestCoreState.resetState();
    }

    /**
     * Clean up GUI components. Must be called on EDT.
     */
    private void cleanupGUIComponents() {
        try {
            if (window != null) {
                window.cleanUp();
                window = null;
            }

            if (mainWindow != null) {
                mainWindow.getApplicationFrame().setVisible(false);
                mainWindow = null;
            }

            frame = null;
        } catch (Exception e) {
            Log.logErrorRB(e, "TEST_GUI_COMPONENT_CLEANUP_FAILED");
        }
    }

    /**
     * Reset state and initialize GUI components for each individual test.
     * This ensures test isolation while reusing the core system setup.
     */
    @Override
    protected void onSetUp() throws Exception {
        resetTestState();
        // Initialize GUI components on EDT
        initializeGUIComponents();
    }

    /**
     * Initialize GUI components on the Event Dispatch Thread.
     * This ensures all Swing operations happen on the correct thread.
     */
    protected void initializeGUIComponents() throws Exception {
        // Create main window on EDT and wait for completion
        CountDownLatch guiLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize UI design manager
                UIDesignManager.initialize();

                // Create main window
                mainWindow = new TestMainWindow(TestMainWindowMenuHandler.class);
                TestCoreInitializer.initMainWindow(mainWindow);

                // Set up main window in core state
                TestCoreState.getInstance().setMainWindow(mainWindow);

                // Fire application startup events
                CoreEvents.fireApplicationStartup();

                // Make window visible (must be done on EDT)
                mainWindow.getApplicationFrame().setVisible(true);

            } catch (Exception e) {
                Log.logErrorRB(e, "TEST_GUI_INITIALIZATION_FAILED");
            } finally {
                guiLatch.countDown();
            }
        });

        // Wait for GUI initialization to complete
        if (!guiLatch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("GUI initialization timed out");
        }

        if (mainWindow == null) {
            throw new IllegalStateException("Main window initialization failed");
        }

        // Set up test fixtures
        frame = mainWindow.getApplicationFrame();
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    static IAutoSave autoSave = new IAutoSave() {
        @Override
        public void enable() {
        }
        @Override
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

    private static final String IMAGE_PARENT = "build/test-results/testAcceptance/";

    /**
     * Captures a screenshot of the current desktop and saves it as a PNG file
     * in a directory structure based on the provided class name.
     *
     * @param className the name of the class used to determine the directory structure
     *                  where the screenshot will be saved
     * @param name      the name of the screenshot file
     * @throws IOException if an I/O error occurs during directory creation,
     *                     file deletion, or saving the screenshot
     */
    protected void takeScreenshot(String className, String name) throws IOException {
        Path imageDir = Paths.get(IMAGE_PARENT).resolve(className);
        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
        }
        ScreenshotTaker screenShotTaker = new ScreenshotTaker();
        Path image = imageDir.resolve(name);
        Files.deleteIfExists(image);
        screenShotTaker.saveDesktopAsPng(image.toString());
    }

    /**
     * Initialize OmegaT Core components once per test class.
     * This should be called only once for the entire test class.
     * @throws Exception when the error occurred.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Apply locale for the test class
        LocaleRule.applyLocaleForClass(new Locale("en"));

        // Initialize core system components that should persist across tests
        initializeOmegaTCore();

        // Initialize project
        initializeProject();
    }

    /**
     * Initialize the core OmegaT system components.
     * Called once per test class to set up the basic infrastructure.
     */
    private static void initializeOmegaTCore() throws Exception {
        // Set up temporary config directory for the test class
        Path configTmp = Files.createTempDirectory("omegat-config");
        FileUtils.forceDeleteOnExit(configTmp.toFile());
        FileUtils.copyDirectory(new File("test-acceptance/data/config"), configTmp.toFile());
        RuntimePreferences.setConfigDir(configTmp.toString());

        // Initialize classloader and plugins
        TestMainInitializer.initClassloader();
        PluginUtils.loadPlugins(Collections.emptyMap());
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());

        // Initialize preferences (must be after RuntimePreferences.setConfigDir)
        Preferences.init();
        Preferences.initFilters();
        Preferences.initSegmentation();
    }

    private static void initializeProject() {
        // Initialize project
        NotLoadedProject nlp = new NotLoadedProject();
        TestCoreState.getInstance().setProject(nlp);
        Core.setProject(nlp);
        TestCoreState.initAutoSave(autoSave);
    }

    /**
     * Clean up class-level resources.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        try {
            // Restore original locale
            LocaleRule.restoreLocaleForClass(Locale.getDefault());

            // Final cleanup of any remaining resources
            TestCoreState.resetState();
        } catch (Exception e) {
            Log.logErrorRB(e, "TEST_CLASS_CLEANUP_FAILED");
        }
    }

    /**
     * Reset CoreState appropriately for GUI testing.
     */
    protected void resetTestState() throws Exception {
        // Smart reset - will preserve singleton instance in GUI context
        TestCoreState.resetState();

        // Create temporary directory for this test
        tmpDir = Files.createTempDirectory("omegat-test-").toFile();
        FileUtils.forceDeleteOnExit(tmpDir);

        // Initialize the core state with clean project
        TestCoreState.getInstance().setProject(new NotLoadedProject());
        TestCoreState.initAutoSave(autoSave);
    }

}
