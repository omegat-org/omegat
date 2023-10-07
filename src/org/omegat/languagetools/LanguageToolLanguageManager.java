package org.omegat.languagetools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.Language;

import org.omegat.core.Core;


public final class LanguageToolLanguageManager {

    private static final Set<Language> LT_LANGUAGE_CLASS_MAP = new HashSet<>();

    private LanguageToolLanguageManager() {}

    public static void registerLTLanguage(Language language) {
        LT_LANGUAGE_CLASS_MAP.add(language);
    }

    static Language getLTLanguage(org.omegat.util.Language lang) {
        // Search for full xx-YY match
        String omLang = lang.getLanguageCode();
        String omCountry = lang.getCountryCode();
        for (Language ltLang : LT_LANGUAGE_CLASS_MAP) {
            if (omLang.equalsIgnoreCase(ltLang.getShortCode())) {
                List<String> countries = Arrays.asList(ltLang.getCountries());
                if (countries.contains(omCountry)) {
                    return ltLang;
                }
            }
        }

        // Search for just xx match
        for (Language ltLang : LT_LANGUAGE_CLASS_MAP) {
            if (omLang.equalsIgnoreCase(ltLang.getShortCode())) {
                return ltLang;
            }
        }
        return null;
    }

    static org.omegat.util.Language getProjectTargetLanguage() {
        return Core.getProject().getProjectProperties().getTargetLanguage();
    }
}
