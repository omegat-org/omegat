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

import picocli.CommandLine;

@CommandLine.Command(name = "stats")
public class StatsCommand implements Runnable {
    private final Parameters parameters;
    @CommandLine.Option(names = {"type"}, paramLabel = "<xml_or_text_or_json>", defaultValue = "xml",
            descriptionKey = "STATS_TYPE")
    String format;
    @CommandLine.Option(names = {"output"}, paramLabel = "<stats-output-file>",
            descriptionKey = "OUTPUT_FILE")
    String output;

    public StatsCommand(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void run() {
        parameters.setStatsOutput(output);
        parameters.setStatsType(format);
        StandardCommandLauncher command = new StandardCommandLauncher(parameters);
        try {
            int status = command.runConsoleStats();
            if (status != 0) {
                System.exit(status);
            }
        } catch (Exception e) {
            System.err.println("Failed to print stats.");
            System.exit(1);
        }

    }
}
