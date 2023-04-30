/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.omegat.core.Core;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author Aaron Madlon-Kay
 */
public class AutoCompleterController extends BasePreferencesController {

    private AutoCompleterPreferencesPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_AUTOCOMPLETER");
    }

    private void initGui() {
        panel = new AutoCompleterPreferencesPanel();
        int mask = Java8Compat.getMenuShortcutKeyMaskEx();
        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask);
        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mask);
        panel.switchWithLRCheckBox.setText(OStrings.getString("PREFS_AUTOCOMPLETE_SWITCH_VIEWS_LR",
                StaticUIUtils.getKeyStrokeText(left), StaticUIUtils.getKeyStrokeText(right)));
        panel.switchWithLRCheckBox.setVisible(Platform.isMacOSX());
    }

    @Override
    protected void initFromPrefs() {
        panel.automaticCheckBox.setSelected(Preferences.isPreference(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY));
        panel.switchWithLRCheckBox.setSelected(Preferences.isPreference(Preferences.AC_SWITCH_VIEWS_WITH_LR));
    }

    @Override
    public void restoreDefaults() {
        panel.automaticCheckBox.setSelected(false);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY, panel.automaticCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_SWITCH_VIEWS_WITH_LR, panel.switchWithLRCheckBox.isSelected());
        Core.getEditor().getAutoCompleter().resetKeys();
    }
}
