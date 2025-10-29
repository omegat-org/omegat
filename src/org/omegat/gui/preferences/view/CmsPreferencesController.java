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
package org.omegat.gui.preferences.view;

import org.omegat.gui.preferences.BasePreferencesController;

import java.awt.Component;
import java.util.ArrayList;

public class CmsPreferencesController extends BasePreferencesController {

    private CmsPreferencesPanel panel;

    @Override
    protected void initFromPrefs() {
        if (panel != null) {
            panel.loadFromPrefs();
        }
    }

    @Override
    public String toString() {
        return "CMS";
    }

    @Override
    public Component getGui() {
        if (panel == null) {
            panel = new CmsPreferencesPanel();
            panel.loadFromPrefs();
        }
        return panel;
    }

    @Override
    public void persist() {
        if (panel != null) {
            panel.saveToPrefs();
        }
        setReloadRequired(false);
        setRestartRequired(false);
    }

    @Override
    public void restoreDefaults() {
        if (panel != null) {
            panel.setTargets(new ArrayList<>());
            panel.saveToPrefs();
        }
    }
}
