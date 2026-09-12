/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.TestPreferencesInitializer;

public class MatchesTextAreaTest {

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    @Test
    public void testReplaceNumbers() {
        ITokenizer tok = new DefaultTokenizer();

        // Simple case
        String source = "chapter 5";
        String srcMatch = "chapter 1";
        String trgMatch = "foo 1";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Double
        source = "chapter 5.5";
        srcMatch = "chapter 1.1";
        trgMatch = "foo 1.1";
        assertEquals("foo 5.5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Different order
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 7 bang 8 bop 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // User-reported problem string (duplicate numbers)
        source = "Point C : Recommence les \u00E9tapes 16 \u00E0 21 \u2013 pages 16 et 17";
        srcMatch = "Point B : Recommence les \u00E9tapes 9 \u00E0 15 \u2013 page 14 et 15";
        trgMatch = "Point B: Repeat steps 9 to 15 \u2013 pages 14 and 15";
        assertEquals("Point B: Repeat steps 16 to 21 \u2013 pages 16 and 17",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop";
        assertEquals("bing 3 bang 2 bop", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing numbers
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 33";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Full-width (ASCII) digits (U+FF10-U+FF19), common in Japanese text,
     * must be treated as equivalent to their half-width counterparts when
     * substituting numbers into a fuzzy match, and the inserted number must
     * adopt the digit width used by the target match. Feature request #1193.
     */
    @Test
    public void testReplaceNumbersFullwidth() {
        ITokenizer tok = new DefaultTokenizer();

        // Reported case: full-width source number, half-width (Latin) target.
        // The inserted number must be converted to half-width to match the target.
        String source = "これは例文９です";
        String srcMatch = "これは例文8です";
        String trgMatch = "This is a sample sentence 8";
        assertEquals("This is a sample sentence 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Equivalence: the match's own source uses a full-width digit while its
        // target uses a half-width one. They must still be recognized as the
        // same number so the substitution is applied.
        source = "chapter ５";
        srcMatch = "chapter ９";
        trgMatch = "foo 9";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A full-width target keeps full-width digits: the half-width source
        // number is converted to full-width to match the target convention.
        source = "chapter 5";
        srcMatch = "chapter 9";
        trgMatch = "第９章";
        assertEquals("第５章",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Full-width digits on both sides of a purely CJK segment.
        source = "第５０章";
        srcMatch = "第１２章";
        trgMatch = "第１２章";
        assertEquals("第５０章",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Regression guard: pure ASCII substitution is unaffected.
        source = "chapter 5";
        srcMatch = "chapter 1";
        trgMatch = "foo 1";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Edge cases and documented scope limits of the full-width digit handling
     * from feature request #1193.
     */
    @Test
    public void testReplaceNumbersWidthEdgeCases() {
        ITokenizer tok = new DefaultTokenizer();

        // Several numbers at once, full-width source into half-width target.
        String source = "x ９ y ８";
        String srcMatch = "x ３ y ４";
        String trgMatch = "p 3 q 4";
        assertEquals("p 9 q 8", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Each substituted number independently follows the width of the
        // target token it replaces (mixed widths in one target).
        source = "x ９ y ８";
        srcMatch = "x ３ y ４";
        // Target mixes a half-width and a full-width digit.
        trgMatch = "p 3 q ４";
        assertEquals("p 9 q ８",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Documented limit: full-width DECIMALS are not recognized as numbers
        // (Double.parseDouble is ASCII-only), so no substitution happens. This
        // pins the current scope; extending to full-width decimals is a
        // possible follow-up.
        source = "x ５．５";
        srcMatch = "x １．１";
        trgMatch = "foo 1.1";
        assertEquals("foo 1.1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Documented scope boundary: Arabic-Indic digits (U+0660-U+0669) and
        // Extended Arabic-Indic digits (U+06F0-U+06F9) are recognized as
        // numbers but are NOT width-normalized by #1193, so such a source
        // number is inserted verbatim rather than converted. Cross
        // numeral-system handling is intentionally out of scope here.
        source = "x ٩";
        srcMatch = "x 8";
        trgMatch = "foo 8";
        assertEquals("foo ٩",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * The reported #1193 scenario on the real Japanese production path: the
     * source is tokenized with the Kuromoji-based Japanese tokenizer while the
     * target uses a Latin tokenizer. The full-width (ASCII) source digit must still
     * be recognized and inserted into the target as a half-width digit.
     */
    @Test
    public void testReplaceNumbersJapaneseTokenizer() {
        ITokenizer jaTok = new LuceneJapaneseTokenizer();
        ITokenizer enTok = new DefaultTokenizer();

        String source = "これは例文９です";
        String srcMatch = "これは例文8です";
        String trgMatch = "This is a sample sentence 8";
        assertEquals("This is a sample sentence 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, jaTok, enTok));
    }
}
