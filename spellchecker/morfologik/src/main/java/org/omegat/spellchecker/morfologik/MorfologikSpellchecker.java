/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura
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

package org.omegat.spellchecker.morfologik;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.apache.commons.io.IOUtils;
import org.languagetool.JLanguageTool;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.AbstractSpellChecker;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.ISpellCheckerProvider;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class MorfologikSpellchecker extends AbstractSpellChecker implements ISpellChecker {

    private static final ILogger LOGGER = LoggerFactory.getLogger(MorfologikSpellchecker.class);
    private static final String SC_DICT_EXTENSION = ".dict";
    private static final String SC_INFO_EXTENSION = ".info";

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerSpellCheckClass(MorfologikSpellchecker.class);
    }

    public static void unloadPlugins() {
    }

    public MorfologikSpellchecker() {
        super();
    }

    @Override
    protected Optional<ISpellCheckerProvider> initializeWithLanguage(final String language) {
        // check that the dict exists
        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellCheckerManager.DEFAULT_DICTIONARY_DIR.getPath());
        File dictionaryFile = new File(dictionaryDir, language + ".dict");
        File infoFile = new File(dictionaryDir, language + ".info");
        if (!dictionaryFile.exists()) {
            // Try installing from LanguageTool bundled resources
            installLTBundledDictionary(dictionaryDir, language);
        }
        if (dictionaryFile.exists() && infoFile.exists()) {
            Dictionary dictionary;
            try {
                dictionary = Dictionary.read(dictionaryFile.toPath());
            } catch (IOException e) {
                LOGGER.atWarn().setCause(e).log();
                return Optional.empty();
            }
            ISpellCheckerProvider provider = new MorfologikSpellCheckerProvider(dictionary);
            return Optional.of(provider);
        }
        return Optional.empty();
    }

    /**
     * If there is a Morfologik dictionary for the current target language
     * bundled with LanguageTool, install it.
     */
    private void installLTBundledDictionary(String dictionaryDir, String language) {
        String resPath = "/" + new Language(language).getLanguageCode() + "/hunspell/" + language + SC_DICT_EXTENSION;
        String infoPath = "/" + new Language(language).getLanguageCode() + "/hunspell/" + language + SC_INFO_EXTENSION;
        if (!JLanguageTool.getDataBroker().resourceExists(resPath)) {
            return;
        }
        try {
            try (InputStream dictStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(resPath);
                 FileOutputStream fos = new FileOutputStream(
                         new File(dictionaryDir, language + SC_DICT_EXTENSION))) {
                IOUtils.copy(dictStream, fos);
            }
            try (InputStream dictStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(infoPath);
                 FileOutputStream fos = new FileOutputStream(
                         new File(dictionaryDir, language + SC_INFO_EXTENSION))) {
                IOUtils.copy(dictStream, fos);
            }
        } catch (Exception ex) {
            LOGGER.atWarn().setCause(ex).log();
        }
    }

    public static class MorfologikSpellCheckerProvider implements ISpellCheckerProvider {
        private final Speller speller;

        public MorfologikSpellCheckerProvider(Dictionary dictionary) {
            speller = new Speller(dictionary, 1);
        }

        @Override
        public boolean isCorrect(final String word) {
            synchronized (this) {
                return !speller.isMisspelled(word);
            }
        }

        @Override
        public List<String> suggest(final String word) {
            List<String> suggestions = new ArrayList<>();
            // slow for long words (the limit is arbitrary)
            if (word.length() < 50) {
                List<Speller.CandidateData> replacementCandidates = speller.findReplacementCandidates(word);
                for (Speller.CandidateData candidate : replacementCandidates) {
                    suggestions.add(candidate.getWord());
                }
            }
            List<Speller.CandidateData> runOnCandidates = speller.replaceRunOnWordCandidates(word);
            for (Speller.CandidateData runOnCandidate : runOnCandidates) {
                suggestions.add(runOnCandidate.getWord());
            }
            return suggestions;
        }

        @Override
        public void learnWord(final String word) {
        }

        @Override
        public void destroy() {
        }
    }
}
