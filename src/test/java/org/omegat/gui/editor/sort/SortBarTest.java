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

package org.omegat.gui.editor.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omegat.util.OStrings;

/**
 * Behavioral tests for {@link SortBar} that do not require a loaded project:
 * the new collapsed-by-default state and the collapsed summary composition.
 * The sort ordering logic itself is covered by {@link MultiKeySorterTest}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SortBarTest {

    private static String unsortedSummary() {
        return OStrings.getString("SORT_BAR_LABEL") + " " + SortKey.NATURAL.getLocalizedName();
    }

    @Test
    public void startsCollapsedByDefault() {
        SortBar bar = new SortBar();
        assertFalse("sort bar must start collapsed", bar.isExpanded());
    }

    @Test
    public void toggleExpandsAndCollapses() {
        SortBar bar = new SortBar();
        bar.toggle();
        assertTrue(bar.isExpanded());
        bar.toggle();
        assertFalse(bar.isExpanded());
    }

    @Test
    public void summaryShowsFileOrderWhenUnsorted() {
        SortBar bar = new SortBar();
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void resetRestoresUnsortedDefault() {
        SortBar bar = new SortBar();
        bar.reset();
        assertEquals(unsortedSummary(), bar.buildSummary());
    }
}
