/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021,2023 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.machinetranslators;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;

import org.omegat.core.TestCoreWireMock;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class IBMWatsonTranslateTest extends TestCoreWireMock {

    @Test
    public void getJsonResults() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_IBMWATSON_TRANSLATE, true);
        IBMWatsonTranslate ibmWatsonTranslate = new IBMWatsonTranslate();
        String json = "{\"translations\": ["
                + "  {\"translation\": \"translated text goes here.\" }"
                + "],"
                + " \"word_count\": 4, \"character_count\": 53 }";
        String translation = ibmWatsonTranslate.getJsonResults(json);
        assertEquals("translated text goes here.", translation);
    }

    @Test
    public void createJsonRequest() throws JsonProcessingException {
        System.setProperty(IBMWatsonTranslate.PROPERTY_MODEL, "MODEL");
        IBMWatsonTranslate ibmWatsonTranslate = new IBMWatsonTranslate();
        Language sLang = new Language("EN");
        Language tLang = new Language("FR");
        String trText = "Translation text.";
        String json = ibmWatsonTranslate.createJsonRequest(sLang, tLang, trText);
        ObjectMapper mapper = new ObjectMapper();
        String expected = "{\"model_id\":\"MODEL\",\"source\":\"EN\","
                + "\"target\":\"FR\",\"text\":[\"Translation text.\"]}";
        assertEquals(mapper.readTree(expected), mapper.readTree(json));
    }

    @Test
    public void testResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_IBMWATSON_TRANSLATE, true);

        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/language-translator/api/v3/translate", port);
        System.setProperty(IBMWatsonTranslate.PROPERTY_URL, url);
        System.setProperty(IBMWatsonTranslate.PROPERTY_LOGIN, "watson4api8key");

        String sourceText = "Hello, how are you today?";

        WireMock.stubFor(WireMock.post(WireMock.anyUrl())   //WireMock.urlPathEqualTo
                // ("/language-translator/api/v3/translate"))
                        .withQueryParam("version", WireMock.equalTo("2018-05-01"))
                        .withRequestBody(WireMock.equalToJson(
                                "{\"source\":\"EN\",\"target\":\"ES\",\"text\":[\"" + sourceText + "\"]}"))
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .withHeader("Authorization", WireMock.containing("Basic "))
                        .withHeader("X-Watson-Learning-Opt-Out", WireMock.equalTo("true"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n"
                                + "  \"translations\": [\n"
                                + "    {\n"
                                + "      \"translation\": \"Hola, \u00BFc\u00F3mo est\u00E1s hoy?\"\n"
                                + "    }\n"
                                + "  ],\n"
                                + "  \"word_count\": 7,\n"
                                + "  \"character_count\": 25\n"
                                + "}")));

        IBMWatsonTranslate ibmWatsonTranslate = new IBMWatsonTranslate();
        String result = ibmWatsonTranslate.translate(new Language("EN"), new Language("ES"), sourceText);
        assertEquals("Hola, \u00BFc\u00F3mo est\u00E1s hoy?", result);
    }
}