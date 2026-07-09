/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.core.matching;

import org.junit.Test;
import org.omegat.util.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for the LevenshteinDistance class.
 *
 * <p>
 * The LevenshteinDistance class computes the edit distance (number of
 * insertions, deletions, or substitutions required) between two sequences of
 * Tokens.
 */
public class LevenshteinDistanceTest {

    /**
     * Test case: Compute the distance between two identical token arrays.
     * Expected result: Distance should be 0.
     */
    @Test
    public void testIdenticalTokens() {
        Token[] source = { new Token("test", 0), new Token("example", 6) };
        Token[] target = { new Token("test", 0), new Token("example", 6) };

        LevenshteinDistance calculator = new LevenshteinDistance();
        int distance = calculator.compute(source, target);

        assertEquals(0, distance);
    }

    /**
     * Test case: Compute the distance between a non-empty token array and an
     * empty token array. Expected result: Distance should equal the size of the
     * non-empty array.
     */
    @Test
    public void testSourceNonEmptyTargetEmpty() {
        Token[] source = { new Token("alpha", 0), new Token("beta", 6) };
        Token[] target = {};

        LevenshteinDistance calculator = new LevenshteinDistance();
        int distance = calculator.compute(source, target);

        assertEquals(source.length, distance);
    }

    /**
     * Test case: Compute the distance between an empty token array and a
     * non-empty token array. Expected result: Distance should equal the size of
     * the non-empty array.
     */
    @Test
    public void testSourceEmptyTargetNonEmpty() {
        Token[] source = {};
        Token[] target = { new Token("gamma", 0), new Token("delta", 6), new Token("epsilon", 10) };

        LevenshteinDistance calculator = new LevenshteinDistance();
        int distance = calculator.compute(source, target);

        assertEquals(target.length, distance);
    }

    /**
     * Test case: Compute the distance between two completely different token
     * arrays. Expected result: Distance should equal the length of the larger
     * array.
     */
    @Test
    public void testCompletelyDifferentTokens() {
        Token[] source = { new Token("A", 0), new Token("B", 1), new Token("C", 2) };
        Token[] target = { new Token("X", 0), new Token("Y", 1), new Token("Z", 2) };

        LevenshteinDistance calculator = new LevenshteinDistance();
        int distance = calculator.compute(source, target);

        assertEquals(source.length, distance);
    }

    /**
     * Test case: Compute the distance with partially similar token arrays.
     * Expected result: Distance should represent correct edit cost.
     */
    @Test
    public void testPartiallySimilarTokens() {
        Token[] source = { new Token("cat", 0), new Token("dog", 3), new Token("fish", 7) };
        Token[] target = { new Token("cat", 0), new Token("wolf", 3), new Token("fish", 8) };

        LevenshteinDistance calculator = new LevenshteinDistance();
        int distance = calculator.compute(source, target);

        assertEquals(1, distance);
    }

    /**
     * Test case: Ensure the compute method throws IllegalArgumentException for
     * null inputs.
     */
    @Test
    public void testNullInputs() {
        Token[] nonNull = { new Token("null", 0) };

        LevenshteinDistance calculator = new LevenshteinDistance();

        try {
            calculator.compute(null, nonNull);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        try {
            calculator.compute(nonNull, null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }
}
