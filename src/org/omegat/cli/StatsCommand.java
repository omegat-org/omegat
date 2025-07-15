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

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.RealProject;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.StatOutputFormat;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.StatsResult;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "stats", resourceBundle = "org.omegat.cli.Parameters")
public class StatsCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    @Nullable LegacyParameters legacyParams;

    @CommandLine.Mixin
    @Nullable Parameters params;

    @CommandLine.Option(names = {
            "type" }, paramLabel = "<xml_or_text_or_json>", defaultValue = "xml", descriptionKey = "STATS_TYPE")
    @Nullable String format;
    @CommandLine.Option(names = {
            "output" }, paramLabel = "<stats-output-file>", descriptionKey = "OUTPUT_FILE")
    @Nullable String output;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    @Nullable String project;

    @Override
    public Integer call() {
        if (params == null || legacyParams == null) {
            return 1;
        }
        legacyParams.initialize();
        params.initialize();
        if (output != null) {
            params.setStatsOutput(output);
        }
        if (format != null) {
            params.setStatsType(format);
        }
        try {
            return runConsoleStats();
        } catch (Exception e) {
            Log.logErrorRB(e, "CT_ERROR_PRINTING_STATS");
            return 1;
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
    int runConsoleStats() {
        if (params == null || legacyParams == null) {
            return 1;
        }
        Log.logInfoRB("STARTUP_CONSOLE_STATS_MODE");

        Core.initializeConsole();

        RealProject p = Common.selectProjectConsoleMode(true, params);
        StatsResult projectStats = CalcStandardStatistics.buildProjectStats(p);
        StatOutputFormat statsMode;
        String outputFilename = params.statsOutput;

        if (outputFilename == null) {
            // no output file specified, print to console.
            System.out.println(projectStats.getTextData());
            p.closeProject();
            return 0;
        } else if (params.statsType == null) {
            // when no stats type specified, try to detect from file extension,
            // otherwise XML.
            if (outputFilename.toLowerCase(Locale.ENGLISH).endsWith(StatOutputFormat.JSON.getFileExtension())) {
                statsMode = StatOutputFormat.JSON;
            } else if (outputFilename.toLowerCase(Locale.ENGLISH).endsWith(StatOutputFormat.XML.getFileExtension())) {
                statsMode = StatOutputFormat.XML;
            } else if (outputFilename.toLowerCase(Locale.ENGLISH).endsWith(StatOutputFormat.TEXT.getFileExtension())) {
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
        File statsFile = Paths.get(FileUtil.expandTildeHomeDir(outputFilename)).toFile();
        Statistics.writeStat(statsFile, projectStats, statsMode);
        return 0;
    }

}
