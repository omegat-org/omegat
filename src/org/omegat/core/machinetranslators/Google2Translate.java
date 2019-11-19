/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of Google Translate API v.2 machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://cloud.google.com/translate/docs/getting-started">Translation API</a>
 */
public class Google2Translate extends BaseTranslate {
    protected static final String PROPERTY_PREMIUM_KEY = "google.api.premium";
    protected static final String PROPERTY_API_KEY = "google.api.key";
    protected static final String GT_URL = "https://translation.googleapis.com/language/translate/v2";
    protected static final Pattern RE_HTML  = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_GOOGLE2_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_GOOGLE2");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..." : text;

        String prev = getFromCache(sLang, tLang, trText);
        if (prev != null) {
            return prev;
        }

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
            return OStrings.getString("GOOGLE_API_KEY_NOTFOUND");
        }

        Map<String, String> params = new TreeMap<String, String>();

        if (isPremium()) {
            params.put("model", "nmt");
        }

        params.put("key", googleKey);
        params.put("source", sLang.getLanguageCode());
        params.put("target", targetLang);
        params.put("q", trText);
        // The 'text' format mangles the tags, whereas the 'html' encodes some characters
        // as entities. Since it's more reliable to convert the entities back, we are
        // using 'html' and convert the text with the unescapeHTML() method.
        params.put("format", "html");

        Map<String, String> headers = new TreeMap<String, String>();
        headers.put("X-HTTP-Method-Override", "GET");

        String v;
        try {
            v = WikiGet.post(GT_URL, params, headers);
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        String tr = getJsonResults(v);

        if (tr == null) {
            return "";
        }

        tr = unescapeHTML(tr);

        tr = cleanSpacesAroundTags(tr, text);

        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    /** Convert entities to character. Ex: "&#39;" to "'". */
    private String unescapeHTML(String text) {

        text = text.replace("&quot;", "\"")
                   .replace("&gt;", ">")
                   .replace("&lt;", "<")
                   .replace("&amp;", "&");

        Matcher m = RE_HTML.matcher(text);
        while (m.find()) {
            String g = m.group();
            int codePoint = Integer.parseInt(m.group(1));
            String cpString = String.valueOf(Character.toChars(codePoint));
            text = text.replace(g, cpString);
        }
        return text;
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

        try {
            Map<String, Object> dataNode = (Map<String, Object>) rootNode.get("data");
            List<Object> translationsList = (List<Object>) dataNode.get("translations");
            Map<String, String> translationNode = (Map<String, String>) translationsList.get(0);
            return translationNode.get("translatedText");
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Whether or not to use the new Neural Machine Translation System
     *
     * @see <a href="https://research.googleblog.com/2016/09/a-neural-network-for-machine.html">A Neural
     *      Network for Machine Translation, at Production Scale</a>
     */
    private boolean isPremium() {
        String value = System.getProperty(PROPERTY_PREMIUM_KEY,
                Preferences.getPreference(PROPERTY_PREMIUM_KEY));
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {
        JCheckBox premiumCheckBox = new JCheckBox(OStrings.getString("MT_ENGINE_GOOGLE2_PREMIUM_LABEL"));
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

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_GOOGLE2_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));

        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_KEY));

        dialog.panel.itemsPanel.add(premiumCheckBox);

        dialog.show();
    }
}
