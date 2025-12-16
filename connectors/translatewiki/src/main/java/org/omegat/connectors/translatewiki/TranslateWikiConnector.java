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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.core.Core;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.plural.PluralData;
import org.omegat.util.plural.PluralInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A connector for interacting with the TranslateWiki API. This class allows OmegaT to
 * communicate with TranslateWiki for retrieving external resources such as translations
 * or message groups. It provides capabilities to query translations and fetch specific
 * translation resources in JSON format.
 * <p>
 * The connector supports the following capabilities:
 * <ul>
 * <li>Reading translation entries.</li>
 * <li>Listing available resources based on a keyword search.</li>
 * </ul>
 * <p>
 * This class integrates with the OmegaT core by registering itself as an external
 * service connector.
 * <p>
 * Features:
 * <ul>
 * <li>Ability to fetch translation data for a specific group and target language.</li>
 * <li>Automatic handling of authentication for APIs that require login credentials.</li>
 * </ul>
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public class TranslateWikiConnector extends AbstractExternalServiceConnector {

    private static final String CONNECTOR_ID = "translatewiki";
    private static final String PREFERENCE_NAME = "translatewiki";
    private static final String FILE_EXTENSION = "json";

    private static final String USER_KEY = "translatewiki.api.username";
    private static final String PASS_KEY = "translatewiki.api.password";
    private static final String BASE_URL = "https://translatewiki.net/";
    private static final String API_PATH = "w/api.php";

    private static final String EXPORT_ACTION = "action=query&list=messagecollection&format=json";
    private static final String QUERY_LANGUAGE = "mclanguage=";
    private static final String GROUP = "mcgroup=";

    private static final String QUERY_ACTION = "action=translationentitysearch&format=json";
    private static final String QUERY_GROUPS = "&entitytype=groups&limit=50&query=";
    private static final String TRANSLATIONENTITYSEARCH = "translationentitysearch";
    private static final String GROUPS = "groups";

    public static void loadPlugins() {
        Core.registerExternalServiceConnectorClass(TranslateWikiConnector.class);
    }

    public static void unloadPlugins() {
        // do nothing
    }

    private final ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/connectors/translatewiki/Bundle");

    @Override
    public String getId() {
        return CONNECTOR_ID;
    }

    @Override
    public String getName() {
        return bundle.getString("TRANSLATEWIKI_NAME");
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
        return PREFERENCE_NAME;
    }

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public List<ExternalResource> listResources(ServiceTarget target, String keyword)
            throws ConnectorException {
        String queryUrl;
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            queryUrl = target.getBaseUrl() + API_PATH + "?" + QUERY_ACTION + QUERY_GROUPS + encoded;
        } catch (Exception e) {
            throw new ConnectorException(bundle.getString("TRANSLATEWIKI_URL_BUILD_ERROR"), e);
        }

        try {
            String json = HttpConnectionUtils.getURL(new URL(queryUrl));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode messages = root.path(TRANSLATIONENTITYSEARCH).path(GROUPS);

            if (messages == null || !messages.isArray()) {
                return Collections.emptyList();
            }

            List<ExternalResource> result = new ArrayList<>();
            for (JsonNode n : messages) {
                String label = n.path("label").asText(null);
                String group = n.path("group").asText(null);
                if (label == null || label.isEmpty()) {
                    continue;
                }
                if (group == null || group.isEmpty()) {
                    continue;
                }
                // Use pattern as both id and name; path is not used at this
                // stage
                result.add(new ExternalResource(group, label, ""));
            }
            return result;
        } catch (IOException e) {
            throw new ConnectorException(StringUtil.format(bundle.getString("TRANSLATEWIKI_GET_ERROR"), queryUrl), e);
        }
    }

    @Override
    public InputStream fetchResource(ServiceTarget target, String resourceId) throws ConnectorException {
        String po = convertJsonToPo(fetchResourceAsJson(target, resourceId));
        return new ByteArrayInputStream(po.getBytes(StandardCharsets.UTF_8));
    }

    @VisibleForTesting
    String fetchResourceAsJson(ServiceTarget target, String resourceId) throws ConnectorException {
        String url = target.getBaseUrl() + API_PATH + "?" + EXPORT_ACTION + "&" + QUERY_LANGUAGE
                + target.getTargetLanguage() + "&" + GROUP + resourceId;
        return getURL(url, target.isLoginRequired() ? getCredStr() : null, 10000);
    }

    @VisibleForTesting
    String convertJsonToPo(String json) {
        // Convert TranslateWiki JSON payload to a PO formatted string.
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode collection = root.path("query").path("messagecollection");

            if (collection == null || !collection.isArray() || collection.isEmpty()) {
                return "";
            }

            // Determine language and group from the first entry
            JsonNode first = collection.get(0);
            String language = first.path("targetLanguage").asText("");
            String primaryGroup = first.path("primaryGroup").asText("");

            String pluralForms;
            PluralInfo pluralInfo = PluralData.getInstance().getPlural(language);
            if  (pluralInfo != null) {
                pluralForms = pluralInfo.getGettextExpression();
            } else {
                // Generic default
                pluralForms = "PluralForms: nplurals=2; plural=(n != 1);";
            }

            // Timestamp in UTC without colon in offset (e.g., +0000)
            OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssxxxx");
            String timestamp = now.format(fmt);

            StringBuilder sb = new StringBuilder();

            // Header
            sb.append("#\n");
            sb.append("msgid \"\"\n");
            sb.append("msgstr \"\"\n");
            // The sample includes an extra empty quoted line before header fields
            sb.append("\"\"\n");
            appendHeaderLine(sb, "PO-Revision-Date: " + timestamp + "\\n");
            appendHeaderLine(sb, "POT-Creation-Date: " + timestamp + "\\n");
            appendHeaderLine(sb, "Content-Type: text/plain; charset=UTF-8\\n");
            appendHeaderLine(sb, "Content-Transfer-Encoding: 8bit\\n");
            appendHeaderLine(sb, "Language: " + language + "\\n");
            appendHeaderLine(sb, "Project-Id-Version: " + primaryGroup + "\\n");
            appendHeaderLine(sb, "Report-Msgid-Bugs-To: translatewiki.net\\n");
            appendHeaderLine(sb, "X-Translation-Project: translatewiki.net <https://translatewiki.net>\\n");
            appendHeaderLine(sb, "X-Language-Code: " + language + "\\n");
            appendHeaderLine(sb, "X-Message-Group: " + primaryGroup + "\\n");
            appendHeaderLine(sb, pluralForms + "\\n");
            sb.append('\n');

            for (JsonNode n : collection) {
                String key = n.path("key").asText("");
                String definition = n.path("definition").asText("");
                String translation = n.path("translation").asText("");

                sb.append("#\n");
                sb.append("msgctxt \"").append(escapePo(key)).append("\"\n");
                sb.append("msgid \"").append(escapePo(definition)).append("\"\n");
                sb.append("msgstr \"").append(escapePo(translation)).append("\"\n\n");
            }

            return sb.toString();
        } catch (IOException e) {
            // If parsing fails, return original
            return json;
        }
    }

    private static void appendHeaderLine(StringBuilder sb, String content) {
        sb.append('"').append(content).append('"').append('\n');
    }

    private static String escapePo(String s) {
        String out = s.replace("\\", "\\\\").replace("\"", "\\\"");
        out = out.replace("\n", "\\n");
        return out;
    }

    private String getCredStr() throws ConnectorException {
        String userId = getCredential(USER_KEY);
        String password = getCredential(PASS_KEY);
        String credStr;
        if (userId.isEmpty() || password.isEmpty()) {
            String[] cred = askCredentials(bundle.getString("TRANSLATEWIKI_LOGIN_REQUIRED"),
                    bundle.getString("TRANSLATEWIKI_BOTPASSWORD_REQUIRED"));
            if (cred == null || cred.length != 2) {
                throw new ConnectorException(bundle.getString("CREDENTIAL_ERROR"));
            }
            setCredential(USER_KEY, cred[0], false);
            setCredential(PASS_KEY, cred[1], false);
            credStr = cred[0] + ":" + cred[1];
        } else {
            credStr = userId + ":" + password;
        }
        return credStr;
    }
}
