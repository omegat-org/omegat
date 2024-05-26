/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2010 Alex Buloichik
 *                2023 Hiroshi Miura
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

package org.omegat.core.spellchecker;

import java.util.Collections;
import java.util.List;

import org.omegat.util.Token;

/**
 * Stub spellchecker which used when other spellcheckers can't be loaded, or
 * dictionary not exists.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Briac Pilpre
 */
public class SpellCheckerDummy implements ISpellCheckerProvider, ISpellChecker {

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void saveWordLists() {
    }

    @Override
    public boolean isCorrect(String word) {
        return true;
    }

    @Override
    public List<String> suggest(String word) {
        return Collections.emptyList();
    }

    @Override
    public void learnWord(String word) {
    }

    @Override
    public void ignoreWord(final String word) {
    }

    @Override
    public List<Token> getMisspelledTokens(final String text) {
        return Collections.emptyList();
    }

    @Override
    public boolean isIgnoredWord(final String word) {
        return false;
    }

    @Override
    public boolean isLearnedWord(final String word) {
        return false;
    }
}
