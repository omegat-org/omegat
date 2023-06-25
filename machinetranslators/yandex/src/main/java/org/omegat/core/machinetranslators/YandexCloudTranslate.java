/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
               2020 Lev Abashkin
               2023 Hiroshi Miura
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

package org.omegat.core.machinetranslators;

import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.fasterxml.jackson.core.JsonProcessingException;
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
 * Support of Yandex.Cloud machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 * @author Lev Abashkin
 *
 * @see <a href=
 *      "https://cloud.yandex.com/docs/translate/api-ref/Translation/">Translation
 *      API</a>
 */
public class YandexCloudTranslate extends BaseCachedTranslate {

    private static final String PROPERTY_OAUTH_TOKEN = "yandex.cloud.oauth-token";
    private static final String PROPERTY_FOLDER_ID = "yandex.cloud.folder-id";
    private static final String PROPERTY_USE_GLOSSARY = "yandex.cloud.use-glossary";
    private static final String PROPERTY_KEEP_TAGS = "yandex.cloud.keep-tags";

    private static final int MAX_GLOSSARY_TERMS = 50;

    // API limit
    // see https://cloud.yandex.com/en/docs/translate/concepts/limits
    private static final int MAX_TEXT_LENGTH = 10000;
    private static final int IAM_TOKEN_TTL_SECONDS = 3600; // Recommended value

    private static final String IAM_TOKEN_URL = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
    private static final String TRANSLATE_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";

