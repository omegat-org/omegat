/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2007 Zoltan Bartko, Alex Buloichik
 *                2009 Didier Briel
 *                2015 Aaron Madlon-Kay
 *                2020 Briac Pilpre
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.spellchecker.hunspell;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import dumonts.hunspell.Hunspell;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.AbstractSpellChecker;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.ISpellCheckerProvider;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.util.Preferences;

/**
 * HunSpell spell checker.
 *
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Briac Pilpre
 */
public class HunSpellChecker extends AbstractSpellChecker implements ISpellChecker {

    private static final ResourceBundle BUNDLE = ResourceBundle
            .getBundle("org.omegat.spellchecker.hunspell.Bundle");
    private static final ILogger LOGGER = LoggerFactory.getLogger(HunSpellChecker.class, BUNDLE);

    /**
     * affix file extension
     */
    public static final String SC_AFFIX_EXTENSION = ".aff";

    /**
     * dictionary file extension
     */
    public static final String SC_DICTIONARY_EXTENSION = ".dic";

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerSpellCheckClass(HunSpellChecker.class);
    }

    public static void unloadPlugins() {
    }

    public HunSpellChecker() {
        super();
    }

    @Override
    protected Optional<ISpellCheckerProvider> initializeWithLanguage(String language) {
        // check that the dict exists
        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellCheckerManager.getDefaultDictionaryDir().getPath());

        File affixName = new File(dictionaryDir, language + SC_AFFIX_EXTENSION);
        File dictionaryName = new File(dictionaryDir, language + SC_DICTIONARY_EXTENSION);

        if (!affixName.exists()) {
            // Try installing from Language module
            if (SpellCheckerManager.installHunspellDictionary(dictionaryDir, language) == null) {
                return Optional.empty();
            }
        }

        // If we still don't have a dictionary, then return
        if (isInvalidFile(affixName) || isInvalidFile(dictionaryName)) {
            return Optional.empty();
        }

        try {
            ISpellCheckerProvider result = new HunspellProvider(dictionaryName, affixName);
            LOGGER.atInfo().logRB("SPELLCHECKER_HUNSPELL_INITIALIZED", language, dictionaryName);
            return Optional.of(result);
        } catch (Throwable ex) {
            LOGGER.atWarn().logRB("SPELLCHECKER_HUNSPELL_EXCEPTION", ex.getMessage());
        }
        return Optional.empty();
    }

    /**
     * A thin wrapper around the LanguageTool Hunspell implementation (which
     * itself wraps native libs)
     *
     * @author Aaron Madlon-Kay
     * @author Briac Pilpre
     */
    public static final class HunspellProvider implements ISpellCheckerProvider {
        private final Hunspell dict;
        private boolean closed = false;

        public HunspellProvider(final File dictName, final File affixName) {
            try {
                this.dict = new Hunspell(dictName.toPath(), affixName.toPath());
            } catch (UnsatisfiedLinkError e) {
                throw new RuntimeException(
                        "Could not create hunspell instance. Please note that LanguageTool supports only 64-bit "
                                + "platforms (Linux, Windows, Mac) and that it requires a 64-bit JVM (Java).",
                        e);
            }
        }

        @Override
        public boolean isCorrect(String word) {
            if (closed) {
                throw new RuntimeException("Attempt to use hunspell instance after closing");
            }
            return dict.spell(word);
        }

        @Override
        public List<String> suggest(String word) {
            if (closed) {
                throw new RuntimeException("Attempt to use hunspell instance after closing");
            }
            return Arrays.asList(dict.suggest(word));
        }

        @Override
        public void learnWord(String word) {
            if (closed) {
                throw new RuntimeException("Attempt to use hunspell instance after closing");
            }
            dict.add(word);
        }

        @Override
        public void destroy() {
            closed = true;
            dict.close();
        }
    }
}
