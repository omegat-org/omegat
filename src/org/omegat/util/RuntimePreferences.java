/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

/**
 * Class for store runtime-only preferences, which shouldn't be saved to config
 * dir.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public final class RuntimePreferences {

    private RuntimePreferences() {
    }

    /** Quiet mode. */
    private static boolean quietMode;

    /** Force use specified config dir. */
    private static String configDir;

    private static boolean projectLockingEnabled = true;

    /** Last opened project location save control **/
    private static boolean locationSaveEnabled = true;

    public static boolean isQuietMode() {
        return quietMode;
    }

    public static void setQuietMode(boolean v) {
        quietMode = v;
    }

    public static String getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(String v) {
        configDir = v;
    }

    public static boolean isProjectLockingEnabled() {
        return projectLockingEnabled;
    }

    public static void setProjectLockingEnabled(boolean v) {
        projectLockingEnabled = v;
    }

    public static boolean isLocationSaveEnabled() {
        return locationSaveEnabled;
    }

    public static void setLocationSaveEnabled(boolean v) {
        locationSaveEnabled = v;
    }
}
