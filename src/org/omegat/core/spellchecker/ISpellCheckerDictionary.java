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

import java.io.Closeable;

import org.apache.lucene.analysis.util.CharArraySet;

public interface ISpellCheckerDictionary extends Closeable {
    /**
     * Get Hunspell dictionary.
     * @return Dictionary object when the language module has. Otherwise, null.
     */
    org.apache.lucene.analysis.hunspell.Dictionary getHunspellDictionary();

    /**
     * Get Morfologik dictionary.
     * @return Dictionary object when the language module has. Otherwise, null.
     */
    morfologik.stemming.Dictionary getMofologikDictionary();

    enum DictionaryType {
        HUNSPELL, MORFOLOGIK,
    }

    /**
     * Get the stop-words for the language.
     * @return stop-words. If the module privides nothing, return null.
     */
    CharArraySet getStopWordSet();

    /**
     * Get a dictionary type.
     * @return type of dictionary. If the module provides nothing, return null.
     */
    DictionaryType getDictionaryType();
}
