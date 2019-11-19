/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.omegat.util.OStrings;

import com.vlsolutions.swing.docking.DefaultDockableContainerFactory;
import com.vlsolutions.swing.docking.DockViewTitleBar;

/**
 * A custom {@code DockableContainerFactory} to allow us to supply custom {@link DockViewTitleBar}s so that we can
 * insert custom buttons.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class CustomContainerFactory extends DefaultDockableContainerFactory {

    private static final Icon SETTINGS_ICON = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.active.png"));
    private static final Icon SETTINGS_ICON_INACTIVE = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.inactive.png"));
    private static final Icon SETTINGS_ICON_PRESSED = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.pressed.png"));

    @Override
    public DockViewTitleBar createTitleBar() {
        return new CustomTitleBar();
    }

    @SuppressWarnings("serial")
    private static class CustomTitleBar extends DockViewTitleBar {

        private JButton settingsButton;

        CustomTitleBar() {
            settingsButton = new JButton(SETTINGS_ICON_INACTIVE);
            settingsButton.setRolloverIcon(SETTINGS_ICON);
            settingsButton.setPressedIcon(SETTINGS_ICON_PRESSED);
            settingsButton.setToolTipText(OStrings.getString("DOCKING_HINT_SETTINGS"));

            // These values are set to match defaults in DockViewTitleBarUI
            settingsButton.setRolloverEnabled(true);
            settingsButton.setBorderPainted(false);
            settingsButton.setContentAreaFilled(false);
            settingsButton.setFocusable(false);
            settingsButton.setMargin(new Insets(0, 2, 0, 2));
            settingsButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

            settingsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    IPaneMenu callback = getSettingsCallback();
                    if (callback == null) {
                        return;
                    }
                    JPopupMenu menu = new JPopupMenu();
                    callback.populatePaneMenu(menu);
                    try {
                        menu.show(settingsButton, 0, 0);
                    } catch (IllegalComponentStateException ignore) {
                        ignore.printStackTrace();
                    }
                }
            });
        }

        private IPaneMenu getSettingsCallback() {
            return (IPaneMenu) getDockable().getDockKey().getProperty(IPaneMenu.PROPERTY_PANE_MENU_ACTION_LISTENER);
        }

        @Override
        public void finishLayout() {
            if (getSettingsCallback() != null) {
                // 4 is the number of default buttons:
                //   CloseButton, MaximizeOrRestoreButton, HideOrDockButton, Float Button
                // We want to insert before all of them, regardless of their visibility.
                add(settingsButton, getComponentCount() - 4);
            }
        }
    }
}
