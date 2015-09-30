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
    
    /**
     * Turkish warrants special testing because it has the letter \u0130
     * (LATIN CAPITAL LETTER I WITH DOT ABOVE); the result (both content
     * and length) of performing <code>"\u0130".toLowerCase()</code> depends
     * on the default Locale, and in the past there were issues with improper
     * lowercasing during tokenization leading to OOB exceptions.
     * <p>
     * Text from https://tr.wikipedia.org/wiki/T%C3%BCrk%C3%A7e
     */
    public void testTurkish() {
        ITokenizer tok = new LuceneTurkishTokenizer();
        String orig = "\u201C\u0130stanbul a\u011Fz\u0131\u201D, T\u00FCrkiye T\u00FCrk\u00E7esi"
                + "yaz\u0131 dilinin kayna\u011F\u0131 olarak kabul edilir; yaz\u0131 dili bu"
                + "a\u011F\u0131z temelinde olu\u015Fmu\u015Ftur.";
        assertVerbatim(new String[] { "\u201C", "\u0130stanbul", " ", "a\u011Fz\u0131", "\u201D",
                ",", " ", "T\u00FCrkiye", " ", "T\u00FCrk\u00E7esiyaz\u0131", " ", "dilinin", " ",
                "kayna\u011F\u0131", " ", "olarak", " ", "kabul", " ", "edilir", ";", " ", "yaz\u0131",
                " ", "dili", " ", "bua\u011F\u0131z", " ", "temelinde", " ", "olu\u015Fmu\u015Ftur", "." },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        assertResult(new String[] { "\u0130stanbul", "a\u011Fz\u0131", "T\u00FCrkiye",
                "T\u00FCrk\u00E7esiyaz\u0131", "dilinin", "kayna\u011F\u0131", "olarak",
                "kabul", "edilir", "yaz\u0131", "dili", "bua\u011F\u0131z", "temelinde",
                "olu\u015Fmu\u015Ftur" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "istanbul", "a\u011Fz\u0131", "t\u00FCrki", "T\u00FCrkiye",
                "t\u00FCrk\u00E7esiyaz", "T\u00FCrk\u00E7esiyaz\u0131", "dil", "dilinin",
                "kaynak", "kayna\u011F\u0131", "olarak", "kabul", "edilir", "yaz", "yaz\u0131",
                "dil", "dili", "buak", "bua\u011F\u0131z", "temel", "temelinde", "olu\u015F",
                "olu\u015Fmu\u015Ftur" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "istanbul", "a\u011Fz\u0131", "t\u00FCrki", "T\u00FCrkiye",
                "t\u00FCrk\u00E7esiyaz", "T\u00FCrk\u00E7esiyaz\u0131", "dil", "dilinin",
                "kaynak", "kayna\u011F\u0131", "kabul", "edilir", "yaz", "yaz\u0131",
                "dil", "dili", "buak", "bua\u011F\u0131z", "temel", "temelinde", "olu\u015F",
                "olu\u015Fmu\u015Ftur" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }
    
    /**
     * Chinese tends to have very few character boundaries breakable by BreakIterator,
     * so LuceneSmartChineseTokenizer tokenizes by code point for verbatim tokenizing.
     * <p>
     * Text from https://zh.wikipedia.org/wiki/%E6%B1%89%E8%AF%AD
     */
    public void testChinese() {
        ITokenizer tok = new LuceneSmartChineseTokenizer();
        String orig = "\u6F22\u8A9E\u7684\u6587\u5B57\u7CFB\u7D71\u2014\u2014\u6F22\u5B57\u662F"
                + "\u4E00\u7A2E\u610F\u97F3\u8A9E\u8A00\uFF0C\u8868\u610F\u7684\u540C\u6642\u4E5F"
                + "\u5177\u4E00\u5B9A\u7684\u8868\u97F3\u529F\u80FD\u3002";
        assertVerbatim(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587", "\u5B57", "\u7CFB",
                "\u7D71", "\u2014", "\u2014", "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E",
                "\u610F", "\u97F3", "\u8A9E", "\u8A00", "\uFF0C", "\u8868", "\u610F", "\u7684",
                "\u540C", "\u6642", "\u4E5F", "\u5177", "\u4E00", "\u5B9A", "\u7684", "\u8868",
                "\u97F3", "\u529F", "\u80FD", "\u3002" },
                tok.tokenizeVerbatimToStrings(orig),
                tok.tokenizeVerbatim(orig),
                orig);
        assertResult(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB",
                "\u7D71", ",", ",", "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E",
                "\u610F", "\u97F3", "\u8A9E", "\u8A00", ",", "\u8868\u610F", "\u7684",
                "\u540C", "\u6642", "\u4E5F", "\u5177", "\u4E00\u5B9A", "\u7684", "\u8868\u97F3",
                "\u529F\u80FD", "," },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB",
                "\u7D71", ",", "\u2014", ",", "\u2014", "\u6F22", "\u5B57", "\u662F", "\u4E00",
                "\u7A2E", "\u610F", "\u97F3", "\u8A9E", "\u8A00", ",", "\uFF0C", "\u8868\u610F",
                "\u7684", "\u540C", "\u6642", "\u4E5F", "\u5177", "\u4E00\u5B9A", "\u7684",
                "\u8868\u97F3", "\u529F\u80FD", ",", "\u3002" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB",
                "\u7D71", "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E", "\u610F", "\u97F3",
                "\u8A9E", "\u8A00", "\u8868\u610F", "\u7684", "\u540C", "\u6642", "\u4E5F",
                "\u5177", "\u4E00\u5B9A", "\u7684", "\u8868\u97F3", "\u529F\u80FD" },
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
