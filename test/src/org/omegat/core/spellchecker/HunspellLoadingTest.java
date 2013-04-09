/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

package org.omegat.core.spellchecker;

import java.io.File;

import org.omegat.util.OConsts;

import junit.framework.TestCase;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Test for hunspell loading for current platform.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class HunspellLoadingTest extends TestCase {
    public void testWin32() throws Exception {
        String baseHunspellLib = SpellCheckerHunspell.getBaseHunspellLibraryName();
        String libraryPath = new File(OConsts.NATIVE_LIBRARY_DIR,
                SpellCheckerHunspell.mapLibraryName(baseHunspellLib)).getAbsolutePath();
        System.out.println("Loading hunspell from " + libraryPath);

        Hunspell hunspell = (Hunspell) Native.loadLibrary(libraryPath, Hunspell.class);
        Pointer pHunspell = hunspell.Hunspell_create("test/data/spelldicts/xx_XX.aff",
                "test/data/spelldicts/xx_XX.dic");
        String encoding = hunspell.Hunspell_get_dic_encoding(pHunspell);

        assertTrue(0 != hunspell.Hunspell_spell(pHunspell,
                SpellCheckerHunspell.prepareString("test", encoding)));
        assertFalse(0 != hunspell.Hunspell_spell(pHunspell,
                SpellCheckerHunspell.prepareString("tzzest", encoding)));

        hunspell.Hunspell_destroy(pHunspell);

        pHunspell = null;
    }
}
