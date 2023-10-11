/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2021,2023 Hiroshi Miura
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
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.IMenuPreferece;
import org.omegat.gui.preferences.MainMenuUI;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.UIDesignManager;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class AppearanceController extends BasePreferencesController {

    private AppearancePreferencesPanel panel;
    private boolean previousFileListDialog;

    private List<LookAndFeelInfo> darkThemeList = new ArrayList<>();
    private List<LookAndFeelInfo> lightThemeList = new ArrayList<>();
    private final DelegatingComboBoxRenderer<String, String> renderer;

    private String preferredLightThemeClass;
    private String preferredDarkThemeClass;
    private final String themeMode;

    public AppearanceController() {
        themeMode = Preferences.getPreferenceDefault(Preferences.THEME_COLOR_MODE, "default");
        renderer = new DelegatingComboBoxRenderer<>() {
            @Override
            protected String getDisplayText(final String value) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getClassName().equals(value)) {
                        return info.getName();
                    }
                }
                return value;
            }
        };
    }

    // FIXME: we should not check class name but use some API.
    private static boolean isDarkTheme(LookAndFeelInfo info) {
        return info.getClassName().contains("Dark") || info.getClassName().contains("Darcula")
                || info.getClassName().contains("HighContrast") || info.getClassName().contains("Carbon")
                || info.getClassName().contains("Dracula") || info.getClassName().contains("Midnight")
                || info.getClassName().contains("Monocai");
    }

    private static boolean isLightTheme(LookAndFeelInfo info) {
        return !isDarkTheme(info);
    }

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initThemeLists();
            initGui();
            initFromPrefs();
            initListeners();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_APPEARANCE");
    }

    private void initThemeLists() {
        List<LookAndFeelInfo> themes = Arrays.asList(UIManager.getInstalledLookAndFeels());
        lightThemeList = themes.stream().filter(AppearanceController::isLightTheme)
                .collect(Collectors.toList());
        darkThemeList = themes.stream().filter(AppearanceController::isDarkTheme)
                .collect(Collectors.toList());
    }

    private void initGui() {
        panel = new AppearancePreferencesPanel();

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
        panel.cbMenustyleSelect.addActionListener(e -> {
            setRestartRequired(isModified());
        });

        String[] lightLafs = lightThemeList.stream().map(LookAndFeelInfo::getClassName)
                .toArray(String[]::new);
        panel.cbLightThemeSelect.setModel(new DefaultComboBoxModel<>(lightLafs));
        panel.cbLightThemeSelect.setRenderer(renderer);
        String[] darkLafs = darkThemeList.stream().map(LookAndFeelInfo::getClassName).toArray(String[]::new);
        panel.cbDarkThemeSelect.setModel(new DefaultComboBoxModel<>(darkLafs));
        panel.cbDarkThemeSelect.setRenderer(renderer);
        panel.themePanel.setBorder(BorderFactory.createTitledBorder(
                OStrings.getString("MW_OPTIONMENU_APPEARANCE_THEME_LABEL")));
        panel.menustylePanel.setBorder(BorderFactory.createTitledBorder(
                OStrings.getString("MW_OPTIONMENU_APPEARANCE_MENUSTYLE_LABEL")));
        Mnemonics.setLocalizedText(panel.useLightDefaultThemeRB,
                OStrings.getString("MW_OPTIONMENU_APPEARANCE_LIGHT_THEME_LABEL"));
        Mnemonics.setLocalizedText(panel.useDarkThemeRB,
                OStrings.getString("MW_OPTIONMENU_APPEARANCE_DARK_THEME_LABEL"));
        Mnemonics.setLocalizedText(panel.syncWithOSColorRB,
                OStrings.getString("MW_OPTIONMENU_APPEARANCE_SYNC_WITH_OS_COLOR"));
        if (Platform.isLinux()) {
            // we don't support Sync OS color mode in linux
            panel.syncWithOSColorRB.setEnabled(false);
        }
    }

    @Override
    protected void initFromPrefs() {
        panel.cbMenustyleSelect.setSelectedItem(Preferences.getPreferenceDefault(Preferences.MENUUI_CLASS_NAME,
                MainMenuUI.class.getName()));
        preferredLightThemeClass = Preferences.getPreferenceDefault(Preferences.THEME_CLASS_NAME,
                UIDesignManager.LIGHT_CLASS_NAME_DEFAULT);
        preferredDarkThemeClass = Preferences.getPreferenceDefault(Preferences.THEME_DARK_CLASS_NAME,
                UIDesignManager.DARK_CLASS_NAME_DEFAULT);
        panel.cbLightThemeSelect.setSelectedItem(preferredLightThemeClass);
        panel.cbDarkThemeSelect.setSelectedItem(preferredDarkThemeClass);
        if (themeMode.equals("dark")) {
            panel.useDarkThemeRB.setSelected(true);
        } else if (themeMode.equals("sync") && !Platform.isLinux()) {
            panel.syncWithOSColorRB.setSelected(true);
        } else {
            panel.useLightDefaultThemeRB.setSelected(true);
        }
        previousFileListDialog = Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD,
                true);
    }

    private void initListeners() {
        panel.restoreWindowButton
                .addActionListener(e -> MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()));
        panel.cbLightThemeSelect.addActionListener(e -> setRestartRequired(isModified()));
        panel.cbDarkThemeSelect.addActionListener(e -> setRestartRequired(isModified()));
        panel.useDarkThemeRB.addActionListener(e -> setRestartRequired(isModified()));
        panel.useLightDefaultThemeRB.addActionListener(e -> setRestartRequired(isModified()));
        panel.syncWithOSColorRB.addActionListener(e -> setRestartRequired(isModified()));
    }

    @Override
    public void restoreDefaults() {
        panel.cbMenustyleSelect.setSelectedItem(MainMenuUI.class.getName());
        Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, previousFileListDialog);
        String defaultClassName = UIDesignManager.LIGHT_CLASS_NAME_DEFAULT;
        String defaultDarkClassName = UIDesignManager.DARK_CLASS_NAME_DEFAULT;
        panel.cbLightThemeSelect.setSelectedItem(defaultClassName);
        panel.cbDarkThemeSelect.setSelectedItem(defaultDarkClassName);
        panel.useLightDefaultThemeRB.setSelected(true);
        previousFileListDialog = Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD,
                true);
        setRestartRequired(isModified());
    }

    private boolean isModified() {
        boolean modified = false;
        if (preferredLightThemeClass != null) {
            modified = !preferredLightThemeClass.equals(panel.cbLightThemeSelect.getSelectedItem());
        }
        if (modified) {
            return true;
        }
        if (preferredDarkThemeClass != null) {
            modified = !preferredDarkThemeClass.equals(panel.cbDarkThemeSelect.getSelectedItem());
        }
        if (modified) {
            return true;
        }
        Object selected = panel.cbMenustyleSelect.getSelectedItem();
        if (selected != null && !selected.toString().equals(Preferences.getPreference(Preferences.MENUUI_CLASS_NAME))) {
            return true;
        }
        if (themeMode.equals("sync")) {
            return !panel.syncWithOSColorRB.isSelected();
        } else if (themeMode.equals("dark")) {
            return !panel.useDarkThemeRB.isSelected();
        } else {
            return !panel.useLightDefaultThemeRB.isSelected();
        }
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.MENUUI_CLASS_NAME,
                panel.cbMenustyleSelect.getSelectedItem().toString());
        if (panel.useLightDefaultThemeRB.isSelected()) {
            Preferences.setPreference(Preferences.THEME_COLOR_MODE, "default");
        } else if (panel.useDarkThemeRB.isSelected()) {
            Preferences.setPreference(Preferences.THEME_COLOR_MODE, "dark");
        } else {
            Preferences.setPreference(Preferences.THEME_COLOR_MODE, "sync");
        }
        Preferences.setPreference(Preferences.THEME_CLASS_NAME, panel.cbLightThemeSelect.getSelectedItem());
        Preferences.setPreference(Preferences.THEME_DARK_CLASS_NAME,
                panel.cbDarkThemeSelect.getSelectedItem());
    }
}
