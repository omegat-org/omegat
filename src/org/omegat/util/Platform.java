/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

/**
 * A class to retrieve some platform information. Shamelessly stolen from JNA
 * (https://jna.dev.java.net)
 * 
 * @author: Zoltan Bartko bartkozoltan@bartkozoltan.com
 */
public final class Platform {
    private static final int UNSPECIFIED = -1;
    private static final int MAC = 0;
    private static final int LINUX = 1;
    private static final int WINDOWS = 2;
    private static final int SOLARIS = 3;
    private static final int osType;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            osType = LINUX;
        } else if (osName.startsWith("Mac")) {
            osType = MAC;
        } else if (osName.startsWith("Windows")) {
            osType = WINDOWS;
        } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            osType = SOLARIS;
        } else {
            osType = UNSPECIFIED;
        }
    }

    private Platform() {
    }

    public static final boolean isMac() {
        return osType == MAC;
    }

    public static final boolean isLinux() {
        return osType == LINUX;
    }

    public static final boolean isWindows() {
        return osType == WINDOWS;
    }

    public static final boolean isSolaris() {
        return osType == SOLARIS;
    }

    public static final boolean isX11() {
        // TODO: check FS or do some other X11-specific test
        return !Platform.isWindows() && !Platform.isMac();
    }

    public static final boolean isWebStart() {
        return System.getProperty("javawebstart.version") != null;
    }
}
