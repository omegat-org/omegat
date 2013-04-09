/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import java.util.Collections;
import java.util.List;

/**
 * Dummy spellchecker which used when other spellcheckers can't be loaded, or
 * dictionary not exist.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerDummy implements ISpellCheckerProvider {
    public void destroy() {
    }

    public boolean isCorrect(String word) {
        return true;
    }

    public List<String> suggest(String word) {
        return Collections.EMPTY_LIST;
    }

    public void learnWord(String word) {
    }
}
