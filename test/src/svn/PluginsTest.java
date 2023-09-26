/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package svn;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.HeadlessException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.StaticUtils;
import org.omegat.util.TestPreferencesInitializer;

public class PluginsTest {

    private static final Path PLUGINS_FILE = Paths.get(StaticUtils.installDir(), PluginUtils.PLUGINS_LIST_FILE);

    @Test
    public void testPluginsListWhitespace() throws Exception {
        Files.lines(PLUGINS_FILE).filter(line -> line.endsWith("\\")).forEach((String line) -> {
            assertTrue("Continuation lines must end with a space.", line.endsWith(" \\"));
        });
    }

    @Test
    public void testPluginsListClasses() throws Exception {
        Properties plugins = new Properties();
        try (Reader fr = Files.newBufferedReader(PLUGINS_FILE, StandardCharsets.UTF_8)) {
            plugins.load(fr);
        }

        TestPreferencesInitializer.init(); // Some plugins depend on inited prefs
        for (Object o : plugins.keySet()) {
            if (o.toString().startsWith("plugin.desc")) {
                continue;
            }
            String[] classes = plugins.getProperty(o.toString()).split(" ");
            for (String cls : classes) {
                try {
                    Class.forName(cls);
                } catch (ClassNotFoundException ex) {
                    fail(ex.toString());
                } catch (HeadlessException ex) {
                    // Don't care about this
                }
            }
        }
    }
}
