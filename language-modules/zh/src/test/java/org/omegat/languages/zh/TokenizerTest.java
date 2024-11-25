/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.languages.zh;

import org.junit.Test;

import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.tokenizer.LuceneGermanTokenizer;
import org.omegat.tokenizer.TokenizerTestBase;

public class TokenizerTest extends TokenizerTestBase {

    /**
     * Chinese tends to have very few character boundaries breakable by
     * BreakIterator, so LuceneSmartChineseTokenizer tokenizes by code point for
     * verbatim tokenizing.
     * <p>
     * Text from https://zh.wikipedia.org/wiki/%E6%B1%89%E8%AF%AD
     */
    @Test
    public void testChinese() {
        ITokenizer tok = new LuceneSmartChineseTokenizer();
        String orig = "\u6F22\u8A9E\u7684\u6587\u5B57\u7CFB\u7D71\u2014\u2014\u6F22\u5B57\u662F"
                + "\u4E00\u7A2E\u610F\u97F3\u8A9E\u8A00\uFF0C\u8868\u610F\u7684\u540C\u6642\u4E5F"
                + "\u5177\u4E00\u5B9A\u7684\u8868\u97F3\u529F\u80FD\u3002";
        assertVerbatim(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587", "\u5B57", "\u7CFB", "\u7D71",
                "\u2014", "\u2014", "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E", "\u610F", "\u97F3",
                "\u8A9E", "\u8A00", "\uFF0C", "\u8868", "\u610F", "\u7684", "\u540C", "\u6642", "\u4E5F",
                "\u5177", "\u4E00", "\u5B9A", "\u7684", "\u8868", "\u97F3", "\u529F", "\u80FD", "\u3002" },
                tok.tokenizeVerbatimToStrings(orig), tok.tokenizeVerbatim(orig), orig);
        assertResult(
                new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB", "\u7D71", ",", ",",
                        "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E", "\u610F", "\u97F3", "\u8A9E",
                        "\u8A00", ",", "\u8868\u610F", "\u7684", "\u540C", "\u6642", "\u4E5F", "\u5177",
                        "\u4E00\u5B9A", "\u7684", "\u8868\u97F3", "\u529F\u80FD", "," },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB", "\u7D71", ",",
                "\u2014", ",", "\u2014", "\u6F22", "\u5B57", "\u662F", "\u4E00", "\u7A2E", "\u610F", "\u97F3",
                "\u8A9E", "\u8A00", ",", "\uFF0C", "\u8868\u610F", "\u7684", "\u540C", "\u6642", "\u4E5F",
                "\u5177", "\u4E00\u5B9A", "\u7684", "\u8868\u97F3", "\u529F\u80FD", ",", "\u3002" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(
                new String[] { "\u6F22", "\u8A9E", "\u7684", "\u6587\u5B57", "\u7CFB", "\u7D71", "\u6F22",
                        "\u5B57", "\u662F", "\u4E00", "\u7A2E", "\u610F", "\u97F3", "\u8A9E", "\u8A00",
                        "\u8868\u610F", "\u7684", "\u540C", "\u6642", "\u4E5F", "\u5177", "\u4E00\u5B9A",
                        "\u7684", "\u8868\u97F3", "\u529F\u80FD" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }

    /**
     * The behavior of the Lucene GermanAnalyzer was better for our purposes in
     * Lucene 3.0, so we implement a custom analyzer that recreates that
     * behavior.
     *
     * @see <a href=
     *      "https://groups.yahoo.com/neo/groups/OmegaT/conversations/messages/28395">
     *      User group discussion</a>
     * @see <a href=
     *      "https://sourceforge.net/p/omegat/mailman/message/36839317/">Sourceforge
     *      archive</a>
     */
    @Test
    public void testGerman() {
        ITokenizer tok = new LuceneGermanTokenizer();
        assertResult(new String[] { "prasentier", "pr\u00e4sentierte" },
                tok.tokenizeWordsToStrings("pr\u00e4sentierte", StemmingMode.GLOSSARY));
        assertResult(new String[] { "prasentier", "pr\u00e4sentieren" },
                tok.tokenizeWordsToStrings("pr\u00e4sentieren", StemmingMode.GLOSSARY));
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
        assertVerbatim(
                new String[] { "The", " ", "quick", ",", " ", "brown", " ", "<x0/>", " ", "jumped", " ",
                        "over", " ", "1", " ", "\"", "lazy", "\"", " ", "\u0130stanbul", ".", " ",
                        "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A", "\u3002" },
                tok.tokenizeVerbatimToStrings(orig), tok.tokenizeVerbatim(orig), orig);
        assertResult(
                new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                        "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.NONE));
        assertResult(
                new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                        "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.GLOSSARY));
        assertResult(
                new String[] { "The", "quick", "brown", "jumped", "over", "lazy", "\u0130stanbul",
                        "\u65E5\u672C\u8A9E", "\u3042\u3044\u3046\u3048\u304A" },
                tok.tokenizeWordsToStrings(orig, StemmingMode.MATCHING));
    }
}
