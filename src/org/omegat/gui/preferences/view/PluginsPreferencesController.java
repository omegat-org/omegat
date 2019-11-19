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

import java.awt.Desktop;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;

/**
 * @author Aaron Madlon-Kay
 */
public class PluginsPreferencesController extends BasePreferencesController {

    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";
    private PluginsPreferencesPanel panel;

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
        return OStrings.getString("PREFS_TITLE_PLUGINS");
    }

    private void initGui() {
        panel = new PluginsPreferencesPanel();
        panel.browsePluginsButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(PLUGINS_WIKI_URL));
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(panel, ex.getLocalizedMessage(),
                        OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Override
    protected void initFromPrefs() {
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    public void persist() {
    }
}
