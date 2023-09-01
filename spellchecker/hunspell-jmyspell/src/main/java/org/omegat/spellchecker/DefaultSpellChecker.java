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

package org.omegat.spellchecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.languagetool.JLanguageTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.ISpellCheckerProvider;
import org.omegat.core.spellchecker.SpellCheckerDummy;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.gui.preferences.PreferencesControllers;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * Default spell checker.
 *
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Briac Pilpre
 */
public class DefaultSpellChecker implements ISpellChecker {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("DefaultSpellChecker");
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSpellChecker.class);

    /** The spell checking provider. */
    private ISpellCheckerProvider checker;

    /** the list of ignored words */
    private final List<String> ignoreList = new ArrayList<>();

    /** the list of learned (valid) words */
    private final List<String> learnedList = new ArrayList<>();

    /** Cache of correct words. */
    private final Set<String> correctWordsCache = new HashSet<>();
    /** Cache of incorrect words. */
    private final Set<String> incorrectWordsCache = new HashSet<>();

    /**
     * the file name with the ignored words
     */
    private Path ignoreFilePath;

    /**
     * the file name with the learned words
     */
    private Path learnedFilePath;

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerSpellCheckClass(DefaultSpellChecker.class);
        PreferencesControllers.addSupplier(SpellcheckerConfigurationController::new);
    }

    public static void unloadPlugins() {
    }

    public DefaultSpellChecker() {
        CoreEvents.registerProjectChangeListener(eventType -> {
            switch (eventType) {
                case LOAD:
                case CREATE:
                    initialize();
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
     * Initialize the library for the given project. Loads the lists of ignored and learned words for the
     * project
     */
    public void initialize() {
        Language targetLanguage = Core.getProject().getProjectProperties().getTargetLanguage();

        Stream<String> toCheck = Stream.of(
                targetLanguage.getLocaleCode(), // Full xx_YY
                targetLanguage.getLocaleCode().replace('_', '-'), // Full xx-YY
                targetLanguage.getLanguageCode()); // xx only

        checker = toCheck.map(this::initializeWithLanguage) .filter(Optional::isPresent).findFirst()
                .orElseGet(Optional::empty).orElseGet(SpellCheckerDummy::new);
        loadWordLists();
    }

    private Optional<ISpellCheckerProvider> initializeWithLanguage(String language) {
        // check that the dict exists
        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellCheckerManager.DEFAULT_DICTIONARY_DIR.getPath());

        File affixName = new File(dictionaryDir, language + OConsts.SC_AFFIX_EXTENSION);
        File dictionaryName = new File(dictionaryDir, language + OConsts.SC_DICTIONARY_EXTENSION);

        // get the data from the preferences
        if (!dictionaryName.exists()) {
            // Try installing from bundled resources
            installBundledDictionary(dictionaryDir, language);
        }
        if (!dictionaryName.exists()) {
            // Try installing from LanguageTool bundled resources
            installLTBundledDictionary(dictionaryDir, language);
        }

        if (isInvalidFile(affixName) || isInvalidFile(dictionaryName)) {
            // If we still don't have a dictionary, then return
            LOGGER.info(BUNDLE.getString("SPELLCHECKER_LANGUAGE_NOT_FOUND"));
            return Optional.empty();
        }
        try {
            ISpellCheckerProvider result = new SpellCheckerLangToolHunspell(dictionaryName, affixName);
            LOGGER.info(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_HUNSPELL_INITIALIZED"),
                    language, dictionaryName));
            return Optional.of(result);
        } catch (Throwable ex) {
            LOGGER.warn(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_HUNSPELL_EXCEPTION"), ex.getMessage()));
        }
        try {
            ISpellCheckerProvider result = new SpellCheckerJMySpell(dictionaryName, affixName);
            LOGGER.info(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_JMYSPELL_INITIALIZED"), language
                    , dictionaryName));
            return Optional.of(result);
        } catch (Exception ex) {
            LOGGER.error(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_JMYSPELL_EXCEPTION"),
                    ex.getMessage()));
        }
        // Try to use a custom spell checker if one is available.
       return Optional.empty();
    }

    private boolean isInvalidFile(File file) {
        try {
            if (!file.exists()) {
                return true;
            }
            if (!file.isFile()) {
                LOGGER.warn(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_DICTIONARY_NOT_FILE"),
                        file.getPath()));
                return true;
            }
            if (!file.canRead()) {
               LOGGER.warn(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_DICTIONARY_NOT_READ"),
                       file.getPath()));
                return true;
            }
            if (file.length() == 0L) {
                // On OS X, attempting to load Hunspell with a zero-length .dic file causes
                // a native exception that crashes the whole program.
                LOGGER.warn(MessageFormat.format(BUNDLE.getString("SPELLCHECKER_DICTIONARY_EMPTY"),
                        file.getPath()));
                return true;
            }
            return false;
        } catch (Throwable ex) {
            LOGGER.warn("", ex);
            return true;
        }
    }

    /**
     * If there is a Hunspell dictionary for the current target language bundled
     * inside this OmegaT distribution, install it.
     */
    private static void installBundledDictionary(String dictionaryDir, String language) {
        try (InputStream bundledDict = SpellCheckerManager.class.getResourceAsStream(language + ".zip")) {
            if (bundledDict == null) {
                // The Relevant dictionary is not present.
                return;
            }
            StaticUtils.extractFromZip(bundledDict, new File(dictionaryDir),
                    Arrays.asList(language + OConsts.SC_AFFIX_EXTENSION,
                            language + OConsts.SC_DICTIONARY_EXTENSION)::contains);
        } catch (IOException e) {
            LOGGER.warn("", e);
        }
    }

    /**
     * If there is a Hunspell dictionary for the current target language bundled
     * with LanguageTool, install it. See <code>init()</code> and
     * <code>getDictionaryPath(String, String)</code> internal methods of
     * <code>org.languagetool.rules.spelling.hunspell.HunspellRule</code>.
     */
    private static void installLTBundledDictionary(String dictionaryDir, String language) {
        String resPath = "/" + new Language(language).getLanguageCode() + "/hunspell/" + language + ".dic";
        if (!JLanguageTool.getDataBroker().resourceExists(resPath)) {
            return;
        }
        try {
            try (InputStream dicStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(resPath);
                 FileOutputStream fos = new FileOutputStream(new File(dictionaryDir, language + ".dic"))) {
                IOUtils.copy(dicStream, fos);
            }
            try (InputStream affStream = JLanguageTool.getDataBroker()
                    .getFromResourceDirAsStream(resPath.replaceFirst(".dic$", ".aff"));
                 FileOutputStream fos = new FileOutputStream(new File(dictionaryDir, language + ".aff"))) {
                IOUtils.copy(affStream, fos);
            }
        } catch (Exception ex) {
            LOGGER.warn("", ex);
        }
    }

    private void loadWordLists() {
        // find out the internal project directory
        String projectDir = Core.getProject().getProjectProperties().getProjectInternal();

        // load the ignored word list
        ignoreFilePath = Paths.get(projectDir, OConsts.IGNORED_WORD_LIST_FILE_NAME);

        ignoreList.clear();
        if (ignoreFilePath.toFile().isFile()) {
            try {
                ignoreList.addAll(Files.readAllLines(ignoreFilePath, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                LOGGER.warn("", ex);
            }
        }

        // now the correct words
        learnedFilePath = Paths.get(projectDir, OConsts.LEARNED_WORD_LIST_FILE_NAME);

        learnedList.clear();
        if (learnedFilePath.toFile().isFile()) {
            try {
                learnedList.addAll(Files.readAllLines(learnedFilePath, StandardCharsets.UTF_8));
                learnedList.forEach(word -> checker.learnWord(word));
            } catch (Exception ex) {
                LOGGER.warn("", ex);
            }
        }
    }

    /**
     * destroy the library
     */
    public void destroy() {
        saveWordLists();
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
        // Write the ignored and learned words to the disk
        try {
            Files.write(ignoreFilePath, ignoreList);
        } catch (IOException ex) {
            LOGGER.warn("", ex);
        }
        try {
            Files.write(learnedFilePath, learnedList);
        } catch (IOException ex) {
            LOGGER.warn("", ex);
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
