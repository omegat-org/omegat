/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2025 Hiroshi Miura
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

package org.omegat.core;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFrame;

import com.vlsolutions.swing.docking.Dockable;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.threads.IAutoSave;
import org.omegat.gui.editor.EditorStub;
import org.omegat.gui.editor.EditorSettingsStub;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.TestingMainMenu;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Core setup for unit tests.
 *
 * @author Alexander_Buloichik
 */
public abstract class TestCore {
    protected File configDir;
    protected IEditorSettings editorSettings;
    protected IEditor editor;

    /**
     * Set-up OmegaT Core properties for unit and functional test.
     * <p>
     * Create a temporary directory for user configuration.
     * This keeps developers' omegat configuration folder clean and
     * provides stable test conditions.
     * It also initializes a main window and editor with modular functions.
     * @throws Exception if file I/O failed.
     */
    @Before
    public final void setUpCore() throws Exception {
        TestCoreState.resetState();
        configDir = Files.createTempDirectory("omegat").toFile();
        TestPreferencesInitializer.init(configDir.getAbsolutePath());
        IMainWindow mainWindow = getMainWindow();
        TestCoreState.getInstance().setMainWindow(mainWindow);
        TestCoreState.getInstance().setProject(new NotLoadedProject());
        TestCoreState.initAutoSave(createTestAutoSave());
        initEditor(mainWindow);
    }

    protected IAutoSave createTestAutoSave() {
        return new IAutoSave() {
            @Override
            public void enable() {
                // ignore all
            }
            @Override
            public void disable() {
                // ignore all
            }
        };
    }

    /**
     * Create a mock of the main menu object.
     *
     * @return Main menu object which implement IMainMenu.
     */
    protected IMainMenu getMainMenu() {
        return new TestingMainMenu();
    }

    /**
     * Create a main Window object.
     *
     * @return Object which implements IMainWindow.
     */
    protected IMainWindow getMainWindow() {
        final IMainMenu mainMenu = getMainMenu();
        return new ConsoleWindow() {
            @Override
            public void addDockable(Dockable pane) {
            }
            @Override
            public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            }
            @Override
            public Font getApplicationFont() {
                return new Font("Dialog", Font.PLAIN, 12);
            }
            @Override
            public JFrame getApplicationFrame() {
                return new JFrame();
            }
            @Override
            public void showLengthMessage(String messageText) {
                // do nothing
            }
            @Override
            public void showProgressMessage(String messageText) {
                // do nothing
            }
            @Override
            public IMainMenu getMainMenu() {
                return mainMenu;
            }
        };
    }

    /**
     * Initialize editor and store it with TestInitializer.initEditor function.
     */
    protected void initEditor(IMainWindow mainWindow) {
        editorSettings = new EditorSettingsStub();
        editor = new EditorStub(editorSettings);
        TestCoreInitializer.initEditor(editor);
    }

    /**
     * Clean up a temporary directory for configuration.
     * @throws IOException if file access failed.
     */
    @After
    public final void tearDownCore() throws IOException {
        TestCoreState.resetState();
        FileUtils.forceDeleteOnExit(configDir);
    }
}
