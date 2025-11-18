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
package org.omegat.languages.br;

import java.io.IOException;
import java.io.InputStream;

import morfologik.stemming.Dictionary;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

@NullMarked
public class BretonMorfologikDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/br/hunspell/";

    private InputStream infoInputStream;
    private InputStream dictInputStream;

    @Override
    public @Nullable Dictionary getMorfologikDictionary(String language) {
        if ("br_FR".startsWith(language)) {
            infoInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "br_FR.info");
            dictInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "br_FR.dict");
            if (infoInputStream == null || dictInputStream == null) {
                return null;
            }
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
