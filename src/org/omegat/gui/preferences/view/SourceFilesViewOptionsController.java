/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Bo Huang
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

import java.awt.BorderLayout;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jspecify.annotations.Nullable;

import org.openide.awt.Mnemonics;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Display options for the Source Files window.
 */
public class SourceFilesViewOptionsController extends BasePreferencesController {

    private @Nullable JPanel panel;
    private @Nullable JCheckBox showProgressCheckBox;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return Objects.requireNonNull(panel);
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_SOURCE_FILES");
    }

    private void initGui() {
        JPanel newPanel = new JPanel(new BorderLayout());
        JCheckBox newShowProgressCheckBox = new JCheckBox();
        newPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        Mnemonics.setLocalizedText(newShowProgressCheckBox,
                OStrings.getString("PREFS_SHOW_PROJECT_FILES_PROGRESS"));
        newPanel.add(newShowProgressCheckBox, BorderLayout.NORTH);
        panel = newPanel;
        showProgressCheckBox = newShowProgressCheckBox;
    }

    @Override
    protected void initFromPrefs() {
        getShowProgressCheckBox().setSelected(Preferences.isPreferenceDefault(
                Preferences.PROJECT_FILES_SHOW_PROGRESS, Preferences.PROJECT_FILES_SHOW_PROGRESS_DEFAULT));
    }

    @Override
    public void restoreDefaults() {
        getShowProgressCheckBox().setSelected(Preferences.PROJECT_FILES_SHOW_PROGRESS_DEFAULT);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_PROGRESS,
                getShowProgressCheckBox().isSelected());
    }

    private JCheckBox getShowProgressCheckBox() {
        if (showProgressCheckBox == null) {
            initGui();
        }
        return Objects.requireNonNull(showProgressCheckBox);
    }
}
