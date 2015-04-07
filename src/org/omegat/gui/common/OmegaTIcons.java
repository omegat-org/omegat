/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre (briacp@gmail.com)
               2015 Aaron Madlon-Kay
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
package org.omegat.gui.common;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.omegat.util.gui.ResourcesUtil;

/**
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public class OmegaTIcons {

    final public static List<Image> ICONS = new ArrayList<Image>();
    final static String RESOURCES = "/org/omegat/gui/resources/";

    /**
     * Sizes: 16x16, 24x24*, 32x32, 40x40, 48x48, 64x64*, 128x128, 256x256 from
     * http://iconhandbook.co.uk/reference/chart/
     */
    static {
        try {
            ICONS.add(ResourcesUtil.getImage(RESOURCES + "OmegaT_small.gif"));
            ICONS.add(ResourcesUtil.getImage(RESOURCES + "OmegaT.gif"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setIconImages(JFrame frame) {
        frame.setIconImages(ICONS);
    }

}
