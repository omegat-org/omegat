/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import java.io.File;

import javax.swing.JComponent;

import org.omegat.core.Core;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.DesktopWrapper;

/**
 * @author Aaron Madlon-Kay
 */
public class GeneralOptionsController extends BasePreferencesController {

    private GeneralOptionsPanel panel;

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
        return OStrings.getString("PREFS_TITLE_GENERAL");
    }

    private void initGui() {
        panel = new GeneralOptionsPanel();
        panel.accessConfigDirButton.addActionListener(e -> openFile(new File(StaticUtils.getConfigDir())));
    }

    private void openFile(File path) {
        try {
            path = path.getCanonicalFile(); // Normalize file name in case it is displayed
        } catch (Exception ex) {
            // Ignore
        }
        if (!path.exists()) {
            Core.getMainWindow().showStatusMessageRB("LFC_ERROR_FILE_DOESNT_EXIST", path);
            return;
        }
        try {
            DesktopWrapper.open(path);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "RPF_ERROR");
            Core.getMainWindow().displayErrorRB(ex, "RPF_ERROR");
        }
    }

    @Override
    protected void initFromPrefs() {
        panel.tabAdvanceCheckBox.setSelected(Core.getEditor().getSettings().isUseTabForAdvance());
        panel.confirmQuitCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.ALWAYS_CONFIRM_QUIT, true));
    }

    @Override
    public void restoreDefaults() {
        panel.tabAdvanceCheckBox.setSelected(false);
        panel.confirmQuitCheckBox.setSelected(true);
    }

    @Override
    public void persist() {
        Core.getEditor().getSettings().setUseTabForAdvance(panel.tabAdvanceCheckBox.isSelected());
        Preferences.setPreference(Preferences.ALWAYS_CONFIRM_QUIT, panel.confirmQuitCheckBox.isSelected());
    }
}
