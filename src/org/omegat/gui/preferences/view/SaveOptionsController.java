/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2012 Didier Briel, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
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

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.omegat.core.data.CommandVarExpansion;
import org.omegat.core.statistics.StatOutputFormat;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class SaveOptionsController extends BasePreferencesController {

    private SaveOptionsPanel panel;

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
        return OStrings.getString("PREFS_TITLE_SAVING_AND_OUTPUT");
    }

    private void initGui() {
        panel = new SaveOptionsPanel();

        panel.insertButton.addActionListener(e -> panel.externalCommandTextArea
                .replaceSelection(panel.variablesList.getSelectedItem().toString()));
    }

    @Override
    protected void initFromPrefs() {
        int saveInterval = Preferences.getPreferenceDefault(Preferences.AUTO_SAVE_INTERVAL,
                Preferences.AUTO_SAVE_DEFAULT);

        panel.minutesSpinner.setValue(saveInterval / 60);
        panel.secondsSpinner.setValue(saveInterval % 60);

        panel.externalCommandTextArea.setText(Preferences.getPreference(Preferences.EXTERNAL_COMMAND));
        panel.allowProjectCmdCheckBox
                .setSelected(Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD));

        panel.variablesList.setModel(
                new DefaultComboBoxModel<>(new Vector<>(CommandVarExpansion.getCommandVariables())));

        int outputFormats = Preferences.getPreferenceDefault(Preferences.STATS_OUTPUT_FORMAT,
                StatOutputFormat.JSON.getId() | StatOutputFormat.TEXT.getId());
        panel.textOutputCheckBox.setSelected(StatOutputFormat.TEXT.isSelected(outputFormats));
        panel.jsonOutputCheckBox.setSelected(StatOutputFormat.JSON.isSelected(outputFormats));
        panel.xmlOutputCheckBox.setSelected(StatOutputFormat.XML.isSelected(outputFormats));
    }

    @Override
    public void restoreDefaults() {
        panel.minutesSpinner.setValue(Preferences.AUTO_SAVE_DEFAULT / 60);
        panel.secondsSpinner.setValue(Preferences.AUTO_SAVE_DEFAULT % 60);

        panel.externalCommandTextArea.setText("");
        panel.allowProjectCmdCheckBox.setSelected(false);

        int outputFormats = StatOutputFormat.getDefaultFormats();
        panel.textOutputCheckBox.setSelected(StatOutputFormat.TEXT.isSelected(outputFormats));
        panel.jsonOutputCheckBox.setSelected(StatOutputFormat.JSON.isSelected(outputFormats));
        panel.xmlOutputCheckBox.setSelected(StatOutputFormat.XML.isSelected(outputFormats));
    }

    @Override
    public void persist() {
        int saveMinutes = 0;
        int saveSeconds = 0;

        try {
            saveMinutes = Integer.parseInt(panel.minutesSpinner.getValue().toString());
        } catch (NumberFormatException nfe) {
            // Eat exception silently
        }

        try {
            saveSeconds = Integer.parseInt(panel.secondsSpinner.getValue().toString());
        } catch (NumberFormatException nfe) {
            // Eat exception silently
        }

        int saveInterval = saveMinutes * 60 + saveSeconds;

        if (saveInterval < 10) {
            saveInterval = 10; // 10 seconds minimum
        }

        Preferences.setPreference(Preferences.AUTO_SAVE_INTERVAL, saveInterval);

        Preferences.setPreference(Preferences.EXTERNAL_COMMAND, panel.externalCommandTextArea.getText());
        Preferences.setPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD,
                panel.allowProjectCmdCheckBox.isSelected());

        int outputFormats = 0;
        outputFormats |= panel.textOutputCheckBox.isSelected() ? StatOutputFormat.TEXT.getId() : 0;
        outputFormats |= panel.jsonOutputCheckBox.isSelected() ? StatOutputFormat.JSON.getId() : 0;
        outputFormats |= panel.xmlOutputCheckBox.isSelected() ? StatOutputFormat.XML.getId() : 0;
        Preferences.setPreference(Preferences.STATS_OUTPUT_FORMAT, outputFormats);
    }
}
