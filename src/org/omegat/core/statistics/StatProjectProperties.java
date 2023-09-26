/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
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

package org.omegat.core.statistics;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;

/**
 * Data class for statistics XML and JSON.
 * 
 * @author Hiroshi Miura
 */
public final class StatProjectProperties {

    @JsonProperty("name")
    private final String projectName;
    @JsonProperty("root")
    private final String projectRoot;
    @JsonProperty("source-langauge")
    private final String sourceLanguage;
    @JsonProperty("target-language")
    private final String targetLanguage;

    @JsonIgnore
    private final String sourceRoot;

    public StatProjectProperties() {
        ProjectProperties props = Core.getProject().getProjectProperties();
        this.projectName = props.getProjectName();
        this.projectRoot = props.getProjectRoot();
        this.sourceLanguage = props.getSourceLanguage().getLanguage();
        this.targetLanguage = props.getTargetLanguage().getLanguage();
        this.sourceRoot = props.getSourceRoot();

    }

    @XmlAttribute(name = "name")
    public String getProjectName() {
        return projectName;
    }

    @XmlAttribute(name = "root")
    public String getProjectRoot() {
        return projectRoot;
    }

    @XmlAttribute(name = "source-language")
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    @XmlAttribute(name = "target-language")
    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getSourceRoot() {
        return sourceRoot;
    }
}
