/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
               2019 Marc Riera Irigoyen
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

import java.awt.Window;
import java.awt.event.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.WikiGet;

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 */
public class ApertiumTranslate extends BaseTranslate {
    protected static final String PROPERTY_APERTIUM_SERVER_CUSTOM = "apertium.server.custom";
    protected static final String PROPERTY_APERTIUM_SERVER_URL = "apertium.server.url";
    protected static final String PROPERTY_APERTIUM_SERVER_KEY = "apertium.server.key";
    protected static final String APERTIUM_SERVER_URL = "https://www.apertium.org/apy";
    protected static final String APERTIUM_SERVER_URL_FORMAT = "%s/translate?q=%s&markUnknown=no&langpair=%s|%s&key=%s";
    // Specific OmegaT key
    protected static final String APERTIUM_SERVER_KEY = "bwuxb5jS+VwSJ8mLz1qMfmMrDGA";

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_APERTIUM_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_APERTIUM");
    }

    /**
     * Modify some country codes to fit with Apertium
     *
     * @param language
     *            An OmegaT language
     * @return A code modified for some Apertium languages
     */
    private String apertiumCode(Language language) {

        String lCode = language.getLanguageCode().toLowerCase(Locale.ENGLISH);
        String locale = language.getLocaleCode();

        if (!StringUtil.isEmpty(language.getCountryCode())) {
            if (locale.equalsIgnoreCase("en_us") || locale.equalsIgnoreCase("pt_br")) {
                return locale; // We need en_US and pt_BR
            } else if (locale.equalsIgnoreCase("oc_ar")) {
                return "oc_aran";
            } else if (locale.equalsIgnoreCase("ca_va")) {
                return "ca_valencia";
            }
        }

        return lCode;
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String prev = getFromCache(sLang, tLang, text);
        if (prev != null) {
            return prev;
        }

        String trText = text;

        String sourceLang = apertiumCode(sLang);
        String targetLang = apertiumCode(tLang);

        String server = getCredential(PROPERTY_APERTIUM_SERVER_URL);
        String apiKey = getCredential(PROPERTY_APERTIUM_SERVER_KEY);

        if (!useCustomServer()) {
            server = APERTIUM_SERVER_URL;
        }
        if (!useCustomServer()) {
            apiKey = APERTIUM_SERVER_KEY;
        }

        String url = String.format(APERTIUM_SERVER_URL_FORMAT, server, URLEncoder.encode(trText, "UTF-8"), sourceLang, targetLang, apiKey);
        String v;
        try {
            v = WikiGet.getURL(url);
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        String tr = getJsonResults(v);

        putToCache(sLang, tLang, trText, tr);
        return tr;
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

        Integer code = 0;
        String tr = null;
        if (rootNode.containsKey("responseStatus")) {
            code = (Integer) rootNode.get("responseStatus");
        }

        if (rootNode.containsKey("responseData")) {
            Map<String, Object> data = (Map<String, Object>) rootNode.get("responseData");
            tr = (String) data.get("translatedText");
        }

        // Returns an error message if there's no translatedText or if there was
        // a problem
        if (tr == null || code != 200) {
            String details = (String) rootNode.get("responseDetails");
            return StringUtil.format(OStrings.getString("APERTIUM_ERROR"), code, details);
        }

        return tr;
    }

    /**
     * Whether or not to use a custom Apertium server
     */
    private boolean useCustomServer() {
        String value = System.getProperty(PROPERTY_APERTIUM_SERVER_CUSTOM,
                Preferences.getPreference(PROPERTY_APERTIUM_SERVER_CUSTOM));
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {

        JCheckBox apiCheckBox = new JCheckBox(OStrings.getString("APERTIUM_CUSTOM_SERVER_LABEL"));
        apiCheckBox.setSelected(useCustomServer());

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();
                System.setProperty(PROPERTY_APERTIUM_SERVER_CUSTOM, Boolean.toString(apiCheckBox.isSelected()));
                Preferences.setPreference(PROPERTY_APERTIUM_SERVER_CUSTOM, apiCheckBox.isSelected());
                String server = panel.valueField1.getText().trim();
                String apiKey = panel.valueField2.getText().trim();
                setCredential(PROPERTY_APERTIUM_SERVER_URL, server, temporary);
                setCredential(PROPERTY_APERTIUM_SERVER_KEY, apiKey, temporary);
            }
        };

        ItemListener toggleInterface = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                dialog.panel.valueLabel1.setEnabled(apiCheckBox.isSelected());
                dialog.panel.valueLabel2.setEnabled(apiCheckBox.isSelected());
                dialog.panel.valueField1.setEnabled(apiCheckBox.isSelected());
                dialog.panel.valueField2.setEnabled(apiCheckBox.isSelected());
                dialog.panel.temporaryCheckBox.setEnabled(apiCheckBox.isSelected());
            }
        };

        apiCheckBox.addItemListener(toggleInterface);

        dialog.panel.itemsPanel.add(apiCheckBox,1);
        dialog.panel.valueLabel1.setText(OStrings.getString("APERTIUM_CUSTOM_SERVER_URL_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_APERTIUM_SERVER_URL));
        dialog.panel.valueField1.setColumns(20);
        dialog.panel.valueLabel2.setText(OStrings.getString("APERTIUM_CUSTOM_SERVER_KEY_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_APERTIUM_SERVER_KEY));

        toggleInterface.itemStateChanged(null);

        dialog.show();
    }

}
