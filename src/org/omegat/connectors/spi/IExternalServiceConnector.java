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
package org.omegat.connectors.spi;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.dto.PresetService;
import org.omegat.connectors.dto.ServiceTarget;

/**
 * Service Provider Interface for External service connectors.
 */
public interface IExternalServiceConnector {
    String getId();

    String getName();

    Set<ConnectorCapability> getCapabilities();

    String getPreferenceName();

    String getFileExtension();

    @Nullable
    String getDefaultBaseUrl();

    /**
     * Optional list of preset services this connector can target. Each preset
     * provides a human-readable name and a default base URL. Implementations
     * may return an empty list if there are no presets.
     */
    default List<PresetService> getPresets() {
        return Collections.emptyList();
    }

    List<ExternalResource> listResources(ServiceTarget target, String keyword) throws ConnectorException;

    InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException;

    InputStream fetchResource(String url) throws ConnectorException;

    default boolean supports(ConnectorCapability c) {
        return getCapabilities().contains(c);
    }
}
