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
               2021 Hiroshi Miura
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
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.RoundedCornerBorder;


public class DefaultFlatTheme extends LookAndFeel {

    private static final String NAME = OStrings.getString("THEME_OMEGAT_DEFAULT_NAME");

    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultFlatTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    private LookAndFeel systemLookAndFeel;

    public DefaultFlatTheme() throws Exception {
        systemLookAndFeel = (LookAndFeel) Class.forName(UIManager.getSystemLookAndFeelClassName()).newInstance();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getID() {
        return "OmegaT"; // NOI18N
    }

    @Override
    public String getDescription() {
        return OStrings.getString("THEME_OMEGAT_DEFAULT_DESCRIPTION");
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
    public UIDefaults getDefaults() {
        // Use system LAF ID because we use the ID to see if it's e.g. Windows
        return setDefaults(super.getDefaults(), systemLookAndFeel.getID());
    }

    /**
     * Apply theme default values to the supplied {@link UIDefaults} object.
     * Suitable for third-party themes that want to modify the default theme.
     *
     * @param defaults the {@link UIDefaults} object such as from
     *                 {@link LookAndFeel#getDefaults()}
     * @param lafId    the ID of the LAF that will make use of the defaults (from
     *                 {@link LookAndFeel#getID()})
     * @return the modified {@link UIDefaults} object
     */
    public static UIDefaults setDefaults(UIDefaults defaults, String lafId) {
        // Colors
        // #EEEEEE on Metal & OS X LAF
        Color standardBgColor = defaults.getColor("Panel.background");
        // #EEEEEE -> #F6F6F6; Lighter than standard background
        Color activeTitleBgColor = adjustRGB(standardBgColor, 0xF6 - 0xEE);
        // #EEEEEE -> #DEDEDE; Darkest background
        Color bottomAreaBgColor = adjustRGB(standardBgColor, 0xDE - 0xEE);
        // #EEEEEE -> #9B9B9B; Standard border. Darker than standard background.
        Color borderColor = adjustRGB(standardBgColor, 0x9B - 0xEE);
        defaults.put("OmegaTBorder.color", borderColor);
        // #EEEEEE -> #575757; Darkest border
        Color statusAreaColor = adjustRGB(standardBgColor, 0x57 - 0xEE);

        // General highlight & shadow used in a lot of places
        defaults.put("VLDocking.highlight", activeTitleBgColor);
        defaults.put("VLDocking.shadow", statusAreaColor);

        // Main window main area
        int outside = 5;
        defaults.put("DockingDesktop.border", new EmptyBorder(outside, outside, outside, outside));

        // Docked, visible panels get two borders if we're not careful:
        // 1. Drawn by VLDocking. Surrounds panel content AND header. Set this to empty margin instead.
        int panel = 2;
        defaults.put("DockView.singleDockableBorder", new EmptyBorder(panel, panel, panel, panel));
        int maxPanel = outside + panel;
        defaults.put("DockView.maximizedDockableBorder", new EmptyBorder(maxPanel, maxPanel, maxPanel, maxPanel));
        // 2. Drawn by OmegaT-defined Dockables. Make this a 1px line.
        defaults.put("OmegaTDockablePanel.border", new MatteBorder(1, 1, 1, 1, borderColor));

        // GTK+ LAF has a default border on the viewport. Disable this.
        defaults.put("OmegaTDockablePanelViewport.border", new EmptyBorder(0, 0, 0, 0));

        // Use proportionally sized internal margin for text document-like panels
        defaults.put("OmegaTDockablePanel.isProportionalMargins", true);

        // Tabbed docked, visible panels are surrounded by LAF-specific chrome, but the surrounding
        // colors don't appear to be available through the API. These values are from visual inspection.
        if (Platform.isMacOSX()) {
            defaults.put("DockView.tabbedDockableBorder", new MatteBorder(0, 5, 5, 5, new Color(0xE6E6E6)));
        } else if (isWindowsLAF(lafId) && !isWindowsClassicLAF(lafId)) {
            defaults.put("DockView.tabbedDockableBorder", new MatteBorder(2, 5, 5, 5, Color.WHITE));
        } else {
            defaults.put("DockView.tabbedDockableBorder", new MatteBorder(5, 5, 5, 5, standardBgColor));
        }

        // Windows 8+ is very square.
        int cornerRadius = isFlatWindows() ? 0 : 8;

        // Panel title bars
        Color activeTitleText = defaults.getColor("Label.foreground");
        // #000000 -> #808080; GTK+ has Color.WHITE for Label.disabledForeground
        Color inactiveTitleText = adjustRGB(activeTitleText, 0x80);
        defaults.put("DockViewTitleBar.border",
                new RoundedCornerBorder(cornerRadius, borderColor, RoundedCornerBorder.SIDE_TOP));
        // Windows 7 "Classic" has Color.WHITE for this
        defaults.put("InternalFrame.activeTitleForeground", activeTitleText);
        defaults.put("InternalFrame.activeTitleBackground", activeTitleBgColor);
        defaults.put("InternalFrame.inactiveTitleForeground", inactiveTitleText);
        defaults.put("InternalFrame.inactiveTitleBackground", standardBgColor);
        // Disable gradient on pane title bars
        defaults.put("DockViewTitleBar.disableCustomPaint", true);

        // Main window bottom area

        // AutoHideButtonPanel is where minimized panel tabs go. Use compound border to give left/right margins.
        defaults.put("AutoHideButtonPanel.bottomBorder", new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, borderColor),
                new EmptyBorder(0, 2 * outside, 0, 2 * outside)));
        defaults.put("AutoHideButtonPanel.background", bottomAreaBgColor);
        defaults.put("AutoHideButton.expandBorderBottom",
                new RoundedCornerBorder(cornerRadius, borderColor, RoundedCornerBorder.SIDE_BOTTOM));
        defaults.put("AutoHideButton.background", standardBgColor);
        // OmegaT-defined status box in lower right
        defaults.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, statusAreaColor));
        // Lowermost section margins
        defaults.put("OmegaTMainWindowBottomMargin.border", new EmptyBorder(0, 2 * outside, outside, 2 * outside));

        defaults.put("OmegaTEditorFilter.border", new MatteBorder(1, 1, 0, 1, borderColor));

        // Undocked panel
        defaults.put("activeCaption", Color.WHITE);
        defaults.put("activeCaptionBorder", borderColor);
        defaults.put("inactiveCaption", standardBgColor);
        defaults.put("inactiveCaptionBorder", borderColor);

        // Icons
        defaults.put("DockViewTitleBar.maximize", getIcon("appbar.app.tall.inactive.png"));
        defaults.put("DockViewTitleBar.maximize.rollover", getIcon("appbar.app.tall.png"));
        defaults.put("DockViewTitleBar.maximize.pressed", getIcon("appbar.app.tall.pressed.png"));
        defaults.put("DockViewTitleBar.restore", getIcon("appbar.window.restore.inactive.png"));
        defaults.put("DockViewTitleBar.restore.rollover", getIcon("appbar.window.restore.png"));
        defaults.put("DockViewTitleBar.restore.pressed", getIcon("appbar.window.restore.pressed.png"));
        defaults.put("DockViewTitleBar.hide", getIcon("appbar.hide.inactive.png"));
        defaults.put("DockViewTitleBar.hide.rollover", getIcon("appbar.hide.png"));
        defaults.put("DockViewTitleBar.hide.pressed", getIcon("appbar.hide.pressed.png"));
        defaults.put("DockViewTitleBar.float", getIcon("appbar.fullscreen.inactive.png"));
        defaults.put("DockViewTitleBar.float.rollover", getIcon("appbar.fullscreen.png"));
        defaults.put("DockViewTitleBar.float.pressed", getIcon("appbar.fullscreen.pressed.png"));
        defaults.put("DockViewTitleBar.dock", getIcon("appbar.window.restore.inactive.png"));
        defaults.put("DockViewTitleBar.dock.rollover", getIcon("appbar.window.restore.png"));
        defaults.put("DockViewTitleBar.dock.pressed", getIcon("appbar.window.restore.pressed.png"));
        defaults.put("DockViewTitleBar.attach", getIcon("appbar.dock.window.inactive.png"));
        defaults.put("DockViewTitleBar.attach.rollover", getIcon("appbar.dock.window.png"));
        defaults.put("DockViewTitleBar.attach.pressed", getIcon("appbar.dock.window.pressed.png"));

        defaults.put("DockViewTitleBar.menu.hide", getIcon("appbar.hide.png"));
        defaults.put("DockViewTitleBar.menu.maximize", getIcon("appbar.app.tall.png"));
        defaults.put("DockViewTitleBar.menu.restore", getIcon("appbar.window.restore.png"));
        defaults.put("DockViewTitleBar.menu.dock", getIcon("appbar.window.restore.png"));
        defaults.put("DockViewTitleBar.menu.float", getIcon("appbar.fullscreen.png"));
        defaults.put("DockViewTitleBar.menu.attach", getIcon("appbar.dock.window.png"));

        defaults.put("DockTabbedPane.menu.hide", getIcon("appbar.hide.png"));
        defaults.put("DockTabbedPane.menu.maximize", getIcon("appbar.app.tall.png"));
        defaults.put("DockTabbedPane.menu.float", getIcon("appbar.fullscreen.png"));

        // Windows only accepts a 32x32 cursor image with no semitransparency, so you basically
        // need a special image just for that.
        defaults.put("DragControler.detachCursor", ResourcesUtil.getBundledImage("appbar.fullscreen.cursor32x32.png"));

        // Use more native-looking icons on OS X
        if (Platform.isMacOSX()) {
            defaults.put("DockViewTitleBar.maximize", getIcon("appbar.fullscreen.corners.inactive.png"));
            defaults.put("DockViewTitleBar.maximize.rollover", getIcon("appbar.fullscreen.corners.png"));
            defaults.put("DockViewTitleBar.maximize.pressed", getIcon("appbar.fullscreen.corners.pressed.png"));
            defaults.put("DockViewTitleBar.restore", getIcon("appbar.restore.corners.inactive.png"));
            defaults.put("DockViewTitleBar.restore.rollover", getIcon("appbar.restore.corners.png"));
            defaults.put("DockViewTitleBar.restore.pressed", getIcon("appbar.restore.corners.pressed.png"));
            defaults.put("DockViewTitleBar.hide", getIcon("appbar.minus.inactive.png"));
            defaults.put("DockViewTitleBar.hide.rollover", getIcon("appbar.minus.png"));
            defaults.put("DockViewTitleBar.hide.pressed", getIcon("appbar.minus.pressed.png"));

            defaults.put("DockViewTitleBar.menu.hide", getIcon("appbar.minus.png"));
            defaults.put("DockViewTitleBar.menu.maximize", getIcon("appbar.fullscreen.corners.png"));
            defaults.put("DockViewTitleBar.menu.restore", getIcon("appbar.restore.corners.png"));

            defaults.put("DockTabbedPane.menu.hide", getIcon("appbar.minus.png"));
            defaults.put("DockTabbedPane.menu.maximize", getIcon("appbar.fullscreen.corners.png"));

            defaults.put("DragControler.detachCursor", ResourcesUtil.getBundledImage("appbar.fullscreen.png"));
        }
        return defaults;
    }

    /**
     * Adjust a color by adding some constant to its RGB values, clamping to the
     * range 0-255.
     */
    private static Color adjustRGB(Color color, int adjustment) {
        Color result = new Color(Math.max(0, Math.min(255, color.getRed() + adjustment)),
                Math.max(0, Math.min(255, color.getGreen() + adjustment)),
                Math.max(0, Math.min(255, color.getBlue() + adjustment)));
        return result;
    }

    // Windows Classic LAF detection from http://stackoverflow.com/a/4386821/448068
    private static boolean isWindowsLAF(String systemID) {
        return systemID.equals("Windows");
    }

    private static boolean isWindowsClassicLAF(String systemID) {
        return isWindowsLAF(systemID) && !(Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive");
    }

    // This check fails to detect Windows 10 correctly on Java 1.8 prior to u60.
    // See: https://bugs.openjdk.java.net/browse/JDK-8066504
    private static boolean isFlatWindows() {
        return System.getProperty("os.name").startsWith("Windows")
                && System.getProperty("os.version").matches("6\\.[23]|10\\..*");
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
}
