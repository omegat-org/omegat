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

import java.awt.Frame;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;

/**
 * Entry point for the new modular Project Properties UI.
 * <p>
 * Provides both a simplified dialog (for creating a new project) and a
 * wizard-style dialog (for editing or resolving project directories), with a
 * small controller to orchestrate validation and applying values back to
 * ProjectProperties.
 */
public final class ProjectConfigUI {

    private ProjectConfigUI() {
    }

    /**
     * Adapter overload for callers still using the legacy dialog mode enum.
     * Maps legacy {@link org.omegat.gui.dialogs.ProjectPropertiesDialog.Mode}
     * to {@link ProjectConfigMode} and delegates to the primary overload.
     *
     * @return Updated properties, or null if cancelled
     */
    public static @Nullable ProjectProperties showDialog(Frame parent, ProjectProperties projectProperties,
            String projFileName, org.omegat.gui.dialogs.ProjectPropertiesDialog.Mode dialogTypeValue) {
        final ProjectConfigMode mode;
        switch (dialogTypeValue) {
        case NEW_PROJECT:
            mode = ProjectConfigMode.NEW_PROJECT;
            break;
        case EDIT_PROJECT:
            mode = ProjectConfigMode.EDIT_PROJECT;
            break;
        case RESOLVE_DIRS:
            mode = ProjectConfigMode.RESOLVE_DIRS;
            break;
        default:
            throw new IllegalArgumentException("Unexpected dialog type: " + dialogTypeValue);
        }
        return showDialog(parent, projectProperties, projFileName, mode);
    }

    /**
     * Show the new Project Properties UI.
     * <p>
     * For NEW_PROJECT: shows a simplified dialog that focuses on core settings
     * (languages). For EDIT_PROJECT and RESOLVE_DIRS: shows a wizard with steps
     * for Languages and Locations, plus optional contributions from extensions
     * via {@link ProjectPropertiesContributor}.
     *
     * @param parent
     *            Parent frame
     * @param projectProperties
     *            properties instance to read/update
     * @param projFileName
     *            project file name (currently unused, reserved for future)
     * @param dialogTypeValue
     *            legacy dialog mode
     * @return Updated properties, or null if cancelled
     */
    public static @Nullable ProjectProperties showDialog(Frame parent, ProjectProperties projectProperties,
        String projFileName, ProjectConfigMode dialogTypeValue) {
        WizardProjectPropertiesDialog dlg = new WizardProjectPropertiesDialog(parent, projectProperties,
                dialogTypeValue);
        dlg.setVisible(true);
        return dlg.isCancelled() ? null : projectProperties;
    }
}
