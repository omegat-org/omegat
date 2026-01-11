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
import org.omegat.connectors.dto.PresetService;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * TracWikiConnector is a subclass of AbstractExternalServiceConnector that provides
 * integration with Trac's wiki functionality. This connector enables the reading
 * of wiki pages by fetching the content from Trac's edit interface.
 * <p>
 * The connector supports fetching wiki content using both URL-based access and service-targeted
 * access. It uses authentication credentials when required, allowing interaction with secure
 * or restricted Trac wiki instances.
 * <p>>
 * Key features and behaviors:
 * - Handles APIs that require login credentials, with support for dynamically prompting
 *   users to input their credentials.
 * - Uses HTTP GET requests to fetch wiki page content.
 * - Extracts raw wiki text from the HTML of the edit page.
 * - Provides a preference name and file extension specific to its functionality.
 * - Allows custom URL configuration for flexible integration.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public class TracWikiConnector extends AbstractExternalServiceConnector {

    private static final String USER_KEY = "tracwiki.api.username";
    private static final String PASS_KEY = "tracwiki.api.password";
    private static final String CONNECTOR_ID = "tracwiki";
    private static final String PREFERENCE_NAME = "tracwiki";
    private static final String WIKI = "/wiki";
    private static final String ACTION_EDIT = "?action=edit";
    private static final String HTML_QUERY = "textarea[name=text]";
    private static final String TEXT_ELEMENT = "text";
    private static final String TXT = "txt";

    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(TracWikiConnector.class);
    }

    public static void unloadPlugins() {
        // do nothing
    }

    private final ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/connectors/tracwiki/Bundle");

    @Override
    public String getId() {
        return CONNECTOR_ID;
    }

    @Override
    public String getName() {
        return bundle.getString("TRACWIKI_NAME");
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ, ConnectorCapability.READ_URL);
    }

    @Override
    public String getPreferenceName() {
        return PREFERENCE_NAME;
    }

    @Override
    public String getFileExtension() {
        return TXT;
    }

    @Override
    public List<PresetService> getPresets() {
        return List.of(new PresetService("trac.edgewall.org", "https://trac.edgewall.org/wiki"));
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        String baseWikiUrl = target.getBaseUrl();
        String editUrl = buildEditUrl(baseWikiUrl, resourceId);
        String html;
        if (target.isLoginRequired()) {
            String userId = getCredential(USER_KEY);
            String password = getCredential(PASS_KEY);
            String credStr;
            if (userId.isEmpty() || password.isEmpty()) {
                String[] cred = askCredentials(bundle.getString("TRACWIKI_CREDENTIAL_TITLE"), "");
                if (cred == null || cred.length != 2) {
                    throw new ConnectorException(bundle.getString("TRACWIKI_CREDENTIAL_ERROR"));
                }
                setCredential(USER_KEY, cred[0], false);
                setCredential(PASS_KEY, cred[1], false);
                credStr = cred[0] + ":" + cred[1];
            } else {
                credStr = userId + ":" + password;
            }
            html = getURL(editUrl, credStr, 10000);
        } else {
            html = httpGet(editUrl);
        }
        String text = extractWikiTextFromEditHtml(html);
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputStream fetchResource(String url) throws ConnectorException {
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            if (path != null && path.contains(WIKI + "/")) {
                String pageName = path.substring(path.indexOf(WIKI + "/") + (WIKI + "/").length());
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
        String base = baseWikiUrl.endsWith("/") ? baseWikiUrl.substring(0, baseWikiUrl.length() - 1)
                : baseWikiUrl;
        if (!base.contains(WIKI)) {
            base = base + WIKI;
        }
        return base + "/" + pageName + ACTION_EDIT;
    }

    private String buildEditUrl(URI u, String pageName) {
        StringBuilder b = new StringBuilder();
        b.append(u.getScheme()).append("://").append(u.getHost());
        if (u.getPort() != -1) {
            b.append(":").append(u.getPort());
        }
        // up to /wiki
        String path = u.getPath();
        int idx = path.indexOf(WIKI + "/");
        if (idx >= 0) {
            b.append(path, 0, idx).append(WIKI).append('/')
                    .append(pageName).append(ACTION_EDIT);
        } else {
            b.append(path);
            if (!path.endsWith("/")) {
                b.append('/');
            }
            // Append without leading slash since we ensured trailing slash above
            b.append(WIKI.substring(1)).append('/')
                    .append(pageName).append(ACTION_EDIT);
        }
        return b.toString();
    }

    private String extractWikiTextFromEditHtml(String html) throws ConnectorException {
        try {
            Document doc = Jsoup.parse(html);
            // Trac edit form typically has <textarea id="text"
            // name="text">raw</textarea>
            Element ta = doc.selectFirst(HTML_QUERY);
            if (ta == null) {
                ta = doc.getElementById(TEXT_ELEMENT);
            }
            if (ta == null) {
                throw new ConnectorException(bundle.getString("TRACWIKI_WIKITEXT_ERROR"));
            }
            return ta.text();
        } catch (Exception e) {
            if (e instanceof ConnectorException) {
                throw (ConnectorException) e;
            }
            throw new ConnectorException(bundle.getString("TRACWIKI_PARSE_ERROR"), e);
        }
    }
}
