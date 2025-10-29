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

package org.omegat.cms.dto;

import java.io.Serializable;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * DTO representing a configured CMS target (connector + project + optional
 * URL/page).
 */
public class CmsTarget implements Serializable {
    private static final long serialVersionUID = 1L;

    private String connectorId = "";
    private String projectId = "";
    private @Nullable String baseUrl; // optional custom URL
    private @Nullable String defaultPage; // optional default page

    public CmsTarget(String connectorId, String projectId, @Nullable String baseUrl,
            @Nullable String defaultPage) {
        this.connectorId = connectorId != null ? connectorId : "";
        this.projectId = projectId != null ? projectId : "";
        this.baseUrl = baseUrl;
        this.defaultPage = defaultPage;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId != null ? connectorId : "";
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId != null ? projectId : "";
    }

    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@Nullable String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public @Nullable String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(@Nullable String defaultPage) {
        this.defaultPage = defaultPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CmsTarget)) {
            return false;
        }
        CmsTarget that = (CmsTarget) o;
        return Objects.equals(connectorId, that.connectorId) && Objects.equals(projectId, that.projectId)
                && Objects.equals(baseUrl, that.baseUrl) && Objects.equals(defaultPage, that.defaultPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId, projectId, baseUrl, defaultPage);
    }

    @Override
    public String toString() {
        return projectId + " (" + connectorId + ")";
    }

}
