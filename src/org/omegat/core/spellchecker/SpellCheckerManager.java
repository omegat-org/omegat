/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2023 Hiroshi Miura
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

package org.omegat.core.spellchecker;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;

public final class SpellCheckerManager {

    private static ISpellChecker spellChecker;

    public static final File DEFAULT_DICTIONARY_DIR = new File(StaticUtils.getConfigDir(),
            OConsts.SPELLING_DICT_DIR);

    private SpellCheckerManager() {
    }

    public static ISpellChecker getCurrentSpellChecker() {
        if (spellChecker != null) {
            return spellChecker;
        }
        // Try to use a custom spell checker if one is available.
        for (Class<?> customSpellChecker : PluginUtils.getSpellCheckClasses()) {
            try {
                spellChecker = (ISpellChecker) customSpellChecker.getDeclaredConstructor().newInstance();
                if (spellChecker.initialize()) {
                    return spellChecker;
                } else {
                    spellChecker = null;
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException
                     | InstantiationException ex) {
                Log.log(ex);
            }
        }
        spellChecker = new SpellCheckerDummy();
        Log.logRB("CORE_SPELLCHECKER_NO_ENGINE");
        return spellChecker;
    }
}
