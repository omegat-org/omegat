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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.Collator;
import java.util.Locale;

import org.junit.Test;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.SegmentBuilder;

/**
 * Unit tests for the new source/structure-based {@link SortKey} criteria that do
 * not require a loaded project (they read only {@link SourceTextEntry}). The
 * project/translation-based criteria (origin, note flag, link status) follow the
 * same pattern as the existing target/date keys and are exercised through the
 * GUI integration tests.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SortKeyTest {

    private static final Collator C = Collator.getInstance(Locale.ENGLISH);

    private static SegmentBuilder seg(SourceTextEntry ste) {
        SegmentBuilder sb = mock(SegmentBuilder.class);
        when(sb.getSourceTextEntry()).thenReturn(ste);
        return sb;
    }

    private static int cmp(SortKey key, boolean asc, SourceTextEntry a, SourceTextEntry b) {
        return key.comparator(C, asc).compare(seg(a), seg(b));
    }

    @Test
    public void duplicateCountAscending() {
        SourceTextEntry a = mock(SourceTextEntry.class);
        when(a.getNumberOfDuplicates()).thenReturn(0);
        SourceTextEntry b = mock(SourceTextEntry.class);
        when(b.getNumberOfDuplicates()).thenReturn(5);
        assertTrue(cmp(SortKey.DUPLICATE_COUNT, true, a, b) < 0);
    }

    @Test
    public void descendingReversesOrder() {
        SourceTextEntry a = mock(SourceTextEntry.class);
        when(a.getNumberOfDuplicates()).thenReturn(0);
        SourceTextEntry b = mock(SourceTextEntry.class);
        when(b.getNumberOfDuplicates()).thenReturn(5);
        assertTrue(cmp(SortKey.DUPLICATE_COUNT, false, a, b) > 0);
    }

    @Test
    public void duplicateStatusUsesOrdinal() {
        SourceTextEntry none = mock(SourceTextEntry.class);
        when(none.getDuplicate()).thenReturn(SourceTextEntry.DUPLICATE.NONE);
        SourceTextEntry next = mock(SourceTextEntry.class);
        when(next.getDuplicate()).thenReturn(SourceTextEntry.DUPLICATE.NEXT);
        assertTrue(cmp(SortKey.DUPLICATE_STATUS, true, none, next) < 0);
    }

    @Test
    public void tagCountCountsProtectedParts() {
        SourceTextEntry few = mock(SourceTextEntry.class);
        when(few.getProtectedParts()).thenReturn(new ProtectedPart[0]);
        SourceTextEntry many = mock(SourceTextEntry.class);
        when(many.getProtectedParts()).thenReturn(new ProtectedPart[3]);
        assertTrue(cmp(SortKey.TAG_COUNT, true, few, many) < 0);
    }

    @Test
    public void tagCountIsNullSafe() {
        SourceTextEntry nul = mock(SourceTextEntry.class);
        when(nul.getProtectedParts()).thenReturn(null);
        SourceTextEntry one = mock(SourceTextEntry.class);
        when(one.getProtectedParts()).thenReturn(new ProtectedPart[1]);
        assertTrue(cmp(SortKey.TAG_COUNT, true, nul, one) < 0);
    }

    @Test
    public void paragraphStartFlag() {
        SourceTextEntry no = mock(SourceTextEntry.class);
        when(no.isParagraphStart()).thenReturn(false);
        SourceTextEntry yes = mock(SourceTextEntry.class);
        when(yes.isParagraphStart()).thenReturn(true);
        assertTrue(cmp(SortKey.PARAGRAPH_START, true, no, yes) < 0);
    }

    @Test
    public void sourceFuzzyFlag() {
        SourceTextEntry no = mock(SourceTextEntry.class);
        when(no.isSourceTranslationFuzzy()).thenReturn(false);
        SourceTextEntry yes = mock(SourceTextEntry.class);
        when(yes.isSourceTranslationFuzzy()).thenReturn(true);
        assertTrue(cmp(SortKey.SOURCE_FUZZY, true, no, yes) < 0);
    }

    @Test
    public void numericDirectionOrdersTextKeyByValue() {
        SourceTextEntry a = mock(SourceTextEntry.class);
        when(a.getSrcText()).thenReturn("item2");
        SourceTextEntry b = mock(SourceTextEntry.class);
        when(b.getSrcText()).thenReturn("item10");
        // numeric: 2 < 10
        assertTrue(SortKey.SOURCE_ALPHA.comparator(C, true, true).compare(seg(a), seg(b)) < 0);
        // plain text: "item10" < "item2" (character '1' < '2')
        assertTrue(SortKey.SOURCE_ALPHA.comparator(C, true, false).compare(seg(a), seg(b)) > 0);
    }

    @Test
    public void supportsNumericForAllTextKeys() {
        for (SortKey k : new SortKey[] { SortKey.SOURCE_ALPHA, SortKey.TARGET_ALPHA, SortKey.NOTE_ALPHA,
                SortKey.COMMENT_ALPHA, SortKey.ORIGIN_ALPHA, SortKey.SOURCE_FILE, SortKey.PATH_ALPHA,
                SortKey.ID_ALPHA }) {
            assertTrue(k + " should support numeric", k.supportsNumeric());
        }
    }

    @Test
    public void supportsNumericOnlyForTextKeys() {
        assertTrue(SortKey.SOURCE_ALPHA.supportsNumeric());
        assertTrue(SortKey.ID_ALPHA.supportsNumeric());
        assertTrue(SortKey.SOURCE_FILE.supportsNumeric());
        assertFalse(SortKey.SOURCE_LENGTH.supportsNumeric());
        assertFalse(SortKey.DUPLICATE_COUNT.supportsNumeric());
        assertFalse(SortKey.NATURAL.supportsNumeric());
    }
}
