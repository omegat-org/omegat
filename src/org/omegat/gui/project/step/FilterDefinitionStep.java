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
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.filters2.FiltersCustomizerController;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

import gen.core.filters.Filters;

/**
 * Dedicated wizard step for editing project-specific file filter definitions
 * using FiltersCustomizerController.
 */
public class FilterDefinitionStep implements Step {

    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel(new BorderLayout());

    private FiltersCustomizerController controller;

    public FilterDefinitionStep(ProjectConfigMode mode) {
        this.mode = mode;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("FILTERSCUSTOMIZER_TITLE_PROJECTSPECIFIC");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        controller = new FiltersCustomizerController(true,
                FilterMaster.createDefaultFiltersConfig(), Preferences.getFilters(), p.getProjectFilters());
        panel.removeAll();
        panel.add(controller.getGui(), BorderLayout.CENTER);

        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            for (java.awt.Component c : panel.getComponents()) {
                c.setEnabled(false);
            }
        }
    }

    @Override
    public @Nullable String validateInput() {
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        if (controller != null) {
            Filters filters = controller.getResult();
            p.setProjectFilters(filters);
        }
    }
}
