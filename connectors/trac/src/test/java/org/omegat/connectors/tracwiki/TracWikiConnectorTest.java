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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.connectors.dto.ServiceTarget;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TracWikiConnectorTest {

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
    public void testFetchResourceWithServiceTarget() throws Exception {
        // Trac edit page: /wiki/PageName?action=edit returns HTML containing <textarea name="text">raw</textarea>
        server.stubFor(get(urlPathEqualTo("/wiki/SamplePage"))
                .withQueryParam("action", equalTo("edit"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html; charset=utf-8")
                        .withBody("<html><body><form><textarea name=\"text\">Hello Trac</textarea></form></body></html>")));

        String base = "http://localhost:" + server.port() + "/wiki";
        ServiceTarget target = new ServiceTarget("tracwiki", "project", base, "");
        TracWikiConnector connector = new TracWikiConnector();

        try (InputStream is = connector.fetchResource(target, "SamplePage")) {
            assertNotNull(is);
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Hello Trac", text);
        }
    }

    @Test
    public void testFetchResourceWithStringUrl() throws Exception {
        // Given an incoming URL like http://host/project/wiki/PageName,
        // connector should request ?action=edit and extract textarea content
        server.stubFor(get(urlPathEqualTo("/project/wiki/AnotherPage"))
                .withQueryParam("action", equalTo("edit"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html; charset=utf-8")
                        .withBody("<html><body><textarea id=\"text\">Raw content</textarea></body></html>")));

        String remote = "http://localhost:" + server.port() + "/project/wiki/AnotherPage";
        TracWikiConnector connector = new TracWikiConnector();

        try (InputStream is = connector.fetchResource(remote)) {
            assertNotNull(is);
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Raw content", text);
        }
    }
}
