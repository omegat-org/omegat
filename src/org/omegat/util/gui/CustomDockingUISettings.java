/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.omegat.util.OStrings;

import com.vlsolutions.swing.docking.ui.DockingUISettings;

public class CustomDockingUISettings extends DockingUISettings {

    public CustomDockingUISettings() {
        super();
    }

    @Override
    protected UIDefaults getDefaults(UIDefaults config) {
        // UI strings
        config.put("DockViewTitleBar.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        config.put("DockViewTitleBar.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        config.put("DockViewTitleBar.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        config.put("DockViewTitleBar.attachButtonText", OStrings.getString("DOCKING_HINT_DOCK"));
        config.put("DockViewTitleBar.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        config.put("DockViewTitleBar.closeButtonText", "");
        config.put("DockTabbedPane.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        config.put("DockTabbedPane.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        config.put("DockTabbedPane.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        config.put("DockTabbedPane.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        config.put("DockTabbedPane.closeButtonText", "");

        // Fonts
        config.put("DockViewTitleBar.titleFont", UIManager.getFont("Label.font"));

        config.put("DockViewTitleBar.isCloseButtonDisplayed", false);

        // Disused icons
        config.put("DockViewTitleBar.menu.close", getIcon("empty.gif"));
        config.put("DockTabbedPane.close", getIcon("empty.gif"));
        config.put("DockTabbedPane.close.rollover", getIcon("empty.gif"));
        config.put("DockTabbedPane.close.pressed", getIcon("empty.gif"));
        config.put("DockTabbedPane.menu.close", getIcon("empty.gif"));

        // Panel notification (blinking tabs/headers) settings
        config.put("DockingDesktop.notificationBlinkCount", 2);
        config.put("DockingDesktop.notificationColor",
                Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());

        // to ensure DockViewTitleBar title readability
        Color textColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
        Color backColor = UIManager.getColor("Panel.background");
        // One of these could be null
        if (textColor != null && backColor != null) {
            if (textColor.equals(backColor)) {
                float[] hsb = Color.RGBtoHSB(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                        null);
                float brightness = hsb[2]; // darkest 0.0f <--> 1.0f brightest
                if (brightness >= 0.5f) {
                    brightness -= 0.5f; // to darker
                } else {
                    brightness += 0.5f; // to brighter
                }
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], brightness);
                ColorUIResource res = new ColorUIResource(rgb);
                config.put("InternalFrame.inactiveTitleForeground", res);
            }
        }

        config.put("DockingDesktop.notificationBlinkCount", 2);
        config.put("DockingDesktop.notificationColor",
                Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());

        super.getDefaults(config);

        // Override by null for accelerators to disable
        // This should be placed after `super.getDefaults` call.
        config.put("DockingDesktop.closeActionAccelerator", null);
        config.put("DockingDesktop.maximizeActionAccelerator", null);
        config.put("DockingDesktop.dockActionAccelerator", null);
        config.put("DockingDesktop.floatActionAccelerator", null);

        return config;
    }

    /**
     * Load icon from classpath.
     *
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    private ImageIcon getIcon(String iconName) {
        Image image = ResourcesUtil.getBundledImage(iconName);
        return image == null ? null : new ImageIcon(image);
    }
}
