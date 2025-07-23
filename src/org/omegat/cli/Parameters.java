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
import org.omegat.util.RuntimePreferences;
import picocli.CommandLine;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Locale;

import static picocli.CommandLine.Option;

public class Parameters {

    /**
     * CLI parameter to disable team functionality (treat as local project)
     */
    public static final String TEAM = "--team";
    @Option(names = { TEAM }, negatable = true, descriptionKey = "params.TEAM")
    boolean team = true;

    /**
     * CLI parameter to specify source tokenizer
     */
    public static final String TOKENIZER_SOURCE = "--ITokenizer";
    @Option(names = { TOKENIZER_SOURCE }, descriptionKey = "params.TOKENIZER_SOURCE")
    @Nullable String tokenizerSource;
    /**
     * CLI parameter to specify target tokenizer
     */
    public static final String TOKENIZER_TARGET = "--ITokenizerTarget";
    @Option(names = { TOKENIZER_TARGET }, descriptionKey = "params.TOKENIZER_TARGET")
    @Nullable String tokenizerTarget;

    // Non-GUI modes only
    public static final String QUIET = "--quiet";
    @Option(names = { QUIET }, descriptionKey = "params.QUIET")
    boolean isQuiet = false;

    public static final String SCRIPT = "--script";
    @Option(names = { SCRIPT }, paramLabel = "<path>", descriptionKey = "params.SCRIPT")
    @Nullable String scriptName;

    public static final String TAG_VALIDATION = "--tag-validation";
    @Option(names = { TAG_VALIDATION }, descriptionKey = "params.TAG_VALIDATION")
    @Nullable String tagValidation;

    // Undocumented CLI options
    public static final String ALTERNATE_FILENAME_FROM = "--alternate-filename-from";
    @Option(names = {
            ALTERNATE_FILENAME_FROM }, paramLabel = "<alternate_filename_from>", hidden = true, descriptionKey = "params.ALTERNATE_FILENAME_FROM")
    @Nullable String alternateFilenameFrom;
    public static final String ALTERNATE_FILENAME_TO = "--alternate-filename-to";
    @Option(names = {
            ALTERNATE_FILENAME_TO }, paramLabel = "<alternate_filename_to>", hidden = true, descriptionKey = "params.ALTERNATE_FILENAME_TO")
    @Nullable String alternateFilenameTo;

    // Development
    public static final String DEV_MANIFESTS = "dev-manifests";

    @Nullable String projectLocation;

    public void setProjectLocation(@Nullable String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public static final String VERBOSE = "--verbose";
    @CommandLine.Option(names = { VERBOSE }, descriptionKey = "params.VERBOSE")
    boolean verbose = false;

    public void initialize() {
        if (verbose) {
            Log.setConsoleLevel(java.util.logging.Level.INFO);
        }
        if (isQuiet) {
            Log.setConsoleLevel(java.util.logging.Level.SEVERE);
            RuntimePreferences.setQuietMode(true);
        }
    }
}
