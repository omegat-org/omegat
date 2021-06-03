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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;

import com.vlsolutions.swing.docking.AutoHidePolicy;
import com.vlsolutions.swing.docking.AutoHidePolicy.ExpandMode;
import com.vlsolutions.swing.docking.DockableContainerFactory;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import org.omegat.Main;
import org.omegat.gui.theme.IThemeInitializer;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;

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

    private static final Set<String> themeKeySet = new HashSet<>();
    private static final Map<String, IThemeInitializer> themeInitilizer = new HashMap<>();

    private UIDesignManager() {
    }

    public static Map<String, String> getThemes() {
        Map<String, String> themes = new TreeMap<>();
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : lafInfo) {
            String name = lookAndFeelInfo.getName();
            String key = lookAndFeelInfo.toString();
            themes.put(key, name);
        }
        return themes;
    }

    public static void registerTheme(IThemeInitializer initializer) {
        UIManager.LookAndFeelInfo info = new UIManager.LookAndFeelInfo(initializer.getName(), initializer.getClassName());
        UIManager.installLookAndFeel(info);
        themeKeySet.add(info.toString());
        themeInitilizer.put(info.toString(), initializer);
    }

    @SuppressWarnings("unused")
    public static void registerTheme(String name, String className) {
        UIManager.LookAndFeelInfo info = new UIManager.LookAndFeelInfo(name, className);
        UIManager.installLookAndFeel(info);
        themeKeySet.add(info.toString());
    }

    public static Set<String> getThemeKeySet() {
        return themeKeySet;
    }

    public static String getThemeClassName(String key) {
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo info: lafInfo) {
            if (key.equals(info.toString())) {
                return info.getClassName();
            }
        }
        return null;
    }

    public static void setDefaultTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Something went wrong!!
            Log.log(ex);
            Main.showError(ex);
        }
    }

    public static String setTheme(String theme) {
        try {
            UIManager.setLookAndFeel(getThemeClassName(theme));
            return theme;
        } catch (Exception e) {
            Log.log(e);
            setDefaultTheme();
            return Preferences.THEME_DEFAULT;
        }
    }

    /**
     * Initialize docking subsystem.
     */
    public static void initialize(final String theme) throws IOException {
        // load colors defaults
        loadDefaultColors();

        // Install VLDocking defaults
        DockingUISettings.getInstance().installUI();
        DockableContainerFactory.setFactory(new CustomContainerFactory());

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

        IThemeInitializer initializer = themeInitilizer.get(theme);
        if (initializer != null) {
            initializer.setup();
        } else {
            installClassicDesign();
        }

        ensureTitlebarReadability();
    }

    @SuppressWarnings("unused")
    private static void installClassicDesign() {
        UIManager.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, Color.BLACK));

        UIManager.put("DockViewTitleBar.hide", getIcon("minimize.gif"));
        UIManager.put("DockViewTitleBar.hide.rollover", getIcon("minimize.rollover.gif"));
        UIManager.put("DockViewTitleBar.hide.pressed", getIcon("minimize.pressed.gif"));
        UIManager.put("DockViewTitleBar.maximize", getIcon("maximize.gif"));
        UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("maximize.rollover.gif"));
        UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("maximize.pressed.gif"));
        UIManager.put("DockViewTitleBar.restore", getIcon("restore.gif"));
        UIManager.put("DockViewTitleBar.restore.rollover", getIcon("restore.rollover.gif"));
        UIManager.put("DockViewTitleBar.restore.pressed", getIcon("restore.pressed.gif"));
        UIManager.put("DockViewTitleBar.dock", getIcon("restore.gif"));
        UIManager.put("DockViewTitleBar.dock.rollover", getIcon("restore.rollover.gif"));
        UIManager.put("DockViewTitleBar.dock.pressed", getIcon("restore.pressed.gif"));
        UIManager.put("DockViewTitleBar.float", getIcon("undock.gif"));
        UIManager.put("DockViewTitleBar.float.rollover", getIcon("undock.rollover.gif"));
        UIManager.put("DockViewTitleBar.float.pressed", getIcon("undock.pressed.gif"));
        UIManager.put("DockViewTitleBar.attach", getIcon("dock.gif"));
        UIManager.put("DockViewTitleBar.attach.rollover", getIcon("dock.rollover.gif"));
        UIManager.put("DockViewTitleBar.attach.pressed", getIcon("dock.pressed.gif"));

        UIManager.put("DockViewTitleBar.menu.hide", getIcon("minimize.gif"));
        UIManager.put("DockViewTitleBar.menu.maximize", getIcon("maximize.gif"));
        UIManager.put("DockViewTitleBar.menu.restore", getIcon("restore.gif"));
        UIManager.put("DockViewTitleBar.menu.dock", getIcon("restore.gif"));
        UIManager.put("DockViewTitleBar.menu.float", getIcon("undock.gif"));
        UIManager.put("DockViewTitleBar.menu.attach", getIcon("dock.gif"));

        UIManager.put("DockTabbedPane.menu.hide", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.maximize", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.float", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif"));

        UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage());
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

    /**
     * Heuristic detection of dark theme.
     * <p>
     *     isDarkTheme method derived from NetBeans licensed by Apache-2.0
     * @return true when dark theme, otherwise false.
     */
    private static boolean isDarkTheme() {
        // Based on tests with different LAFs and color combinations, a light
        // theme can be reliably detected by observing the brightness value of
        // the HSB Values of Table.background and Table.foreground
        //
        // Results from the test (Theme / Foreground / Background)
        // Gtk - Numix (light) / 0.2 / 0.97
        // Gtk - BlackMATE (dark) / 1.0 / 0.24
        // Gtk - Clearlooks (light) / 0.1 / 1.0
        // Gtk - ContrastHighInverse (dark) / 1.0 / 0.0
        // Gtk - DustSand (light) / 0.19 / 1.0
        // Gtk - TraditionalOkTest (light) / 0.0 / 0.74
        // Gtk - Menta (light) / 0.17 / 0.96
        // DarkNimbus (dark) / 0.9 / 0.19
        // DarkMetal (dark) / 0.87 / 0.19
        // CDE (light) / 0.0 / 0.76
        // Nimbus (light) / 0.0 / 1.0
        // Metall (light) / 0.2 / 1.0
        // Windows (light) / 0.0 / 1.0
        // Windows Classic (light) / 0.0 / 1.0
        // Windows HighContrast Black (dark) / 1.0 / 0
        Color foreground = UIManager.getColor("Table.foreground");
        Color background = UIManager.getColor("Table.background");
        float foreground_brightness = Color.RGBtoHSB(
                foreground.getRed(),
                foreground.getGreen(),
                foreground.getBlue(),
                null)[2];
        float background_brightness = Color.RGBtoHSB(
                background.getRed(),
                background.getGreen(),
                background.getBlue(),
                null)[2];
        return background_brightness < foreground_brightness;
    }

    private static void loadColors(final String scheme) throws IOException {
        ResourcesUtil.getBundleColorProperties(scheme).forEach((k, v) -> {
            if (v.toString().charAt(0) != '#') {
                throw new RuntimeException("Invalid color value for key " + k + ": " + v);
            }
            try {
                String hex = v.toString().substring(1);
                Color color;
                if (hex.length() <= 6) {
                    color = new Color(Integer.parseInt(hex, 16)); // int(rgb)
                } else {
                    long val = Long.parseLong(hex, 16);
                    int a = (int) (val & 0xFF);
                    int b = (int) (val >> 8 & 0xFF);
                    int g = (int) (val >> 16 & 0xFF);
                    int r = (int) (val >> 24 & 0xFF);
                    color = new Color(r, g, b, a); // hasAlpha
                }
                UIManager.put(k.toString(), color);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Invalid color value for key '" + k + "': " + v, ex);
            }
        });
    }

    /**
     * Load application default colors
     */
    private static void loadDefaultColors() throws IOException {
        Color hilite;
        if (isDarkTheme()) {
            loadColors("dark");
            hilite = UIManager.getColor("TextArea.background").brighter();  // NOI18N
            // Hack for JDK GTKLookAndFeel bug.
            // TextPane.background is always white but should be a text_background of GTK.
            // List.background is as same color as text_background.
            if (Platform.isLinux() && Color.WHITE.equals(UIManager.getColor("TextPane.background"))) {
                UIManager.put("TextPane.background", UIManager.getColor("List.background"));
            }
        } else {
            loadColors("light");
            Color bg = UIManager.getColor("TextArea.background").darker();  // NOI18N
            hilite = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 32);
        }
        UIManager.put("OmegaT.alternatingHilite", hilite);
    }

}
