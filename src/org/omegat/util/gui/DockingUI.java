/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula
 Portions copyright 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.util.OStrings;

import com.vlsolutions.swing.docking.ui.DockingUISettings;

/**
 * Docking UI support.
 */
public class DockingUI {

    /**
     * Initialize docking subsystem.
     */
    public static void initialize() {
        DockingUISettings.getInstance().installUI();
        UIManager.put("DockViewTitleBar.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        UIManager.put("DockViewTitleBar.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        UIManager.put("DockViewTitleBar.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        UIManager.put("DockViewTitleBar.attachButtonText", OStrings.getString("DOCKING_HINT_DOCK"));
        UIManager.put("DockViewTitleBar.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        UIManager.put("DockViewTitleBar.closeButtonText", new String());
        UIManager.put("DockTabbedPane.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE"));
        UIManager.put("DockTabbedPane.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE"));
        UIManager.put("DockTabbedPane.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE"));
        UIManager.put("DockTabbedPane.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK"));
        UIManager.put("DockTabbedPane.closeButtonText", new String());

        UIManager.put("DockViewTitleBar.titleFont", new JLabel().getFont());

        UIManager.put("DockViewTitleBar.isCloseButtonDisplayed", Boolean.FALSE);

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

        UIManager.put("DockViewTitleBar.menu.close", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close.rollover", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.close.pressed", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.close", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.hide", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.maximize", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.float", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif"));
        UIManager.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif"));

        UIManager.put("DockingDesktop.closeActionAccelerator", null);
        UIManager.put("DockingDesktop.maximizeActionAccelerator", null);
        UIManager.put("DockingDesktop.dockActionAccelerator", null);
        UIManager.put("DockingDesktop.floatActionAccelerator", null);

        UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage());
    }

    /**
     * Load icon from classpath.
     * 
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    private static ImageIcon getIcon(final String iconName) {
        return ResourcesUtil.getIcon("/org/omegat/gui/resources/" + iconName);
    }

    /**
     * Move window to the center of main window.
     * 
     * @param window
     *            window
     */
    public static void displayCentered(final Window window) {
        Point mLocation = Core.getMainWindow().getApplicationFrame().getLocation();
        Dimension mSize = Core.getMainWindow().getApplicationFrame().getSize();

        Point mCenter = new Point();
        mCenter.x = mLocation.x + mSize.width / 2;
        mCenter.y = mLocation.y + mSize.height / 2;

        Dimension dSize = window.getSize();
        Point dLocation = new Point();
        dLocation.x = mCenter.x - dSize.width / 2;
        dLocation.y = mCenter.y - dSize.height / 2;
        window.setLocation(dLocation);
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
