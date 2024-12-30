/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2007 Zoltan Bartko
 *                2008-2011 Didier Briel
 *                2012 Martin Fleurke, Didier Briel
 *                2015 Aaron Madlon-Kay
 *                2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences.view;

import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.DictionaryManager;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author Zoltan Bartko
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class SpellcheckerConfigurationController extends BasePreferencesController {

    private SpellcheckerConfigurationPanel panel;

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
        return OStrings.getString("PREFS_TITLE_SPELLCHECKER");
    }

    private void initGui() {
        panel = new SpellcheckerConfigurationPanel();

        panel.autoSpellcheckCheckBox.addActionListener(e -> updateDetailPanel());

        panel.directoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                directoryChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                directoryChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                directoryChanged();
            }
        });

        panel.directoryTextField.addActionListener(e -> updateLanguageList());

        panel.directoryChooserButton.addActionListener(e -> chooseDirectory());

        panel.languageList.setModel(new DefaultListModel<>());
    }

    private void chooseDirectory() {
        // open a dialog box to choose the directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(OStrings.getString("GUI_SPELLCHECKER_FILE_CHOOSER_TITLE"));
        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            // we should write the result into the directory text field
            File file = fileChooser.getSelectedFile();
            panel.directoryTextField.setText(file.getAbsolutePath());
        }
    }

    private File getDictDir() {
        String dirName = panel.directoryTextField.getText();

        if (StringUtil.isEmpty(dirName)) {
            return null;
        }

        File dir = new File(dirName);
        if (dir.isFile() || (dir.exists() && !dir.canRead())) {
            return null;
        }

        return dir;
    }

    private void directoryChanged() {
        updateLanguageList();
    }

    /**
     * Updates the state of the detail panel based on the check box state
     */
    private void updateDetailPanel() {
        StaticUIUtils.setHierarchyEnabled(panel.detailPanel, panel.autoSpellcheckCheckBox.isSelected());
        directoryChanged();
    }

    /**
     * Updates the language list based on the directory text field
     */
    private void updateLanguageList() {
        DefaultListModel<String> languageListModel = (DefaultListModel<String>) panel.languageList.getModel();

        // initialize the language list model
        languageListModel.clear();

        File dir = getDictDir();
        if (dir == null) {
            return;
        }

        DictionaryManager dicMan = new DictionaryManager(dir);
        dicMan.getLocalDictionaryNameList().stream().sorted().forEach(languageListModel::addElement);
    }

    protected Language getCurrentLanguage() {
        if (Core.getProject().isProjectLoaded()) {
            return Core.getProject().getProjectProperties().getTargetLanguage();
        } else {
            return new Language(Preferences.getPreference(Preferences.TARGET_LOCALE));
        }
    }

    @Override
    protected void initFromPrefs() {
        panel.autoSpellcheckCheckBox
                .setSelected(Preferences.isPreferenceDefault(Preferences.ALLOW_AUTO_SPELLCHECKING, true));
        // initialize things from the preferences
        updateDetailPanel();
        directoryChanged();

        String dictDirPath = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellCheckerManager.getDefaultDictionaryDir().getPath());
        panel.directoryTextField.setText(dictDirPath);

        // Create dict dir if it doesn't exist, so user can install immediately
        File dictDir = new File(dictDirPath);
        if (!dictDir.exists()) {
            dictDir.mkdirs();
        }
    }

    @Override
    public void restoreDefaults() {
        panel.autoSpellcheckCheckBox.setSelected(true);
        directoryChanged();
        File dictDir = SpellCheckerManager.getDefaultDictionaryDir();
        panel.directoryTextField.setText(dictDir.getPath());
        // Create dict dir if it doesn't exist, so user can install immediately
        if (!dictDir.exists()) {
            dictDir.mkdirs();
        }
    }

    @Override
    public void persist() {
        boolean isNeedToSpell = panel.autoSpellcheckCheckBox.isSelected();
        Preferences.setPreference(Preferences.ALLOW_AUTO_SPELLCHECKING, isNeedToSpell);
        Preferences.setPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                panel.directoryTextField.getText());
        if (isNeedToSpell && Core.getProject().isProjectLoaded()) {
            ISpellChecker sc = Core.getSpellChecker();
            sc.destroy();
            sc.initialize();
        }
        SwingUtilities.invokeLater(() -> Core.getEditor().getSettings().setAutoSpellChecking(isNeedToSpell));
    }
}
