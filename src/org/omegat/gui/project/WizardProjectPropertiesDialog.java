/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.step.ContributorStep;
import org.omegat.gui.project.step.DirectoriesStep;
import org.omegat.gui.project.step.ExportAndCommandStep;
import org.omegat.gui.project.step.LanguagesAndOptionsStep;
import org.omegat.util.OStrings;
import org.omegat.gui.project.step.Step;
import org.openide.awt.Mnemonics;

@SuppressWarnings("serial")
class WizardProjectPropertiesDialog extends JDialog {

    private final ProjectProperties props;
    private final ProjectConfigMode mode;
    private boolean cancelled = true;

    private final java.util.List<org.omegat.gui.project.step.Step> steps = new java.util.ArrayList<>();
    private int current = 0;

    private final JPanel cards = new JPanel(new CardLayout());
    private final JPanel left = new JPanel();

    private final JButton backBtn = new JButton();
    private final JButton nextBtn = new JButton();
    private final JButton finishBtn = new JButton();
    private final JButton cancelBtn = new JButton();

    WizardProjectPropertiesDialog(Frame parent, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, true);
        this.props = props;
        this.mode = mode;
        setTitle(OStrings.getString("PP_TITLE"));
        Mnemonics.setLocalizedText(backBtn, OStrings.getString("BUTTON_BACK"));
        Mnemonics.setLocalizedText(nextBtn, OStrings.getString("BUTTON_NEXT"));
        Mnemonics.setLocalizedText(finishBtn, OStrings.getString("BUTTON_FINISH"));
        Mnemonics.setLocalizedText(cancelBtn, OStrings.getString("BUTTON_CANCEL"));
        buildSteps();
        buildUI();
        pack();
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(parent);
    }

    private void buildSteps() {
        steps.add(new LanguagesAndOptionsStep(mode));
        steps.add(new DirectoriesStep(mode));
        steps.add(new ExportAndCommandStep(mode));
        // Load contributions
        for (ProjectPropertiesContributor c : ServiceLoader.load(ProjectPropertiesContributor.class)) {
            steps.add(new ContributorStep(c));
        }
        // Initialize
        steps.forEach(s -> s.onLoad(props));
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());

        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            JLabel lbl = new JLabel((i + 1) + ". " + s.getTitle());
            lbl.setName("wizardStepLabel" + i);
            left.add(lbl);
            left.add(Box.createVerticalStrut(6));
            cards.add(new JScrollPane(s.getComponent()), "step" + i);
        }

        content.add(left, BorderLayout.WEST);
        content.add(cards, BorderLayout.CENTER);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backBtn.addActionListener(this::onBack);
        nextBtn.addActionListener(this::onNext);
        finishBtn.addActionListener(this::onFinish);
        cancelBtn.addActionListener(e -> {
            cancelled = true;
            setVisible(false);
        });
        nav.add(backBtn);
        nav.add(nextBtn);
        nav.add(cancelBtn);
        nav.add(finishBtn);
        content.add(nav, BorderLayout.SOUTH);

        setContentPane(content);
        updateNav();
    }

    private void onBack(ActionEvent e) {
        if (current > 0) {
            current--;
            showCurrent();
        }
    }

    private void onNext(ActionEvent e) {
        Step s = steps.get(current);
        String err = s.validateInput();
        if (err != null) {
            // Keep it simple: do not show a dialog, just ignore move when
            // invalid.
            return;
        }
        if (current < steps.size() - 1) {
            current++;
            showCurrent();
        }
    }

    private void onFinish(ActionEvent e) {
        for (Step s : steps) {
            String err = s.validateInput();
            if (err != null) {
                return;
            }
        }
        steps.forEach(s -> s.onSave(props));
        cancelled = false;
        setVisible(false);
    }

    private void showCurrent() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, "step" + current);
        updateNav();
    }

    private void updateNav() {
        backBtn.setEnabled(current > 0);
        nextBtn.setEnabled(current < steps.size() - 1);
        finishBtn.setEnabled(true);
        // Highlight current step label
        for (int i = 0; i < left.getComponentCount(); i += 2) {
            if (left.getComponent(i) instanceof JLabel) {
                JLabel l = (JLabel) left.getComponent(i);
                l.setEnabled((i / 2) <= current);
            }
        }
    }

    boolean isCancelled() {
        return cancelled;
    }
}
