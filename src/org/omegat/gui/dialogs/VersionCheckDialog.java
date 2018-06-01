/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Desktop;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.VersionChecker;
import org.omegat.util.gui.StaticUIUtils;

public class VersionCheckDialog {

    private static final String DOWNLOAD_URL = "https://omegat.org/download";

    public static void checkAndShowResultAsync(Window parent) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return VersionChecker.getInstance().isUpToDate();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(parent, OStrings.getString("VERSION_CHECK_UP_TO_DATE"));
                    } else {
                        new VersionCheckDialog(VersionChecker.getInstance().getRemoteVersion()).show(parent);
                    }
                } catch (Exception e) {
                    Log.log(e);
                    JOptionPane.showMessageDialog(parent, e.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private final String newVersion;

    public VersionCheckDialog(String newVersion) {
        this.newVersion = newVersion;
    }

    public void show(Window parent) {
        JDialog dialog = new JDialog(parent);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);
        StaticUIUtils.setEscapeClosable(dialog);

        VersionCheckPanel panel = new VersionCheckPanel();
        panel.message.setText(OStrings.getString("VERSION_CHECK_OUT_OF_DATE", newVersion, OStrings.getSimpleVersion()));
        panel.autoCheckCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.VERSION_CHECK_AUTOMATIC,
                Preferences.VERSION_CHECK_AUTOMATIC_DEFAULT));
        panel.autoCheckCheckBox.addActionListener(e -> Preferences.setPreference(Preferences.VERSION_CHECK_AUTOMATIC,
                panel.autoCheckCheckBox.isSelected()));
        panel.goToDownloadsButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(DOWNLOAD_URL));
                StaticUIUtils.closeWindowByEvent(dialog);
            } catch (IOException ex) {
                Log.log(ex);
                JOptionPane.showMessageDialog(parent, ex.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));

        dialog.getContentPane().add(panel);
        dialog.getRootPane().setDefaultButton(panel.goToDownloadsButton);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

}
