/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2021 Hiroshi Miura
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

import java.awt.Component;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIDesignManager;

/**
 * @author Aaron Madlon-Kay
 */
public class AppearanceController extends BasePreferencesController {

    private AppearancePreferencesPanel panel;

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
        Map<String, String> themes = UIDesignManager.getThemes();
        themes.forEach((key, value) -> themeSelectionModel
                .addElement(new ThemeLabel(key, value, new ImageIcon(UIDesignManager.getThemeImage(key)))));
        panel.cbThemeSelect.setModel(themeSelectionModel);
        ListThemeRenderer renderer = new ListThemeRenderer();
        panel.cbThemeSelect.setRenderer(renderer);
        // TODO: Properly abstract the restore function
        panel.restoreWindowButton
                .addActionListener(e -> MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()));
    }

    @Override
    protected void initFromPrefs() {
        String currentTheme = Preferences.getPreferenceDefault(Preferences.THEME_SELECTED_NAME,
                Preferences.THEME_DEFAULT);
        if (!setSelection(currentTheme)) {
            restoreDefaults();
        }
    }

    @Override
    public void restoreDefaults() {
        setSelection("Default");
    }

    private boolean setSelection(String key) {
        Map<String, String> themes = UIDesignManager.getThemes();
        if (themes.keySet().contains(key)) {
            for (int i = 0; i < themeSelectionModel.getSize(); i++) {
                ThemeLabel obj = themeSelectionModel.getElementAt(i);
                if (key.equals(obj.getKey())) {
                    themeSelectionModel.setSelectedItem(obj);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void persist() {
        ThemeLabel theme = (ThemeLabel) themeSelectionModel.getSelectedItem();
        Preferences.setPreference(Preferences.THEME_SELECTED_NAME, theme.getKey());
    }

    private static DefaultComboBoxModel<ThemeLabel> themeSelectionModel = new DefaultComboBoxModel<>();

    @SuppressWarnings("serial")
    static class ListThemeRenderer extends JLabel implements ListCellRenderer<ThemeLabel> {

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ThemeLabel> jList,
                ThemeLabel data,
                int i,
                boolean isSelected,
                boolean b) {
            setText(data.getText());
            setIcon(data.getIcon());
            return this;
        }
    }
    public static class ThemeLabel {

        String text;
        Icon icon;
        String key;

        ThemeLabel(String key, String text, Icon icon) {
            this.key = key;
            this.text = text;
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public Icon getIcon() {
            return icon;
        }

        public String getKey() {
            return key;
        }
    }
}
