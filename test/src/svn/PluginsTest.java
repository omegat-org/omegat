/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package svn;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Properties;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.StaticUtils;

import junit.framework.TestCase;

public class PluginsTest extends TestCase {

    private static final File PLUGINS_FILE = new File(StaticUtils.installDir(), PluginUtils.PLUGINS_LIST_FILE);

    public void testPluginsListWhitespace() throws Exception {
        Files.lines(PLUGINS_FILE.toPath()).filter(line -> line.endsWith("\\")).forEach((String line) -> {
            assertTrue("Continuation lines must end with a space.", line.endsWith(" \\"));
        });
    }

    public void testPluginsListClasses() throws Exception {
        Properties plugins = new Properties();
        try (FileReader fr = new FileReader(PLUGINS_FILE)) {
            plugins.load(fr);
        }
        for (Object o : plugins.keySet()) {
            String[] classes = plugins.getProperty(o.toString()).split(" ");
            for (String cls : classes) {
                try {
                    Class.forName(cls);
                } catch (ClassNotFoundException ex) {
                    fail(ex.toString());
                } catch (ExceptionInInitializerError ex) {
                    // Don't care about this.
                } catch (NoClassDefFoundError ex) {
                    // Don't care about this either. It looks similar to
                    // ClassNotFoundException, but this actually means a class
                    // that failed to instantiate due to
                    // ExceptionInInitializerError was then again referenced in
                    // another class.
                }
            }
        }
    }
}
