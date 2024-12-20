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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.util.Preferences;

public abstract class TestCoreGUI extends AssertJSwingJUnitTestCase {

    protected FrameFixture window;
    protected File tmpDir;

    protected void closeProject() throws Exception {
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

    protected void openSampleProject(String projectPath) throws Exception {
        // 0. Prepare project folder
        tmpDir = Files.createTempDirectory("omegat-sample-project-").toFile();
        File projSrc = new File(projectPath);
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
            ProjectUICommands.projectOpen(tmpDir, true);
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        });
        // 3. check Project loaded
        assertTrue("Sample project should be loaded.", Core.getProject().isProjectLoaded());
    }

    @Override
    protected void onTearDown() throws Exception {
        // window.cleanUp();
    }

    @Override
    protected void onSetUp() throws Exception {
        window = TestCoreGUIInitializer.getInstance().initialize();
    }
}
