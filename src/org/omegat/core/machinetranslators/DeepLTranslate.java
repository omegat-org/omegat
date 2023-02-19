/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
               2021,2023 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.machinetranslators;

import java.awt.Window;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.omegat.core.Core;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Support of DeepL machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 *
 * @see <a href="https://www.deepl.com/api.html">Translation API</a>
 */
public class DeepLTranslate extends BaseCachedTranslate {
    protected static final String PROPERTY_API_KEY = "deepl.api.key";
    // DO NOT MOVE TO THE V2 API until it becomes available for CAT tool
    // integration.
    //
    // > Version 2 (v2) of the DeepL API is not compatible with CAT tools and is
    // > not included in DeepL plans for CAT tool users.
    //
    // See https://www.deepl.com/docs-api/accessing-the-api/api-versions/
    protected static final String DEEPL_URL = "https://api.deepl.com/v1/translate";
    private final static int MAX_TEXT_LENGTH = 5000;

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_DEEPL_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_DEEPL");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH - 3) + "..." :
                text;
        String prev = getFromCache(sLang, tLang, trText);
        if (prev != null) {
            return prev;
        }

        String apiKey = getCredential(PROPERTY_API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception(OStrings.getString("DEEPL_API_KEY_NOTFOUND"));
        }

        Map<String, String> params = new TreeMap<>();

        // No check is done, but only "EN", "DE", "FR", "ES", "IT", "NL", "PL"
        // are supported right now.

        params.put("text", trText);
        params.put("source_lang", sLang.getLanguageCode().toUpperCase());
        params.put("target_lang", tLang.getLanguageCode().toUpperCase());
        params.put("tag_handling", "xml");
        // Check if the project segmentation is done by sentence
        String splitSentence = Core.getProject().getProjectProperties().isSentenceSegmentingEnabled() ? "1"
                : "0";
        params.put("split_sentences", splitSentence);
        params.put("preserve_formatting", "1");
        params.put("auth_key", apiKey);

        Map<String, String> headers = new TreeMap<>();

        String v = HttpConnectionUtils.get(DEEPL_URL, params, headers, "UTF-8");
        String tr = getJsonResults(v);
        if (tr == null) {
            return null;
        }
        tr = unescapeHTML(tr);
        tr = cleanSpacesAroundTags(tr, trText);
        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    /**
     * Parse API response and return translated text.
     * 
     * @param json
     *            API response json string.
     * @return translation, or null when API returns empty result, or error
     *         message when parse failed.
     */
    @SuppressWarnings("unchecked")
    protected String getJsonResults(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // { "translations": [ { "detected_source_language": "DE", "text":
            // "Hello World!" } ] }
            JsonNode rootNode = mapper.readTree(json);
            JsonNode translations = rootNode.get("translations");
            if (translations.has(0)) {
                return translations.get(0).get("text").asText();
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return OStrings.getString("MT_JSON_ERROR");
        }
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String key = panel.valueField1.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_API_KEY, key, temporary);
            }
        };

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_DEEPL_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));

        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_KEY));

        dialog.show();
    }
}
