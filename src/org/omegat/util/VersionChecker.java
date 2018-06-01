/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for checking for updated versions of OmegaT.
 *
 * @author Aaron Madlon-Kay
 */
public final class VersionChecker {

    private static final Logger LOGGER = Logger.getLogger(VersionChecker.class.getName());

    // We would prefer to host this on SourceForge, but when HTTPS is enabled SourceForge
    // uses strong encryption that can't be used with standard JRE restrictions:
    // https://sourceforge.net/p/forge/site-support/14321/
    //
    // - Prior to Java 8u151 you had to install the JCE Unlimited Strength Jurisdiction Policy Files:
    // http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
    //
    // - As of Java 8u151 you can set `crypto.policy=unlimited` in java.security, however this
    // is still too cumbersome for general use
    //
    // - As of Java 8u162, unlimited crypto is the default. However switching SourceForge to
    // HTTPS would break WebStart for the "standard" distro (currently on Java 6), so we must wait
    // until all distros are on Java 8+.
    private static final String VERSION_FILE = "https://omegat.ci.cloudbees.com/job/omegat-publish-release-version-"
            + (OStrings.IS_BETA ? "latest" : "standard") + "/lastSuccessfulBuild/artifact/Version.properties";

    private static final int FETCH_INTERVAL = 60 * 5 * 1000;

    private static class SingletonHelper {
        private static final VersionChecker INSTANCE = new VersionChecker();
    }

    public static VersionChecker getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private VersionChecker() {
    }

    private Properties mProps = new Properties();
    private long lastFetched = -1L;

    private boolean shouldFetch() {
        if (mProps.isEmpty()) {
            return true;
        }
        return System.currentTimeMillis() - lastFetched >= FETCH_INTERVAL;
    }

    private void fetch() throws Exception {
        if (!shouldFetch()) {
            return;
        }
        LOGGER.fine("Fetching latest version info");
        Properties props = new Properties();
        try (InputStream in = new URL(VERSION_FILE).openStream()) {
            props.load(in);
        }
        try {
            OStrings.validateVersion(props::getProperty);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Fetched version info was invalid: " + props, e);
        }
        mProps = props;
        lastFetched = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Fetched latest version info: {0}", props);
    }

    public int compareVersions() throws Exception {
        fetch();
        // Don't compare REVISION because it is not always consistent across release binaries
        return compareVersions(OStrings.VERSION, OStrings.UPDATE, mProps.getProperty("version"),
                mProps.getProperty("update"));
    }

    public boolean isUpToDate() throws Exception {
        boolean result = compareVersions() >= 0;
        if (result) {
            LOGGER.fine("OmegaT is up to date");
        } else {
            LOGGER.log(Level.FINE, "A newer version of OmegaT is available: {0}", getRemoteVersion());
        }
        return result;
    }

    public String getRemoteVersion() throws Exception {
        fetch();
        return OStrings.getSimpleVersion(mProps.getProperty("version"), mProps.getProperty("update"));
    }

    public static int compareVersions(String version1, String update1, String version2, String update2) {
        return compare(getVersionNumbers(version1, update1), getVersionNumbers(version2, update2));
    }

    private static List<Integer> getVersionNumbers(String version, String update) {
        List<Integer> result = new ArrayList<>();
        for (String n : version.split("\\.")) {
            result.add(Integer.parseInt(n));
        }
        result.add(Integer.parseInt(update));
        // Don't include REVISION because it is not always consistent across release binaries
        return result;
    }

    private static int compare(List<Integer> l1, List<Integer> l2) {
        if (l1.size() != l2.size()) {
            throw new IllegalArgumentException("Lists must be same size");
        }
        for (int i = 0; i < l1.size(); i++) {
            int c = Integer.compare(l1.get(i), l2.get(i));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }
}
