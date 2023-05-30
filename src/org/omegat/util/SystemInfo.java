/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util;

import java.util.Locale;
import java.util.StringTokenizer;

public class SystemInfo {
    // platforms
    public static final boolean isWindows;
    public static final boolean isMacOS;
    public static final boolean isLinux;

    // OS versions
    public static final long osVersion;
    public static final boolean isWindows_10_orLater;
    public static final boolean isWindows_11_orLater;
    public static final boolean isMacOS_10_11_ElCapitan_orLater;
    public static final boolean isMacOS_10_14_Mojave_orLater;
    public static final boolean isMacOS_10_15_Catalina_orLater;

    // OS architecture
    public static final boolean isX86;
    public static final boolean isX86_64;
    public static final boolean isAARCH64;

    // Java versions
    public static final long javaVersion;
    public static final boolean isJava_9_orLater;
    public static final boolean isJava_11_orLater;
    public static final boolean isJava_12_orLater;
    public static final boolean isJava_15_orLater;
    public static final boolean isJava_17_orLater;
    public static final boolean isJava_18_orLater;

    // Java VMs
    public static final boolean isJetBrainsJVM;
    public static final boolean isJetBrainsJVM_11_orLater;

    // UI toolkits
    public static final boolean isKDE;

    // features
    public static final boolean isMacFullWindowContentSupported;

    static {
        // platforms
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        isWindows = osName.startsWith("windows");
        isMacOS = osName.startsWith("mac");
        isLinux = osName.startsWith("linux");

        // OS versions
        osVersion = scanVersion(System.getProperty("os.version"));
        isWindows_10_orLater = (isWindows && osVersion >= toVersion(10, 0, 0, 0));
        isMacOS_10_11_ElCapitan_orLater = (isMacOS && osVersion >= toVersion(10, 11, 0, 0));
        isMacOS_10_14_Mojave_orLater = (isMacOS && osVersion >= toVersion(10, 14, 0, 0));
        isMacOS_10_15_Catalina_orLater = (isMacOS && osVersion >= toVersion(10, 15, 0, 0));

        // OS architecture
        String osArch = System.getProperty("os.arch");
        isX86 = osArch.equals("x86");
        isX86_64 = osArch.equals("amd64") || osArch.equals("x86_64");
        isAARCH64 = osArch.equals("aarch64");

        // Java versions
        javaVersion = scanVersion(System.getProperty("java.version"));
        isJava_9_orLater = (javaVersion >= toVersion(9, 0, 0, 0));
        isJava_11_orLater = (javaVersion >= toVersion(11, 0, 0, 0));
        isJava_12_orLater = (javaVersion >= toVersion(12, 0, 0, 0));
        isJava_15_orLater = (javaVersion >= toVersion(15, 0, 0, 0));
        isJava_17_orLater = (javaVersion >= toVersion(17, 0, 0, 0));
        isJava_18_orLater = (javaVersion >= toVersion(18, 0, 0, 0));

        // Java VMs
        isJetBrainsJVM = System.getProperty("java.vm.vendor", "Unknown").toLowerCase(Locale.ENGLISH)
                .contains("jetbrains");
        isJetBrainsJVM_11_orLater = isJetBrainsJVM && isJava_11_orLater;

        // UI toolkits
        isKDE = (isLinux && System.getenv("KDE_FULL_SESSION") != null);

        // features
        // available since Java 12; backported to Java 11.0.8 and 8u292
        isMacFullWindowContentSupported = isMacOS && (javaVersion >= toVersion(11, 0, 8, 0)
                || (javaVersion >= toVersion(1, 8, 0, 292) && !isJava_9_orLater));

        // Note: Keep following at the end of this block because (optional)
        // loading of a native library uses fields of this class. E.g. isX86_64

        // Windows 11 detection is implemented in Java 8u321, 11.0.14, 17.0.2
        // and 18 (or later).
        // (see https://bugs.openjdk.java.net/browse/JDK-8274840)
        // For older Java versions, need to use a native library to get OS build number.
        // Now no place to detect Win11, so we skip a use of a native library.
        boolean isWin_11_orLater = false;
        try {
            isWin_11_orLater = isWindows_10_orLater && scanWindowsVersion(osName) >= toVersion(11, 0, 0, 0);
        } catch (Throwable ex) {
            // catch to avoid that application can not start if a native library
            // is not up-to-date
            Log.logErrorRB(ex, null);
        }
        isWindows_11_orLater = isWin_11_orLater;
    }

    private static long scanWindowsVersion(String osName) {
        final String leading = "windows ";
        return scanVersion(osName.startsWith(leading) ? osName.substring(leading.length()) : osName);
    }

    private static long scanVersion(String version) {
        int major = 1;
        int minor = 0;
        int micro = 0;
        int patch = 0;
        try {
            StringTokenizer st = new StringTokenizer(version, "._-+");
            major = Integer.parseInt(st.nextToken());
            minor = Integer.parseInt(st.nextToken());
            micro = Integer.parseInt(st.nextToken());
            patch = Integer.parseInt(st.nextToken());
        } catch (Exception ex) {
            // ignore
        }

        return toVersion(major, minor, micro, patch);
    }

    private static long toVersion(int major, int minor, int micro, int patch) {
        return ((long) major << 48) + ((long) minor << 32) + ((long) micro << 16) + patch;
    }
}
