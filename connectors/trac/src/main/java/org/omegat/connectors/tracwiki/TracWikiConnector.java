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
import org.omegat.connectors.dto.ExternalProject;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.core.Core;
import org.omegat.gui.preferences.PreferencesControllers;
import org.omegat.util.Preferences;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TracWikiConnector extends AbstractExternalServiceConnector {

    public static final String PREF_USE_XMLRPC = "tracwiki.useXmlRpc";

    @SuppressWarnings("unused")
    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(TracWikiConnector.class);
        // Register preferences UI for this connector
        PreferencesControllers.addSupplier(TracWikiPreferencesController::new);
    }

    @SuppressWarnings("unused")
    public static void unloadPlugins() {
        // do nothing
    }

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        // Map of projectId -> base wiki URL
        CONFIG.put("tracwiki", "https://trac.edgewall.org/wiki");
    }

    @Override
    public String getId() {
        return "trac";
    }

    @Override
    public String getName() {
        return "Trac connector";
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ, ConnectorCapability.LIST_PROJECTS, ConnectorCapability.SEARCH);
    }

    @Override
    public String getPreferenceName() {
        return "trac";
    }

    @Override
    public List<ExternalProject> listProjects() {
        // Expose configured Trac instances as projects
        List<ExternalProject> projects = new ArrayList<>();
        for (Map.Entry<String, String> e : CONFIG.entrySet()) {
            projects.add(new ExternalProject(e.getKey(), e.getValue()));
        }
        return projects.isEmpty() ? Collections.emptyList() : projects;
    }

    @Override
    public List<ExternalResource> listResources(String projectId) throws ConnectorException {
        String baseWikiUrl = CONFIG.get(projectId);
        if (baseWikiUrl == null) {
            throw new ConnectorException("Unknown projectId: " + projectId);
        }
        boolean useRpc = Preferences.isPreferenceDefault(PREF_USE_XMLRPC, false);
        if (!useRpc) {
            // Listing pages is not supported in scraping mode (requires complex index parsing).
            throw new ConnectorException("Listing resources is not supported in HTML scraping mode. Enable XML-RPC in preferences.");
        }
        TracWikiRpc rpc = createRpcFromWikiUrl(baseWikiUrl);
        List<String> pages;
        try {
            pages = rpc.getAllPages();
        } catch (Exception e) {
            throw new ConnectorException("Failed to list wiki pages via RPC", e);
        }
        List<ExternalResource> resources = new ArrayList<>(pages.size());
        for (String page : pages) {
            resources.add(new ExternalResource(page, page, "/wiki/" + page));
        }
        return resources;
    }

    @Override
    public InputStream fetchResource(String projectId, String resourceId) throws ConnectorException {
        String baseWikiUrl = CONFIG.get(projectId);
        if (baseWikiUrl == null) {
            throw new ConnectorException("Unknown projectId: " + projectId);
        }
        boolean useRpc = Preferences.isPreferenceDefault(PREF_USE_XMLRPC, false);
        if (useRpc) {
            TracWikiRpc rpc = createRpcFromWikiUrl(baseWikiUrl);
            String text;
            try {
                text = rpc.getPage(resourceId);
            } catch (Exception e) {
                throw new ConnectorException("Failed to fetch page via RPC: " + resourceId, e);
            }
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        } else {
            // HTML scraping via edit form
            String editUrl = buildEditUrl(baseWikiUrl, resourceId);
            String html = httpGet(editUrl);
            String text = extractWikiTextFromEditHtml(html);
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public InputStream fetchResource(String url) throws ConnectorException {
        boolean useRpc = Preferences.isPreferenceDefault(PREF_USE_XMLRPC, false);
        // Try to route Trac wiki page URLs appropriately per preference; fallback to HTTP GET
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            if (path != null && path.contains("/wiki/")) {
                String pageName = path.substring(path.indexOf("/wiki/") + 6);
                if (useRpc) {
                    TracWikiRpc rpc = createRpcFromHost(u);
                    String text = rpc.getPage(pageName);
                    return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                } else {
                    String editUrl = buildEditUrl(u, pageName);
                    String html = httpGet(editUrl);
                    String text = extractWikiTextFromEditHtml(html);
                    return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                }
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

    private TracWikiRpc createRpcFromWikiUrl(String wikiUrl) throws ConnectorException {
        try {
            URI u = URI.create(wikiUrl);
            return createRpcFromHost(u);
        } catch (IllegalArgumentException e) {
            throw new ConnectorException("Invalid wiki URL: " + wikiUrl, e);
        }
    }

    private TracWikiRpc createRpcFromHost(URI u) throws ConnectorException {
        try {
            StringBuilder ep = new StringBuilder();
            ep.append(u.getScheme()).append("://").append(u.getHost());
            if (u.getPort() != -1) {
                ep.append(":").append(u.getPort());
            }
            // Prefer anonymous RPC when possible
            ep.append("/rpc");
            return new TracWikiRpc(ep.toString(), "", "");
        } catch (Exception e) {
            throw new ConnectorException("Failed to create Trac WikiRPC client", e);
        }
    }
}
