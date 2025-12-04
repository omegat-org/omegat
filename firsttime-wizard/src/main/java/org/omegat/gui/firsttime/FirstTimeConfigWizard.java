/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.firsttime;

import java.util.Collections;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Preferences;
import org.omegat.util.gui.MenuExtender;
import org.openide.awt.Mnemonics;

@SuppressWarnings("unused")
public final class FirstTimeConfigWizard {

    private static final FirstTimeConfigWizardModuleListener LISTENER = new FirstTimeConfigWizardModuleListener();

    private FirstTimeConfigWizard() {}

    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(LISTENER);
    }

    public static void unloadPlugins() {
        CoreEvents.unregisterApplicationEventListener(LISTENER);
    }

    public static class FirstTimeConfigWizardModuleListener implements IApplicationEventListener {

        private @Nullable JMenuItem menuItem;

        @Override
        public void onApplicationStartup() {
            initMenu();
            // Show wizard once if not suppressed
            boolean suppressWizard = Boolean.parseBoolean(
                    Preferences.getPreferenceDefault(Preferences.FIRST_TIME_WIZARD_DONE, "false"));
            if (Preferences.isFirstRun() && !suppressWizard) {
                SwingUtilities.invokeLater(() -> {
                    FirstTimeConfigWizardDialog dlg = new FirstTimeConfigWizardDialog(
                            Core.getMainWindow().getApplicationFrame());
                    dlg.setVisible(true);
                    // Restart OmegaT only when required by changes made in the wizard.
                    if (dlg.isFinished() && dlg.isRestartRequired()) {
                        // If no project is loaded, skip any confirmation dialogs.
                        boolean noProjectLoaded = !Core.getProject().isProjectLoaded();
                        ProjectUICommands.projectRestart(null, noProjectLoaded);
                    }
                });
            }
        }

        private void initMenu() {
            menuItem = new JMenuItem();
            Mnemonics.setLocalizedText(menuItem, FirstTimeConfigurationWizardUtil.getString("menu.firsttimewizard", "First Time Configuration..."));
            menuItem.addActionListener(e -> {
                FirstTimeConfigWizardDialog dlg = new FirstTimeConfigWizardDialog(
                        Core.getMainWindow().getApplicationFrame());
                dlg.setVisible(true);
                if (dlg.isFinished()) {
                    if (dlg.isRestartRequired()) {
                        boolean noProjectLoaded = !Core.getProject().isProjectLoaded();
                        ProjectUICommands.projectRestart(null, noProjectLoaded);
                    }
                }
            });
            MenuExtender.addMenuItem(MenuExtender.MenuKey.HELP, menuItem);
        }

        @Override
        public void onApplicationShutdown() {
            if (menuItem != null) {
                MenuExtender.removeMenuItems(MenuExtender.MenuKey.HELP, Collections.singletonList(menuItem));
            }
        }
    }
}
