/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2021 Hiroshi Miura
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.IMenuPreferece;
import org.omegat.gui.preferences.MainMenuUI;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.UIDesignManager;

/**
 * @author Aaron Madlon-Kay
 */
public class AppearanceController extends BasePreferencesController {

    private AppearancePreferencesPanel panel;
    private boolean previousFileListDialog;

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
        return OStrings.getString("PREFS_TITLE_APPEARANCE");
    }

    private void initGui() {
        panel = new AppearancePreferencesPanel();
        String[] lafs = Arrays.asList(UIManager.getInstalledLookAndFeels()).stream().map(LookAndFeelInfo::getClassName)
                .toArray(String[]::new);
        panel.cbThemeSelect.setModel(new DefaultComboBoxModel<>(lafs));
        panel.cbThemeSelect.setRenderer(new DelegatingComboBoxRenderer<String, String>() {
            @Override
            protected String getDisplayText(String value) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getClassName().equals(value)) {
                        return info.getName();
                    }
                }
                return value;
            }
        });

        List<IMenuPreferece> menuStylePrefs = new ArrayList<>();
        menuStylePrefs.add(new MainMenuUI());
        menuStylePrefs.addAll(UIDesignManager.getMenuUIPreferences());
        String[] menuStyles = menuStylePrefs.stream()
                .map(clazz -> clazz.getClass().getName())
                .toArray(String[]::new);
        panel.cbMenustyleSelect.setModel(new DefaultComboBoxModel<>(menuStyles));
        panel.cbMenustyleSelect.setRenderer(new DelegatingComboBoxRenderer<String, String>() {
            @Override
            protected String getDisplayText(final String value) {
                return menuStylePrefs.stream()
                        .filter(p -> p.getClass().getName().equals(value))
                        .map(IMenuPreferece::getMenuUIName)
                        .findFirst().orElse("");
            }
        });

        panel.cbThemeSelect.addActionListener(e -> {
            setRestartRequired(isModified());
        });
        panel.cbMenustyleSelect.addActionListener(e -> {
            setRestartRequired(isModified());
        });
        // TODO: Properly abstract the restore function
        panel.restoreWindowButton
                .addActionListener(e -> MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()));
    }

    private boolean isModified() {
        boolean modified = false;
        Object selected = panel.cbThemeSelect.getSelectedItem();
        if (selected != null) {
            modified |= !UIManager.getLookAndFeel().getClass().getName().equals(selected.toString());
        }
        selected = panel.cbMenustyleSelect.getSelectedItem();
        if (selected != null) {
            modified |= !selected.toString().equals(Preferences.getPreference(Preferences.MENUUI_CLASS_NAME));
        }
        return modified;
    }

    @Override
    protected void initFromPrefs() {
        panel.cbThemeSelect.setSelectedItem(UIManager.getLookAndFeel().getClass().getName());
        panel.cbMenustyleSelect.setSelectedItem(Preferences.getPreferenceDefault(Preferences.MENUUI_CLASS_NAME,
                MainMenuUI.class.getName()));
        previousFileListDialog = Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD,
                true);
    }

    @Override
    public void restoreDefaults() {
        panel.cbThemeSelect.setSelectedItem(Preferences.THEME_CLASS_NAME_DEFAULT);
        panel.cbMenustyleSelect.setSelectedItem(MainMenuUI.class.getName());
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, previousFileListDialog);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.THEME_CLASS_NAME, panel.cbThemeSelect.getSelectedItem().toString());
        Preferences.setPreference(Preferences.MENUUI_CLASS_NAME,
                panel.cbMenustyleSelect.getSelectedItem().toString());
    }
}
