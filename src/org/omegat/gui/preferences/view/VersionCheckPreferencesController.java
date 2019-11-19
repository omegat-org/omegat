/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences.view;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.omegat.gui.dialogs.VersionCheckDialog;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Aaron Madlon-Kay
 */
public class VersionCheckPreferencesController extends BasePreferencesController {

    private VersionCheckPreferencesPanel panel;

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
        return OStrings.getString("PREFS_TITLE_VERSION_CHECK");
    }

    private void initGui() {
        panel = new VersionCheckPreferencesPanel();
        panel.checkNowButton.addActionListener(
                e -> VersionCheckDialog.checkAndShowResultAsync(SwingUtilities.getWindowAncestor(panel)));
        panel.currentVersionLabel
                .setText(OStrings.getString("PREFS_VERSION_CURRENT_VERSION", OStrings.getSimpleVersion()));
        panel.updateChannelLabel.setText(OStrings.getString("PREFS_VERSION_UPDATE_CHANNEL",
                OStrings.IS_BETA ? OStrings.getString("PREFS_VERSION_CHANNEL_LATEST")
                        : OStrings.getString("PREFS_VERSION_CHANNEL_STABLE")));
    }

    @Override
    protected void initFromPrefs() {
        panel.autoCheckCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.VERSION_CHECK_AUTOMATIC, true));
    }

    @Override
    public void restoreDefaults() {
        panel.autoCheckCheckBox.setSelected(true);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.VERSION_CHECK_AUTOMATIC, panel.autoCheckCheckBox.isSelected());
    }
}
