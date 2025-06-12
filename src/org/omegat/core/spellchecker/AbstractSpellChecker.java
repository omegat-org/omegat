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
package org.omegat.core.spellchecker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.core.data.IProject;
import org.omegat.util.Log;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Token;

/**
 * Abstract spell checker with method to handle ISpellCheckerProvider.
 */
public abstract class AbstractSpellChecker implements ISpellChecker {

    /**
     * The spell checking provider.
     */
    private ISpellCheckerProvider checker;

    /**
     * the list of ignored words
     */
    private final List<String> ignoreList = new ArrayList<>();

    /**
     * the list of learned (valid) words
     */
    private final List<String> learnedList = new ArrayList<>();

    /**
     * Cache of correct words.
     */
    private final Set<String> correctWordsCache = new HashSet<>();
    /**
     * Cache of incorrect words.
     */
    private final Set<String> incorrectWordsCache = new HashSet<>();

    public AbstractSpellChecker() {
        CoreEvents.registerProjectChangeListener(eventType -> {
            switch (eventType) {
                case LOAD:
                case CREATE:
                    initialize();
                    break;
                case SAVE:
                    saveWordLists();
                    break;
                case CLOSE:
                    destroy();
                    break;
                default:
                    // Nothing
            }
            resetCache();
        });
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                resetCache();
            }

