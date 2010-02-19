/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of Google Translate machine translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class GoogleTranslate extends BaseTranslate {
    protected static final String GT_URL = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=#sourceLang#|#targetLang#&q=";
    protected static final String MARK_BEG = "{\"translatedText\":\"";
    protected static final String MARK_END = "\"}";
    protected static final Pattern RE_UNICODE = Pattern
            .compile("\\\\u([0-9A-Fa-f]{4})");
    protected static final Pattern RE_HTML = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_GOOGLE_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_GOOGLE");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text)
            throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..."
                : text;
        String url = GT_URL.replace("#sourceLang#", sLang.getLanguageCode())
                .replace("#targetLang#", tLang.getLanguageCode())
                + URLEncoder.encode(trText, "UTF-8");

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
        return tr;
    }
}
