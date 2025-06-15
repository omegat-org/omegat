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

package org.omegat.languages.de;

import org.omegat.core.spellchecker.SpellCheckDictionaryType;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.languagetools.LanguageManager;

public final class GermanPlugin {

    private static final String AUSTRARIAN_GERMAN = "org.languagetool.language.AustrianGerman";
    private static final String GERMANY_GERMAN = "org.languagetool.language.GermanyGerman";
    private static final String SWISS_GERMAN = "org.languagetool.language.SwissGerman";
    private static final String HUNSPELL_DICTIONARY = "org.omegat.languages.de.GermanHunspellDictionary";
    private static final String MORFOLOGIK_DICTIONARY = "org.omegat.languages.de.GermanMorfologikDictionary";

    private GermanPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage("de-AT", AUSTRARIAN_GERMAN);
        LanguageManager.registerLTLanguage("de-DE", GERMANY_GERMAN);
        LanguageManager.registerLTLanguage("de-CH", SWISS_GERMAN);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_AT", SpellCheckDictionaryType.HUNSPELL,
                HUNSPELL_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_DE", SpellCheckDictionaryType.HUNSPELL,
                HUNSPELL_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_CH", SpellCheckDictionaryType.HUNSPELL,
                HUNSPELL_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de", SpellCheckDictionaryType.HUNSPELL,
                HUNSPELL_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_AT",
                SpellCheckDictionaryType.MORFOLOGIK, MORFOLOGIK_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_DE",
                SpellCheckDictionaryType.MORFOLOGIK, MORFOLOGIK_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de_CH",
                SpellCheckDictionaryType.MORFOLOGIK, MORFOLOGIK_DICTIONARY);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("de", SpellCheckDictionaryType.MORFOLOGIK,
                MORFOLOGIK_DICTIONARY);
    }

    public static void unloadPlugins() {
    }

}
