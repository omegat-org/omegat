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
    private static ImageIcon getIcon(String iconName) {
        return new ImageIcon(DockingUI.class.getResource("/org/omegat/gui/resources/" + // NOI18N
                iconName));
    }
}
