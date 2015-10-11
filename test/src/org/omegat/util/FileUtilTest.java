/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

import org.omegat.util.FileUtil.ICollisionCallback;

import junit.framework.TestCase;

/**
 * @author Aaron Madlon-Kay
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
}
