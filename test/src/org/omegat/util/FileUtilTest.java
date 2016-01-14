/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay, Alex Buloichik
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.omegat.util.FileUtil.ICollisionCallback;

/**
 * @author Aaron Madlon-Kay
 * @author Alex Buloichik
 */
public class FileUtilTest extends TestCase {

    private File base;

    @Override
    protected void setUp() throws Exception {
        base = FileUtil.createTempDir();
        System.out.println("Base: " + base.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(FileUtil.deleteTree(base));
    }

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
    
    public void testGetFileExtension() {
        assertEquals("js", FileUtil.getFileExtension("foo.js"));
        assertEquals("zip", FileUtil.getFileExtension("foo.js/bar.zip"));
        assertEquals("zip", FileUtil.getFileExtension("C:\\foo.js\\bar.zip"));
        assertEquals("tar.gz", FileUtil.getFileExtension("foo.tar.gz"));
        assertEquals("", FileUtil.getFileExtension("foo"));
        assertEquals("", FileUtil.getFileExtension("foo/.bar"));
        assertEquals("", FileUtil.getFileExtension("foo\\.bar"));
    }

    public void testStripFileExtension() {
        assertEquals("foo", FileUtil.stripFileExtension("foo.js"));
        assertEquals("foo.js/bar", FileUtil.stripFileExtension("foo.js/bar.zip"));
        assertEquals("C:/foo.js/bar", FileUtil.stripFileExtension("C:\\foo.js\\bar.zip"));
        assertEquals("foo", FileUtil.stripFileExtension("foo.tar.gz"));
        assertEquals("foo", FileUtil.stripFileExtension("foo"));
        assertEquals("foo/.bar", FileUtil.stripFileExtension("foo/.bar"));
        assertEquals("foo/.bar", FileUtil.stripFileExtension("foo\\.bar"));
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

    private File writeFile(File file, String content) throws FileNotFoundException {
        File dir = file.getParentFile();
        if (!dir.isDirectory()) {
            assertTrue(dir.mkdirs());
        }
        PrintStream stream = new PrintStream(file);
        stream.println(content);
        stream.close();
        return file;
    }
    
    private boolean fileContentsAreEqual(File file1, File file2) throws IOException {
        return readFile(file1).equals(readFile(file2));
    }
    
    private String readFile(File file) throws IOException {
        InputStreamReader stream = new InputStreamReader(new FileInputStream(file));
        char[] cbuf = new char[256];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = stream.read(cbuf)) != -1) {
            sb.append(cbuf, 0, len);
        }
        stream.close();
        return sb.toString();
    }

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

    public void testAbsoluteForSystem() throws Exception {
        assertEquals("C:/zzz", FileUtil.absoluteForSystem("C:\\zzz", Platform.OsType.WIN64));
        assertEquals("/zzz", FileUtil.absoluteForSystem("C:\\zzz", Platform.OsType.LINUX64));
        assertEquals("/zzz", FileUtil.absoluteForSystem("C:\\zzz", Platform.OsType.MAC64));
        assertEquals("/zzz", FileUtil.absoluteForSystem("\\zzz", Platform.OsType.WIN64));
        assertEquals("/zzz", FileUtil.absoluteForSystem("\\zzz", Platform.OsType.LINUX64));
        assertEquals("/zzz", FileUtil.absoluteForSystem("\\zzz", Platform.OsType.MAC64));
    }

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
        assertEquals("\n", FileUtil.getEOL(out, "UTF-8"));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, "UTF-8");
            assertEquals("\n", FileUtil.getEOL(out, "UTF-8"));
        }

        FileUtils.writeByteArrayToFile(out, eolr);
        assertEquals("\r", FileUtil.getEOL(out, "UTF-8"));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, "UTF-8");
            assertEquals("\r", FileUtil.getEOL(out, "UTF-8"));
        }

        FileUtils.writeByteArrayToFile(out, eolrn);
        assertEquals("\r\n", FileUtil.getEOL(out, "UTF-8"));
        for (byte[] eolfrom : eols) {
            FileUtils.writeByteArrayToFile(in, eolfrom);
            FileUtil.copyFileWithEolConversion(in, out, "UTF-8");
            assertEquals("\r\n", FileUtil.getEOL(out, "UTF-8"));
        }
    }
}
