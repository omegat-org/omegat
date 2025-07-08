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

import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import static picocli.CommandLine.Option;

@CommandLine.Command(name = "omegat", mixinStandardHelpOptions = true, version = "6.1.0",
        subcommands = {StartCommand.class, AlignCommand.class, TranslateCommand.class, StatsCommand.class,
                TeamCommand.class, CommandLine.HelpCommand.class})
public class LegacyParameters implements Runnable {

    @CommandLine.Option(names = { "-V", "--version" }, versionHelp = true)
    boolean versionInfoRequested;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true)
    boolean usageHelpRequested;

    // Hide deprecated old command syntax in help message.
    // We can set false for test purpose.
    private static final boolean HIDE_DEPRECATED_OPTIONS = false;

    public static final String CONFIG_FILE = "--config-file";
    @Option(names = { CONFIG_FILE }, paramLabel = "<path>", descriptionKey = "CONFIG_FILE")
    String configFile;

    public static final String RESOURCE_BUNDLE = "--resource-bundle";
    @Option(names = { RESOURCE_BUNDLE }, paramLabel = "<bundle>", descriptionKey = "RESOURCE_BUNDLE")
    String resourceBundle;

    public static final String CONFIG_DIR = "--config-dir";
    @Option(names = { CONFIG_DIR }, paramLabel = "<path>", descriptionKey = "CONFIG_DIR")
    String configDir;

    public static final String DISABLE_PROJECT_LOCKING = "--disable-project-locking";
    @Option(names = { DISABLE_PROJECT_LOCKING }, descriptionKey = "DISABLE_PROJECT_LOCKING")
    boolean disableProjectLocking;

    public static final String DISABLE_LOCATION_SAVE = "--disable-location-save";
    @Option(names = { DISABLE_LOCATION_SAVE }, descriptionKey = "DISABLE_LOCATION_SAVE")
    boolean disableLocationSave;

    /**
     * CLI parameter to disable team functionality (treat as local project)
     */
    public static final String NO_TEAM = "--no-team";
    @Option(names = { NO_TEAM }, descriptionKey = "NO_TEAM")
    boolean noTeam;

    // All modes
    public static final String MODE = "--mode";
    @Option(names = {
            MODE }, paramLabel = "<console-mode-name>", hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "MODE")
    String consoleMode;

    // CONSOLE_TRANSLATE mode
    public static final String SOURCE_PATTERN = "--source-pattern";
    @Option(names = { SOURCE_PATTERN }, hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "SOURCE_PATTERN")
    String sourcePattern;

    // CONSOLE_CREATEPSEUDOTRANSLATETMX mode
    public static final String PSEUDOTRANSLATETMX = "--pseudotranslatetmx";
    @Option(names = {
            PSEUDOTRANSLATETMX }, paramLabel = "<path>", hidden = HIDE_DEPRECATED_OPTIONS,
            descriptionKey = "PSEUDO_TRANSLATE_TMX")
    String pseudoTranslateTmxPath;

    public static final String PSEUDOTRANSLATETYPE = "--pseudotranslatetype";
    @Option(names = {
            PSEUDOTRANSLATETYPE }, paramLabel = "<equal_or_empty>", hidden = HIDE_DEPRECATED_OPTIONS,
            descriptionKey = "PSEUDO_TRANSLATE_TYPE")
    String pseudoTranslateTypeName;

    // CONSOLE_ALIGN mode
    public static final String ALIGNDIR = "--alignDir";
    @Option(names = {
            ALIGNDIR }, paramLabel = "<path>", hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "ALIGN_DIR")
    String alignDirPath;

    // CONSOLE_STATS mode
    public static final String STATS_OUTPUT = "--output-file";
    public static final String STATS_MODE = "--stats-type";
    @Option(names = {
            STATS_OUTPUT }, paramLabel = "<stats-output-file>", hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "OUTPUT_FILE")
    String statsOutput;
    @Option(names = {
            STATS_MODE }, paramLabel = "<xml_or_text_or_json>", hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "STATS_TYPE")
    String statsType;

    public void setStatsOutput(String statsOutput) {
        this.statsOutput = statsOutput;
    }

    public void setStatsType(String statsType) {
        this.statsType = statsType;
    }

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE, arity = "0..1")
    String project;

    public void initialize() {
        applyConfigFile(configFile);
    }

    /**
     * Default method when launch.
     */
    @Override
    public void run() {
        int result;
        if (consoleMode == null) {
            Parameters params = new Parameters();
            params.initialize();
            params.setProjectLocation(project);
            StartCommand command = new StartCommand(params);
            result = command.runGUI();
            if (result != 0) {
                System.exit(result);
            }
        } else {
            try {
                Parameters params = new Parameters();
                params.initialize();
                params.setProjectLocation(project);
                switch (consoleMode) {
                case ("console-translate"):
                    TranslateCommand translateCommand = new TranslateCommand(project);
                    result = translateCommand.runConsoleTranslate();
                    if (result != 0) {
                        System.exit(result);
                    }
                    break;
                case ("console-align"):
                    AlignCommand alignCommand = new AlignCommand(project);
                    try {
                        int status = alignCommand.runConsoleAlign();
                        if (status != 0) {
                            System.exit(status);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to align.");
                        System.exit(1);
                    }
                    break;
                case ("console-stats"):
                    StatsCommand statsCommand = new StatsCommand(params);
                    try {
                        result = statsCommand.runConsoleStats();
                        if (result != 0) {
                            System.exit(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to print stats.");
                        System.exit(1);
                    }
                    break;
                case ("console-createpseudotranslatetmx"):
                    PseudoTranslateCommand pseudoTranslateCommand = new PseudoTranslateCommand();
                    try {
                        int status = pseudoTranslateCommand.runCreatePseudoTranslateTMX();
                        if (status != 0) {
                            System.exit(status);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to create pseudo-translate TMX.");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unknown console mode: " + consoleMode);
                    System.exit(1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> constructGuiArgs() {
        List<String> result = new ArrayList<>();
        if (noTeam) {
            result.add(NO_TEAM);
        }
        if (configDir != null) {
            result.add(CONFIG_DIR);
            result.add(configDir);
        }
        if (resourceBundle != null) {
            result.add(RESOURCE_BUNDLE);
            result.add(resourceBundle);
        }
        return result;
    }

    /**
     * Load System properties from a specified .properties file. In order to
     * allow this to reliably change the display language, it must called before
     * any use of {@link Log#log}, thus it logs to {@link System#out}.
     *
     * @param path
     *            to config file
     */
    void applyConfigFile(String path) {
        if (path == null) {
            return;
        }
        File configFile = new File(FileUtil.expandTildeHomeDir(path));
        if (!configFile.exists()) {
            return;
        }
        System.out.println("Reading config from " + path);
        try (FileInputStream in = new FileInputStream(configFile)) {
            PropertyResourceBundle config = new PropertyResourceBundle(in);
            // Put config properties into System properties and into OmegaT
            // params.
            for (String key : config.keySet()) {
                String value = config.getString(key);
                System.setProperty(key, value);
                System.out.println("Read from config: " + key + "=" + value);
            }
            // Apply language preferences, if present.
            // This must be done with Locale.setDefault(). Merely doing
            // System.setProperty() will not work.
            if (config.containsKey("user.language")) {
                String userLanguage = config.getString("user.language");
                Locale userLocale = config.containsKey("user.country")
                        ? new Locale(userLanguage, config.getString("user.country"))
                        : new Locale(userLanguage);
                Locale.setDefault(userLocale);
            }
        } catch (FileNotFoundException exception) {
            System.err.println("Config file not found: " + path);
        } catch (IOException exception) {
            System.err.println("Error while reading config file: " + path);
        }
    }
}
