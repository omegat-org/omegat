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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Token;

public class TokenTest extends TestCore {

    /**
     * Basic Token class test.
     * <p>
     * This uses GLOSSARY stemming mode.
     * </p>
     */
    @Test
    public void testGlossaryTokenEqualityEnglish() {
        ITokenizer tok = new LuceneJapaneseTokenizer();
        String str = "source and target";
        String glos = "target";
        Token[] strTokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.GLOSSARY);
        Token[] glosTokens = tok.tokenizeWords(glos, ITokenizer.StemmingMode.GLOSSARY);
        assertEquals(3, strTokens.length);
        assertEquals(1, glosTokens.length);
        assertFalse(strTokens[0].deepEquals(glosTokens[0]));
        assertFalse(strTokens[2].deepEquals(glosTokens[0]));
        assertNotEquals(strTokens[0], glosTokens[0]);
        assertEquals(strTokens[2], glosTokens[0]);
    }

    /**
     * Test case of BUG#1034
     */
    @Test(expected = AssertionError.class)
    public void testGlossaryTokenEqualityJapanese() {
        ITokenizer tok = new LuceneJapaneseTokenizer();
        String str = "\u5834\u6240";
        String glos = "\u5857\u5E03";
        Token[] strTokens = tok.tokenizeWords(str, ITokenizer.StemmingMode.GLOSSARY);
        Token[] glosTokens = tok.tokenizeWords(glos, ITokenizer.StemmingMode.GLOSSARY);
        assertFalse(strTokens[0].deepEquals(glosTokens[0]));
        assertNotEquals(strTokens[0], glosTokens[0]);
    }
}
