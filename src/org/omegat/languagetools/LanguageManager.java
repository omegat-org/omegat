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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.Language;
import org.languagetool.Languages;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;


public final class LanguageManager {

    private static final Set<String> LT_LANGUAGE_CLASSES = new HashSet<>();

    private LanguageManager() {}

    public static void registerLTLanguage(String fqcn) {
        LT_LANGUAGE_CLASSES.add(fqcn);
    }

    static Language getLTLanguage(String omLang, String omCountry) {
        if (omLang == null) {
            return null;
        }
        // Search for full xx-YY match
        for (String fqcn : LT_LANGUAGE_CLASSES) {
            Language ltLang = Languages.getOrAddLanguageByClassName(fqcn);
            if (omLang.equalsIgnoreCase(ltLang.getShortCode())) {
                List<String> countries = Arrays.asList(ltLang.getCountries());
                if (countries.contains(omCountry)) {
                    return ltLang;
                }
            }
        }

        // Search for just xx match
        for (String fqcn : LT_LANGUAGE_CLASSES) {
            Language ltLang = Languages.getOrAddLanguageByClassName(fqcn);
            if (omLang.equalsIgnoreCase(ltLang.getShortCode())) {
                return ltLang;
            }
        }
        return null;
    }

    static org.omegat.util.Language getProjectTargetLanguage() {
        if (Core.getProject() == null) {
            return null;
        }
        ProjectProperties prop = Core.getProject().getProjectProperties();
        if (prop == null) {
            return null;
        }
        return prop.getTargetLanguage();
    }
}
