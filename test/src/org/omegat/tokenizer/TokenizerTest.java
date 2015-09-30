/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2015 Aaron Madlon-Kay
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

import org.apache.commons.lang.StringUtils;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Token;

import junit.framework.TestCase;

public class TokenizerTest extends TestCase {

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
        assertResult(new String[] { "the", "quick", "brown", "jump", "jumped", "over", "lazi", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "quick", "brown", "jump", "jumped", "lazi", "lazy", "dog" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }
    
    /**
     * LuceneJapaneseTokenizer includes two customizations that warrant testing:
     * <ol><li>Special removal of tags (e.g. &lt;x0/>) when stemming
     * <li>Re-joining of tags when doing verbatim or non-stemming tokenizing
     * </ol>
     */
    public void testJapanese() {
        ITokenizer tok = new LuceneJapaneseTokenizer();
        String orig = "\u6211\u3005\u306E\u3059\u3079\u3066\u306F\u540C\u3058\uFF11\u500B\u306E\u60D1"
                + "\u661F\uFF08\u82F1\uFF1A\u300Ca planet\u300D\uFF09\u306B\u4F4F\u307F\u3001\u6211"
                + "\u3005\u306E\u3059\u3079\u3066\u306F\u305D\u306E\u751F\u7269\u570F\u306E1.5\u90E8"
                + "\u3067\u3042\u308B<x0/>\u3002";
        assertVerbatim(new String[] { "\u6211\u3005", "\u306E", "\u3059\u3079\u3066", "\u306F", "\u540C\u3058",
                "\uFF11", "\u500B", "\u306E", "\u60D1\u661F", "\uFF08", "\u82F1", "\uFF1A", "\u300C", "a",
                " ", "planet", "\u300D", "\uFF09", "\u306B", "\u4F4F\u307F", "\u3001", "\u6211\u3005", "\u306E",
                "\u3059\u3079\u3066", "\u306F", "\u305D\u306E", "\u751F\u7269", "\u570F", "\u306E", "1", ".", "5",
                "\u90E8", "\u3067", "\u3042\u308B", "<x0/>", "\u3002" },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        assertResult(new String[] { "\u6211\u3005", "\u306E", "\u3059\u3079\u3066", "\u306F", "\u540C\u3058",
                "\u500B", "\u306E", "\u60D1\u661F", "\uFF08", "\u82F1", "\uFF1A", "\u300C", "a", "planet",
                "\u300D", "\uFF09", "\u306B", "\u4F4F\u307F", "\u3001", "\u6211\u3005", "\u306E",
                "\u3059\u3079\u3066", "\u306F", "\u305D\u306E", "\u751F\u7269", "\u570F", "\u306E", ".",
                "\u90E8", "\u3067", "\u3042\u308B", "\u3002" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "\u6211\u3005", "\u306E", "\u3059\u3079\u3066", "\u306F", "\u540C\u3058",
                "\u500B", "\u306E", "\u60D1\u661F", "\u82F1", "a", "planet", "\u306B", "\u4F4F\u3080",
                "\u4F4F\u307F", "\u6211\u3005", "\u306E", "\u3059\u3079\u3066", "\u306F", "\u305D\u306E",
                "\u751F\u7269", "\u570F", "\u306E", "\u90E8", "\u3060", "\u3067", "\u3042\u308B" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "\u6211\u3005", "\u3059\u3079\u3066", "\u540C\u3058", "\u500B",
                "\u60D1\u661F", "\u82F1", "a", "planet", "\u4F4F\u3080", "\u4F4F\u307F", "\u6211\u3005",
                "\u3059\u3079\u3066", "\u751F\u7269", "\u570F", "\u90E8" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
        
        // Check for TagJoiningFilter
        orig = "<x0/>\u3042</x0>\u300C<x1/>\u300D<x2/>\u3002<foo bar 123";
        assertVerbatim(new String[] { "<x0/>", "\u3042", "</x0>", "\u300C", "<x1/>", "\u300D", "<x2/>", "\u3002",
                "<", "foo", " ", "bar", " ", "123" },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        // Check for tag removal
        assertResult(new String[] { "\u3042", "foo", "bar" },
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
        System.out.print('\n');
        assertEquals(expected.length, test.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], test[i]);
        }
    }
}
