/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
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

package org.omegat.core.team2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * Class for read/save project-specific settings.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectTeamSettings {
    private final File configFile;

    public ProjectTeamSettings(File configDir) {
        this.configFile = new File(configDir, "settings.properties");
    }

    /**
     * Get setting.
     */
    public synchronized String get(String key) {
        try {
            Properties p = new Properties();
            if (configFile.exists()) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    p.load(in);
                }
            }
            return p.getProperty(key);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Update setting.
     */
    public synchronized void set(String key, String newValue) {
        try {
            Properties p = new Properties();
            File f = configFile;
            File fNew = new File(configFile.getAbsolutePath() + ".new");
            if (f.exists()) {
                try (FileInputStream in = new FileInputStream(f)) {
                    p.load(in);
                }
            } else {
                f.getParentFile().mkdirs();
            }
            if (newValue != null) {
                p.setProperty(key, newValue);
            } else {
                p.remove(key);
            }
            try (FileOutputStream out = new FileOutputStream(fNew)) {
                p.store(out, null);
            }
            f.delete();
            FileUtils.moveFile(fNew, f);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
