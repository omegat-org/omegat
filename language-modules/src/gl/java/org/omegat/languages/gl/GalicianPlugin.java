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

package org.omegat.languages.gl;

import org.omegat.core.spellchecker.DictionaryBroker;
import org.omegat.languagetools.LanguageManager;

import java.net.URISyntaxException;
import java.net.URL;

public class GalicianPlugin {

    private static final String GALICIAN = "org.languagetool.language.Galician";
    private static final String RESOURCE_PATH = "/org/omegat/languages/gl/";

    private GalicianPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage(GALICIAN);
        try {
            URL url = GalicianPlugin.class.getResource(RESOURCE_PATH + "gl_ES.dic");
            DictionaryBroker.registerDictionary(url.toURI());
            url = GalicianPlugin.class.getResource(RESOURCE_PATH + "gl_ES.aff");
            DictionaryBroker.registerDictionary(url.toURI());
        } catch (URISyntaxException ignored) {
        }

    }

    public static void unloadPlugins() {
    }

}
