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

package org.omegat.gui.preferences.view;

import javax.swing.JComponent;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Aaron Madlon-Kay
 */
public class DictionaryPreferencesController extends BasePreferencesController {

    private DictionaryPreferencesPanel panel;

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
        return OStrings.getString("PREFS_TITLE_DICTIONARY");
    }

    private void initGui() {
        panel = new DictionaryPreferencesPanel();
    }

    @Override
    protected void initFromPrefs() {
        panel.fuzzyMatchingCheckBox.setSelected(Preferences.isPreference(Preferences.DICTIONARY_FUZZY_MATCHING));
        panel.autoCheckSegmentsCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.DICTIONARY_AUTO_SEARCH,
                false));
        panel.condensedViewCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.DICTIONARY_CONDENSED_VIEW,
                false));
    }

    @Override
    public void restoreDefaults() {
        panel.fuzzyMatchingCheckBox.setSelected(false);
        panel.autoCheckSegmentsCheckBox.setSelected(false);
        panel.condensedViewCheckBox.setSelected(false);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.DICTIONARY_FUZZY_MATCHING, panel.fuzzyMatchingCheckBox.isSelected());
        Preferences.setPreference(Preferences.DICTIONARY_AUTO_SEARCH, panel.autoCheckSegmentsCheckBox.isSelected());
        Preferences.setPreference(Preferences.DICTIONARY_CONDENSED_VIEW, panel.condensedViewCheckBox.isSelected());
    }
}
