/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
 *                2019 Marc Riera Irigoyen
 *                2021 Kevin Brubeck Unhammer
 *                2021-2023 Hiroshi Miura
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.omegat.core.Core;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 */
public class ApertiumTranslate extends BaseCachedTranslate {

    private static final int HTTP_OK = 200;

    protected static final String PROPERTY_APERTIUM_MARKUNKNOWN = "apertium.server.markunknown";
    protected static final String PROPERTY_APERTIUM_SERVER_CUSTOM = "apertium.server.custom";
    protected static final String PROPERTY_APERTIUM_SERVER_URL = "apertium.server.url";
    protected static final String PROPERTY_APERTIUM_SERVER_KEY = "apertium.server.key";
    protected static final String APERTIUM_SERVER_URL_DEFAULT = "https://www.apertium.org/apy";
    protected static final String APERTIUM_SERVER_URL_FORMAT = "%s/translate?q=%s&markUnknown=%s&langpair=%s|%s&key=%s";
    // Specific OmegaT key
    protected static final String APERTIUM_SERVER_KEY_DEFAULT = "bwuxb5jS+VwSJ8mLz1qMfmMrDGA";
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(ApertiumTranslate.class);
/*
        Core.registerMachineTranslationClass(BelazarTranslate.class);
        Core.registerMachineTranslationClass(DeepLTranslate.class);
        Core.registerMachineTranslationClass(Google2Translate.class);
        Core.registerMachineTranslationClass(IBMWatsonTranslate.class);
        Core.registerMachineTranslationClass(MyMemoryHumanTranslate.class);
        Core.registerMachineTranslationClass(MyMemoryMachineTranslate.class);
        Core.registerMachineTranslationClass(YandexCloudTranslate.class);
 */
    }

    public static void unloadPlugins() {
    }

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_APERTIUM_TRANSLATE;
    }

    /**
     * Apertium engine name.
     *
     * @return engine name.
     */
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
        String sourceLang = apertiumCode(sLang);
        String targetLang = apertiumCode(tLang);

        String server = getCustomServerUrl();
        String apiKey = getCredential(PROPERTY_APERTIUM_SERVER_KEY);

        if (!useCustomServer()) {
            server = APERTIUM_SERVER_URL_DEFAULT;
            apiKey = APERTIUM_SERVER_KEY_DEFAULT;
        }
        String markUnknownVal = useMarkUnknown() ? "yes" : "no";
        String url = String.format(APERTIUM_SERVER_URL_FORMAT, server, URLEncoder.encode(text, "UTF-8"),
                markUnknownVal, sourceLang, targetLang, apiKey);
        String v;
        try {
            v = HttpConnectionUtils.getURL(new URL(url));
        } catch (IOException e) {
            Log.logErrorRB(e, "APERTIUM_CUSTOM_SERVER_NOTFOUND");
            throw new Exception(OStrings.getString("APERTIUM_CUSTOM_SERVER_NOTFOUND"));
        }

        return getJsonResults(v);
    }

    /**
     * Parse response and return translation text.
     *
     * @param json
     *            response string.
     * @return translation text, or null when engine returns empty result, or
     *         error message when parse failed.
     */
    @SuppressWarnings("unchecked")
    protected String getJsonResults(String json) throws Exception {
        try {
            JsonNode rootNode = mapper.readTree(json);
            if (!rootNode.has("responseStatus")) {
                throw new MachineTranslateError(OStrings.getString("APERTIUM_CUSTOM_SERVER_INVALID"));
            }
            int code = rootNode.get("responseStatus").asInt();
            if (code == HTTP_OK) {
                String tr = rootNode.get("responseData").get("translatedText").asText();
                if (tr != null) {
                    return tr;
                }
            }
            // throw exception if there's no translatedText or if there was
            // a problem
            String details = rootNode.get("responseDetails").asText();
            throw new MachineTranslateError(StringUtil.format(OStrings.getString("APERTIUM_ERROR"), code, details));
        } catch (JsonParseException e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            throw new MachineTranslateError(OStrings.getString("MT_JSON_ERROR"));
        }
    }

    /**
     * Whether to use the markUnknown feature.
     */
    private boolean useMarkUnknown() {
        String value = System.getProperty(PROPERTY_APERTIUM_MARKUNKNOWN,
                Preferences.getPreference(PROPERTY_APERTIUM_MARKUNKNOWN));
        return Boolean.parseBoolean(value);
    }

    /**
     * Whether to use a custom Apertium server.
     */
    private boolean useCustomServer() {
        String value = System.getProperty(PROPERTY_APERTIUM_SERVER_CUSTOM,
                Preferences.getPreference(PROPERTY_APERTIUM_SERVER_CUSTOM));
        return Boolean.parseBoolean(value);
    }

    /**
     * Get the custom server URL.
     */
    private String getCustomServerUrl() {
        return System.getProperty(PROPERTY_APERTIUM_SERVER_URL,
                Preferences.getPreference(PROPERTY_APERTIUM_SERVER_URL));
    }

    /**
     * Apertium engine is configurable.
     *
     * @return true
     */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static final int CONFIG_URL_COLUMN_WIDTH = 20;

    /**
     * Show configuration UI.
     *
     * @param parent
     *            main window.
     */
    @Override
    public void showConfigurationUI(Window parent) {

        JCheckBox unkCheckBox = new JCheckBox(OStrings.getString("APERTIUM_MARKUNKNOWN_LABEL"));
        unkCheckBox.setSelected(useMarkUnknown());
        JCheckBox apiCheckBox = new JCheckBox(OStrings.getString("APERTIUM_CUSTOM_SERVER_LABEL"));
        apiCheckBox.setSelected(useCustomServer());

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();
                System.setProperty(PROPERTY_APERTIUM_MARKUNKNOWN, Boolean.toString(unkCheckBox.isSelected()));
                Preferences.setPreference(PROPERTY_APERTIUM_MARKUNKNOWN, unkCheckBox.isSelected());
                System.setProperty(PROPERTY_APERTIUM_SERVER_CUSTOM,
                        Boolean.toString(apiCheckBox.isSelected()));
                Preferences.setPreference(PROPERTY_APERTIUM_SERVER_CUSTOM, apiCheckBox.isSelected());
                String server = panel.valueField1.getText().trim();
                String apiKey = panel.valueField2.getText().trim();

                if (!getCustomServerUrl().equals(server)) {
                    clearCache();
                }

                System.setProperty(PROPERTY_APERTIUM_SERVER_URL, server);
                Preferences.setPreference(PROPERTY_APERTIUM_SERVER_URL, server);
                setCredential(PROPERTY_APERTIUM_SERVER_KEY, apiKey, temporary);
            }
        };

        Runnable updateOk = () -> {
            boolean needsFields = apiCheckBox.isSelected();
            boolean hasFields = !dialog.panel.valueField1.getText().trim().isEmpty();
            boolean canConfirm = !needsFields || hasFields;
            dialog.panel.okButton.setEnabled(canConfirm);
        };

        DocumentListener toggleOkButton = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                updateOk.run();
            }

            public void insertUpdate(DocumentEvent event) {
                updateOk.run();
            }

            public void removeUpdate(DocumentEvent event) {
                updateOk.run();
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
                updateOk.run();
            }
        };

        apiCheckBox.addItemListener(toggleInterface);
        dialog.panel.valueField1.getDocument().addDocumentListener(toggleOkButton);

        dialog.panel.itemsPanel.add(unkCheckBox);
        dialog.panel.itemsPanel.add(apiCheckBox, 1);
        dialog.panel.valueLabel1.setText(OStrings.getString("APERTIUM_CUSTOM_SERVER_URL_LABEL"));
        dialog.panel.valueField1.setText(getCustomServerUrl());
        dialog.panel.valueField1.setColumns(CONFIG_URL_COLUMN_WIDTH);
        dialog.panel.valueLabel2.setText(OStrings.getString("APERTIUM_CUSTOM_SERVER_KEY_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_APERTIUM_SERVER_KEY));
        dialog.panel.temporaryCheckBox
                .setSelected(isCredentialStoredTemporarily(PROPERTY_APERTIUM_SERVER_KEY));

        toggleInterface.itemStateChanged(null);

        dialog.show();
    }

}