    private String IAMErrorMessage = null;
    private String cachedIAMToken = null;
    private long lastIAMTokenTime = 0L;

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(YandexCloudTranslate.class);
    }

    public static void unloadPlugins() {
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_YANDEX_CLOUD");
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    protected int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    @Override
    protected String translate(final Language sLang, final Language tLang, final String text)
            throws Exception {
        String oAuthToken = getCredential(PROPERTY_OAUTH_TOKEN);
        if (oAuthToken == null || oAuthToken.isEmpty()) {
            throw new Exception(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_OAUTH_TOKEN_NOT_FOUND"));
        }

        String folderId = getCredential(PROPERTY_FOLDER_ID);
        if (folderId == null || folderId.isEmpty()) {
            throw new Exception(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_FOLDER_ID_NOT_FOUND"));
        }

        String IAMToken = getIAMToken(oAuthToken);
        if (IAMToken == null) {
            throw new Exception(IAMErrorMessage);
        }

        String request = createJsonRequest(sLang, tLang, text, folderId);

        Map<String, String> headers = new TreeMap<>();
        headers.put("Authorization", "Bearer " + IAMToken);

        String response;
        try {
            response = HttpConnectionUtils.postJSON(TRANSLATE_URL, request, headers);
        } catch (HttpConnectionUtils.ResponseError e) {
            String errorMessage = extractErrorMessage(e.body);
            if (errorMessage == null) {
                errorMessage = OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_TRANSLATE_RESPONSE");
                throw new MachineTranslateError(errorMessage);
            }
            throw new MachineTranslateError(e.getMessage());
        }
        if (response == null) {
            return null;
        }
        String tr = extractTranslation(response);
        if (tr == null) {
            return null;
        }
        return cleanSpacesAroundTags(tr, text);
    }

    @Override
    public void showConfigurationUI(Window parent) {

        JPanel extraPanel = new JPanel();
        extraPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.Y_AXIS));
        extraPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        JCheckBox glossaryCheckBox = new JCheckBox(
                OStrings.getString("MT_ENGINE_YANDEX_CLOUD_GLOSSARY_CHECKBOX"));
        extraPanel.add(glossaryCheckBox);
        JCheckBox keepTagsCheckBox = new JCheckBox(
                OStrings.getString("MT_ENGINE_YANDEX_CLOUD_KEEP_TAGS_CHECKBOX"));
        extraPanel.add(keepTagsCheckBox);

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();

                String folderId = panel.valueField1.getText().trim();
                setCredential(PROPERTY_FOLDER_ID, folderId, temporary);

                String oAuthToken = panel.valueField2.getText().trim();
                setCredential(PROPERTY_OAUTH_TOKEN, oAuthToken, temporary);

                Preferences.setPreference(PROPERTY_USE_GLOSSARY, glossaryCheckBox.isSelected());
                Preferences.setPreference(PROPERTY_KEEP_TAGS, keepTagsCheckBox.isSelected());
            }
        };

        dialog.panel.itemsPanel.add(extraPanel);

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_FOLDER_ID_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_FOLDER_ID));

        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_OAUTH_TOKEN_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_OAUTH_TOKEN));

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_OAUTH_TOKEN));

        glossaryCheckBox.setSelected(Preferences.isPreferenceDefault(PROPERTY_USE_GLOSSARY, false));
        keepTagsCheckBox.setSelected(Preferences.isPreferenceDefault(PROPERTY_KEEP_TAGS, true));

        dialog.show();
    }

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_YANDEX_CLOUD_TRANSLATE;
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(final String json) {
        JsonNode rootNode;
        ObjectMapper mapper = new ObjectMapper();
        try {
            rootNode = mapper.readTree(json);
            return rootNode.get("message").asText();
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_ENGINE_YANDEX_CLOUD_BAD_ERROR_REPORT");
            return null;
        }
    }

    protected String createJsonRequest(final Language sLang, final Language tLang, final String trText,
            final String folderId) throws JsonProcessingException {
        Map<String, Object> params = new TreeMap<>();
        params.put("sourceLanguageCode", sLang.getLanguageCode().toLowerCase());
        params.put("targetLanguageCode", tLang.getLanguageCode().toLowerCase());
        params.put("folderId", folderId);
        if (Preferences.isPreference(PROPERTY_KEEP_TAGS)) {
            params.put("format", "HTML");
        }
        if (Preferences.isPreference(PROPERTY_USE_GLOSSARY)) {
            Map<String, String> glossaryTerms = glossarySupplier.get();
            if (!glossaryTerms.isEmpty()) {
                params.put("glossaryConfig", createGlossaryConfigPart(glossaryTerms));
            }
        }
        params.put("texts", Collections.singletonList(trText));
        return new ObjectMapper().writeValueAsString(params);
    }

    @SuppressWarnings("unchecked")
    protected String extractTranslation(final String json) throws MachineTranslateError {
        JsonNode rootNode;
        ObjectMapper mapper = new ObjectMapper();
        try {
            rootNode = mapper.readTree(json);
            return rootNode.get("translations").get(0).get("text").asText();
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            throw new MachineTranslateError(OStrings.getString(
                    "MT_ENGINE_YANDEX_CLOUD_BAD_TRANSLATE_RESPONSE"));
        }
    }

    @SuppressWarnings("unchecked")
    private String getIAMToken(final String oAuthToken) {
        if (System.currentTimeMillis() - lastIAMTokenTime > IAM_TOKEN_TTL_SECONDS * 1_000) {

            String request = "{\"yandexPassportOauthToken\":\"" + oAuthToken + "\"}";
            String response;
            JsonNode rootNode;
            ObjectMapper mapper = new ObjectMapper();

            try {
                response = HttpConnectionUtils.postJSON(IAM_TOKEN_URL, request, null);
            } catch (HttpConnectionUtils.ResponseError e) {
                // Try to extract error message from the error body
                IAMErrorMessage = extractErrorMessage(e.body);
                if (IAMErrorMessage == null) {
                    IAMErrorMessage = OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_IAM_RESPONSE");
                }
                return null;
            } catch (IOException e) {
                IAMErrorMessage = e.getLocalizedMessage();
                return null;
            }

            try {
                rootNode = mapper.readTree(response);
                cachedIAMToken = rootNode.get("iamToken").asText();
                lastIAMTokenTime = System.currentTimeMillis();
            } catch (Exception e) {
                Log.logErrorRB(e, "MT_ENGINE_YANDEX_CLOUD_BAD_IAM_RESPONSE");
                IAMErrorMessage = OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_IAM_RESPONSE");
                return null;
            }
        }
        return cachedIAMToken;
    }

    /**
     * create glossary config part of request json. we make visibility to
     * protected for test purpose.
     * 
     * @param glossaryTerms
     *            glossary map.
     */
    protected GlossaryConfig createGlossaryConfigPart(Map<String, String> glossaryTerms) {
        List<GlossaryPair> pairs = new ArrayList<>();
        for (Map.Entry<String, String> e : glossaryTerms.entrySet()) {
            pairs.add(new GlossaryPair(e.getKey(), e.getValue()));
            if (pairs.size() >= MAX_GLOSSARY_TERMS) {
                break;
            }
        }
        return new GlossaryConfig(new GlossaryData(pairs));
    }

    /**
     * Json definition: glossaryConfig.
     */
    static class GlossaryConfig {
        public final GlossaryData glossaryData;

        GlossaryConfig(GlossaryData glossaryData) {
            this.glossaryData = glossaryData;
        }
    }

    /**
     * Json definition: glossaryData.
     */
    static class GlossaryData {
        public final List<GlossaryPair> glossaryPairs;

        GlossaryData(List<GlossaryPair> glossaryPairs) {
            this.glossaryPairs = Collections.unmodifiableList(glossaryPairs);
        }
    }

    /**
     * Json definition: glossaryPair.
     */
    static class GlossaryPair {
        public final String sourceText;
        public final String translatedText;

        GlossaryPair(String sourceText, String translatedText) {
            this.sourceText = sourceText;
            this.translatedText = translatedText;
        }
    }
}
