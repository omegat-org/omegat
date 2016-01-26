/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2011 Didier Briel
               2015 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.omegat.core.Core;
import org.omegat.gui.dictionaries.IDictionaries;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;

/**
 * Class for load dictionaries.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class DictionariesManager implements DirectoryMonitor.Callback {
    public static final String IGNORE_FILE = "ignore.txt";
    public static final String DICTIONARY_SUBDIR = "dictionary";

    private final IDictionaries pane;
    protected DirectoryMonitor monitor;
    protected final List<IDictionaryFactory> factories = new ArrayList<IDictionaryFactory>();
    protected final Map<String, IDictionary> dictionaries = new TreeMap<String, IDictionary>();
    protected final Set<String> ignoreWords = new TreeSet<String>();

    public DictionariesManager(final IDictionaries pane) {
        this.pane = pane;
        factories.add(new LingvoDSL());
        factories.add(new StarDict());
    }

    public void addDictionaryFactory(IDictionaryFactory dict) {
        synchronized (factories) {
            factories.add(dict);
        }
        if (monitor != null) {
            monitor.fin();
            start(monitor.getDir());
        }
    }

    public void removeDictionaryFactory(IDictionaryFactory factory) {
        synchronized (factories) {
            factories.remove(factory);
        }
    }

    public void start(File dictDir) {
        monitor = new DirectoryMonitor(dictDir, this);
        monitor.start();
    }

    public void stop() {
        monitor.fin();
        synchronized (this) {
            dictionaries.clear();
        }
    }

    /**
     * Executed on file changed.
     */
    public void fileChanged(File file) {
        synchronized (dictionaries) {
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
                synchronized (this) {
                    dictionaries.put(file.getPath(), factory.loadDict(file));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Load ignored words from 'ignore.txt' file.
     */
    protected void loadIgnoreWords(final File f) throws IOException {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader rd = null;
        try {
            fis = new FileInputStream(f);
            isr = new InputStreamReader(fis, OConsts.UTF8);
            rd = new BufferedReader(isr);
            synchronized (ignoreWords) {
                ignoreWords.clear();
                String line;
                while ((line = rd.readLine()) != null) {
                    ignoreWords.add(line.trim());
                }
            }
            rd.close();
            isr.close();
            fis.close();
        } finally {
            IOUtils.closeQuietly(rd);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(fis);
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
        saveIgnoreWords(words);
    }
    
    private synchronized void saveIgnoreWords(Collection<String> words) {
        if (monitor == null) {
            Log.log("Could not save ignore words because no dictionary dir has been set.");
            return;
        }

        File outFile = new File(monitor.getDir(), IGNORE_FILE);
        File outFileTmp = new File(monitor.getDir(), IGNORE_FILE + ".new");

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter wr = null;
        try {
            fos = new FileOutputStream(outFileTmp);
            osw = new OutputStreamWriter(fos, OConsts.UTF8);
            wr = new BufferedWriter(osw);
            for (String w : words) {
                wr.write(w + System.getProperty("line.separator"));
            }
            outFile.delete();
            FileUtil.rename(outFileTmp, outFile);
            wr.close();
            osw.close();
            fos.close();
        } catch (Exception ex) {
            Log.log("Error saving ignore words: " + ex.getMessage());
        } finally {
            IOUtils.closeQuietly(wr);
            IOUtils.closeQuietly(osw);
            IOUtils.closeQuietly(fos);
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
            dicts = new ArrayList<IDictionary>(dictionaries.values());
        }
        List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
        for (String word : words) {
            for (IDictionary di : dicts) {
                if (isIgnoreWord(word)) {
                    continue;
                }
                try {
                    List<DictionaryEntry> entries = di.readArticles(word);
                    if (entries.isEmpty()) {
                        Locale loc = Core.getProject().getProjectProperties().getSourceLanguage().getLocale();
                        String lowerCaseWord = word.toLowerCase(loc);
                        if (isIgnoreWord(lowerCaseWord)) {
                            continue;
                        }
                        entries = di.readArticles(lowerCaseWord);
                    }
                    result.addAll(entries);
                } catch (Exception ex) {
                    Log.log(ex);
                }
            }
        }
        return result;
    }
}
