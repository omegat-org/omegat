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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;

public final class SpellCheckerManager {

    private static ISpellChecker spellChecker;

    private SpellCheckerManager() {
    }

    public static File getDefaultDictionaryDir() {
        return new File(StaticUtils.getConfigDir(), OConsts.SPELLING_DICT_DIR);
    }

    /**
     * Get a spell checker engine used currently.
     * 
     * @return spell checker engine.
     */
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

    private static final Map<String, String> morfologikDictionaryProviders = new HashMap<>();
    private static final Map<String, String> hunspellDictionaryProviders = new HashMap<>();

    public static Set<String> getMorfologikDictionaryLanguages() {
        return morfologikDictionaryProviders.keySet();
    }

    public static Set<String> getHunspellDictionaryLanguages() {
        return hunspellDictionaryProviders.keySet();
    }

    /**
     * The entry point to register spell checker dictionary class.
     * 
     * @param lang
     *            supported language e.g., en, en_US
     * @param type
     *            enum type of dictionary.
     * @param dictionaryProvider
     *            full qualified class path of the dictionary provider.
     */
    public static void registerSpellCheckerDictionaryProvider(String lang, SpellCheckDictionaryType type,
            String dictionaryProvider) {
        if (Objects.requireNonNull(type) == SpellCheckDictionaryType.HUNSPELL) {
            hunspellDictionaryProviders.put(lang, dictionaryProvider);
        } else if (type == SpellCheckDictionaryType.MORFOLOGIK) {
            morfologikDictionaryProviders.put(lang, dictionaryProvider);
        }
    }

    /**
     * Install hunspell dictionary into the specified directory.
     * 
     * @param dictionaryDir
     *            target directory.
     * @param language
     *            target dictionary language.
     * @return Path of the dictionary installed.
     */
    public static Path installHunspellDictionary(String dictionaryDir, String language) {
        String className = hunspellDictionaryProviders.get(language);
        if (className == null) {
            return null;
        }
        ISpellCheckerDictionary dictionaryProvider = getSpellCheckerDictionary(className);
        return dictionaryProvider.installHunspellDictionary(Paths.get(dictionaryDir), language);
    }

    /**
     * Provide the Hunspell dictionary object.
     * 
     * @param language
     *            target language e.g, en, en_US
     * @return Lucene hunspell Dictionary object.
     */
    public static org.apache.lucene.analysis.hunspell.Dictionary getHunspellDictionary(String language) {
        String className = hunspellDictionaryProviders.get(language);
        if (className == null) {
            return null;
        }
        ISpellCheckerDictionary dictionaryProvider = getSpellCheckerDictionary(className);
        return dictionaryProvider.getHunspellDictionary(language);
    }

    /**
     * Provide the Mofologik dictionary object.
     * 
     * @param language
     *            target language, e.g., en, en_US
     * @return Morfologik Dictionary object.
     */
    public static morfologik.stemming.Dictionary getMorfologikDictionary(String language) {
        String className = morfologikDictionaryProviders.get(language);
        if (className == null) {
            return null;
        }
        ISpellCheckerDictionary dictionaryProvider = getSpellCheckerDictionary(className);
        return dictionaryProvider.getMorfologikDictionary(language);
    }

    private static ISpellCheckerDictionary getSpellCheckerDictionary(String className) {
        try {
            Class<?> aClass = PluginUtils.getLanguageClassLoader().loadClass(className);
            Constructor<?> constructor = aClass.getConstructor();
            return (ISpellCheckerDictionary) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class '" + className + " could not be found in classpath", e);
        } catch (Exception e) {
            throw new RuntimeException("Object for class '" + className + " could not be created", e);
        }
    }
}
