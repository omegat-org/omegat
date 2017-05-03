/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik, Didier Briel
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

import java.awt.Window;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.WikiGet;

/**
 * Support of Microsoft Translator machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://www.microsoft.com/en-us/translator/translatorapi.aspx">Translator API</a>
 * @see <a href="https://msdn.microsoft.com/en-us/library/ff512421.aspx">Translate Method reference</a>
 */
public class MicrosoftTranslate extends BaseTranslate {
    protected static final String PROPERTY_CLIENT_ID = "microsoft.api.client_id";
    protected static final String PROPERTY_CLIENT_SECRET = "microsoft.api.client_secret";

    protected static final String URL_TOKEN = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13/";
    protected static final String URL_TRANSLATE = "http://api.microsofttranslator.com/v2/Http.svc/Translate";
    protected static final Pattern RE_RESPONSE = Pattern.compile("<string.+?>(.+)</string>");

    protected String accessToken;

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_MICROSOFT_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_MICROSOFT");
    }

    @Override
    protected synchronized String translate(Language sLang, Language tLang, String text) throws Exception {
        text = text.length() > 10000 ? text.substring(0, 9997) + "..." : text;
        String prev = getFromCache(sLang, tLang, text);
        if (prev != null) {
            return prev;
        }

        String langFrom = checkMSLang(sLang);
        String langTo = checkMSLang(tLang);
        try {
            String translation;
            if (accessToken == null) {
                requestToken();
                translation = requestTranslate(langFrom, langTo, text);
            } else {
                try {
                    translation = requestTranslate(langFrom, langTo, text);
                } catch (WikiGet.ResponseError ex) {
                    if (ex.code == 400) {
                        requestToken();
                        translation = requestTranslate(langFrom, langTo, text);
                    } else {
                        throw ex;
                    }
                }
            }
            if (translation != null) {
                putToCache(sLang, tLang, text, translation);
            }
            return translation;
        } catch (WikiGet.ResponseError ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            Log.log(ex);
            return ex.getLocalizedMessage();
        }
    }

    /**
     * Converts language codes to Microsoft ones.
     * @param language
     *              a project language
     * @return either a language code, or a Chinese language code plus a Microsoft variant
     */
    private String checkMSLang(Language language) {
        if (language.getLanguage().compareToIgnoreCase("zh-cn") == 0) {
            return "zh-CHS";
        } else if ((language.getLanguage().compareToIgnoreCase("zh-tw") == 0)
                || (language.getLanguage().compareToIgnoreCase("zh-hk") == 0)) {
            return "zh-CHT";
        } else {
            return language.getLanguageCode();
        }

    }

    @SuppressWarnings("unchecked")
    private void requestToken() throws Exception {
        String id = getCredential(PROPERTY_CLIENT_ID);
        String secret = getCredential(PROPERTY_CLIENT_SECRET);
        if (StringUtil.isEmpty(id) || StringUtil.isEmpty(secret)) {
            throw new Exception(OStrings.getString("MT_ENGINE_MICROSOFT_KEY_NOTFOUND"));
        }

        Map<String, String> p = new TreeMap<String, String>();
        p.put("client_id", id);
        p.put("client_secret", secret);
        p.put("scope", "http://api.microsofttranslator.com");
        p.put("grant_type", "client_credentials");
        String r = WikiGet.post(URL_TOKEN, p, null);
        Map<String, Object> rmap = (Map<String, Object>) JsonParser.parse(r);
        accessToken = (String) rmap.get("access_token");
    }

    private String requestTranslate(String langFrom, String langTo, String text) throws Exception {
        Map<String, String> p = new TreeMap<String, String>();
        p.put("appId", "Bearer " + accessToken);
        p.put("text", text);
        p.put("from", langFrom);
        p.put("to", langTo);
        p.put("contentType", "text/plain");

        String r = WikiGet.get(URL_TRANSLATE, p, null);
        Matcher m = RE_RESPONSE.matcher(r);
        if (m.matches()) {
            String translatedText = m.group(1);
            translatedText = translatedText.replace("&lt;", "<");
            translatedText = translatedText.replace("&gt;", ">");
            return translatedText;
        } else {
            Log.logWarningRB("MT_ENGINE_MICROSOFT_WRONG_RESPONSE");
            return null;
        }
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
                String id = panel.valueField1.getText().trim();
                String secret = panel.valueField2.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_CLIENT_ID, id, temporary);
                setCredential(PROPERTY_CLIENT_SECRET, secret, temporary);
            }
        };
        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_MICROSOFT_CLIENT_ID_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_CLIENT_ID));
        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_MICROSOFT_CLIENT_SECRET_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_CLIENT_SECRET));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_CLIENT_SECRET));
        dialog.show();
    }
}
