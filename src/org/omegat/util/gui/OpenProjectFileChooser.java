/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.util.gui;

import java.io.File;

import org.omegat.util.StaticUtils;

/**
 * File Chooser to open project. Project is a directory, so it's a bit tricky,
 * we need to react on both: - changing directory - and hitting OK.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class OpenProjectFileChooser extends OmegaTFileChooser {

    public OpenProjectFileChooser() {
        setFileSelectionMode(DIRECTORIES_ONLY);
        setMultiSelectionEnabled(false);
    }

    public void approveSelection() {
        // user hit 'open' button - redirect command to open project or
        // recurse into lower directory
        if (StaticUtils.isProjectDir(getSelectedFile())) {
            // The parent directory is made current,
            // and the project's directory is the selected 'file'.
            super.approveSelection();
        } else {
            setCurrentDirectory(getSelectedFile());
        }
    }

    public void setCurrentDirectory(File dir) {
        if (StaticUtils.isProjectDir(dir)) {
            setSelectedFile(dir);
            approveSelection();
        } else {
            super.setCurrentDirectory(dir);
        }
    }
}
