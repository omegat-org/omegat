package org.omegat.gui.dialogs;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

public class GlossaryEntryDialogTest extends TestCoreGUI {

    private File tmpDir;

    @Test
    public void testOpenGlossaryEntryDialog() throws Exception {
        // Prepare project folder
        tmpDir = Files.createTempDirectory("omegat").toFile();
        File projSrc = new File("test-acceptance/data/project/");
        FileUtils.copyDirectory(projSrc, tmpDir);
        //
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, false);
        assertFalse(Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true));
        //
        SwingUtilities.invokeAndWait(() -> ProjectUICommands.projectOpen(tmpDir));
        final JMenuItem menuItem = window.menuItem(BaseMainWindowMenu.EDIT_MENU).target();
        SwingUtilities.invokeAndWait(() -> menuItem.setEnabled(true));
        //
        window.menuItem(BaseMainWindowMenu.EDIT_MENU).click();
        window.menuItem(BaseMainWindowMenu.EDIT_CREATE_GLOSSARY_MENUITEM).click();
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).requireVisible();
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).textBox(CreateGlossaryEntry.SOURCE_TEXT_FIELD).enterText("foo");
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).textBox(CreateGlossaryEntry.TARGET_TEXT_FIELD).enterText("bar");
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).button(CreateGlossaryEntry.OK_BUTTON).click();
        // check glossary entry

    }
}
