/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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

package org.omegat.gui.accesstool;

import org.omegat.gui.main.MainWindowBurgerMenu;
import org.omegat.gui.main.MainWindowMenu;
import org.omegat.gui.preferences.IMenuPreferece;
import org.omegat.util.Platform;
import org.omegat.util.gui.UIDesignManager;

public final class QuickAccess implements IMenuPreferece {

    public QuickAccess() {
    }

    public static void loadPlugins() {
        UIDesignManager.addMenuUIPreference(new QuickAccess());
    }

    public static void unloadPlugins(){
    }

    @Override
    public String getMenuUIName() {
                                return "Quick Access UI";
                                                         }

    @Override
    public String getMenuUIClassName() {
        if (Platform.isMacOSX()) {
            return MainWindowMenu.class.getName();
        }
        return MainWindowBurgerMenu.class.getName();
    }

    @Override
    public String getToolbarClassName() {
        if (Platform.isMacOSX()) {
            return AccessTools.class.getName();
        }
        return null;
    }
}
