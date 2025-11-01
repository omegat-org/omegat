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
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.step.DirectoriesAndExportTMStep;
import org.omegat.gui.project.step.LanguagesAndOptionsStep;
import org.omegat.gui.project.step.Step;
import org.omegat.util.OStrings;

/**
 * Minimal, streamlined dialog for capturing core Project Properties. Focuses on
 * source/target languages for quick project creation.
 */
@SuppressWarnings("serial")
class SingleProjectPropertiesDialog extends AbstractProjectPropertiesDialog {

    private final JPanel cards = new JPanel(new CardLayout());
    private final JPanel left = new JPanel();
    private Step step;

    SingleProjectPropertiesDialog(Frame parent, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, true, props, mode);
        updateUIText();
        setName(DIALOG_NAME);
        buildUI(mode);
        pack();
        setMinimumSize(new Dimension(420, 180));
        setLocationRelativeTo(parent);
    }

    private void buildUI(ProjectConfigMode mode) {
        if (Objects.requireNonNull(mode) == ProjectConfigMode.RESOLVE_DIRS) {
            step = new DirectoriesAndExportTMStep(mode);
        } else {
            step = new LanguagesAndOptionsStep(mode);
        }
        step.onLoad(props);

        JPanel content = new JPanel(new BorderLayout());
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lbl = new JLabel(step.getTitle());
        lbl.setName("wizardStepLabel");
        left.add(lbl);
        cards.add(new JScrollPane(step.getComponent()), "step");
        content.add(left, BorderLayout.WEST);
        content.add(cards, BorderLayout.CENTER);

        // Buttons
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton(OStrings.getString("BUTTON_OK"));
        ok.setName(OK_BUTTON_NAME);
        ok.addActionListener(this::onOk);
        JButton cancel = new JButton(OStrings.getString("BUTTON_CANCEL"));
        cancel.setName(CANCEL_BUTTON_NAME);
        cancel.addActionListener(e -> {
            cancelled = true;
            setVisible(false);
        });
        nav.add(cancel);
        nav.add(ok);

        content.add(nav, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void onOk(ActionEvent e) {
        String err = step.validateInput();
        if (err != null) {
            return;
        }
        step.onSave(props);
        cancelled = false;
        setVisible(false);
    }

    public static final String DIALOG_NAME = "single_project_properties_dialog";
    public static final String OK_BUTTON_NAME = "single_project_properties_ok_button";
    public static final String CANCEL_BUTTON_NAME = "single_project_properties_cancel_button";

}
