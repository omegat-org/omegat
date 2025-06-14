/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import org.assertj.swing.core.GenericTypeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;
import org.omegat.util.Language;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

import javax.swing.JButton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ProjectPropertiesDialogTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testProjectPropertiesDialogShowAndOk() throws Exception {
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        //
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_EDIT_MENUITEM).click();
        robot().waitForIdle();
        // Project Properties dialog is modal and visible
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).requireModal();
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).requireVisible();
        // check source and target language
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).comboBox(ProjectPropertiesDialog.SOURCE_LOCALE_CB_NAME)
                .requireEditable();
        Language srcLang = new Language("en");
        Language targetLang = new Language("fr");
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).comboBox(ProjectPropertiesDialog.SOURCE_LOCALE_CB_NAME)
                .requireSelection(srcLang.getLocaleCode() + " - " + srcLang.getDisplayName());
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).comboBox(ProjectPropertiesDialog.TARGET_LOCALE_CB_NAME)
                .requireSelection(targetLang.getLocaleCode() + " - " + targetLang.getDisplayName());
        // check source and target tokenizer
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).comboBox(ProjectPropertiesDialog.SOURCE_TOKENIZER_FIELD_NAME)
                .requireSelection(LuceneEnglishTokenizer.class.getSimpleName());
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).comboBox(ProjectPropertiesDialog.TARGET_TOKENIZER_FIELD_NAME)
                .requireSelection(LuceneFrenchTokenizer.class.getSimpleName());
        // Check sentence setmenting
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).checkBox(ProjectPropertiesDialog.SENTENCE_SEGMENTING_CB_NAME)
                .requireText(Mnemonics.removeMnemonics(OStrings.getString("PP_SENTENCE_SEGMENTING")));
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).checkBox(ProjectPropertiesDialog.SENTENCE_SEGMENTING_CB_NAME)
                .requireNotSelected();
        // click Ok
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).button(ProjectPropertiesDialog.OK_BUTTON_NAME).click();
        // Click No for restart dialog
        robot().waitForIdle();
        window.dialog().button(new GenericTypeMatcher<>(JButton.class, true) {
            @Override
            protected boolean isMatching(JButton btn) {
                return btn.getText().equals("No");
            }
        }).click();
        closeProject();
    }

    @Test
    public void testProjectPropertiesDialogShowAndCancel() throws Exception {
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        //
        window.menuItem(BaseMainWindowMenu.PROJECT_MENU).click();
        window.menuItem(BaseMainWindowMenu.PROJECT_EDIT_MENUITEM).click();
        robot().waitForIdle();
        // Project Properties dialog is modal and visible
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).requireModal();
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).requireVisible();
        // click cancel and close the project
        window.dialog(ProjectPropertiesDialog.DIALOG_NAME).button(ProjectPropertiesDialog.CANCEL_BUTTON_NAME).click();
        closeProject();
    }
}
