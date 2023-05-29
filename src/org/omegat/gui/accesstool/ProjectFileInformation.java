/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.accesstool;

import java.nio.file.Paths;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;

class ProjectFileInformation {

    private static final int FILE_SELECTOR_WIDTH = 48;

    private final String filePath;
    private final int segments;

    public ProjectFileInformation(final String activeFileName) {
        filePath = Paths.get(activeFileName).getFileName().toString();
        segments =
                Core.getProject().getProjectFiles().stream()
                        .filter(fi -> Paths.get(fi.filePath).getFileName().toString().equals(this.filePath))
                        .map(fi -> fi.entries.size())
                        .findFirst()
                        .orElse(0);
    }

    public ProjectFileInformation(final IProject.FileInfo f) {
        filePath = Paths.get(f.filePath).getFileName().toString();
        segments = f.entries.size();
    }

    public String getFilePath() {
        return filePath;
    }

    public int getSegments() {
        return segments;
    }

    public int getModelRow() {
        int modelRow = -1;
        List<IProject.FileInfo> projectFiles = Core.getProject().getProjectFiles();
        for (int i = 0; i < projectFiles.size(); i++) {
            if (Paths.get(projectFiles.get(i).filePath).getFileName().toString().equals(this.filePath)) {
                modelRow = i;
                break;
            }
        }
        return modelRow;
    }
}
