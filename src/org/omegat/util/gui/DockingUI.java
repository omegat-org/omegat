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
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;

import com.vlsolutions.swing.docking.AutoHidePolicy;
import com.vlsolutions.swing.docking.AutoHidePolicy.ExpandMode;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

import java.awt.Font;
import java.io.FileNotFoundException;

/**
 * Docking UI support.
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
 */
public class DockingUI {

    /**
     * Initialize docking subsystem.
     */
    public static void initialize() {
        // Install VLDocking defaults
        DockingUISettings.getInstance().installUI();
        
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

        // Classic design overridden by flat design
        //installClassicDesign();
        
        installFlatDesign();
        
        // Panel notification (blinking tabs/headers) settings
        UIManager.put("DockingDesktop.notificationBlinkCount", 2);
        UIManager.put("DockingDesktop.notificationColor", new Color(0xFFE8E8));
        
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
                    brightness -= 0.5f;    // to darker
                } else {
                    brightness += 0.5f;    // to brighter
                }
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], brightness);
                ColorUIResource res = new ColorUIResource(rgb);
                UIManager.put("InternalFrame.inactiveTitleForeground", res);
            }
        }
    }
    
    private static void installFlatDesign() {
        // Colors
        Color standardBgColor = UIManager.getColor("Panel.background"); // #EEEEEE on Metal & OS X LAF
        Color activeTitleBgColor = adjustRGB(standardBgColor, 0xF6 - 0xEE); // #EEEEEE -> #F6F6F6; Lighter than standard background
        Color bottomAreaBgColor = adjustRGB(standardBgColor, 0xDE - 0xEE); // #EEEEEE -> #DEDEDE; Darkest background
        Color borderColor = adjustRGB(standardBgColor, 0x9B - 0xEE); // #EEEEEE -> #9B9B9B; Standard border. Darker than standard background.
        UIManager.put("OmegaTBorder.color", borderColor);
        Color statusAreaColor = adjustRGB(standardBgColor, 0x57 - 0xEE); // #EEEEEE -> #575757; Darkest border
        
        // General highlight & shadow used in a lot of places
        UIManager.put("VLDocking.highlight", activeTitleBgColor);
        UIManager.put("VLDocking.shadow", statusAreaColor);
        
        // Main window main area
        int outside = 5;
        UIManager.put("DockingDesktop.border", new EmptyBorder(outside, outside, outside, outside));
        
        // Docked, visible panels get two borders if we're not careful:
        // 1. Drawn by VLDocking. Surrounds panel content AND header. Set this to empty margin instead.
        int panel = 2;
        UIManager.put("DockView.singleDockableBorder", new EmptyBorder(panel, panel, panel, panel));
        int maxPanel = outside + panel;
        UIManager.put("DockView.maximizedDockableBorder", new EmptyBorder(maxPanel, maxPanel, maxPanel, maxPanel));
        // 2. Drawn by OmegaT-defined Dockables. Make this a 1px line.
        UIManager.put("OmegaTDockablePanel.border", new MatteBorder(1, 1, 1, 1, borderColor));

        // GTK+ LAF has a default border on the viewport. Disable this.
        UIManager.put("OmegaTDockablePanelViewport.border", new EmptyBorder(0, 0, 0, 0));
        
        // Use proportionally sized internal margin for text document-like panels
        UIManager.put("OmegaTDockablePanel.isProportionalMargins", true);
        
        // Tabbed docked, visible panels are surrounded by LAF-specific chrome, but the surrounding
        // colors don't appear to be available through the API. These values are from visual inspection.
        if (Platform.isMacOSX()) {
            UIManager.put("DockView.tabbedDockableBorder", new MatteBorder(0, 5, 5, 5, new Color(0xE6E6E6)));
        } else if (isWindowsLAF() && !isWindowsClassicLAF()) {
            UIManager.put("DockView.tabbedDockableBorder", new MatteBorder(2, 5, 5, 5, Color.WHITE));
        } else {
            UIManager.put("DockView.tabbedDockableBorder", new MatteBorder(5, 5, 5, 5, standardBgColor));
        }
        
        // Windows 8+ is very square.
        int cornerRadius = isFlatWindows() ? 0 : 8;
        
        // Panel title bars
        Color activeTitleText = UIManager.getColor("Label.foreground");
        Color inactiveTitleText = adjustRGB(activeTitleText, 0x80); // #000000 -> #808080; GTK+ has Color.WHITE for Label.disabledForeground
        UIManager.put("DockViewTitleBar.border", new RoundedCornerBorder(cornerRadius, borderColor, RoundedCornerBorder.SIDE_TOP));
        UIManager.put("InternalFrame.activeTitleForeground", activeTitleText); // Windows 7 "Classic" has Color.WHITE for this
        UIManager.put("InternalFrame.activeTitleBackground", activeTitleBgColor);
        UIManager.put("InternalFrame.inactiveTitleForeground", inactiveTitleText); 
        UIManager.put("InternalFrame.inactiveTitleBackground", standardBgColor);
        // Disable gradient on pane title bars
        UIManager.put("DockViewTitleBar.disableCustomPaint", true);

        // Main window bottom area

        // AutoHideButtonPanel is where minimized panel tabs go. Use compound border to give left/right margins.
        UIManager.put("AutoHideButtonPanel.bottomBorder", new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, borderColor),
                new EmptyBorder(0, 2 * outside, 0, 2 * outside)));
        UIManager.put("AutoHideButtonPanel.background", bottomAreaBgColor);
        UIManager.put("AutoHideButton.expandBorderBottom", new RoundedCornerBorder(cornerRadius, borderColor, RoundedCornerBorder.SIDE_BOTTOM));
        UIManager.put("AutoHideButton.background", standardBgColor);
        // OmegaT-defined status box in lower right
        UIManager.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, statusAreaColor));
        // Lowermost section margins
        UIManager.put("OmegaTMainWindowBottomMargin.border", new EmptyBorder(0, 2 * outside, outside, 2 * outside));
        
        UIManager.put("OmegaTEditorFilter.border", new MatteBorder(1, 1, 0, 1, borderColor));
        
        // Undocked panel
        UIManager.put("activeCaption", Color.WHITE);
        UIManager.put("activeCaptionBorder", borderColor);
        UIManager.put("inactiveCaption", standardBgColor);
        UIManager.put("inactiveCaptionBorder", borderColor);
        
        // Icons
        UIManager.put("DockViewTitleBar.maximize", getIcon("appbar.app.tall.inactive.png"));
        UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("appbar.app.tall.png"));
        UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("appbar.app.tall.pressed.png"));
        UIManager.put("DockViewTitleBar.restore", getIcon("appbar.window.restore.inactive.png"));
        UIManager.put("DockViewTitleBar.restore.rollover", getIcon("appbar.window.restore.png"));
        UIManager.put("DockViewTitleBar.restore.pressed", getIcon("appbar.window.restore.pressed.png"));
        UIManager.put("DockViewTitleBar.hide", getIcon("appbar.hide.inactive.png"));
        UIManager.put("DockViewTitleBar.hide.rollover", getIcon("appbar.hide.png"));
        UIManager.put("DockViewTitleBar.hide.pressed", getIcon("appbar.hide.pressed.png"));
        UIManager.put("DockViewTitleBar.float", getIcon("appbar.fullscreen.inactive.png"));
        UIManager.put("DockViewTitleBar.float.rollover", getIcon("appbar.fullscreen.png"));
        UIManager.put("DockViewTitleBar.float.pressed", getIcon("appbar.fullscreen.pressed.png"));
        UIManager.put("DockViewTitleBar.dock", getIcon("appbar.window.restore.inactive.png"));
        UIManager.put("DockViewTitleBar.dock.rollover", getIcon("appbar.window.restore.png"));
        UIManager.put("DockViewTitleBar.dock.pressed", getIcon("appbar.window.restore.pressed.png"));
        UIManager.put("DockViewTitleBar.attach", getIcon("appbar.dock.window.inactive.png"));
        UIManager.put("DockViewTitleBar.attach.rollover", getIcon("appbar.dock.window.png"));
        UIManager.put("DockViewTitleBar.attach.pressed", getIcon("appbar.dock.window.pressed.png"));
        
        UIManager.put("DockViewTitleBar.menu.hide", getIcon("appbar.hide.png"));
        UIManager.put("DockViewTitleBar.menu.maximize", getIcon("appbar.app.tall.png"));
        UIManager.put("DockViewTitleBar.menu.restore", getIcon("appbar.window.restore.png"));
        UIManager.put("DockViewTitleBar.menu.dock", getIcon("appbar.window.restore.png"));
        UIManager.put("DockViewTitleBar.menu.float", getIcon("appbar.fullscreen.png"));
        UIManager.put("DockViewTitleBar.menu.attach", getIcon("appbar.dock.window.png"));

        UIManager.put("DockTabbedPane.menu.hide", getIcon("appbar.hide.png"));
        UIManager.put("DockTabbedPane.menu.maximize", getIcon("appbar.app.tall.png"));
        UIManager.put("DockTabbedPane.menu.float", getIcon("appbar.fullscreen.png"));
        
        // Windows only accepts a 32x32 cursor image with no semitransparency, so you basically
        // need a special image just for that.
        UIManager.put("DragControler.detachCursor", getImage("appbar.fullscreen.cursor32x32.png"));
        
        // Use more native-looking icons on OS X
        if (Platform.isMacOSX()) {
            UIManager.put("DockViewTitleBar.maximize", getIcon("appbar.fullscreen.corners.inactive.png"));
            UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("appbar.fullscreen.corners.png"));
            UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("appbar.fullscreen.corners.pressed.png"));
            UIManager.put("DockViewTitleBar.restore", getIcon("appbar.restore.corners.inactive.png"));
            UIManager.put("DockViewTitleBar.restore.rollover", getIcon("appbar.restore.corners.png"));
            UIManager.put("DockViewTitleBar.restore.pressed", getIcon("appbar.restore.corners.pressed.png"));
            UIManager.put("DockViewTitleBar.hide", getIcon("appbar.minus.inactive.png"));
            UIManager.put("DockViewTitleBar.hide.rollover", getIcon("appbar.minus.png"));
            UIManager.put("DockViewTitleBar.hide.pressed", getIcon("appbar.minus.pressed.png"));
            
            UIManager.put("DockViewTitleBar.menu.hide", getIcon("appbar.minus.png"));
            UIManager.put("DockViewTitleBar.menu.maximize", getIcon("appbar.fullscreen.corners.png"));
            UIManager.put("DockViewTitleBar.menu.restore", getIcon("appbar.restore.corners.png"));
            
            UIManager.put("DockTabbedPane.menu.hide", getIcon("appbar.minus.png"));
            UIManager.put("DockTabbedPane.menu.maximize", getIcon("appbar.fullscreen.corners.png"));
            
            UIManager.put("DragControler.detachCursor", getImage("appbar.fullscreen.png"));
        }
    }
    
    /**
     * Adjust a color by adding some constant to its RGB values, wrapping around within the range 0-255.
     */
    private static Color adjustRGB(Color color, int adjustment) {
        Color result = new Color((color.getRed() + adjustment + 255) % 255,
                (color.getGreen() + adjustment + 255) % 255,
                (color.getBlue() + adjustment + 255) % 255);
        return result;
    }
    
    // Windows Classic LAF detection from http://stackoverflow.com/a/4386821/448068
    private static boolean isWindowsLAF() {
        return UIManager.getLookAndFeel().getID().equals("Windows");
    }

    private static boolean isWindowsClassicLAF() {
        return isWindowsLAF() &&
                !(Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive");
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
        Image image = getImage(iconName);
        return image == null ? null : new ImageIcon(image);
    }
    
    private static Image getImage(String imageName) {
        try {
            return ResourcesUtil.getImage("/org/omegat/gui/resources/" + imageName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Move window to the center of main window.
     * 
     * @param window
     *            window
     */
    public static void displayCentered(final Window window) {
        window.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
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
}
