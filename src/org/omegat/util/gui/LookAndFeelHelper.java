/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Hiroshi Miura
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

import javax.swing.UIManager;
import java.awt.Color;

import org.omegat.util.Log;

/**
 * Helper to initialize LaF.
 */
public class LookAndFeelHelper {

    /** is LaF theme dark? */
    private static Boolean isDark = null;

    /**
     * Heuristic detection of dark theme.
     * <p>
     *     isDarkTheme method derived from NetBeans licensed by Apache-2.0
     *     and re-licensed into GPL3.
     * @return true when dark theme, otherwise false.
     */
    public static boolean isDarkTheme() {
        if(isDark != null) {
            return isDark;
        }
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
        isDark = background_brightness < foreground_brightness;
        return isDark;
    }

    /**
     * Set LookAndFeel default and load application colors
     */
    public static void setDefaultLaf() {
        // Workaround for JDK bug 6389282 (OmegaT bug bug 1555809)
        // it should be called before setLookAndFeel() for GTK LookandFeel
        // Contributed by Masaki Katakai (SF: katakai)
        UIManager.getInstalledLookAndFeels();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (isDarkTheme()) {
                UIManager.put("OmegaT.AlternatingHilite",
                        UIManager.getColor("TextArea.background").brighter());  // NOI18N
            } else {
                UIManager.put("OmegaT.AlternatingHilite",
                        UIManager.getColor("TextArea.background").darker());  // NOI18N
            }
        } catch (Exception e) {
            // do nothing
            Log.logErrorRB("MAIN_ERROR_CANT_INIT_OSLF");
        }
    }
}
