package org.omegat.gui.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.main.TestCoreGUI;

public class GlossaryEntryDialogTest extends TestCoreGUI {

    @Override
    protected void onStartUp() {
        // load sample project
        File proj = new File("test-acceptance/data/project/");
        try {
            SwingUtilities.invokeAndWait(() -> {
                ProjectUICommands.projectOpen(proj);
                window.menuItem(BaseMainWindowMenu.EDIT_MENU).target().setEnabled(true);
            });
        } catch (InterruptedException | InvocationTargetException ignored) {
            Assert.fail();
        }
    }

    @Test
    public void testOpenGlossaryEntryDialog() {
        window.menuItem(BaseMainWindowMenu.EDIT_MENU).click();
        window.menuItem(BaseMainWindowMenu.EDIT_CREATE_GLOSSARY_MENUITEM).click();
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).requireVisible();
        window.dialog(CreateGlossaryEntry.DIALOG_NAME).requireFocused();
    }
}
