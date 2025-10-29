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

package org.omegat.cms.connectors.wikimedia;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.cms.AbstractCmsConnector;
import org.omegat.cms.dto.CmsProject;
import org.omegat.cms.dto.CmsResource;
import org.omegat.cms.spi.CmsCapability;
import org.omegat.cms.spi.CmsException;
import org.omegat.util.WikiGet;

/**
 * Connector for Wikimedia/MediaWiki content retrieval.
 */
@SuppressWarnings("unused")
public class WikimediaConnector extends AbstractCmsConnector {

    public static void loadPlugins() {
        Core.registerCmsConnectorClass(WikimediaConnector.class);
    }

    public static void unloadPlugins() {
    }

    private static final Map<String, String> config = new HashMap<>();

    static {
        config.put("Wikipedia", "https://www.wikipedia.org");
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
    public Set<CmsCapability> getCapabilities() {
        return Set.of(CmsCapability.READ, CmsCapability.SEARCH);
    }

    @Override
    public String getPreferenceName() {
        return "wikimedia";
    }

    @Override
    public List<CmsProject> listProjects() throws CmsException {
        // MediaWiki instance may not have projects; return single pseudo
        // project from base URL if provided
        return List.of(new CmsProject(config.get("Wikipedia"), "Wikimedia"));
    }

    @Override
    public List<CmsResource> listResources(String projectId) throws CmsException {
        // Not implemented: would require API query; return empty
        return Collections.emptyList();
    }

    @Override
    public InputStream fetchResource(String projectId, String resourceId) throws CmsException {
        String joined = getResourceUrl(config.get(projectId) + "/index.php?title=" + resourceId);
        String page = httpGet(joined);
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream fetchResource(String remoteUrl) throws CmsException {
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
