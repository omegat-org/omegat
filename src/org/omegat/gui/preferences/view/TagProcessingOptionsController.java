/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke
               2013 Aaron Madlon-Kay
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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.Core;
import org.omegat.core.statistics.StatisticsSettings;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;

/**
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class TagProcessingOptionsController extends BasePreferencesController {

    private TagProcessingOptionsPanel panel;

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
        return OStrings.getString("PREFS_TITLE_TAG_PROCESSING");
    }

    private void initGui() {
        panel = new TagProcessingOptionsPanel();
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                validate();
                checkReloadRequired();
            }
        };
        panel.cbCountingProtectedText.addActionListener(e -> checkReloadRequired());
        panel.removePatternRegExpTF.getDocument().addDocumentListener(docListener);
        panel.customPatternRegExpTF.getDocument().addDocumentListener(docListener);
    }

    private void checkReloadRequired() {
        boolean customPatternChanged = valueIsDifferent(Preferences.CHECK_CUSTOM_PATTERN,
                panel.customPatternRegExpTF.getText());
        boolean removePatternChanged = valueIsDifferent(Preferences.CHECK_REMOVE_PATTERN,
                panel.removePatternRegExpTF.getText());
        boolean statsHandlingChanged = (StatisticsSettings.isCountingProtectedText()
                || StatisticsSettings.isCountingCustomTags()) != panel.cbCountingProtectedText.isSelected();
        setReloadRequired(customPatternChanged || removePatternChanged || statsHandlingChanged);
    }

    @Override
    protected void initFromPrefs() {
        panel.noCheckRadio.setSelected(Preferences.isPreferenceDefault(Preferences.DONT_CHECK_PRINTF_TAGS,
                Preferences.DONT_CHECK_PRINTF_TAGS_DEFAULT));
        panel.simpleCheckRadio.setSelected(Preferences.isPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS));
        panel.fullCheckRadio.setSelected(Preferences.isPreference(Preferences.CHECK_ALL_PRINTF_TAGS));
        panel.javaPatternCheckBox.setSelected(Preferences.isPreference(Preferences.CHECK_JAVA_PATTERN_TAGS));
        panel.cbCountingProtectedText.setSelected(
                StatisticsSettings.isCountingProtectedText() || StatisticsSettings.isCountingCustomTags());
        panel.customPatternRegExpTF.setText(Preferences.getPreferenceDefault(Preferences.CHECK_CUSTOM_PATTERN, 
                PatternConsts.CHECK_CUSTOM_PATTERN_DEFAULT));
        panel.removePatternRegExpTF.setText(Preferences.getPreference(Preferences.CHECK_REMOVE_PATTERN));
        panel.looseTagOrderCheckBox.setSelected(Preferences.isPreference(Preferences.LOOSE_TAG_ORDERING));
        panel.cbTagsValidRequired.setSelected(Preferences.isPreference(Preferences.TAGS_VALID_REQUIRED));
    }

    @Override
    public void restoreDefaults() {
        panel.noCheckRadio.setSelected(Preferences.DONT_CHECK_PRINTF_TAGS_DEFAULT);
        panel.simpleCheckRadio.setSelected(false);
        panel.fullCheckRadio.setSelected(false);
        panel.javaPatternCheckBox.setSelected(false);
        panel.customPatternRegExpTF.setText(PatternConsts.CHECK_CUSTOM_PATTERN_DEFAULT);
        panel.removePatternRegExpTF.setText("");
        panel.looseTagOrderCheckBox.setSelected(false);
        panel.cbTagsValidRequired.setSelected(false);
        @SuppressWarnings("unused")
        boolean countingProtectedTextDefault = Preferences.STAT_COUNTING_PROTECTED_TEXT_DEFAULT
                || Preferences.STAT_COUNTING_CUSTOM_TAGS_DEFAULT;
        panel.cbCountingProtectedText.setSelected(countingProtectedTextDefault);
    }

    @Override
    public boolean validate() {
        boolean result = true;
        String customPatternMessage = null;
        String removePatternMessage = null;
        try {
            Pattern.compile(panel.customPatternRegExpTF.getText());
        } catch (PatternSyntaxException e) {
            result = false;
            customPatternMessage = e.getLocalizedMessage();
        }
        try {
            Pattern.compile(panel.removePatternRegExpTF.getText());
        } catch (PatternSyntaxException e) {
            result = false;
            removePatternMessage = e.getLocalizedMessage();
        }
        panel.customPatternWarningTextArea.setText(customPatternMessage);
        panel.removePatternWarningTextArea.setText(removePatternMessage);
        return result;
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.DONT_CHECK_PRINTF_TAGS, panel.noCheckRadio.isSelected());
        Preferences.setPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS, panel.simpleCheckRadio.isSelected());
        Preferences.setPreference(Preferences.CHECK_ALL_PRINTF_TAGS, panel.fullCheckRadio.isSelected());
        Preferences.setPreference(Preferences.CHECK_JAVA_PATTERN_TAGS, panel.javaPatternCheckBox.isSelected());
        Preferences.setPreference(Preferences.CHECK_CUSTOM_PATTERN, panel.customPatternRegExpTF.getText());
        Preferences.setPreference(Preferences.CHECK_REMOVE_PATTERN, panel.removePatternRegExpTF.getText());
        Preferences.setPreference(Preferences.LOOSE_TAG_ORDERING, panel.looseTagOrderCheckBox.isSelected());
        Preferences.setPreference(Preferences.TAGS_VALID_REQUIRED, panel.cbTagsValidRequired.isSelected());
        StatisticsSettings.setCountingProtectedText(panel.cbCountingProtectedText.isSelected());
        StatisticsSettings.setCountingCustomTags(panel.cbCountingProtectedText.isSelected());
        PatternConsts.updatePlaceholderPattern();
        PatternConsts.updateRemovePattern();
        PatternConsts.updateCustomTagPattern();
        SwingUtilities.invokeLater(() -> Core.getEditor().getSettings().updateTagValidationPreferences());
    }
}
