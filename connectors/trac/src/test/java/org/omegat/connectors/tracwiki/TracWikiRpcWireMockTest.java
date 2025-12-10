/**************************************************************************
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2025 Hiroshi Miura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.connectors.tracwiki;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TracWikiRpcWireMockTest {

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
    public void testGetAllPagesAndGetPage() throws Exception {
        // Stub XML-RPC response for wiki.getAllPages
        String allPagesResponse =
                "<?xml version=\"1.0\"?>" +
                "<methodResponse>" +
                "  <params><param><value>" +
                "    <array><data>" +
                "      <value><string>WikiStart</string></value>" +
                "      <value><string>SandBox</string></value>" +
                "    </data></array>" +
                "  </value></param></params>" +
                "</methodResponse>";

        server.stubFor(post(urlEqualTo("/rpc"))
                .withRequestBody(containing("<methodName>wiki.getAllPages</methodName>"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody(allPagesResponse)));

        // Stub XML-RPC response for wiki.getPage("WikiStart")
        String pageResponse =
                "<?xml version=\"1.0\"?>" +
                "<methodResponse>" +
                "  <params><param><value><string>= Welcome to Trac =</string></value></param></params>" +
                "</methodResponse>";

        server.stubFor(post(urlEqualTo("/rpc"))
                .withRequestBody(containing("<methodName>wiki.getPage</methodName>"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody(pageResponse)));

        String endpoint = "http://localhost:" + server.port() + "/rpc";
        TracWikiRpc rpc = new TracWikiRpc(endpoint, "", "");

        List<String> pages = rpc.getAllPages();
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertEquals("WikiStart", pages.get(0));
        assertEquals("SandBox", pages.get(1));

        String pageText = rpc.getPage("WikiStart");
        assertEquals("= Welcome to Trac =", pageText);
    }
}
