/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

/**
 * An initializer for ensuring that tests can't pollute (or be polluted by)
 * actual user preferences.
 *
 * @author Aaron Madlon-Kay
 *
 */
public final class TestPreferencesInitializer {

    private TestPreferencesInitializer() {
    }

    /**
     * Init the preferences system using a temp dir for the config dir where
     * prefs files are read and written. Convenience method for
     * {@link #init(String)}.
     *
     * @throws IOException
     */
    public static void init() throws IOException {
        Path tmp = Files.createTempDirectory("omegat");
        init(tmp.toString());
        FileUtils.forceDeleteOnExit(tmp.toFile());
    }

    /**
     * Init the preferences system using the supplied path as the config dir
     * where prefs files are read and written.
     *
     * @param configDir
     */
    public static synchronized void init(String configDir) {
        RuntimePreferences.setConfigDir(configDir);
        Preferences.init();
        Preferences.initFilters();
        Preferences.initSegmentation();
    }
}
