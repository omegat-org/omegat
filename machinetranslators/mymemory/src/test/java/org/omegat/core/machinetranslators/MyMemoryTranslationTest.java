/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.Test;

import org.omegat.core.TestCoreWireMock;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class MyMemoryTranslationTest extends TestCoreWireMock {

    private static final int HTTP_OK = 200;

    @Test
    public void testMMMTResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_MYMEMORY_MACHINE_TRANSLATE, true);

        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/get", port);
        System.setProperty(AbstractMyMemoryTranslate.MYMEMORY_API_KEY, "apikey");
        System.setProperty(AbstractMyMemoryTranslate.MYMEMORY_API_EMAIL, "api@mail");

        String sourceText = "Hello, how are you today?";
        Map<String, StringValuePattern> params = new HashMap<>();
        params.put("q", WireMock.equalTo("Hello World!"));
        params.put("of", WireMock.equalTo("json"));
        params.put("langpair", WireMock.equalTo("en|it"));
        params.put("mt", WireMock.equalTo("1"));
        params.put("key", WireMock.equalTo("apikey"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/get"))
                .withQueryParams(params)
                .willReturn(WireMock.aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"responseData\":"
                                + "{\"translatedText\":\"Ciao Mondo!\",\"match\":1},"
                                + "\"quotaFinished\":false,"
                                + "\"mtLangSupported\":null,"
                                + "\"responseDetails\":\"\","
                                + "\"responseStatus\":200,"
                                + "\"responderId\":null,"
                                + "\"exception_code\":null,"
                                + "\"matches\":"
                                + "["
                                + "{\"id\":\"708671558\",\"segment\":\"Hello World!\","
                                + "\"translation\":\"Ciao Mondo!\",\"source\":\"en-GB\",\"target\":\"it-IT\","
                                + "\"quality\":\"74\",\"reference\":null,\"usage-count\":95,"
                                + "\"subject\":\"All\",\"created-by\":\"MateCat\","
                                + "\"last-updated-by\":\"MateCat\","
                                + "\"create-date\":\"2023-02-04 09:39:47\","
                                + "\"last-update-date\":\"2023-02-04 09:39:47\","
                                + "\"match\":1"
                                + "}]"
                                + "}")
                ));

        MyMemoryMachineTranslate myMemoryMachineTranslate = new MyMemoryMachineTranslate(url);
        String result = myMemoryMachineTranslate.translate(new Language("EN"), new Language("IT"),
                "Hello World!");
        assertEquals("Ciao Mondo!", result);
    }

    @Test
    public void testMMHTResponse() throws Exception {
        Preferences.setPreference(Preferences.ALLOW_MYMEMORY_MACHINE_TRANSLATE, true);

        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/get", port);
        System.setProperty(AbstractMyMemoryTranslate.MYMEMORY_API_KEY, "apikey");
        System.setProperty(AbstractMyMemoryTranslate.MYMEMORY_API_EMAIL, "api@mail");

        String sourceText = "Hello, how are you today?";
        Map<String, StringValuePattern> params = new HashMap<>();
        params.put("q", WireMock.equalTo("Hello World!"));
        params.put("of", WireMock.equalTo("json"));
        params.put("langpair", WireMock.equalTo("en|it"));
        params.put("mt", WireMock.equalTo("0"));
        params.put("key", WireMock.equalTo("apikey"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/get"))
                .withQueryParams(params)
                .willReturn(WireMock.aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"responseData\":"
                                + "{\"translatedText\":\"Ciao Mondo!\",\"match\":1},"
                                + "\"quotaFinished\":false,"
                                + "\"mtLangSupported\":null,"
                                + "\"responseDetails\":\"\","
                                + "\"responseStatus\":200,"
                                + "\"responderId\":null,"
                                + "\"exception_code\":null,"
                                + "\"matches\":"
                                + "["
                                + "{\"id\":\"708671558\",\"segment\":\"Hello World!\","
                                + "\"translation\":\"Ciao Mondo!\",\"source\":\"en-GB\",\"target\":\"it-IT\","
                                + "\"quality\":\"74\",\"reference\":null,\"usage-count\":95,"
                                + "\"subject\":\"All\",\"created-by\":\"MateCat\","
                                + "\"last-updated-by\":\"MateCat\","
                                + "\"create-date\":\"2023-02-04 09:39:47\","
                                + "\"last-update-date\":\"2023-02-04 09:39:47\","
                                + "\"match\":1"
                                + "}]"
                                + "}")
                ));

        MyMemoryHumanTranslate myMemoryMachineTranslate = new MyMemoryHumanTranslate(url);
        String result = myMemoryMachineTranslate.translate(new Language("EN"), new Language("IT"),
                "Hello World!");
        assertEquals("Ciao Mondo!", result);
    }
}
