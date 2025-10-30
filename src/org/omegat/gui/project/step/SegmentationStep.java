/**************************************************************************
 * OmegaT - Computer Assisted Translation (CAT) tool
 *         with fuzzy matching, translation memory, keyword search,
 *         glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2025 Hiroshi Miura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.project.step;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

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

    private SegmentationCustomizerController controller;

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
        // Initialize and embed controller GUI
        controller = new SegmentationCustomizerController(true, SRX.getDefault(), Preferences.getSRX(), p.getProjectSRX());
        panel.removeAll();
        panel.add(controller.getGui(), BorderLayout.CENTER);

        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // Disable interaction in resolve mode
            for (java.awt.Component c : panel.getComponents()) {
                c.setEnabled(false);
            }
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
}
