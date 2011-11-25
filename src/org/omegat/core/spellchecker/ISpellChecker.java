/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2008 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.spellchecker;

import java.util.List;

/**
 * Interface for access to spell checker.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public interface ISpellChecker {

    /** Initialize spell checker for current project. */
    void initialize();

    /** Destroy internal cache and free memory. */
    void destroy();

    /** Save the word lists to disk */
    void saveWordLists();

    /**
     * Check the word. If it is ignored or learned (valid), returns true.
     * Otherwise false.
     */
    boolean isCorrect(String word);

    /**
     * return a list of strings as suggestions
     */
    List<String> suggest(String word);

    /**
     * Add a word to the list of correct words
     */
    void learnWord(String word);

    /**
     * Add a word to the list of ignored words
     */
    void ignoreWord(String word);
}
