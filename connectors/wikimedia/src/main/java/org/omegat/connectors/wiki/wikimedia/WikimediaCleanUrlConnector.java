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

package org.omegat.connectors.wiki.wikimedia;

import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Connector for Wikimedia/MediaWiki content retrieval.
 */
@SuppressWarnings("unused")
public class WikimediaCleanUrlConnector extends AbstractWikimediaConnector {

    @Override
    public String getId() {
        return "wikimedia";
    }

    @Override
    public String getName() {
        return "Wikimedia(Clean URL)";
    }

    @Override
    public String getPreferenceName() {
        return "wikimedia";
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        String base = target.getBaseUrl();
        if (!base.endsWith("/")) {
            base += "/";
        }
        String page = httpGet(getResourceUrl(base + resourceId));
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }
}
