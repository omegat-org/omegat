/**************************************************************************
 Demo Machine Translation plugin for OmegaT(http://www.omegat.org/)

 This file was copied exactly from OmegaT
       (org.omegat.core.machinetranslators.GoogleTranslate.java)
  
 The original code header and contents follow directly below.

**************************************************************************/
/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.plugin.machinetranslators;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.WikiGet;

/**
 * Support of Google Translate machine translation.
 *
 * http://code.google.com/intl/be/apis/ajaxlanguage/documentation/#Translation
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Google2Translate extends BaseTranslate {
    protected static final String GT_URL = "http://ajax.googleapis.com/ajax/services/language/translate";
    protected static final String MARK_BEG = "{\"translatedText\":\"";
    protected static final String MARK_END = "\"}";
    protected static final Pattern RE_UNICODE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
    protected static final Pattern RE_HTML = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName()
    {
        return "allow_google2_translate";
    }

    public String getName() {
        return "Google2 Translate";
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..." : text;

        Map<String, String> p = new TreeMap<String, String>();
        p.put("v", "1.0");
        String targetLang = tLang.getLanguageCode();
        // Differentiate in target between simplified and traditional Chinese
        if ((tLang.getLanguage().compareToIgnoreCase("zh-cn") == 0)
                || (tLang.getLanguage().compareToIgnoreCase("zh-tw") == 0))
            targetLang = tLang.getLanguage();
        else if ((tLang.getLanguage().compareToIgnoreCase("zh-hk") == 0))
            targetLang = "ZH-TW"; // Google doesn't recognize ZH-HK

        p.put("langpair", sLang.getLanguageCode() + '|' + targetLang);
        p.put("q", trText);

        String v = WikiGet.post(GT_URL, p);
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
        String tr = v.substring(beg, end);

        // Attempt to clean spaces added by GT
        // Spaces after
        Matcher tag = PatternConsts.OMEGAT_TAG_SPACE.matcher(tr);
        while (tag.find()) {
            String searchTag = tag.group();
            if (text.indexOf(searchTag) == -1) { // The tag didn't appear with a
                // trailing space in the source text
                String replacement = searchTag.substring(0, searchTag.length() - 1);
                tr = tr.replace(searchTag, replacement);
            }
        }

        // Spaces before
        tag = PatternConsts.SPACE_OMEGAT_TAG.matcher(tr);
        while (tag.find()) {
            String searchTag = tag.group();
            if (text.indexOf(searchTag) == -1) { // The tag didn't appear with a
                // leading space in the source text
                String replacement = searchTag.substring(1, searchTag.length());
                tr = tr.replace(searchTag, replacement);
            }
        }
        return tr;
    }
}