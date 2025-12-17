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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.util.WikiGet;

/**
 * Connector for Wikimedia/MediaWiki content retrieval.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public abstract class AbstractWikimediaConnector extends AbstractExternalServiceConnector {

    private static final String FILE_EXT = "UTF8";
    private static final String ACTION_RAW = "action=raw";
    private static final String INDEX_PHP_TITLE = "index.php?title=";
    private static final String INDEX_PHP_TITLE_ESCAPED = "index.php\\?title=";

    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(WikimediaCleanUrlConnector.class);
        Core.registerExternalServiceConnectorClass(WikimediaDefaultConnector.class);
    }

    public static void unloadPlugins() {
        // do nothing
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ, ConnectorCapability.READ_URL);
    }

    @Override
    public String getFileExtension() {
        return FILE_EXT;
    }

    @Override
    public InputStream fetchResource(String remoteUrl) throws ConnectorException {
        String page = httpGet(getResourceUrl(remoteUrl));
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }

    protected String getResourceUrl(String remoteUrl) {
        if (remoteUrl.indexOf(INDEX_PHP_TITLE) > 0) {
            // We're directly calling the mediawiki index.php script
            String[] splitted = remoteUrl.split(INDEX_PHP_TITLE_ESCAPED);
            String s = splitted[splitted.length - 1];
            s = s.replaceAll(" ", "_");
            s = URLEncoder.encode(s, StandardCharsets.UTF_8);
            splitted[splitted.length - 1] = s;
            return WikiGet.joinString(INDEX_PHP_TITLE, splitted) + "&" + ACTION_RAW;
        } else {
            // assume script is behind some sort
            // of url-rewriting
            String[] splitted = remoteUrl.split("/");
            String s = splitted[splitted.length - 1];
            s = s.replaceAll(" ", "_");
            s = URLEncoder.encode(s, StandardCharsets.UTF_8);
            splitted[splitted.length - 1] = s;
            return WikiGet.joinString("/", splitted) + "?" + ACTION_RAW;
        }
    }
}
