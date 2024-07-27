/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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

import org.omegat.core.spellchecker.DictionaryBroker;
import org.omegat.languagetools.LanguageManager;

import java.net.URISyntaxException;

public class GermanPlugin {

    private static final String AUSTRARIAN_GERMAN = "org.languagetool.language.AustrianGerman";
    private static final String GERMANY_GERMAN = "org.languagetool.language.GermanyGerman";
    private static final String SWISS_GERMAN = "org.languagetool.language.SwissGerman";
    private static final String RESOURCE_PATH = "/org/omegat/languages/de/";
    private static final String[] languages = new String[] {"de_AT", "de_CH", "de_DE"};

    private GermanPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage(AUSTRARIAN_GERMAN);
        LanguageManager.registerLTLanguage(GERMANY_GERMAN);
        LanguageManager.registerLTLanguage(SWISS_GERMAN);
        try {
            for (String lang : languages) {
                DictionaryBroker.registerDictionary(GermanPlugin.class.getResource(RESOURCE_PATH + lang + ".dic").toURI());
                DictionaryBroker.registerDictionary(GermanPlugin.class.getResource(RESOURCE_PATH + lang + ".aff").toURI());
            }
        } catch (URISyntaxException ignored) {
        }
    }

    public static void unloadPlugins() {
    }

}
