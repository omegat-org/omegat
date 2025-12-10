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

package org.omegat.connectors.tracwiki;

import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.core.Core;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TracWikiConnector extends AbstractExternalServiceConnector {

    @SuppressWarnings("unused")
    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(TracWikiConnector.class);
    }

    @SuppressWarnings("unused")
    public static void unloadPlugins() {
        // do nothing
    }

    @Override
    public String getId() {
        return "tracwiki";
    }

    @Override
    public String getName() {
        return "Trac connector";
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ);
    }

    @Override
    public String getPreferenceName() {
        return "tracwiki";
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        String baseWikiUrl = target.getBaseUrl();
        String editUrl = buildEditUrl(baseWikiUrl, resourceId);
        String html = httpGet(editUrl);
        String text = extractWikiTextFromEditHtml(html);
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputStream fetchResource(String url) throws ConnectorException {
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            if (path != null && path.contains("/wiki/")) {
                String pageName = path.substring(path.indexOf("/wiki/") + 6);
                String editUrl = buildEditUrl(u, pageName);
                String html = httpGet(editUrl);
                String text = extractWikiTextFromEditHtml(html);
                return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignore) {
            // fall through to HTTP
        }

        String page = httpGet(url);
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }

    private String buildEditUrl(String baseWikiUrl, String pageName) {
        // Expect base like https://host/wiki
        String base = baseWikiUrl.endsWith("/") ? baseWikiUrl.substring(0, baseWikiUrl.length() - 1) : baseWikiUrl;
        if (!base.contains("/wiki")) {
            // Try to append wiki path
            base = base + "/wiki";
        }
        return base + "/" + pageName + "?action=edit";
    }

    private String buildEditUrl(URI u, String pageName) {
        StringBuilder b = new StringBuilder();
        b.append(u.getScheme()).append("://").append(u.getHost());
        if (u.getPort() != -1) {
            b.append(":").append(u.getPort());
        }
        // up to /wiki
        String path = u.getPath();
        int idx = path.indexOf("/wiki/");
        if (idx >= 0) {
            b.append(path, 0, idx).append("/wiki/").append(pageName).append("?action=edit");
        } else {
            b.append(path);
            if (!path.endsWith("/")) {
                b.append('/');
            }
            b.append("wiki/").append(pageName).append("?action=edit");
        }
        return b.toString();
    }

    private String extractWikiTextFromEditHtml(String html) throws ConnectorException {
        try {
            Document doc = Jsoup.parse(html);
            // Trac edit form typically has <textarea id="text" name="text">raw</textarea>
            Element ta = doc.selectFirst("textarea[name=text]");
            if (ta == null) {
                ta = doc.getElementById("text");
            }
            if (ta == null) {
                throw new ConnectorException("Failed to locate wiki text textarea in edit form");
            }
            return ta.text();
        } catch (Exception e) {
            if (e instanceof ConnectorException) {
                throw (ConnectorException) e;
            }
            throw new ConnectorException("Failed to parse Trac edit page HTML", e);
        }
    }
}
