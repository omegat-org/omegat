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

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
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
}
