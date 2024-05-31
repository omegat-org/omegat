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

import javax.swing.Action;

import org.openide.awt.AbstractMnemonicsAction;

import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

@SuppressWarnings("serial")
public class SettingsOpenFileAction extends AbstractMnemonicsAction {
    public SettingsOpenFileAction() {
        super(OStrings.getString("GUI_GLOSSARYWINDOW_SETTINGS_OPEN_FILE"), OStrings.getLocale());
        final String key = "projectAccessWriteableGlossaryMenuItem";
        putValue(Action.ACTION_COMMAND_KEY, key);
        putValue(Action.ACCELERATOR_KEY, PropertiesShortcuts.getMainMenuShortcuts().getKeyStrokeOrNull(key));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        Log.logInfoRB("LOG_MENU_CLICK", e.getActionCommand());
        int modifier = e.getModifiers();
        ProjectUICommands.openWritableGlossaryFile((modifier & ActionEvent.ALT_MASK) != modifier);
    }
}
