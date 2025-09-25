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

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.RuntimePreferenceStore;

/**
 * Class for store runtime-only preferences, which shouldn't be saved to config
 * dir.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public final class RuntimePreferences {

    private RuntimePreferences() {
    }

    public static boolean isQuietMode() {
        return RuntimePreferenceStore.getInstance().isQuietMode();
    }

    public static void setQuietMode(boolean v) {
        RuntimePreferenceStore.getInstance().setQuietMode(v);
    }

    public static String getConfigDir() {
        return RuntimePreferenceStore.getInstance().getConfigDir();
    }

    public static void setConfigDir(String v) {
        RuntimePreferenceStore.getInstance().setConfigDir(v);
    }

    public static boolean isProjectLockingEnabled() {
        return !RuntimePreferenceStore.getInstance().isProjectLockingDisabled();
    }

    public static void setProjectLockingEnabled(boolean v) {
        if (!v) {
            RuntimePreferenceStore.getInstance().setProjectLockingDisabled();
        }
    }

    public static boolean isLocationSaveEnabled() {
        return RuntimePreferenceStore.getInstance().isLocationSaveEnabled();
    }

    public static void setLocationSaveEnabled(boolean v) {
        if (!v) {
            RuntimePreferenceStore.getInstance().setLocationSaveDisable();
        }
    }

    public static void setAlternateFilenames(String anAlternateFilenameFrom, String anAlternateFilenameTo) {
        if (anAlternateFilenameFrom == null || anAlternateFilenameTo == null) {
            throw new IllegalArgumentException("Both alternateFilenameFrom and alternateFilenameTo are required.");
        }
        RuntimePreferenceStore.getInstance().setAlternateFilenameFrom(anAlternateFilenameFrom);
        RuntimePreferenceStore.getInstance().setAlternateFilenameTo(anAlternateFilenameTo);
    }

    public static String getAlternateFilenameFrom() {
        return RuntimePreferenceStore.getInstance().getAlternateFilenameFrom();
    }

    public static String getAlternateFilenameTo() {
        return RuntimePreferenceStore.getInstance().getAlternateFilenameTo();
    }

    public static void setTokenizerSource(@Nullable String tokenizerSource) {
        RuntimePreferenceStore.getInstance().setTokenizerSource(tokenizerSource);
    }

    public static void setTokenizerTarget(@Nullable String tokenizerTarget) {
        RuntimePreferenceStore.getInstance().setTokenizerTarget(tokenizerTarget);
    }

    public static void setNoTeam() {
        RuntimePreferenceStore.getInstance().setNoTeam();
    }
}
