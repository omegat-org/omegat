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
import org.omegat.gui.project.ProjectPropertiesContributor;

/**
 * Wraps a ProjectPropertiesContributor as a wizard Step.
 */
public class ContributorStep implements ProjectWizardStep {
    private final ProjectPropertiesContributor delegate;
    private final JComponent comp;

    public ContributorStep(ProjectPropertiesContributor delegate) {
        this.delegate = delegate;
        this.comp = delegate.createComponent();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public JComponent getComponent() {
        return comp;
    }

    @Override
    public void onLoad(ProjectProperties props) {
        delegate.onLoad(props);
    }

    @Override
    public @Nullable String validateInput() {
        return delegate.validateInput();
    }

    @Override
    public void onSave(ProjectProperties props) {
        delegate.onSave(props);
    }
}
