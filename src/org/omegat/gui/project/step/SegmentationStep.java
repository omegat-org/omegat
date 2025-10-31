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
package org.omegat.gui.project.step;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.segmentation.SRX;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.gui.segmentation.SegmentationCustomizerController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Dedicated wizard step for editing project-specific segmentation rules
 * using SegmentationCustomizerController.
 */
public class SegmentationStep implements Step {

    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel(new BorderLayout());

    private @Nullable SegmentationCustomizerController controller;
    private @Nullable JLabel disabledLabel;
    private @Nullable JComponent controllerGui;

    public SegmentationStep(ProjectConfigMode mode) {
        this.mode = mode;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("GUI_SEGMENTATION_TITLE_PROJECTSPECIFIC");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        // Initialize and embed controller GUI, hiding internal project-specific checkbox in wizard
        controller = new SegmentationCustomizerController(true, false, SRX.getDefault(), Preferences.getSRX(), p.getProjectSRX());
        panel.removeAll();
        controllerGui = controller.getGui();
        // Info label explaining why disabled
        if (disabledLabel == null) {
            disabledLabel = new JLabel(OStrings.getString("WIZ_SEGMENTATION_DISABLED_MESSAGE"));
            disabledLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            disabledLabel.setName("wizard_segmentation_disabled_label");
        }
        panel.add(disabledLabel, BorderLayout.NORTH);
        panel.add(controllerGui, BorderLayout.CENTER);
        disabledLabel.setVisible(false); // default hidden until wizard toggles

        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // Disable interaction in resolve mode
            controllerGui.setEnabled(false);
            disabledLabel.setVisible(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        // No specific validation beyond controller's own constraints
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        if (controller != null) {
            SRX srx = controller.getResult();
            p.setProjectSRX(srx);
        }
    }

    /**
     * Allows the wizard to enable or disable the segmentation editing based on
     * the selection in LanguagesAndOptionsStep.
     */
    public void setProjectSpecificSegmentationEnabled(boolean enabled) {
        if (controller != null) {
            controller.setProjectSpecificEnabled(enabled);
        }
        // Enable/disable only the editor UI, keep the info label readable
        if (controllerGui != null) {
            controllerGui.setEnabled(enabled);
        }
        if (disabledLabel != null) {
            disabledLabel.setVisible(!enabled && mode != ProjectConfigMode.RESOLVE_DIRS);
        }
    }
}
