/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/**
 * JMySpell spell checker implementation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerJMySpell implements ISpellCheckerProvider {
    private org.dts.spell.SpellChecker jmyspell;

    @Override
    public void init(String language) throws SpellCheckerException {
        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellChecker.DEFAULT_DICTIONARY_DIR.getPath());

        File affixName = new File(dictionaryDir, language + OConsts.SC_AFFIX_EXTENSION);
        File dictionaryName = new File(dictionaryDir, language + OConsts.SC_DICTIONARY_EXTENSION);

        if (!SpellChecker.isValidFile(affixName) || !SpellChecker.isValidFile(dictionaryName)) {
            throw new SpellCheckerException("No dictionary found at " + affixName + " or " + dictionaryName);
        }

        try {
            SpellDictionary dict = new OpenOfficeSpellDictionary(dictionaryName, affixName, false);
            jmyspell = new org.dts.spell.SpellChecker(dict);
            jmyspell.setCaseSensitive(false);
        } catch (IOException e) {
            throw new SpellCheckerException("Error loading jmyspell dictionary", e);
        }
        
        Log.log("Initialized jMySpell spell checker for language '" + language + "' dictionary "
                + dictionaryName);
    }

    @Override
    public void destroy() {
        jmyspell = null;
    }

    @Override
    public boolean isCorrect(String word) {
        return jmyspell.isCorrect(word);
    }

    @Override
    public List<String> suggest(String word) {
        return jmyspell.getDictionary().getSuggestions(word, 20);
    }

    @Override
    public void learnWord(String word) {
    }
}
