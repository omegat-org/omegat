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

 This program is distributed in the hope that it will be useful,
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

import javax.swing.JFrame;

import org.custommonkey.xmlunit.XMLTestCase;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.RuntimePreferences;

import com.vlsolutions.swing.docking.Dockable;

/**
 * Core setup for unit tests.
 * 
 * @author Alexander_Buloichik
 */
public abstract class TestCore extends XMLTestCase {
    protected void setUp() throws Exception {
        File configDir = new File(System.getProperty("java.io.tmpdir"), "OmegaT test config");
        removeDir(configDir);

        RuntimePreferences.setConfigDir(configDir.getAbsolutePath());

        Core.setMainWindow(new IMainWindow() {
            public void addDockable(Dockable pane) {
            }

            public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            }

            public Font getApplicationFont() {
                return new Font("Dialog", Font.PLAIN, 12);
            }

            public JFrame getApplicationFrame() {
                return null;
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

            public void showErrorDialogRB(String message, Object[] args, String title) {
            }

            public void unlockUI() {
            }

            public IMainMenu getMainMenu() {
                return null;
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

    protected static void removeDir(File dir) {
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    removeDir(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
