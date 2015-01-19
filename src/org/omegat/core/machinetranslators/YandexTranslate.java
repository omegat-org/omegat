/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik, Didier Briel
               2014 oisee
               2015 Kos Ivantsov
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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;
import org.xml.sax.InputSource;

/**
 * Yandex Translate plugin for OmegaT (Caching translations).
 * @author oisee
 */
class YTPPostResponse {

    public int code;
    public String response;
}

@SuppressWarnings({"unchecked", "rawtypes"})
public class YandexTranslate extends BaseTranslate {

    public static final int ERR_OK = 200;
    public static final int ERR_KEY_INVALID = 401;
    public static final int ERR_KEY_BLOCKED = 402;
    public static final int ERR_DAILY_REQ_LIMIT_EXCEEDED = 403;
    public static final int ERR_DAILY_CHAR_LIMIT_EXCEEDED = 404;
    public static final int ERR_TEXT_TOO_LONG = 413;
    public static final int ERR_UNPROCESSABLE_TEXT = 422;
    public static final int ERR_LANG_NOT_SUPPORTED = 501;

    protected static final String USER_AGENT = "Mozilla/5.0";

    protected static final String GT_URL = "https://translate.yandex.net/api/v1.5/tr/translate";

    protected static Map<String, String> translationCache = new HashMap<String, String>();

    protected static String mvYandexKey = System.getProperty("yandex.api.key");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_YANDEX_TRANSLATE;
    }

    public String getName() {
        if (mvYandexKey == null) {
            return OStrings.getString("MT_ENGINE_YANDEX_KEY_NOTFOUND");
        } else {
            return OStrings.getString("MT_ENGINE_YANDEX");
        }
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        if (mvYandexKey == null) {
            return "";
        }

        String lvSourceLang = sLang.getLanguageCode().substring(0, 2).toLowerCase();
        String lvTargetLang = tLang.getLanguageCode().substring(0, 2).toLowerCase();

        String lvShorText = text.length() > 10000 ? text.substring(0, 9999) + "…" : text;
        String lvCacheText = lvSourceLang + '-' + lvTargetLang + lvShorText;
        String lvCachedResult = translationCache.get(lvCacheText);
        if (lvCachedResult != null) {
            return lvCachedResult;
        }

        //----------------------------------------------------------------------
        Map<String, String> p = new TreeMap<String, String>();
        p.put("key", mvYandexKey);
        p.put("lang", lvSourceLang + '-' + lvTargetLang);
        p.put("text", lvShorText);

        YTPPostResponse response = requestTranslate(p);//WikiGet.post(GT_URL, p);

        XPathFactory xPathFactory = XPathFactory.newInstance();

        //XPath xPathCode = xPathFactory.newXPath();
        //String pathCode = "/Translation/@code";
        //String code = (String) xPathCode.evaluate(pathCode, new InputSource(new StringReader(lvResponse)));
        
        switch (response.code) {
            case ERR_OK:
                break;
            case ERR_KEY_INVALID:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_INVALID_KEY");
            case ERR_KEY_BLOCKED:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_API_BLOCKED");
            case ERR_DAILY_REQ_LIMIT_EXCEEDED:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_DAILY_LIMIT_DETECT");
            case ERR_DAILY_CHAR_LIMIT_EXCEEDED:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_DAILY_LIMIT_VOLUME");
            case ERR_TEXT_TOO_LONG:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_MAZ_SIZE");
            case ERR_UNPROCESSABLE_TEXT:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_TRANSLATION_NOT_POSSIBLE");
            case ERR_LANG_NOT_SUPPORTED:
                return response.code +": " + OStrings.getString("MT_ENGINE_YANDEX_DIRECTION_NOT_SUPPORTED");
            default:
                break;
        }

        XPath xPathText = xPathFactory.newXPath();
        String pathText = "/Translation[@code='200']/text";

        String result = (String) xPathText.evaluate(pathText, new InputSource(new StringReader(response.response)));

        translationCache.put(lvCacheText, result);
        return result;
    }

    protected YTPPostResponse requestTranslate(Map params) throws Exception {
        YTPPostResponse response = new YTPPostResponse();
        try {
            response.response = WikiGet.post(GT_URL, params);
            response.code = ERR_OK;
        } catch (WikiGet.ResponseError ex) {
            response.response = null;
            response.code = ex.code;
        }
        return response;
    }
}

