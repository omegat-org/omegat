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
import javax.swing.JPanel;

import org.omegat.core.data.ProjectProperties;
import org.omegat.externalfinder.ExternalFinder;
import org.omegat.externalfinder.gui.ExternalFinderPreferencesController;
import org.omegat.externalfinder.item.ExternalFinderConfiguration;
import org.omegat.util.OStrings;

/**
 * Dedicated wizard step for configuring project-specific External Finder settings.
 */
public class ExternalFinderStep implements Step {

    private final JPanel panel = new JPanel(new BorderLayout());

    private ExternalFinderPreferencesController controller;

    @Override
    public String getTitle() {
        // Reuse preferences title for local (project) external finder configuration
        return OStrings.getString("PREFS_TITLE_LOCAL_EXTERNALFINDER");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        ExternalFinderConfiguration cfg = ExternalFinder.getProjectConfig();
        controller = new ExternalFinderPreferencesController(true,
                cfg == null ? ExternalFinderConfiguration.empty() : cfg);
        panel.removeAll();
        panel.add(controller.getGui(), BorderLayout.CENTER);
    }

    @Override
    public void onSave(ProjectProperties p) {
        if (controller != null) {
            ExternalFinderConfiguration result = controller.getResult();
            ExternalFinder.setProjectConfig(result);
        }
    }
}
