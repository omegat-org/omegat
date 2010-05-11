/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.machinetranslators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.URLEncoder;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 */
public class ApertiumTranslate extends BaseTranslate {
    protected static String GT_URL = "http://api.apertium.org/json/translate?q=";
    // Specific OmegaT key
    protected static String GT_URL2 = "&langpair=#sourceLang#|#targetLang#&key=bwuxb5jS+VwSJ8mLz1qMfmMrDGA";
    protected static String MARK_BEG = "{\"translatedText\":\"";
    protected static String MARK_END = "\"}";
    protected static Pattern RE_UNICODE = Pattern
            .compile("\\\\u([0-9A-Fa-f]{4})");
    protected static Pattern RE_HTML = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_APERTIUM_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_APERTIUM");
    }

    /**
     * Modify some country codes to fit with Apertium
     * @param language An OmegaT language
     * @return A code modified for some Apertium languages
     */
    private String apertiumCode(Language language) {
        
        String lCode = language.getLanguageCode().toLowerCase();
        String locale =language.getLocaleCode();

        if (!StringUtil.isEmpty(language.getCountryCode())) {
            if ( locale.equalsIgnoreCase("en_us") ||
                 locale.equalsIgnoreCase("pt_br") )
                return locale; // We need en_US and pt_BR
            else if (locale.equalsIgnoreCase("oc_ar"))
                return "oc_aran";
            else if (locale.equalsIgnoreCase("ca_va"))
                return "ca_valencia";
        }

        return lCode;
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text)
            throws Exception {

        String trText = text;

        String sourceLang = apertiumCode(sLang);
        String targetLang = apertiumCode(tLang);

        String url2 = GT_URL2.replace("#sourceLang#", sourceLang).replace(
                "#targetLang#", targetLang);
        String url = GT_URL + URLEncoder.encode(trText, "UTF-8") + url2;

        String v = WikiGet.getURL(url);
        while (true) {
            Matcher m = RE_UNICODE.matcher(v);
            if (!m.find()) {
                break;
            }
            String g = m.group();
            char c = (char) Integer.parseInt(m.group(1), 16);
            v = v.replace(g, Character.toString(c));
        }
        v = v.replace("&quot;", "&#34;");
        v = v.replace("&nbsp;", "&#160;");
        v = v.replace("&amp;", "&#38;");
        v = v.replace("\\\"", "\"");
        while (true) {
            Matcher m = RE_HTML.matcher(v);
            if (!m.find()) {
                break;
            }
            String g = m.group();
            char c = (char) Integer.parseInt(m.group(1));
            v = v.replace(g, Character.toString(c));
        }

        int beg = v.indexOf(MARK_BEG) + MARK_BEG.length();
        int end = v.indexOf(MARK_END, beg);
        String tr = v.substring(beg, end - 2); // Remove \n
        return tr;
    }
}
