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

import java.util.List;
import javax.swing.JComponent;
import javax.swing.SpinnerListModel;

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

    private static SpinnerListModel themeSpinnerModel= new SpinnerListModel();
    public static SpinnerListModel getThemeSpinnerModel() {
        return themeSpinnerModel;
    }

    
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
        List<String> themeNames = UIDesignManager.getThemes();
        themeSpinnerModel.setList(themeNames);
        panel = new AppearancePreferencesPanel();
        // TODO: Properly abstract the restore function
        panel.restoreWindowButton
                .addActionListener(e -> MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()));
    }
    
    @Override
    protected void initFromPrefs() {
        String currentTheme = Preferences.getPreferenceDefault(Preferences.THEME_SELECTED_NAME, "Default");
        // Todo: check theme exist or not, when not exist fallback to default
        themeSpinnerModel.setValue(currentTheme);
    }

    @Override
    public void restoreDefaults() {
        themeSpinnerModel.setValue("Default");
    }

    @Override
    public void persist() {
        String theme = (String) themeSpinnerModel.getValue();
        Preferences.setPreference(Preferences.THEME_SELECTED_NAME, theme);
    }
}
