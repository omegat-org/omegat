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

import org.omegat.connectors.dto.PresetService;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Connector for Wikimedia/MediaWiki content retrieval.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public class WikimediaCleanUrlConnector extends AbstractWikimediaConnector {

    private static final String DEFAULT_BASE_URL = "https://en.wikipedia.org/wiki/";
    private static final String WIKIMEDIA_ID = "wikimedia";
    private static final String PREFERENCE_NAME = "wikimedia";

    private final ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/connectors/wiki/wikimedia/Bundle");

    @Override
    public String getId() {
        return WIKIMEDIA_ID;
    }

    @Override
    public String getName() {
        return bundle.getString("WIKIMEDIA_CLEANURL_CONNECTOR_NAME");
    }

    @Override
    public String getPreferenceName() {
        return PREFERENCE_NAME;
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

    @Override
    public List<PresetService> getPresets() {
        return List.of(
                new PresetService("Wikipedia.org", DEFAULT_BASE_URL),
                new PresetService("Wikivoyage", "https://en.wikivoyage.org/wiki/"));
    }
}
