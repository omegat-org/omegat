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

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OStrings;

import javax.swing.JDialog;
import java.awt.Frame;

abstract class AbstractProjectPropertiesDialog extends JDialog {

    protected final ProjectConfigMode mode;
    protected final ProjectProperties props;
    protected boolean cancelled = true;

    AbstractProjectPropertiesDialog(Frame parent, boolean modal, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, modal);
        this.mode = mode;
        this.props = props;
    }

    void showDialog() {
        setVisible(true);
    }

    boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the properties resulting from this dialog.
     * Default implementation returns the instance passed in the constructor.
     */
    ProjectProperties getResultProperties() {
        return props;
    }

    protected void updateUIText() {
        switch (mode) {
        case NEW_PROJECT:
            setTitle(OStrings.getString("PP_CREATE_PROJ"));
            break;
        case RESOLVE_DIRS:
            setTitle(OStrings.getString("PP_OPEN_PROJ"));
            break;
        case EDIT_PROJECT:
            setTitle(OStrings.getString("PP_EDIT_PROJECT"));
            break;
        }
    }
}