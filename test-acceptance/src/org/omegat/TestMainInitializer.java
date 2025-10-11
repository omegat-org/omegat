/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

package org.omegat;

import javax.swing.UIManager;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.languagetools.LanguageManager;
import org.omegat.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class TestMainInitializer {

    private static final String FRENCH = "org.languagetool.language.French";
    public static final String PLUGINS_LIST_FILE = "test-acceptance/plugins.properties";

    private TestMainInitializer() {
    }

    public static void initClassloader() {
        if (PluginUtils.getMachineTranslationClasses().isEmpty()) {
            Log.log("Loading plugins from " + PLUGINS_LIST_FILE);
            Properties props = new Properties();
            try (InputStream fis = Files.newInputStream(Paths.get(PLUGINS_LIST_FILE))) {
                props.load(fis);
                PluginUtils.loadPluginFromProperties(props);
            } catch (ClassNotFoundException | IOException ex) {
                Log.log(ex);
            }
            LanguageManager.registerLTLanguage("fr", FRENCH);
            UIManager.put("ClassLoader", PluginUtils.getClassLoader(PluginUtils.PluginType.THEME));
        }
    }

}
