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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WikimediaConnectorWireMockTest {

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
    public void testFetchResourceWithIndexPhpForm() throws Exception {
        // Expectation: spaces are converted to underscores and action=raw is appended
        server.stubFor(get(urlPathEqualTo("/index.php"))
                .withQueryParam("title", equalTo("Main_Page"))
                .withQueryParam("action", equalTo("raw"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain; charset=utf-8")
                        .withBody("== Hello from MediaWiki ==\nSample content")));

        String remote = "http://localhost:" + server.port() + "/index.php?title=Main Page";
        WikimediaDefaultConnector connector = new WikimediaDefaultConnector();

        try (InputStream is = connector.fetchResource(remote)) {
            assertNotNull(is);
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("== Hello from MediaWiki ==\nSample content", text);
        }
    }

    @Test
    public void testFetchResourceWithRewrittenPathForm() throws Exception {
        // Expectation: last path segment uses underscores and ?action=raw is appended
        server.stubFor(get(urlPathEqualTo("/Wiki/Main_Page"))
                .withQueryParam("action", equalTo("raw"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain; charset=utf-8")
                        .withBody("Rewritten OK")));

        String remote = "http://localhost:" + server.port() + "/Wiki/Main Page";
        WikimediaCleanUrlConnector connector = new WikimediaCleanUrlConnector();

        try (InputStream is = connector.fetchResource(remote)) {
            assertNotNull(is);
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Rewritten OK", text);
        }
    }
}
