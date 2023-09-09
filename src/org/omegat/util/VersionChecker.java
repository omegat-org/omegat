/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for checking for updated versions of OmegaT.
 *
 * @author Aaron Madlon-Kay
 */
public final class VersionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionChecker.class);

    // When HTTPS is enabled SourceForge at one point used strong encryption
    // that can't be used with standard JRE restrictions:
    // https://sourceforge.net/p/forge/site-support/14321/
    //
    // - Prior to Java 8u151 you had to install the JCE Unlimited Strength
    // Jurisdiction Policy Files:
    // http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
    //
    // - As of Java 8u151 you can set `crypto.policy=unlimited` in
    // java.security, however this
    // is still too cumbersome for general use
    //
    // - As of Java 8u162, unlimited crypto is the default
    //
    // - As of 2018-11-8, everything seems to work fine out-of-the-box as far
    // - back as Java 8u141 (other versions not tested)
    private static final String VERSION_FILE = "https://omegat.sourceforge.io/Version-"
            + (OStrings.IS_BETA ? "latest" : "standard") + ".properties";

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

    private void fetch(boolean force) throws Exception {
        if (!force && !shouldFetch()) {
            return;
        }
        LOGGER.atDebug().log("Fetching latest version info");
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
        LOGGER.atDebug().log("Fetched latest version info: {}", props);
    }

    private int compareVersions(boolean force) throws Exception {
        fetch(force);
        // Don't compare REVISION because it is not always consistent across
        // release binaries
        return compareVersions(OStrings.VERSION, OStrings.UPDATE, mProps.getProperty("version"),
                mProps.getProperty("update"));
    }

    /**
     * Check if OmegaT is up to date.
     *
     * @param force
     *            If true, fetch the latest data from the server even if the
     *            current data is not stale yet
     * @throws Exception
     */
    public boolean isUpToDate(boolean force) throws Exception {
        boolean result = compareVersions(force) >= 0;
        if (result) {
            LOGGER.atDebug().log("OmegaT is up to date");
        } else {
            LOGGER.atDebug().setMessage("A newer version of OmegaT is available: {}")
                    .addArgument(this::getRemoteVersion).log();
        }
        return result;
    }

    public String getRemoteVersion() {
        try {
            fetch(false);
        } catch (Exception ignored) {
        }
        return OStrings.getSimpleVersion(mProps.getProperty("version"), mProps.getProperty("update"));
    }

    /**
     * Compares OmegaT version numbers.
     * 
     * @param version1
     *            e.g. "5.4.0"
     * @param update1
     *            e.g. "0"
     * @param version2
     *            e.g. "3.0.6"
     * @param update2
     *            "1"
     * @return value 0 if version1 update1 == version2 update2; value less than
     *         0 if version1 update1 &lt; version2 update2; and value greater
     *         than 0 if version1 update1 &gt; version2 update2
     */
    public static int compareVersions(String version1, String update1, String version2, String update2) {
        return compare(getVersionNumbers(version1, update1), getVersionNumbers(version2, update2));
    }

    public static int compareMinorVersions(String version1, String version2) {
        List<Integer> l1 = getVersionNumbers(version1, "0");
        List<Integer> l2 = getVersionNumbers(version2, "0");
        return compare(l1.subList(0, Math.min(l1.size(), 2)), l2.subList(0, Math.min(l2.size(), 2)));
    }

    private static List<Integer> getVersionNumbers(String version, String update) {
        List<Integer> result = new ArrayList<>();
        if (version == null) {
            return result;
        }
        for (String n : version.split("\\.")) {
            result.add(Integer.parseInt(n));
        }
        result.add(Integer.parseInt(update));
        // Don't include REVISION because it is not always consistent across
        // release binaries
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
