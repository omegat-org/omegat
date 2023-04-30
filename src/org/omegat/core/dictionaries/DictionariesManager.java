/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2011 Didier Briel
               2015 Aaron Madlon-Kay
               2021 Hiroshi Miura
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

package org.omegat.core.dictionaries;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.omegat.gui.dictionaries.IDictionaries;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * Class for load dictionaries.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class DictionariesManager implements DirectoryMonitor.Callback {
    public static final String IGNORE_FILE = "ignore.txt";

    private final IDictionaries pane;
    protected DirectoryMonitor monitor;
    protected final List<IDictionaryFactory> factories = new ArrayList<>();
    protected final Map<String, IDictionary> dictionaries = new TreeMap<>();
    protected final List<IDictionary> onlineDictionaries = new ArrayList<>();
    protected final Set<String> ignoreWords = new TreeSet<>();

    private Language indexLanguage;
    private ITokenizer tokenizer;

    public DictionariesManager(final IDictionaries pane) {
        this.pane = pane;
        indexLanguage = new Language(Locale.getDefault());
        tokenizer = new DefaultTokenizer();
    }

    /**
     * Add dictionary factory.
     * @param dict factory to register.
     */
    public void addDictionaryFactory(IDictionaryFactory dict) {
        synchronized (factories) {
            factories.add(dict);
        }
        if (monitor != null) {
            monitor.fin();
            start(monitor.getDir());
        }
    }

    /**
     * Remove dictionary Factory.
     * @param factory factory to unregister.
     */
    public void removeDictionaryFactory(IDictionaryFactory factory) {
        synchronized (factories) {
            factories.remove(factory);
        }
    }

    /**
     * Add online dictionary(dictionary without local data).
     * @param dict dictionary lookup driver.
     */
    public void addOnlineDictionary(IDictionary dict) {
        synchronized (onlineDictionaries) {
            onlineDictionaries.add(dict);
        }
    }

    /**
     * Remove online dictionary.
     * @param dict dictionary lookup driver to remove from registration.
     */
    public void removeOnlineDictionary(IDictionary dict) {
        synchronized (onlineDictionaries) {
            onlineDictionaries.remove(dict);
        }
    }

    public void start(File dictDir) {
        monitor = new DirectoryMonitor(dictDir, this);
        monitor.start();
    }

    public void stop() {
        monitor.fin();
        synchronized (this) {
            dictionaries.values().forEach(this::closeDict);
            dictionaries.clear();
        }
    }

    private void closeDict(IDictionary dict) {
        try {
            dict.close();
        } catch (Exception e) {
            Log.log(e);
        }
    }

    /**
     * Executed on file changed.
     */
    public void fileChanged(File file) {
        synchronized (dictionaries) {
            IDictionary dict = dictionaries.get(file.getPath());
            if (dict != null) {
                closeDict(dict);
            }
            dictionaries.remove(file.getPath());
        }
        if (!file.exists()) {
            return;
        }
        try {
            long st = System.currentTimeMillis();
            if (file.getName().equals(IGNORE_FILE)) {
                loadIgnoreWords(file);
            } else if (loadDictionary(file)) {
                long en = System.currentTimeMillis();
                Log.log("Loaded dictionary from '" + file.getPath() + "': " + (en - st) + "ms");
            }
        } catch (Exception ex) {
            Log.log("Error load dictionary from '" + file.getPath() + "': " + ex.getMessage());
        }
        pane.refresh();
    }

    /**
     * Check all known dictionary factories to see if they support this file.
     * Will stop at the first supporting factory and attempt to load the
     * dictionary.
     *
     * @param file
     *            Dictionary file to be loaded
     * @return Whether or not the file was loaded
     * @throws Exception
     *             Even when a file appears to be supported, exceptions can
     *             still occur while loading.
     */
    private boolean loadDictionary(File file) throws Exception {
        if (!file.isFile()) {
            return false;
        }
        List<IDictionaryFactory> currFactories;
        synchronized (factories) {
            currFactories = new ArrayList<IDictionaryFactory>(factories);
        }
        for (IDictionaryFactory factory : currFactories) {
            if (factory.isSupportedFile(file)) {
                IDictionary dict = factory.loadDict(file, indexLanguage);
                synchronized (this) {
                    dictionaries.put(file.getPath(), dict);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Load ignored words from 'ignore.txt' file.
     */
    protected void loadIgnoreWords(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        synchronized (ignoreWords) {
            ignoreWords.clear();
            lines.stream().map(String::trim).forEach(line -> ignoreWords.add(line));
        }
    }

    /**
     * Add new ignore word.
     */
    public void addIgnoreWord(final String word) {
        Collection<String> words = Collections.emptyList();
        synchronized (ignoreWords) {
            ignoreWords.add(word);
            words = new ArrayList<String>(ignoreWords);
        }
        if (monitor != null) {
            saveIgnoreWords(words, new File(monitor.getDir(), IGNORE_FILE));
        }
    }

    private static void saveIgnoreWords(Collection<String> words, File outFile) {
        try {
            File outFileTmp = new File(outFile.getPath() + ".new");
            Files.write(outFileTmp.toPath(), words);
            outFile.delete();
            FileUtil.rename(outFileTmp, outFile);
        } catch (IOException ex) {
            Log.log("Error saving ignore words");
            Log.log(ex);
        }
    }

    private boolean isIgnoreWord(String word) {
        synchronized (ignoreWords) {
            return ignoreWords.contains(word);
        }
    }

    /**
     * Find words list in all dictionaries.
     *
     * @param words
     *            words list
     * @return articles list
     */
    public List<DictionaryEntry> findWords(Collection<String> words) {
        List<IDictionary> dicts;
        synchronized (this) {
            dicts = new ArrayList<>(dictionaries.values());
            dicts.addAll(onlineDictionaries);
        }
        Collection<String> queryWords = words.stream()
                .filter(word -> !isIgnoreWord(word) && !isStopWord(word))
                .collect(Collectors.toList());
        return dicts.parallelStream()
                .map(dict -> doLookUp(dict, queryWords))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<DictionaryEntry> doLookUp(IDictionary dict, Collection<String> words) {
        List<DictionaryEntry> result;
        try {
            result = dict.retrieveArticles(words);
        } catch (Exception ex) {
            Log.log(ex);
            return Collections.emptyList();
        }
        if (!doFuzzyMatching()) {
            return result;
        }
        // The verbatim word didn't get any hits; try the stem.
        try {
            List<String> notFound = new ArrayList<>(words);
            notFound.removeAll(result.stream().map(DictionaryEntry::getQuery).collect(Collectors.toSet()));
            List<String> predictive = new ArrayList<>();
            for (String word : notFound) {
                String[] stemmed = tokenizer.tokenizeWordsToStrings(word, StemmingMode.MATCHING);
                if (stemmed.length > 1) {
                    predictive.add(stemmed[0]);
                }
            }
            if (predictive.size() > 0) {
                result.addAll(dict.retrieveArticlesPredictive(predictive));
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return result;
    }

    private boolean isStopWord(final String word) {
        String[] stemmed = tokenizer.tokenizeWordsToStrings(word, StemmingMode.MATCHING);
        return stemmed.length == 0;
    }

    public void setIndexLanguage(Language indexLanguage) {
        this.indexLanguage = indexLanguage;
    }

    public void setTokenizer(ITokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public boolean doFuzzyMatching() {
        return Preferences.isPreferenceDefault(Preferences.DICTIONARY_FUZZY_MATCHING, true);
    }

    public String[] getStemmedWords(final String word) {
        return tokenizer.tokenizeWordsToStrings(word, StemmingMode.MATCHING);
    }
}
