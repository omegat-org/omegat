/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

/**
 * Tests for (some) static utility methods.
 *
 * @author Maxym Mykhalchuk
 * @author Aaron Madlon-Kay
 */
public class StaticUtilsTest extends TestCase {

    public void testCompileFileMask() {
        Pattern r = StaticUtils.compileFileMask("Ab1-&*/**");
        assertEquals("(?:|.*/)Ab1\\-\\&[^/]*(?:|/.*)", r.pattern());
    }
    
    public void testFilePatterns() {
        // From https://confluence.atlassian.com/fisheye/pattern-matching-guide-298976797.html

        String p = "*.txt";
        assertTrue(patternMatches(p, "/foo.txt"));
        assertTrue(patternMatches(p, "/bar/foo.txt"));
        assertFalse(patternMatches(p, "/foo.txty"));
        assertFalse(patternMatches(p, "/bar/foo.txty/"));

        p = "/*.txt";
        assertTrue(patternMatches(p, "/foo.txt"));
        assertFalse(patternMatches(p, "/bar/foo.txt"));

        p = "dir1/file.txt";
        assertTrue(patternMatches(p, "/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir2/dir1/file.txt"));

        p = "**/dir1/file.txt";
        assertTrue(patternMatches(p, "/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir2/dir1/file.txt"));

        p = "/**/dir1/file.txt";
        assertTrue(patternMatches(p, "/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir2/dir1/file.txt"));

        p = "/dir3/**/dir1/file.txt";
        assertTrue(patternMatches(p, "/dir3/dir1/file.txt"));
        assertTrue(patternMatches(p, "/dir3/dir2/dir1/file.txt"));
        assertFalse(patternMatches(p, "/dir3/file.txt"));
        assertFalse(patternMatches(p, "/dir1/file.txt"));

        p = "/dir1/**";
        assertTrue(patternMatches(p, "/dir1/foo"));
        assertTrue(patternMatches(p, "/dir1/foo/bar"));
        assertTrue(patternMatches(p, "/dir1/foo.baz"));
        assertFalse(patternMatches(p, "/dir11/foo"));

        p = "/dir1*";
        assertTrue(patternMatches(p, "/dir11"));
        assertTrue(patternMatches(p, "/dir12"));
        assertTrue(patternMatches(p, "/dir12345"));
        assertFalse(patternMatches(p, "/dir1/dir2"));

        p = "/dir??";
        assertTrue(patternMatches(p, "/dir11"));
        assertTrue(patternMatches(p, "/dir12"));
        assertFalse(patternMatches(p, "/dir12345"));

        // From https://ant.apache.org/manual/dirtasks.html#patterns

        p = "*.java";
        assertTrue(patternMatches(p, ".java"));
        assertTrue(patternMatches(p, "x.java"));
        assertTrue(patternMatches(p, "FooBar.java"));
        assertFalse(patternMatches(p, "FooBar.xml"));

        p = "?.java";
        assertTrue(patternMatches(p, "x.java"));
        assertTrue(patternMatches(p, "A.java"));
        assertFalse(patternMatches(p, ".java"));
        assertFalse(patternMatches(p, "xyz.java"));

        p = "/?abc/*/*.java";
        assertTrue(patternMatches(p, "/xabc/foobar/test.java"));

        p = "/test/**";
        assertTrue(patternMatches(p, "/test/x.java"));
        assertTrue(patternMatches(p, "/test/foo/bar/xyz.html"));
        assertFalse(patternMatches(p, "/xyz.xml"));
        
        assertEquals(StaticUtils.compileFileMask("mypackage/test/**").pattern(),
                StaticUtils.compileFileMask("mypackage/test/").pattern());

        p = "**/CVS/*";
        assertTrue(patternMatches(p, "CVS/Repository"));
        assertTrue(patternMatches(p, "org/apache/CVS/Entries"));
        assertTrue(patternMatches(p, "org/apache/jakarta/tools/ant/CVS/Entries"));
        assertFalse(patternMatches(p, "org/apache/CVS/foo/bar/Entries"));

        p = "org/apache/jakarta/**";
        assertTrue(patternMatches(p, "org/apache/jakarta/tools/ant/docs/index.html"));
        assertTrue(patternMatches(p, "org/apache/jakarta/test.xml"));
        assertFalse(patternMatches(p, "org/apache/xyz.java"));

        p = "org/apache/**/CVS/*";
        assertTrue(patternMatches(p, "org/apache/CVS/Entries"));
        assertTrue(patternMatches(p, "org/apache/jakarta/tools/ant/CVS/Entries"));
        assertFalse(patternMatches(p, "org/apache/CVS/foo/bar/Entries"));

        // Ant docs claim this pattern "Matches all files that have a test
        // element in their path, including test as a filename."
        p = "**/test/**";
        assertTrue(patternMatches(p, "test"));
        assertTrue(patternMatches(p, "/test"));
        assertTrue(patternMatches(p, "foo/test"));
        assertTrue(patternMatches(p, "/foo/test"));
        assertTrue(patternMatches(p, "foo/test/bar"));
        assertTrue(patternMatches(p, "/foo/test/bar"));
        assertFalse(patternMatches(p, "/foo/tests/bar"));
        assertFalse(patternMatches(p, "/foo/tests.bar"));

        p = "foo/**/bar";
        assertFalse(patternMatches(p, "foobar"));
        assertFalse(patternMatches(p, "foobaz/bar"));
    }

    private static boolean patternMatches(String pattern, String path) {
        Pattern p = StaticUtils.compileFileMask(pattern);
        return p.matcher(path).matches();
    }

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

    public void testBuildFileList() throws Exception {

        File tempDir = Files.createTempDirectory("omegat").toFile();
        assertTrue(tempDir.isDirectory());

        File subDir = new File(tempDir, "a");
        assertTrue(subDir.mkdirs());

        File aFile = new File(subDir, "foo");
        assertTrue(aFile.createNewFile());
        aFile = new File(subDir, "bar");
        assertTrue(aFile.createNewFile());

        List<File> list1 = StaticUtils.buildFileList(tempDir, false);
        assertTrue(list1.isEmpty());

        List<File> list2 = StaticUtils.buildFileList(tempDir, true);
        assertEquals(2, list2.size());

        Collections.sort(list2);
        assertTrue(list2.get(0).getPath().endsWith("bar"));

        try {
            Files.createSymbolicLink(new File(tempDir, "baz").toPath(), tempDir.toPath());
            List<File> list3 = StaticUtils.buildFileList(tempDir, true);
            assertEquals(list2.size(), list3.size());
            IntStream.range(0, list2.size()).forEach(i -> assertEquals(list2.get(i), list3.get(i)));
        } catch (UnsupportedOperationException | IOException ex) {
            // Creating symbolic links appears to not be supported on this system
        }
        
        FileUtils.deleteDirectory(tempDir);
    }

    public void testInstallDir() {
        File installDir = new File(StaticUtils.installDir());

        assertTrue(installDir.isDirectory());

        for (String dir : new String[] { "src", "docs", "lib" }) {
            assertTrue(new File(installDir, dir).isDirectory());
        }
    }
}
