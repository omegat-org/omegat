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

import org.junit.Test;

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
    public void testFormatTranslatedUniqueProgress() {
        assertEquals("0 (0%)", ProjectFilesListController.formatTranslatedUniqueProgress(0, 0));
        assertEquals("0 (0%)", ProjectFilesListController.formatTranslatedUniqueProgress(0, 13));
        assertEquals("13 (100.0%)", ProjectFilesListController.formatTranslatedUniqueProgress(13, 13));
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

    private static ProjectFilesListController.FileProgress fileProgress(int translated, int total) {
        return new ProjectFilesListController.FileProgress(translated, total);
    }
}
