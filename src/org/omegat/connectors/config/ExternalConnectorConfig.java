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

package org.omegat.connectors.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.jspecify.annotations.Nullable;
import org.omegat.connectors.dto.ServiceTarget;

/**
 * XML root for External Service integration configuration persisted in cms.xml.
 */
@JacksonXmlRootElement(localName = "externalconnectors")
public class ExternalConnectorConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "version")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String version;

    @JacksonXmlProperty(localName = "target")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ServiceTarget> targets = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ServiceTarget> getTargets() {
        return targets;
    }

    public void setTargets(@Nullable List<ServiceTarget> targets) {
        this.targets = targets != null ? targets : new ArrayList<>();
    }
}
