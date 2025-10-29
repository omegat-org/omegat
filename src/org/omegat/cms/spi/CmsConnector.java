package org.omegat.cms.spi;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.omegat.cms.dto.CmsProject;
import org.omegat.cms.dto.CmsResource;

/**
 * Service Provider Interface for External CMS connectors.
 */
public interface CmsConnector {
    String getId();

    String getName();

    Set<CmsCapability> getCapabilities();

    String getPreferenceName();

    List<CmsProject> listProjects() throws CmsException;

    List<CmsResource> listResources(String projectId) throws CmsException;

    InputStream fetchResource(String projectId, String resourceId) throws CmsException;

    void pushTranslation(String projectId, String resourceId, InputStream translated) throws CmsException;

    default boolean supports(CmsCapability c) {
        return getCapabilities() != null && getCapabilities().contains(c);
    }
}
