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
               2026 Stephan Pakebusch
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

import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import org.omegat.cli.CommonParameters;
import org.omegat.cli.LegacyParameters;
import org.omegat.cli.SubCommands;
import org.omegat.core.Core;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.FileUtil;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;
import picocli.CommandLine;

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
        // Workaround for Java 17 or later support of JAXB.
        // See https://sourceforge.net/p/omegat/feature-requests/1682/#12c5
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        // --config-dir must take effect before plugins are loaded (user
        // plugins live below the configuration folder) and thus before the
        // command line is parsed by picocli; pre-scan the arguments like
        // extractPluginConfiguration() does.
        String configDir = extractConfigDir(args);
        if (configDir != null) {
            RuntimePreferences.setConfigDir(FileUtil.expandTildeHomeDir(configDir));
        }

        Map<String, String> pluginConfig = extractPluginConfiguration(args);
        PluginUtils.loadPlugins(pluginConfig);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.omegat.cli.Parameters");
        // construct parser and execute
        CommandLine commandLine = new CommandLine(new LegacyParameters());
        commandLine.setResourceBundle(resourceBundle);
        commandLine.setExecutionStrategy(new CommandLine.RunLast());
        SubCommands.registerSubCommandEntriesToCommandLine(commandLine);

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

    public static void restartGUI(@Nullable String projectDir) {
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

    /**
     * Pre-scan the command-line arguments for --config-dir before picocli
     * parses them, so that user plugins below the configuration folder are
     * already visible to loadPlugins().
     */
    @VisibleForTesting
    @Nullable
    static String extractConfigDir(String @Nullable [] args) {
        if (args == null) {
            return null;
        }
        for (int i = 0; i < args.length; i++) {
            String value = null;
            if (LegacyParameters.CONFIG_DIR.equals(args[i]) && i + 1 < args.length) {
                value = args[i + 1];
            } else if (args[i].startsWith(LegacyParameters.CONFIG_DIR + "=")) {
                value = args[i].substring(LegacyParameters.CONFIG_DIR.length() + 1);
            }
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Reconstructs the command line arguments for a GUI restart from the
     * runtime preferences. Options of the top-level command must precede the
     * start sub-command; the options of its {@link CommonParameters} mixin
     * must follow it.
     */
    @VisibleForTesting
    static void constructCommandParams(List<String> command) {
        if (RuntimePreferences.getConfigDir() != null) {
            command.add(LegacyParameters.CONFIG_DIR);
            command.add(RuntimePreferences.getConfigDir());
        }
        if (RuntimePreferences.getConfigFile() != null) {
            command.add(LegacyParameters.CONFIG_FILE);
            command.add(RuntimePreferences.getConfigFile());
        }
        if (RuntimePreferences.getResourceBundleFile() != null) {
            command.add(LegacyParameters.RESOURCE_BUNDLE);
            command.add(RuntimePreferences.getResourceBundleFile());
        }
        if (!RuntimePreferences.isProjectLockingEnabled()) {
            command.add(LegacyParameters.DISABLE_PROJECT_LOCKING);
        }
        if (!RuntimePreferences.isLocationSaveEnabled()) {
            command.add(LegacyParameters.DISABLE_LOCATION_SAVE);
        }
        if (RuntimePreferences.isNoTeam()) {
            command.add(LegacyParameters.NO_TEAM);
        }
        command.add("start");
        if (RuntimePreferences.isQuietMode()) {
            command.add(CommonParameters.QUIET);
        }
        if (RuntimePreferences.getTokenizerSource() != null) {
            command.add(CommonParameters.TOKENIZER_SOURCE);
            command.add(RuntimePreferences.getTokenizerSource());
        }
        if (RuntimePreferences.getTokenizerTarget() != null) {
            command.add(CommonParameters.TOKENIZER_TARGET);
            command.add(RuntimePreferences.getTokenizerTarget());
        }
        if (useAlternateFilename()) {
            command.add(CommonParameters.ALTERNATE_FILENAME_FROM);
            command.add(RuntimePreferences.getAlternateFilenameFrom());
            command.add(CommonParameters.ALTERNATE_FILENAME_TO);
            command.add(RuntimePreferences.getAlternateFilenameTo());
        }
    }

    private static boolean useAlternateFilename() {
        return RuntimePreferences.getAlternateFilenameFrom() != null
                && RuntimePreferences.getAlternateFilenameTo() != null;
    }
}
