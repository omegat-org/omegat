/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke, Alex Buloichik, Didier Briel
               2012 Aaron Madlon-Kay
               2013 Kyle Katarn, Aaron Madlon-Kay
               2014 Alex Buloichik
               2018 Enrique Estevez Fernandez
               2022-2025 Hiroshi Miura
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

package org.omegat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.omegat.cli.LegacyParameters;
import picocli.CommandLine;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * The main OmegaT class, used to launch the program.
 *
 * @author Keith Godfrey
 * @author Martin Fleurke
 * @author Alex Buloichik
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Kyle Katarn
 * @author Hiroshi Miura
 */

public final class Main implements Runnable {

    private Main() {
    }

    @CommandLine.Parameters
    private static LegacyParameters legacyParameters;

    public static void main(String[] args) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.omegat.cli.Parameters");
        // construct parser and execute
        legacyParameters = new LegacyParameters();
        CommandLine commandLine = new CommandLine(legacyParameters);
        commandLine.setResourceBundle(resourceBundle);
        commandLine.setExecutionStrategy(new CommandLine.RunLast());
        commandLine.execute(args);
    }

    public static void restartGUI(String projectDir) {
        // Check we have `java` command in java.home
        Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        List<String> command = new ArrayList<>();
        if (javaBin.toFile().exists()) {
            // Build command: java -cp ... org.omegat.Main
            command.add(javaBin.toString());
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            command.addAll(runtimeMxBean.getInputArguments()); // JVM args
            command.add("-cp");
            command.add(runtimeMxBean.getClassPath());
            command.add(Main.class.getName());
        } else {
            // assumes jpackage
            var installDir = StaticUtils.installDir();
            if (installDir == null) {
                return;
            } else {
                javaBin = Paths.get(StaticUtils.installDir()).getParent().resolve("bin/OmegaT");
                if (!javaBin.toFile().exists()) {
                    // abort restart
                    Core.getMainWindow().displayWarningRB("LOG_RESTART_FAILED_NOT_FOUND");
                    return;
                }
                command.add(javaBin.toString());
            }
        }
        if (projectDir != null) {
            command.add(projectDir);
        }
        // Now ready to restart.
        Log.log("===         Restart OmegaT           ===");
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException e) {
            Log.log(e);
            System.exit(1);
        }
    }

    @Override
    public void run() {

    }
}
