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

import org.omegat.core.Core;
import org.omegat.core.data.RealProject;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.StatOutputFormat;
import org.omegat.core.statistics.StatsResult;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import picocli.CommandLine;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@CommandLine.Command(name = "stats")
public class StatsCommand implements Runnable {

    @CommandLine.ParentCommand
    private LegacyParameters legacyParams;

    @CommandLine.Mixin
    private Parameters params;

    @CommandLine.Option(names = {"type"}, paramLabel = "<xml_or_text_or_json>", defaultValue = "xml",
            descriptionKey = "STATS_TYPE")
    String format;
    @CommandLine.Option(names = {"output"}, paramLabel = "<stats-output-file>",
            descriptionKey = "OUTPUT_FILE")
    String output;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    String project;

    public StatsCommand() {
    }

    public StatsCommand(Parameters params) {
        this.params = params;
    }

    @Override
    public void run() {
        legacyParams.initialize();
        params.initialize();
        params.setStatsOutput(output);
        params.setStatsType(format);
        try {
            int status = runConsoleStats();
            if (status != 0) {
                System.exit(status);
            }
        } catch (Exception e) {
            System.err.println("Failed to print stats.");
            System.exit(1);
        }

    }

    /**
     * Displays or writes project statistics.
     * <p>
     * takes two optional arguments
     * <code>[--output-file=(file path) [--stats-type=[XML|JSON|TEXT]]]</code>
     * when omitted, display stats text(localized). When file I/O error
     * occurred, especially when parent directory does not exist warns it and
     * return 1.
     */
    int runConsoleStats() throws Exception {
        Log.logInfoRB("STARTUP_CONSOLE_STATS_MODE");

        Core.initializeConsole();

        RealProject p = Common.selectProjectConsoleMode(true, params);
        StatsResult projectStats = CalcStandardStatistics.buildProjectStats(p);

        if (params.statsOutput == null) {
            // no output file specified, print to console.
            System.out.println(projectStats.getTextData());
            p.closeProject();
            return 0;
        }

        String outputFilename = params.statsOutput;
        StatOutputFormat statsMode;
        if (params.statsType == null) {
            // when no stats type specified, try to detect from file extension,
            // otherwise XML.
            if (outputFilename.toLowerCase().endsWith(StatOutputFormat.JSON.getFileExtension())) {
                statsMode = StatOutputFormat.JSON;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.XML.getFileExtension())) {
                statsMode = StatOutputFormat.XML;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.TEXT.getFileExtension())) {
                statsMode = StatOutputFormat.TEXT;
            } else {
                statsMode = StatOutputFormat.XML;
            }
        } else if (StatOutputFormat.JSON.toString().equalsIgnoreCase(params.statsType)) {
            statsMode = StatOutputFormat.JSON;
        } else if (StatOutputFormat.XML.toString().equalsIgnoreCase(params.statsType)) {
            statsMode = StatOutputFormat.XML;
        } else if (StatOutputFormat.TEXT.toString().equalsIgnoreCase(params.statsType)) {
            statsMode = StatOutputFormat.TEXT;
        } else {
            statsMode = StatOutputFormat.XML;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(
                Paths.get(FileUtil.expandTildeHomeDir(outputFilename)), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), StandardCharsets.UTF_8)) {
            switch (statsMode) {
                case TEXT:
                    writer.write(projectStats.getTextData());
                    break;
                case JSON:
                    writer.write(projectStats.getJsonData());
                    break;
                case XML:
                    writer.write(projectStats.getXmlData());
                    break;
                default:
                    Log.logWarningRB("CONSOLE_STATS_WARNING_TYPE");
                    break;
            }
        } catch (NoSuchFileException nsfe) {
            Log.logErrorRB("CONSOLE_STATS_FILE_OPEN_ERROR");
            return 1;
        } finally {
            p.closeProject();
        }
        return 0;
    }

}
