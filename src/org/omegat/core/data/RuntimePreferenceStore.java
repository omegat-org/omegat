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

package org.omegat.core.data;

import org.jetbrains.annotations.VisibleForTesting;

/**
 * Store runtime-only preference values, which shouldn't be saved to config
 * dir.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Hiroshi Miura
 */
public class RuntimePreferenceStore {

    /**
     * Singleton instance.
     */
    protected static volatile RuntimePreferenceStore instance = new RuntimePreferenceStore();

    /**
     * Returns the singleton instance.
     * @return the instance
     */
    public static RuntimePreferenceStore getInstance() {
        return instance;
    }

    /**
     * Set a new instance for the test.
     */
    @VisibleForTesting
    static void setInstance(RuntimePreferenceStore instance) {
        RuntimePreferenceStore.instance = instance;
    }

    @VisibleForTesting
    protected RuntimePreferenceStore() {
    }

    /** Quiet mode. */
    private boolean quietMode;

    /** Force use specified config dir. */
    private String configDir;

    /** Project locking is disabled. */
    private boolean projectLockingDisabled = false;

    /** The last opened project location is not saved **/
    private boolean locationSaveEnabled = true;

    private String alternateFilenameFrom;

    private String alternateFilenameTo;

    private String tokenizerSource;

    private String tokenizerTarget;

    private boolean noTeam = false;

    /**
     * Checks if the application is running in quiet mode.
     *
     * @return {@code true} if quiet mode is enabled, {@code false} otherwise.
     */
    public boolean isQuietMode() {
        return quietMode;
    }

    public void setQuietMode(boolean v) {
        quietMode = v;
    }

    /**
     * Returns the directory path where the configuration is stored.
     *
     * @return the configuration directory as a string
     */
    public String getConfigDir() {
        return configDir;
    }

    /**
     * Override the configuration directory to the specified value.
     *
     * @param v the configuration directory path to set
     */
    public void setConfigDir(String v) {
        configDir = v;
    }

    public boolean isProjectLockingDisabled() {
        return projectLockingDisabled;
    }

    public void setProjectLockingDisabled() {
        projectLockingDisabled = true;
    }

    public boolean isLocationSaveEnabled() {
        return locationSaveEnabled;
    }

    public void setLocationSaveDisable() {
        locationSaveEnabled = false;
    }

    /**
     * Checks if the noTeam flag is enabled.
     *
     * @return {@code true} if noTeam mode is active, {@code false} otherwise
     */
    public boolean isNoTeam() {
        return noTeam;
    }

    /**
     * Enables the noTeam mode by setting the internal {@code noTeam} flag to
     * {@code true}.
     * This flag may be used to indicate that the application is operating
     * without team-related features or configurations.
     */
    public void setNoTeam() {
        noTeam = true;
    }

    /**
     * Sets the tokenizer source override for runtime.
     *
     * @param v the tokenizer source to set
     */
    public void setTokenizerSource(String v) {
        tokenizerSource = v;
    }

    /**
     * Returns the tokenizer source override for runtime.
     *
     * @return the current tokenizer source as a string
     */
    public String getTokenizerSource() {
        return tokenizerSource;
    }

    /**
     * Sets the tokenizer target override for runtime.
     *
     * @param v the tokenizer target to set
     */
    public void setTokenizerTarget(String v) {
        tokenizerTarget = v;
    }

    /**
     * Returns the tokenizer target override for runtime.
     *
     * @return the current tokenizer target as a string
     */
    public String getTokenizerTarget() {
        return tokenizerTarget;
    }

    public String getAlternateFilenameFrom() {
        return alternateFilenameFrom;
    }

    public void setAlternateFilenameFrom(String v) {
        alternateFilenameFrom = v;
    }

    public String getAlternateFilenameTo() {
        return alternateFilenameTo;
    }

    /**
     * Sets the value of the alternate filename.
     *
     * @param v the alternate filename
     */
    public void setAlternateFilenameTo(String v) {
        alternateFilenameTo = v;
    }
}
