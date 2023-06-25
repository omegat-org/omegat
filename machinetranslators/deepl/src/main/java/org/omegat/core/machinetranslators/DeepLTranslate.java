/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2010 Alex Buloichik, Didier Briel
 *                2011 Briac Pilpre, Alex Buloichik
 *                2013 Didier Briel
 *                2016 Aaron Madlon-Kay
 *                2021,2023 Hiroshi Miura
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

import java.awt.Window;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.TranslationException;
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
    private String temporaryKey = null;

    protected static final String DEEPL_V1_URL = "https://api.deepl.com/v1/translate";

    // DO NOT MOVE TO THE V2 API until it becomes available for CAT tool
    // integration.
    //
    // > Version 2 (v2) of the DeepL API is not compatible with CAT tools and is
    // > not included in DeepL plans for CAT tool users.
    //
    // See https://www.deepl.com/docs-api/accessing-the-api/api-versions/
    protected static final String DEEPL_PATH = "/v1/translate";
    protected final String deepLUrl;

    private final static int MAX_TEXT_BYTES = 128 * 1024; // Max limit is 128KiB
    protected static final String DEEPL_URL = "https://api.deepl.com/v1/translate";
    // See https://support.deepl.com/hc/en-us/articles/4405712799250-Character-count-for-translation-within
    // -applications
    // max application limit is 5000 characters.
    private final static int MAX_TEXT_LENGTH = 5000;

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(DeepLTranslate.class);
    }

    public static void unloadPlugins() {
    }

    public DeepLTranslate() {
        deepLUrl = DEEPL_V1_URL;
    }

    /**
     * Constructor for tests.
     * @param baseUrl custom base url
     * @param key temporary api key
     */
    public DeepLTranslate(String baseUrl, String key) {
        deepLUrl = baseUrl + DEEPL_PATH;
        temporaryKey = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_DEEPL_TRANSLATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_DEEPL");
    }

    @Override
    protected int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String apiKey = getCredential(PROPERTY_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            if (temporaryKey == null) {
                throw new MachineTranslateError(OStrings.getString("DEEPL_API_KEY_NOTFOUND"));
            }
            apiKey = temporaryKey;
        }

        Map<String, String> params = new TreeMap<>();

        // No check is done, but only "EN", "DE", "FR", "ES", "IT", "NL", "PL"
        // are supported right now.

        params.put("text", text);
        params.put("source_lang", sLang.getLanguageCode().toUpperCase());
        params.put("target_lang", tLang.getLanguageCode().toUpperCase());
        params.put("tag_handling", "xml");
        // Check if the project segmentation is done by sentence
        ProjectProperties projectProperties = Core.getProject().getProjectProperties();
        String splitSentence; // can be null when testing
        if (projectProperties != null && projectProperties.isSentenceSegmentingEnabled()) {
            splitSentence = "1";
        } else {
            splitSentence = "0";
        }
        params.put("split_sentences", splitSentence);
        params.put("preserve_formatting", "1");
        params.put("auth_key", apiKey);

        Map<String, String> headers = new TreeMap<>();

        String v = HttpConnectionUtils.get(deepLUrl, params, headers, "UTF-8");
        String tr = getJsonResults(v);
        if (tr == null) {
            return null;
        }
        tr = BaseTranslate.unescapeHTML(tr);
        return cleanSpacesAroundTags(tr, text);
    }

    /**
     * Parse API response and return translated text.
     * @param json
     *            API response json string.
     * @return translation, or null when API returns empty result, or error
     *         message when parse failed.
     */
    protected String getJsonResults(String json) throws TranslationException, MachineTranslateError {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // { "translations": [ { "detected_source_language": "DE", "text":
            // "Hello World!" } ] }
            JsonNode rootNode = mapper.readTree(json);
            JsonNode translations = rootNode.get("translations");
            if (translations == null) {
                Log.logErrorRB("MT_JSON_ERROR");
                throw new MachineTranslateError(OStrings.getString("MT_JSON_ERROR"));
            }
            if (translations.has(0)) {
                JsonNode textNode = translations.get(0).get("text");
                if (textNode == null) {
                    Log.logErrorRB("MT_JSON_ERROR");
                    throw new MachineTranslateError(OStrings.getString("MT_JSON_ERROR"));
                }
                return translations.get(0).get("text").asText();
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            throw new MachineTranslateError(OStrings.getString("MT_JSON_ERROR"));
        }
        Log.logErrorRB( "MT_JSON_ERROR");
        throw new MachineTranslateError(OStrings.getString("MT_JSON_ERROR"));
    }

    /**
     * Engine is configurable.
     *
     * @return true
     */
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
