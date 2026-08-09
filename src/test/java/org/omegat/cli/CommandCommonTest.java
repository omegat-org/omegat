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

package org.omegat.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.TestRuntimePreferenceStore;
import org.omegat.util.RuntimePreferences;

import picocli.CommandLine;

/**
 * Tests that the options shared through the CommonParameters mixin reach the
 * runtime preferences.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class CommandCommonTest {

    @Before
    public void setUp() {
        TestRuntimePreferenceStore.resetPristine();
    }

    @After
    public void tearDown() {
        TestRuntimePreferenceStore.reset();
    }

    @Test
    public void testParseCommonParamsAppliesSubCommandOptions() {
        CommandLine.ParseResult result = new CommandLine(new LegacyParameters()).parseArgs("start",
                "--no-project-locking", "--no-location-save", "--no-team", "--ITokenizer",
                "org.omegat.tokenizer.LuceneEnglishTokenizer", "--ITokenizerTarget",
                "org.omegat.tokenizer.LuceneGermanTokenizer");
        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        StartCommand command = (StartCommand) start.commandSpec().userObject();

        CommandCommon.parseCommonParams(command.params);

        assertFalse(RuntimePreferences.isProjectLockingEnabled());
        assertFalse(RuntimePreferences.isLocationSaveEnabled());
        assertTrue(RuntimePreferences.isNoTeam());
        assertEquals("org.omegat.tokenizer.LuceneEnglishTokenizer",
                RuntimePreferences.getTokenizerSource());
        assertEquals("org.omegat.tokenizer.LuceneGermanTokenizer",
                RuntimePreferences.getTokenizerTarget());
    }

    /**
     * The synthetic positive form of the negatable option must keep the
     * default; the original declaration had this inverted.
     */
    @Test
    public void testParseCommonParamsPositiveTeamKeepsDefault() {
        CommandLine.ParseResult result = new CommandLine(new LegacyParameters()).parseArgs("start", "--team");
        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        StartCommand command = (StartCommand) start.commandSpec().userObject();

        CommandCommon.parseCommonParams(command.params);

        assertFalse(RuntimePreferences.isNoTeam());
    }

    @Test
    public void testParseCommonParamsDefaultsLeaveStoreUntouched() {
        CommandLine.ParseResult result = new CommandLine(new LegacyParameters()).parseArgs("start");
        CommandLine.ParseResult start = result.subcommand();
        assertNotNull(start);
        StartCommand command = (StartCommand) start.commandSpec().userObject();

        CommandCommon.parseCommonParams(command.params);

        assertTrue(RuntimePreferences.isProjectLockingEnabled());
        assertTrue(RuntimePreferences.isLocationSaveEnabled());
        assertFalse(RuntimePreferences.isNoTeam());
    }
}
