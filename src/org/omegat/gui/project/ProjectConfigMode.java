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

/**
 * Mode for the Project Properties UI.
 * <p>
 * This is a refactoring of the legacy
 * org.omegat.gui.dialogs.ProjectPropertiesDialog.Mode so that the mode type
 * lives alongside the new modular UI under org.omegat.gui.project.
 * <p>
 * Backward compatibility: The legacy enum still exists for callers that
 * reference it. New and refactored code should prefer this enum.
 */
public enum ProjectConfigMode {
    /** This UI is used to create a new project. */
    NEW_PROJECT,
    /**
     * This UI is used to resolve missing directories of an existing project.
     */
    RESOLVE_DIRS,
    /** This UI is used to edit project properties. */
    EDIT_PROJECT
}
