package org.omegat.cms.dto;

/**
 * Common shape for CMS DTOs that have an id and an optional display name.
 */
public interface CmsIdentified {
    String getId();
    /**
     * Optional human-friendly name. May return null.
     */
    String getName();
}
