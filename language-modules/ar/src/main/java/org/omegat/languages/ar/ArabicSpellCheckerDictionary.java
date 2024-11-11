package org.omegat.languages.ar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.lucene.analysis.hunspell.Dictionary;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

public class ArabicSpellCheckerDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/ar/hunspell/";

    private InputStream affixInputStream;
    private InputStream dictInputStream;

    @Override
    public Dictionary getHunspellDictionary() {
        affixInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "ar.aff");
        dictInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "ar.dic");
        return new Dictionary(affixInputStream, Collections.singletonList(dictInputStream), true);
    }

    @Override
    public SpellCheckDictionaryType getDictionaryType() {
        return SpellCheckDictionaryType.HUNSPELL;
    }

    @Override
    public void close() {
        try {
            affixInputStream.close();
            dictInputStream.close();
        } catch (IOException ignored) {}
    }
}
