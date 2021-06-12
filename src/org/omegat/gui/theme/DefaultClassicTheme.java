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
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import org.omegat.util.gui.ResourcesUtil;


public class DefaultClassicTheme extends LookAndFeel {

    private static final String NAME = "Classic";
    private static final String CLASS_NAME = "org.omegat.gui.theme.DefaultClassicTheme";
    private static final String DESCRIPTION = "CLassic OmegaT theme";

    private LookAndFeel systemLookAndFeel;

    public DefaultClassicTheme() {
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(UIManager.getSystemLookAndFeelClassName());
            systemLookAndFeel = (LookAndFeel) clazz.getConstructor().newInstance();
            systemLookAndFeel.initialize();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultFlatTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    /**
     * Return a short string that identifies this look and feel, e.g.
     * "CDE/Motif".  This string should be appropriate for a menu item.
     * Distinct look and feels should have different names, e.g.
     * a subclass of MotifLookAndFeel that changes the way a few components
     * are rendered should be called "CDE/Motif My Way"; something
     * that would be useful to a user trying to select a L&amp;F from a list
     * of names.
     *
     * @return short identifier for the look and feel
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Return a string that identifies this look and feel.  This string
     * will be used by applications/services that want to recognize
     * well known look and feel implementations.  Presently
     * the well known names are "Motif", "Windows", "Mac", "Metal".  Note
     * that a LookAndFeel derived from a well known superclass
     * that doesn't make any fundamental changes to the look or feel
     * shouldn't override this method.
     *
     * @return identifier for the look and feel
     */
    @Override
    public String getID() {
        return "Classic";
    }

    /**
     * Return a one line description of this look and feel implementation,
     * e.g. "The CDE/Motif Look and Feel".   This string is intended for
     * the user, e.g. in the title of a window or in a ToolTip message.
     *
     * @return short description for the look and feel
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * If the underlying platform has a "native" look and feel, and
     * this is an implementation of it, return {@code true}.  For
     * example, when the underlying platform is Solaris running CDE
     * a CDE/Motif look and feel implementation would return {@code
     * true}.
     *
     * @return {@code true} if this look and feel represents the underlying
     * platform look and feel
     */
    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    /**
     * Return {@code true} if the underlying platform supports and or permits
     * this look and feel.  This method returns {@code false} if the look
     * and feel depends on special resources or legal agreements that
     * aren't defined for the current platform.
     *
     * @return {@code true} if this is a supported look and feel
     * @see UIManager#setLookAndFeel
     */
    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    public UIDefaults getDefaults() {
        UIDefaults defaults = systemLookAndFeel.getDefaults();
        defaults.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, Color.BLACK));

        defaults.put("DockViewTitleBar.hide", getIcon("minimize.gif"));
        defaults.put("DockViewTitleBar.hide.rollover", getIcon("minimize.rollover.gif"));
        defaults.put("DockViewTitleBar.hide.pressed", getIcon("minimize.pressed.gif"));
        defaults.put("DockViewTitleBar.maximize", getIcon("maximize.gif"));
        defaults.put("DockViewTitleBar.maximize.rollover", getIcon("maximize.rollover.gif"));
        defaults.put("DockViewTitleBar.maximize.pressed", getIcon("maximize.pressed.gif"));
        defaults.put("DockViewTitleBar.restore", getIcon("restore.gif"));
        defaults.put("DockViewTitleBar.restore.rollover", getIcon("restore.rollover.gif"));
        defaults.put("DockViewTitleBar.restore.pressed", getIcon("restore.pressed.gif"));
        defaults.put("DockViewTitleBar.dock", getIcon("restore.gif"));
        defaults.put("DockViewTitleBar.dock.rollover", getIcon("restore.rollover.gif"));
        defaults.put("DockViewTitleBar.dock.pressed", getIcon("restore.pressed.gif"));
        defaults.put("DockViewTitleBar.float", getIcon("undock.gif"));
        defaults.put("DockViewTitleBar.float.rollover", getIcon("undock.rollover.gif"));
        defaults.put("DockViewTitleBar.float.pressed", getIcon("undock.pressed.gif"));
        defaults.put("DockViewTitleBar.attach", getIcon("dock.gif"));
        defaults.put("DockViewTitleBar.attach.rollover", getIcon("dock.rollover.gif"));
        defaults.put("DockViewTitleBar.attach.pressed", getIcon("dock.pressed.gif"));
        defaults.put("DockViewTitleBar.menu.hide", getIcon("minimize.gif"));
        defaults.put("DockViewTitleBar.menu.maximize", getIcon("maximize.gif"));
        defaults.put("DockViewTitleBar.menu.restore", getIcon("restore.gif"));
        defaults.put("DockViewTitleBar.menu.dock", getIcon("restore.gif"));
        defaults.put("DockViewTitleBar.menu.float", getIcon("undock.gif"));
        defaults.put("DockViewTitleBar.menu.attach", getIcon("dock.gif"));
        defaults.put("DockTabbedPane.menu.hide", getIcon("empty.gif"));
        defaults.put("DockTabbedPane.menu.maximize", getIcon("empty.gif"));
        defaults.put("DockTabbedPane.menu.float", getIcon("empty.gif"));
        defaults.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif"));
        defaults.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif"));
        defaults.put("DragControler.detachCursor", getIcon("undock.gif").getImage());
        return defaults;
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
