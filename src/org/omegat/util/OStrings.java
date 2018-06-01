/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Localizable strings.
 * <p>
 * Please don't add any new strings here, use <code>getString</code> method.
 * This class still has so many strings for legacy reasons only.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public final class OStrings {

    private OStrings() {
    }

    /** For custom deployments of OmegaT that need to be distinguishable from "stock" OmegaT */
    public static final String BRANDING = "";

    /** Just a version, e.g. "1.6" */
    public static final String VERSION;

    /** Update number, e.g. 2, for 1.6.0_02 */
    public static final String UPDATE;

    /** Repository revision number, e.g. r7500 */
    public static final String REVISION;

    /** Indicates whether this is a "beta" (or "latest") version or a "standard" version. */
    public static final boolean IS_BETA;

    static {
        ResourceBundle b = ResourceBundle.getBundle("org/omegat/Version");
        validateVersion(b::getString);
        VERSION = b.getString("version");
        UPDATE = b.getString("update");
        REVISION = b.getString("revision");
        IS_BETA = !b.getString("beta").isEmpty();
    }

    /** Resource bundle that contains all the strings */
    private static ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Bundle");

    /**
     * Returns resource bundle.
     */
    public static ResourceBundle getResourceBundle() {
        return bundle;
    }

    /**
     * Loads resources from the specified file. If the file cannot be loaded,
     * resources are reverted to the default locale. Useful when testing
     * localisations outside the jar file.
     */
    public static void loadBundle(String filename) {
        boolean loaded = false;
        try {
            // Load the resource bundle
            FileInputStream in = new FileInputStream(filename);
            bundle = new PropertyResourceBundle(in);
            loaded = true;
            in.close();
        } catch (FileNotFoundException exception) {
            System.err.println("Resource bundle file not found: " + filename);
        } catch (IOException exception) {
            System.err.println("Error while reading resource bundle file: " + filename);
        }

        // Check if the resource bundle has been successfully
        // loaded, and if not, revert to the default
        if (!loaded) {
            System.err.println("Reverting to resource bundle for the default locale");
            bundle = ResourceBundle.getBundle("org/omegat/Bundle");
        }
    }

    /** Returns a localized String for a key */
    public static String getString(String key) {
        return bundle.getString(key);
    }

    /**
     * Returns a localized string for a key, formatted with the given arguments. Formatting is done by
     * {@link StringUtil#format(String, Object...)}.
     */
    public static String getString(String key, Object... formatArgs) {
        return StringUtil.format(getString(key), formatArgs);
    }

    /**
     * Get the application name for display purposes (includes branding)
     */
    public static String getApplicationDisplayName() {
        String name = bundle.getString("application-name");
        return BRANDING.isEmpty() ? name : name + " " + BRANDING;
    }

    /**
     * Get the raw application name (suitable for file paths, etc.; includes branding)
     */
    public static String getApplicationName() {
        String name = bundle.getString("application-name");
        return BRANDING.isEmpty() ? name : name + "_" + BRANDING;
    }

    /**
     * Get the token for identifying stock vs branded OmegaT files. Intended to
     * be used in filenames, etc. Is the empty string if {@link #BRANDING} is
     * empty.
     */
    public static String getBrandingToken() {
        return BRANDING.isEmpty() ? "" : "-" + BRANDING;
    }

    /**
     * Returns the OmegaT "pretty" version for display (includes the application name).
     * Example: "OmegaT 3.5", "OmegaT 3.5.1_2"
     */
    public static String getDisplayNameAndVersion() {
        if (UPDATE != null && !UPDATE.equals("0")) {
            return StringUtil.format(getString("app-version-template-pretty-update"),
                    getApplicationDisplayName(), VERSION, UPDATE);
        } else {
            return StringUtil.format(getString("app-version-template-pretty"),
                    getApplicationDisplayName(), VERSION);
        }
    }

    /**
     * Returns the OmegaT full version for logs, etc. (includes the application name).
     * Example: "OmegaT-3.5_0_dev", "OmegaT-3.5.1_0_r7532"
     */
    public static String getNameAndVersion() {
        return StringUtil.format(getString("app-version-template"), getApplicationName(),
                VERSION, UPDATE, REVISION);
    }

    /**
     * Returns the OmegaT full version for logs, etc. (does not include the application name).
     * Example: "3.5_0_dev", "3.5.1_0_r7532"
     */
    public static String getVersion() {
        return StringUtil.format(getString("version-template"), VERSION, UPDATE, REVISION);
    }

    /**
     * Returns the OmegaT simple version for displaying to the user for comparison e.g. in a version update message.
     * Does not include the revision because the revision is not guaranteed to be consistent across binaries in a single
     * release. Example: "3.5.0", "3.5.1_1"
     */
    public static String getSimpleVersion() {
        return getSimpleVersion(VERSION, UPDATE);
    }

    public static String getSimpleVersion(String version, String update) {
        if (update != null && !update.equals("0")) {
            return getString("version-template-simple", version, update);
        } else {
            return version;
        }
    }

    /** Returns default text for progress bar when no project is loaded
     *
     */
    public static String getProgressBarDefaultPrecentageText() {
        return StringUtil.format(OStrings.getString("MW_PROGRESS_DEFAULT_PERCENTAGE"),
                "--%", "--", "--%", "--", "--");
    }

    /**
     * Returns the textual marker for the current segment.
     * NOTE: segment marker is assumed to contain "0000" string to overwrite
     * with entry number. If zeros not detected, entry number will not be
     * displayed
     */
    public static String getSegmentMarker() {
        return getString("TF_CUR_SEGMENT_START");
    }

    /**
     * Check to make sure the given Properties contains valid information about an OmegaT version. See
     * Version.properties for more info.
     *
     * @param map
     *            A function that accepts a key and returns a value
     * @throws IllegalArgumentException
     *             If the version info is invalid
     */
    public static void validateVersion(Function<String, String> map) {
        String version = map.apply("version");
        String update = map.apply("update");
        String revision = map.apply("revision");
        String beta = map.apply("beta");
        if (version == null) {
            throw new IllegalArgumentException("Field 'version' must not be null");
        }
        if (update == null) {
            throw new IllegalArgumentException("Field 'update' must not be null");
        }
        if (revision == null) {
            throw new IllegalArgumentException("Field 'revision' must not be null");
        }
        if (beta == null) {
            throw new IllegalArgumentException("Field 'beta' must not be null");
        }
        if (version.split("\\.").length != 3) {
            throw new IllegalArgumentException("Field 'version' must be 3 parts");
        }
        if (!beta.isEmpty() && !"_Beta".equals(beta)) {
            throw new IllegalArgumentException("Field 'beta' must be empty or '_Beta'");
        }
    }
}
