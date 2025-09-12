/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.properties;

import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.JTableFixture;
import org.junit.Test;
import org.omegat.gui.main.TestCoreGUI;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class SegmentPropertiesAreaTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Test
    public void testSegmentPropertiesPaneExist() throws Exception {
        // load project
        openSampleProjectWaitPropertyPane(PROJECT_PATH);
        robot().waitForIdle();
        // check a segment properties pane
        assertNotNull(window);
        window.scrollPane("Segment Properties").requireEnabled();
        window.scrollPane("Segment Properties").requireVisible();
        // Check a table content
        JTableFixture segmentPropertiesTable = window.table("SegmentPropertiesTable").requireVisible();

        // Define expectations
        int expectedRowCount = 8;
        int expectedColumnCount = 3;
        final Map<String, String> expectation = getExpectedMap();

        // Check values of the table
        segmentPropertiesTable.requireRowCount(expectedRowCount);
        segmentPropertiesTable.requireColumnCount(expectedColumnCount);
        for (int i = 0; i < expectedRowCount; i++) {
            String key = segmentPropertiesTable.valueAt(TableCell.row(i).column(0));
            String value = segmentPropertiesTable.valueAt(TableCell.row(i).column(1));
            assertThat(key).isIn(expectation.keySet());
            assertThat(value).isEqualTo(expectation.get(key));
        }
    }

    private Map<String, String> getExpectedMap() {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
        ZonedDateTime utcDateTime = ZonedDateTime.parse("20241127T035216Z", customFormatter);
        ZonedDateTime expectedDateTime = utcDateTime.withZoneSameInstant(java.time.ZoneId.systemDefault());
        final String expected = expectedDateTime
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy h:m:s a", Locale.getDefault()));
        final String translator = "Hiroshi Miura";
        //
        final Map<String, String> expectation = new HashMap<>();
        expectation.put("ID", "APERTIUM_ERROR");
        expectation.put("Changed by", translator);
        expectation.put("Changed on", expected);
        expectation.put("Created on", expected);
        expectation.put("Created by", translator);
        expectation.put("Origin", "Unknown/Manual");
        expectation.put("File", "Bundle.properties");
        expectation.put("Repeated", "FIRST");
        return expectation;
    }

}
