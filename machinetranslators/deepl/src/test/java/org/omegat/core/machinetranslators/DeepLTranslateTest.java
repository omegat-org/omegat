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
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class DeepLTranslateTest extends TestCore {

    @Test
    public void testGetJsonResults() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_DEEPL_TRANSLATE, true);
        DeepLTranslate deepLTranslate = new DeepLTranslate();
        String json = "{ \"translations\": [ { \"detected_source_language\": \"DE\", \"text\": \"Hello World!\" } ] }";
        String result = deepLTranslate.getJsonResults(json);
        assertEquals("Hello World!", result);
    }

    @Test
    public void testGetJsonResultsWithWrongJson() {
        Preferences.setPreference(Preferences.ALLOW_DEEPL_TRANSLATE, true);
        DeepLTranslate deepLTranslate = new DeepLTranslate();
        String json = "{ \"response\": \"failed\" }";
        assertThrows(Exception.class, () -> { deepLTranslate.getJsonResults(json); });
    }

    @Test
    public void testResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_DEEPL_TRANSLATE, true);
        String key = "deepl8api8key";

        Map<String, StringValuePattern> params = new HashMap<>();
        params.put("text", WireMock.equalTo("source text"));
        params.put("source_lang", WireMock.equalTo("DE"));
        params.put("target_lang", WireMock.equalTo("EN"));
        params.put("tag_handling", WireMock.equalTo("xml"));
        params.put("auth_key", WireMock.matching("\\w+"));
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/translate"))
                .withQueryParams(params)
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"translations\":[ "
                                + "{ \"detected_source_language\": \"DE\", \"text\": \"Hello World!\" }"
                                + " ] }")));
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d", port);
        String sourceText = "source text";
        DeepLTranslate deepLTranslate = new DeepLTranslate(url, key);
        String result = deepLTranslate.translate(new Language("DE"), new Language("EN"), sourceText);
        assertEquals("Hello World!", result);
    }
}
