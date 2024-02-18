package org.omegat.gui.main;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.LogDialog;

class TestMainWindowMenuHandler extends BaseMainWindowMenuHandler {
    JFrame mw;

    public static boolean quited = false;

    public TestMainWindowMenuHandler(JFrame mw) {
        this.mw = mw;
    }

    @Override
    public void projectExitMenuItemActionPerformed() {
        quited = true;
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
