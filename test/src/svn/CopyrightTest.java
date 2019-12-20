/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package svn;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Test for copyright notes exists in source files.
 *
 * @author Alex Buloichik
 */
public class CopyrightTest {
    protected static final String[] MUST_EXIST = new String[] { "OmegaT - Computer Assisted Translation (CAT) tool",
            "Copyright (C)", "Home page: http://www.omegat.org/", "This file is part of OmegaT",
            "OmegaT is free software: you can redistribute it and/or modify",
            "it under the terms of the GNU General Public License as published by",
            "the Free Software Foundation, either version 3 of the License, or", "(at your option) any later version.",
            "OmegaT is distributed in the hope that it will be useful,",
            "but WITHOUT ANY WARRANTY; without even the implied warranty of",
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the",
            "GNU General Public License for more details.",
            "You should have received a copy of the GNU General Public License",
            "along with this program.  If not, see <http://www.gnu.org/licenses/>." };

    @Test
    public void testCopyright() throws Exception {
        List<File> sourceFiles = new ArrayList<File>();
        list(new File("src"), sourceFiles);
        list(new File("test"), sourceFiles);
        ByteArrayOutputStream fdata = new ByteArrayOutputStream();
        for (File f : sourceFiles) {
            if (f.getPath().replace('\\', '/').startsWith("src/gen/")) {
                // skip jaxb generated files
                continue;
            }
            FileUtils.copyFile(f, fdata);
            String data = fdata.toString("ISO-8859-1");
            checkNote(f, data);
            fdata.reset();
        }
    }

    protected void checkNote(File f, String data) {
        int pos = data.indexOf("\npackage ");
        if (pos > 0) {
            data = data.substring(0, pos);
        }
        for (String con : MUST_EXIST) {
            assertTrue("There is no copyright note in '" + f.getAbsolutePath() + "' : " + con, data.contains(con));
        }
    }

    protected void list(File dir, List<File> files) {
        for (File f : dir.listFiles()) {
            String fn = f.getName();
            if (f.getName().endsWith(".java")) {
                files.add(f);
            } else if (fn.equals("build.xml")) {
                files.add(f);
            } else if (fn.endsWith(".properties")) {
                if (fn.startsWith("Version") || fn.startsWith("Bundle") || fn.startsWith("project")) {
                    files.add(f);
                }
            } else if (f.isDirectory()) {
                list(f, files);
            }
        }
    }
}
