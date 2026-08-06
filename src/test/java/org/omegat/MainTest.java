/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.cli.CommonParameters;
import org.omegat.cli.LegacyParameters;
import org.omegat.core.data.TestRuntimePreferenceStore;
import org.omegat.util.RuntimePreferences;

import picocli.CommandLine;

/**
 * Tests for the command line pre-scan and the restart command line in Main.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MainTest {

    @Before
    public void setUp() {
        TestRuntimePreferenceStore.resetPristine();
    }

    @After
    public void tearDown() {
        TestRuntimePreferenceStore.reset();
    }

    @Test
    public void testExtractConfigDirSeparateValue() {
        assertEquals("/tmp/omegat-conf",
                Main.extractConfigDir(new String[] { "--config-dir", "/tmp/omegat-conf", "start" }));
    }

    @Test
    public void testExtractConfigDirEqualsForm() {
        assertEquals("/tmp/omegat-conf",
                Main.extractConfigDir(new String[] { "start", "--config-dir=/tmp/omegat-conf" }));
    }

    @Test
    public void testExtractConfigDirAbsent() {
        assertNull(Main.extractConfigDir(new String[] { "start", "project" }));
        assertNull(Main.extractConfigDir(new String[] { "--config-dir" }));
        assertNull(Main.extractConfigDir(new String[] { "--config-dir=" }));
        assertNull(Main.extractConfigDir(new String[0]));
        assertNull(Main.extractConfigDir(null));
    }

    /**
     * The reconstructed restart command line must parse with the real command
     * definition, and the top-level options must reach the top-level command.
     */
    @Test
    public void testConstructCommandParamsRoundTrip() {
        RuntimePreferences.setConfigDir("/tmp/omegat-conf");
        RuntimePreferences.setQuietMode(true);
        RuntimePreferences.setNoTeam();
        RuntimePreferences.setAlternateFilenames("draft-*.txt", "final-*.txt");

        CommandLine.ParseResult result = parseReconstructedCommandLine();

        assertEquals("/tmp/omegat-conf", result.matchedOptionValue(LegacyParameters.CONFIG_DIR, null));
        assertTrue(result.hasMatchedOption(LegacyParameters.NO_TEAM));
        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        assertTrue(start.hasMatchedOption(CommonParameters.QUIET));
        assertEquals("draft-*.txt", start.matchedOptionValue(CommonParameters.ALTERNATE_FILENAME_FROM, null));
        assertEquals("final-*.txt", start.matchedOptionValue(CommonParameters.ALTERNATE_FILENAME_TO, null));
    }

    /**
     * A restart must keep the configuration file, the resource bundle, the
     * lock and location-save switches and the tokenizer overrides.
     */
    @Test
    public void testConstructCommandParamsKeepsRuntimeOptions() {
        RuntimePreferences.setConfigFile("/tmp/omegat.properties");
        RuntimePreferences.setResourceBundleFile("/tmp/Bundle_xx.properties");
        RuntimePreferences.setProjectLockingEnabled(false);
        RuntimePreferences.setLocationSaveEnabled(false);
        RuntimePreferences.setTokenizerSource("org.omegat.tokenizer.LuceneEnglishTokenizer");
        RuntimePreferences.setTokenizerTarget("org.omegat.tokenizer.LuceneGermanTokenizer");

        CommandLine.ParseResult result = parseReconstructedCommandLine();

        assertEquals("/tmp/omegat.properties", result.matchedOptionValue(LegacyParameters.CONFIG_FILE, null));
        assertEquals("/tmp/Bundle_xx.properties",
                result.matchedOptionValue(LegacyParameters.RESOURCE_BUNDLE, null));
        assertTrue(result.hasMatchedOption(LegacyParameters.DISABLE_PROJECT_LOCKING));
        assertTrue(result.hasMatchedOption(LegacyParameters.DISABLE_LOCATION_SAVE));
        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        assertEquals("org.omegat.tokenizer.LuceneEnglishTokenizer",
                start.matchedOptionValue(CommonParameters.TOKENIZER_SOURCE, null));
        assertEquals("org.omegat.tokenizer.LuceneGermanTokenizer",
                start.matchedOptionValue(CommonParameters.TOKENIZER_TARGET, null));
    }

    /**
     * The project folder is appended after the reconstructed options and must
     * arrive as the positional parameter of the start sub-command.
     */
    @Test
    public void testConstructCommandParamsProjectAfterOptions() {
        RuntimePreferences.setConfigDir("/tmp/omegat-conf");
        List<String> command = new ArrayList<>();
        Main.constructCommandParams(command);
        command.add("/tmp/project");

        CommandLine.ParseResult result = new CommandLine(new LegacyParameters())
                .parseArgs(command.toArray(new String[0]));

        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        assertEquals("/tmp/project", start.matchedPositionalValue(0, null));
    }

    private CommandLine.ParseResult parseReconstructedCommandLine() {
        List<String> command = new ArrayList<>();
        Main.constructCommandParams(command);
        return new CommandLine(new LegacyParameters()).parseArgs(command.toArray(new String[0]));
    }
}
