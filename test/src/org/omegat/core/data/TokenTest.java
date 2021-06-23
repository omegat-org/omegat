
/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Token;

public class TokenTest {

    /**
     * Test case of BUG#1034
     */
    @Test
    public void testGlossaryTokenEqualityJapanese() {
        ITokenizer tok = new LuceneJapaneseTokenizer();
        String str = "場所";
        String glos = "塗布";
        Token[] fullTextTokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.GLOSSARY);
        Token[] glosTokens = tok.tokenizeWords(glos, ITokenizer.StemmingMode.GLOSSARY);
        assertFalse(fullTextTokens[0].deepEquals(glosTokens[0]));
        assertNotEquals(fullTextTokens[0], glosTokens[0]);
    }

}

