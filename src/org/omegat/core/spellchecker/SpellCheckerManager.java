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

public class SpellCheckerManager {

    private static ISpellChecker spellChecker;

    public static final File DEFAULT_DICTIONARY_DIR = new File(StaticUtils.getConfigDir(),
            OConsts.SPELLING_DICT_DIR);

    public static ISpellChecker getCurrentSpellChecker() {
        if (spellChecker != null) {
            return spellChecker;
        }
        // Try to use a custom spell checker if one is available.
        Class<?> defaultSpellCheckerClass = null;
        for (Class<?> customSpellChecker : PluginUtils.getSpellCheckClasses()) {
            // Try default spellchecker last.
            if ("org.omegat.core.spellchecker.DefaultSpellChecker".equals(customSpellChecker.getName())) {
                defaultSpellCheckerClass = customSpellChecker;
                continue;
            }
            try {
                spellChecker =
                        (ISpellChecker) customSpellChecker.getDeclaredConstructor().newInstance();
                return spellChecker;
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InstantiationException ignored) {
            }
        }
        if (spellChecker == null && defaultSpellCheckerClass != null) {
            try {
                spellChecker =
                        (ISpellChecker) defaultSpellCheckerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                spellChecker = new SpellCheckerDummy();
            }
        } else {
            spellChecker = new SpellCheckerDummy();
            Log.log("No active spell checker engine found");
        }
        return spellChecker;
    }
}
