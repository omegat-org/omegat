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
package org.omegat.gui.scripting;

import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import java.util.ResourceBundle;

public final class ScriptingUtils {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.scripting.Bundle");
    private static final ILogger LOGGER = LoggerFactory.getLogger(ScriptingWindow.class, BUNDLE);

    private ScriptingUtils() {
    }
    
    public static String getBundleString(String key) {
        return BUNDLE.getString(key);
    }
    
    public static ILogger getLogger() {
        return LOGGER;
    }
}
