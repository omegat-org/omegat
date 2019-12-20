/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

package org.omegat.tokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener.PROJECT_CHANGE_TYPE;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/**
 * Methods for tokenize string.
 *
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { Tokenizer.DISCOVER_AT_RUNTIME })
public class HunspellTokenizer extends BaseTokenizer {

    private static Map<Language, File> affixFiles;
    private static Map<Language, File> dictionaryFiles;
    private static String[] availableDictLangs;

    private volatile Dictionary dict;
    private volatile boolean failedToLoadDict;

    private Dictionary getDict() {
        if (failedToLoadDict) {
            return null;
        }
        Dictionary result = dict;
        if (result == null) {
            synchronized (this) {
                result = dict;
                if (result == null) {
                    result = dict = initDict(getEffectiveLanguage());
                    if (result == null) {
                        failedToLoadDict = true;
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(strOrig));
        if (stemsAllowed) {
            Dictionary dictionary = getDict();
            if (dictionary == null) {
                return tokenizer;
            }

            return new HunspellStemFilter(tokenizer, dictionary);

            /// TODO: implement stop words checks
        } else {
            return tokenizer;
        }
    }

    @Override
    public String[] getSupportedLanguages() {
        populateInstalledDicts();
        return availableDictLangs == null ? new String[0] : availableDictLangs;
    }

    private static synchronized void populateInstalledDicts() {
        if (affixFiles != null && dictionaryFiles != null) {
            return;
        }
        affixFiles = new HashMap<>();
        dictionaryFiles = new HashMap<>();

        String dictionaryDirPath = Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY);
        if (dictionaryDirPath.isEmpty()) {
            return;
        }

        File dictionaryDir = new File(dictionaryDirPath);
        if (!dictionaryDir.isDirectory()) {
            return;
        }

        for (File file : dictionaryDir.listFiles()) {
            String name = file.getName();
            if (name.endsWith(OConsts.SC_AFFIX_EXTENSION)) {
                Language lang = new Language(name.substring(0, name.lastIndexOf(OConsts.SC_AFFIX_EXTENSION)));
                affixFiles.put(lang, file);
                affixFiles.put(new Language(lang.getLanguageCode()), file);
            } else if (name.endsWith(OConsts.SC_DICTIONARY_EXTENSION)) {
                Language lang = new Language(name.substring(0, name.lastIndexOf(OConsts.SC_DICTIONARY_EXTENSION)));
                dictionaryFiles.put(lang, file);
                dictionaryFiles.put(new Language(lang.getLanguageCode()), file);
            }
        }

        Set<Language> commonLangs = new HashSet<>(affixFiles.keySet());
        commonLangs.retainAll(dictionaryFiles.keySet());
        availableDictLangs = langsToStrings(commonLangs);
    }

    private static Dictionary initDict(Language language) {
        populateInstalledDicts();

        File affixFile;
        File dictionaryFile;
        synchronized (HunspellTokenizer.class) {
            affixFile = affixFiles.get(language);
            dictionaryFile = dictionaryFiles.get(language);
        }

        if (affixFile == null || dictionaryFile == null || !affixFile.exists() || !dictionaryFile.exists()) {
            Log.logErrorRB("HUNSPELL_TOKENIZER_DICT_NOT_INSTALLED", language.getLocale());
            return null;
        }
        try {
            return new Dictionary(new FileInputStream(affixFile), new FileInputStream(dictionaryFile));
        } catch (Throwable t) {
            Log.log(t);
            return null;
        }
    }

    private static String[] langsToStrings(Set<Language> langs) {
        List<String> result = new ArrayList<String>(langs.size() * 2);
        for (Language lang : langs) {
            result.add(lang.getLanguage().toLowerCase(Locale.ENGLISH));
            result.add(lang.getLanguageCode().toLowerCase(Locale.ENGLISH));
        }
        return result.toArray(new String[result.size()]);
    }

    private static synchronized void reset() {
        affixFiles = null;
        dictionaryFiles = null;
        availableDictLangs = null;
    }

    public static void loadPlugins() {
        Core.registerTokenizerClass(HunspellTokenizer.class);
        CoreEvents.registerProjectChangeListener(e -> {
            if (e == PROJECT_CHANGE_TYPE.LOAD) {
                reset();
            }
        });
    }

    public static void unloadPlugins() {
    }
}
