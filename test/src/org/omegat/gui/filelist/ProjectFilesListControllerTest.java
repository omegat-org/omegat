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
    public void testFileProgressCompareTo() {
        ProjectFilesListController.FileProgress lower = new ProjectFilesListController.FileProgress(1, 4);
        ProjectFilesListController.FileProgress higher = new ProjectFilesListController.FileProgress(2, 4);
        ProjectFilesListController.FileProgress sameRatioMoreSegments =
                new ProjectFilesListController.FileProgress(2, 8);

        assertTrue(lower.compareTo(higher) < 0);
        assertTrue(higher.compareTo(lower) > 0);
        assertTrue(lower.compareTo(sameRatioMoreSegments) < 0);
    }
}
