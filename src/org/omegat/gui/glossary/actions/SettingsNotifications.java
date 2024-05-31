/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

package org.omegat.gui.glossary.actions;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import org.openide.awt.AbstractMnemonicsAction;

import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

public class SettingsNotifications extends AbstractMnemonicsAction {
    public SettingsNotifications() {
        super(OStrings.getString("GUI_GLOSSARYWINDOW_SETTINGS_NOTIFICATIONS"), OStrings.getLocale());
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        Object notify = e.getSource();
        if (notify instanceof JCheckBoxMenuItem) {
            Preferences.setPreference(Preferences.NOTIFY_GLOSSARY_HITS, ((JCheckBoxMenuItem) notify).isSelected());
        }
    }
}
