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
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
    private final JComboBox<ProgressDisplayMode> progressDisplayComboBox = new JComboBox<>(
            ProgressDisplayMode.values());

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

        JPanel progressDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        JLabel progressDisplayLabel = new JLabel();
        Mnemonics.setLocalizedText(progressDisplayLabel,
                OStrings.getString("PREFS_PROJECT_FILES_PROGRESS_DISPLAY"));
        progressDisplayLabel.setLabelFor(progressDisplayComboBox);

        progressDisplayPanel.add(progressDisplayLabel);
        progressDisplayPanel.add(progressDisplayComboBox);
        panel.add(progressDisplayPanel, BorderLayout.NORTH);
    }

    @Override
    protected void initFromPrefs() {
        progressDisplayComboBox.setSelectedItem(ProgressDisplayMode.fromPreferenceValue(
                Preferences.getPreferenceDefault(Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE,
                        Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE_DEFAULT)));
    }

    @Override
    public void restoreDefaults() {
        progressDisplayComboBox.setSelectedItem(ProgressDisplayMode.OFF);
    }

    @Override
    public void persist() {
        ProgressDisplayMode selected = (ProgressDisplayMode) progressDisplayComboBox.getSelectedItem();
        Preferences.setPreference(Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE,
                selected == null ? Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE_DEFAULT : selected.value);
    }

    private enum ProgressDisplayMode {
        OFF(Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE_OFF,
                "PREFS_PROJECT_FILES_PROGRESS_DISPLAY_OFF"), PERCENTAGE(
                        Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE_PERCENTAGE,
                        "PREFS_PROJECT_FILES_PROGRESS_DISPLAY_PERCENTAGE"), PERCENTAGE_BARS(
                                Preferences.PROJECT_FILES_PROGRESS_DISPLAY_MODE_PERCENTAGE_BARS,
                                "PREFS_PROJECT_FILES_PROGRESS_DISPLAY_PERCENTAGE_BARS");

        private final String value;
        private final String labelKey;

        ProgressDisplayMode(String value, String labelKey) {
            this.value = value;
            this.labelKey = labelKey;
        }

        private static ProgressDisplayMode fromPreferenceValue(String value) {
            for (ProgressDisplayMode mode : values()) {
                if (mode.value.equals(value)) {
                    return mode;
                }
            }
            return OFF;
        }

        @Override
        public String toString() {
            return OStrings.getString(labelKey);
        }
    }
}
