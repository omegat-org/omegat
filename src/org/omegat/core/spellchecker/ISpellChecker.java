/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2008 Didier Briel
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

import java.util.List;

import org.omegat.util.Token;

/**
 * Interface for access to a spell checker.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public interface ISpellChecker {

    /** Initialize a spell checker for a current project. */
    boolean initialize();

    /** Destroy internal cache and free memory. */
    void destroy();

    /** Save the word lists to disk. */
    void saveWordLists();

    /**
     * Check the word. If it is ignored or learned (valid), returns true.
     * Otherwise, false.
     */
    boolean isCorrect(String word);

    /**
     * return a list of strings as suggestions.
     */
    List<String> suggest(String word);

    /**
     * Add a word to the list of correct words.
     */
    void learnWord(String word);

    /**
     * Add a word to the list of ignored words.
     */
    void ignoreWord(String word);

    /**
     * Get a list of misspelled tokens from the given text.
     */
    List<Token> getMisspelledTokens(String text);

    /**
     * Determine if the given word is on the ignored list.
     */
    boolean isIgnoredWord(String word);

    /**
     * Determine if the given word is on the learned list.
     */
    boolean isLearnedWord(String word);

}
