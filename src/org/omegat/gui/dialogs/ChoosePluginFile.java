/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.gui.dialogs;

import java.io.File;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;


@SuppressWarnings("serial")
public class ChoosePluginFile extends JFileChooser {

    public ChoosePluginFile() {
        super(Preferences.getPreference(Preferences.CURRENT_FOLDER));

        setMultiSelectionEnabled(false);
        setFileHidingEnabled(true);
        setFileSelectionMode(FILES_ONLY);
        setDialogTitle(OStrings.getString("GUI_PLUGIN_OPEN"));
        setAcceptAllFileFilterUsed(false);
        addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return OStrings.getString("GUI_PLUGIN_OPEN");
            }

            @Override
            public boolean accept(File f) {
                return isAcceptable(f);
            }
        });
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return isAcceptable(f);
    }

    private boolean isAcceptable(File f) {
        if (!f.isFile()) {
            return false;
        }
        String name = f.getName().toLowerCase(Locale.ENGLISH);
        return name.endsWith(OConsts.JAR_EXTENSION) || name.endsWith(OConsts.ZIP_EXTENSION);
    }
}
