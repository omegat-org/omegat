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

package org.omegat.gui.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.CsvExportOptions.Scope;
import org.omegat.gui.editor.CsvFormatOptions.CsvCharset;
import org.omegat.gui.editor.CsvFormatOptions.SeparatorChoice;

/**
 * Tests for the entry gathering of the CSV export: scope, editor filter and
 * display order.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorPaneMenuCollectEntriesTest {

    private SourceTextEntry one;
    private SourceTextEntry two;
    private SourceTextEntry three;
    private SourceTextEntry other;
    private EditorController editor;
    private EditorPaneMenu menu;

    @Before
    public void setUp() {
        one = entry(1, "Sentence one.");
        two = entry(2, "Sentence two.");
        three = entry(3, "Sentence three.");
        other = entry(4, "Other document sentence.");

        IProject project = mock(IProject.class);
        when(project.isProjectLoaded()).thenReturn(true);
        when(project.getAllEntries()).thenReturn(List.of(one, two, three, other));
        IProject.FileInfo first = new IProject.FileInfo("chapter.html");
        first.entries.addAll(List.of(one, two, three));
        IProject.FileInfo second = new IProject.FileInfo("other.html");
        second.entries.add(other);
        when(project.getProjectFiles()).thenReturn(List.of(first, second));
        Core.setProject(project);

        editor = mock(EditorController.class);
        editor.displayedFileIndex = 0;
        menu = new EditorPaneMenu(editor);
    }

    private static SourceTextEntry entry(int num, String source) {
        return new SourceTextEntry(new EntryKey("chapter.html", source, null, null, null, null), num, null,
                null, List.of());
    }

    private static CsvExportOptions options(Scope scope, boolean applyFilter, boolean applySort) {
        Map<CsvColumn, Boolean> columns = new LinkedHashMap<>();
        for (CsvColumn column : CsvColumn.values()) {
            columns.put(column, true);
        }
        return new CsvExportOptions(scope, applyFilter, applySort, columns, new CsvFormatOptions(
                CsvCharset.UTF_8, SeparatorChoice.COMMA, ',', false, false,
                CsvFormatOptions.QuoteEscape.DOUBLED));
    }

    private void displayInOrder(SourceTextEntry... entries) {
        editor.m_docSegList = Arrays.stream(entries).map(ste -> {
            SegmentBuilder builder = mock(SegmentBuilder.class);
            when(builder.getSourceTextEntry()).thenReturn(ste);
            return builder;
        }).toArray(SegmentBuilder[]::new);
    }

    @Test
    public void testProjectScopeInProjectOrder() {
        assertEquals(List.of(one, two, three, other),
                menu.collectEntries(options(Scope.PROJECT, false, false)));
    }

    @Test
    public void testProjectScopeWithEditorFilter() {
        when(editor.getFilter()).thenReturn(new SourceContainsFilter("Sentence"));
        assertEquals(List.of(one, two, three), menu.collectEntries(options(Scope.PROJECT, true, false)));
        // Filter present but not applied.
        assertEquals(List.of(one, two, three, other),
                menu.collectEntries(options(Scope.PROJECT, false, false)));
    }

    @Test
    public void testCurrentFileInProjectOrder() {
        displayInOrder(three, one, two);
        assertEquals(List.of(one, two, three),
                menu.collectEntries(options(Scope.CURRENT_FILE, false, false)));
    }

    @Test
    public void testCurrentFileInDisplayOrder() {
        displayInOrder(three, one, two);
        assertEquals(List.of(three, one, two),
                menu.collectEntries(options(Scope.CURRENT_FILE, false, true)));
    }

    @Test
    public void testCurrentFileDisplayOrderFallsBackWithoutDocument() {
        editor.m_docSegList = null;
        assertEquals(List.of(one, two, three),
                menu.collectEntries(options(Scope.CURRENT_FILE, false, true)));
    }

    @Test
    public void testCurrentFileOfSecondDocumentAndClampedIndex() {
        editor.displayedFileIndex = 1;
        assertEquals(List.of(other), menu.collectEntries(options(Scope.CURRENT_FILE, false, false)));
        editor.displayedFileIndex = 99;
        assertEquals(List.of(other), menu.collectEntries(options(Scope.CURRENT_FILE, false, false)));
    }

    @Test
    public void testCurrentFileFilteredDisplayOrder() {
        // With an active filter the display list holds the allowed entries only.
        displayInOrder(two, one);
        when(editor.getFilter()).thenReturn(new SourceContainsFilter("Sentence"));
        assertEquals(List.of(two, one), menu.collectEntries(options(Scope.CURRENT_FILE, true, true)));
    }

    /** Minimal editor filter matching entries whose source contains a text. */
    private static final class SourceContainsFilter implements IEditorFilter {
        private final String needle;

        SourceContainsFilter(String needle) {
            this.needle = needle;
        }

        @Override
        public boolean allowed(@Nullable SourceTextEntry ste) {
            return ste != null && ste.getSrcText().contains(needle);
        }

        @Override
        public java.awt.Component getControlComponent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSourceAsEmptyTranslation() {
            return false;
        }
    }
}
