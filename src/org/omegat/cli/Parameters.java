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

import java.util.ArrayList;
import java.util.List;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "omegat", mixinStandardHelpOptions = true, version = "6.1.0")
public class Parameters implements Runnable {

    // Hide deprecated old command syntax in help message.
    // We can set false for test purpose.
    private static final boolean HIDE_DEPRECATED_OPTIONS = true;

    @Option(names = { "-V", "--version" }, versionHelp = true)
    boolean versionInfoRequested;

    @Option(names = { "-h", "--help" }, usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = { "--verbose" }, descriptionKey = "VERBOSE")
    boolean verbose;

    // All modes
    public static final String MODE = "--mode";
    @Option(names = {
            MODE }, paramLabel = "<console-mode-name>", hidden = HIDE_DEPRECATED_OPTIONS, descriptionKey = "MODE")
    String consoleMode;

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

    /**
     * CLI parameter to specify source tokenizer
     */
    public static final String TOKENIZER_SOURCE = "--ITokenizer";
    @Option(names = { TOKENIZER_SOURCE }, descriptionKey = "TOKENIZER_SOURCE")
    String tokenizerSource;
    /**
     * CLI parameter to specify target tokenizer
     */
    public static final String TOKENIZER_TARGET = "--ITokenizerTarget";
    @Option(names = { TOKENIZER_TARGET }, descriptionKey = "TOKENIZER_TARGET")
    String tokenizerTarget;

    // Non-GUI modes only
    public static final String QUIET = "--quiet";
    @Option(names = { QUIET }, descriptionKey = "QUIET")
    boolean isQuiet;

    public static final String SCRIPT = "--script";
    @Option(names = { SCRIPT }, paramLabel = "<path>", descriptionKey = "SCRIPT")
    String scriptName;

    public static final String TAG_VALIDATION = "--tag-validation";
    @Option(names = { TAG_VALIDATION }, descriptionKey = "TAG_VALIDATION")
    String tagValidation;

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
            STATS_OUTPUT }, paramLabel = "<stats-output-file>", hidden = true, descriptionKey = "OUTPUT_FILE")
    String statsOutput;
    @Option(names = {
            STATS_MODE }, paramLabel = "<xml_or_text_or_json>", hidden = true, descriptionKey = "STATS_TYPE")
    String statsType;

    // Undocumented CLI options
    public static final String ALTERNATE_FILENAME_FROM = "--alternate-filename-from";
    @Option(names = { ALTERNATE_FILENAME_FROM }, paramLabel = "<alternate_filename_from>", hidden = true, descriptionKey = "ALTERNATE_FILENAME_FROM")
    String alternateFilenameFrom;
    public static final String ALTERNATE_FILENAME_TO = "--alternate-filename-to";
    @Option(names = { ALTERNATE_FILENAME_TO }, paramLabel = "<alternate_filename_to>", hidden = true, descriptionKey = "ALTERNATE_FILENAME_TO")
    String alternateFilenameTo;

    // Development
    public static final String DEV_MANIFESTS = "dev-manifests";

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", description = "Path to the project", defaultValue = "")
    String projectLocation;

    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public void setStatsOutput(String statsOutput) {
        this.statsOutput = statsOutput;
    }

    public void setStatsType(String statsType) {
        this.statsType = statsType;
    }

    /**
     * Default method when launch.
     */
    @Override
    public void run() {
        StandardCommandLauncher command;
        int result;
        if (consoleMode == null) {
            command = new StandardCommandLauncher(this);
            result = command.runGUI();
            if (result != 0) {
                System.exit(result);
            }
        } else {
            try {
                switch (consoleMode) {
                case ("console-translate"):
                    command = new StandardCommandLauncher(this);
                    result = command.runConsoleTranslate();
                    if (result != 0) {
                        System.exit(result);
                    }
                    break;
                case ("console-align"):
                    command = new StandardCommandLauncher(this);
                    try {
                        int status = command.runConsoleAlign();
                        if (status != 0) {
                            System.exit(status);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to align.");
                        System.exit(1);
                    }
                    break;
                case ("console-stats"):
                    command = new StandardCommandLauncher(this);
                    try {
                        result = command.runConsoleStats();
                        if (result != 0) {
                            System.exit(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to print stats.");
                        System.exit(1);
                    }
                    break;
                case ("console-createpseudotranslatetmx"):
                    command = new StandardCommandLauncher(this);
                    try {
                        int status = command.runCreatePseudoTranslateTMX();
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
        if (isQuiet) {
            result.add(QUIET);
        }
        if (tokenizerSource != null) {
            result.add(TOKENIZER_SOURCE);
            result.add(tokenizerSource);
        }
        if (tokenizerTarget != null) {
            result.add(TOKENIZER_TARGET);
            result.add(tokenizerTarget);
        }
        if (configDir != null) {
            result.add(CONFIG_DIR);
            result.add(configDir);
        }
        if (resourceBundle != null) {
            result.add(RESOURCE_BUNDLE);
            result.add(resourceBundle);
        }
        if (scriptName != null) {
            result.add(SCRIPT);
            result.add(scriptName);
        }
        return result;
    }

}
