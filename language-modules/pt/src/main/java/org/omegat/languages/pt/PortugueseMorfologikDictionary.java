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

import org.languagetool.JLanguageTool;
import org.omegat.core.spellchecker.AbstractMorfologikDictionary;

import java.io.InputStream;
import java.util.Arrays;

public class PortugueseMorfologikDictionary extends AbstractMorfologikDictionary {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/pt/spelling/";
    private static final String[] SUPPORTED = { "pt-BR" };

    @Override
    protected String[] getDictionaries() {
        return SUPPORTED;
    }

    @Override
    protected String getDictionary(String language) {
        String target = language.replace("_", "-");
        return Arrays.stream(getDictionaries()).filter(lang -> lang.startsWith(target)).findFirst().orElse(null);
    }

    @Override
    protected InputStream getResourceAsStream(final String resource) {
        return JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + resource);
    }
}
