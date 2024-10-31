/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

    private LanguageCodes() {
    }

    // Codes of "languagerulename".
    static final String CATALAN_CODE = "Catalan";
    static final String CZECH_CODE = "Czech";
    static final String GERMAN_CODE = "German";
    static final String ENGLISH_CODE = "English";
    static final String SPANISH_CODE = "Spanish";
    static final String FINNISH_CODE = "Finnish";
    static final String FRENCH_CODE = "French";
    static final String ITALIAN_CODE = "Italian";
    static final String JAPANESE_CODE = "Japanese";
    static final String DUTCH_CODE = "Dutch";
    static final String POLISH_CODE = "Polish";
    static final String RUSSIAN_CODE = "Russian";
    static final String SWEDISH_CODE = "Swedish";
    static final String SLOVAK_CODE = "Slovak";
    static final String CHINESE_CODE = "Chinese";
    static final String DEFAULT_CODE = "Default";
    static final String F_TEXT_CODE = "Text";
    static final String F_HTML_CODE = "HTML";

    // Language Keys from Resource Bundle
    static final String CATALAN_KEY = "CORE_SRX_RULES_LANG_CATALAN";
    static final String CZECH_KEY = "CORE_SRX_RULES_LANG_CZECH";
    static final String GERMAN_KEY = "CORE_SRX_RULES_LANG_GERMAN";
    static final String ENGLISH_KEY = "CORE_SRX_RULES_LANG_ENGLISH";
    static final String SPANISH_KEY = "CORE_SRX_RULES_LANG_SPANISH";
    static final String FINNISH_KEY = "CORE_SRX_RULES_LANG_FINNISH";
    static final String FRENCH_KEY = "CORE_SRX_RULES_LANG_FRENCH";
    static final String ITALIAN_KEY = "CORE_SRX_RULES_LANG_ITALIAN";
    static final String JAPANESE_KEY = "CORE_SRX_RULES_LANG_JAPANESE";
    static final String DUTCH_KEY = "CORE_SRX_RULES_LANG_DUTCH";
    static final String POLISH_KEY = "CORE_SRX_RULES_LANG_POLISH";
    static final String RUSSIAN_KEY = "CORE_SRX_RULES_LANG_RUSSIAN";
    static final String SWEDISH_KEY = "CORE_SRX_RULES_LANG_SWEDISH";
    static final String SLOVAK_KEY = "CORE_SRX_RULES_LANG_SLOVAK";
    static final String CHINESE_KEY = "CORE_SRX_RULES_LANG_CHINESE";
    static final String DEFAULT_KEY = "CORE_SRX_RULES_LANG_DEFAULT";
    static final String F_TEXT_KEY = "CORE_SRX_RULES_FORMATTING_TEXT";
    static final String F_HTML_KEY = "CORE_SRX_RULES_FORMATTING_HTML";

    private static final String CATALAN_PATTERN = "CA.*";
    private static final String CZECH_PATTERN = "CS.*";
    private static final String GERMAN_PATTERN = "DE.*";
    private static final String ENGLISH_PATTERN = "EN.*";
    private static final String SPANISH_PATTERN = "ES.*";
    private static final String FINNISH_PATTERN = "FI.*";
    private static final String FRENCH_PATTERN = "FR.*";
    private static final String ITALIAN_PATTERN = "IT.*";
    private static final String JAPANESE_PATTERN = "JA.*";
    private static final String DUTCH_PATTERN = "NL.*";
    private static final String POLISH_PATTERN = "PL.*";
    private static final String RUSSIAN_PATTERN = "RU.*";
    private static final String SWEDISH_PATTERN = "SV.*";
    private static final String SLOVAK_PATTERN = "SK.*";
    private static final String CHINESE_PATTERN = "ZH.*";

    /** A Map from language codes to language keys. */
    private static final Map<String, String> codeKeyHash = new HashMap<>();
    private static final Map<String, String> patternHash = new HashMap<>();

    static {
        codeKeyHash.put(CATALAN_CODE, CATALAN_KEY);
        codeKeyHash.put(CZECH_CODE, CZECH_KEY);
        codeKeyHash.put(GERMAN_CODE, GERMAN_KEY);
        codeKeyHash.put(ENGLISH_CODE, ENGLISH_KEY);
        codeKeyHash.put(SPANISH_CODE, SPANISH_KEY);
        codeKeyHash.put(FINNISH_CODE, FINNISH_KEY);
        codeKeyHash.put(FRENCH_CODE, FRENCH_KEY);
        codeKeyHash.put(ITALIAN_CODE, ITALIAN_KEY);
        codeKeyHash.put(JAPANESE_CODE, JAPANESE_KEY);
        codeKeyHash.put(DUTCH_CODE, DUTCH_KEY);
        codeKeyHash.put(POLISH_CODE, POLISH_KEY);
        codeKeyHash.put(RUSSIAN_CODE, RUSSIAN_KEY);
        codeKeyHash.put(SWEDISH_CODE, SWEDISH_KEY);
        codeKeyHash.put(SLOVAK_CODE, SLOVAK_KEY);
        codeKeyHash.put(CHINESE_CODE, CHINESE_KEY);
        codeKeyHash.put(DEFAULT_CODE, DEFAULT_KEY);
        codeKeyHash.put(F_TEXT_CODE, F_TEXT_KEY);
        codeKeyHash.put(F_HTML_CODE, F_HTML_KEY);
        patternHash.put(CATALAN_PATTERN, CATALAN_CODE);
        patternHash.put(CZECH_PATTERN, CZECH_CODE);
        patternHash.put(GERMAN_PATTERN, GERMAN_CODE);
        patternHash.put(ENGLISH_PATTERN, ENGLISH_CODE);
        patternHash.put(SPANISH_PATTERN, SPANISH_CODE);
        patternHash.put(FINNISH_PATTERN, FINNISH_CODE);
        patternHash.put(FRENCH_PATTERN, FRENCH_CODE);
        patternHash.put(ITALIAN_PATTERN, ITALIAN_CODE);
        patternHash.put(JAPANESE_PATTERN, JAPANESE_CODE);
        patternHash.put(DUTCH_PATTERN, DUTCH_CODE);
        patternHash.put(POLISH_PATTERN, POLISH_CODE);
        patternHash.put(RUSSIAN_PATTERN, RUSSIAN_CODE);
        patternHash.put(SWEDISH_PATTERN, SWEDISH_CODE);
        patternHash.put(SLOVAK_PATTERN, SLOVAK_CODE);
        patternHash.put(CHINESE_PATTERN, CHINESE_CODE);
    }

    /**
     * Returns localized language name for a given language code.
     *
     * @param code
     *            language code
     */
    public static String getLanguageName(String code) {
        if (!codeKeyHash.containsKey(code)) {
            return code;
        }
        String key = codeKeyHash.get(code);
        return OStrings.getString(key);
    }

    public static boolean isLanguageCodeKnown(String code) {
        return codeKeyHash.containsKey(code);
    }

    public static String getLanguageCodeByName(String name) {
        for (Map.Entry<String, String> entry : codeKeyHash.entrySet()) {
            if (OStrings.getString(entry.getValue()).equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getLanguageCodeByPattern(String pattern) {
        return patternHash.get(pattern);
    }
}
