/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences.view;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.CredentialsManager;
import org.omegat.util.OStrings;

/**
 * @author Aaron Madlon-Kay
 */
public class SecureStoreController extends BasePreferencesController {

    private SecureStorePanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_SECURE_STORE");
    }

    private void initGui() {
        panel = new SecureStorePanel();
        panel.resetPasswordButton.addActionListener(e -> resetMasterPassword());
        panel.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    updateMasterPasswordStatus();
                }
            }
        });
    }

    private void resetMasterPassword() {
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(panel),
                OStrings.getString("PREFS_SECURE_STORAGE_RESET_MASTER_PASSWORD_MESSAGE"),
                OStrings.getString("PREFS_SECURE_STORAGE_RESET_MASTER_PASSWORD_TITLE"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
            CredentialsManager.getInstance().clearMasterPassword();
            updateMasterPasswordStatus();
        }
    }

    @Override
    protected void initFromPrefs() {
        updateMasterPasswordStatus();
    }

    private void updateMasterPasswordStatus() {
        boolean isSet = CredentialsManager.getInstance().isMasterPasswordSet();
        boolean isStored = CredentialsManager.getInstance().isMasterPasswordStored();
        String status;
        if (isSet && isStored) {
            status = OStrings.getString("PREFS_SECURE_STORAGE_MASTER_PASSWORD_SET_STORED");
        } else if (isSet) {
            status = OStrings.getString("PREFS_SECURE_STORAGE_MASTER_PASSWORD_SET");
        } else {
            status = OStrings.getString("PREFS_SECURE_STORAGE_MASTER_PASSWORD_NOT_SET");
        }
        panel.masterPasswordStatusLabel.setText(status);
        panel.resetPasswordButton.setEnabled(isSet);
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    public void persist() {
    }

    @Override
    public boolean canRestoreDefaults() {
        return false;
    }
}