            public void onEntryActivated(SourceTextEntry newEntry) {
            }
        });
    }

    /**
     * Initialize the library for the given project. Loads the lists of ignored
     * and learned words for the project
     */
    public boolean initialize() {
        Language targetLanguage = Core.getProject().getProjectProperties().getTargetLanguage();

        // check targets "xx_YY", "xx-YY" and "xx" only
        Stream<String> toCheck = Stream.of(targetLanguage.getLocaleCode(),
                targetLanguage.getLocaleCode().replace('_', '-'), targetLanguage.getLanguageCode());

        checker = toCheck.map(this::initializeWithLanguage).filter(Optional::isPresent).findFirst()
                .orElseGet(Optional::empty).orElse(null);

        if (checker == null) {
            Log.logWarningRB("SPELLCHECKER_LANGUAGE_NOT_FOUND", targetLanguage);
            return false;
        } else {
            return loadWordLists();
        }
    }

    protected abstract Optional<ISpellCheckerProvider> initializeWithLanguage(String language);

    protected boolean isInvalidFile(File file) {
        try {
            if (!file.exists()) {
                return true;
            }
            if (!file.isFile()) {
                Log.logWarningRB("SPELLCHECKER_DICTIONARY_NOT_FILE", file.getPath());
                return true;
            }
            if (!file.canRead()) {
                Log.logWarningRB("SPELLCHECKER_DICTIONARY_NOT_READ", file.getPath());
                return true;
            }
            if (file.length() == 0L) {
                // On OS X, attempting to load Hunspell with a zero-length .dic
                // file causes
                // a native exception that crashes the whole program.
                Log.logWarningRB("SPELLCHECKER_DICTIONARY_EMPTY", file.getPath());
                return true;
            }
            return false;
        } catch (Throwable ex) {
            Log.logWarningRB("SPELLCHECKER_DICTIONARY_FAILED_ACCESS", file.getPath(), ex.getLocalizedMessage());
            return true;
        }
    }

    /**
     * Loading word list files into ignoreList and learnedList collections.
     * @return true when succeeded, otherwise false.
     */
    protected boolean loadWordLists() {
        // find out the internal project directory
        String projectDir = Core.getProject().getProjectProperties().getProjectInternal();

        // load the ignored word list
        ignoreList.clear();
        Path ignoreFilePath = Paths.get(projectDir, OConsts.IGNORED_WORD_LIST_FILE_NAME);
        if (ignoreFilePath.toFile().exists() && ignoreFilePath.toFile().isFile() && ignoreFilePath.toFile().canRead()) {
            try {
                ignoreList.addAll(Files.readAllLines(ignoreFilePath, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                Log.logWarningRB("SPELLCHECKER_IGNORE_FILE_READ_ERROR", ex.getLocalizedMessage());
                return false;
            }
        }

        // now the correct words
        learnedList.clear();
        Path learnedFilePath = Paths.get(projectDir, OConsts.LEARNED_WORD_LIST_FILE_NAME);
        if (learnedFilePath.toFile().exists() && learnedFilePath.toFile().isFile()
                && learnedFilePath.toFile().canRead()) {
            try {
                learnedList.addAll(Files.readAllLines(learnedFilePath, StandardCharsets.UTF_8));
                learnedList.forEach(word -> checker.learnWord(word));
            } catch (Exception ex) {
                Log.logWarningRB("SPELLCHECKER_LEARNED_FILE_READ_ERROR", ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * destroy the library
     */
    public void destroy() {
        if (checker != null) {
            checker.destroy();
            checker = null;
        }
    }

    protected void resetCache() {
        synchronized (this) {
            incorrectWordsCache.clear();
            correctWordsCache.clear();
        }
    }

    /**
     * Save the word lists to disk
     */
    public void saveWordLists() {
        // find out the internal project directory
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String projectDir = Core.getProject().getProjectProperties().getProjectInternal();
        if (!Paths.get(projectDir).toFile().exists() || !Paths.get(projectDir).toFile().isDirectory()) {
            return;
        }

        // Write the ignored and learned words to the disk
        Path ignoreFilePath = Paths.get(projectDir, OConsts.IGNORED_WORD_LIST_FILE_NAME);
        try {
            Files.write(ignoreFilePath, ignoreList);
        } catch (IOException ex) {
            Log.logWarningRB("SPELLCHECKER_IGNORE_FILE_WRITE_ERROR", ex.getLocalizedMessage());
        }

        Path learnedFilePath = Paths.get(projectDir, OConsts.LEARNED_WORD_LIST_FILE_NAME);
        try {
            Files.write(learnedFilePath, learnedList);
        } catch (IOException ex) {
            Log.logWarningRB("SPELLCHECKER_LEARNED_FILE_WRITE_ERROR", ex.getLocalizedMessage());
        }
    }

    /**
     * Check the word. If it is ignored or learned (valid), returns true.
     * Otherwise, false.
     */
    public boolean isCorrect(String word) {
        // Check if a spellchecker is already initialized. If not, skip
        // checking to prevent nullPointerErrors.
        if (checker == null) {
            return true;
        }

        word = normalize(word);

        // check in cache first
        synchronized (this) {
            if (incorrectWordsCache.contains(word)) {
                return false;
            } else if (correctWordsCache.contains(word)) {
                return true;
            }
        }

        boolean isCorrect;

        // if it is valid (learned), it is ok
        if (learnedList.contains(word) || ignoreList.contains(word)) {
            isCorrect = true;
        } else {
            isCorrect = checker.isCorrect(word);
        }

        // remember in cache
        synchronized (this) {
            if (isCorrect) {
                correctWordsCache.add(word);
            } else {
                incorrectWordsCache.add(word);
            }
        }
        return isCorrect;
    }

    /**
     * return a list of strings as suggestions
     */
    public List<String> suggest(String word) {
        // Check if a spellchecker is already initialized.
        // If not, skip checking to prevent nullPointerErrors.
        if (checker == null) {
            return Collections.emptyList();
        }
        if (isCorrect(word)) {
            return Collections.emptyList();
        }

        return checker.suggest(normalize(word));
    }

    /**
     * Add a word to the list of ignored words
     */
    public void ignoreWord(String word) {
        word = normalize(word);
        if (!ignoreList.contains(word)) {
            ignoreList.add(word);
            synchronized (this) {
                incorrectWordsCache.remove(word);
                correctWordsCache.add(word);
            }
        }
    }

    /**
     * Add a word to the list of correct words
     */
    public void learnWord(String word) {
        word = normalize(word);
        if (!learnedList.contains(word)) {
            learnedList.add(word);
            checker.learnWord(word);
            synchronized (this) {
                incorrectWordsCache.remove(word);
                correctWordsCache.add(word);
            }
        }
    }

    @Override
    public boolean isIgnoredWord(String word) {
        return ignoreList.contains(normalize(word));
    }

    @Override
    public boolean isLearnedWord(String word) {
        return learnedList.contains(normalize(word));
    }

    /**
     * Normalize the orthography of the word by replacing
     * alternative characters with "canonical" ones.
     */
    private static String normalize(String word) {
        // U+2019 RIGHT SINGLE QUOTATION MARK to U+0027 APOSTROPHE
        return word.replace('\u2019', '\'');
    }

    @Override
    public List<Token> getMisspelledTokens(String text) {
        return Stream.of(Core.getProject().getTargetTokenizer().tokenizeWords(text, ITokenizer.StemmingMode.NONE))
                .filter(tok -> !isCorrect(tok.getTextFromString(text))).collect(Collectors.toList());
    }
}
