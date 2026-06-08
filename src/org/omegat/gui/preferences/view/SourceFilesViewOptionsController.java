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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openide.awt.Mnemonics;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Display options for the Source Files window.
 */
public class SourceFilesViewOptionsController extends BasePreferencesController {

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JCheckBox showProgressCheckBox = new JCheckBox();
    private final JCheckBox showProgressBarsCheckBox = new JCheckBox();

    public SourceFilesViewOptionsController() {
        initGui();
        initFromPrefs();
    }

    @Override
    public JComponent getGui() {
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_SOURCE_FILES");
    }

    private void initGui() {
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));

        Mnemonics.setLocalizedText(showProgressCheckBox,
                OStrings.getString("PREFS_SHOW_PROJECT_FILES_PROGRESS"));
        Mnemonics.setLocalizedText(showProgressBarsCheckBox,
                OStrings.getString("PREFS_SHOW_PROJECT_FILES_PROGRESS_BARS"));

        showProgressCheckBox.addActionListener(e -> updateEnabledness());

        checkBoxPanel.add(showProgressCheckBox);
        checkBoxPanel.add(showProgressBarsCheckBox);
        panel.add(checkBoxPanel, BorderLayout.NORTH);
    }

    @Override
    protected void initFromPrefs() {
        showProgressCheckBox.setSelected(Preferences.isPreferenceDefault(
                Preferences.PROJECT_FILES_SHOW_PROGRESS, Preferences.PROJECT_FILES_SHOW_PROGRESS_DEFAULT));
        showProgressBarsCheckBox.setSelected(Preferences.isPreferenceDefault(
                Preferences.PROJECT_FILES_SHOW_PROGRESS_BARS,
                Preferences.PROJECT_FILES_SHOW_PROGRESS_BARS_DEFAULT));
        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        showProgressCheckBox.setSelected(Preferences.PROJECT_FILES_SHOW_PROGRESS_DEFAULT);
        showProgressBarsCheckBox.setSelected(Preferences.PROJECT_FILES_SHOW_PROGRESS_BARS_DEFAULT);
        updateEnabledness();
    }

    private void updateEnabledness() {
        if (!showProgressCheckBox.isSelected()) {
            showProgressBarsCheckBox.setSelected(false);
        }
        showProgressBarsCheckBox.setEnabled(showProgressCheckBox.isSelected());
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_PROGRESS, showProgressCheckBox.isSelected());
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_PROGRESS_BARS,
                showProgressCheckBox.isSelected() && showProgressBarsCheckBox.isSelected());
    }
}
