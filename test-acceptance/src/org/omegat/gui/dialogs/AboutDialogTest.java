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

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.OStrings;

public class AboutDialogTest extends TestCoreGUI {

    @Test
    public void testAboutDialog() throws InterruptedException {
        window.menuItem("TF_MENU_HELP").click();
        window.menuItem("HELP_ABOUT_MENUITEM").click();
        // Check about dialog
        window.dialog(AboutDialog.DIALOG_NAME).requireModal();
        Pattern pattern = Pattern.compile(OStrings.getApplicationDisplayName()
                + "\\s+\\S+\\s+\\d+\\.\\d+\\.\\d+(\\s+)?\\([@0-9A-Fa-fv]+\\)");
        window.dialog(AboutDialog.DIALOG_NAME).label(AboutDialog.VERSION_LABEL_NAME).requireText(pattern);
        // String aboutMessage =
        // window.dialog(AboutDialog.DIALOG_NAME).textBox().text();
        String javaVersion = window.dialog(AboutDialog.DIALOG_NAME).label(AboutDialog.JAVA_VERSION_LABEL_NAME)
                .text();
        assertTrue(javaVersion.contains(System.getProperty("java.version")));
        // check license dialog
        window.dialog(AboutDialog.DIALOG_NAME).button(AboutDialog.LICENSE_BUTTON_NAME).click();
        // close about dialog
        window.dialog(AboutDialog.DIALOG_NAME).button(AboutDialog.OK_BUTTON_NAME).click();
    }

}
