/*
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
               2024-2026 Hiroshi Miura
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
 */

package org.omegat.core.segmentation.util;

import org.jspecify.annotations.Nullable;
import org.omegat.core.segmentation.MapRule;
import org.omegat.util.OStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Code-Key mappings for segmentation code.
 *
 * @since 1.6.0 RC 9
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public final class LanguageCodes {

    private static volatile @Nullable LanguageCodes instance;

    public static LanguageCodes getInstance() {
        if (instance == null) {
            synchronized (LanguageCodes.class) {
                if (instance == null) {
                    instance = new LanguageCodes();
                }
            }
        }
        return Objects.requireNonNull(instance);
    }

    public String getStandardNameFromMapRule(MapRule mr) {
        String language = getLanguageCodeByPattern(mr.getPattern());
        if (language == null) {
            language = getLanguageCodeByName(mr.getLanguage());
        }
        if (language == null) {
            language = mr.getLanguage();
        }
        return language;
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

    /**
     * A Map from language codes to language keys.
     */
    private static final Map<String, String> CODE_KEY_HASH = new HashMap<>();
    private static final Map<String, String> PATTERN_HASH = new HashMap<>();

    static {
        CODE_KEY_HASH.put(CATALAN_CODE, CATALAN_KEY);
        CODE_KEY_HASH.put(CZECH_CODE, CZECH_KEY);
        CODE_KEY_HASH.put(GERMAN_CODE, GERMAN_KEY);
        CODE_KEY_HASH.put(ENGLISH_CODE, ENGLISH_KEY);
        CODE_KEY_HASH.put(SPANISH_CODE, SPANISH_KEY);
        CODE_KEY_HASH.put(FINNISH_CODE, FINNISH_KEY);
        CODE_KEY_HASH.put(FRENCH_CODE, FRENCH_KEY);
        CODE_KEY_HASH.put(ITALIAN_CODE, ITALIAN_KEY);
        CODE_KEY_HASH.put(JAPANESE_CODE, JAPANESE_KEY);
        CODE_KEY_HASH.put(DUTCH_CODE, DUTCH_KEY);
        CODE_KEY_HASH.put(POLISH_CODE, POLISH_KEY);
        CODE_KEY_HASH.put(RUSSIAN_CODE, RUSSIAN_KEY);
        CODE_KEY_HASH.put(SWEDISH_CODE, SWEDISH_KEY);
        CODE_KEY_HASH.put(SLOVAK_CODE, SLOVAK_KEY);
        CODE_KEY_HASH.put(CHINESE_CODE, CHINESE_KEY);
        CODE_KEY_HASH.put(DEFAULT_CODE, DEFAULT_KEY);
        CODE_KEY_HASH.put(F_TEXT_CODE, F_TEXT_KEY);
        CODE_KEY_HASH.put(F_HTML_CODE, F_HTML_KEY);
        PATTERN_HASH.put(CATALAN_PATTERN, CATALAN_CODE);
        PATTERN_HASH.put(CZECH_PATTERN, CZECH_CODE);
        PATTERN_HASH.put(GERMAN_PATTERN, GERMAN_CODE);
        PATTERN_HASH.put(ENGLISH_PATTERN, ENGLISH_CODE);
        PATTERN_HASH.put(SPANISH_PATTERN, SPANISH_CODE);
        PATTERN_HASH.put(FINNISH_PATTERN, FINNISH_CODE);
        PATTERN_HASH.put(FRENCH_PATTERN, FRENCH_CODE);
        PATTERN_HASH.put(ITALIAN_PATTERN, ITALIAN_CODE);
        PATTERN_HASH.put(JAPANESE_PATTERN, JAPANESE_CODE);
        PATTERN_HASH.put(DUTCH_PATTERN, DUTCH_CODE);
        PATTERN_HASH.put(POLISH_PATTERN, POLISH_CODE);
        PATTERN_HASH.put(RUSSIAN_PATTERN, RUSSIAN_CODE);
        PATTERN_HASH.put(SWEDISH_PATTERN, SWEDISH_CODE);
        PATTERN_HASH.put(SLOVAK_PATTERN, SLOVAK_CODE);
        PATTERN_HASH.put(CHINESE_PATTERN, CHINESE_CODE);
    }

    public boolean isLanguageCodeKnown(String code) {
        return CODE_KEY_HASH.containsKey(code);
    }

    /**
     * Returns localized language name for a given language code.
     *
     * @param code
     *            language code
     */
    public String getLanguageName(String code) {
        if (!CODE_KEY_HASH.containsKey(code)) {
            return code;
        }
        String key = CODE_KEY_HASH.get(code);
        return OStrings.getString(key);
    }

    public @Nullable String getLanguageCodeByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : CODE_KEY_HASH.entrySet()) {
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

    public @Nullable String getLanguageCodeByPattern(String pattern) {
        return PATTERN_HASH.get(pattern);
    }
}
