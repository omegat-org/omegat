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

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class Google2TranslateTest extends TestCore {

    private static final String json = "{\n"
            + "  \"data\": {\n"
            + "    \"translations\": [\n"
            + "      {\n"
            + "        \"translatedText\": \"Hallo Welt\",\n"
            + "        \"detectedSourceLanguage\": \"en\"\n"
            + "      },\n"
            + "      {\n"
            + "        \"translatedText\": \"Mein Name ist Jeff\",\n"
            + "        \"detectedSourceLanguage\": \"en\"\n"
            + "      }\n"
            + "    ]\n"
            + "  }\n"
            + "}";

    @Test
    public void testGetJsonResults() throws MachineTranslateError {
        Preferences.setPreference(Preferences.ALLOW_GOOGLE2_TRANSLATE, true);
        Google2Translate google2Translate = new Google2Translate();
        String translation = google2Translate.getJsonResults(json);
        assertEquals("Hallo Welt", translation);
    }

    @Test
    public void testResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_GOOGLE2_TRANSLATE, true);
        String key = "google8api8key";
        String sourceText = "source text";
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d", port);

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/language/translate/v2"))
                        .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(WireMock.and(
                                WireMock.containing("q=source+text"),
                                WireMock.containing("source=en"),
                                WireMock.containing("target=de")
                        ))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(json)
                )
        );
        Google2Translate google2Translate = new Google2Translate(url, key);
        String result = google2Translate.translate(new Language("EN"), new Language("DE"), sourceText);
        assertEquals("Hallo Welt", result);
    }
}
