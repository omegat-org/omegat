/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core.spellchecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import morfologik.stemming.Dictionary;

public abstract class AbstractMorfologikDictionary implements ISpellCheckerDictionary, AutoCloseable {
    private static final String DICT_EXT = ".dict";
    private static final String META_EXT = ".info";

    private InputStream infoInputStream;
    private InputStream dictInputStream;

    protected abstract String[] getDictionaries();

    protected String getDictionary(String language) {
        return Arrays.stream(getDictionaries()).filter(language::startsWith).findFirst().orElse(null);
    }
    protected abstract InputStream getResourceAsStream(String resource);

    @Override
    public morfologik.stemming.Dictionary getMorfologikDictionary(String language) {
        String target = getDictionary(language);
        if (target != null) {
            infoInputStream = getResourceAsStream(target + META_EXT);
            dictInputStream = getResourceAsStream(target + DICT_EXT);
            try {
                return Dictionary.read(dictInputStream, infoInputStream);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    @Override
    public SpellCheckDictionaryType getDictionaryType() {
        return SpellCheckDictionaryType.MORFOLOGIK;
    }

    @Override
    public void close() {
        try {
            infoInputStream.close();
            dictInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
