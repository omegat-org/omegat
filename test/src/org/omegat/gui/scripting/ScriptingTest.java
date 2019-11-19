/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.scripting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assume;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

public class ScriptingTest extends TestCore {

    /**
     * Test for bug #775: Unresolvable scripting folder setting can cause an empty
     * Scripting window
     * <p>
     * NPE while initializing quick script menu entries when the script folder path
     * member is null (failed to be set because it was invalid).
     *
     * @see <a href="https://sourceforge.net/p/omegat/bugs/775/">Bug #775</a>
     */
    @Test
    public void testLoadScriptingWindow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Set quick script
        Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + 1, "blah");

        // Set bogus scripts folder (a file can't be a folder!)
        File tmp = File.createTempFile("omegat", "tmp");
        try {
            Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, tmp.getAbsolutePath());
            new ScriptingWindow();
        } finally {
            assertTrue(tmp.delete());
        }
    }

    @Test
    public void testCompileScripts() throws Exception {
        File scriptDir = new File(StaticUtils.installDir(), ScriptingWindow.DEFAULT_SCRIPTS_DIR);
        assertTrue(scriptDir.isDirectory());
        for (File f : scriptDir.listFiles()) {
            if (!f.isFile()) {
                continue;
            }
            String ext = FilenameUtils.getExtension(f.getName());
            ScriptEngine engine = ScriptRunner.MANAGER.getEngineByExtension(ext);
            if (engine instanceof Compilable) {
                Compilable cEngine = (Compilable) engine;
                try (BufferedReader br = Files.newBufferedReader(f.toPath())) {
                    assertNotNull(cEngine.compile(br));
                }
            }
        }
    }

    @Test
    public void testScriptProperties() throws Exception {
        File scriptDir = new File(StaticUtils.installDir(), ScriptingWindow.DEFAULT_SCRIPTS_DIR);
        assertTrue(scriptDir.isDirectory());
        File propsDir = new File(scriptDir, "properties");
        assertTrue(propsDir.isDirectory());

        List<String> scripts = Collections.emptyList();
        try (Stream<Path> stream = Files.list(scriptDir.toPath())) {
            scripts = stream.map(Path::toFile).filter(File::isFile).map(File::getName)
                    .map(FilenameUtils::removeExtension).filter(n -> !n.isEmpty()).collect(Collectors.toList());
        }
        assertFalse(scripts.isEmpty());

        for (File f : propsDir.listFiles()) {
            if (!f.isFile() || f.getName().equals(".DS_Store")) {
                continue;
            }
            assertTrue("Script properties file appears to be orphaned: " + f,
                    scripts.stream().anyMatch(s -> f.getName().startsWith(s)));
        }
    }

    /**
     * Check that engines for OmegaT built-in scripting languages are available.
     * Currently those are:
     * <ul>
     * <li>JavaScript
     * <li>Groovy
     * </ul>
     * This is more a check on the test classpath than anything else
     * ({@link #testCompileScripts()} won't indicate if Groovy goes missing, for
     * instance), though presumably if an engine is available on the test
     * runtime classpath then it will be available in the runtime classpath as
     * well.
     */
    @Test
    public void testAvailableEngines() {
        List<String> extensions = ScriptRunner.MANAGER.getEngineFactories().stream()
                .map(ScriptEngineFactory::getExtensions).flatMap(List::stream).collect(Collectors.toList());
        assertTrue(extensions.contains("js"));
        assertTrue(extensions.contains("groovy"));
    }
}
