/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
 2011 Briac Pilpre, Alex Buloichik
 2013 Didier Briel
 2016 Aaron Madlon-Kay
 2020 Lev Abashkin
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

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Window;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Support of Yandex.Cloud machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 * @author Lev Abashkin
 *
 * @see <a href="https://cloud.yandex.com/docs/translate/api-ref/Translation/">Translation API</a>
 */
public class YandexCloudTranslate extends BaseTranslate {

    private static final String PROPERTY_OAUTH_TOKEN = "yandex.cloud.oauth-token";
    private static final String PROPERTY_FOLDER_ID = "yandex.cloud.folder-id";
    private static final String PROPERTY_USE_GLOSSARY = "yandex.cloud.use-glossary";

    private static final int MAX_GLOSSARY_TERMS = 50;
    private static final int MAX_TEXT_LENGTH = 10000;
    private static final int IAM_TOKEN_TTL_SECONDS = 3600; // Recommended value

    private static final String IAM_TOKEN_URL = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
    private static final String TRANSLATE_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";

    private String IAMErrorMessage = null;
    private String cachedIAMToken = null;
    private long lastIAMTokenTime = 0L;

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_YANDEX_CLOUD");
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUsingGlossary() {
        return true;
    }

    @Override
    public String getTranslation(Language sLang, Language tLang, String text, List<GlossaryEntry> glossaryTerms) {
        if (!enabled) {
            return null;
        }

        String trText = text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH - 3) + "..." : text;
        String prev = getFromCache(sLang, tLang, trText);
        if (prev != null) {
            return prev;
        }

        String oAuthToken = getCredential(PROPERTY_OAUTH_TOKEN);
        if (oAuthToken == null || oAuthToken.isEmpty()) {
            return OStrings.getString("MT_ENGINE_YANDEX_CLOUD_OAUTH_TOKEN_NOT_FOUND");
        }

        String folderId = getCredential(PROPERTY_FOLDER_ID);
        if (folderId == null || folderId.isEmpty()) {
            return OStrings.getString("MT_ENGINE_YANDEX_CLOUD_FOLDER_ID_NOT_FOUND");
        }

        String IAMToken = getIAMToken(oAuthToken);
        if (IAMToken == null) {
            return IAMErrorMessage;
        }

        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("{")
                .append("\"sourceLanguageCode\":\"").append(sLang.getLanguageCode().toLowerCase()).append("\",")
                .append("\"targetLanguageCode\":\"").append(tLang.getLanguageCode().toLowerCase()).append("\",")
                .append("\"format\": \"HTML\",") // HTML format keeps OmegaT tags intact
                .append("\"folderId\": \"").append(folderId).append("\",")
                .append(getGlossaryConfigPart(glossaryTerms))
                .append("\"texts\": [").append(JsonParser.quote(trText)).append("]}");

        Map<String, String> headers = new TreeMap<>();
        headers.put("Authorization", "Bearer " + IAMToken);

        String response;
        try {
            response = WikiGet.postJSON(TRANSLATE_URL, requestBuilder.toString(), headers);
        } catch (WikiGet.ResponseError e) {
            String errorMessage = extractErrorMessage(e.body);
            if (errorMessage == null) {
                errorMessage = OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_TRANSLATE_RESPONSE");
            }
            return errorMessage;
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        String tr = extractTranslation(response);

        if (tr == null) {
            return "";
        }

        tr = cleanSpacesAroundTags(tr, trText);
        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    @Override
    public void showConfigurationUI(Window parent) {

        JPanel extraPanel = new JPanel();
        extraPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.X_AXIS));
        extraPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        JCheckBox glossaryCheckBox = new JCheckBox(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_GLOSSARY_CHECKBOX"));
        extraPanel.add(glossaryCheckBox);

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();

                String folderId = panel.valueField1.getText().trim();
                setCredential(PROPERTY_FOLDER_ID, folderId, temporary);

                String oAuthToken = panel.valueField2.getText().trim();
                setCredential(PROPERTY_OAUTH_TOKEN, oAuthToken, temporary);

                Preferences.setPreference(PROPERTY_USE_GLOSSARY, glossaryCheckBox.isSelected());
            }
        };

        dialog.panel.itemsPanel.add(extraPanel);

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_FOLDER_ID_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_FOLDER_ID));

        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_YANDEX_CLOUD_OAUTH_TOKEN_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_OAUTH_TOKEN));

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_OAUTH_TOKEN));

        glossaryCheckBox.setSelected(Preferences.isPreferenceDefault(PROPERTY_USE_GLOSSARY, false));

        dialog.show();
    }

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_YANDEX_CLOUD_TRANSLATE;
    }

    /**
     * Dummy method required by base abstract class
     */
    @Override
    protected String translate(Language sLang, Language tLang, String text) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(final String json) {
        Map<String, Object> rootNode;
        try {
            rootNode = (Map<String, Object>) JsonParser.parse(json);
            return (String) rootNode.get("message");
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_ENGINE_YANDEX_CLOUD_BAD_ERROR_REPORT");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTranslation(final String json) {
        try {
            Map<String, Object> rootNode;
            rootNode = (Map<String, Object>) JsonParser.parse(json);
            List<Object> translationsList = (List<Object>) rootNode.get("translations");
            Map<String, String> translationNode = (Map<String, String>) translationsList.get(0);
            return translationNode.get("text");
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_TRANSLATE_RESPONSE");
        }
    }

    @SuppressWarnings("unchecked")
    private String getIAMToken(final String oAuthToken) {
        if (System.currentTimeMillis() - lastIAMTokenTime > IAM_TOKEN_TTL_SECONDS * 1000) {

            String request = "{\"yandexPassportOauthToken\":\"" + oAuthToken + "\"}";
            String response;
            Map<String, Object> rootNode;

            try {
                response = WikiGet.postJSON(IAM_TOKEN_URL, request, null);
            } catch (WikiGet.ResponseError e) {
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
                rootNode = (Map<String, Object>) JsonParser.parse(response);
                cachedIAMToken = (String) rootNode.get("iamToken");
                lastIAMTokenTime = System.currentTimeMillis();
            } catch (Exception e) {
                Log.logErrorRB(e, "MT_ENGINE_YANDEX_CLOUD_BAD_IAM_RESPONSE");
                IAMErrorMessage = OStrings.getString("MT_ENGINE_YANDEX_CLOUD_BAD_IAM_RESPONSE");
                return null;
            }
        }
        return cachedIAMToken;
    }

    private String getGlossaryConfigPart(List<GlossaryEntry> glossaryTerms) {

        if (!Preferences.isPreference(PROPERTY_USE_GLOSSARY) || glossaryTerms.isEmpty()) {
            return "";
        }

        if (glossaryTerms.size() > MAX_GLOSSARY_TERMS) {
            glossaryTerms = glossaryTerms.subList(0, MAX_GLOSSARY_TERMS);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"glossaryConfig\":{\"glossaryData\":{\"glossaryPairs\":[");

        Iterator<GlossaryEntry> iterator = glossaryTerms.iterator();
        while (iterator.hasNext()) {
            GlossaryEntry e = iterator.next();
            sb.append("{\"sourceText\":")
                .append(JsonParser.quote(e.getSrcText()))
                .append(",\"translatedText\":")
                .append(JsonParser.quote(e.getLocText()))
                .append("}");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]}},");
        return sb.toString();
    }
}
