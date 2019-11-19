/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko, Alex Buloichik
               2009 Didier Briel
               2015 Aaron Madlon-Kay
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

package org.omegat.core.spellchecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.languagetool.JLanguageTool;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * Common spell checker interface for use any spellchecker providers.
 *
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class SpellChecker implements ISpellChecker {

    public static final File DEFAULT_DICTIONARY_DIR = new File(StaticUtils.getConfigDir(),
            OConsts.SPELLING_DICT_DIR);

    /** The spell checking provider. */
    private ISpellCheckerProvider checker;

    /** the list of ignored words */
    private List<String> ignoreList = new ArrayList<String>();

    /** the list of learned (valid) words */
    private List<String> learnedList = new ArrayList<String>();

    /** Cache of correct words. */
    private final Set<String> correctWordsCache = new HashSet<String>();
    /** Cache of incorrect words. */
    private final Set<String> incorrectWordsCache = new HashSet<String>();

    /**
     * the file name with the ignored words
     */
    private Path ignoreFilePath;

    /**
     * the file name with the learned words
     */
    private Path learnedFilePath;

    /** Creates a new instance of SpellChecker */
    public SpellChecker() {
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
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
            }
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

        checker = toCheck.map(SpellChecker::initializeWithLanguage).filter(Optional::isPresent).findFirst()
                .orElseGet(() -> Optional.of(new SpellCheckerDummy())).get();

        if (checker instanceof SpellCheckerDummy) {
            Log.log("No spell checker found for language " + targetLanguage);
        }

        loadWordLists();
    }

    private static Optional<ISpellCheckerProvider> initializeWithLanguage(String language) {
        // initialize the spell checker - get the data from the preferences

        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                DEFAULT_DICTIONARY_DIR.getPath());

        File dictBasename = new File(dictionaryDir, language);
        File affixName = new File(dictionaryDir, language + OConsts.SC_AFFIX_EXTENSION);
        File dictionaryName = new File(dictionaryDir, language + OConsts.SC_DICTIONARY_EXTENSION);

        if (!dictionaryName.exists()) {
            // Try installing from bundled resources
            installBundledDictionary(dictionaryDir, language);
        }

        if (!dictionaryName.exists()) {
            // Try installing from LanguageTool bundled resources
            installLTBundledDictionary(dictionaryDir, language);
        }

        if (!isValidFile(affixName) || !isValidFile(dictionaryName)) {
            // If we still don't have a dictionary then return
            return Optional.empty();
        }

        try {
            ISpellCheckerProvider result = new SpellCheckerLangToolHunspell(dictBasename.getPath());
            Log.log("Initialized LanguageTool Hunspell spell checker for language '" + language
                    + "' dictionary " + dictionaryName);
            return Optional.of(result);
        } catch (Throwable ex) {
            Log.log("Error loading hunspell: " + ex.getMessage());
        }
        try {
            ISpellCheckerProvider result = new SpellCheckerJMySpell(dictionaryName.getPath(),
                    affixName.getPath());
            Log.log("Initialized JMySpell spell checker for language '" + language + "' dictionary "
                    + dictionaryName);
            return Optional.of(result);
        } catch (Exception ex) {
            Log.log("Error loading jmyspell: " + ex.getMessage());
        }
        return Optional.empty();
    }

    private static boolean isValidFile(File file) {
        try {
            if (!file.exists()) {
                return false;
            }
            if (!file.isFile()) {
                Log.log("Spelling dictionary exists but is not a file: " + file.getPath());
                return false;
            }
            if (!file.canRead()) {
                Log.log("Can't read spelling dictionary: " + file.getPath());
                return false;
            }
            if (file.length() == 0L) {
                // On OS X, attempting to load Hunspell with a zero-length .dic file causes
                // a native exception that crashes the whole program.
                Log.log("Spelling dictionary appears to be empty: " + file.getPath());
                return false;
            }
            return true;
        } catch (Throwable ex) {
            Log.log(ex);
            return false;
        }
    }

    /**
     * If there is a Hunspell dictionary for the current target language bundled
     * inside this OmegaT distribution, install it.
     */
    private static void installBundledDictionary(String dictionaryDir, String language) {
        try (InputStream bundledDict = SpellChecker.class.getResourceAsStream(language + ".zip")) {
            if (bundledDict == null) {
                // Relevant dictionary not present.
                return;
            }
            StaticUtils.extractFromZip(bundledDict, new File(dictionaryDir),
                    Arrays.asList(language + OConsts.SC_AFFIX_EXTENSION,
                            language + OConsts.SC_DICTIONARY_EXTENSION)::contains);
        } catch (IOException e) {
            Log.log(e);
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
            Log.log(ex);
        }
    }

    private void loadWordLists() {
        // find out the internal project directory
        String projectDir = Core.getProject().getProjectProperties().getProjectInternal();

        // load the ignore list
        ignoreFilePath = Paths.get(projectDir, OConsts.IGNORED_WORD_LIST_FILE_NAME);

        ignoreList.clear();
        if (ignoreFilePath.toFile().isFile()) {
            try {
                ignoreList.addAll(Files.readAllLines(ignoreFilePath, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                Log.log(ex);
            }
        }

        // now the correct words
        learnedFilePath = Paths.get(projectDir, OConsts.LEARNED_WORD_LIST_FILE_NAME);

        learnedList.clear();
        if (learnedFilePath.toFile().isFile()) {
            try {
                learnedList.addAll(Files.readAllLines(learnedFilePath, StandardCharsets.UTF_8));
                learnedList.stream().forEach(word -> checker.learnWord(word));
            } catch (Exception ex) {
                Log.log(ex);
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
            Log.log(ex);
        }
        try {
            Files.write(learnedFilePath, learnedList);
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    /**
     * Check the word. If it is ignored or learned (valid), returns true. Otherwise false.
     */
    public boolean isCorrect(String word) {
        // check if spellchecker is already initialized. If not, skip checking
        // to prevent nullPointerErrors.
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
     * Normalize the orthography of the word by replacing alternative characters with "canonical" ones.
     */
    private static String normalize(String word) {
        // U+2019 RIGHT SINGLE QUOTATION MARK to U+0027 APOSTROPHE
        return word.replace('\u2019', '\'');
    }

    @Override
    public List<Token> getMisspelledTokens(String text) {
        return Stream.of(Core.getProject().getTargetTokenizer().tokenizeWords(text, StemmingMode.NONE))
                .filter(tok -> !isCorrect(tok.getTextFromString(text))).collect(Collectors.toList());
    }
}
