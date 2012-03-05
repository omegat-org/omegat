/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
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

package org.omegat.core.segmentation;

import java.util.HashMap;
import java.util.Map;

import org.omegat.util.OStrings;

/**
 * Code-Key mappings for segmentation code.
 * 
 * @since 1.6.0 RC 9
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public final class LanguageCodes {
    // Language Codes
    public static final String CATALAN_CODE = "Catalan";
    public static final String ENGLISH_CODE = "English";
    public static final String SPANISH_CODE = "Spanish";
    public static final String GERMAN_CODE = "German";
    public static final String ITALIAN_CODE = "Italian";
    public static final String JAPANESE_CODE = "Japanese";
    public static final String DUTCH_CODE = "Dutch";
    public static final String POLISH_CODE = "Polish";
    public static final String RUSSIAN_CODE = "Russian";
    public static final String DEFAULT_CODE = "Default";
    public static final String F_TEXT_CODE = "Text";
    public static final String F_HTML_CODE = "HTML";

    // Language Keys from Resource Bundle
    public static final String CATALAN_KEY = "CORE_SRX_RULES_LANG_CATALAN";
    public static final String ENGLISH_KEY = "CORE_SRX_RULES_LANG_ENGLISH";
    public static final String SPANISH_KEY = "CORE_SRX_RULES_LANG_SPANISH";
    public static final String GERMAN_KEY = "CORE_SRX_RULES_LANG_GERMAN";
    public static final String ITALIAN_KEY = "CORE_SRX_RULES_LANG_ITALIAN";
    public static final String JAPANESE_KEY = "CORE_SRX_RULES_LANG_JAPANESE";
    public static final String DUTCH_KEY = "CORE_SRX_RULES_LANG_DUTCH";
    public static final String POLISH_KEY = "CORE_SRX_RULES_LANG_POLISH";
    public static final String RUSSIAN_KEY = "CORE_SRX_RULES_LANG_RUSSIAN";
    public static final String DEFAULT_KEY = "CORE_SRX_RULES_LANG_DEFAULT";
    public static final String F_TEXT_KEY = "CORE_SRX_RULES_FORMATTING_TEXT";
    public static final String F_HTML_KEY = "CORE_SRX_RULES_FORMATTING_HTML";

    /** A Map from language codes to language keys. */
    private static Map<String, String> codeKeyHash = new HashMap<String, String>();

    static {
        codeKeyHash.put(CATALAN_CODE, CATALAN_KEY);
        codeKeyHash.put(ENGLISH_CODE, ENGLISH_KEY);
        codeKeyHash.put(SPANISH_CODE, SPANISH_KEY);
        codeKeyHash.put(GERMAN_CODE, GERMAN_KEY);
        codeKeyHash.put(ITALIAN_CODE, ITALIAN_KEY);
        codeKeyHash.put(JAPANESE_CODE, JAPANESE_KEY);
        codeKeyHash.put(DUTCH_CODE, DUTCH_KEY);
        codeKeyHash.put(POLISH_CODE, POLISH_KEY);
        codeKeyHash.put(RUSSIAN_CODE, RUSSIAN_KEY);
        codeKeyHash.put(DEFAULT_CODE, DEFAULT_KEY);
        codeKeyHash.put(F_TEXT_CODE, F_TEXT_KEY);
        codeKeyHash.put(F_HTML_CODE, F_HTML_KEY);
    }

    /**
     * Returns localized language name for a given language code.
     * 
     * @param code
     *            language code
     */
    public static String getLanguageName(String code) {
        if (!codeKeyHash.containsKey(code))
            return code;
        String key = codeKeyHash.get(code);
        return OStrings.getString(key);
    }
}
