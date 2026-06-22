/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 zollsoft
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.gui.editor.sort.MultiKeySorter.KeySpec;

/**
 * Unit tests for the sort comparator chaining and preference serialization.
 * Covers the source-based keys (which only need {@link SegmentBuilder} /
 * {@link SourceTextEntry}); target/note/status keys depend on the live project
 * and are exercised through the GUI integration tests instead.
 */
public class MultiKeySorterTest {

    /** Build a mock segment with the given project entry number and source text. */
    private static SegmentBuilder seg(int entryNum, String src) {
        SourceTextEntry ste = mock(SourceTextEntry.class);
        when(ste.getSrcText()).thenReturn(src);
        when(ste.entryNum()).thenReturn(entryNum);
        SegmentBuilder sb = mock(SegmentBuilder.class);
        when(sb.getSourceTextEntry()).thenReturn(ste);
        return sb;
    }

    private static MultiKeySorter sorter(KeySpec... keys) {
        return new MultiKeySorter(Arrays.asList(keys), Locale.ENGLISH);
    }

    /** Sort a copy and return the resulting entry-number order. */
    private static List<Integer> order(List<SegmentBuilder> segs, MultiKeySorter sorter) {
        List<SegmentBuilder> copy = new ArrayList<>(segs);
        copy.sort(sorter.getComparator());
        List<Integer> result = new ArrayList<>();
        for (SegmentBuilder sb : copy) {
            result.add(sb.getSourceTextEntry().entryNum());
        }
        return result;
    }

    @Test
    public void emptySorterKeepsNaturalOrder() {
        List<SegmentBuilder> segs = Arrays.asList(seg(3, "c"), seg(1, "a"), seg(2, "b"));
        assertTrue(sorter().isEmpty());
        assertEquals(Arrays.asList(1, 2, 3), order(segs, sorter()));
    }

    @Test
    public void sourceAlphaAscendingAndDescending() {
        List<SegmentBuilder> segs = Arrays.asList(seg(1, "banana"), seg(2, "apple"), seg(3, "cherry"));
        assertEquals(Arrays.asList(2, 1, 3),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_ALPHA, true))));
        assertEquals(Arrays.asList(3, 1, 2),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_ALPHA, false))));
    }

    @Test
    public void sourceLengthAscending() {
        List<SegmentBuilder> segs = Arrays.asList(seg(1, "aa"), seg(2, "b"), seg(3, "ccc"));
        assertEquals(Arrays.asList(2, 1, 3),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_LENGTH, true))));
    }

    @Test
    public void sourceRhymeSortsByReversedString() {
        // reversed: "ba"->"ab"(1), "ca"->"ac"(2), "ab"->"ba"(3)
        List<SegmentBuilder> segs = Arrays.asList(seg(1, "ba"), seg(2, "ca"), seg(3, "ab"));
        assertEquals(Arrays.asList(1, 2, 3),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_RHYME, true))));
        // distinct from plain alphabetical, which would yield "ab"(3),"ba"(1),"ca"(2)
        assertEquals(Arrays.asList(3, 1, 2),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_ALPHA, true))));
    }

    @Test
    public void naturalTiebreakerKeepsEqualKeysInEntryOrder() {
        // equal length: ties must fall back to natural entry-number order
        List<SegmentBuilder> segs = Arrays.asList(seg(5, "xx"), seg(2, "yy"), seg(9, "z"));
        assertEquals(Arrays.asList(9, 2, 5),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_LENGTH, true))));
    }

    @Test
    public void secondaryKeyBreaksPrimaryTies() {
        // primary length asc, secondary source alpha desc
        List<SegmentBuilder> segs = Arrays.asList(seg(1, "bb"), seg(2, "aa"), seg(3, "c"));
        assertEquals(Arrays.asList(3, 1, 2),
                order(segs, sorter(new KeySpec(SortKey.SOURCE_LENGTH, true),
                        new KeySpec(SortKey.SOURCE_ALPHA, false))));
    }

    @Test
    public void preferenceStringRoundTrip() {
        List<KeySpec> keys = Arrays.asList(new KeySpec(SortKey.SOURCE_ALPHA, true),
                new KeySpec(SortKey.SOURCE_LENGTH, false));
        String s = MultiKeySorter.toPreferenceString(keys);
        assertEquals("SOURCE_ALPHA:asc;SOURCE_LENGTH:desc", s);

        List<KeySpec> parsed = MultiKeySorter.fromPreferenceString(s);
        assertEquals(2, parsed.size());
        assertEquals(SortKey.SOURCE_ALPHA, parsed.get(0).key);
        assertTrue(parsed.get(0).ascending);
        assertEquals(SortKey.SOURCE_LENGTH, parsed.get(1).key);
        assertFalse(parsed.get(1).ascending);
    }

    @Test
    public void fromPreferenceStringHandlesEmptyAndNull() {
        assertTrue(MultiKeySorter.fromPreferenceString(null).isEmpty());
        assertTrue(MultiKeySorter.fromPreferenceString("").isEmpty());
        assertTrue(MultiKeySorter.fromPreferenceString("   ").isEmpty());
    }

    @Test
    public void fromPreferenceStringSkipsUnknownKeys() {
        List<KeySpec> parsed = MultiKeySorter.fromPreferenceString("FOO:asc;SOURCE_ALPHA:desc");
        assertEquals(1, parsed.size());
        assertEquals(SortKey.SOURCE_ALPHA, parsed.get(0).key);
        assertFalse(parsed.get(0).ascending);
    }

    @Test
    public void emptyKeyListSerializesToEmptyString() {
        assertEquals("", MultiKeySorter.toPreferenceString(Collections.emptyList()));
    }
}
