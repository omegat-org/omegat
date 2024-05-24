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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

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

    @Override
    protected void onSetUp() throws Exception {
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
        //
        frame = GuiActionRunner.execute(() -> {
            Core.setProject(new NotLoadedProject());
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
