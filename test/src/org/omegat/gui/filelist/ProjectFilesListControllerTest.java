/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Bo Huang
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

package org.omegat.gui.filelist;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.junit.Test;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProjectFilesListControllerTest {

    @Test
    public void testFormatProgressPercent() {
        assertEquals("0%", ProjectFilesListController.formatProgressPercent(0, 0));
        assertEquals("0%", ProjectFilesListController.formatProgressPercent(0, 10));
        assertEquals("50.0%", ProjectFilesListController.formatProgressPercent(5, 10));
        assertEquals("33.3%", ProjectFilesListController.formatProgressPercent(1, 3));
        assertEquals("100.0%", ProjectFilesListController.formatProgressPercent(3, 3));
    }

    @Test
    public void testCompareFileProgress() {
        ProjectFilesListController.FileProgress lower = fileProgress(1, 4);
        ProjectFilesListController.FileProgress higher = fileProgress(2, 4);
        ProjectFilesListController.FileProgress sameRatioMoreSegments = fileProgress(2, 8);
        ProjectFilesListController.FileProgress sameTranslatedMoreSegments = fileProgress(0, 10);
        ProjectFilesListController.FileProgress sameTranslatedFewerSegments = fileProgress(0, 5);

        assertTrue(ProjectFilesListController.compareFileProgress(lower, higher) < 0);
        assertTrue(ProjectFilesListController.compareFileProgress(higher, lower) > 0);
        assertTrue(ProjectFilesListController.compareFileProgress(lower, sameRatioMoreSegments) < 0);
        assertTrue(ProjectFilesListController.compareFileProgress(sameTranslatedFewerSegments,
                sameTranslatedMoreSegments) < 0);
    }

    @Test
    public void testCalculateFileProgressUsesUniqueEntries() {
        SourceTextEntry uniqueTranslated = sourceEntry("unique-translated", 1);
        SourceTextEntry duplicateTranslated = sourceEntry("duplicate-translated", 2);
        SourceTextEntry uniqueUntranslated = sourceEntry("unique-untranslated", 3);
        List<SourceTextEntry> entries = Arrays.asList(uniqueTranslated, duplicateTranslated,
                uniqueUntranslated);
        Set<SourceTextEntry> translatedEntries = new HashSet<>(
                Arrays.asList(uniqueTranslated, duplicateTranslated));
        Set<SourceTextEntry> uniqueEntries = new HashSet<>(
                Arrays.asList(uniqueTranslated, uniqueUntranslated));

        ProjectFilesListController.FileProgress progress = ProjectFilesListController
                .calculateFileProgress(entries, translatedEntries::contains, uniqueEntries::contains);

        assertEquals(1, progress.getTranslated());
        assertEquals(2, progress.getTotal());
        assertEquals("50.0%", progress.toString());
    }

    @Test
    public void testProgressColorThresholds() {
        assertEquals(new Color(210, 80, 70, 120),
                ProjectFilesListController.getProgressColor(fileProgress(0, 10)));
        assertEquals(new Color(85, 160, 85, 120),
                ProjectFilesListController.getProgressColor(fileProgress(5, 10)));
        assertEquals(new Color(75, 135, 220, 120),
                ProjectFilesListController.getProgressColor(fileProgress(10, 10)));
    }

    @Test
    public void testProgressFillWidthShowsMinimumForZeroProgress() {
        assertEquals(3, ProjectFilesListController.getProgressFillWidth(fileProgress(0, 10), 100));
        assertEquals(0, ProjectFilesListController.getProgressFillWidth(fileProgress(0, 0), 100));
        assertEquals(100, ProjectFilesListController.getProgressFillWidth(fileProgress(10, 10), 100));
    }

    @Test
    public void testUpdateProgressColumnRemovesAndRestoresColumn() {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn filenameColumn = new TableColumn(0);
        TableColumn progressColumn = new TableColumn(5);
        columnModel.addColumn(filenameColumn);
        columnModel.addColumn(progressColumn);

        ProjectFilesListController.updateProgressColumn(columnModel, progressColumn, 1, false);

        assertEquals(1, columnModel.getColumnCount());
        assertEquals(filenameColumn, columnModel.getColumn(0));

        ProjectFilesListController.updateProgressColumn(columnModel, progressColumn, 1, true);

        assertEquals(2, columnModel.getColumnCount());
        assertEquals(filenameColumn, columnModel.getColumn(0));
        assertEquals(progressColumn, columnModel.getColumn(1));
    }

    @Test
    public void testSyncTotalColumnsKeepsProgressBeforeMargin() {
        DefaultTableColumnModel filesColumns = columnModel(0, 1, 2, 3, 4, 5);
        DefaultTableColumnModel totalsColumns = columnModel(0, 1, 2, 3, 4, 6, 5);

        ProjectFilesListController.syncTotalColumnsToFileColumns(filesColumns, totalsColumns);

        assertColumnOrder(totalsColumns, 0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testSyncTotalColumnsFollowsFileColumnOrder() {
        DefaultTableColumnModel filesColumns = columnModel(0, 5, 1, 2, 3, 4);
        DefaultTableColumnModel totalsColumns = columnModel(0, 1, 2, 3, 4, 5, 6);

        ProjectFilesListController.syncTotalColumnsToFileColumns(filesColumns, totalsColumns);

        assertColumnOrder(totalsColumns, 0, 5, 1, 2, 3, 4, 6);
    }

    private static ProjectFilesListController.FileProgress fileProgress(int translated, int total) {
        return new ProjectFilesListController.FileProgress(translated, total);
    }

    private static DefaultTableColumnModel columnModel(int... modelIndexes) {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        for (int modelIndex : modelIndexes) {
            columnModel.addColumn(new TableColumn(modelIndex));
        }
        return columnModel;
    }

    private static void assertColumnOrder(DefaultTableColumnModel columnModel, int... modelIndexes) {
        assertEquals(modelIndexes.length, columnModel.getColumnCount());
        for (int i = 0; i < modelIndexes.length; i++) {
            assertEquals(modelIndexes[i], columnModel.getColumn(i).getModelIndex());
        }
    }

    private static SourceTextEntry sourceEntry(String source, int entryNum) {
        return new SourceTextEntry(new EntryKey("source.txt", source, null, "", "", null), entryNum, null,
                null, Collections.emptyList());
    }
}
