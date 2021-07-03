/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Aaron Madlon-Kay
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

package org.omegat.gui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.madlonkay.desktopsupport.DesktopSupport;
import org.omegat.util.Platform;
import org.omegat.util.gui.ResourcesUtil;

/**
 * A LAF class that delegates almost everything to the system LAF.
 *
 * Implementers must provide a distinct identity ({@link #getName()},
 * {@link #getID()}, {@link #getDescription()}). They should also probably
 * supply novel defaults ({@link #initialize()}, {@link #getDefaults()}).
 *
 * @author Aaron Madlon-Kay
 */
public abstract class DelegatingLookAndFeel extends LookAndFeel {
    /**
     * Load icon from classpath.
     *
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    protected static ImageIcon getIcon(String iconName) {
        Image image = ResourcesUtil.getBundledImage(iconName);
        return image == null ? null : new ImageIcon(image);
    }

    protected final LookAndFeel systemLookAndFeel;

    public DelegatingLookAndFeel() throws Exception {
        String systemLafClass = UIManager.getSystemLookAndFeelClassName();
        String systemLafName = null;
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getClassName().equals(systemLafClass)) {
                systemLafName = info.getName();
            }
        }
        if (systemLafName == null) {
            // Should never happen: system LAF is guaranteed to be installed
            throw new RuntimeException("Could not identify system LAF name");
        }
        systemLookAndFeel = DesktopSupport.getSupport().createLookAndFeel(systemLafName);
    }

    /**
     * Heuristic detection of dark theme.
     * <p>
     *     isDarkTheme method derived from NetBeans licensed by Apache-2.0
     * @return true when dark theme, otherwise false.
     */
    protected static boolean isDarkTheme(UIDefaults defaults) {
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
        Color foreground = defaults.getColor("Table.foreground");
        Color background = defaults.getColor("Table.background");
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

    protected static void loadColors(UIDefaults defaults, final String scheme) throws IOException {
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
                defaults.put(k.toString(), color);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Invalid color value for key '" + k + "': " + v, ex);
            }
        });
    }

    /**
     * Load application default colors
     */
    protected static void loadDefaultColors(UIDefaults defaults) throws IOException {
        Color hilite;
        if (isDarkTheme(defaults)) {
            loadColors(defaults, "dark");
            hilite = defaults.getColor("TextArea.background").brighter();  // NOI18N
            // Hack for JDK GTKLookAndFeel bug.
            // TextPane.background is always white but should be a text_background of GTK.
            // List.background is as same color as text_background.
            if (Platform.isLinux() && Color.WHITE.equals(defaults.getColor("TextPane.background"))) {
                defaults.put("TextPane.background", defaults.getColor("List.background"));
            }
        } else {
            loadColors(defaults, "light");
            Color bg = defaults.getColor("TextArea.background").darker();  // NOI18N
            hilite = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 32);
        }
        defaults.put("OmegaT.alternatingHilite", hilite);
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return systemLookAndFeel.isNativeLookAndFeel();
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return systemLookAndFeel.isSupportedLookAndFeel();
    }

    @Override
    public LayoutStyle getLayoutStyle() {
        return systemLookAndFeel.getLayoutStyle();
    }

    @Override
    public Icon getDisabledIcon(JComponent component, Icon icon) {
        return systemLookAndFeel.getDisabledIcon(component, icon);
    }

    @Override
    public Icon getDisabledSelectedIcon(JComponent component, Icon icon) {
        return systemLookAndFeel.getDisabledSelectedIcon(component, icon);
    }

    @Override
    public boolean getSupportsWindowDecorations() {
        return systemLookAndFeel.getSupportsWindowDecorations();
    }

    @Override
    public void provideErrorFeedback(Component component) {
        systemLookAndFeel.provideErrorFeedback(component);
    }

    @Override
    public void initialize() {
        systemLookAndFeel.initialize();
    }

    @Override
    public void uninitialize() {
        systemLookAndFeel.uninitialize();
    }
}
