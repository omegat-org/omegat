/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre (briacp@gmail.com)
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.omegat.util.gui.ResourcesUtil;

public class OmegaTIcons {

    final public static List<Image> ICONS = new ArrayList<Image>();
    final static String RESOURCES = "/org/omegat/gui/resources/";

    /**
     * Sizes: 16×16, 24×24*, 32×32, 40x40, 48×48, 64×64*, 128×128, 256×256 from
     * http://iconhandbook.co.uk/reference/chart/
     */
    static {
        ICONS.add(ResourcesUtil.getIcon(RESOURCES + "OmegaT_small.gif").getImage());
        ICONS.add(ResourcesUtil.getIcon(RESOURCES + "OmegaT.gif").getImage());
    }

    public static void setIconImages(JFrame frame) {
        frame.setIconImages(ICONS);
    }

}
