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

import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Base class for subcommands.
 *
 * @author Hiroshi Miura
 */
public abstract class BaseSubCommand implements Callable<Integer> {

    private @Nullable Map<String, String> params;
    private @Nullable List<String> args;

    public void setParameters(Map<String, String> params) {
        this.params = params;
    }

    public void setArguments(List<String> args) {
        this.args = args;
    }

    @SuppressWarnings("unused")
    protected @Nullable String getArg(int index) {
        return args != null && index < args.size() ? args.get(index) : null;
    }

    @SuppressWarnings("unused")
    protected boolean hasParam(String key) {
        return params != null && params.containsKey(key);
    }

    protected @Nullable String getParam(String key) {
        if (params == null) {
            return null;
        }
        return params.get(key);
    }

    /**
     * Returns true if the subcommand requires a project to be loaded.
     *
     * @return true if project is required, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean isProjectRequired() {
        return false;
    }
}
