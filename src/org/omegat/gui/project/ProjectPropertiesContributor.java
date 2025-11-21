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

import javax.swing.JComponent;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;

/**
 * Extension point for contributing custom UI to the Project Properties wizard.
 * <p>
 * Implementations should be registered via Java's ServiceLoader under
 * META-INF/services/org.omegat.gui.project.ProjectPropertiesContributor.
 */
public interface ProjectPropertiesContributor {
    /**
     * A human-readable title for this contribution.
     */
    String getTitle();

    /**
     * Create the UI component to embed in the wizard.
     */
    JComponent createComponent();

    /**
     * Called when the wizard is opened to populate the UI from project
     * properties.
     */
    default void onLoad(ProjectProperties props) {
    }

    /**
     * Validate current UI state. Return null if valid; otherwise, return a
     * localized error message.
     */
    default @Nullable String validateInput() {
        return null;
    }

    /**
     * Apply current UI state to project properties.
     */
    default void onSave(ProjectProperties props) {
    }
}
