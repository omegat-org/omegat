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

import com.vlsolutions.swing.docking.DockingDesktop;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.languagetools.LanguageClassBroker;
import org.omegat.languagetools.LanguageDataBroker;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import picocli.CommandLine;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static picocli.CommandLine.Option;

public class Parameters {

    /**
     * CLI parameter to disable team functionality (treat as local project)
     */
    public static final String NO_TEAM = "--noteam";
    @Option(names = { NO_TEAM }, descriptionKey = "NO_TEAM")
    boolean noTeam = false;

    /**
     * CLI parameter to specify source tokenizer
     */
    public static final String TOKENIZER_SOURCE = "--ITokenizer";
    @Option(names = { TOKENIZER_SOURCE }, descriptionKey = "TOKENIZER_SOURCE")
    @Nullable String tokenizerSource;
    /**
     * CLI parameter to specify target tokenizer
     */
    public static final String TOKENIZER_TARGET = "--ITokenizerTarget";
    @Option(names = { TOKENIZER_TARGET }, descriptionKey = "TOKENIZER_TARGET")
    @Nullable String tokenizerTarget;

    // Non-GUI modes only
    public static final String QUIET = "--quiet";
    @Option(names = { QUIET }, descriptionKey = "QUIET")
    boolean isQuiet = false;

    public static final String SCRIPT = "--script";
    @Option(names = { SCRIPT }, paramLabel = "<path>", descriptionKey = "SCRIPT")
    @Nullable String scriptName;

    public static final String TAG_VALIDATION = "--tag-validation";
    @Option(names = { TAG_VALIDATION }, descriptionKey = "TAG_VALIDATION")
    @Nullable String tagValidation;

    // CONSOLE_STATS mode
    public static final String STATS_OUTPUT = "--output-file";
    public static final String STATS_MODE = "--stats-type";
    @Option(names = {
            STATS_OUTPUT }, paramLabel = "<stats-output-file>", hidden = true, descriptionKey = "OUTPUT_FILE")
    @Nullable String statsOutput;
    @Option(names = {
            STATS_MODE }, paramLabel = "<xml_or_text_or_json>", hidden = true, descriptionKey = "STATS_TYPE")
    @Nullable String statsType;

    // Undocumented CLI options
    public static final String ALTERNATE_FILENAME_FROM = "--alternate-filename-from";
    @Option(names = {
            ALTERNATE_FILENAME_FROM }, paramLabel = "<alternate_filename_from>", hidden = true, descriptionKey = "ALTERNATE_FILENAME_FROM")
    @Nullable String alternateFilenameFrom;
    public static final String ALTERNATE_FILENAME_TO = "--alternate-filename-to";
    @Option(names = {
            ALTERNATE_FILENAME_TO }, paramLabel = "<alternate_filename_to>", hidden = true, descriptionKey = "ALTERNATE_FILENAME_TO")
    @Nullable String alternateFilenameTo;

    // Development
    public static final String DEV_MANIFESTS = "dev-manifests";

    @Nullable String projectLocation;

    public void setProjectLocation(@Nullable String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public void setStatsOutput(@Nullable String statsOutput) {
        this.statsOutput = statsOutput;
    }

    public void setStatsType(@Nullable String statsType) {
        this.statsType = statsType;
    }

    public static final String VERBOSE = "--verbose";
    @CommandLine.Option(names = { VERBOSE }, descriptionKey = "VERBOSE")
    boolean verbose = false;

    public void initialize() {
        if (verbose) {
            Log.setConsoleLevel(java.util.logging.Level.INFO);
        }
        if (isQuiet) {
            Log.setConsoleLevel(java.util.logging.Level.SEVERE);
        }
        showStartUpLogInfo();
        initializeApp();
    }

    private void showStartUpLogInfo() {
        // initialize logging backend and loading configuration.
        Log.logInfoRB("STARTUP_LOGGING_INFO", StringUtils.repeat('=', 120), OStrings.getNameAndVersion(),
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
                        .format(ZonedDateTime.now()),
                ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                Locale.getDefault().toLanguageTag());
        Log.logInfoRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"),
                System.getProperty("java.version"), System.getProperty("java.home"));

        Log.logInfoRB("STARTUP_GUI_DOCKING_FRAMEWORK", DockingDesktop.getDockingFrameworkVersion());
    }

    private void initializeApp() {
        // Workaround for Java 17 or later support of JAXB.
        // See https://sourceforge.net/p/omegat/feature-requests/1682/#12c5
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        System.setProperty("http.agent", OStrings.getDisplayNameAndVersion());

        // Do migration and load various settings. The order is important!
        Preferences.init();
        // broker should be loaded before module loading
        JLanguageTool.setClassBrokerBroker(new LanguageClassBroker());
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        PluginUtils.loadPlugins(null);
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
        Preferences.initFilters();
        Preferences.initSegmentation();
    }

    public List<String> constructGuiArgs() {
        List<String> result = new ArrayList<>();
        if (isQuiet) {
            result.add(QUIET);
        }
        if (verbose) {
            result.add(VERBOSE);
        }
        if (tokenizerSource != null) {
            result.add(TOKENIZER_SOURCE);
            result.add(tokenizerSource);
        }
        if (tokenizerTarget != null) {
            result.add(TOKENIZER_TARGET);
            result.add(tokenizerTarget);
        }
        if (scriptName != null) {
            result.add(SCRIPT);
            result.add(scriptName);
        }
        return result;
    }

}
