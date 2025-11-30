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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.omegat.gui.preferences.IPreferencesController;

/**
 * Initial step of the first-time wizard allowing users to:
 * - start with default configuration,
 * - jump to advanced preferences window.
 */
final class StartOptionsStepController implements IPreferencesController {

    private final Runnable onStartDefault;
    private final Runnable onAdvanced;

    private JPanel panel;

    StartOptionsStepController(Runnable onStartDefault, Runnable onAdvanced) {
        this.onStartDefault = onStartDefault;
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

        String desc = FirstTimeConfigurationWizardUtil.getString(
                "start.description",
                "Choose how to begin configuring OmegaT. You can start with the defaults or open the full Preferences window.");
        JTextArea text = new JTextArea(desc);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(text);

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);

        JButton startDefaultBtn = new JButton(FirstTimeConfigurationWizardUtil.getString(
                "button.startDefault", "Start with default configuration"));
        startDefaultBtn.addActionListener(e -> onStartDefault.run());
        buttons.add(startDefaultBtn, gbc);

        gbc.gridy++;
        JButton advancedBtn = new JButton(FirstTimeConfigurationWizardUtil.getString(
                "button.advancedPrefs", "Go to advanced configuration"));
        advancedBtn.addActionListener(e -> onAdvanced.run());
        buttons.add(advancedBtn, gbc);

        panel.add(buttons);
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
