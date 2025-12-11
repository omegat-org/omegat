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

package org.omegat.connectors.translatewiki;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.core.Core;
import org.omegat.util.HttpConnectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class TranslateWikiConnector extends AbstractExternalServiceConnector {

    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(TranslateWikiConnector.class);
    }

    public static void unloadPlugins() {
        // do nothing
    }

    private static final String USER_KEY = "translatewiki.api.username";
    private static final String PASS_KEY = "translatewiki.api.password";
    private static final String BASE_URL = "https://translatewiki.net/";
    private static final String API_PATH = "w/api.php";

    private static final String EXPORT_ACTION = "action=query&list=messagecollection&format=json";
    private static final String QUERY_LANGUAGE = "mclanguage=";
    private static final String VALUE_ENGLISH = "en";
    private static final String GROUP = "mcgroup=";

    private static final String QUERY_ACTION = "action=translationentitysearch&format=json";
    private static final String QUERY_GROUPS = "&entitytype=groups&limit=50&query=";

    @Override
    public String getId() {
        return "translatewiki";
    }

    @Override
    public String getName() {
        return "TranslateWiki";
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ, ConnectorCapability.LIST);
    }

    @Override
    public String getDefaultBaseUrl() {
        return BASE_URL;
    }

    @Override
    public String getPreferenceName() {
        return "transaltewiki";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public List<ExternalResource> listResources(ServiceTarget target, String keyword) throws ConnectorException {
        String queryUrl;
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            queryUrl = target.getBaseUrl() + API_PATH + "?" + QUERY_ACTION + QUERY_GROUPS + encoded;
        } catch (Exception e) {
            throw new ConnectorException("Failed to build query URL", e);
        }

        try {
            String json = HttpConnectionUtils.getURL(new URL(queryUrl));
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode messages = root.path("translationentitysearch").path("groups");

            if (messages == null || !messages.isArray()) {
                return Collections.emptyList();
            }

            java.util.ArrayList<ExternalResource> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode n : messages) {
                String label = n.path("label").asText(null);
                String group = n.path("group").asText(null);
                if (label == null || label.isEmpty()) {
                    continue;
                }
                if (group == null || group.isEmpty()) {
                    continue;
                }
                // Use pattern as both id and name; path is not used at this stage
                result.add(new ExternalResource(group, label, ""));
            }
            return result;
        } catch (IOException e) {
            throw new ConnectorException("GET failed: " + queryUrl, e);
        }
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        String url = target.getBaseUrl() + API_PATH + "?" + EXPORT_ACTION + "&" + QUERY_LANGUAGE + VALUE_ENGLISH + "&"
                + GROUP + resourceId;
        String page;
        if (target.isLoginRequired()) {
            String userId = getCredential(USER_KEY);
            String password = getCredential(PASS_KEY);
            String credStr;
            if (userId.isEmpty() || password.isEmpty()) {
                String[] cred = askCredentials("Please enter Bot user ID(User@BotName) and a passcode",
                        "Please create bot password from https://translatewiki.net/wiki/Special:BotPasswords");
                if (cred == null || cred.length != 2) {
                    throw new ConnectorException("Invalid credentials");
                }
                setCredential(USER_KEY, cred[0], false);
                setCredential(PASS_KEY, cred[1], false);
                credStr = cred[0] + ":" + cred[1];
            } else {
                credStr = userId + ":" + password;
            }
            page = getURL(url, credStr, 10000);
        } else {
            page = httpGet(url);
        }
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }
}
