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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.EnumSet;
import java.util.Map;

import org.junit.Test;

import org.omegat.core.matching.MatchEquivalence;

/**
 * The shipped test-area prefill of the equivalence dialog: both sample
 * variants fold onto each other with the default class set and stay apart
 * without folding.
 *
 * @author Stephan Pakebusch
 */
public class MatchEquivalenceDialogTest {

    @Test
    public void sampleVariantsFoldEqualByDefault() {
        Map<Integer, String> foldMap = MatchEquivalence.buildFoldMap(MatchEquivalence.all());
        assertEquals(MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_TYPOGRAPHIC, foldMap),
                MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_PLAIN, foldMap));
    }

    @Test
    public void sampleVariantsDifferWithoutFolding() {
        Map<Integer, String> empty = MatchEquivalence
                .buildFoldMap(EnumSet.noneOf(MatchEquivalence.class));
        assertNotEquals(MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_TYPOGRAPHIC, empty),
                MatchEquivalence.fold(MatchEquivalenceDialog.TEST_SAMPLE_PLAIN, empty));
    }
}
