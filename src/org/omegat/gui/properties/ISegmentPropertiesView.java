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

package org.omegat.gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.omegat.util.gui.ResourcesUtil;

/**
 * Common interface for UI components used to display key=value properties of the current segment
 *
 * @author Aaron Madlon-Kay
 */
public interface ISegmentPropertiesView {
    Icon SETTINGS_ICON = new ImageIcon(ResourcesUtil.getBundledImage("appbar.settings.active.png"));
    Icon SETTINGS_ICON_INACTIVE = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.inactive.png"));
    Icon SETTINGS_ICON_PRESSED = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.pressed.png"));
    Icon SETTINGS_ICON_INVISIBLE = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return SETTINGS_ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return SETTINGS_ICON.getIconHeight();
        }
    };

    String PROPERTY_TRANSLATION_KEY = "SEGPROP_KEY_";
    Border FOCUS_BORDER = new MatteBorder(1, 1, 1, 1, new Color(0x76AFE8));
    Border MARGIN_BORDER = new EmptyBorder(1, 5, 1, 5);
    Border FOCUS_COMPOUND_BORDER = new CompoundBorder(MARGIN_BORDER, FOCUS_BORDER);

    void update();

    JComponent getViewComponent();

    void notifyUser(List<Integer> notify);

    void install(SegmentPropertiesArea parent);

    String getKeyAtPoint(Point p);
}
