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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.dto.ServiceTarget;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TranslateWikiConnectorTest {

    private WireMockServer server;

    @Before
    public void setUp() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        WireMock.configureFor("localhost", server.port());
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testListResources() throws Exception {
        // Stub translatewiki search API for groups
        server.stubFor(get(urlPathEqualTo("/w/api.php"))
                .withQueryParam("action", equalTo("translationentitysearch"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("entitytype", equalTo("groups"))
                .withQueryParam("limit", equalTo("50"))
                .withQueryParam("query", equalTo("core"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{" +
                                "\"translationentitysearch\": {" +
                                "  \"groups\": [" +
                                "    {\"label\": \"Core messages\", \"group\": \"core\"}," +
                                "    {\"label\": \"Extensions\", \"group\": \"ext\"}" +
                                "  ]" +
                                "}}")));

        String base = "http://localhost:" + server.port() + "/";
        ServiceTarget target = new ServiceTarget("translatewiki", "omegat", base, "en", false);
        TranslateWikiConnector connector = new TranslateWikiConnector();

        List<ExternalResource> resources = connector.listResources(target, "core");
        assertNotNull(resources);
        assertEquals(2, resources.size());

        ExternalResource first = resources.get(0);
        assertEquals("core", first.getId());
        assertEquals("Core messages", first.getName());
        assertEquals("", first.getPath());

        ExternalResource second = resources.get(1);
        assertEquals("ext", second.getId());
        assertEquals("Extensions", second.getName());
    }

    @Test
    public void testFetchResourceAsJson() throws Exception {
        // Load real sample payload from test resources
        byte[] payload;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("payload.json")) {
            assertNotNull("core.json must be on test classpath", is);
            payload = is.readAllBytes();
        }

        // Stub translatewiki export API for messagecollection
        server.stubFor(get(urlPathEqualTo("/w/api.php"))
                .withQueryParam("action", equalTo("query"))
                .withQueryParam("list", equalTo("messagecollection"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("mclanguage", equalTo("ja"))
                .withQueryParam("mcgroup", equalTo("core"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(payload)));

        String base = "http://localhost:" + server.port() + "/";
        ServiceTarget target = new ServiceTarget("translatewiki", "omegat", base, "ja", false);
        TranslateWikiConnector connector = new TranslateWikiConnector();

        String json = connector.fetchResourceAsJson(target, "core");
        byte[] received = json.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(payload, received);
        // quick sanity check for JSON start
        String head = new String(received, 0, Math.min(received.length, 20), StandardCharsets.UTF_8).trim();
        assertTrue(head.startsWith("{") || head.startsWith("[{"));
    }

    @Test
    public void testFetchResource() throws Exception {
        byte[] payload;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("payload.json")) {
            assertNotNull("core.json must be on test classpath", is);
            payload = is.readAllBytes();
        }

        // Stub translatewiki export API for messagecollection
        server.stubFor(get(urlPathEqualTo("/w/api.php"))
                .withQueryParam("action", equalTo("query"))
                .withQueryParam("list", equalTo("messagecollection"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("mclanguage", equalTo("ja"))
                .withQueryParam("mcgroup", equalTo("core"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(payload)));

        String base = "http://localhost:" + server.port() + "/";
        ServiceTarget target = new ServiceTarget("translatewiki", "omegat", base, "ja", false);
        TranslateWikiConnector connector = new TranslateWikiConnector();

        try (InputStream is = connector.fetchResource(target, "core")) {
            assertNotNull(is);
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("\"X-Translation-Project: translatewiki.net <https://translatewiki.net>\\n\""));
            assertTrue(text.contains("\"X-Language-Code: ja\\n\""));
            //
            assertTrue(text.contains("msgctxt \"date.formats.heatmap\""));
            assertTrue(text.contains("msgid \"Choose file\""));
        }
    }

    @Test
    public void testConvertJsonToPo() {
        // Test case 1: Valid JSON payload
        String jsonPayload = "{" +
                "\"query\": {" +
                "\"messagecollection\": [" +
                "{" +
                "\"key\": \"welcome.key\"," +
                "\"definition\": \"Welcome to TranslateWiki!\"," +
                "\"translation\": \"ようこそ TranslateWiki へ！\"," +
                "\"targetLanguage\": \"ja\"," +
                "\"primaryGroup\": \"core\"" +
                "}" +
                "]}}";

        TranslateWikiConnector connector = new TranslateWikiConnector();
        String po = connector.convertJsonToPo(jsonPayload);

        // Assert the output contains key elements
        assertTrue(po.contains("msgctxt \"welcome.key\""));
        assertTrue(po.contains("msgid \"Welcome to TranslateWiki!\""));
        assertTrue(po.contains("msgstr \"ようこそ TranslateWiki へ！\""));
        assertTrue(po.contains("Language: ja"));
        assertTrue(po.contains("Project-Id-Version: core"));

        // Test case 2: Empty JSON
        String emptyJsonPayload = "{}";

        String result = connector.convertJsonToPo(emptyJsonPayload);
        assertEquals("", result);

        // Test case 3: Missing fields
        String incompleteJsonPayload = "{" +
                "\"query\": {" +
                "\"messagecollection\": [" +
                "{" +
                "\"key\": \"incomplete.key\"," +
                "\"definition\": \"Incomplete entry\"," +
                "\"translation\": \"\"" +
                "}" +
                "]}}";

        String poOutput = connector.convertJsonToPo(incompleteJsonPayload);

        // Verify the partial output for valid fields
        assertTrue(poOutput.contains("msgid \"Incomplete entry\""));
        assertTrue(poOutput.contains("msgstr \"\""));
    }
}
