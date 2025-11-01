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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Component;
import java.awt.Container;

import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.step.ContributorStep;
import org.omegat.gui.project.step.DirectoriesAndExportTMStep;
import org.omegat.gui.project.step.ExternalCommandStep;
import org.omegat.util.Preferences;
import org.omegat.gui.project.step.LanguagesAndOptionsStep;
import org.omegat.gui.project.step.SegmentationStep;
import org.omegat.gui.project.step.FilterDefinitionStep;
import org.omegat.gui.project.step.ExternalFinderStep;
import org.omegat.gui.project.step.RepositoriesMappingStep;
import org.omegat.util.OStrings;
import org.omegat.gui.project.step.Step;
import org.omegat.gui.project.step.ProjectFolderStep;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.Mnemonics;

@SuppressWarnings("serial")
class WizardProjectPropertiesDialog extends AbstractProjectPropertiesDialog {

    private final List<Step> steps = new ArrayList<>();
    private int current = 0;

    private final JPanel cards = new JPanel(new CardLayout());
    private final JPanel left = new JPanel();

    private final JButton backBtn = new JButton();
    private final JButton nextBtn = new JButton();
    private final JButton finishBtn = new JButton();
    private final JButton cancelBtn = new JButton();

    private @Nullable ProjectProperties resultProps;
    private boolean remainingStepsBuilt = false;

    @Nullable
    private LanguagesAndOptionsStep languagesAndOptionsStep;
    @Nullable
    private SegmentationStep segmentationStep;
    @Nullable
    private FilterDefinitionStep filterDefinitionStep;
    @Nullable
    private ProjectFolderStep folderStep;

