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

package org.omegat.languages.sv;

import java.net.URISyntaxException;
import java.net.URL;

import org.omegat.core.spellchecker.DictionaryBroker;
import org.omegat.languagetools.LanguageManager;


public final class SwedishPlugin {

    private static final String SWEDISH = "org.languagetool.language.Swedish";
    private static final String RESOURCE_PATH = "/org/omegat/languages/sv/";

    public SwedishPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage(SWEDISH);
        try {
            URL url = SwedishPlugin.class.getResource(RESOURCE_PATH + "sv_FI.dic");
            DictionaryBroker.registerDictionary(url.toURI());
            url = SwedishPlugin.class.getResource(RESOURCE_PATH + "sv_FI.aff");
            DictionaryBroker.registerDictionary(url.toURI());
            url = SwedishPlugin.class.getResource(RESOURCE_PATH + "sv_SE.dic");
            DictionaryBroker.registerDictionary(url.toURI());
            url = SwedishPlugin.class.getResource(RESOURCE_PATH + "sv_SE.aff");
            DictionaryBroker.registerDictionary(url.toURI());
         } catch (URISyntaxException ignored) {
        }

    }

    public static void unloadPlugins() {
    }

}
