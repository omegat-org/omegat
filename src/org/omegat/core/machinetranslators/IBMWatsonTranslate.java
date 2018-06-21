/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of IBM Watson machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://www.ibm.com/watson/developercloud/language-translator/api/v3/">Translation API</a>
 */
public class IBMWatsonTranslate extends BaseTranslate {
    protected static final String PROPERTY_LOGIN = "ibmwatson.api.login";
    protected static final String PROPERTY_PASSWORD = "ibmwatson.api.password";
    protected static final String PROPERTY_MODEL = "ibmwatson.api.model";

    protected static final String WATSON_URL = "https://gateway.watsonplatform.net/language-translator/api/v3/translate";
    protected static final String WATSON_VERSION = "2018-05-01";
    protected static final Pattern RE_HTML = Pattern.compile("&#([0-9]+);");

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

        if (apiPassword == null || apiPassword.isEmpty() || apiLogin == null || apiLogin.isEmpty()) {
            return OStrings.getString("IBMWATSON_API_KEY_NOTFOUND");
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"text\":[" + JsonParser.quote(trText) + "],");

        String modelId = getModelId();
        if (modelId != null && !modelId.isEmpty()) {
            json.append("\"model_id\":" + JsonParser.quote(modelId) + ",");
        }
        json.append("\"source\":" + JsonParser.quote(sLang.getLanguageCode().toUpperCase()) + ",");
        json.append("\"target\":" + JsonParser.quote(tLang.getLanguageCode().toUpperCase()) + "}");

        Map<String, String> headers = new TreeMap<>();

        // By default, all Watson services log requests and their results.
        // The logged data is not shared or made public. To prevent IBM
        // from accessing your data for general service improvements, set
        // the X-Watson-Learning-Opt-Out request header to true for all requests.

        // Let's opt out then.
        headers.put("X-Watson-Learning-Opt-Out", "true");

        String authentication = "Basic " + Base64.getMimeEncoder().encodeToString((apiLogin + ":" + apiPassword).getBytes(StandardCharsets.ISO_8859_1));
        headers.put("Authorization", authentication);
        headers.put("Accept", "application/json");

        String v;
        try {
            v = WikiGet.postJSON(WATSON_URL + "?version=" + WATSON_VERSION, json.toString(), headers);
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        String tr = getJsonResults(v);

        if (tr == null) {
            return "";
        }

        tr = unescapeHTML(tr);

        tr = cleanSpacesAroundTags(tr, trText);

        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    private String getModelId() {
        return System.getProperty(PROPERTY_MODEL, Preferences.getPreference(PROPERTY_MODEL));
    }

    @SuppressWarnings("unchecked")
    protected String getJsonResults(String json) {
        Map<String, Object> rootNode;
        try {
            rootNode = (Map<String, Object>) JsonParser.parse(json);
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return OStrings.getString("MT_JSON_ERROR");
        }

        //    {
        //        "translations": [{
        //          "translation": "translated text goes here."
        //        }],
        //        "word_count": 4,
        //        "character_count": 53
        //      }

        try {
            List<Object> translationsList = (List<Object>) rootNode.get("translations");
            Map<String, String> translationNode = (Map<String, String>) translationsList.get(0);
            return translationNode.get("translation");
        } catch (NullPointerException e) {
            return null;
        }
    }

    /** Convert entities to character. Ex: "&#39;" to "'". */
    private String unescapeHTML(String text) {

        text = text.replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&");

        Matcher m = RE_HTML.matcher(text);
        while (m.find()) {
            String g = m.group();
            int codePoint = Integer.parseInt(m.group(1));
            String cpString = String.valueOf(Character.toChars(codePoint));
            text = text.replace(g, cpString);
        }
        return text;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {

        JPanel modelPanel = new JPanel();
        modelPanel.setLayout(new java.awt.GridLayout(1, 2));
        modelPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        modelPanel.setAlignmentX(0.0F);

        JLabel modelIdLabel = new JLabel(OStrings.getString("MT_ENGINE_IBMWATSON_MODELID_LABEL"));
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        modelPanel.add(modelIdLabel, gridBagConstraints);

        JTextField modelIdField = new JTextField(Preferences.getPreferenceDefault(PROPERTY_MODEL, ""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        modelIdLabel.setLabelFor(modelIdField);
        modelPanel.add(modelIdField, gridBagConstraints);

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
            }
        };

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_IBMWATSON_LOGIN_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_LOGIN));

        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_IBMWATSON_PASSWORD_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_PASSWORD));

        // TODO Apparently, the API URL can change if the user has their own instance.

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_PASSWORD));

        dialog.panel.itemsPanel.add(modelPanel);

        dialog.show();
    }
}
