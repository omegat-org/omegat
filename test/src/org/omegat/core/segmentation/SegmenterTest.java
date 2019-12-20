/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.core.segmentation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.omegat.util.Language;

/**
 * Tests for OmegaT segmentation.
 *
 * @author Maxym Mykhalchuk
 */
public class SegmenterTest {

    private Segmenter segmenter = new Segmenter(SRX.getDefault());

    /**
     * Test of segment method, of class org.omegat.core.segmentation.Segmenter.
     */
    @Test
    public void testSegment() {
        List<StringBuilder> spaces = new ArrayList<StringBuilder>();
        List<String> segments = segmenter.segment(new Language("en"), "<br7>\n\n<br5>\n\nother", spaces,
                new ArrayList<Rule>());
        assertEquals(3, segments.size());
        assertEquals("<br7>", segments.get(0));
        assertEquals("<br5>", segments.get(1));
        assertEquals("other", segments.get(2));
    }

    /**
     * Test of glue method, of class org.omegat.core.segmentation.Segmenter.
     */
    @Test
    public void testGlue() {
        List<StringBuilder> spaces = new ArrayList<StringBuilder>();
        List<Rule> brules = new ArrayList<Rule>();
        String oldString = "<br7>\n\n<br5>\n\nother";
        List<String> segments = segmenter.segment(new Language("en"), oldString, spaces, brules);
        String newString = segmenter.glue(new Language("en"), new Language("fr"), segments, spaces, brules);
        assertEquals(oldString, newString);
    }

    /**
     * Test of glue method for CJK, of class org.omegat.core.segmentation.Segmenter.
     */
    @Test
    public void testGlueCJK() {
        final String enFullstop = ".";
        final String jaFullstop = "\\u3002"; // Unicode escaped

        // basic combination
        final String source = "Foo. Bar.\nHere.\n\nThere.\r\nThis.\tThat.\n\tOther.";
        final String translated = source.replace(" ", "").replace(enFullstop, jaFullstop);
        assertEquals(translated, getPseudoTranslationFromEnToJa(source));

        // spaces after/before \n
        final String source2 = "Foo. \n Bar.";
        final String translated2 = "Foo\\u3002\n Bar\\u3002";
        assertEquals(translated2, getPseudoTranslationFromEnToJa(source2));

        // spaces after/before \t
        final String source3 = "Foo. \t Bar.";
        final String translated3 = "Foo\\u3002\t Bar\\u3002";
        assertEquals(translated3, getPseudoTranslationFromEnToJa(source3));
    }

    private String getPseudoTranslationFromEnToJa(final String source) {
        final String enFullstop = ".";
        final String jaFullstop = "\\u3002";
        List<StringBuilder> spaces = new ArrayList<StringBuilder>();
        List<Rule> brules = new ArrayList<Rule>();
        List<String> segments = segmenter.segment(new Language("en"), source, spaces, brules);

        // pseudo-translation (just replace full-stop char)
        for (int i = 0; i < segments.size(); i++) {
            segments.set(i, segments.get(i).replace(enFullstop, jaFullstop));
        }
        return segmenter.glue(new Language("en"), new Language("ja"), segments, spaces, brules);
    }
}
