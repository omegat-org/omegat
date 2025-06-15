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

import java.io.InputStream;

import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.AbstractMorfologikDictionary;

public class EnglishMorfologikDictionary extends AbstractMorfologikDictionary {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/en/hunspell/";
    private static final String[] SUPPORTED = { "en_GB", "en_US", "en_AU", "en_CA", "en_NZ", "en_ZA" };

    @Override
    protected String[] getDictionaries() {
        return SUPPORTED;
    }

    @Override
    protected InputStream getResourceAsStream(final String resource) {
        return JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + resource);
    }
}
