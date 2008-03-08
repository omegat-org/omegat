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

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;

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
        UIManager.put("DockViewTitleBar.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE")); // NOI18N
        UIManager.put("DockViewTitleBar.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE")); // NOI18N
        UIManager.put("DockViewTitleBar.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE")); // NOI18N
        UIManager.put("DockViewTitleBar.attachButtonText", OStrings.getString("DOCKING_HINT_DOCK")); // NOI18N
        UIManager.put("DockViewTitleBar.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK")); // NOI18N
        UIManager.put("DockViewTitleBar.closeButtonText", new String()); // NOI18N
        UIManager.put("DockTabbedPane.minimizeButtonText", OStrings.getString("DOCKING_HINT_MINIMIZE")); // NOI18N
        UIManager.put("DockTabbedPane.maximizeButtonText", OStrings.getString("DOCKING_HINT_MAXIMIZE")); // NOI18N
        UIManager.put("DockTabbedPane.restoreButtonText", OStrings.getString("DOCKING_HINT_RESTORE")); // NOI18N
        UIManager.put("DockTabbedPane.floatButtonText", OStrings.getString("DOCKING_HINT_UNDOCK")); // NOI18N
        UIManager.put("DockTabbedPane.closeButtonText", new String());

        UIManager.put("DockViewTitleBar.titleFont", new JLabel().getFont()); // NOI18N

        UIManager.put("DockViewTitleBar.isCloseButtonDisplayed", Boolean.FALSE);// NOI18N

        UIManager.put("DockViewTitleBar.hide", getIcon("minimize.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.hide.rollover", getIcon("minimize.rollover.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.hide.pressed", getIcon("minimize.pressed.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.maximize", getIcon("maximize.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("maximize.rollover.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("maximize.pressed.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.restore", getIcon("restore.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.restore.rollover", getIcon("restore.rollover.gif"));// NOI18N
        UIManager.put("DockViewTitleBar.restore.pressed", getIcon("restore.pressed.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.dock", getIcon("restore.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.dock.rollover", getIcon("restore.rollover.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.dock.pressed", getIcon("restore.pressed.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.float", getIcon("undock.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.float.rollover", getIcon("undock.rollover.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.float.pressed", getIcon("undock.pressed.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.attach", getIcon("dock.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.attach.rollover", getIcon("dock.rollover.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.attach.pressed", getIcon("dock.pressed.gif")); // NOI18N

        UIManager.put("DockViewTitleBar.menu.hide", getIcon("minimize.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.menu.maximize", getIcon("maximize.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.menu.restore", getIcon("restore.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.menu.dock", getIcon("restore.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.menu.float", getIcon("undock.gif")); // NOI18N
        UIManager.put("DockViewTitleBar.menu.attach", getIcon("dock.gif")); // NOI18N

        UIManager.put("DockViewTitleBar.menu.close", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.close", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.close.rollover", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.close.pressed", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.close", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.hide", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.maximize", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.float", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif")); // NOI18N
        UIManager.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif")); // NOI18N

        UIManager.put("DockingDesktop.closeActionAccelerator", null); // NOI18N
        UIManager.put("DockingDesktop.maximizeActionAccelerator", null); // NOI18N
        UIManager.put("DockingDesktop.dockActionAccelerator", null); // NOI18N
        UIManager.put("DockingDesktop.floatActionAccelerator", null); // NOI18N

        UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage()); // NOI18N
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
}
