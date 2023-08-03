/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021 Hiroshi Miura
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
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.Test;

import org.omegat.core.TestCoreWireMock;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class ApertiumTranslateTest extends TestCoreWireMock {

    private static final String JSON = "{\"responseData\": "
            + "{\"translatedText\": \"Abc\"}, "
            + "\"responseDetails\": null, "
            + "\"responseStatus\": 200}";
    private static final int HTTP_OK = 200;

    @Test
    public void testGetJsonResults() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_APERTIUM_TRANSLATE, true);
        ApertiumTranslate apertiumTranslate = new ApertiumTranslate();
        String result = apertiumTranslate.getJsonResults(JSON);
        assertEquals("Abc", result);
    }

    @Test
    public void testResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_APERTIUM_TRANSLATE, true);
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d", port);
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_CUSTOM, "true");
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_URL, url);
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_KEY, "abcdefg");

        Map<String, StringValuePattern> params = new HashMap<>();
        params.put("q", WireMock.equalTo("source text"));
        params.put("langpair", WireMock.equalTo("en|de"));
        params.put("key", WireMock.matching("\\w+"));
        params.put("markUnknown", WireMock.equalTo("no"));
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/translate"))
                .withQueryParams(params)
                .willReturn(WireMock.aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(JSON)
                )
        );
        ApertiumTranslate apertiumTranslate = new ApertiumTranslate();
        String result = apertiumTranslate.translate(new Language("EN"), new Language("DE"), "source text");
        assertEquals("Abc", result);
    }

    @Test
    public void testErrorResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_APERTIUM_TRANSLATE, true);
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d", port);
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_CUSTOM, "true");
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_URL, url);
        System.setProperty(ApertiumTranslate.PROPERTY_APERTIUM_SERVER_KEY, "abcdefg");

        String errorResponse = "{\"responseStatus\": 400,"
                + " \"responseDetails\": \"That pair is invalid, use e.g. eng|spa\"}\"";

        Map<String, StringValuePattern> params = new HashMap<>();
        params.put("q", WireMock.equalTo("This    works well?"));
        params.put("langpair", WireMock.equalTo("en|es"));
        params.put("key", WireMock.matching("\\w+"));
        params.put("markUnknown", WireMock.equalTo("no"));
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/translate"))
                .withQueryParams(params)
                .willReturn(WireMock.aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponse)
                )
        );
        ApertiumTranslate apertiumTranslate = new ApertiumTranslate();
        assertThrows(MachineTranslateError.class, () -> {
            apertiumTranslate.translate(new Language("EN"), new Language("ES"), "This    works well?");
        });
    }

}
