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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class WordCompleterTest {

    @Test
    public void testWordCompletion() {
        WordCompleter c = new WordCompleter();

        // Completions should be empty before training
        assertEquals(Collections.emptyList(), c.completeWord("foo"));

        c.train("foob foobar foobaz foobiz".split(" "));

        // Shorter than min length
        assertTrue("f".length() < WordCompleter.MIN_CHARS);
        assertEquals(Collections.emptyList(), c.completeWord("f"));

        assertEquals(Arrays.asList("foob foobar foobaz foobiz".split(" ")), c.completeWord("foo"));

        // The word to be completed is not included in results even if it is in
        // the data
        assertEquals(Arrays.asList("foobar foobaz foobiz".split(" ")), c.completeWord("foob"));

        assertEquals(Arrays.asList("foobar foobaz".split(" ")), c.completeWord("fooba"));

        // Prediction is case-sensitive
        assertEquals(Collections.emptyList(), c.completeWord("Fooba"));
    }

    @Test
    public void testReset() {
        WordCompleter c = new WordCompleter();
        c.train("foob foobar foobaz foobiz".split(" "));

        assertEquals(Arrays.asList("foob foobar foobaz foobiz".split(" ")), c.completeWord("foo"));

        c.reset();

        assertEquals(Collections.emptyList(), c.completeWord("foo"));
    }

    @Test
    public void testEmptyInput() {
        WordCompleter c = new WordCompleter();

        try {
            c.train(null);
            fail("Should throw NPE when given null input");
        } catch (NullPointerException e) {
            // OK
        }

        c.train("foob foobar foobaz foobiz".split(" "));

        // Empty string should return empty completions
        assertEquals(Collections.emptyList(), c.completeWord(""));

        try {
            c.completeWord(null);
            fail("Should throw NPE when given null input");
        } catch (NullPointerException e) {
            // OK
        }
    }
}
