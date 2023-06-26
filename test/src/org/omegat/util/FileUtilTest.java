/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay, Alex Buloichik
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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemLoopException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.util.FileUtil.ICollisionCallback;

/**
 * @author Aaron Madlon-Kay
 * @author Alex Buloichik
 */
public class FileUtilTest {

    private static final Logger LOGGER = Logger.getLogger(FileUtilTest.class.getName());

    private File base;

    private String oldTz;

    @Before
    public final void setUp() throws Exception {
        oldTz = System.setProperty("user.timezone", "UTC");
        base = Files.createTempDirectory("omegat").toFile();
    }

    @After
    public final void tearDown() throws Exception {
        safeDeleteDirectory(base);
        if (oldTz != null) {
            System.setProperty("user.timezone", oldTz);
        }
    }

    private void safeDeleteDirectory(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            // Apache Commons IO 2.8.0+ seem to be quite strict and fails
            // cleaning up tmp files on macOS
            LOGGER.warning("Failed to clean up " + file + ": " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCopyFilesTo() throws Exception {
        File targetDir = makeDir(base, "target");

        // Make structure to copy into targetDir
        File sourceDir = makeDir(base, "source");
        File file1 =  writeFile(new File(sourceDir, "file1"), "file1-first");
        File file2 = writeFile(new File(sourceDir, "sub1/file2"), "file2-first");

        // Copy all files. Make sure they are identical.
        FileUtil.copyFilesTo(targetDir, sourceDir.listFiles(), null);
        final File file1trg = new File(targetDir, "file1");
        final File sub1trg = new File(targetDir, "sub1");
        final File file2trg = new File(targetDir, "sub1/file2");
        assertTrue(fileContentsAreEqual(file1, file1trg));
        assertTrue(new File(targetDir, "sub1").isDirectory());
        assertTrue(fileContentsAreEqual(file2, file2trg));

        // Modify source files.
        File file3 = new File(sourceDir, "file3");
        writeFile(file3, "file3-first");
        writeFile(file1, "file1-second");
        writeFile(file2, "file2-second");

        // Do copy but don't overwrite anything. Only file3 should be copied.
        FileUtil.copyFilesTo(targetDir, sourceDir.listFiles(), new ICollisionCallback() {
            @Override
            public boolean shouldReplace(File file, int thisFile, int totalFiles) {
                return false;
            }
            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        File file3trg = new File(targetDir, "file3");
        assertFalse(fileContentsAreEqual(file1, file1trg));
        assertFalse(fileContentsAreEqual(file2, file2trg));
        assertTrue(fileContentsAreEqual(file3, file3trg));

        // Add sub1/file4 to target; this will disappear after replacing sub1.
        File file4trg = writeFile(new File(targetDir, "sub1/file4"), "file4");

        // Do copy and overwrite sub1 only. sub1/file2 should be replaced
        // and sub1/file4 should disappear.
        FileUtil.copyFilesTo(targetDir, sourceDir.listFiles(), new ICollisionCallback() {
            @Override
            public boolean shouldReplace(File file, int thisFile, int totalFiles) {
                return file.equals(sub1trg);
            }
            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        assertFalse(fileContentsAreEqual(file1, file1trg));
        assertTrue(fileContentsAreEqual(file2, file2trg));
        assertTrue(fileContentsAreEqual(file3, file3trg));
        assertFalse(file4trg.exists());

        // Do copy and cancel on last file. None of the files should be changed.
        // shouldReplace() should be called once for all files in source/.
        CountingCallback callback = new CountingCallback() {
            private boolean isCanceled = false;

            @Override
            public boolean shouldReplace(File file, int thisFile, int totalFiles) {
                calledTimes++;
                isCanceled = thisFile + 1 == totalFiles;
                return !isCanceled();
            }

            @Override
            public boolean isCanceled() {
                return isCanceled;
            }
        };
        FileUtil.copyFilesTo(targetDir, sourceDir.listFiles(), callback);
        assertEquals(sourceDir.listFiles().length, callback.calledTimes);
        assertFalse(fileContentsAreEqual(file1, file1trg));
        assertTrue(fileContentsAreEqual(file2, file2trg));
        assertTrue(fileContentsAreEqual(file3, file3trg));

        // Try copying to non-existent destination.
        File newTarget = new File(base, "newtarget");
        FileUtil.copyFilesTo(newTarget, sourceDir.listFiles(), null);
        assertTrue(newTarget.isDirectory());
        assertTrue(fileContentsAreEqual(file1, new File(newTarget, "file1")));

        // Try copying to destination that exists but is a file.
        File targetFile = writeFile(new File(base, "targetFile"), "");
        try {
            FileUtil.copyFilesTo(targetFile, sourceDir.listFiles(), null);
            fail("copyFilesTo should fail when target dir is a file.");
        } catch (IOException ex) {
        }
    }

    private abstract class CountingCallback implements ICollisionCallback {
        int calledTimes = 0;
    }

    private File makeDir(File parent, String... names) {
        File dir = parent;
        for (String name : names) {
            dir = new File(dir, name);
        }
        assertTrue(dir.mkdirs());
        return dir;
    }

    private File writeFile(File file, String content) throws FileNotFoundException, UnsupportedEncodingException {
        File dir = file.getParentFile();
        if (!dir.isDirectory()) {
            assertTrue(dir.mkdirs());
        }
        PrintStream stream = new PrintStream(file, StandardCharsets.US_ASCII.name());
        stream.println(content);
        stream.close();
        return file;
    }

    private boolean fileContentsAreEqual(File file1, File file2) throws IOException {
        return Arrays.equals(FileUtils.readFileToByteArray(file1), FileUtils.readFileToByteArray(file2));
    }

    @Test
    public void testRelative() throws Exception {
        assertFalse(FileUtil.isRelative("C:\\zz"));
        assertFalse(FileUtil.isRelative("z:/zz"));
        assertFalse(FileUtil.isRelative("c:\\zz"));
        assertFalse(FileUtil.isRelative("z:/zz"));
        assertTrue(FileUtil.isRelative("1:/zz"));
        assertFalse(FileUtil.isRelative("/zz"));
        assertFalse(FileUtil.isRelative("\\zz"));
        assertTrue(FileUtil.isRelative("zz/"));
    }

    @Test
    public void testAbsoluteForSystem() {
        if (Platform.isWindows) {
            assertEquals("C:/zzz", FileUtil.absoluteForSystem("C:\\zzz"));
        } else {
            assertEquals("/zzz", FileUtil.absoluteForSystem("C:\\zzz"));
        }
        assertEquals("/zzz", FileUtil.absoluteForSystem("\\zzz"));
    }

    @Test
    public void testEOL() throws Exception {
        File dir = new File("build/testdata/");
        dir.mkdirs();

        File in = new File(dir, "in.eol");
        File out = new File(dir, "out.eol");

        byte[] eoln = "12\n34\n56\n".getBytes("UTF-8");
        byte[] eolr = "12\r34\r56\r".getBytes("UTF-8");
        byte[] eolrn = "12\r\n34\r\n56\r\n".getBytes("UTF-8");
        byte[][] eols = new byte[][] { eoln, eolr, eolrn };

        FileUtils.writeByteArrayToFile(out, eoln);
        assertEquals("\n", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, StandardCharsets.UTF_8);
            assertEquals("\n", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        }

        FileUtils.writeByteArrayToFile(out, eolr);
        assertEquals("\r", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, StandardCharsets.UTF_8);
            assertEquals("\r", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        }

        FileUtils.writeByteArrayToFile(out, eolrn);
        assertEquals("\r\n", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, StandardCharsets.UTF_8);
            assertEquals("\r\n", FileUtil.getEOL(out, StandardCharsets.UTF_8));
        }

        FileUtils.writeByteArrayToFile(out, new byte[0]);
        assertEquals(null, FileUtil.getEOL(out, StandardCharsets.UTF_8));
    }

    @Test
    public void testDeleteTree() throws Exception {
        // /root
        File root = new File(base, "root");
        // /root/sub
        File sub = new File(root, "sub");
        assertTrue(sub.mkdirs());

        // /root2
        File root2 = new File(base, "root2");
        assertTrue(root2.mkdirs());
        File file = new File(root2, "file");
        assertTrue(file.createNewFile());

        try {
            // /root/sub/subsub -> /root2
            Files.createSymbolicLink(new File(sub, "subsub").toPath(), root2.toPath());
        } catch (UnsupportedOperationException | IOException ex) {
            // Creating symlinks not supported on this system
        }

        // Can't delete /root yet because it's not empty
        assertFalse(root.delete());
        FileUtils.deleteDirectory(root);
        assertFalse(root.exists());

        // Make sure we didn't follow the symlink
        assertTrue(file.exists());
    }

    @Test
    public void testCompileFileMask() {
        Pattern r = FileUtil.compileFileMask("Ab1-&*/**");
        assertEquals("(?:|.*/)Ab1\\-\\&[^/]*(?:|/.*)", r.pattern());
    }

    @Test
    public void testFilePatterns() {
        // From
        // https://confluence.atlassian.com/fisheye/pattern-matching-guide-298976797.html

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

        assertEquals(FileUtil.compileFileMask("mypackage/test/**").pattern(),
                FileUtil.compileFileMask("mypackage/test/").pattern());

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
        Pattern p = FileUtil.compileFileMask(pattern);
        return p.matcher(path).matches();
    }

    @Test
    public void testBuildFileList() throws Exception {
        File tempDir = Files.createTempDirectory("omegat").toFile();
        assertTrue(tempDir.isDirectory());

        File subDir = new File(tempDir, "a");
        assertTrue(subDir.mkdirs());

        File aFile = new File(subDir, "foo");
        assertTrue(aFile.createNewFile());
        aFile = new File(subDir, "bar");
        assertTrue(aFile.createNewFile());

        List<File> list1 = FileUtil.buildFileList(tempDir, false);
        assertTrue(list1.isEmpty());

        List<File> list2 = FileUtil.buildFileList(tempDir, true);
        assertEquals(2, list2.size());

        Collections.sort(list2);
        assertTrue(list2.get(0).getPath().endsWith("bar"));

        try {
            File lnk = new File(tempDir, "hoge");
            Files.createSymbolicLink(lnk.toPath(), subDir.toPath());
            List<File> list3 = FileUtil.buildFileList(lnk, true);
            List<File> list4 = FileUtil.buildFileList(subDir, true);
            assertEquals(list3.size(), list4.size());
            assertTrue(IntStream.range(0, list3.size()).allMatch(i -> {
                try {
                    return list3.get(i).getCanonicalFile().equals(list4.get(i).getCanonicalFile());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }));
        } catch (UnsupportedOperationException | IOException ex) {
            // Creating symbolic links appears to not be supported on this
            // system
        }

        try {
            Files.createSymbolicLink(new File(tempDir, "baz").toPath(), tempDir.toPath());
            FileUtil.buildFileList(tempDir, true);
            fail("Should die from file system loop");
        } catch (UnsupportedOperationException | IOException ex) {
            // Creating symbolic links appears to not be supported on this
            // system
        } catch (UncheckedIOException ex) {
            if (!(ex.getCause() instanceof FileSystemLoopException)) {
                throw ex;
            }
            // Creating symbolic links appears to not be supported on this
            // system
        }

        safeDeleteDirectory(tempDir);
    }

    @Test
    public void testGetUniqueNames() {
        // Singleton case, all platform conventions
        assertEquals(Arrays.asList("foo.txt"), FileUtil.getUniqueNames(Arrays.asList("C:\\foo\\foo.txt")));
        assertEquals(Arrays.asList("foo.txt"), FileUtil.getUniqueNames(Arrays.asList("C:/foo/foo.txt")));
        assertEquals(Arrays.asList("foo.txt"), FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt")));

        // All unique, all platform conventions
        assertEquals(Arrays.asList("foo.txt", "bar.txt", "baz.txt"),
                FileUtil.getUniqueNames(Arrays.asList("C:\\foo\\foo.txt", "C:\\foo\\bar.txt", "C:\\bar\\baz.txt")));
        assertEquals(Arrays.asList("foo.txt", "bar.txt", "baz.txt"),
                FileUtil.getUniqueNames(Arrays.asList("C:/foo/foo.txt", "C:/foo/bar.txt", "C:/bar/baz.txt")));
        assertEquals(Arrays.asList("foo.txt", "bar.txt", "baz.txt"),
                FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt", "/foo/bar.txt", "/bar/baz.txt")));

        // One-level duplicate, all platform conventions
        assertEquals(Arrays.asList("foo.txt", "foo/bar.txt", "bar/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("C:\\foo\\foo.txt", "C:\\foo\\bar.txt", "C:\\bar\\bar.txt")));
        assertEquals(Arrays.asList("foo.txt", "foo/bar.txt", "bar/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("C:/foo/foo.txt", "C:/foo/bar.txt", "C:/bar/bar.txt")));
        assertEquals(Arrays.asList("foo.txt", "foo/bar.txt", "bar/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt", "/foo/bar.txt", "/bar/bar.txt")));

        // Unhelpful extra depth doesn't affect result
        assertEquals(Arrays.asList("foo.txt", "foo/bar.txt", "bar/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt", "/foo/bar.txt", "/faz/bar/bar.txt")));

        // Two-level duplicate
        assertEquals(Arrays.asList("foo.txt", "faz/foo/bar.txt", "fop/foo/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt", "/faz/foo/bar.txt", "/fop/foo/bar.txt")));

        // Actual duplicates result in full original paths
        assertEquals(Arrays.asList("foo.txt", "/foo/bar.txt", "/foo/bar.txt"),
                FileUtil.getUniqueNames(Arrays.asList("/foo/foo.txt", "/foo/bar.txt", "/foo/bar.txt")));

        // Test normalization
        assertEquals(Arrays.asList("foo", "bar/baz", "buz/baz", "baz/baz"),
                FileUtil.getUniqueNames(Arrays.asList("foo/", "bar/boo/../baz", "/buz/baz", "/baz//baz")));

        // Un-normalizable paths are untouched
        assertEquals(Arrays.asList("//foo", "../foo"), FileUtil.getUniqueNames(Arrays.asList("//foo", "../foo")));
        // Singleton case
        assertEquals(Arrays.asList("../foo"), FileUtil.getUniqueNames(Arrays.asList("../foo")));

        // Non-path
        assertEquals(Arrays.asList("My Awesome Glossary"),
                FileUtil.getUniqueNames(Arrays.asList("My Awesome Glossary")));
    }

    @Test
    public void testBackupFilename() throws IOException {
        File tempDir = Files.createTempDirectory("omegat").toFile();
        assertTrue(tempDir.isDirectory());

        File original = new File(tempDir, "backup.test");
        original.createNewFile();
        original.setLastModified(1684085727566l);

        assertTrue(original.exists());
        assertEquals(1684085727566l, original.lastModified());
        assertEquals("backup.test.202305141735.bak", FileUtil.getBackupFilename(original));

        safeDeleteDirectory(tempDir);
    }
}
