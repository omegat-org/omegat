/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.omegat.util.StaticUtils;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ScriptRunnerTest {

    @Test
    public void testCompileScripts() throws Exception {
        File scriptDir = new File(StaticUtils.installDir(), ScriptingModule.DEFAULT_SCRIPTS_DIR);
        assertTrue("scriptDir is " + scriptDir.toPath(), scriptDir.isDirectory());
        for (File f : scriptDir.listFiles()) {
            if (!f.isFile()) {
                continue;
            }
            String ext = FilenameUtils.getExtension(f.getName());
            ScriptEngine engine = ScriptRunner.getManager().getEngineByExtension(ext);
            if (engine instanceof Compilable) {
                Compilable cEngine = (Compilable) engine;
                try (BufferedReader br = Files.newBufferedReader(f.toPath())) {
                    assertNotNull(cEngine.compile(br));
                }
            }
        }
    }

    /**
     * Check that engines for OmegaT built-in scripting languages are available.
     * Current those are:
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
        List<String> extensions = ScriptRunner.getManager().getEngineFactories().stream()
                .map(ScriptEngineFactory::getExtensions).flatMap(List::stream).collect(Collectors.toList());
        assertTrue("Not found a Javascript engine", extensions.contains("js"));
        assertTrue("Not found a Groovy engine", extensions.contains("groovy"));
    }
}
