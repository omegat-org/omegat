/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

package org.omegat.tokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.omegat.core.Core;
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

    private static Map<Language, File> AFFIX_FILES;
    private static Map<Language, File> DICTIONARY_FILES;
    
    private HunspellDictionary dict;
    
    public HunspellDictionary getDict() {
        if (dict != null) {
            return dict;
        }
        
        Language language;
        if (Core.getProject().getSourceTokenizer() == this)
            language = Core.getProject().getProjectProperties().getSourceLanguage();
        else
            language = Core.getProject().getProjectProperties().getTargetLanguage();
        
        if (AFFIX_FILES == null || DICTIONARY_FILES == null) {
            populateInstalledDicts();
        }
        
        File affixFile = AFFIX_FILES.get(language);
        File dictionaryFile = DICTIONARY_FILES.get(language);
        
        if (affixFile == null || dictionaryFile == null
                || !affixFile.exists() || !dictionaryFile.exists()) {
            Log.logErrorRB("HUNSPELL_TOKENIZER_DICT_NOT_INSTALLED", language.getLocale());
        }
        
        try {
            dict = new HunspellDictionary(new FileInputStream(affixFile),
                    new FileInputStream(dictionaryFile),
                    getBehavior());
            return dict;
        } catch (Exception ex) {
            // Nothing
        }   
        return null;
    }
    
    @Override
    protected TokenStream getTokenStream(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed) {
        if (stemsAllowed) {
            HunspellDictionary dictionary = getDict();
            if (dictionary == null) {
                return new StandardTokenizer(getBehavior(),
                    new StringReader(strOrig.toLowerCase()));
            }
            
            return new HunspellStemFilter(new StandardTokenizer(getBehavior(),
                    new StringReader(strOrig.toLowerCase())), dictionary);
            
            /// TODO: implement stop words checks
        } else {
            return new StandardTokenizer(getBehavior(),
                    new StringReader(strOrig.toLowerCase()));
        }
    }
    
    @Override
    public String[] getSupportedLanguages() {
        
        populateInstalledDicts();
        
        Set<Language> commonLangs = AFFIX_FILES.keySet();
        commonLangs.retainAll(DICTIONARY_FILES.keySet());
        
        return langsToStrings(commonLangs);
    }
    
    private static void populateInstalledDicts() {
        AFFIX_FILES = new HashMap<Language, File>();
        DICTIONARY_FILES = new HashMap<Language, File>();
        
        String dictionaryDirPath = Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY);
        if (dictionaryDirPath.isEmpty()) {
            return;
        }
        
        File dictionaryDir = new File(dictionaryDirPath);
        if (!dictionaryDir.exists() || !dictionaryDir.isDirectory()) {
            return;
        }
        
        for (File file : dictionaryDir.listFiles()) {
            String name = file.getName();
            if (name.endsWith(OConsts.SC_AFFIX_EXTENSION)) {
                Language lang = new Language(name.substring(0, name.lastIndexOf(OConsts.SC_AFFIX_EXTENSION)));
                AFFIX_FILES.put(lang, file);
                AFFIX_FILES.put(new Language(lang.getLanguageCode()), file);
            } else if (name.endsWith(OConsts.SC_DICTIONARY_EXTENSION)) {
                Language lang = new Language(name.substring(0, name.lastIndexOf(OConsts.SC_DICTIONARY_EXTENSION)));
                DICTIONARY_FILES.put(lang, file);
                DICTIONARY_FILES.put(new Language(lang.getLanguageCode()), file);
            }
            
        }
    }
    
    private static String[] langsToStrings(Set<Language> langs) {
        List<String> result = new ArrayList<String>();
        for (Language lang : langs) {
            result.add(lang.getLanguage().toLowerCase());
            result.add(lang.getLanguageCode().toLowerCase());
        }
        return result.toArray(new String[0]);
    }
}
