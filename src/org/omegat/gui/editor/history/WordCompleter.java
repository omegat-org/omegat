/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.trie4j.patricia.PatriciaTrie;

public class WordCompleter {

    static final int MIN_CHARS = 3;

    private PatriciaTrie data;

    public WordCompleter() {
        reset();
    }

    public void reset() {
        data = new PatriciaTrie();
    }

    public void train(String[] tokens) {
        for (String token : tokens) {
            if (token.codePointCount(0, token.length()) > MIN_CHARS) {
                data.insert(token);
            }
        }
    }

    public List<String> completeWord(String seed) {
        if (data.size() == 0 || seed.codePointCount(0, seed.length()) < MIN_CHARS) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String s : data.predictiveSearch(seed)) {
            if (!s.equalsIgnoreCase(seed)) {
                result.add(s);
            }
        }
        return result;
    }
}
