package org.omegat.gui.main;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.LogDialog;
import org.omegat.gui.filelist.IProjectFilesList;

public class TestMainWindowMenuHandler extends BaseMainWindowMenuHandler {

    JFrame mw;

    public TestMainWindowMenuHandler(IMainWindow mw) {
        this.mw = mw.getApplicationFrame();
    }

    /**
     * Create a new project.
     */
    public void projectNewMenuItemActionPerformed() {
        ProjectUICommands.projectCreate();
    }

    @Override
    public void projectExitMenuItemActionPerformed() {
        mw.setVisible(false);
        mw.setEnabled(false);
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

    /**
     * Show log
     */
    public void helpLogMenuItemActionPerformed() {
        new LogDialog(mw).setVisible(true);
    }

    @Override
    public void helpAboutMenuItemActionPerformed() {
        JDialog aboutDialog = new AboutDialog(mw);
        aboutDialog.setVisible(true);
    }
}
