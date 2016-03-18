/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Files;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.custommonkey.xmlunit.XMLTestCase;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.FileUtil;
import org.omegat.util.RuntimePreferences;

import com.vlsolutions.swing.docking.Dockable;

/**
 * Core setup for unit tests.
 * 
 * @author Alexander_Buloichik
 */
public abstract class TestCore extends XMLTestCase {
    protected File configDir;

    protected void setUp() throws Exception {
        configDir = Files.createTempDirectory("omegat").toFile();
        RuntimePreferences.setConfigDir(configDir.getAbsolutePath());

        final IMainMenu mainMenu = new IMainMenu() {
            public JMenu getToolsMenu() {
                return new JMenu();
            }

            public JMenu getProjectMenu() {
                return new JMenu();
            }

            public JMenu getOptionsMenu() {
                return new JMenu();
            }

            public JMenu getMachineTranslationMenu() {
                return new JMenu();
            }

            public JMenu getGlossaryMenu() {
                return new JMenu();
            }

			public JMenuItem getProjectRecentMenuItem() {
                return new JMenu();
			}

            public JMenu getAutoCompletionMenu() {
                return new JMenu();
            }

            public void invokeAction(String action, int modifiers) {
            }
        };

        Core.setMainWindow(new IMainWindow() {
            public void addDockable(Dockable pane) {
            }

            public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            }

            public Font getApplicationFont() {
                return new Font("Dialog", Font.PLAIN, 12);
            }

            public JFrame getApplicationFrame() {
                return new JFrame();
            }

            public void lockUI() {
            }

            public void showLengthMessage(String messageText) {
            }

            public void showProgressMessage(String messageText) {
            }

            public void showStatusMessageRB(String messageKey, Object... params) {
            }

            public void displayWarningRB(String warningKey, Object... params) {
            }
            
            public void displayWarningRB(String warningKey, String supercedesKey, Object... params) {
            }

            public void showErrorDialogRB(String title, String message, Object... args) {
            }

            public void unlockUI() {
            }

            public IMainMenu getMainMenu() {
                return mainMenu;
            }
            
            public Cursor getCursor() {
                return null;
            }

            public void setCursor(Cursor cursor) {
            }

            public int showConfirmDialog(Object message, String title,
                    int optionType, int messageType) throws HeadlessException {
                return 0;
            }

            public void showMessageDialog(String message) {
            }
        });
        Core.setCurrentProject(new NotLoadedProject());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(FileUtil.deleteTree(configDir));
    }
}
