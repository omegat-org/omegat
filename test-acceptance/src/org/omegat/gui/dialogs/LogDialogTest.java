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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.TestCoreGUI;

public class LogDialogTest extends TestCoreGUI {

    @Test
    public void testLogDialog() {
        window.menuItem(BaseMainWindowMenu.HELP_MENU).click();
        window.menuItem(BaseMainWindowMenu.HELP_LOG_MENUITEM).click();
        window.dialog(LogDialog.DIALOG_NAME).requireModal();
        assertFalse(
                window.dialog(LogDialog.DIALOG_NAME).textBox(LogDialog.LOG_TEXTPANE_NAME).text().isEmpty());
        window.dialog(LogDialog.DIALOG_NAME).button(LogDialog.OK_BUTTON_NAME).click();
    }

}
