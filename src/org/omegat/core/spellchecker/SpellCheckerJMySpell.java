/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.spellchecker;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;

/**
 * JMySpell spell checker implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerJMySpell implements ISpellCheckerProvider {
    /** Local logger. */
    private static final Logger LOGGER = Logger
            .getLogger(SpellCheckerJMySpell.class.getName());

    private org.dts.spell.SpellChecker jmyspell;

    public SpellCheckerJMySpell(String language, String dictionaryName,
            String affixName) throws Exception {

        SpellDictionary dict = new OpenOfficeSpellDictionary(new File(
                dictionaryName), new File(affixName), false);
        jmyspell = new org.dts.spell.SpellChecker(dict);
        jmyspell.setCaseSensitive(false);

        LOGGER.finer("Initialize SpellChecker by JMySpell for language '"
                + language + "' dictionary " + dictionaryName);
    }

    public void destroy() {
        jmyspell = null;
    }

    public boolean isCorrect(String word) {
        return jmyspell.isCorrect(word);
    }

    public List<String> suggest(String word) {
        return jmyspell.getDictionary().getSuggestions(word, 20);
    }

    public void learnWord(String word) {
    }
}
