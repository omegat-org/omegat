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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.gui.preferences.view.AppearanceController;
import org.omegat.gui.preferences.view.FontSelectionController;
import org.omegat.gui.preferences.view.GeneralOptionsController;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.util.Preferences;

/**
 * Simple wizard dialog for first-time configuration.
 * Steps:
 * 1) Start options Step
 * 2) Theme (AppearanceController)
 * 3) Font (FontSelectionController)
 * 4) General options (GeneralOptionsController)
 * 5) Plugins (PluginsPreferencesController)
 * 6) Freedoms (GreetingStepController)
 */
public class FirstTimeConfigWizardDialog extends JDialog {

    // Center: steps cards
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // South: nav
    private final JButton backButton = new JButton();
    private final JButton nextButton = new JButton();
    private final JButton finishButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JLabel statusLabel = new JLabel();

    // West: steps list
    private final DefaultListModel<String> stepsModel = new DefaultListModel<>();
    private final JList<String> stepsList = new JList<>(stepsModel);

    // East: right side container (explanations or greeting manual)
    private final CardLayout eastLayout = new CardLayout();
    private final JPanel eastPanel = new JPanel(eastLayout);
    private final JTextArea explanation = new JTextArea();


    private final IPreferencesController[] steps;
    private int index = 0;
    private boolean finished = false;

