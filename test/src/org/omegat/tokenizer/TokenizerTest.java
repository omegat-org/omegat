/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

package org.omegat.tokenizer;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Token;

public class TokenizerTest {

    @Test
    public void testEnglish() {
        ITokenizer tok = new LuceneEnglishTokenizer();
        String orig = "The quick, brown <x0/> jumped over 1 \"lazy\" dog.";
        assertVerbatim(new String[] { "The", " ", "quick", ",", " ", "brown", " ", "<x0/>", " ",
                "jumped", " ", "over", " ", "1", " ", "\"", "lazy", "\"", " ", "dog", "." },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        assertResult(new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "the", "quick", "brown", "x0", "jump", "jumped", "over", "1", "lazi", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "quick", "brown", "jump", "jumped", "over", "lazi", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }

    /**
     * The DefaultTokenizer has a completely different implementation from the
     * Lucene-base tokenizers (the latter were originally an external plugin,
     * for licensing reasons). It's based on Java's BreakIterator. It warrants
     * testing so that it doesn't get overlooked when changes are made to the
     * other tokenizers.
     */
    @Test
    public void testDefault() {
        ITokenizer tok = new DefaultTokenizer();
        String orig = "The quick, brown <x0/> jumped over 1 \"lazy\" \u0130stanbul. "
                + "\u65E5\u672C\u8A9E\u3042\u3044\u3046\u3048\u304A\u3002";
        assertVerbatim(new String[] { "The", " ", "quick", ",", " ", "brown", " ", "<x0/>", " ",
                "jumped", " ", "over", " ", "1", " ", "\"", "lazy", "\"", " ", "\u0130stanbul", ".",
                " ", "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A", "\u3002" },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        assertResult(new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }

    private void assertVerbatim(String[] expected, String[] test, Token[] testTok, String origString) {
        assertResult(expected, test);
        assertEquals(StringUtils.join(expected), StringUtils.join(test));
        assertEquals(expected.length, testTok.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], testTok[i].getTextFromString(origString));
        }
    }

    private void assertResult(String[] expected, String[] test) {
//        for (String s : test) {
//            System.out.print('"');
//            System.out.print(s.replace("\"", "\\\""));
//            System.out.print("\", ");
//        }
//        System.out.print('\n');
        assertEquals(expected.length, test.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], test[i]);
        }
    }
}
