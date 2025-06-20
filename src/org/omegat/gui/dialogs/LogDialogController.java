/*******************************************************************************
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
 ******************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.omegat.util.Log;
import org.omegat.util.Preferences;
import org.omegat.util.gui.OSXIntegration;

/**
 * @author miurahr
 */
public class LogDialogController {
    public static final String DIALOG_NAME = "log_dialog";
    public static final String OK_BUTTON_NAME = "log_dialog_ok_button";
    public static final String SAVE_AS_BUTTON_NAME = "log_dialog_save_as_button";
    public static final String LOG_TEXTPANE_NAME = "log_dialog_log_textpane";
    private final LogDialog logDialog;

    /**
     * Create LogDialog dialog.
     * 
     * @param parent
     *            parent frame of the dialog.
     */
    public LogDialogController(JFrame parent) {
        logDialog = new LogDialog(parent);
    }

    /**
     * Utility function to show LogDialog dialog and load current session log.
     * 
     * @param parent
     *            parent frame of the dialog.
     */
    public static void show(JFrame parent) {
        new LogDialogController(parent).show();
    }

    /**
     * Load the current session log on the LogDialog and show it.
     */
    public void show() {
        logDialog.setName(DIALOG_NAME);
        logDialog.okButton.setName(OK_BUTTON_NAME);
        logDialog.saveAsButton.setName(SAVE_AS_BUTTON_NAME);
        logDialog.logTextPane.setName(LOG_TEXTPANE_NAME);
        logDialog.saveAsButton.addActionListener(this::saveAsButtonActionPerformed);
        loadCurrentSessionLog(logDialog.logTextPane);
        logDialog.setVisible(true);
    }

    private void loadCurrentSessionLog(JTextPane logTextPane) {
        File logLocation = new File(Log.getLogFilePath());
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return loadCurrentSessionLogFromFile(logLocation);
            }

            protected void done() {
                try {
                    logTextPane.setText(get());
                } catch (Exception e) {
                    Log.log(e);
                }
                OSXIntegration.setProxyIcon(logDialog.getRootPane(), logLocation);
            };
        }.execute();
    }

    /**
     * Load current session log from log file.
     * 
     * @param logLocation
     *            log file path to retrieve log messages.
     * @return Log messages as String.
     */
    protected String loadCurrentSessionLogFromFile(File logLocation) {
        final StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(logLocation.toPath())) {
            lines.forEachOrdered(s -> sb.append(s).append("\n"));
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    private void saveAsButtonActionPerformed(ActionEvent evt) {
        File outFile = showChooserLogFile();
        if (outFile == null) {
            return;
        }
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(outFile.toPath())) {
                logDialog.logTextPane.write(writer);
            }
        } catch (IOException ex) {
            Log.log(ex);
        }
        Preferences.setPreference(Preferences.CURRENT_FOLDER, outFile.getParent());
    }

    private File showChooserLogFile() {
        String curDir = Preferences.getPreferenceDefault(Preferences.CURRENT_FOLDER,
                System.getProperty("user.home"));
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(curDir, Log.getLogFileName()));
        if (chooser.showSaveDialog(logDialog) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File outFile = chooser.getSelectedFile();
        if (outFile.exists() && !FileCollisionDialog.promptToReplace(logDialog, outFile.getName())) {
            return null;
        }
        return outFile;
    }
}
