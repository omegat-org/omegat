/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022,2023 Hiroshi Miura
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

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class YandexCloudTranslateTest extends TestCore {

    @Test
    public void getJsonResults() throws MachineTranslateError {
        YandexCloudTranslate yandexCloudTranslate = new YandexCloudTranslate();
        String json = "{\"translations\": [{\"text\": \"translated text goes here.\" }]}";
        String translation = yandexCloudTranslate.extractTranslation(json);
        assertEquals("translated text goes here.", translation);
    }

    @Test
    public void createJsonRequest() throws JsonProcessingException {
        Preferences.setPreference("yandex.cloud.keep-tags", true);
        Preferences.setPreference("yandex.cloud.use-glossary", false);
        YandexCloudTranslate yandexCloudTranslate = new YandexCloudTranslate();
        Language sLang = new Language("EN");
        Language tLang = new Language("FR");
        String trText = "Translation text.";
        String folderId = "ID";
        String json = yandexCloudTranslate.createJsonRequest(sLang, tLang, trText, folderId);
        ObjectMapper mapper = new ObjectMapper();
        String expected = "{\"folderId\":\"ID\",\"format\":\"HTML\",\"sourceLanguageCode\":\"en\","
                + "\"targetLanguageCode\":\"fr\",\"texts\":[\"Translation text.\"]}";
        assertEquals(mapper.readTree(expected), mapper.readTree(json));
    }

    @Test
    public void createGlossaryConfigPartTest() throws JsonProcessingException {
        YandexCloudTranslate yandexCloudTranslate = new YandexCloudTranslate();
        Map<String, Object> params = new TreeMap<>();
        Map<String, String> glossaryTerms = new TreeMap<>();
        glossaryTerms.put("source1", "translation1");
        glossaryTerms.put("source2", "translation2");
        params.put("glossaryConfig", yandexCloudTranslate.createGlossaryConfigPart(glossaryTerms));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(params);
        String expected = "{\"glossaryConfig\":{\"glossaryData\":{\"glossaryPairs\":"
                + "["
                + "{\"sourceText\":\"source1\",\"translatedText\":\"translation1\"},"
                + "{\"sourceText\":\"source2\",\"translatedText\":\"translation2\"}"
                + "]"
                + "}}}";
        assertEquals(mapper.readTree(expected), mapper.readTree(json));
    }
}
