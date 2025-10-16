/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import java.util.HashSet;
import java.util.Set;

public final class SubCommands {

    private SubCommands() {
    }

    /**
     * Plugin interface.
     */
    private static final Set<SubCommandEntry> subCommandSet = new HashSet<>();

    /**
     * register command plugin.
     * @param name command string.
     * @param subcommand class of command, it should be runnable with "@Command" annotation.
     */
    public static synchronized void registerConsoleCommand(String name, Class<?> subcommand) {
        subCommandSet.add(new SubCommandEntry(name, subcommand));
    }

    public static Set<SubCommandEntry> getSubCommandSet() {
        return subCommandSet;
    }

    /**
     * Subcommand POJO.
     */
    public static class SubCommandEntry {
        public String name;
        public Class<?> subcommand;

        SubCommandEntry(String name, Class<?> subcommand) {
            this.name = name;
            this.subcommand = subcommand;
        }
    }
}
