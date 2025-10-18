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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.omegat.cli.LegacyParameters;
import org.omegat.cli.SubCommands;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.RuntimePreferences;
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
public final class Main {

    // Development
    public static final String DEV_MANIFESTS = "dev-manifests";

    private Main() {
    }

    public static void main(String[] args) {
        // Stage 2: Load plugins with configuration
        Map<String, String> pluginConfig = extractPluginConfiguration(args);
        PluginUtils.loadPlugins(pluginConfig);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.omegat.cli.Parameters");
        // construct parser and execute
        CommandLine commandLine = new CommandLine(new LegacyParameters());
        commandLine.setResourceBundle(resourceBundle);
        commandLine.setExecutionStrategy(new CommandLine.RunLast());

        // Register subcommands provided by modules/plugins
        for (SubCommands.SubCommandEntry entry : SubCommands.getSubCommandEntries()) {
            commandLine.addSubcommand(entry.name, entry.subcommand);
        }

        // Explicitly handle top-level help to ensure `./OmegaT --help` prints usage
        if (args != null && args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            commandLine.usage(System.out);
            return;
        }
        int status = commandLine.execute(args);
        if (status != 0) {
            // Should not call exit when starting GUI.
            System.exit(status);
        }
    }

    public static void restartGUI(String projectDir) {
        // Check we have `java` command in java.home
        Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        String installDir = StaticUtils.installDir();
        Path parent = null;
        if (installDir != null) {
            parent = Paths.get(installDir).getParent();
        }
        if (!javaBin.toFile().exists()) {
            // on Windows
            javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java.exe");
        }
        List<String> command = new ArrayList<>();
        if (javaBin.toFile().exists()) {
            // Build command: java -cp ... org.omegat.Main
            command.add(javaBin.toString());
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            command.addAll(runtimeMxBean.getInputArguments()); // JVM args
            command.add("-cp");
            command.add(runtimeMxBean.getClassPath());
            command.add(Main.class.getName());
            constructCommandParams(command);
        } else if (parent != null) {
            // assumes jpackage or Windows installer
            javaBin = parent.resolve("bin/OmegaT");
            if (!javaBin.toFile().exists()) {
                javaBin = parent.resolve("OmegaT.exe");
            }
            if (!javaBin.toFile().exists()) {
                // abort restart
                Core.getMainWindow().displayWarningRB("LOG_RESTART_FAILED_NOT_FOUND");
                return;
            }
            command.add(javaBin.toString());
            constructCommandParams(command);
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

    /**
     * Extract plugin-related configuration from command-line arguments
     * before full parsing. This allows plugins to be loaded with proper
     * configuration before subcommands are registered.
     */
    private static Map<String, String> extractPluginConfiguration(String[] args) {
        Map<String, String> config = new HashMap<>();
        if (args == null) {
            return config;
        }

        for (int i = 0; i < args.length; i++) {
            if ("--dev-manifests".equals(args[i]) && i + 1 < args.length) {
                config.put(DEV_MANIFESTS, args[i + 1]);
                break;
            }
        }
        return config;
    }

    private static void constructCommandParams(List<String> command) {
        command.add("start");
        if (RuntimePreferences.isNoTeam()) {
            command.add("--no-team");
        }
        if (RuntimePreferences.isQuietMode()) {
            command.add("--quiet");
        }
        if (useAlternateFilename()) {
            command.add("--alternate-filename-from");
            command.add(RuntimePreferences.getAlternateFilenameFrom());
            command.add("--alternate-filenames-to");
            command.add(RuntimePreferences.getAlternateFilenameTo());
        }
    }

    private static boolean useAlternateFilename() {
        return RuntimePreferences.getAlternateFilenameFrom() != null
                && RuntimePreferences.getAlternateFilenameTo() != null;
    }
}
