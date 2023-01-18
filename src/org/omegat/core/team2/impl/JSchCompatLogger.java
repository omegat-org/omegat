/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.core.team2.impl;


import java.util.logging.Logger;

import org.omegat.util.Log;

public class JSchCompatLogger implements com.jcraft.jsch.Logger {

    private static final Logger LOGGER = Logger.getLogger("git-ssh");

    private final int loggingLevel;

    public JSchCompatLogger() {
        loggingLevel = WARN;
    }

    @Override
    public boolean isEnabled(final int level) {
        return level >= loggingLevel;
    }


    @Override
    public void log(final int level, final String message) {
        if (level >= loggingLevel) {
            Log.log(message);
        } else {
            Log.logDebug(LOGGER, message);
        }
    }

    @Override
    public void log(final int level, final String message, final Throwable cause) {
        if (level >= loggingLevel) {
            Log.log(message);
            Log.log(cause);
        } else {
            Log.logDebug(LOGGER, message);
            Log.logDebug(LOGGER, cause.getMessage());
        }
    }
}
