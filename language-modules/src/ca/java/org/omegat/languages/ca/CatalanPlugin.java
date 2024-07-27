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

package org.omegat.languages.ca;

import org.omegat.core.spellchecker.DictionaryBroker;
import org.omegat.languagetools.LanguageManager;

import java.net.URISyntaxException;

public final class CatalanPlugin {

    private static final String CATALAN = "org.languagetool.language.Catalan";
    private static final String RESOURCE_PATH = "/org/omegat/languages/ca/";

    private CatalanPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage(CATALAN);
        try {
            DictionaryBroker.registerDictionary(CatalanPlugin.class.getResource(RESOURCE_PATH + "ca.dic").toURI());
            DictionaryBroker.registerDictionary(CatalanPlugin.class.getResource(RESOURCE_PATH + "ca.aff").toURI());
            DictionaryBroker.registerDictionary(CatalanPlugin.class.getResource(
                    RESOURCE_PATH + "ca-valencia.dic").toURI());
            DictionaryBroker.registerDictionary(CatalanPlugin.class.getResource(
                    RESOURCE_PATH + "ca-valencia.aff").toURI());
        } catch (URISyntaxException ignored) {
        }
    }

    public static void unloadPlugins() {
    }

}
