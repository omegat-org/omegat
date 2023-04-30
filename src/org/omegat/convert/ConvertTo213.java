/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.convert;

import java.io.File;
import java.io.FileOutputStream;

import org.omegat.util.Preferences;
import org.omegat.util.Preferences.IPreferences;
import org.omegat.util.PreferencesImpl;
import org.omegat.util.PreferencesXML;
import org.omegat.util.StaticUtils;

/**
 * Convert UI settings to v2.1.3.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class ConvertTo213 {

    private ConvertTo213() {
    }

    public static void convertUIConfig(File out) throws Exception {
        File prefsFile = new File(StaticUtils.getConfigDir(), Preferences.FILE_PREFERENCES);
        if (!prefsFile.isFile()) {
            return;
        }
        IPreferences prefs = new PreferencesImpl(new PreferencesXML(prefsFile, null));
        String layout = prefs.getPreference(Preferences.MAINWINDOW_LAYOUT);
        if (!layout.isEmpty()) {
            byte[] bytes = StaticUtils.uudecode(layout);
            String xml = new String(bytes, "UTF-8");
            xml = xml.replace("GOOGLE_TRANSLATE", "MACHINE_TRANSLATE");

            FileOutputStream o = new FileOutputStream(out);
            try {
                o.write(xml.getBytes("UTF-8"));
            } finally {
                o.close();
            }
        }
        Preferences.setPreference(Preferences.MAINWINDOW_LAYOUT, "");
    }
}
