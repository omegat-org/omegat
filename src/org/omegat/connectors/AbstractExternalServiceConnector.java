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

package org.omegat.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.omegat.connectors.dto.ExternalProject;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.spi.ExternalServiceConnector;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.util.HttpConnectionUtils;

/**
 * Base class for External service connectors with common helpers and defaults.
 */
@NullMarked
public abstract class AbstractExternalServiceConnector implements ExternalServiceConnector {

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public abstract String getPreferenceName();

    @Override
    public List<ExternalProject> listProjects() throws ConnectorException {
        return Collections.emptyList();
    }

    @Override
    public List<ExternalResource> listResources(String projectId) throws ConnectorException {
        return Collections.emptyList();
    }

    @Override
    public InputStream fetchResource(String projectId, String resourceId) throws ConnectorException {
        throw new ConnectorException("Fetch not implemented");
    }

    @Override
    public InputStream fetchResource(String url) throws ConnectorException {
        throw new ConnectorException("Fetch not implemented");
    }

    @Override
    public void pushTranslation(String projectId, String resourceId, InputStream translated)
            throws ConnectorException {
        throw new ConnectorException("Push not supported");
    }

    protected String httpGet(String url) throws ConnectorException {
        try {
            return HttpConnectionUtils.getURL(new URL(url));
        } catch (IOException e) {
            throw new ConnectorException("GET failed: " + url, e);
        }
    }
}
