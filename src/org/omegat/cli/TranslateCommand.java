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

import java.util.Objects;

@CommandLine.Command(name = "translate")
public class TranslateCommand implements Runnable {
    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    String project;

    private final Parameters params;

    public TranslateCommand(Parameters parent) {
        params = parent;
    }

    @Override
    public void run() {
        params.setProjectLocation(Objects.requireNonNullElse(project, "."));
        StandardCommandLauncher command = new StandardCommandLauncher(params);
        int status = command.runConsoleTranslate();
        if (status != 0) {
            System.exit(status);
        }
    }
}
