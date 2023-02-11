/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
               2022,2023 Hiroshi Miura
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

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Support of IBM Watson machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 *
 * @see <a href=
 *      "https://www.ibm.com/watson/developercloud/language-translator/api/v3/">Translation
 *      API</a>
 */
public class IBMWatsonTranslate extends BaseCachedTranslate {
    protected static final String PROPERTY_LOGIN = "ibmwatson.api.login";
    protected static final String PROPERTY_PASSWORD = "ibmwatson.api.password";
    protected static final String PROPERTY_MODEL = "ibmwatson.api.model";
    protected static final String PROPERTY_URL = "ibmwatson.api.url";

    protected static final String WATSON_URL = "https://gateway.watsonplatform.net/language-translator/api/v3/translate";
    protected static final String WATSON_VERSION = "2018-05-01";

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_IBMWATSON_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_IBMWATSON");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..." : text;
        String prev = getFromCache(sLang, tLang, trText);
        if (prev != null) {
            return prev;
        }

        String apiLogin = getCredential(PROPERTY_LOGIN);
        String apiPassword = getCredential(PROPERTY_PASSWORD);

        if (apiLogin == null || apiLogin.isEmpty()) {
            throw new Exception(OStrings.getString("IBMWATSON_API_KEY_NOTFOUND"));
        }

        // If the instance uses IAM authentication
        // (https://console.bluemix.net/docs/services/watson/getting-started-iam.html)
        // only an API Key is provided, in this case the authentication header
        // is "apikey:apikey"
        // see
        // https://www.ibm.com/watson/developercloud/language-translator/api/v2/curl.html?curl#authentication
        if (apiPassword == null || apiPassword.isEmpty()) {
            apiPassword = apiLogin;
            apiLogin = "apikey";
        }
        String json = createJsonRequest(sLang, tLang, trText);

        Map<String, String> headers = new TreeMap<>();

        // By default, all Watson services log requests and their results.
        // The logged data is not shared or made public. To prevent IBM
        // from accessing your data for general service improvements, set
        // the X-Watson-Learning-Opt-Out request header to true for all
        // requests.

        // Let's opt out then.
        headers.put("X-Watson-Learning-Opt-Out", "true");

        String authentication = "Basic " + Base64.getMimeEncoder(-1, new byte[] {})
                .encodeToString((apiLogin + ":" + apiPassword).getBytes(StandardCharsets.ISO_8859_1));
        headers.put("Authorization", authentication);
        headers.put("Accept", "application/json");

        String v = HttpConnectionUtils.postJSON(getWatsonUrl() + "/v3/translate?version=" + WATSON_VERSION,
                json, headers);
        String tr = getJsonResults(v);
        if (tr == null) {
            return null;
        }
        tr = unescapeHTML(tr);
        tr = cleanSpacesAroundTags(tr, trText);
        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    private String getModelId() {
        return System.getProperty(PROPERTY_MODEL, Preferences.getPreference(PROPERTY_MODEL));
    }

    private String getWatsonUrl() {
        String url = System.getProperty(PROPERTY_URL, Preferences.getPreference(PROPERTY_URL));
        if (url == null || url.isEmpty()) {
            url = WATSON_URL;
        }
        return url;
    }

    /**
     * Create Watson request and return as json string.
     */
    protected String createJsonRequest(Language sLang, Language tLang, String trText)
            throws JsonProcessingException {
        Map<String, Object> params = new TreeMap<>();
        params.put("text", Collections.singletonList(trText));
        String modelId = getModelId();
        if (modelId != null && !modelId.isEmpty()) {
            params.put("model_id", modelId);
        }
        params.put("source", sLang.getLanguageCode().toUpperCase());
        params.put("target", tLang.getLanguageCode().toUpperCase());
        return new ObjectMapper().writeValueAsString(params);
    }

    /**
     * Parse Watson response and return translated text.sed
     * 
     * @param json
     *            response.
     * @return translated text.
     */
    @SuppressWarnings("unchecked")
    protected String getJsonResults(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode translations = rootNode.get("translations");
            if (translations.has(0)) {
                return translations.get(0).get("translation").asText();
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

        JPanel watsonPanel = new JPanel();
        watsonPanel.setLayout(new java.awt.GridBagLayout());
        watsonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        watsonPanel.setAlignmentX(0.0F);

        // Info about IAM authentication
        JLabel iamAuthLabel = new JLabel(OStrings.getString("MT_ENGINE_IBMWATSON_IAM_AUTHENTICATION"));
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        watsonPanel.add(iamAuthLabel, gridBagConstraints);

        // API URL
        JLabel urlLabel = new JLabel(OStrings.getString("MT_ENGINE_IBMWATSON_URL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        watsonPanel.add(urlLabel, gridBagConstraints);

        JTextField urlField = new JTextField(Preferences.getPreferenceDefault(PROPERTY_URL, WATSON_URL));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        urlLabel.setLabelFor(urlField);
        watsonPanel.add(urlField, gridBagConstraints);

        // Custom Model
        JLabel modelIdLabel = new JLabel(OStrings.getString("MT_ENGINE_IBMWATSON_MODELID_LABEL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        watsonPanel.add(modelIdLabel, gridBagConstraints);

        JTextField modelIdField = new JTextField(Preferences.getPreferenceDefault(PROPERTY_MODEL, ""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        modelIdLabel.setLabelFor(modelIdField);
        watsonPanel.add(modelIdField, gridBagConstraints);

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();

                String login = panel.valueField1.getText().trim();
                setCredential(PROPERTY_LOGIN, login, temporary);

                String password = panel.valueField2.getText().trim();
                setCredential(PROPERTY_PASSWORD, password, temporary);

                System.setProperty(PROPERTY_MODEL, modelIdField.getText());
                Preferences.setPreference(PROPERTY_MODEL, modelIdField.getText());

                System.setProperty(PROPERTY_URL, urlField.getText());
                Preferences.setPreference(PROPERTY_URL, urlField.getText());
            }
        };

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_IBMWATSON_LOGIN_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_LOGIN));

        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_IBMWATSON_PASSWORD_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_PASSWORD));

        // TODO Apparently, the API URL can change if the user has their own
        // instance.

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_PASSWORD));

        dialog.panel.itemsPanel.add(watsonPanel);

        dialog.show();
    }
}
