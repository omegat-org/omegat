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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.dto.ExternalProject;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.util.WikiGet;

/**
 * Connector for Wikimedia/MediaWiki content retrieval.
 */
@SuppressWarnings("unused")
public class WikimediaConnector extends AbstractExternalServiceConnector {

    public static void loadPlugins() {
        Core.registerCmsConnectorClass(WikimediaConnector.class);
    }

    public static void unloadPlugins() {
        // do nothing
    }

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        CONFIG.put("Wikipedia", "https://www.wikipedia.org");
    }

    @Override
    public String getId() {
        return "wikimedia";
    }

    @Override
    public String getName() {
        return "Wikimedia";
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ, ConnectorCapability.SEARCH);
    }

    @Override
    public String getPreferenceName() {
        return "wikimedia";
    }

    @Override
    public List<ExternalProject> listProjects() throws ConnectorException {
        // MediaWiki instance may not have projects; return single pseudo
        // project from base URL if provided
        return List.of(new ExternalProject(CONFIG.get("Wikipedia"), "Wikimedia"));
    }

    @Override
    public List<ExternalResource> listResources(String projectId) throws ConnectorException {
        // Not implemented: would require API query; return empty
        return Collections.emptyList();
    }

    @Override
    public InputStream fetchResource(String projectId, String resourceId) throws ConnectorException {
        String joined = getResourceUrl(CONFIG.get(projectId) + "/index.php?title=" + resourceId);
        String page = httpGet(joined);
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream fetchResource(String remoteUrl) throws ConnectorException {
        String page = httpGet(getResourceUrl(remoteUrl));
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }

    private String getResourceUrl(String remoteUrl) {
        if (remoteUrl.indexOf("index.php?title=") > 0) {
            // We're directly calling the mediawiki index.php script
            String[] splitted = remoteUrl.split("index.php\\?title=");
            String s = splitted[splitted.length - 1];
            s = s.replaceAll(" ", "_");
            // s=URLEncoder.encode(s, "UTF-8"); // breaks previously
            // correctly encoded page names
            splitted[splitted.length - 1] = s;
            return WikiGet.joinString("index.php?title=", splitted) + "&action=raw";
        } else {
            // assume script is behind some sort
            // of url-rewriting
            String[] splitted = remoteUrl.split("/");
            String s = splitted[splitted.length - 1];
            s = s.replaceAll(" ", "_");
            // s=URLEncoder.encode(s, "UTF-8");
            splitted[splitted.length - 1] = s;
            return WikiGet.joinString("/", splitted) + "?action=raw";
        }
    }
}
