/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre
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

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of Google Translate API v.2 machine translation.
 * https://code.google.com/apis/language/translate/v2/getting_started.html
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 */
public class Google2Translate extends BaseTranslate {
    protected static final String  GT_URL   = "https://www.googleapis.com/language/translate/v2";
    protected static final Pattern RE_UNICODE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
    protected static final Pattern RE_HTML  = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_GOOGLE2_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_GOOGLE2");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..." : text;

        String targetLang = tLang.getLanguageCode();
        // Differentiate in target between simplified and traditional Chinese
        if ((tLang.getLanguage().compareToIgnoreCase("zh-cn") == 0)
                || (tLang.getLanguage().compareToIgnoreCase("zh-tw") == 0))
            targetLang = tLang.getLanguage();
        else if ((tLang.getLanguage().compareToIgnoreCase("zh-hk") == 0))
            targetLang = "ZH-TW"; // Google doesn't recognize ZH-HK

        StringBuffer queryString = new StringBuffer();
        
        String googleKey = System.getProperty("google.api.key");
        
        queryString.append("key=" + googleKey);
        queryString.append("&source=" + sLang.getLanguageCode());
        queryString.append("&target=" +  targetLang);
        queryString.append("&q=" + URLEncoder.encode(trText, "utf-8"));
        
        String v = WikiGet.getURL(GT_URL + "?" + queryString.toString());

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

        Pattern pattern = java.util.regex.Pattern.compile("\\{\\s*\"translatedText\"\\s*:\\s*\"(.*?)\"\\s*\\s*\\}\\s*]");
        Matcher matcher = pattern.matcher(v);
        boolean matchFound = matcher.find();

        String tr = "";

        if (matchFound) {
            tr = matcher.group(1);
        }

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
