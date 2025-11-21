/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2025 Hiroshi Miura.
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
package org.omegat.gui.tipoftheday;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.gui.MenuExtender;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

@NullMarked
public final class TipOfTheDayModule {

    static final boolean ENABLED = true;

    private static @Nullable TipOfTheDayModuleListener listener;

    private TipOfTheDayModule() {}

    @SuppressWarnings("unused")
    public static void loadPlugins() {
        listener = new TipOfTheDayModuleListener();
        CoreEvents.registerApplicationEventListener(listener);
    }

    @SuppressWarnings("unused")
    public static void unloadPlugins() {
        if (listener != null) {
            CoreEvents.unregisterApplicationEventListener(listener);
            listener = null;
        }
    }

    public static class TipOfTheDayModuleListener implements IApplicationEventListener {

        private final TipOfTheDayController controller = new TipOfTheDayController();
        private @Nullable JMenuItem totdMenu;

        @Override
        public void onApplicationStartup() {
            if (ENABLED && TipOfTheDayUtils.hasIndex()) {
                initUI();
                initMenu();
                SwingUtilities.invokeLater(() -> controller.start(false));
            }
        }

        private void initUI() {
            ResourceBundle bundle = ResourceBundle.getBundle("org.omegat.gui.tipoftheday.Bundle");
            UIDefaults defaults = UIManager.getDefaults();
            for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
                String key = keys.nextElement();
                defaults.put(key, bundle.getObject(key));
            }
            defaults.put("TipOfTheDay.background", defaults.getColor("TextPane.background"));
            defaults.put("TipOfTheDay.foreground", defaults.getColor("TextPane.foreground"));
            defaults.put("TipOfTheDay.borderColor", defaults.getColor("TextPane.borderColor"));
        }

        private void initMenu() {
            totdMenu = new JMenuItem();
            totdMenu.setText(UIManager.getDefaults().getString("TipOfTheDay.menuItemText"));
            totdMenu.setToolTipText(UIManager.getDefaults().getString("TipOfTheDay.menuToolTipText"));
            // show Tip of the Day dialog on startup.
            totdMenu.addActionListener(actionEvent -> controller.start(true));
            MenuExtender.addMenuItem(MenuExtender.MenuKey.HELP, totdMenu);
        }

        @Override
        public void onApplicationShutdown() {
            if (ENABLED) {
                MenuExtender.removeMenuItems(MenuExtender.MenuKey.HELP, Collections.singletonList(totdMenu));
            }
        }
    }
}
