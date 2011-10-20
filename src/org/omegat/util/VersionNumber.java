/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parse and compare version numbers.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class VersionNumber implements Comparable<VersionNumber> {

    static final Pattern RE_VERSION = Pattern.compile("([0-9]+)(\\.([0-9]+)(\\.([0-9]+)(\\.([0-9]+))?)?)?");

    protected int v1, v2, v3, v4;

    /**
     * Parse version string like '1.2.3.4', or '1.2.3'.
     */
    public VersionNumber(String version) {
        Matcher m = RE_VERSION.matcher(version);
        if (!m.matches()) {
            throw new IllegalArgumentException("Wrong version number: " + version);
        }
        v1 = Integer.parseInt(m.group(1));
        if (m.group(2) != null) {
            v2 = Integer.parseInt(m.group(3));
        }
        if (m.group(4) != null) {
            v3 = Integer.parseInt(m.group(5));
        }
        if (m.group(6) != null) {
            v4 = Integer.parseInt(m.group(7));
        }
    }

    @Override
    public String toString() {
        return v1 + "." + v2 + "." + v3 + "." + v4;
    }

    public int compareTo(VersionNumber o) {
        if (v1 != o.v1) {
            return v1 < o.v1 ? -1 : 1;
        } else if (v2 != o.v2) {
            return v2 < o.v2 ? -1 : 1;
        } else if (v3 != o.v3) {
            return v3 < o.v3 ? -1 : 1;
        } else if (v4 != o.v4) {
            return v4 < o.v4 ? -1 : 1;
        } else {
            return 0;
        }
    }
}
