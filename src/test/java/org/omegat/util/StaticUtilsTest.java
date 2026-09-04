/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2026 Stephan Pakebusch
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

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Tests for (some) static utility methods.
 *
 * @author Maxym Mykhalchuk
 * @author Aaron Madlon-Kay
 */
public class StaticUtilsTest {

    @Test
    public void testParseCLICommand() {
        String cmd = " sort  \"/path with/spaces in/it\"    /path\\ with/escaped\\ spaces/"
                + " \"escape\\\"escape\" 'noescape\\'noescape'' \"noescape\\ noescape\""
                + " C:\\windows\\path";
        String[] args = StaticUtils.parseCLICommand(cmd);
        assertEquals("/path with/spaces in/it", args[1]);
        assertEquals("/path with/escaped spaces/", args[2]);
        assertEquals("escape\"escape", args[3]);
        assertEquals("noescape\\noescape", args[4]);
        assertEquals("noescape\\ noescape", args[5]);
        assertEquals("C:\\windows\\path", args[6]);
        assertEquals(args.length, 7);
        args = StaticUtils.parseCLICommand(" ");
        assertEquals(args[0], "");
        assertEquals(args.length, 1);
    }

    @Test
    public void testInstallDir() {
        File installDir = new File(StaticUtils.installDir());

        assertTrue(installDir.isDirectory());

        for (String dir : new String[] { "src", "lib", "release" }) {
            assertTrue(new File(installDir, dir).isDirectory());
        }
    }

    @Test
    public void testGlobToRegex() {
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab?d", false), "abcd"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("ab?d", false), "abd"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abcccccd"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abd"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abde"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*", false), "abdefg"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("$a[b-c]!?*d{}", false), "$a[b-c]!?1234d{}"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a?", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a ?", false), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a* b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a* b", true), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a*b", false), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*b", true), "a\u00A0b"));

        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", true), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a b", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", true), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a *", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a *", true), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a ?", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a ?", true), "a\u00A0b"));
    }

    @Test
    public void testDetermineUserScriptsPathFirstRun() throws Exception {
        withTempHome(dataDir -> {
            Path scriptsPath = StaticUtils.determineUserScriptsPath();
            assertEquals(dataDir.resolve("scripts"), scriptsPath);
            assertTrue(Files.isDirectory(scriptsPath));
        });
    }

    @Test
    public void testDetermineUserScriptsPathMigratesLegacyFolder() throws Exception {
        withTempHome(dataDir -> {
            Path legacyPath = dataDir.resolve("script");
            Files.createDirectories(legacyPath);
            Files.write(legacyPath.resolve("my_script.groovy"), "// user content".getBytes(StandardCharsets.UTF_8));
            Path scriptsPath = StaticUtils.determineUserScriptsPath();
            assertEquals(dataDir.resolve("scripts"), scriptsPath);
            assertTrue(Files.exists(scriptsPath.resolve("my_script.groovy")));
            assertFalse(Files.exists(legacyPath));
        });
    }

    @Test
    public void testDetermineUserScriptsPathIgnoresLegacyFile() throws Exception {
        withTempHome(dataDir -> {
            Path legacyFile = dataDir.resolve("script");
            Files.write(legacyFile, "not a folder".getBytes(StandardCharsets.UTF_8));
            Path scriptsPath = StaticUtils.determineUserScriptsPath();
            assertEquals(dataDir.resolve("scripts"), scriptsPath);
            assertTrue(Files.isDirectory(scriptsPath));
            assertTrue(Files.isRegularFile(legacyFile));
        });
    }

    @Test
    public void testDetermineUserScriptsPathKeepsExistingFolders() throws Exception {
        withTempHome(dataDir -> {
            Path legacyPath = dataDir.resolve("script");
            Files.createDirectories(legacyPath);
            Path existingPath = dataDir.resolve("scripts");
            Files.createDirectories(existingPath);
            Files.write(existingPath.resolve("current.groovy"), "// current".getBytes(StandardCharsets.UTF_8));
            Path scriptsPath = StaticUtils.determineUserScriptsPath();
            assertEquals(existingPath, scriptsPath);
            assertTrue(Files.exists(scriptsPath.resolve("current.groovy")));
            assertTrue(Files.isDirectory(legacyPath));
        });
    }

    private interface DataDirConsumer {
        void accept(Path dataDir) throws IOException;
    }

    private static void withTempHome(DataDirConsumer test) throws IOException {
        Path tempHome = Files.createTempDirectory("omegat-home-");
        String originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempHome.toString());
        try {
            // getApplicationDataDir on Windows requires this folder to exist
            // to resolve under the home directory.
            Files.createDirectories(tempHome.resolve("AppData").resolve("Local"));
            test.accept(Paths.get(StaticUtils.getApplicationDataDir()));
        } finally {
            System.setProperty("user.home", originalHome);
            FileUtils.deleteQuietly(tempHome.toFile());
        }
    }
}
