/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2009 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.spellchecker;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An interface with the Hunspell (http://hunspell.sourceforge.net) spell
 * checking library.
 * 
 * @author Zoltan Bartko bartkozoltan@bartkozoltan.com
 * @author Didier Briel
 */
public interface Hunspell extends Library {

    /**
     * create the spell checker
     * 
     * @param aff
     *            : the affix file
     * @param dic
     *            : the dictionary file
     */
    public Pointer Hunspell_create(String aff, String dic);

    /**
     * destroy the spell checker
     */
    public void Hunspell_destroy(Pointer pHunspell);

    /**
     * spell(word) - spellcheck word output: 0 = bad word, not 0 = good word
     */
    public int Hunspell_spell(Pointer pHunspell, byte[] word);

    /**
     * get the dictionary encoding
     */
    public String Hunspell_get_dic_encoding(Pointer pHunspell);

    /**
     * suggest(suggestions, word) - search suggestions input: pointer to an
     * array of strings pointer and the (bad) word array of strings pointer
     * (here *slst) may not be initialized output: number of suggestions in
     * string array, and suggestions in a newly allocated array of strings
     * (*slts will be NULL when number of suggestion equals 0.)
     */
    public int Hunspell_suggest(Pointer pHunspell, PointerByReference slst, byte[] word);

    /**
     * put a word into the custom dictionary
     */
    public int Hunspell_put_word(Pointer pHunspell, byte[] word);

    /**
     * put a word into the custom dictionary same function as put_word, renamed
     * as of Hunspell 1.2 (01/11/2007)
     */
    public int Hunspell_add(Pointer pHunspell, byte[] word);

}
