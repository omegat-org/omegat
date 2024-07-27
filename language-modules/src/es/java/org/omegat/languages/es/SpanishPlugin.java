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

package org.omegat.languages.es;

import org.omegat.core.spellchecker.DictionaryBroker;
import org.omegat.languagetools.LanguageManager;

import java.net.URISyntaxException;

public class SpanishPlugin {

    private static final String SPANISH = "org.languagetool.language.Spanish";
    private static final String SPANISH_VOSEO = "org.languagetool.language.SpanishVoseo";

    private static final String RESOURCE_PATH = "/org/omegat/languages/en/";
    private static final String[] languages = new String[] {"es_AR", "es_BO", "es_CL", "es_CR", "es_CU",
    "es_DO", "es_EC", "es_ES", "es_GQ", "es_GT", "es_HN", "es_MX", "es_NI", "es_PA", "es_PE", "es_PH",
    "es_PR", "es_PY", "es_SV", "es_US", "es_UY", "es_VE"};

    private SpanishPlugin() {
    }

    public static void loadPlugins() {
        LanguageManager.registerLTLanguage(SPANISH);
        LanguageManager.registerLTLanguage(SPANISH_VOSEO);
        try {
            for (String lang : languages) {
                DictionaryBroker.registerDictionary(SpanishPlugin.class.getResource(RESOURCE_PATH + lang + ".dic").toURI());
                DictionaryBroker.registerDictionary(SpanishPlugin.class.getResource(RESOURCE_PATH + lang + ".aff").toURI());
            }
        } catch (URISyntaxException ignored) {
        }
    }

    public static void unloadPlugins() {
    }

}
