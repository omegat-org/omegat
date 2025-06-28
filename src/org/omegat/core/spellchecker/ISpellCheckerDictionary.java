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

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.nio.file.Path;

public interface ISpellCheckerDictionary extends Closeable {
    /**
     * Get Hunspell dictionary.
     * 
     * @return Dictionary object when the language module has. Otherwise, null.
     */
    default @Nullable org.apache.lucene.analysis.hunspell.Dictionary getHunspellDictionary(String language) {
        return null;
    }

    /**
     * Get Morfologik dictionary.
     * 
     * @return Dictionary object when the language module has. Otherwise, null.
     */
    default @Nullable morfologik.stemming.Dictionary getMorfologikDictionary(String language) {
        return null;
    }

    default @Nullable Path installHunspellDictionary(Path dictionaryDir, String language) {
        return null;
    }

    /**
     * Get a dictionary type.
     * 
     * @return type of dictionary. If the module provides nothing, return null.
     */
    SpellCheckDictionaryType getDictionaryType();
}
