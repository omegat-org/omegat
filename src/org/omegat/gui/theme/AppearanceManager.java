/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 miurahr.
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.gui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.github.weisj.darklaf.platform.preferences.SystemPreferencesManager;
import com.github.weisj.darklaf.theme.spec.ColorToneRule;
import com.github.weisj.darklaf.theme.spec.ContrastRule;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.gui.CustomContainerFactory;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.Styles;

import com.vlsolutions.swing.docking.AutoHidePolicy;
import com.vlsolutions.swing.docking.DockableContainerFactory;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

public final class AppearanceManager {

    private AppearanceManager() {
    }

    private static final SystemPreferencesManager systemPreferencesManager = new SystemPreferencesManager(
            true);

    /**
     * Initialize docking subsystem.
     */
    public static void initialize(ClassLoader mainClassLoader) throws IOException {
        // Install VLDocking defaults
        DockingUISettings.getInstance().installUI();
        DockableContainerFactory.setFactory(new CustomContainerFactory());

        // Set Look And Feel
        String theme = Preferences.getPreferenceDefault(Preferences.THEME_CLASS_NAME,
                Preferences.THEME_CLASS_NAME_DEFAULT);
        AppearanceManager.setTheme(theme, mainClassLoader);

        if (UIManager.getColor("OmegaT.source") == null) {
            // Theme apparently did not load default colors so we do so now
            AppearanceManager.loadDefaultColors(UIManager.getDefaults());
        }

        // Enable animated popup when mousing over minimized tab
        AutoHidePolicy.getPolicy().setExpandMode(AutoHidePolicy.ExpandMode.EXPAND_ON_ROLLOVER);

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
        UIManager.put("DockingDesktop.notificationColor",
                Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());

        ensureTitlebarReadability();
    }

    private static void ensureTitlebarReadability() {
        // to ensure DockViewTitleBar title readability
        Color textColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
        Color backColor = UIManager.getColor("Panel.background");
        if (textColor != null && backColor != null) { // One of these could be
                                                      // null
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
                UIManager.put("InternalFrame.inactiveTitleForeground", res);
            }
        }

        UIManager.put("DockingDesktop.notificationBlinkCount", 2);
        UIManager.put("DockingDesktop.notificationColor",
                Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());
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

    public static boolean isDarkPreference(UIDefaults uiDefaults) {
        ColorToneRule colorToneRule = systemPreferencesManager.provider().getPreference().getColorToneRule();
        return colorToneRule.equals(ColorToneRule.DARK);
    }

    public static boolean isHighlight() {
        ContrastRule contrastRule = systemPreferencesManager.provider().getPreference().getContrastRule();
        return contrastRule.equals(ContrastRule.HIGH_CONTRAST);
    }

    /**
     * Heuristic detection of dark theme.
     * <p>
     * isDarkTheme method derived from NetBeans licensed by Apache-2.0
     * 
     * @return true when dark theme, otherwise false.
     */
    static boolean isDarkTheme(UIDefaults uiDefaults) {
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
        Color foreground = uiDefaults.getColor("Table.foreground");
        Color background = uiDefaults.getColor("Table.background");
        float foreground_brightness = Color.RGBtoHSB(foreground.getRed(), foreground.getGreen(),
                foreground.getBlue(), null)[2];
        float background_brightness = Color.RGBtoHSB(background.getRed(), background.getGreen(),
                background.getBlue(), null)[2];
        return background_brightness < foreground_brightness;
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

    private static void loadColors(UIDefaults defaults, String k, String v) {
        if (v.charAt(0) != '#') {
            throw new RuntimeException("Invalid color value for key " + k + ": " + v);
        }
        try {
            String hex = v.substring(1);
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
            defaults.put(k, color);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid color value for key '" + k + "': " + v, ex);
        }
    }

    // Windows Classic LAF detection from
    // http://stackoverflow.com/a/4386821/448068
    static boolean isWindowsLAF(String systemID) {
        return systemID.equals("Windows");
    }

    static boolean isWindowsClassicLAF(String systemID) {
        return isWindowsLAF(systemID)
                && !(Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive");
    }

    /**
     * Load application default colors
     */
    static void loadDefaultColors(UIDefaults uiDefaults) throws IOException {
        if (isDarkTheme(uiDefaults)) {
            loadDefaultAppDarkColors(uiDefaults);
        } else {
            loadDefaultAppLightColors(uiDefaults);
        }
    }

    public static void loadDefaultAppDarkColors(UIDefaults uiDefaults) throws IOException {
        ResourcesUtil.getBundleColorProperties("dark")
                .forEach((k, v) -> loadColors(uiDefaults, k.toString(), v.toString()));
        Color hilite = uiDefaults.getColor("TextArea.background").brighter(); // NOI18N
        // Hack for JDK GTKLookAndFeel bug.
        // TextPane.background is always white but should be a text_background
        // of GTK.
        // List.background is as same color as text_background.
        if (Platform.isLinux() && Color.WHITE.equals(uiDefaults.getColor("TextPane.background"))) {
            uiDefaults.put("TextPane.background", uiDefaults.getColor("List.background"));
        }
        uiDefaults.put("OmegaT.alternatingHilite", hilite);
    }

    public static void loadDefaultSystemDarkColors(UIDefaults uiDefaults) throws IOException {
        ResourcesUtil.getBundleSystemColorProperties("dark")
                .forEach((k, v) -> loadColors(uiDefaults, k.toString(), v.toString()));
    }

    public static void loadDefaultAppLightColors(UIDefaults uiDefaults) throws IOException {
        ResourcesUtil.getBundleColorProperties("light")
                .forEach((k, v) -> loadColors(uiDefaults, k.toString(), v.toString()));
        Color bg = uiDefaults.getColor("TextArea.background").darker(); // NOI18N
        Color hilite = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 32);
        uiDefaults.put("OmegaT.alternatingHilite", hilite);
    }

    // This check fails to detect Windows 10 correctly on Java 1.8 prior to u60.
    // See: https://bugs.openjdk.java.net/browse/JDK-8066504
    static boolean isFlatWindows() {
        return System.getProperty("os.name").startsWith("Windows")
                && System.getProperty("os.version").matches("6\\.[23]|10\\..*");
    }

    /**
     * Adjust a color by adding some constant to its RGB values, clamping to the
     * range 0-255.
     */
    static Color adjustRGB(Color color, int adjustment) {
        return new Color(Math.max(0, Math.min(255, color.getRed() + adjustment)),
                Math.max(0, Math.min(255, color.getGreen() + adjustment)),
                Math.max(0, Math.min(255, color.getBlue() + adjustment)));
    }
}
