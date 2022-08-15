/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.statistics;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;

/**
 * Data class for statistics XML and JSON.
 * @author Hiroshi Miura
 */
public class StatProjectProperties {

    private final String projectName;
    private final String projectRoot;
    private final String sourceLanguage;
    private final String targetLanguage;

    public StatProjectProperties() {
        ProjectProperties props = Core.getProject().getProjectProperties();
        this.projectName = props.getProjectName();
        this.projectRoot = props.getProjectRoot();
        this.sourceLanguage = props.getSourceLanguage().getLanguage();
        this.targetLanguage = props.getTargetLanguage().getLanguage();
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }
}
