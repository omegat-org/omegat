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
package org.omegat.gui;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.TestCoreGUI;

public class ProjectMenuTest extends TestCoreGUI {

    File tempDir;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        tempDir = Files.createTempDirectory("omegat").toFile();
    }

    /**
     * Test project menus.
     */
    @Test
    public void testNewProject() {
        window.requireTitle("OmegaT 6.1.0");
        // 2. click menu Project > new project
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_NEW_MENUITEM).click();
        window.fileChooser(NewProjectFileChooser.DIALOG_NAME).requireEnabled();
        window.fileChooser().selectFile(tempDir);
        window.fileChooser().approve();
        assertFalse(Core.getProject().isProjectLoaded());
        /*
         * window.dialog(Timeout.timeout(30000L)).requireVisible();
         * window.dialog(ProjectPropertiesDialog.DIALOG_NAME).requireModal();
         * window.dialog(ProjectPropertiesDialog.DIALOG_NAME).button(
         * ProjectPropertiesDialog.OK_BUTTON_NAME).click();
         * assertTrue(Core.getProject().isProjectLoaded());
         */
        // 3. select project > Exit menu.
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_EXIT_MENUITEM).click();
        window.requireNotVisible();
        window.requireDisabled();
    }

}
