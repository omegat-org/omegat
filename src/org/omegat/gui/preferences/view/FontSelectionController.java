/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel
               2016 Aaron Madlon-Kay
               2023 Hiroshi Miura
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

import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.plaf.FontUIResource;

import org.omegat.core.CoreEvents;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.FontUtil;
import org.omegat.util.gui.UIScale;

/**
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class FontSelectionController extends BasePreferencesController {

    private FontSelectionPanel panel;

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
        return OStrings.getString("PREFS_TITLE_FONT");
    }

    private void initGui() {
        panel = new FontSelectionPanel();
        panel.fontComboBox.setModel(new DefaultComboBoxModel<>(StaticUtils.getFontNames()));
        panel.fontComboBox.addActionListener(e -> panel.previewTextArea.setFont(getSelectedFont()));
        panel.sizeSpinner.addChangeListener(e -> panel.previewTextArea.setFont(getSelectedFont()));
        panel.applyToDictionaryPaneCheckBox.addChangeListener(e -> {
            if (panel.applyToDictionaryPaneCheckBox.isSelected()) {
                panel.sizeDictionarySpinner.setValue(panel.sizeSpinner.getValue());
                panel.sizeDictionarySpinner.setEnabled(false);
            } else {
                panel.sizeDictionarySpinner.setEnabled(true);
            }
        });
    }

    @Override
    protected void initFromPrefs() {
        String configuredFontName = FontUtil.getConfiguredFontName();
        int confguredFontSize = FontUtil.getConfiguredFontSize();
        panel.fontComboBox.setSelectedItem(configuredFontName);
        panel.sizeSpinner.setValue(confguredFontSize);
        panel.previewTextArea.setFont(getSelectedFont());
        int dictionaryFontSize = Preferences.getPreferenceDefault(Preferences.TF_DICTIONARY_FONT_SIZE,
                confguredFontSize);
        panel.sizeDictionarySpinner.setValue(dictionaryFontSize);
        panel.applyToProjectFilesCheckBox
                .setSelected(Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT));
        panel.applyToDictionaryPaneCheckBox
                .setSelected(Preferences.isPreferenceDefault(Preferences.DICTIONARY_USE_FONT, true));
        if (panel.applyToDictionaryPaneCheckBox.isSelected()) {
            panel.sizeDictionarySpinner.setValue(panel.sizeSpinner.getValue());
            panel.sizeDictionarySpinner.setEnabled(false);
        }
    }

    @Override
    public void restoreDefaults() {
        Font oldFont = FontUtil.getDefaultFont();
        int fontSize = oldFont.getSize();
        panel.fontComboBox.setSelectedItem(oldFont.getName());
        panel.sizeSpinner.setValue(fontSize);
        panel.previewTextArea.setFont(getSelectedFont());
        panel.sizeDictionarySpinner.setValue(fontSize);
        panel.applyToProjectFilesCheckBox.setSelected(false);
        panel.applyToDictionaryPaneCheckBox.setSelected(true);
        panel.sizeDictionarySpinner.setEnabled(false);
    }

    /**
     * Create scaled composite font.
     * @return
     */
    private FontUIResource getSelectedFont() {
        return FontUtil.createCompositeFont((String) panel.fontComboBox.getSelectedItem(), Font.PLAIN,
                UIScale.scale(((Number) panel.sizeSpinner.getValue()).intValue()));
    }

    @Override
    public void persist() {
        boolean applyToProjFiles = panel.applyToProjectFilesCheckBox.isSelected();
        boolean applyToDicitonaryPane = panel.applyToDictionaryPaneCheckBox.isSelected();
        String newFontName = (String) panel.fontComboBox.getSelectedItem();
        int newFontSize = ((Number) panel.sizeSpinner.getValue()).intValue();
        if (!FontUtil.getConfiguredFontName().equals(newFontName)
                || newFontSize != FontUtil.getConfiguredFontSize()
                || applyToProjFiles != Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)
                || applyToDicitonaryPane != Preferences.isPreference(Preferences.DICTIONARY_USE_FONT)) {
            Preferences.setPreference(Preferences.PROJECT_FILES_USE_FONT, applyToProjFiles);
            Preferences.setPreference(Preferences.TF_SRC_FONT_NAME, newFontName);
            Preferences.setPreference(Preferences.DICTIONARY_USE_FONT, applyToDicitonaryPane);
            Preferences.setPreference(Preferences.TF_SRC_FONT_SIZE, newFontSize);
            if (applyToDicitonaryPane) {
                Preferences.setPreference(Preferences.TF_DICTIONARY_FONT_SIZE, newFontSize);
            } else {
                Preferences.setPreference(Preferences.TF_DICTIONARY_FONT_SIZE,
                        panel.sizeDictionarySpinner.getValue());
            }
            CoreEvents.fireFontChanged(getSelectedFont());
        }
    }
}
