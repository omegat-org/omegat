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

import java.util.HashMap;
import java.util.Map;

public final class SubCommands {

    private SubCommands() {
    }

    private static final Map<String, Class<?>> SUB_COMMAND_ENTRIES = new HashMap<>();

    /**
     * Registers a console command by associating a command name with its corresponding subcommand class.
     * This method adds the specified command to a collection of subcommands, enabling it to be later
     * utilized or executed through the command-line interface.
     *
     * @param name       the name of the console command to be registered.
     * @param subcommand the class representing the subcommand implementation associated with the given name.
     */
    public static synchronized void registerConsoleCommand(String name, Class<?> subcommand) {
        SUB_COMMAND_ENTRIES.put(name, subcommand);
    }

    /**
     * Registers all subcommand entries stored in the collection to the provided
     * command line object.
     * <p>
     * This method iterates through the predefined set of
     * subcommand entries and dynamically adds them to the given command line
     * instance.
     *
     * @param commandLine the command line object to which the subcommand
     *                    entries will be registered.
     */
    public static void registerSubCommandEntriesToCommandLine(CommandLine commandLine) {
        for (Map.Entry<String, Class<?>> entry : SUB_COMMAND_ENTRIES.entrySet()) {
            commandLine.addSubcommand(entry.getKey(), entry.getValue());
        }
    }
}
