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

import javax.swing.JComponent;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.RepositoriesMappingController;
import org.omegat.gui.dialogs.RepositoriesMappingPanel;
import org.omegat.util.OStrings;

import gen.core.project.RepositoryDefinition;

/**
 * Wizard step for configuring project repositories mapping (Team project).
 * This embeds the reusable RepositoriesMappingPanel and delegates logic to RepositoriesMappingController.
 */
public class RepositoriesMappingStep implements ProjectWizardStep {

    private final RepositoriesMappingPanel panel;
    private final RepositoriesMappingController controller;

    public RepositoriesMappingStep() {
        // no-op; controller will be bound on load
        panel = new RepositoriesMappingPanel();
        controller = new RepositoriesMappingController();
    }

    @Override
    public String getTitle() {
        return OStrings.getString("RMD_TITLE");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        controller.bindToPanel(panel, p.getRepositories());
    }

    @Override
    public @Nullable String validateInput() {
        return controller.validateInput();
    }

    @Override
    public void onSave(ProjectProperties p) {
        String err = controller.onOk();
        if (err == null) {
            java.util.List<RepositoryDefinition> data = controller.getResult();
            p.setRepositories(data);
        }
    }
}