    public FirstTimeConfigWizardDialog(Frame owner) {
        super(owner, "Welcome to OmegaT", true);
        // Now that the dialog is constructed, set localized title
        setTitle(FirstTimeConfigurationWizardUtil.getString("wizard.title", "Welcome to OmegaT"));

        // Instantiate controllers
        StartOptionsStepController start = new StartOptionsStepController(this::openAdvancedPreferences);
        PluginsPreferencesController plugins = new PluginsPreferencesController();
        AppearanceController appearance = new AppearanceController();
        FontSelectionController font = new FontSelectionController();
        GeneralOptionsController general = new GeneralOptionsController();
        IPreferencesController greetingStep = new GreetingStepController();

        steps = new IPreferencesController[] { start, appearance, font, general, plugins, greetingStep };

        setLayout(new BorderLayout());

        // WEST: Steps list
        for (int i = 0; i < steps.length; i++) {
            String name = steps[i].toString();
            stepsModel.addElement((i + 1) + ". " + name);
        }
        stepsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepsList.setEnabled(false); // display only
        stepsList.setVisibleRowCount(steps.length);
        JScrollPane westScroll = new JScrollPane(stepsList);
        westScroll.setPreferredSize(new Dimension(220, 100));
        westScroll.setBorder(BorderFactory.createTitledBorder(FirstTimeConfigurationWizardUtil.getString("steps.title", "Steps")));
        add(westScroll, BorderLayout.WEST);

        // CENTER: cards with actual step UIs
        for (int i = 0; i < steps.length; i++) {
            JComponent gui = (JComponent) steps[i].getGui();
            JScrollPane scroll = new JScrollPane(gui);
            scroll.setBorder(null);
            cardPanel.add(scroll, "step" + i);
        }
        add(cardPanel, BorderLayout.CENTER);

        // EAST: explanation area or greetings manual
        explanation.setEditable(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane explainScroll = new JScrollPane(explanation);
        explainScroll.setPreferredSize(new Dimension(280, 100));
        explainScroll.setBorder(BorderFactory.createTitledBorder(FirstTimeConfigurationWizardUtil.getString("explain.title", "Explanation")));
        eastPanel.add(explainScroll, "explain");

        add(eastPanel, BorderLayout.EAST);

        // NAVIGATION
        configureActions();
        JPanel nav = new JPanel();
        nav.add(backButton);
        nav.add(nextButton);
        nav.add(finishButton);
        nav.add(cancelButton);
        // South container with buttons and status bar
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(nav, BorderLayout.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // Header
        JLabel header = new JLabel(FirstTimeConfigurationWizardUtil.getString("header.text", "Let's set up a few preferences"));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(header, BorderLayout.NORTH);

        setPreferredSize(new Dimension(980, 560));
        pack();
        setLocationRelativeTo(owner);
        updateState();
    }

    private void configureActions() {
        backButton.setAction(new AbstractAction(FirstTimeConfigurationWizardUtil.getString("button.back", "Back")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index > 0) {
                    index--;
                    updateState();
                }
            }
        });
        nextButton.setAction(new AbstractAction(FirstTimeConfigurationWizardUtil.getString("button.next", "Next")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < steps.length - 1) {
                    if (!steps[index].validate()) {
                        return; // stay until valid
                    }
                    boolean restartRequired = steps[index].isRestartRequired();
                    index++;
                    updateState();
                    if (restartRequired) {
                        statusLabel.setText(FirstTimeConfigurationWizardUtil.getString("status.restartRequired", "Changes on the previous step require restarting OmegaT."));
                    } else {
                        statusLabel.setText("");
                    }
                }
            }
        });
        finishButton.setAction(new AbstractAction(FirstTimeConfigurationWizardUtil.getString("button.finish", "Finish")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate all steps
                for (int i = 0; i < steps.length; i++) {
                    IPreferencesController c = steps[i];
                    if (!c.validate()) {
                        index = i;
                        updateState();
                        return;
                    }
                }
                for (IPreferencesController c : steps) {
                    c.persist();
                }
                // Mark wizard as done so it won't be shown again automatically
                Preferences.setPreference(Preferences.FIRST_TIME_WIZARD_DONE, Boolean.TRUE.toString());
                finished = true;
                dispose();
            }
        });
        // Replace the traditional "Cancel" with "Start with default" per requirements.
        // This triggers the same flow as the first-page "Start with default" action.
        cancelButton.setAction(new AbstractAction(
                FirstTimeConfigurationWizardUtil.getString("button.startDefault", "Start with default configuration")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishWithDefaults();
            }
        });
    }

    private void finishWithDefaults() {
        // No changes necessary: simply finish the wizard.
        Preferences.setPreference(Preferences.FIRST_TIME_WIZARD_DONE, Boolean.TRUE.toString());
        finished = true;
        dispose();
    }

    private void openAdvancedPreferences() {
        // Close wizard and open full Preferences window for advanced users
        // Mark wizard as done: user opted to configure via full preferences
        Preferences.setPreference(Preferences.FIRST_TIME_WIZARD_DONE, Boolean.TRUE.toString());
        dispose();
        PreferencesWindowController pwc = new PreferencesWindowController();
        java.awt.Window owner = getOwner();
        // Open at Appearance as a sensible default category
        pwc.show(owner, org.omegat.gui.preferences.view.AppearanceController.class);
    }

    // Removed jumpToPluginsStep: plugin review button eliminated from first page

    private void updateState() {
        cardLayout.show(cardPanel, "step" + index);
        stepsList.setSelectedIndex(index);
        backButton.setEnabled(index > 0);
        nextButton.setEnabled(index < steps.length - 1);
        finishButton.setEnabled(index == steps.length - 1);
        // Clear any previous status message when changing steps
        statusLabel.setText("");
        // Update explanation text based on current step
        String explanation;
        switch (index) {
        case 0:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.start", "");
            break;
        case 1:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.plugins", "");
            break;
        case 2:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.appearance", "");
            break;
        case 3:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.font", "");
            break;
        case 4:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.general", "");
            break;
        case 5:
            explanation = FirstTimeConfigurationWizardUtil.getString("explain.greeting", "");
            break;
        default:
            explanation = ""; // No explanation
            break;
        }
        boolean hasExplanation = explanation != null && !explanation.trim().isEmpty();

        // Show or hide the explanation panel depending on availability
        if (hasExplanation) {
            this.explanation.setText(explanation);
            this.explanation.setCaretPosition(0);
            eastLayout.show(eastPanel, "explain");
            if (!eastPanel.isVisible()) {
                eastPanel.setVisible(true);
            }
        } else {
            // Hide the panel entirely when there's no explanation for this step
            this.explanation.setText("");
            if (eastPanel.isVisible()) {
                eastPanel.setVisible(false);
            }
        }
        // Refresh layout in case visibility changed
        revalidate();
        repaint();
    }

    public boolean isFinished() {
        return finished;
    }
    
    public boolean isRestartRequired() {
        return Arrays.stream(steps).anyMatch(IPreferencesController::isRestartRequired);
    }
}
