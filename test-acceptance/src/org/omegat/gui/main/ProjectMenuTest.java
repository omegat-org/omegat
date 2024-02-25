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

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.NewProjectFileChooser;

public class ProjectMenuTest extends TestCoreGUI {

    File tempDir;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        tempDir = Files.createTempDirectory("omegat").toFile();
    }

    @Test
    public void testMainWindowTitle() {
        window.requireTitle("OmegaT 6.1.0");
    }

    @Test
    public void testNewProject() {
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_NEW_MENUITEM).click();
        window.fileChooser(NewProjectFileChooser.DIALOG_NAME).requireEnabled();
        window.fileChooser().selectFile(tempDir);
        window.fileChooser().approve();
        assertTrue(Core.getProject().isProjectLoaded());
        assertFalse(Core.getProject().isRemoteProject());
    }

    @Test
    public void testExit() {
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_EXIT_MENUITEM).click();
        assertTrue(TestMainWindowMenuHandler.quited);
    }


}
