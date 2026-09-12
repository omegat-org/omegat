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

package org.omegat.gui.editor.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchRangeFilterTest {

    @Test
    public void testAllowed() {
        MatchRangeFilter filter = new MatchRangeFilter("95%-100%", Arrays.asList(1, 5));
        assertTrue(filter.allowed(createEntry(1)));
        assertFalse(filter.allowed(createEntry(2)));
        assertTrue(filter.allowed(createEntry(5)));
        assertFalse(filter.allowed(null));
    }

    @Test
    public void testProperties() {
        MatchRangeFilter filter = new MatchRangeFilter("Repetitions", Collections.singleton(1));
        assertFalse(filter.isSourceAsEmptyTranslation());
        assertNotNull(filter.getControlComponent());
    }

    private SourceTextEntry createEntry(int entryNum) {
        EntryKey key = new EntryKey("file.txt", "Source text " + entryNum, null, null, null, null);
        return new SourceTextEntry(key, entryNum, null, null, Collections.emptyList());
    }
}
