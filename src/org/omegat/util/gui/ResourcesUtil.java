/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * Utils for load resources from classpath.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public final class ResourcesUtil {

    private ResourcesUtil() {
    }

    private static final String RESOURCES = "/org/omegat/gui/resources/";

    /**
     * @see <a href="http://iconhandbook.co.uk/reference/chart/">Icon Reference
     *      Chart</a>
     */
    public static final Image APP_ICON_32X32 = getBundledImage("OmegaT.gif");
    public static final Image APP_ICON_16X16 = getBundledImage("OmegaT_small.gif");

    /**
     * Load icon.
     *
     * @param resourceName
     *            resource name
     */
    public static Image getImage(final String resourceName) {
        URL resourceURL = ResourcesUtil.class.getResource(resourceName);
        return Toolkit.getDefaultToolkit().getImage(resourceURL);
    }

    /**
     * Load icon from classpath.
     *
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    public static Image getBundledImage(String imageName) {
        return getImage(RESOURCES + imageName);
    }
}
