/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.tokenizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.junit.Test;
import org.omegat.util.Token;

public class DefaultTokenizerTest {

    @Test
    public void testContains() {
        String text = "The quick brown fox jumped over the lazy dog.";
        Token[] tokensList = new DefaultTokenizer().tokenizeVerbatim(text);
        for (Token tok : toTokArr(text)) {
            assertTrue(DefaultTokenizer.isContains(tokensList, tok));
        }
        assertFalse(DefaultTokenizer.isContains(tokensList, new Token("elephant", 0)));
    }

    @Test
    public void testContainsAll() {
        String text = "The quick brown fox jumped over the lazy dog.";
        Token[] tokensList = new DefaultTokenizer().tokenizeVerbatim(text);
        Token[] listForFind = toTokArr(text);
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, listForFind, true));
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, listForFind, false));

        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("The quick brown"), true));
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("The quick brown"), false));
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("the lazy dog"), true));
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("the lazy dog"), false));

        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("The brown"), true));
        assertFalse(DefaultTokenizer.isContainsAll(tokensList, toTokArr("The brown"), false));
        assertTrue(DefaultTokenizer.isContainsAll(tokensList, toTokArr("the dog"), true));
        assertFalse(DefaultTokenizer.isContainsAll(tokensList, toTokArr("the dog"), false));
    }

    private static Token[] toTokArr(String str) {
        return Stream.of(str.split("\\b")).map(w -> new Token(w, 0)).toArray(Token[]::new);
    }
}
