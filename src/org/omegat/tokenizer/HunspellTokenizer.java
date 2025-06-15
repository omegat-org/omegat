/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2024 Hiroshi Miura
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

package org.omegat.tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;

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

    protected Dictionary getDict() {
        if (failedToLoadDict) {
            return null;
        }
        Dictionary result = dict;
        if (result == null) {
            synchronized (this) {
                result = dict;
                if (result == null) {
                    result = initDict(getEffectiveLanguage());
                    dict = result;
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
            CharArraySet stopWords;
            if (stopWordsAllowed) {
                stopWords = getEffectiveStopWordSet();
            } else {
                stopWords = CharArraySet.EMPTY_SET;
            }
            return new StopFilter(new HunspellStemFilter(tokenizer, dictionary), stopWords);
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

        var fileList = dictionaryDir.listFiles();
        if (fileList == null) {
            return;
        }

        for (File file : fileList) {
            String name = file.getName();
            if (name.endsWith(OConsts.SC_AFFIX_EXTENSION)) {
                Language lang = new Language(name.substring(0, name.lastIndexOf(OConsts.SC_AFFIX_EXTENSION)));
                affixFiles.put(lang, file);
                affixFiles.put(new Language(lang.getLanguageCode()), file);
            } else if (name.endsWith(OConsts.SC_DICTIONARY_EXTENSION)) {
                Language lang = new Language(
                        name.substring(0, name.lastIndexOf(OConsts.SC_DICTIONARY_EXTENSION)));
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
        return result.toArray(new String[0]);
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

    private static final String STOPWORDS_COMMENT = "#";
    private static final String STOPWORDS_BASE_DIR = "stopwords/";

    private CharArraySet getEffectiveStopWordSet() {
        String language = getEffectiveLanguage().getLanguageCode();
        String country = getEffectiveLanguage().getCountryCode();
        switch (language) {
        case "ar":
            return ArabicAnalyzer.getDefaultStopSet();
        case "hy":
            return ArmenianAnalyzer.getDefaultStopSet();
        case "eu":
            return BasqueAnalyzer.getDefaultStopSet();
        case "es":
            if (country.equals("BR")) {
                return BrazilianAnalyzer.getDefaultStopSet();
            } else {
                return SpanishAnalyzer.getDefaultStopSet();
            }
        case "bg":
            return BulgarianAnalyzer.getDefaultStopSet();
        case "ca":
            return CatalanAnalyzer.getDefaultStopSet();
        case "cs":
            return CzechAnalyzer.getDefaultStopSet();
        case "da":
            return DanishAnalyzer.getDefaultStopSet();
        case "nl":
            return DutchAnalyzer.getDefaultStopSet();
        case "en":
            return EnglishAnalyzer.getDefaultStopSet();
        case "fi":
            return FinnishAnalyzer.getDefaultStopSet();
        case "fr":
            return FrenchAnalyzer.getDefaultStopSet();
        case "gl":
            return GalicianAnalyzer.getDefaultStopSet();
        case "de":
            return GermanAnalyzer.getDefaultStopSet();
        case "el":
            return GreekAnalyzer.getDefaultStopSet();
        case "hi":
            return HindiAnalyzer.getDefaultStopSet();
        case "hu":
            return HungarianAnalyzer.getDefaultStopSet();
        case "id":
            return IndonesianAnalyzer.getDefaultStopSet();
        case "ga":
            return IrishAnalyzer.getDefaultStopSet();
        case "it":
            return ItalianAnalyzer.getDefaultStopSet();
        case "lv":
            return LatvianAnalyzer.getDefaultStopSet();
        case "nb":
            return NorwegianAnalyzer.getDefaultStopSet();
        case "fa":
            return PersianAnalyzer.getDefaultStopSet();
        case "pl":
            return PolishAnalyzer.getDefaultStopSet();
        case "pt":
            return PortugueseAnalyzer.getDefaultStopSet();
        case "ro":
            return RomanianAnalyzer.getDefaultStopSet();
        case "ru":
            return RussianAnalyzer.getDefaultStopSet();
        case "sv":
            return SwedishAnalyzer.getDefaultStopSet();
        case "th":
            return ThaiAnalyzer.getDefaultStopSet();
        case "tr":
            return TurkishAnalyzer.getDefaultStopSet();
        case "ja":
            return JapaneseAnalyzer.getDefaultStopSet();
        default:
            return loadStopwordSet(STOPWORDS_BASE_DIR + language + "/stopwords-" + language + ".txt"
            );
        }
    }

    private CharArraySet loadStopwordSet(String resource) {
        try (InputStream is = HunspellTokenizer.class.getResourceAsStream(resource)) {
            if (is != null) {
                try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return WordlistLoader.getWordSet(reader, STOPWORDS_COMMENT, new CharArraySet(16, true));
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return CharArraySet.EMPTY_SET;
    }
}
