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

package org.omegat.languages.pt;

import org.omegat.core.spellchecker.SpellCheckDictionaryType;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.languagetools.LanguageManager;

public final class PortuguesePlugin {

    private static final String PORTUGUESE = "org.languagetool.language.Portuguese";
    private static final String PORTUGAL_PORTUGUESE = "org.languagetool.language.PortugalPortuguese";
    private static final String BRAZILIAN_PORTUGUESE = "org.languagetool.language.BrazilianPortuguese";
    private static final String ANGOLA_PORTUGUESE = "org.languagetool.language.AngolaPortuguese";
    private static final String MOZAMBIQUE_PORTUGUESE = "org.languagetool.language.MozambiquePortuguese";
    private static final String MORFOLOGIK_DICTIONARY = "org.omegat.languages.pt.PortugueseMorfologikDictionary";

    private PortuguesePlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage("pt", PORTUGUESE);
        LanguageManager.registerLTLanguage("pt-PT", PORTUGAL_PORTUGUESE);
        LanguageManager.registerLTLanguage("pt-BR", BRAZILIAN_PORTUGUESE);
        LanguageManager.registerLTLanguage("pt-AO", ANGOLA_PORTUGUESE);
        LanguageManager.registerLTLanguage("pt-MZ", MOZAMBIQUE_PORTUGUESE);
        SpellCheckerManager.registerSpellCheckerDictionaryProvider("pt_BR",
                SpellCheckDictionaryType.MORFOLOGIK, MORFOLOGIK_DICTIONARY);
    }

    public static void unloadPlugins() {
    }

}
