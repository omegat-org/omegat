/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.Main;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;

/**
 * Acceptance test for CLI execution with script.
 */
public class ConsoleTranslateWithScriptTest extends TestCore {

    private Path tmpDir;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() throws Exception {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Initialize OmegaT core for testing
        TestCoreInitializer.initMainWindow(null);
        Core.setProject(new NotLoadedProject());

        tmpDir = Files.createTempDirectory("omegat-test-project");
        Path projectData = Paths.get("test-acceptance/data/project");
        if (Files.exists(projectData)) {
            FileUtils.copyDirectory(projectData.toFile(), tmpDir.toFile());
        }

        // Prevent System.exit from killing the test
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                throw new SecurityException("System.exit(" + status + ") called");
            }

            @Override
            public void checkPermission(java.security.Permission perm) {
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        System.setSecurityManager(null);
        System.setOut(originalOut);
        System.setErr(originalErr);
        if (tmpDir != null && Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testConsoleTranslateWithScript() {
        // Use the script path from the issue description if it exists, otherwise use the one from VCS status
        String scriptPath = "test-acceptance/data/scripting/greeting.groovy";
        if (!Files.exists(Paths.get(scriptPath))) {
            scriptPath = "test-acceptance/data/scripts/greeting.groovy";
        }

        String[] args = {
                tmpDir.toString(),
                "--mode=console-translate",
                "--script=" + scriptPath
        };

        try {
            Main.main(args);
        } catch (Throwable t) {
            System.err.println("Main.main threw an exception:");
            t.printStackTrace(System.err);
        }

        String output = outContent.toString();
        String error = errContent.toString();

        // Print to original stdout for debugging if test fails in Gradle
        originalOut.println("STDOUT:\n" + output);
        originalOut.println("STDERR:\n" + error);

        assertTrue("Output should contain script greeting. Actual output:\n" + output + "\nError output:\n" + error,
                output.contains("Hello from script! Running acceptance test…"));
        assertTrue("Output should contain project info from script. Actual output:\n" + output + "\nError output:\n" + error,
                output.contains("Project name:"));
        assertTrue("Output should contain script compile run when compiling:\n" + output + "\nError output:\n" + error,
                output.contains("Compile project"));
        assertTrue("Output should contain script close run when closing:\n" + output + "\nError output:\n" + error,
                output.contains("Bye from script: closing project"));
    }

    @Test
    public void testConsoleStatWithScript() {
        // Use the script path from the issue description if it exists, otherwise use the one from VCS status
        String scriptPath = "test-acceptance/data/scripting/greeting.groovy";
        if (!Files.exists(Paths.get(scriptPath))) {
            scriptPath = "test-acceptance/data/scripts/greeting.groovy";
        }

        String[] args = {
                tmpDir.toString(),
                "--mode=console-stats",
                "--script=" + scriptPath
        };

        try {
            Main.main(args);
        } catch (Throwable t) {
            System.err.println("Main.main threw an exception:");
            t.printStackTrace(System.err);
        }

        String output = outContent.toString();
        String error = errContent.toString();

        // Print to original stdout for debugging if test fails in Gradle
        originalOut.println("STDOUT:\n" + output);
        originalOut.println("STDERR:\n" + error);

        assertTrue("Output should contain script greeting. Actual output:\n" + output + "\nError output:\n" + error,
            output.contains("Hello from script! Running acceptance test…"));
        assertTrue("Output should contain project info from script. Actual output:\n" + output + "\nError output:\n" + error,
            output.contains("Project name:"));
    }
}
