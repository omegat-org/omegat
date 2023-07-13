/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tokyo.northside.swing.TipOfTheDay;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Preferences;
import org.omegat.util.gui.MenuExtender;
import org.omegat.util.gui.MenuExtender.MenuKey;

public final class TipOfTheDayController {

    static final String INDEX_YAML = "tips.yaml";
    private static final String TIPOFTHEDAY_SHOW_ON_STARTUP = "tipoftheday_show_on_start";
    private static final String TIPOFTHEDAY_CURRENT_TIP = "tipoftheday_current_tip";

    @SuppressWarnings("unused")
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                if (TipOfTheDayUtils.hasIndex()) {
                    initUI();
                    // FIXME: disable temporary for 6.1 release
                    // initMenu();
                    SwingUtilities.invokeLater(() -> {
                        TipOfTheDayController.start(false);
                    });
                }
            }

            private void initUI() {
                ResourceBundle bundle = ResourceBundle.getBundle("org.omegat.gui.resources.TipOfTheDay");
                for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
                    String key = keys.nextElement();
                    UIManager.getDefaults().put(key, bundle.getObject(key));
                }
            }

            private void initMenu() {
                JMenuItem totdMenu = new JMenuItem();
                totdMenu.setText(UIManager.getDefaults().getString("TipOfTheDay.menuItemText"));
                totdMenu.setToolTipText(UIManager.getDefaults().getString("TipOfTheDay.menuToolTipText"));
                // force to show Tip of the Day dialog.
                totdMenu.addActionListener(actionEvent -> TipOfTheDayController.start(true));
                MenuExtender.addMenuItem(MenuKey.HELP, totdMenu);
            }

            @Override
            public void onApplicationShutdown() {
            }
        });
    }

    public static void unloadPlugins() {
    }

    private TipOfTheDayController() {
    }

    public static void start(final boolean force) {
        if (force) {
            showComponent();
        }
        // FIXME: temporary default to be false in 6.1 development.
        if (Preferences.isPreferenceDefault(TIPOFTHEDAY_SHOW_ON_STARTUP, false)) {
            showComponent();
        }
    }

    public static void showComponent() {
        TipOfTheDay totd = new TipOfTheDay(new OmegaTTipOfTheDayModel());
        String current = Preferences.getPreference(TIPOFTHEDAY_CURRENT_TIP);
        int currentTip = 0;
        if (current != null) {
            try {
                currentTip = (Integer.parseInt(current) + 1) % totd.getModel().getTipCount();
            } catch (NumberFormatException ignored) {
            }
        }
        totd.setCurrentTip(currentTip);

        TipOfTheDay.ShowOnStartupChoice choice = new TipOfTheDay.ShowOnStartupChoice() {
            @Override
            public void setShowingOnStartup(boolean showOnStartup) {
                Preferences.setPreference(TIPOFTHEDAY_SHOW_ON_STARTUP, showOnStartup);
            }

            @Override
            public boolean isShowingOnStartup() {
                return Preferences.isPreferenceDefault(TIPOFTHEDAY_SHOW_ON_STARTUP, true);
            }
        };
        JFrame mainFrame = Core.getMainWindow().getApplicationFrame();
        JDialog dialog = totd.getUI().createDialog(mainFrame, choice);
        dialog.setPreferredSize(new Dimension(900, 450));
        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();
        Preferences.setPreference(TIPOFTHEDAY_CURRENT_TIP, totd.getCurrentTip());
    }
}
