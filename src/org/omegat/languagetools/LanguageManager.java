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

    static Language getLTLanguage(org.omegat.util.Language lang) {
        if (lang == null) {
            return null;
        }
        String fqcn = LT_LANGUAGE_CLASSES.get(lang.getLanguage());
        if (fqcn != null) {
            return Languages.getOrAddLanguageByClassName(fqcn);
        }
        // Search for language code
        fqcn = LT_LANGUAGE_CLASSES.get(lang.getLanguageCode());
        if (fqcn != null) {
            return Languages.getOrAddLanguageByClassName(fqcn);
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
                return Languages.getOrAddLanguageByClassName(entry.getValue());
            }
        }
        return null;
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
