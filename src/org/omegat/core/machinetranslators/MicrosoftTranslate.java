/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik, Didier Briel
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

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of Microsoft Translator machine translation.
 * 
 * http://www.microsofttranslator.com/dev/
 * http://msdn.microsoft.com/en-us/library/ff512421.aspx
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class MicrosoftTranslate extends BaseTranslate {
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
        String langFrom = checkMSLang(sLang);
        String langTo = checkMSLang(tLang);
        try {
            if (accessToken == null) {
                requestToken();
                return requestTranslate(langFrom, langTo, text);
            } else {
                try {
                    return requestTranslate(langFrom, langTo, text);
                } catch (WikiGet.ResponseError ex) {
                    if (ex.code == 400) {
                        requestToken();
                        return requestTranslate(langFrom, langTo, text);
                    } else {
                        throw ex;
                    }
                }
            }
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
        if (language.getLanguage().compareToIgnoreCase("zh-cn") == 0){
            return "zh-CHS";
        } else if ((language.getLanguage().compareToIgnoreCase("zh-tw") == 0) ||
                   (language.getLanguage().compareToIgnoreCase("zh-hk") == 0)) {
            return "zh-CHT";
        } else {
            return language.getLanguageCode();         
        }
        
    }
    private void requestToken() throws Exception {
        Map<String, String> p = new TreeMap<String, String>();

        if (System.getProperty("microsoft.api.client_id") == null
                || System.getProperty("microsoft.api.client_secret") == null) {
            throw new Exception(OStrings.getString("MT_ENGINE_MICROSOFT_KEY_NOTFOUND"));
        }

        p.put("client_id", System.getProperty("microsoft.api.client_id"));
        p.put("client_secret", System.getProperty("microsoft.api.client_secret"));
        p.put("scope", "http://api.microsofttranslator.com");
        p.put("grant_type", "client_credentials");
        String r = WikiGet.post(URL_TOKEN, p, null);
        Map<String, String> rmap = unpackJson(r);
        accessToken = rmap.get("access_token");
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

    int pos;
    String str;

    public Map<String, String> unpackJson(String v) {
        if (!v.startsWith("{") || !v.endsWith("}")) {
            throw new RuntimeException("Wrong");
        }
        str = v.substring(1, v.length() - 1);
        pos = 0;

        Map<String, String> result = new TreeMap<String, String>();

        while (true) {
            String key = readString();
            mustBe(':');
            String value = readString();
            result.put(key, value);
            if (pos == str.length()) {
                break;
            }
            mustBe(',');
        }
     
        return result;
    }

    String readString() {
        if (str.charAt(pos) != '"') {
            throw new RuntimeException("Wrong");
        }
        int pose = str.indexOf('"', pos + 1);
        if (pose < 0) {
            throw new RuntimeException("Wrong");
        }
        String result = str.substring(pos + 1, pose);
        pos = pose + 1;
        return result;
    }

    void mustBe(char c) {
        if (str.charAt(pos) != c) {
            throw new RuntimeException("Wrong");
        }
        pos++;
    }
}
