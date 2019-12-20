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

package org.omegat.gui.editor.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.omegat.gui.editor.history.WordPredictor.Prediction;

public class WordPredictorTest {

    @Test
    public void testWordPredictor() {
        WordPredictor p = new WordPredictor();

        {
            // Should be empty before training
            assertEquals(Collections.emptyList(), p.predictWord("a"));
        }

        p.train("a big brown bear".split(" "));
        p.train("a big brown bench".split(" "));

        {
            List<Prediction> pns = p.predictWord("a");
            assertEquals(1, pns.size());
            assertPrediction("big", 100d, pns.get(0));
        }
        {
            List<Prediction> pns = p.predictWord("big");
            assertEquals(1, pns.size());
            assertPrediction("brown", 100d, pns.get(0));
        }
        {
            // Word must occur more than once to be considered interesting.
            assertEquals(Collections.emptyList(), p.predictWord("brown"));
            assertEquals(Collections.emptyList(), p.predictWord("bear"));
            assertEquals(Collections.emptyList(), p.predictWord("foo"));
        }
    }

    @Test
    public void testWordFrequency() {
        WordPredictor p = new WordPredictor();

        p.train("a big brown bear".split(" "));
        p.train("a big brown bench".split(" "));
        p.train("a big yellow banana".split(" "));

        {
            List<Prediction> pns = p.predictWord("big");
            assertEquals(1, pns.size());
            assertPrediction("brown", 100d, pns.get(0));
        }

        p.train("a big yellow duck".split(" "));

        {
            List<Prediction> pns = p.predictWord("big");
            assertEquals(2, pns.size());
            assertPrediction("brown", 50d, pns.get(0));
            assertPrediction("yellow", 50d, pns.get(1));
        }

        p.train("a big yellow daisy".split(" "));

        {
            List<Prediction> pns = p.predictWord("big");
            assertEquals(2, pns.size());
            assertPrediction("yellow", (3d / 5) * 100, pns.get(0));
            assertPrediction("brown", (2d / 5) * 100, pns.get(1));
        }
    }

    @Test
    public void testMinFrequency() {
        WordPredictor p = new WordPredictor();

        for (int i = 0; i < 2; i++) {
            p.train("a big brown bear".split(" "));
            p.train("a big brown bench".split(" "));
            p.train("a big brown bazooka".split(" "));
            p.train("a big brown bazinga".split(" "));
            p.train("a big brown balloon".split(" "));
            p.train("a big brown boulder".split(" "));
            p.train("a big brown blanket".split(" "));
            p.train("a big brown balcony".split(" "));
            p.train("a big brown binder".split(" "));
            p.train("a big brown book".split(" "));
        }

        {
            List<Prediction> pns = p.predictWord("brown");
            assertEquals(10, pns.size());
            assertPrediction("balcony", 10d, pns.get(0));
            assertPrediction("boulder", 10d, pns.get(9));
        }

        // Add an eleventh distinct word after "brown", making none of the
        // predictions higher than the minimum threshold for being considered
        // interesting.
        p.train("a big brown bath".split(" "));
        p.train("a big brown bath".split(" "));

        {
            assertEquals(Collections.emptyList(), p.predictWord("brown"));
        }
        {
            List<Prediction> pns = p.predictWord("a");
            assertEquals(1, pns.size());
            assertPrediction("big", 100d, pns.get(0));
        }
    }

    @Test
    public void testReset() {
        WordPredictor p = new WordPredictor();

        p.train("a big brown bath".split(" "));
        p.train("a big brown bath".split(" "));
        {
            List<Prediction> pns = p.predictWord("a");
            assertEquals(1, pns.size());
            assertPrediction("big", 100d, pns.get(0));
        }

        p.reset();

        {
            assertEquals(Collections.emptyList(), p.predictWord("a"));
        }
    }

    @Test
    public void testEmptyInput() {
        WordPredictor p = new WordPredictor();

        {
            try {
                p.train(null);
                fail("should throw NPE on null input");
            } catch (NullPointerException e) {
                // OK
            }
        }

        p.train("a big brown bear".split(" "));
        p.train("a big brown bench".split(" "));

        {
            // Empty string should give empty predictions even after training
            assertEquals(Collections.emptyList(), p.predictWord(""));
        }

        {
            try {
                p.predictWord(null);
                fail("Should throw NPE on null input");
            } catch (NullPointerException e) {
                // OK
            }
        }
    }

    private void assertPrediction(String word, double freq, Prediction p) {
        assertEquals(word, p.getWord());
        assertEquals(freq, p.getFrequency(), 0d);
    }
}
