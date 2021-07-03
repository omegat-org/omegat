/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009-2010 Alex Buloichik
               2014 Yu Tang
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

import com.vlsolutions.swing.docking.AutoHidePolicy;
import com.vlsolutions.swing.docking.AutoHidePolicy.ExpandMode;
import com.vlsolutions.swing.docking.DockableContainerFactory;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

/**
 * UI Design Manager.
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Benjamin Siband
 * @author Kim Bruning
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public final class UIDesignManager {

    private UIDesignManager() {
    }

    public static void setTheme(String lafClassName, ClassLoader classLoader) {
        try {
            Class<?> clazz = classLoader.loadClass(lafClassName);
            UIManager.setLookAndFeel((LookAndFeel) clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            Log.log(e);
            if (!lafClassName.equals(Preferences.THEME_CLASS_NAME_DEFAULT)) {
                setTheme(Preferences.THEME_CLASS_NAME_DEFAULT, classLoader);
            }
        }
    }

    /**
     * Initialize docking subsystem.
     */
    public static void initialize(ClassLoader mainClassLoader) throws IOException {
        // Install VLDocking defaults
        DockingUISettings.getInstance().installUI();
        DockableContainerFactory.setFactory(new CustomContainerFactory());

        // Set Look And Feel
        String theme = Preferences.getPreferenceDefault(Preferences.THEME_CLASS_NAME, Preferences.THEME_CLASS_NAME_DEFAULT);
        UIDesignManager.setTheme(theme, mainClassLoader);

        // Enable animated popup when mousing over minimized tab
        AutoHidePolicy.getPolicy().setExpandMode(ExpandMode.EXPAND_ON_ROLLOVER);

        // UI strings
        UIManager.put("DockViewTitleBar.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        UIManager.put("DockViewTitleBar.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        UIManager.put("DockViewTitleBar.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        UIManager.put("DockViewTitleBar.attachButtonText", OStrings.getString("DOCKING_HINT_DOCK"));
        UIManager.put("DockViewTitleBar.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        UIManager.put("DockViewTitleBar.closeButtonText", "");
        UIManager.put("DockTabbedPane.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        UIManager.put("DockTabbedPane.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        UIManager.put("DockTabbedPane.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        UIManager.put("DockTabbedPane.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        UIManager.put("DockTabbedPane.closeButtonText", "");

        // Fonts
        Font defaultFont = UIManager.getFont("Label.font");
        UIManager.put("DockViewTitleBar.titleFont", defaultFont);
        UIManager.put("JTabbedPaneSmartIcon.font", defaultFont);
        UIManager.put("AutoHideButton.font", defaultFont);

        // UI settings
        UIManager.put("DockViewTitleBar.isCloseButtonDisplayed", false);
        UIManager.put("DockingDesktop.closeActionAccelerator", null);
        UIManager.put("DockingDesktop.maximizeActionAccelerator", null);
        UIManager.put("DockingDesktop.dockActionAccelerator", null);
        UIManager.put("DockingDesktop.floatActionAccelerator", null);

        // Disused icons
        UIManager.put("DockViewTitleBar.menu.close", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close.rollover", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close.pressed", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.close", getIcon("empty.gif"));

        // Panel notification (blinking tabs/headers) settings
        UIManager.put("DockingDesktop.notificationBlinkCount", 2);
        UIManager.put("DockingDesktop.notificationColor", Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());

        ensureTitlebarReadability();
    }

    private static void ensureTitlebarReadability() {
        // to ensure DockViewTitleBar title readability
        Color textColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
        Color backColor = UIManager.getColor("Panel.background");
        if (textColor != null && backColor != null) { // One of these could be null
            if (textColor.equals(backColor)) {
                float[] hsb = Color.RGBtoHSB(textColor.getRed(),
                        textColor.getGreen(), textColor.getBlue(), null);
                float brightness = hsb[2]; // darkest 0.0f <--> 1.0f brightest
                if (brightness >= 0.5f) {
                    brightness -= 0.5f; // to darker
                } else {
                    brightness += 0.5f; // to brighter
                }
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], brightness);
                ColorUIResource res = new ColorUIResource(rgb);
                UIManager.put("InternalFrame.inactiveTitleForeground", res);
            }
        }

        UIManager.put("DockingDesktop.notificationBlinkCount", 2);
        UIManager.put("DockingDesktop.notificationColor", Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());
    }

    /**
     * Load icon from classpath.
     *
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    private static ImageIcon getIcon(String iconName) {
        Image image = ResourcesUtil.getBundledImage(iconName);
        return image == null ? null : new ImageIcon(image);
    }

    /**
     * Removes first, last and duplicate separators from menu.
     */
    public static void removeUnusedMenuSeparators(final JPopupMenu menu) {
        if (menu.getComponentCount() > 0 && menu.getComponent(0) instanceof JSeparator) {
            // remove first separator
            menu.remove(0);
        }
        if (menu.getComponentCount() > 0
                && menu.getComponent(menu.getComponentCount() - 1) instanceof JSeparator) {
            // remove last separator
            menu.remove(menu.getComponentCount() - 1);
        }
        for (int i = 0; i < menu.getComponentCount() - 1; i++) {
            if (menu.getComponent(i) instanceof JSeparator && menu.getComponent(i + 1) instanceof JSeparator) {
                // remove duplicate separators
                menu.remove(i);
            }
        }
    }

    /**
     * Ensure that any "closed" Dockables are made visible.
     */
    public static void ensureDockablesVisible(DockingDesktop desktop) {
        for (DockableState state : desktop.getDockables()) {
            if (state.isClosed()) {
                // VLDocking says this is how you re-show a closed Dockable,
                // but it prints a stack trace. So just ignore it?
                desktop.addDockable(state.getDockable());
            }
        }
    }

    /**
     * Traverse the given container's parents until either an instance of
     * DockingDesktop is found, or null is found.
     *
     * @param c
     *            The container to search
     * @return Either the parent DockingDesktop, or null
     */
    public static DockingDesktop getDesktop(Container c) {
        while (c != null && !(c instanceof DockingDesktop)) {
            c = c.getParent(); // find dockable desktop
        }
        return (DockingDesktop) c;
    }

}
