/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2024 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.languages.en;

import java.io.IOException;

import org.omegat.languagetools.LanguageManager;

public final class EnglishPlugin {

    private static final String ENGLISH = "org.languagetool.language.English";
    private static final String AMERICAN_ENGLISH = "org.languagetool.language.AmericanEnglish";
    private static final String BRITISH_ENGLISH = "org.languagetool.language.BritishEnglish";
    private static final String CANADIAN_ENGLISH = "org.languagetool.language.CanadianEnglish";
    private static final String AUSTRALIAN_ENGLISH = "org.languagetool.language.AustralianEnglish";

    private EnglishPlugin() {
    }

    public static void loadPlugins() throws IOException {
        LanguageManager.registerLTLanguage("en", ENGLISH);
        LanguageManager.registerLTLanguage("en-US", AMERICAN_ENGLISH);
        LanguageManager.registerLTLanguage("en-GB", BRITISH_ENGLISH);
        LanguageManager.registerLTLanguage("en-CA", CANADIAN_ENGLISH);
        LanguageManager.registerLTLanguage("en-AU", AUSTRALIAN_ENGLISH);
    }

    public static void unloadPlugins() {
    }

}
