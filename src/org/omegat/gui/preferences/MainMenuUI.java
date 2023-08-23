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

package org.omegat.gui.preferences;

import org.omegat.util.OStrings;

public class MainMenuUI implements IMenuPreferece {

    public final static String DEFAULT_MENUBAR = "org.omegat.gui.main.MainWindowMenu";

    @Override
    public String getMenuUIName() {
        return OStrings.getString("MENUUI_OMEGAT_DEFAULT_NAME");
    }

    @Override
    public String getMenuUIClassName() {
        return DEFAULT_MENUBAR;
    }

    @Override
    public String getToolbarClassName() {
        return null;
    }
}
