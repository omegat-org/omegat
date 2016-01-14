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
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.omegat.filters.TestFilterBase;

import junit.framework.TestCase;

public class LFileCopyTest extends TestCase {

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        tempDir = FileUtil.createTempDir();
        assertTrue(tempDir.isDirectory());
    }

    public void testFileCopy() throws Exception {
        // Create file "a" with contents "foobar".
        File a = new File(tempDir, "a");
        PrintWriter fw = new PrintWriter(a, "UTF-8");
        fw.println("foobar");
        fw.close();
        assertFalse(fw.checkError());

        File b = new File(tempDir, "b");

        // Copy file "a" to file "b". Contents should be identical.
        FileUtils.copyFile(a, b);
        TestFilterBase.compareBinary(a, b);

        // Copy file "a" to itself. Contents should remain identical
        // and not get clobbered per https://sourceforge.net/p/omegat/bugs/787/
        FileUtils.copyFile(a, a);
        TestFilterBase.compareBinary(a, b);
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(FileUtil.deleteTree(tempDir));
    }
}
