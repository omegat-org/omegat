/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko
               2015 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
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
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public class GlossaryAutoCompleterOptionsController extends BasePreferencesController {

    private GlossaryAutoCompleterOptionsPanel panel;

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
        return OStrings.getString("PREFS_TITLE_AUTOCOMPLETER_GLOSSARY");
    }

    private void initGui() {
        panel = new GlossaryAutoCompleterOptionsPanel();
        panel.displaySourceCheckBox
                .addActionListener(e -> activateSourceItems(panel.displaySourceCheckBox.isSelected()));
        panel.enabledCheckBox.addActionListener(e -> updateEnabledness());
    }

    private void activateSourceItems(boolean activate) {
        panel.sortBySourceCheckBox.setEnabled(activate);
        panel.sourceFirstRadioButton.setEnabled(activate);
        panel.targetFirstRadioButton.setEnabled(activate);
    }

    @Override
    protected void initFromPrefs() {
        panel.displaySourceCheckBox.setSelected(Preferences.isPreference(Preferences.AC_GLOSSARY_SHOW_SOURCE));
        panel.sourceFirstRadioButton
                .setSelected(!Preferences.isPreference(Preferences.AC_GLOSSARY_SHOW_TARGET_BEFORE_SOURCE));
        panel.targetFirstRadioButton
                .setSelected(Preferences.isPreference(Preferences.AC_GLOSSARY_SHOW_TARGET_BEFORE_SOURCE));
        panel.sortBySourceCheckBox.setSelected(Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_BY_SOURCE));
        panel.longerFirstCheckBox.setSelected(Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_BY_LENGTH));
        panel.sortEntriesCheckBox.setSelected(Preferences.isPreference(Preferences.AC_GLOSSARY_SORT_ALPHABETICALLY));
        panel.enabledCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.AC_GLOSSARY_ENABLED,
                Preferences.AC_GLOSSARY_ENABLED_DEFAULT));

        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        panel.displaySourceCheckBox.setSelected(false);
        panel.sourceFirstRadioButton.setSelected(true);
        panel.targetFirstRadioButton.setSelected(false);
        panel.sortBySourceCheckBox.setSelected(false);
        panel.longerFirstCheckBox.setSelected(false);
        panel.sortEntriesCheckBox.setSelected(false);
        panel.enabledCheckBox.setSelected(Preferences.AC_GLOSSARY_ENABLED_DEFAULT);

        updateEnabledness();
    }

    private void updateEnabledness() {
        activateSourceItems(panel.displaySourceCheckBox.isSelected());
        StaticUIUtils.setHierarchyEnabled(panel.optionsPanel, panel.enabledCheckBox.isSelected());
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.AC_GLOSSARY_SHOW_SOURCE, panel.displaySourceCheckBox.isSelected());
        if (panel.displaySourceCheckBox.isSelected()) {
            Preferences.setPreference(Preferences.AC_GLOSSARY_SHOW_TARGET_BEFORE_SOURCE,
                    panel.targetFirstRadioButton.isSelected());
            Preferences.setPreference(Preferences.AC_GLOSSARY_SORT_BY_SOURCE, panel.sortBySourceCheckBox.isSelected());
        }
        Preferences.setPreference(Preferences.AC_GLOSSARY_SORT_BY_LENGTH, panel.longerFirstCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_GLOSSARY_SORT_ALPHABETICALLY, panel.sortEntriesCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_GLOSSARY_ENABLED, panel.enabledCheckBox.isSelected());
    }
}
