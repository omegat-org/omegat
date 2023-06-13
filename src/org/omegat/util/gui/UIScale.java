/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                2019 FormDev Software GmbH
 *                Home page: https://www.omegat.org/
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;

import org.omegat.util.Platform;

/**
 * This class handles scaling in Swing UIs. It computes a user scaling factor
 * based on font size and provides methods to scale integer, float,
 * {@link Dimension} and {@link Insets}. This class is look and feel
 * independent.
 * <p>
 * Two scaling modes are supported for HiDPI displays:
 *
 * <h2>1) system scaling mode</h2>
 *
 * This mode is supported since Java 9 on all platforms and in some Java 8 VMs
 * (e.g. Apple and JetBrains). The JRE determines the scale factor per-display
 * and adds a scaling transformation to the graphics object. E.g. invokes
 * {@code java.awt.Graphics2D.scale( 1.5, 1.5 )} for 150%. So the JRE does the
 * scaling itself. E.g. when you draw a 10px line, a 15px line is drawn on
 * screen. The scale factor may be different for each connected display. The
 * scale factor may change for a window when moving the window from one display
 * to another one.
 *
 * <h2>2) user scaling mode</h2>
 *
 * This mode is mainly for Java 8 compatibility, but is also used on Linux or if
 * the default font is changed. The user scale factor is computed based on the
 * used font. The JRE does not scale anything. So we have to invoke
 * {@link #scale(float)} where necessary. There is only one user scale factor
 * for all displays. The user scale factor may change if the active LaF,
 * "defaultFont" or "Label.font" has changed. If system scaling mode is
 * available, the user scale factor is usually 1, but may be larger on Linux or
 * if the default font is changed.
 *
 * This class is derived from FlatLaf library licensed by Apache-2.0.
 * 
 * @author Karl Tauber
 */
public final class UIScale {

    private UIScale() {
    }

