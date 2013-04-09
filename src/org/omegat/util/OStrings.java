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
public class OStrings {

    private static String __VERSION_KEY = "version";
    private static String __UPDATE_KEY = "update";

    /** Just a version, e.g. "1.6" */
    public static final String VERSION = ResourceBundle.getBundle("org/omegat/Version").getString(
            __VERSION_KEY);

    /** Update number, e.g. 2, for 1.6.0_02 */
    public static final String UPDATE = ResourceBundle.getBundle("org/omegat/Version")
            .getString(__UPDATE_KEY);

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
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
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
     * Returns the OmegaT version for display (includes the application name)
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String getDisplayVersion() {
        return ((UPDATE != null) && !UPDATE.equals("0")) ? StaticUtils.format(
                getString("version-update-template"), new Object[] { VERSION, UPDATE }) : StaticUtils.format(
                getString("version-template"), new Object[] { VERSION, UPDATE });
    }

    /**
     * Returns the textual marker for the current segment.
     * NOTE: segment marker is assumed to contain "0000" string to overwrite
     * with entry number. If zeros not detected, entry number will not be
     * displayed
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String getSegmentMarker() {
        return getString("TF_CUR_SEGMENT_START");
    }

}
