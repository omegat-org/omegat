/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichik
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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.omegat.util.StaticUtils;

/**
 * Class for read/save repository-specific settings in the ~/.omegat/ directory.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TeamSettings {

    private TeamSettings() {
    }

    private static File configFile;

    private static synchronized File getConfigFile() {
        if (configFile == null) {
            configFile = new File(StaticUtils.getConfigDir(), "repositories.properties");
        }
        return configFile;
    }

    public static synchronized Set<Object> listKeys() {
        try {
            Properties p = new Properties();
            if (getConfigFile().exists()) {
                FileInputStream in = new FileInputStream(getConfigFile());
                try {
                    p.load(in);
                } finally {
                    in.close();
                }
            }
            return p.keySet();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get setting.
     */
    public static synchronized String get(String key) {
        try {
            Properties p = new Properties();
            if (getConfigFile().exists()) {
                FileInputStream in = new FileInputStream(getConfigFile());
                try {
                    p.load(in);
                } finally {
                    in.close();
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
    public static synchronized void set(String key, String newValue) {
        try {
            Properties p = new Properties();
            File f = getConfigFile();
            File fNew = new File(getConfigFile().getAbsolutePath() + ".new");
            if (f.exists()) {
                FileInputStream in = new FileInputStream(f);
                try {
                    p.load(in);
                } finally {
                    in.close();
                }
            } else {
                f.getParentFile().mkdirs();
            }
            if (newValue != null) {
                p.setProperty(key, newValue);
            } else {
                p.remove(key);
            }
            FileOutputStream out = new FileOutputStream(fNew);
            try {
                p.store(out, null);
            } finally {
                out.close();
            }
            f.delete();
            FileUtils.moveFile(fNew, f);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
