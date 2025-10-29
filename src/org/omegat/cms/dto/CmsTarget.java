package org.omegat.cms.dto;

import java.io.Serializable;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * DTO representing a configured CMS target (connector + project + optional URL/page).
 */
public class CmsTarget implements Serializable {
    private static final long serialVersionUID = 1L;

    private String connectorId = "";
    private String projectId = "";
    private @Nullable String baseUrl; // optional custom URL
    private @Nullable String defaultPage; // optional default page

    public CmsTarget() {
    }

    public CmsTarget(String connectorId, String projectId, @Nullable String baseUrl, @Nullable String defaultPage) {
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
        if (this == o) return true;
        if (!(o instanceof CmsTarget)) return false;
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
