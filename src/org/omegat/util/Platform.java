/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2011 Alex Buloichik
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
 * A class to retrieve some platform information.
 * 
 * @author: Zoltan Bartko bartkozoltan@bartkozoltan.com
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class Platform {
    public enum OsType {
        // os.arch=amd64, os.name=Linux, os.version=3.0.0-12-generic
        LINUX64,
        // os.arch=i386, os.name=Linux, os.version=3.0.0-12-generic
        LINUX32,
        // os.arch=x86_64, os.name=Mac OS X, os.version=10.6.8
        MAC64,
        // os.arch=i386, os.name=Mac OS X, os.version=10.6.8
        MAC32,
        // os.arch=amd64, os.name=Windows 7, os.version=6.1
        WIN64,
        // os.arch=x86, os.name=Windows 7, os.version=6.1
        WIN32,
        // unknown system
        OTHER
    }

    private static OsType osType = OsType.OTHER;

    static {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (osName != null && osArch != null) {
            if (osName.startsWith("Linux")) {
                osType = osArch.contains("64") ? OsType.LINUX64 : OsType.LINUX32;
            } else if (osName.startsWith("Mac")) {
                osType = osArch.contains("64") ? OsType.MAC64 : OsType.MAC32;
            } else if (osName.startsWith("Windows")) {
                osType = osArch.contains("64") ? OsType.WIN64 : OsType.WIN32;
            }
        }
    }

    private Platform() {
    }

    public static OsType getOsType() {
        return osType;
    }

    public static final boolean isWebStart() {
        return System.getProperty("javawebstart.version") != null;
    }
}
