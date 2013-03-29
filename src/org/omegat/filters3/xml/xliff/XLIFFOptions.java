/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007-2013 Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3.xml.xliff;

import java.util.Map;

import org.omegat.filters2.AbstractOptions;

/**
 * Options for the XLIFF filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * OpenDoc filter have the following options ([+] means default on).
 * Translatable elements:
 * <ul>
 * <li>[] Compatibility with 2.6
 * </ul>
 * 
 * @author Didier Briel
 */
public class XLIFFOptions extends AbstractOptions {
    private static final String OPTION_26_COMPATIBILITY = "compatibility26";

    public XLIFFOptions(Map<String, String> config) {
        super(config);
    }

    /**
     * Returns whether 2.6 compatibility should be applied
     */
    public boolean get26Compatibility() {
        return getBoolean(OPTION_26_COMPATIBILITY, false);
    }

    /**
     * Sets whether 2.6 compatibility should be applied.
     */
    public void set26Compatibility(boolean compatibility26) {
        setBoolean(OPTION_26_COMPATIBILITY, compatibility26);
    }

   
}
