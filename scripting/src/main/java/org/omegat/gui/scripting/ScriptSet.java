/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Briac Pilpre
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
package org.omegat.gui.scripting;

import org.omegat.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ScriptSet {
    private final Properties props;
    private final File setFile;

    public ScriptSet(File setFile) {
        this.setFile = setFile;
        props = new Properties();

        try {
            FileInputStream is = new FileInputStream(setFile);
            props.load(is);
            is.close();
        } catch (IOException e) {
            Log.log(e);
        }
    }

    public static void saveSet(final File setFile, final String title, final String[] quickScripts) throws IOException {
        Properties p = new Properties();

        p.setProperty("title", title);

        for (int i = 0; i < ScriptingWindow.NUMBERS_OF_QUICK_SCRIPTS; i++) {
            if (quickScripts[i] != null) {
                p.setProperty(Integer.toString(i + 1), quickScripts[i]);
            }
        }

        p.store(new FileOutputStream(setFile), "OmegaT Script Set");
    }

    public String getTitle() {
        if (props.containsKey("title")) {
            return props.getProperty("title");
        } else {
            return setFile.getName();
        }
    }

    public ScriptItem getScriptItem(int key) {
        if (key > ScriptingWindow.NUMBERS_OF_QUICK_SCRIPTS) {
            key = ScriptingWindow.NUMBERS_OF_QUICK_SCRIPTS;
        }

        String idx = Integer.toString(key);
        if (props.containsKey(idx)) {
            return new ScriptItem(new File(setFile.getParentFile(), props.getProperty(idx)));
        }

        return null;
    }

    public void load() {

    }

}
