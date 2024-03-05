package org.omegat.gui.main;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.core.search.SearchMode;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.LogDialog;
import org.omegat.gui.editor.EditorUtils;
import org.omegat.gui.filelist.IProjectFilesList;
import org.omegat.gui.search.SearchWindowController;
import org.omegat.util.StringUtil;

public class TestMainWindowMenuHandler extends BaseMainWindowMenuHandler {

    IMainWindow mainWindow;

    public TestMainWindowMenuHandler(IMainWindow mw) {
        this.mainWindow = mw;
    }

    /**
     * Create a new project.
     */
    public void projectNewMenuItemActionPerformed() {
        ProjectUICommands.projectCreate();
    }

    @Override
    public void projectExitMenuItemActionPerformed() {
        mainWindow.getApplicationFrame().setVisible(false);
        mainWindow.getApplicationFrame().setEnabled(false);
    }

    public void viewFileListMenuItemActionPerformed() {
        IProjectFilesList projWin = Core.getProjectFilesList();
        if (projWin == null) {
            return;
        }
        projWin.setActive(!projWin.isActive());
    }

    public void editCreateGlossaryEntryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getGlossary().showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
    }

    public void editFindInProjectMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        SearchWindowController search = new SearchWindowController(SearchMode.SEARCH);
        ((TestCoreGUI.TestMainWindow)mainWindow).addSearchWindow(search);

        search.makeVisible(getTrimmedSelectedTextInMainWindow());
    }

    private String getTrimmedSelectedTextInMainWindow() {
        String selection = null;
        Component component = mainWindow.getApplicationFrame().getMostRecentFocusOwner();
        if (component instanceof JTextComponent) {
            selection = ((JTextComponent) component).getSelectedText();
            if (!StringUtil.isEmpty(selection)) {
                selection = EditorUtils.removeDirectionChars(selection);
                selection = selection.trim();
            }
        }
        return selection;
    }

    /**
     * Show log
     */
    public void helpLogMenuItemActionPerformed() {
        new LogDialog(mainWindow.getApplicationFrame()).setVisible(true);
    }

    @Override
    public void helpAboutMenuItemActionPerformed() {
        JDialog aboutDialog = new AboutDialog(mainWindow.getApplicationFrame());
        aboutDialog.setVisible(true);
    }
}
