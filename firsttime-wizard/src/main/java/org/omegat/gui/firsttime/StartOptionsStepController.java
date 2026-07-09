/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2025 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.gui.firsttime;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.omegat.gui.preferences.IPreferencesController;

/**
 * Initial step of the first-time wizard allowing users to:
 * - start with default configuration,
 * - jump to advanced preferences window.
 */
final class StartOptionsStepController implements IPreferencesController {

    private final Runnable onAdvanced;

    private JPanel panel;

    StartOptionsStepController(Runnable onAdvanced) {
        this.onAdvanced = onAdvanced;
    }

    @Override
    public void addFurtherActionListener(FurtherActionListener listener) {
        // no-op
    }

    @Override
    public void removeFurtherActionListener(FurtherActionListener listener) {
        // no-op
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isReloadRequired() {
        return false;
    }

    @Override
    public String toString() {
        return FirstTimeConfigurationWizardUtil.getString("step.start", "Get started");
    }

    @Override
    public Component getGui() {
        if (panel == null) {
            initGui();
        }
        return panel;
    }

    private void initGui() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel normalSteps = new JLabel(FirstTimeConfigurationWizardUtil.getString("label.normalSteps",
                "Click next button and customize settings."));
        panel.add(normalSteps);
        panel.add(Box.createVerticalStrut(8));
        JPanel advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        JLabel advancedLabel = new JLabel(FirstTimeConfigurationWizardUtil.getString(
                "label.advancedPrefs", "Go directly to regular preferences dialog and dismiss this wizard"));
        advancedPanel.add(advancedLabel);
        JButton advancedBtn = new JButton(FirstTimeConfigurationWizardUtil.getString(
                "button.advancedPrefs", "Go to advanced configuration"));
        advancedBtn.addActionListener(e -> onAdvanced.run());
        advancedPanel.add(advancedBtn);
        panel.add(advancedPanel);
    }

    @Override
    public void persist() {
        // nothing to persist on this step
    }

    @Override
    public void undoChanges() {
        // no-op
    }

    @Override
    public void restoreDefaults() {
        // no-op
    }

    @Override
    public boolean canRestoreDefaults() {
        return false;
    }
}
