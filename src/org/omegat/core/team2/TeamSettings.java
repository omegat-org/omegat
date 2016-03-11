/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import org.omegat.core.Core;
import org.omegat.util.FileUtil;

/**
 * Class for read/save repository-specific settings.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TeamSettings {

    /**
     * Get setting.
     */
    public synchronized static String get(String key) {
        try {
            Properties p = new Properties();
            File versionsFile = new File(Core.getProject().getProjectProperties().getProjectRootDir(),
                    RemoteRepositoryProvider.REPO_SUBDIR + "settings.properties");
            if (versionsFile.exists()) {
                FileInputStream in = new FileInputStream(versionsFile);
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
    public synchronized static void set(String key, String newValue) {
        try {
            Properties p = new Properties();
            File projectDir = Core.getProject().getProjectProperties().getProjectRootDir();
            File f = new File(projectDir, RemoteRepositoryProvider.REPO_SUBDIR + "settings.properties");
            File fNew = new File(projectDir, RemoteRepositoryProvider.REPO_SUBDIR + "settings.properties.new");
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
            FileUtil.move(fNew, f);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
