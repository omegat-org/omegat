/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

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
    protected static final String GT_URL = "https://www.apertium.org/apy/translate?q=";
    // Specific OmegaT key
    protected static final String GT_URL2 = "&markUnknown=no&langpair=#sourceLang#|#targetLang#&key=bwuxb5jS+VwSJ8mLz1qMfmMrDGA";

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

        String lCode = language.getLanguageCode().toLowerCase();
        String locale = language.getLocaleCode();

        if (!StringUtil.isEmpty(language.getCountryCode())) {
            if (locale.equalsIgnoreCase("en_us") || locale.equalsIgnoreCase("pt_br"))
                return locale; // We need en_US and pt_BR
            else if (locale.equalsIgnoreCase("oc_ar"))
                return "oc_aran";
            else if (locale.equalsIgnoreCase("ca_va"))
                return "ca_valencia";
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

        String url2 = GT_URL2.replace("#sourceLang#", sourceLang).replace("#targetLang#", targetLang);
        String url = GT_URL + URLEncoder.encode(trText, "UTF-8") + url2;
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
}
