/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.core.spellchecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.omegat.util.OStrings;

/**
 * The tooltip of a misspelled word: formatting of the spelling suggestions
 * shown on hover.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
public class SpellCheckerMarkerTest {

    @Test
    public void noSuggestions() {
        assertEquals(OStrings.getString("SC_NO_SUGGESTIONS"),
                SpellCheckerMarker.formatSuggestions(Collections.emptyList()));
    }

    @Test
    public void suggestionsAreTaggedAndJoined() {
        String tooltip = SpellCheckerMarker.formatSuggestions(Arrays.asList("word", "ward"));
        assertTrue(tooltip.contains("<suggestion>word</suggestion>, <suggestion>ward</suggestion>"));
    }

    @Test
    public void suggestionCountIsLimited() {
        List<String> many = Arrays.asList("one", "two", "three", "four", "five", "six", "seven");
        String tooltip = SpellCheckerMarker.formatSuggestions(many);
        assertTrue(tooltip.contains("five"));
        assertFalse("only the first five suggestions fit the tooltip", tooltip.contains("six"));
    }

    @Test
    public void suggestionsAreEscaped() {
        // Words never contain markup, but the tooltip pipeline renders
        // HTML, so the formatting must stay safe whatever the checker
        // returns.
        String tooltip = SpellCheckerMarker.formatSuggestions(Arrays.asList("a<b&c"));
        assertTrue(tooltip.contains("a&lt;b&amp;c"));
        assertFalse(tooltip.contains("a<b&c"));
    }
}
