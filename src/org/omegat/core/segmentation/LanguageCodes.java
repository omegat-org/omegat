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

import org.omegat.util.OStrings;

import java.util.HashMap;
import java.util.Map;

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
    public static final String CATALAN_CODE = "Catalan";
    public static final String CZECH_CODE = "Czech";
    public static final String GERMAN_CODE = "German";
    public static final String ENGLISH_CODE = "English";
    public static final String SPANISH_CODE = "Spanish";
    public static final String FINNISH_CODE = "Finnish";
    public static final String FRENCH_CODE = "French";
    public static final String ITALIAN_CODE = "Italian";
    public static final String JAPANESE_CODE = "Japanese";
    public static final String DUTCH_CODE = "Dutch";
    public static final String POLISH_CODE = "Polish";
    public static final String RUSSIAN_CODE = "Russian";
    public static final String SWEDISH_CODE = "Swedish";
    public static final String SLOVAK_CODE = "Slovak";
    public static final String CHINESE_CODE = "Chinese";
    public static final String DEFAULT_CODE = "Default";
    public static final String F_TEXT_CODE = "Text";
    public static final String F_HTML_CODE = "HTML";

    // Language Keys from Resource Bundle
    public static final String CATALAN_KEY = "CORE_SRX_RULES_LANG_CATALAN";
    public static final String CZECH_KEY = "CORE_SRX_RULES_LANG_CZECH";
    public static final String GERMAN_KEY = "CORE_SRX_RULES_LANG_GERMAN";
    public static final String ENGLISH_KEY = "CORE_SRX_RULES_LANG_ENGLISH";
    public static final String SPANISH_KEY = "CORE_SRX_RULES_LANG_SPANISH";
    public static final String FINNISH_KEY = "CORE_SRX_RULES_LANG_FINNISH";
    public static final String FRENCH_KEY = "CORE_SRX_RULES_LANG_FRENCH";
    public static final String ITALIAN_KEY = "CORE_SRX_RULES_LANG_ITALIAN";
    public static final String JAPANESE_KEY = "CORE_SRX_RULES_LANG_JAPANESE";
    public static final String DUTCH_KEY = "CORE_SRX_RULES_LANG_DUTCH";
    public static final String POLISH_KEY = "CORE_SRX_RULES_LANG_POLISH";
    public static final String RUSSIAN_KEY = "CORE_SRX_RULES_LANG_RUSSIAN";
    public static final String SWEDISH_KEY = "CORE_SRX_RULES_LANG_SWEDISH";
    public static final String SLOVAK_KEY = "CORE_SRX_RULES_LANG_SLOVAK";
    public static final String CHINESE_KEY = "CORE_SRX_RULES_LANG_CHINESE";
    public static final String DEFAULT_KEY = "CORE_SRX_RULES_LANG_DEFAULT";
    public static final String F_TEXT_KEY = "CORE_SRX_RULES_FORMATTING_TEXT";
    public static final String F_HTML_KEY = "CORE_SRX_RULES_FORMATTING_HTML";

    /** A Map from language codes to language keys. */
    private static final Map<String, String> codeKeyHash = new HashMap<>();

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
        if (name == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : codeKeyHash.entrySet()) {
            if (OStrings.getString(entry.getValue()).equals(name)) {
                return entry.getKey();
            }
        }
        // migration heuristics: Germany translation changed in v5.5.
        // See:
        // https://github.com/omegat-org/omegat/pull/1158#issuecomment-2448788253
        if (name.contains("Textdateien")) {
            return LanguageCodes.F_TEXT_CODE;
        }
        return null;
    }

    public static String getLanguageCodeByPattern(String pattern) {
        return patternHash.get(pattern);
    }
}
