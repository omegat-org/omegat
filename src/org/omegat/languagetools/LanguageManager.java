/*******************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
 ******************************************************************************/
package org.omegat.languagetools;

import java.util.HashMap;
import java.util.Map;

import org.languagetool.Language;
import org.languagetool.Languages;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;

public final class LanguageManager {

    private static final Map<String, String> LT_LANGUAGE_CLASSES = new HashMap<>();

    private LanguageManager() {
    }

    public static void registerLTLanguage(String lang, String fqcn) {
        LT_LANGUAGE_CLASSES.put(lang, fqcn);
    }

    /**
     * Get LanguageTool language.
     * <p>
     *     This code made side effect to load LT modules eventually.
     *     It loads a specified language-country.
     *     It also loads a variant of countries, such as "en-US" for "en-AU".
     *     And also it loads language code module such as "en".
     *     It is because some language definition depends on another language,
     *     for example, en-AU depends on en-US.
     *     When loading only en-AU language module class, it failed to load
     *     with an error, not-found "en-US".
     * </p>
     * @param lang OmegaT language code.
     * @return LanguageTool's Language object.
     */
    public static Language getLTLanguage(org.omegat.util.Language lang) {
        Language result = null;
        if (lang == null) {
            return null;
        }
        // search for language-country code.
        String fqcn = LT_LANGUAGE_CLASSES.get(lang.getLanguage());
        if (fqcn != null) {
            result = Languages.getOrAddLanguageByClassName(fqcn);
        }
        // Search for language code
        fqcn = LT_LANGUAGE_CLASSES.get(lang.getLanguageCode());
        if (fqcn != null) {
            // when exists, load it.
            Language language = Languages.getOrAddLanguageByClassName(fqcn);
            if (result == null) {
                result = language;
            }
        }
        // Search for just language code match but allow country difference
        String languageCode;
        for (Map.Entry<String, String> entry : LT_LANGUAGE_CLASSES.entrySet()) {
            if (entry.getKey().contains("-")) {
                languageCode = entry.getKey().substring(0, entry.getKey().indexOf('-'));
            } else {
                languageCode = entry.getKey();
            }
            if (languageCode.equals(lang.getLanguageCode())) {
                // when exists, load it.
                Language language = Languages.getOrAddLanguageByClassName(entry.getValue());
                if (result == null) {
                    result = language;
                }
            }
        }
        return result;
    }

    static Language getLTLanguage() {
        if (Core.getProject() == null) {
            return null;
        }
        ProjectProperties prop = Core.getProject().getProjectProperties();
        if (prop == null) {
            return null;
        }
        org.omegat.util.Language omLang = prop.getTargetLanguage();
        if (omLang == null) {
            return null;
        }
        Language lang = getLTLanguage(omLang);
        if (lang == null) {
            lang = getLTLanguage(new org.omegat.util.Language("en-US"));
        }
        return lang;
    }
}
