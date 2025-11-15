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
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.filters2.FiltersCustomizerController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

import gen.core.filters.Filters;

/**
 * Dedicated wizard step for editing project-specific file filter definitions
 * using FiltersCustomizerController.
 */
public class FilterDefinitionStep implements ProjectWizardStep {

    private final JPanel panel = new JPanel(new BorderLayout());

    private FiltersCustomizerController controller;
    private @Nullable JLabel disabledLabel;
    private @Nullable JComponent controllerGui;

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
        controller = new FiltersCustomizerController(true, FilterMaster.createDefaultFiltersConfig(), Preferences.getFilters(), p.getProjectFilters());
        panel.removeAll();
        controllerGui = controller.getGui();
        // Info label explaining why disabled
        if (disabledLabel == null) {
            disabledLabel = new JLabel(OStrings.getString("WIZ_FILTERS_DISABLED_MESSAGE"));
            disabledLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            disabledLabel.setName("wizard_filters_disabled_label");
        }
        panel.add(disabledLabel, BorderLayout.NORTH);
        panel.add(controllerGui, BorderLayout.CENTER);
        disabledLabel.setVisible(false); // default hidden until wizard toggles
    }

    @Override
    public void onSave(ProjectProperties p) {
        if (controller != null) {
            Filters filters = controller.getResult();
            p.setProjectFilters(filters);
        }
    }

    /**
     * Allows the wizard to enable or disable the filters editing based on
     * the selection in LanguagesAndOptionsStep.
     */
    public void setProjectSpecificFiltersEnabled(boolean enabled) {
        if (controller != null) {
            controller.setProjectSpecificEnabled(enabled);
        }
        if (controllerGui != null) {
            controllerGui.setEnabled(enabled);
        }
        if (disabledLabel != null) {
            disabledLabel.setVisible(!enabled);
        }
    }
}
