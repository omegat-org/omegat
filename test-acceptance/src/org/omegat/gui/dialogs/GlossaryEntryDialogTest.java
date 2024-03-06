package org.omegat.gui.dialogs;

import static org.junit.Assert.assertFalse;

import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.omegat.gui.glossary.GlossaryTextArea;
import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

public class GlossaryEntryDialogTest extends TestCoreGUI {

    private File tmpDir;

    /**
     * Test case to create glossary entry and see a result.
     * @throws Exception
     */
    @Test
    public void testOpenGlossaryEntryDialog() throws Exception {
        // 0. Prepare project folder
        tmpDir = Files.createTempDirectory("omegat-sample-project-").toFile();
        File projSrc = new File("test-acceptance/data/project/");
        FileUtils.copyDirectory(projSrc, tmpDir);
        FileUtils.forceDeleteOnExit(tmpDir);
        // 1. Prepare preference for the test;
        // * OmegaT doesn't show a file list dialog on a project load event.
        // * Glossary sort order preference
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, false);
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_LENGTH, false);
        Preferences.setPreference(Preferences.GLOSSARY_SORT_BY_SRC_LENGTH, false);
        assertFalse(Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true));
        // 2. Open a sample project.
        SwingUtilities.invokeAndWait(() -> ProjectUICommands.projectOpen(tmpDir));
        // Enforce "Edit" menu enabled.
        final JMenuItem menuItem = window.menuItem(BaseMainWindowMenu.EDIT_MENU).target();
        SwingUtilities.invokeAndWait(() -> menuItem.setEnabled(true));
        // 3. Click menu Edit > Create glossary menu items.
        window.menuItem(BaseMainWindowMenu.EDIT_MENU).click();
        window.menuItem(BaseMainWindowMenu.EDIT_CREATE_GLOSSARY_MENUITEM).click();
        // 4. Check Create glossary dialog visible
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).requireVisible();
        // 5. Enter glossary keyword and translation in the field
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).textBox(CreateGlossaryEntry.SOURCE_TEXT_FIELD)
                .enterText("Apertium");
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).textBox(CreateGlossaryEntry.TARGET_TEXT_FIELD)
                .enterText("Translation Engine");
        // 6. Click a OK button
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).button(CreateGlossaryEntry.OK_BUTTON).click();
        // 7. Enforce refresh glossary pane
        GlossaryTextArea glossaryTextArea =
                ((GlossaryTextArea) (window.textBox(GlossaryTextArea.TEXTPANE_NAME).target()));
        glossaryTextArea.refresh();
        // 8. Wait showing a search result during some operation
        window.textBox(GlossaryTextArea.TEXTPANE_NAME).rightClick();
        window.pressKey(KeyEvent.VK_ESCAPE);
        // Check the glossary pane shown in the pane
        assertFalse(glossaryTextArea.getDisplayedEntries().isEmpty());
    }
}