    private static PropertyChangeSupport changeSupport;

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null)
            changeSupport = new PropertyChangeSupport(UIScale.class);
        changeSupport.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null)
            return;
        changeSupport.removePropertyChangeListener(listener);
    }

    // ---- system scaling (mac/windows)

    /**
     * Returns the system scale factor for the given graphics context.
     */
    public static double getSystemScaleFactor(Graphics2D g) {
        return getSystemScaleFactor(g.getDeviceConfiguration());
    }

    /**
     * Returns the system scale factor for the given graphics configuration.
     */
    public static double getSystemScaleFactor(GraphicsConfiguration gc) {
        return (gc != null) ? gc.getDefaultTransform().getScaleX() : 1;
    }

    // ---- user scaling (linux)

    private static float scaleFactor = 1;
    private static boolean initialized;

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        // listener to update a scale factor if LaF changed, "defaultFont" or
        // "Label.font" changed
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                switch (e.getPropertyName()) {
                case "lookAndFeel":
                    // it is not necessary (and possible) to remove listener of
                    // old LaF defaults
                    if (e.getNewValue() instanceof LookAndFeel)
                        UIManager.getLookAndFeelDefaults().addPropertyChangeListener(this);
                    updateScaleFactor();
                    break;

                case "defaultFont":
                case "Label.font":
                    updateScaleFactor();
                    break;
                }
            }
        };
        UIManager.addPropertyChangeListener(listener);
        UIManager.getDefaults().addPropertyChangeListener(listener);
        UIManager.getLookAndFeelDefaults().addPropertyChangeListener(listener);

        updateScaleFactor();
    }

    private static void updateScaleFactor() {
        // use font size to calculate a scale factor (instead of DPI)
        // because even if we are on a HiDPI display, it is not sure
        // that a larger font size is set by the current LaF
        // (e.g., can avoid large icons with small text)
        Font font = UIManager.getFont("defaultFont");
        if (font == null)
            font = UIManager.getFont("Label.font");

        setUserScaleFactor(computeFontScaleFactor(font));
    }

    private static float computeFontScaleFactor(Font font) {
        if (Platform.isWindows) {
            // Special handling for Windows to be compatible with OS scaling,
            // which distinguish between "screen scaling" and "text scaling".
            // - Windows "screen scaling" scales everything (text, icon, gaps,
            // etc)
            // and may have different scaling factors for each screen.
            // - Windows "text scaling" increases only the font size, but on all
            // screens.
            //
            // Both can be changed by the user in the Windows 10 Settings:
            // - Settings > Display > Scale and layout
            // - Settings > Ease of Access > Display > Make text bigger (100% -
            // 225%)
            if (font instanceof UIResource) {
                Font uiFont = (Font) Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font");
                if (uiFont == null || uiFont.getSize() == font.getSize()) {
                    // Do not apply own scaling if the JRE scales using a
                    // Windows screen scale factor.
                    // If a user increases font size in Windows 10 settings,
                    // desktop property "win.messagebox.font" is changed,
                    // and we use the larger font.
                    return 1;
                }
            }
        }
        if (Platform.isLinux && !isSystemScaling()) {
            // see class com.sun.java.swing.plaf.gtk.PangoFonts background
            // information
            Object value = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/DPI");
            if (value instanceof Integer) {
                int dpi = (Integer) value / 1024;
                if (dpi < 96) {
                    return 1;
                }
                return (float) (dpi / 96.0);
            }
        }
        return computeScaleFactor(font);
    }

    private static float computeScaleFactor(Font font) {
        // default font size
        float fontSizeDivider = 12f;

        if (Platform.isWindows) {
            // Windows LaF uses Tahoma font rather than the actual Windows
            // system font (Segoe UI),
            // and its size is always ca. 10% smaller than the actual system
            // font size.
            // Tahoma 11 is used at 100%
            if ("Tahoma".equals(font.getFamily()))
                fontSizeDivider = 11f;
        } else if (Platform.isMacOS) {
            // the default font size on macOS is 13
            fontSizeDivider = 13f;
        } else if (Platform.isLinux) {
            // the default font size for Unity and Gnome is 15 and for KDE it is
            // 13
            fontSizeDivider = Platform.isKDE ? 13f : 15f;
        }

        return font.getSize() / fontSizeDivider;
    }

    private static void setUserScaleFactor(float scaleFactor) {
        if (scaleFactor < 1f) {
            // round small scale factor to 1/10
            scaleFactor = Math.round(scaleFactor * 10f) / 10f;
        } else if (scaleFactor > 1f) {
            // round scale factor to 1/4
            scaleFactor = Math.round(scaleFactor * 4f) / 4f;
        }

        // minimum scale factor
        scaleFactor = Math.max(scaleFactor, 0.1f);

        float oldScaleFactor = UIScale.scaleFactor;
        UIScale.scaleFactor = scaleFactor;

        if (changeSupport != null) {
            changeSupport.firePropertyChange("userScaleFactor", oldScaleFactor, scaleFactor);
        }
    }

    /**
     * Get scale factor.
     * @return float number.
     */
    public static float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Multiplies the given value by the user scale factor.
     */
    public static float scale(float value) {
        initialize();
        if (scaleFactor == 1) {
            return value;
        }
        return value * scaleFactor;
    }

    /**
     * Multiplies the given value by the user scale factor and rounds the
     * result.
     */
    public static int scale(int value) {
        initialize();
        if (scaleFactor == 1) {
            return value;
        }
        return Math.round(value * scaleFactor);
    }

    /**
     * Similar as {@link #scale(int)} but always "rounds down".
     * <p>
     * For use in special cases. {@link #scale(int)} is the preferred method.
     */
    public static int scale2(int value) {
        initialize();
        if (scaleFactor == 1) {
            return value;
        }
        return (int) (value * scaleFactor);
    }

    /**
     * Divides the given value by the user scale factor.
     */
    public static float unscale(float value) {
        initialize();
        if (scaleFactor == 1f) {
            return value;
        }
        return value / scaleFactor;
    }

    /**
     * Divides the given value by the user scale factor and rounds the result.
     */
    public static int unscale(int value) {
        initialize();
        if (scaleFactor == 1f) {
            return value;
        }
        return Math.round(value / scaleFactor);
    }

    /**
     * If a user scale factor is not 1, scale the given graphics context by
     * invoking {@link Graphics2D#scale(double, double)} with a user scale
     * factor.
     */
    public static void scaleGraphics(Graphics2D g) {
        initialize();
        if (scaleFactor != 1f)
            g.scale(scaleFactor, scaleFactor);
    }

    /**
     * Scales the given dimension with the user scale factor.
     * <p>
     * If a user scale factor is 1, then the given dimension is simply returned.
     * Otherwise, a new instance of {@link Dimension} or
     * {@link DimensionUIResource} is returned, depending on whether the passed
     * dimension implements {@link UIResource}.
     */
    public static Dimension scale(Dimension dimension) {
        initialize();
        if (dimension == null || scaleFactor == 1f) {
            return dimension;
        }
        if (dimension instanceof UIResource) {
            return new DimensionUIResource(scale(dimension.width), scale(dimension.height));
        }
        return new Dimension(scale(dimension.width), scale(dimension.height));
    }

    /**
     * Scales the given insets with the user scale factor.
     * <p>
     * If a user scale factor is 1, then the given insets are simply returned.
     * Otherwise, a new instance of {@link Insets} or {@link InsetsUIResource}
     * is returned, depending on whether the passed dimension implements
     * {@link UIResource}.
     */
    public static Insets scale(Insets insets) {
        initialize();
        if (insets == null || scaleFactor == 1f) {
            return insets;
        }
        if (insets instanceof UIResource) {
            return new InsetsUIResource(scale(insets.top), scale(insets.left), scale(insets.bottom),
                    scale(insets.right));
        }
        return new Insets(scale(insets.top), scale(insets.left), scale(insets.bottom), scale(insets.right));
    }

    /**
     * Returns true if the JRE scales, which is the case if: - environment
     * variable GDK_SCALE is set and running on Java 9 or later - running on
     * JetBrains Runtime 11 or later, and scaling is enabled in system Settings
     */
    public static boolean isSystemScaling() {
        if (GraphicsEnvironment.isHeadless())
            return true;

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();
        return getSystemScaleFactor(gc) > 1;
    }
}
