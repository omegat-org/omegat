/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.spellchecker;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.languagetool.rules.spelling.hunspell.DumontsHunspellDictionary;
import org.languagetool.rules.spelling.hunspell.HunspellDictionary;

import org.omegat.core.spellchecker.ISpellCheckerProvider;

/**
 * A thin wrapper around the LanguageTool Hunspell implementation (which itself
 * wraps native libs)
 *
 * @author Aaron Madlon-Kay
 * @author Briac Pilpre
 */
public class SpellCheckerLangToolHunspell implements ISpellCheckerProvider {
    private final HunspellDictionary dict;

    public SpellCheckerLangToolHunspell(final File dictName, final File affixName) {
        this.dict = new DumontsHunspellDictionary(dictName.toPath(), affixName.toPath());
    }

    @Override
    public boolean isCorrect(String word) {
        return dict.spell(word);
    }

    @Override
    public List<String> suggest(String word) {
        return dict.suggest(word);
    }

    @Override
    public void learnWord(String word) {
        dict.add(word);
    }

    @Override
    public void destroy() {
        try {
            dict.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
