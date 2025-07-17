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
package org.omegat.cli;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.omegat.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ConsoleStatsTest extends ConsoleTestsCommon {

    @Parameterized.Parameters(name = "{index}: statsType={0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "text", "Project Statistics", AssertionType.CONTAINS },
                { "json", "{\"total\":{\"segments\":0,", AssertionType.STARTS_WITH },
                { "xml", "<omegat-stats>", AssertionType.LINE_2_STARTS_WITH } });
    }

    private enum AssertionType {
        CONTAINS, STARTS_WITH, LINE_2_STARTS_WITH;
    }

    private final String statsType;
    private final String expectedContent;
    private final AssertionType assertionType;

    public ConsoleStatsTest(String statsType, String expectedContent, AssertionType assertionType) {
        this.statsType = statsType;
        this.expectedContent = expectedContent;
        this.assertionType = assertionType;
    }

    @Test
    public void testConsoleStatsLegacy() throws Exception {
        prepareTestConsoleStats();

        Path outputFile = getTargetDir().resolve("stats." + statsType);
        Main.main(new String[] { String.format("--config-dir=%s", getConfigDir()), "--mode=console-stats",
                String.format("--stats-type=%s", statsType), String.format("--output-file=%s", outputFile),
                getProjectDir().toString() });

        Assertions.assertThat(outputFile).exists();
        List<@NotNull String> lines = Files.readAllLines(outputFile);

        switch (assertionType) {
        case STARTS_WITH:
            Assertions.assertThat(lines.get(0)).startsWith(expectedContent);
            break;
        case LINE_2_STARTS_WITH:
            Assertions.assertThat(lines.get(1)).startsWith(expectedContent);
            break;
        case CONTAINS:
            Assertions.assertThat(lines).contains(expectedContent);
            break;
        default:
            fail("Unknown assertion type.");
        }
    }

    private void prepareTestConsoleStats() throws Exception {
        prep();
        Path testTmx = Paths.get("test/data/tmx/project_save.tmx");
        Files.copy(testTmx, getProjectDir().resolve("omegat/project_save.tmx"));
    }
}
