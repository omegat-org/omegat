/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.issues;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * A simple, square icon of a single, solid color.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class SimpleColorIcon implements Icon {

    private final Color color;
    static final int ICON_DIMENSION = 16;

    public SimpleColorIcon(Color color) {
        this.color = color;
    }

    public SimpleColorIcon(String color) {
        this.color = Color.decode(color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, ICON_DIMENSION, ICON_DIMENSION);
    }

    @Override
    public int getIconWidth() {
        return ICON_DIMENSION;
    }

    @Override
    public int getIconHeight() {
        return ICON_DIMENSION;
    }
}
