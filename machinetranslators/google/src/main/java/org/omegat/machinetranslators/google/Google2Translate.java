/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2010 Alex Buloichik, Didier Briel
 *                2011 Briac Pilpre, Alex Buloichik
 *                2013 Didier Briel
 *                2016 Aaron Madlon-Kay
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

package org.omegat.machinetranslators.google;

import java.awt.Window;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.JCheckBox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseCachedTranslate;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.core.machinetranslators.MachineTranslateError;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 * Support of Google Translate API v.2 machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 *
 * @see <a href=
 *      "https://cloud.google.com/translate/docs/basic/setup-basic">Translation
 *      API</a>
 */
public class Google2Translate extends BaseCachedTranslate {

    public static final String ALLOW_GOOGLE2_TRANSLATE = "allow_google2_translate";
    protected static final String PROPERTY_PREMIUM_KEY = "google.api.premium";
    protected static final String PROPERTY_API_KEY = "google.api.key";
    /**
     * {@see https://cloud.google.com/translate/quotas}
     */
    protected static final String GT_DEFAULT_URL = "https://translation.googleapis.com";
    protected static final String GT_PATH = "/language/translate/v2";
    private final String googleTranslateUrl;
    private @Nullable String temporaryKey;
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final int MAX_TEXT_LENGTH_PREMIUM = 30000;
    private static final String BUNDLE_BASENAME = "org.omegat.machinetranslators.google.Bundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_BASENAME);
    private static final ILogger LOGGER = LoggerFactory.getLogger(Google2Translate.class, BUNDLE);

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(Google2Translate.class);
    }

    public static void unloadPlugins() {
    }

    public Google2Translate() {
        googleTranslateUrl = GT_DEFAULT_URL + GT_PATH;
    }

    /**
     * Constructor for test.
     * 
     * @param baseUrl
     *            custom url.
     * @param key
     *            temprary key.
     */
    public Google2Translate(String baseUrl, String key) {
        googleTranslateUrl = baseUrl + GT_PATH;
        temporaryKey = key;
    }

    /**
     * Return GOOGLE2 preference constant.
     *
     * @return ALLOW_GOOGLE2_TRANSLATE
     */
    @Override
    protected String getPreferenceName() {
        return ALLOW_GOOGLE2_TRANSLATE;
    }

    /**
     * Return Google2 engine name.
     *
     * @return localized name.
     */
    @Override
    public String getName() {
        return BUNDLE.getString("MT_ENGINE_GOOGLE2");
    }

    protected int getMaxTextLength() {
        if (isPremium()) {
            return MAX_TEXT_LENGTH_PREMIUM;
        }
        return MAX_TEXT_LENGTH;
    }

    /**
     * Query Google Translate API and return translation text.
     *
     * @param sLang
     *            source language.
     * @param tLang
     *            target language.
     * @param text
     *            source text.
     * @return translation.
     * @throws Exception
     *             when error occurred.
     */
    @Override
    protected @Nullable String translate(Language sLang, Language tLang, String text) throws Exception {
        String targetLang = tLang.getLanguageCode();
        // Differentiate in target between simplified and traditional Chinese
        if (tLang.getLanguage().compareToIgnoreCase("zh-cn") == 0
                || tLang.getLanguage().compareToIgnoreCase("zh-tw") == 0) {
            targetLang = tLang.getLanguage();
        } else if (tLang.getLanguage().compareToIgnoreCase("zh-hk") == 0) {
            targetLang = "ZH-TW"; // Google doesn't recognize ZH-HK
        }

        String googleKey = getCredential(PROPERTY_API_KEY);
        if (googleKey == null || googleKey.isEmpty()) {
            if (temporaryKey == null) {
                throw new MachineTranslateError(BUNDLE.getString("GOOGLE_API_KEY_NOTFOUND"));
            }
            googleKey = temporaryKey;
        }

        Map<String, String> params = new TreeMap<String, String>();

        if (isPremium()) {
            params.put("model", "nmt");
        }

        params.put("key", googleKey);
        params.put("source", sLang.getLanguageCode());
        params.put("target", targetLang);
        params.put("q", text);
        // The 'text' format mangles the tags, whereas the 'html' encodes some
        // characters
        // as entities. Since it's more reliable to convert the entities back,
        // we are
        // using 'html' and convert the text with the unescapeHTML() method.
        params.put("format", "html");

        Map<String, String> headers = new TreeMap<String, String>();
        headers.put("X-HTTP-Method-Override", "GET");

        String v = HttpConnectionUtils.post(googleTranslateUrl, params, headers);
        String tr = getJsonResults(v);
        if (tr == null) {
            return null;
        }
        tr = BaseTranslate.unescapeHTML(tr);
        return cleanSpacesAroundTags(tr, text);
    }

    /**
     * Parse response and return translation.
     *
     * @param json
     *            response string.
     * @return translation text.
     */
    protected String getJsonResults(String json) throws MachineTranslateError {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            if (root != null && root.get("error") == null) {
                var translations = root.get("data").get("translations");
                if (translations != null && !translations.isEmpty()) {
                    return translations.get(0).get("translatedText").asText();
                }
            }
        } catch (Exception e) {
            LOGGER.atError().setCause(e).setMessageRB("MT_JSON_ERROR").log();
            throw new MachineTranslateError(BUNDLE.getString("MT_JSON_ERROR"));
        }
        LOGGER.atError().setMessageRB("MT_JSON_ERROR").log();
        throw new MachineTranslateError(BUNDLE.getString("MT_JSON_ERROR"));
    }

    /**
     * Whether to use the new Neural Machine Translation System.
     *
     * @see <a href=
     *      "https://research.googleblog.com/2016/09/a-neural-network-for-machine.html">A
     *      Neural Network for Machine Translation, at Production Scale</a>
     */
    private boolean isPremium() {
        String value = System.getProperty(PROPERTY_PREMIUM_KEY,
                Preferences.getPreference(PROPERTY_PREMIUM_KEY));
        return Boolean.parseBoolean(value);
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

    /**
     * Default configuration UI.
     *
     * @param parent
     *            main window.
     */
    @Override
    public void showConfigurationUI(Window parent) {
        JCheckBox premiumCheckBox = new JCheckBox(BUNDLE.getString("MT_ENGINE_GOOGLE2_PREMIUM_LABEL"));
        premiumCheckBox.setSelected(isPremium());

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String key = panel.valueField1.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_API_KEY, key, temporary);

                System.setProperty(PROPERTY_PREMIUM_KEY, Boolean.toString(premiumCheckBox.isSelected()));
                Preferences.setPreference(PROPERTY_PREMIUM_KEY, premiumCheckBox.isSelected());
            }
        };

        dialog.panel.valueLabel1.setText(BUNDLE.getString("MT_ENGINE_GOOGLE2_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));

        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_KEY));

        dialog.panel.itemsPanel.add(premiumCheckBox);

        dialog.show();
    }
}