    WizardProjectPropertiesDialog(Frame parent, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, true, props, mode);
        updateUIText();
        setName(DIALOG_NAME);
        buildSteps();
        buildUI();
        pack();
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(parent);
    }

    private void buildSteps() {
        if (mode == ProjectConfigMode.NEW_PROJECT) {
            // First step: choose project folder. Build all remaining steps now but delay initialization until folder selected.
            folderStep = new ProjectFolderStep();
            steps.add(folderStep);
            buildRemainingSteps();
        } else {
            folderStep = null;
            buildRemainingSteps();
            initializeRemainingSteps(props);
            remainingStepsBuilt = true;
        }
    }

    private void buildRemainingSteps() {
        languagesAndOptionsStep = new LanguagesAndOptionsStep(mode);
        steps.add(languagesAndOptionsStep);
        steps.add(new DirectoriesAndExportTMStep(mode));
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            steps.add(new ExternalCommandStep(mode));
        }
        // Load contributions
        for (ProjectPropertiesContributor c : ServiceLoader.load(ProjectPropertiesContributor.class)) {
            steps.add(new ContributorStep(c));
        }
        filterDefinitionStep = new FilterDefinitionStep(mode);
        steps.add(filterDefinitionStep);
        steps.add(new RepositoriesMappingStep(mode));
        segmentationStep = new SegmentationStep(mode);
        steps.add(segmentationStep);
        steps.add(new ExternalFinderStep(mode));
    }

    private void initializeRemainingSteps(ProjectProperties loadProps) {
        // Initialize UI state for all steps
        steps.forEach(s -> s.onLoad(loadProps));
        // Synchronize step enablement with checkboxes in languages step
        final LanguagesAndOptionsStep lang = this.languagesAndOptionsStep;
        final SegmentationStep seg = this.segmentationStep;
        final FilterDefinitionStep filt = this.filterDefinitionStep;
        if (lang != null) {
            boolean segEnabled = lang.isProjectSpecificSegmentationSelected();
            if (seg != null) {
                seg.setProjectSpecificSegmentationEnabled(segEnabled);
            }
            boolean filtersEnabled = lang.isProjectSpecificFiltersSelected();
            if (filt != null) {
                filt.setProjectSpecificFiltersEnabled(filtersEnabled);
            }
            if (seg != null) {
                lang.addProjectSpecificSegmentationListener(e -> {
                    seg.setProjectSpecificSegmentationEnabled(lang.isProjectSpecificSegmentationSelected());
                    updateNav();
                });
            }
            if (filt != null) {
                lang.addProjectSpecificFiltersListener(e -> {
                    filt.setProjectSpecificFiltersEnabled(lang.isProjectSpecificFiltersSelected());
                    updateNav();
                });
            }
        }
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
        Mnemonics.setLocalizedText(backBtn, OStrings.getString("BUTTON_BACK"));
        Mnemonics.setLocalizedText(nextBtn, OStrings.getString("BUTTON_NEXT"));
        Mnemonics.setLocalizedText(finishBtn, OStrings.getString("BUTTON_FINISH"));
        Mnemonics.setLocalizedText(cancelBtn, OStrings.getString("BUTTON_CANCEL"));
        backBtn.addActionListener(this::onBack);
        nextBtn.addActionListener(this::onNext);
        finishBtn.addActionListener(this::onFinish);
        finishBtn.setName(OK_BUTTON_NAME);
        cancelBtn.addActionListener(e -> {
            cancelled = true;
            setVisible(false);
        });
        cancelBtn.setName(CANCEL_BUTTON_NAME);
        nav.add(backBtn);
        nav.add(nextBtn);
        nav.add(cancelBtn);
        nav.add(finishBtn);
        content.add(nav, BorderLayout.SOUTH);

        setContentPane(content);
        if (mode == ProjectConfigMode.NEW_PROJECT && folderStep != null) {
            wireFolderFieldUpdates();
        }
        showCurrent();
    }

    private void wireFolderFieldUpdates() {
        JComponent root = folderStep.getComponent();
        JTextField tf = findTextFieldByName(root, ProjectFolderStep.FOLDER_FIELD_NAME);
        if (tf != null) {
            tf.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateNav();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateNav();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateNav();
                }
            });
        }
    }

    private static @Nullable JTextField findTextFieldByName(Component comp, String name) {
        if (comp instanceof JTextField) {
            String n = comp.getName();
            if (name.equals(n)) {
                return (JTextField) comp;
            }
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                JTextField tf = findTextFieldByName(child, name);
                if (tf != null) {
                    return tf;
                }
            }
        }
        return null;
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
        if (mode == ProjectConfigMode.NEW_PROJECT && !remainingStepsBuilt && current == 0 && folderStep != null) {
            // Initialize properties and remaining steps now (UI already built)
            File dir = folderStep.getSelectedDir();
            resultProps = new ProjectProperties(dir);
            initializeRemainingSteps(resultProps);
            remainingStepsBuilt = true;
            // Move to the next step (languages)
            current = 1;
            showCurrent();
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
        ProjectProperties targetProps = (resultProps != null) ? resultProps : props;
        steps.forEach(s -> s.onSave(targetProps));
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
        boolean canProceed = true;
        if (mode == ProjectConfigMode.NEW_PROJECT && !remainingStepsBuilt && current == 0 && folderStep != null) {
            // Do lightweight validation
            canProceed = folderStep.validateInput() == null;
        }
        boolean hasNext = current < steps.size() - 1 || (mode == ProjectConfigMode.NEW_PROJECT &&
                !remainingStepsBuilt && current == 0);
        nextBtn.setEnabled(hasNext && canProceed);
        // Finish only enabled once the project folder has been selected and remaining steps built in NEW_PROJECT
        boolean finishEnabled = (mode != ProjectConfigMode.NEW_PROJECT) || remainingStepsBuilt;
        finishBtn.setEnabled(finishEnabled);
        // Highlight current step label
        for (int i = 0; i < left.getComponentCount(); i += 2) {
            if (left.getComponent(i) instanceof JLabel) {
                JLabel l = (JLabel) left.getComponent(i);
                l.setEnabled((i / 2) <= current);
            }
        }
    }

    public static final String DIALOG_NAME = "wizaard_project_properties_dialog";
    public static final String OK_BUTTON_NAME = "wizard_project_properties_ok_button";
    public static final String CANCEL_BUTTON_NAME = "wizard_project_properties_cancel_button";

    @Override
    ProjectProperties getResultProperties() {
        return (resultProps != null) ? resultProps : super.getResultProperties();
    }
}
