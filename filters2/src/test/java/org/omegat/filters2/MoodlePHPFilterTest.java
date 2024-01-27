/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2018 Enrique Estevez Fernandez
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

package org.omegat.filters2;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

import org.omegat.core.data.IProject;
import org.omegat.filters.TestFilterBase;
import org.omegat.filters2.moodlephp.MoodlePHPFilter;

public class MoodlePHPFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        parse(new MoodlePHPFilter(), "src/test/resources/data/filters/MoodlePHP/file.php");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "src/test/resources/data/filters/MoodlePHP/file.php";
        IProject.FileInfo fi = loadSourceFiles(new MoodlePHPFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Accessibility", "access", null, null, null, null);
        checkMulti("Accessibility help", "accesshelp", null, null, null, null);
        checkMulti("Unrecognised options:\n" +
                "  {$a}\n" +
                "Please use --help option.", "cliunknowoption", null, null, null, null);
        checkMulti("You cannot uninstall the \\'{$a->filter}\\' because it is part of the \\'{$a->module}\\' module.", "cannotdeletemodfilter", null, null, null, null);
        checkMulti("List of groups or contexts whose members are allowed to create attributes. Separate multiple groups with \\';\\'. Usually something like \\'cn=teachers,ou=staff,o=myorg\\'", "auth_ldap_attrcreators", null, null, null, null);
        checkMultiEnd();
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new MoodlePHPFilter(),
                "src/test/resources/data/filters/MoodlePHP/file.php");
    }

    @Test
    public void testAlign() throws Exception {
        final AlignResult ar = new AlignResult();
        align(new MoodlePHPFilter(),
                Paths.get("src/test/resources/data/filters/MoodlePHP/filesAlign.php").toAbsolutePath().toFile(),
                Paths.get("src/test/resources/data/filters/MoodlePHP/filesAlign_gl.php").toAbsolutePath().toFile(),
                (id, source, translation, isFuzzy, path, filter) -> ar.found = id.equals("access")
                        && source.equals("Accessibility") && translation.equals("Accesibilidade"));
        assertTrue(ar.found);
    }

    public static class AlignResult {

        boolean found = false;
    }
}
